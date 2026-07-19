package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.entity.User;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.GoodService;
import com.bukuro.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoodController.class)
@Import(SecurityConfig.class)
class GoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodService goodService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User buildUser(Long id) {
        return User.builder().id(id).username("user" + id).email("user" + id + "@example.com")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/good でグッドが実行され204が返る")
    void good_authenticated_returns204() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));

        mockMvc.perform(post("/api/posts/5/good")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goodService).addGood(1L, 5L);
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/good で既にグッド済みの場合は409が返る")
    void good_alreadyGooded_returns409() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));
        doThrow(new DuplicateRecordException("すでにグッド済みです"))
                .when(goodService).addGood(1L, 5L);

        mockMvc.perform(post("/api/posts/5/good")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RECORD"));
    }

    @Test
    @DisplayName("未認証で POST /api/posts/{postId}/ungood にアクセスすると401が返る")
    void ungood_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/posts/5/ungood").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/ungood でグッド取り消しが実行され204が返る")
    void ungood_authenticated_returns204() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));

        mockMvc.perform(post("/api/posts/5/ungood")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(goodService).removeGood(1L, 5L);
    }

    @Test
    @DisplayName("未認証で POST /api/posts/{postId}/good にアクセスすると401が返る")
    void good_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/posts/5/good").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
