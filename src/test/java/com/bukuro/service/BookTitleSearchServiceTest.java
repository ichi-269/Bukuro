package com.bukuro.service;

import com.bukuro.client.NdlApiClient;
import com.bukuro.client.OpenBdApiClient;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookTitleSearchServiceTest {

    @Mock
    private NdlApiClient ndlApiClient;

    @Mock
    private OpenBdApiClient openBdApiClient;

    @InjectMocks
    private BookTitleSearchService bookTitleSearchService;

    private BookDto ndlBook(String isbn) {
        return BookDto.builder()
                .isbn(isbn)
                .title("NDLタイトル")
                .author("NDL著者")
                .publisher("NDL出版社")
                .coverUrl(null)
                .build();
    }

    private BookDto openBdBook(String isbn) {
        return BookDto.builder()
                .isbn(isbn)
                .title("OpenBDタイトル")
                .author("OpenBD著者")
                .publisher("OpenBD出版社")
                .coverUrl("https://cover.openbd.jp/" + isbn + ".jpg")
                .build();
    }

    @Test
    @DisplayName("searchByTitle は NDL クライアントに委譲して結果を返す")
    void searchByTitle_delegatesToNdlClient() {
        List<BookDto> expected = List.of(ndlBook("9784000000011"));
        when(ndlApiClient.searchByTitle("テスト")).thenReturn(expected);

        List<BookDto> result = bookTitleSearchService.searchByTitle("テスト");

        assertThat(result).isEqualTo(expected);
        verify(ndlApiClient).searchByTitle("テスト");
    }

    @Test
    @DisplayName("NDL APIがExternalApiExceptionを投げると searchByTitle から伝播する")
    void searchByTitle_ndlApiThrows_propagatesException() {
        when(ndlApiClient.searchByTitle(anyString()))
                .thenThrow(new ExternalApiException("タイムアウト"));

        assertThatThrownBy(() -> bookTitleSearchService.searchByTitle("テスト"))
                .isInstanceOf(ExternalApiException.class);
    }

    @Test
    @DisplayName("getBookWithCover: OpenBDにデータあり → OpenBDのデータを返す")
    void getBookWithCover_openBdHasData_returnsOpenBdBook() {
        BookDto openBd = openBdBook("9784000000011");
        when(openBdApiClient.searchByIsbn("9784000000011")).thenReturn(Optional.of(openBd));

        BookDto result = bookTitleSearchService.getBookWithCover(
                "9784000000011", "NDLタイトル", "NDL著者", "NDL出版社");

        assertThat(result.getCoverUrl()).isEqualTo("https://cover.openbd.jp/9784000000011.jpg");
        assertThat(result.getTitle()).isEqualTo("OpenBDタイトル");
    }

    @Test
    @DisplayName("getBookWithCover: OpenBDにデータなし → NDLのデータを使う")
    void getBookWithCover_openBdEmpty_returnsNdlData() {
        when(openBdApiClient.searchByIsbn("9784000000011")).thenReturn(Optional.empty());

        BookDto result = bookTitleSearchService.getBookWithCover(
                "9784000000011", "NDLタイトル", "NDL著者", "NDL出版社");

        assertThat(result.getTitle()).isEqualTo("NDLタイトル");
        assertThat(result.getAuthor()).isEqualTo("NDL著者");
        assertThat(result.getPublisher()).isEqualTo("NDL出版社");
        assertThat(result.getCoverUrl()).isNull();
    }

    @Test
    @DisplayName("getBookWithCover: OpenBD APIが失敗 → NDLのデータにフォールバック")
    void getBookWithCover_openBdThrows_fallsBackToNdlData() {
        when(openBdApiClient.searchByIsbn("9784000000011"))
                .thenThrow(new ExternalApiException("接続エラー"));

        BookDto result = bookTitleSearchService.getBookWithCover(
                "9784000000011", "NDLタイトル", "NDL著者", "NDL出版社");

        assertThat(result.getTitle()).isEqualTo("NDLタイトル");
        assertThat(result.getCoverUrl()).isNull();
    }
}
