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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

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
    @DisplayName("POST /posts/{postId}/good でグッドが実行されてリダイレクトされる")
    void good_authenticated_redirectsToPost() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));

        mockMvc.perform(post("/posts/5/good")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/5"));

        verify(goodService).addGood(1L, 5L);
    }

    @Test
    @DisplayName("POST /posts/{postId}/good で既にグッド済みの場合はリダイレクト（フラッシュメッセージ）")
    void good_alreadyGooded_redirectsWithWarning() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));
        doThrow(new DuplicateRecordException("すでにグッド済みです"))
                .when(goodService).addGood(1L, 5L);

        mockMvc.perform(post("/posts/5/good")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/5"))
                .andExpect(flash().attribute("warningMessage", "すでにグッド済みです"));
    }

    @Test
    @DisplayName("未認証で POST /posts/{postId}/ungood にアクセスするとログインページへリダイレクトされる")
    void ungood_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/posts/5/ungood").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("POST /posts/{postId}/ungood でグッド取り消しが実行されてリダイレクトされる")
    void ungood_authenticated_redirectsToPost() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L));

        mockMvc.perform(post("/posts/5/ungood")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/5"));

        verify(goodService).removeGood(1L, 5L);
    }

    @Test
    @DisplayName("未認証で POST /posts/{postId}/good にアクセスするとログインページへリダイレクトされる")
    void good_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/posts/5/good").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
