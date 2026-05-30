package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import com.bukuro.service.BookSearchService;
import com.bukuro.service.BookTitleSearchService;
import com.bukuro.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookSearchController.class)
@Import(SecurityConfig.class)
class BookSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchService bookSearchService;

    @MockBean
    private BookTitleSearchService bookTitleSearchService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor LOGGED_IN =
            SecurityMockMvcRequestPostProcessors.user("user@example.com").roles("USER");

    private BookDto sampleNdlBook() {
        return BookDto.builder()
                .isbn("9784000000011")
                .title("テスト駆動開発")
                .author("Kent Beck")
                .publisher("オーム社")
                .coverUrl(null)
                .build();
    }

    @Test
    @DisplayName("POST /books/search/title: 正常な検索で title-results ビューと候補が返る")
    void searchByTitle_validKeyword_returnsTitleResults() throws Exception {
        when(bookTitleSearchService.searchByTitle("テスト駆動開発"))
                .thenReturn(List.of(sampleNdlBook()));

        mockMvc.perform(post("/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .param("keyword", "テスト駆動開発"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/title-results"))
                .andExpect(model().attribute("keyword", "テスト駆動開発"))
                .andExpect(model().attributeExists("candidates"));
    }

    @Test
    @DisplayName("POST /books/search/title: 空キーワードは検索フォームに戻す")
    void searchByTitle_blankKeyword_returnsSearchForm() throws Exception {
        mockMvc.perform(post("/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("book/search"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /books/search/title: NDL APIエラーは検索フォームにエラーメッセージを表示")
    void searchByTitle_apiError_returnsSearchFormWithError() throws Exception {
        when(bookTitleSearchService.searchByTitle(anyString()))
                .thenThrow(new ExternalApiException("接続エラー"));

        mockMvc.perform(post("/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .param("keyword", "テスト"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/search"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /books/search/confirm: 正常なISBNで confirm ビューが返る")
    void confirmFromTitle_validIsbn_returnsConfirmView() throws Exception {
        BookDto book = BookDto.builder()
                .isbn("9784000000011")
                .title("テスト駆動開発")
                .author("Kent Beck")
                .publisher("オーム社")
                .coverUrl("https://cover.openbd.jp/9784000000011.jpg")
                .build();
        when(bookTitleSearchService.getBookWithCover(
                "9784000000011", "テスト駆動開発", "Kent Beck", "オーム社"))
                .thenReturn(book);

        mockMvc.perform(post("/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .param("isbn", "9784000000011")
                        .param("title", "テスト駆動開発")
                        .param("author", "Kent Beck")
                        .param("publisher", "オーム社"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/confirm"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @DisplayName("POST /books/search/confirm: OpenBD APIエラーは検索フォームにエラーメッセージを表示")
    void confirmFromTitle_apiError_returnsSearchFormWithError() throws Exception {
        when(bookTitleSearchService.getBookWithCover(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new ExternalApiException("接続エラー"));

        mockMvc.perform(post("/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .param("isbn", "9784000000011")
                        .param("title", "テスト")
                        .param("author", "著者")
                        .param("publisher", "出版社"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/search"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /books/search/confirm: 空ISBNは検索フォームにエラーメッセージを表示")
    void confirmFromTitle_blankIsbn_returnsSearchFormWithError() throws Exception {
        mockMvc.perform(post("/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .param("isbn", "   ")
                        .param("title", "テスト"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/search"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("未認証で POST /books/search/title にアクセスするとリダイレクト")
    void searchByTitle_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/books/search/title")
                        .with(csrf())
                        .param("keyword", "テスト"))
                .andExpect(status().is3xxRedirection());
    }
}
