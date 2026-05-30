package com.bukuro.client;

import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class OpenBdApiClient {

    private static final String OPENBD_API_URL = "https://api.openbd.jp/v1/get";
    private static final String EXPECTED_COVER_DOMAIN = "https://cover.openbd.jp/";

    private final RestClient restClient;

    public OpenBdApiClient(@Value("${openbd.api.timeout-seconds:3}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(OPENBD_API_URL)
                .build();
    }

    public Optional<BookDto> searchByIsbn(String isbn) {
        try {
            List<OpenBdResponse> responses = restClient.get()
                    .uri("?isbn={isbn}", isbn)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});

            if (responses == null || responses.isEmpty() || responses.get(0) == null) {
                return Optional.empty();
            }

            OpenBdResponse response = responses.get(0);
            if (response.getSummary() == null) {
                return Optional.empty();
            }

            return Optional.of(toBookDto(response));

        } catch (ResourceAccessException e) {
            // タイムアウトまたは接続エラー
            log.warn("OpenBD API request failed: {}", e.getMessage());
            throw new ExternalApiException("書誌情報の取得に失敗しました。しばらく後で再試行してください", e);
        } catch (Exception e) {
            log.error("Unexpected error calling OpenBD API", e);
            throw new ExternalApiException("書誌情報の取得中に予期しないエラーが発生しました", e);
        }
    }

    private BookDto toBookDto(OpenBdResponse response) {
        Summary summary = response.getSummary();
        return BookDto.builder()
                .isbn(summary.getIsbn())
                .title(summary.getTitle())
                .author(summary.getAuthor())
                .publisher(summary.getPublisher())
                .coverUrl(isValidCoverUrl(summary.getCover()) ? summary.getCover() : null)
                .build();
    }

    private static boolean isValidCoverUrl(String url) {
        return url != null && url.startsWith(EXPECTED_COVER_DOMAIN);
    }

    // OpenBD APIレスポンスのデシリアライズ用内部クラス

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OpenBdResponse {
        private Summary summary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Summary {
        private String isbn;
        private String title;
        private String author;
        private String publisher;
        // OpenBD summary.cover フィールドが書影URL
        private String cover;
    }
}
