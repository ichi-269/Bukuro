package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.entity.User;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.UserService;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User buildUser(Long id, String username, String email) {
        return User.builder().id(id).username(username).email(email)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("正常な入力で POST /api/register すると201とユーザー情報が返る")
    void register_validInput_returns201() throws Exception {
        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.register(org.mockito.ArgumentMatchers.any()))
                .thenReturn(buildUser(1L, "newuser", "new@example.com"));

        mockMvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "new@example.com",
                                "username", "newuser",
                                "password", "password123",
                                "passwordConfirm", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("パスワード不一致で POST /api/register すると400が返る")
    void register_passwordMismatch_returns400() throws Exception {
        mockMvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@example.com",
                                "username", "testuser",
                                "password", "password123",
                                "passwordConfirm", "different"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(org.hamcrest.Matchers.hasItem("passwordConfirm")));
    }

    @Test
    @DisplayName("重複メールで POST /api/register すると400が返る")
    void register_duplicateEmail_returns400() throws Exception {
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "existing@example.com",
                                "username", "newuser",
                                "password", "password123",
                                "passwordConfirm", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(org.hamcrest.Matchers.hasItem("email")));
    }

    @Test
    @DisplayName("重複ユーザー名で POST /api/register すると400が返る")
    void register_duplicateUsername_returns400() throws Exception {
        when(userService.existsByUsername("takenuser")).thenReturn(true);

        mockMvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "unique@example.com",
                                "username", "takenuser",
                                "password", "password123",
                                "passwordConfirm", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(org.hamcrest.Matchers.hasItem("username")));
    }

    @Test
    @DisplayName("未認証で GET /api/me にアクセスすると200とnullボディが返る")
    void me_unauthenticated_returns200WithNullBody() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("認証済みで GET /api/me にアクセスするとユーザー情報が返る")
    void me_authenticated_returnsUser() throws Exception {
        when(userService.getUserByEmail("me@example.com")).thenReturn(buildUser(1L, "myuser", "me@example.com"));

        mockMvc.perform(get("/api/me")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("myuser"))
                .andExpect(jsonPath("$.email").value("me@example.com"));
    }
}
