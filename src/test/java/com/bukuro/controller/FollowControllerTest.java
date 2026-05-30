package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.entity.User;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.FollowService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FollowController.class)
@Import(SecurityConfig.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User buildUser(Long id, String username, String email) {
        return User.builder().id(id).username(username).email(email)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("POST /users/{username}/follow でフォローが実行されてリダイレクトされる")
    void follow_authenticated_redirectsToUserPage() throws Exception {
        User currentUser = buildUser(1L, "me", "me@example.com");
        User targetUser = buildUser(2L, "target", "target@example.com");

        when(userService.getUserByEmail("me@example.com")).thenReturn(currentUser);
        when(userService.getUserByUsername("target")).thenReturn(targetUser);

        mockMvc.perform(post("/users/target/follow")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/target"));

        verify(followService).follow(1L, 2L);
    }

    @Test
    @DisplayName("POST /users/{username}/unfollow でアンフォローが実行されてリダイレクトされる")
    void unfollow_authenticated_redirectsToUserPage() throws Exception {
        User currentUser = buildUser(1L, "me", "me@example.com");
        User targetUser = buildUser(2L, "target", "target@example.com");

        when(userService.getUserByEmail("me@example.com")).thenReturn(currentUser);
        when(userService.getUserByUsername("target")).thenReturn(targetUser);

        mockMvc.perform(post("/users/target/unfollow")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/target"));

        verify(followService).unfollow(1L, 2L);
    }

    @Test
    @DisplayName("未認証で POST /users/{username}/follow にアクセスするとログインページへリダイレクトされる")
    void follow_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/users/target/follow").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
