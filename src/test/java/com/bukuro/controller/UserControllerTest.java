package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.dto.ProfileEditForm;
import com.bukuro.entity.Book;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @MockBean
    private FollowService followService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("存在するユーザー名で GET /api/users/{username} にアクセスすると200が返る")
    void show_existingUser_returns200() throws Exception {
        User user = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .createdAt(LocalDateTime.now()).build();
        Book book = Book.builder().id(1L).isbn("9784000000000").title("本").author("著者").build();
        Post post = Post.builder()
                .id(1L).user(user).book(book)
                .title("記事タイトル").body("本文").isPublic(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(postService.findPublicByUserId(1L)).thenReturn(List.of(post));

        mockMvc.perform(get("/api/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileUser.username").value("testuser"))
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.postCount").value(1));
    }

    @Test
    @DisplayName("存在しないユーザー名で GET /api/users/{username} にアクセスすると404が返る")
    void show_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/api/users/nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("未認証でも GET /api/users/{username} にアクセスできる")
    void show_unauthenticated_returns200() throws Exception {
        User user = User.builder()
                .id(1L).username("pubuser").email("pub@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("pubuser")).thenReturn(user);
        when(postService.findPublicByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/pubuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postCount").value(0))
                .andExpect(jsonPath("$.isOwnPage").value(false));
    }

    @Test
    @DisplayName("自分のページにアクセスすると isOwnPage が true になる")
    void show_ownPage_isOwnPageTrue() throws Exception {
        User user = User.builder()
                .id(1L).username("ownuser").email("own@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("ownuser")).thenReturn(user);
        when(postService.findPublicByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/ownuser")
                        .with(user("own@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOwnPage").value(true));
    }

    @Test
    @DisplayName("他人のページにアクセスすると isOwnPage が false になる")
    void show_otherPage_isOwnPageFalse() throws Exception {
        User profileUser = User.builder()
                .id(2L).username("otheruser").email("other@example.com")
                .createdAt(LocalDateTime.now()).build();
        User currentUser = User.builder()
                .id(1L).username("me").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("otheruser")).thenReturn(profileUser);
        when(userService.getUserByEmail("me@example.com")).thenReturn(currentUser);
        when(postService.findPublicByUserId(2L)).thenReturn(List.of());
        when(followService.isFollowing(anyLong(), anyLong())).thenReturn(false);

        mockMvc.perform(get("/api/users/otheruser")
                        .with(user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOwnPage").value(false));
    }

    @Test
    @DisplayName("GET /api/users/{username}/followers でフォロワー一覧が返る")
    void followers_existingUser_returns200() throws Exception {
        User profileUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .createdAt(LocalDateTime.now()).build();
        User follower = User.builder()
                .id(2L).username("follower1").email("f1@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("testuser")).thenReturn(profileUser);
        when(followService.getFollowers(1L)).thenReturn(List.of(follower));

        mockMvc.perform(get("/api/users/testuser/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("follower1"));
    }

    @Test
    @DisplayName("GET /api/users/{username}/following でフォロー中一覧が返る")
    void following_existingUser_returns200() throws Exception {
        User profileUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .createdAt(LocalDateTime.now()).build();
        User followee = User.builder()
                .id(3L).username("followee1").email("fe1@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("testuser")).thenReturn(profileUser);
        when(followService.getFollowees(1L)).thenReturn(List.of(followee));

        mockMvc.perform(get("/api/users/testuser/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("followee1"));
    }

    @Test
    @DisplayName("存在しないユーザーの GET /api/users/{username}/followers は404が返る")
    void followers_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/api/users/nobody/followers"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("存在しないユーザーの GET /api/users/{username}/following は404が返る")
    void following_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/api/users/nobody/following"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("有効なフォームで PUT /api/profile/edit すると更新後のユーザー情報が返る")
    void editProfile_validForm_returnsUpdatedUser() throws Exception {
        User me = User.builder()
                .id(1L).username("oldname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();
        User updated = User.builder()
                .id(1L).username("newname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(userService.updateProfile(anyLong(), any(ProfileEditForm.class))).thenReturn(updated);

        mockMvc.perform(put("/api/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "newname", "bio", "新しい自己紹介"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"));
    }

    @Test
    @DisplayName("未認証で PUT /api/profile/edit にアクセスすると401が返る")
    void editProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/profile/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "x"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("バリデーションエラーで PUT /api/profile/edit すると400が返る")
    void editProfile_validationError_returns400() throws Exception {
        mockMvc.perform(put("/api/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("重複ユーザー名で PUT /api/profile/edit すると409が返る")
    void editProfile_duplicateUsername_returns409() throws Exception {
        User me = User.builder()
                .id(1L).username("myname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(userService.updateProfile(anyLong(), any(ProfileEditForm.class)))
                .thenThrow(new IllegalStateException("このユーザー名はすでに使用されています: taken"));

        mockMvc.perform(put("/api/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "taken", "bio", ""))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
