package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /register で登録フォームが表示される")
    void registerForm_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    @DisplayName("GET /login でログインフォームが表示される")
    void loginForm_returnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("正常な入力で POST /register するとログインページへリダイレクト")
    void register_validInput_redirectsToLogin() throws Exception {
        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "new@example.com")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));
    }

    @Test
    @DisplayName("パスワード不一致で POST /register するとフォームが再表示される")
    void register_passwordMismatch_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "test@example.com")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("passwordConfirm", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("重複メールで POST /register するとフォームが再表示される")
    void register_duplicateEmail_returnsFormWithError() throws Exception {
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "existing@example.com")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
    }

    @Test
    @DisplayName("重複ユーザー名で POST /register するとフォームが再表示される")
    void register_duplicateUsername_returnsFormWithError() throws Exception {
        when(userService.existsByUsername("takenuser")).thenReturn(true);

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "unique@example.com")
                        .param("username", "takenuser")
                        .param("password", "password123")
                        .param("passwordConfirm", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "username"));
    }

    @Test
    @DisplayName("未認証で /shelf にアクセスするとログインページへリダイレクトされる")
    void shelf_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/shelf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
