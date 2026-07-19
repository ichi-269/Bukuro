package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import com.bukuro.service.BookSearchService;
import com.bukuro.service.BookTitleSearchService;
import com.bukuro.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/books/search/title: 正常な検索で候補一覧が返る")
    void searchByTitle_validKeyword_returnsCandidates() throws Exception {
        when(bookTitleSearchService.searchByTitle("テスト駆動開発"))
                .thenReturn(List.of(sampleNdlBook()));

        mockMvc.perform(post("/api/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("keyword", "テスト駆動開発"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("テスト駆動開発"));
    }

    @Test
    @DisplayName("POST /api/books/search/title: 空キーワードは400を返す")
    void searchByTitle_blankKeyword_returns400() throws Exception {
        mockMvc.perform(post("/api/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("keyword", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/books/search/title: NDL APIエラーは502を返す")
    void searchByTitle_apiError_returns502() throws Exception {
        when(bookTitleSearchService.searchByTitle(anyString()))
                .thenThrow(new ExternalApiException("接続エラー"));

        mockMvc.perform(post("/api/books/search/title")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("keyword", "テスト"))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EXTERNAL_API_ERROR"));
    }

    @Test
    @DisplayName("POST /api/books/search/confirm: 正常なISBNで書誌情報が返る")
    void confirmFromTitle_validIsbn_returnsBook() throws Exception {
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

        mockMvc.perform(post("/api/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "isbn", "9784000000011",
                                "title", "テスト駆動開発",
                                "author", "Kent Beck",
                                "publisher", "オーム社"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("テスト駆動開発"));
    }

    @Test
    @DisplayName("POST /api/books/search/confirm: OpenBD APIエラーは502を返す")
    void confirmFromTitle_apiError_returns502() throws Exception {
        when(bookTitleSearchService.getBookWithCover(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new ExternalApiException("接続エラー"));

        mockMvc.perform(post("/api/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "isbn", "9784000000011",
                                "title", "テスト",
                                "author", "著者",
                                "publisher", "出版社"))))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("POST /api/books/search/confirm: 空ISBNは400を返す")
    void confirmFromTitle_blankIsbn_returns400() throws Exception {
        mockMvc.perform(post("/api/books/search/confirm")
                        .with(LOGGED_IN).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "isbn", "   ",
                                "title", "テスト"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("未認証で POST /api/books/search/title にアクセスすると401が返る")
    void searchByTitle_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/books/search/title")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("keyword", "テスト"))))
                .andExpect(status().isUnauthorized());
    }
}
