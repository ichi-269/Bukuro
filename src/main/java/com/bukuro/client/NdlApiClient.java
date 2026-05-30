package com.bukuro.client;

import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NdlApiClient {

    private static final String NDL_API_URL = "https://ndlsearch.ndl.go.jp/api/opensearch";
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final int MAX_RESULTS = 20;

    private final RestClient restClient;

    public NdlApiClient(@Value("${ndl.api.timeout-seconds:3}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(NDL_API_URL)
                .build();
    }

    public List<BookDto> searchByTitle(String keyword) {
        String xml;
        try {
            xml = restClient.get()
                    .uri("?title={title}&cnt={cnt}", keyword, MAX_RESULTS)
                    .retrieve()
                    .body(String.class);
        } catch (ResourceAccessException e) {
            log.warn("NDL API request failed: {}", e.getMessage());
            throw new ExternalApiException("NDL APIへの接続に失敗しました。しばらく後で再試行してください", e);
        } catch (Exception e) {
            log.error("Unexpected error calling NDL API", e);
            throw new ExternalApiException("NDL APIの呼び出し中に予期しないエラーが発生しました", e);
        }

        if (xml == null || xml.isBlank()) {
            return List.of();
        }

        try {
            return parseXml(xml);
        } catch (Exception e) {
            log.error("Failed to parse NDL API response", e);
            return List.of();
        }
    }

    List<BookDto> parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList items = doc.getElementsByTagName("item");
        List<BookDto> results = new ArrayList<>();

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            if (!isBook(item)) {
                continue;
            }

            String isbn = extractIsbn(item);
            if (isbn == null) {
                continue;
            }

            String title = getElementText(item, null, "title");
            String author = getElementText(item, DC_NAMESPACE, "creator");
            String publisher = getElementText(item, DC_NAMESPACE, "publisher");

            results.add(BookDto.builder()
                    .isbn(isbn)
                    .title(title)
                    .author(author)
                    .publisher(publisher)
                    .coverUrl(null)
                    .build());
        }

        return results;
    }

    private boolean isBook(Element item) {
        NodeList categories = item.getElementsByTagName("category");
        for (int i = 0; i < categories.getLength(); i++) {
            if ("図書".equals(categories.item(i).getTextContent().trim())) {
                return true;
            }
        }
        return false;
    }

    private String extractIsbn(Element item) {
        NodeList identifiers = item.getElementsByTagNameNS(DC_NAMESPACE, "identifier");
        String fallbackIsbn = null;

        for (int i = 0; i < identifiers.getLength(); i++) {
            Element el = (Element) identifiers.item(i);
            String type = el.getAttributeNS(XSI_NAMESPACE, "type");
            String value = el.getTextContent().replaceAll("[-\\s]", "").trim();

            if (value.isEmpty()) {
                continue;
            }

            if (type.contains("ISBN13") && value.matches("\\d{13}")) {
                return value;
            }
            if (type.contains("ISBN") && value.matches("\\d{10,13}") && fallbackIsbn == null) {
                fallbackIsbn = value;
            }
        }

        return fallbackIsbn;
    }

    private String getElementText(Element parent, String namespaceUri, String localName) {
        NodeList nodes = namespaceUri != null
                ? parent.getElementsByTagNameNS(namespaceUri, localName)
                : parent.getElementsByTagName(localName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }
}
