package com.bukuro.client;

import com.bukuro.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NdlApiClientTest {

    private NdlApiClient client;

    @BeforeEach
    void setUp() {
        client = new NdlApiClient(3);
    }

    private String buildRss(String... items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\"\n");
        sb.append("  xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n");
        sb.append("  xmlns:dcndl=\"http://ndl.go.jp/dcndl/terms/\"\n");
        sb.append("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("<channel>\n");
        for (String item : items) {
            sb.append(item);
        }
        sb.append("</channel>\n</rss>");
        return sb.toString();
    }

    private String bookItem(String title, String author, String publisher, String isbn13) {
        return "<item>\n"
                + "  <title>" + title + "</title>\n"
                + "  <category>図書</category>\n"
                + "  <dc:creator>" + author + "</dc:creator>\n"
                + "  <dc:publisher>" + publisher + "</dc:publisher>\n"
                + "  <dc:identifier xsi:type=\"dcndl:ISBN13\">" + isbn13 + "</dc:identifier>\n"
                + "</item>\n";
    }

    @Test
    @DisplayName("図書アイテムが正しくパースされる")
    void parseXml_bookItem_extractsFields() throws Exception {
        String xml = buildRss(bookItem("人月の神話", "フレデリック・ブルックス", "丸善出版", "978-4-621-06631-6"));

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).hasSize(1);
        BookDto book = results.get(0);
        assertThat(book.getTitle()).isEqualTo("人月の神話");
        assertThat(book.getAuthor()).isEqualTo("フレデリック・ブルックス");
        assertThat(book.getPublisher()).isEqualTo("丸善出版");
        assertThat(book.getIsbn()).isEqualTo("9784621066316");
        assertThat(book.getCoverUrl()).isNull();
    }

    @Test
    @DisplayName("categoryが図書でないアイテムは除外される")
    void parseXml_nonBookItem_isExcluded() throws Exception {
        String magazine = "<item>\n"
                + "  <title>週刊雑誌</title>\n"
                + "  <category>雑誌</category>\n"
                + "  <dc:identifier xsi:type=\"dcndl:ISBN13\">9784000000001</dc:identifier>\n"
                + "</item>\n";
        String xml = buildRss(magazine);

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("ISBNのないアイテムは除外される")
    void parseXml_itemWithoutIsbn_isExcluded() throws Exception {
        String noIsbn = "<item>\n"
                + "  <title>ISBN無し本</title>\n"
                + "  <category>図書</category>\n"
                + "  <dc:identifier xsi:type=\"dcndl:NDLBibID\">000001234</dc:identifier>\n"
                + "</item>\n";
        String xml = buildRss(noIsbn);

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("ISBNのハイフンは除去される")
    void parseXml_hyphenatedIsbn_stripsHyphens() throws Exception {
        String xml = buildRss(bookItem("テスト本", "著者", "出版社", "978-4-621-06631-6"));

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIsbn()).isEqualTo("9784621066316");
    }

    @Test
    @DisplayName("複数アイテムが全て返る")
    void parseXml_multipleItems_returnsAll() throws Exception {
        String xml = buildRss(
                bookItem("本A", "著者A", "出版A", "9784000000011"),
                bookItem("本B", "著者B", "出版B", "9784000000022")
        );

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(BookDto::getTitle).containsExactly("本A", "本B");
    }

    @Test
    @DisplayName("空のchannelでは空リストが返る")
    void parseXml_emptyChannel_returnsEmptyList() throws Exception {
        String xml = buildRss();

        List<BookDto> results = client.parseXml(xml);

        assertThat(results).isEmpty();
    }
}
