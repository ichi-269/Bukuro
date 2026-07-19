package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.entity.Book;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.service.CustomUserDetailsService;
import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import com.bukuro.dto.ProfileEditForm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PostService postService;

    @MockBean
    private FollowService followService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("存在するユーザー名で GET /users/{username} にアクセスすると200が返る")
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

        mockMvc.perform(get("/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/show"))
                .andExpect(model().attributeExists("profileUser"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("postCount", 1));
    }

    @Test
    @DisplayName("存在しないユーザー名で GET /users/{username} にアクセスすると404が返る")
    void show_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/users/nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("未認証でも GET /users/{username} にアクセスできる")
    void show_unauthenticated_returns200() throws Exception {
        User user = User.builder()
                .id(1L).username("pubuser").email("pub@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("pubuser")).thenReturn(user);
        when(postService.findPublicByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/pubuser"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("postCount", 0))
                .andExpect(model().attribute("isOwnPage", false));
    }

    @Test
    @DisplayName("自分のページにアクセスすると isOwnPage が true になる")
    void show_ownPage_isOwnPageTrue() throws Exception {
        User user = User.builder()
                .id(1L).username("ownuser").email("own@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("ownuser")).thenReturn(user);
        when(postService.findPublicByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/ownuser")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("own@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwnPage", true));
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

        mockMvc.perform(get("/users/otheruser")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isOwnPage", false));
    }

    @Test
    @DisplayName("GET /users/{username}/followers でフォロワー一覧が返る")
    void followers_existingUser_returns200() throws Exception {
        User profileUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .createdAt(LocalDateTime.now()).build();
        User follower = User.builder()
                .id(2L).username("follower1").email("f1@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("testuser")).thenReturn(profileUser);
        when(followService.getFollowers(1L)).thenReturn(List.of(follower));

        mockMvc.perform(get("/users/testuser/followers"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/followers"))
                .andExpect(model().attributeExists("profileUser"))
                .andExpect(model().attribute("users", Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("GET /users/{username}/following でフォロー中一覧が返る")
    void following_existingUser_returns200() throws Exception {
        User profileUser = User.builder()
                .id(1L).username("testuser").email("test@example.com")
                .createdAt(LocalDateTime.now()).build();
        User followee = User.builder()
                .id(3L).username("followee1").email("fe1@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByUsername("testuser")).thenReturn(profileUser);
        when(followService.getFollowees(1L)).thenReturn(List.of(followee));

        mockMvc.perform(get("/users/testuser/following"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/following"))
                .andExpect(model().attributeExists("profileUser"))
                .andExpect(model().attribute("users", Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("存在しないユーザーの GET /users/{username}/followers は404が返る")
    void followers_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/users/nobody/followers"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("存在しないユーザーの GET /users/{username}/following は404が返る")
    void following_notExistingUser_returns404() throws Exception {
        when(userService.getUserByUsername("nobody"))
                .thenThrow(new ResourceNotFoundException("ユーザーが見つかりません: nobody"));

        mockMvc.perform(get("/users/nobody/following"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("認証済みで GET /profile/edit にアクセスすると編集フォームが表示される")
    void editProfileForm_authenticated_returns200() throws Exception {
        User me = User.builder()
                .id(1L).username("myuser").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);

        mockMvc.perform(get("/profile/edit").with(user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile-edit"))
                .andExpect(model().attributeExists("profileEditForm"));
    }

    @Test
    @DisplayName("未認証で GET /profile/edit にアクセスするとログインにリダイレクトされる")
    void editProfileForm_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("有効なフォームで POST /profile/edit するとプロフィールページにリダイレクトされる")
    void editProfile_validForm_redirectsToUserPage() throws Exception {
        User me = User.builder()
                .id(1L).username("oldname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();
        User updated = User.builder()
                .id(1L).username("newname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(userService.updateProfile(anyLong(), any(ProfileEditForm.class))).thenReturn(updated);

        mockMvc.perform(post("/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .param("username", "newname")
                        .param("bio", "新しい自己紹介"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/newname"));
    }

    @Test
    @DisplayName("バリデーションエラーで POST /profile/edit すると編集フォームに戻る")
    void editProfile_validationError_returnsForm() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .param("username", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile-edit"));
    }

    @Test
    @DisplayName("重複ユーザー名で POST /profile/edit するとエラーメッセージ付きで編集フォームに戻る")
    void editProfile_duplicateUsername_returnsFormWithError() throws Exception {
        User me = User.builder()
                .id(1L).username("myname").email("me@example.com")
                .createdAt(LocalDateTime.now()).build();

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(userService.updateProfile(anyLong(), any(ProfileEditForm.class)))
                .thenThrow(new IllegalStateException("このユーザー名はすでに使用されています: taken"));

        mockMvc.perform(post("/profile/edit")
                        .with(user("me@example.com").roles("USER"))
                        .with(csrf())
                        .param("username", "taken")
                        .param("bio", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile-edit"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
