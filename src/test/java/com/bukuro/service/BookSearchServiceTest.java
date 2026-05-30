package com.bukuro.service;

import com.bukuro.client.OpenBdApiClient;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.BookNotFoundException;
import com.bukuro.exception.ExternalApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private OpenBdApiClient openBdApiClient;

    @InjectMocks
    private BookSearchService bookSearchService;

    @Test
    @DisplayName("有効なISBNで searchByIsbn を呼ぶと BookDto が返る")
    void searchByIsbn_validIsbn_returnsBookDto() {
        // Given
        BookDto expected = BookDto.builder()
                .isbn("9784774192178")
                .title("テスト駆動開発")
                .author("Kent Beck")
                .publisher("オーム社")
                .coverUrl("https://cover.openbd.jp/9784774192178.jpg")
                .build();
        when(openBdApiClient.searchByIsbn("9784774192178")).thenReturn(Optional.of(expected));

        // When
        BookDto result = bookSearchService.searchByIsbn("9784774192178");

        // Then
        assertThat(result.getIsbn()).isEqualTo("9784774192178");
        assertThat(result.getTitle()).isEqualTo("テスト駆動開発");
    }

    @Test
    @DisplayName("ハイフン付きISBNは正規化されてAPIに渡される")
    void searchByIsbn_isbnWithHyphens_normalizesBeforeSearch() {
        // Given
        BookDto expected = BookDto.builder().isbn("9784774192178").title("本").author("著者").build();
        when(openBdApiClient.searchByIsbn("9784774192178")).thenReturn(Optional.of(expected));

        // When
        bookSearchService.searchByIsbn("978-4-7741-9217-8");

        // Then
        verify(openBdApiClient).searchByIsbn("9784774192178");
    }

    @Test
    @DisplayName("OpenBDに存在しないISBNで searchByIsbn を呼ぶと BookNotFoundException が発生する")
    void searchByIsbn_notFoundIsbn_throwsBookNotFoundException() {
        // Given
        when(openBdApiClient.searchByIsbn("9999999999999")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookSearchService.searchByIsbn("9999999999999"))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    @DisplayName("OpenBD APIがタイムアウトした場合 ExternalApiException が伝播する")
    void searchByIsbn_apiTimeout_propagatesExternalApiException() {
        // Given
        when(openBdApiClient.searchByIsbn(anyString()))
                .thenThrow(new ExternalApiException("タイムアウト"));

        // When / Then
        assertThatThrownBy(() -> bookSearchService.searchByIsbn("9784774192178"))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("タイムアウト");
    }

    @Test
    @DisplayName("normalizeIsbn はハイフンとスペースを除去する")
    void normalizeIsbn_removesHyphensAndSpaces() {
        assertThat(bookSearchService.normalizeIsbn("978-4-7741-9217-8")).isEqualTo("9784774192178");
        assertThat(bookSearchService.normalizeIsbn("978 4774192178")).isEqualTo("9784774192178");
        assertThat(bookSearchService.normalizeIsbn("9784774192178")).isEqualTo("9784774192178");
    }
}
