package com.bukuro.controller;

import com.bukuro.config.SecurityConfig;
import com.bukuro.entity.Book;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private UserService userService;

    @MockBean
    private FollowService followService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User buildUser(Long id, String email) {
        return User.builder().id(id).username("user" + id).email(email)
                .createdAt(LocalDateTime.now()).build();
    }

    private Post buildPost(Long id, User user) {
        Book book = Book.builder().id(1L).isbn("9784000000000").title("本").author("著者").build();
        return Post.builder().id(id).user(user).book(book)
                .title("記事" + id).body("本文").isPublic(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("未認証で GET / にアクセスすると200が返り feedPosts モデルがない")
    void index_unauthenticated_returns200WithoutFeed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attributeDoesNotExist("feedPosts"))
                .andExpect(model().attributeDoesNotExist("feedType"));
    }

    @Test
    @DisplayName("フォロー中あり・パラメータなし: feedType=following でフォロイーの記事が返る")
    void index_authenticatedWithFollowees_returnsFeed() throws Exception {
        User me = buildUser(1L, "me@example.com");
        Post post = buildPost(10L, buildUser(2L, "other@example.com"));

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(3L);
        when(postService.findFollowingFeed(1L)).thenReturn(List.of(post));

        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("feedType", "following"))
                .andExpect(model().attribute("hasFollowees", true))
                .andExpect(model().attributeExists("feedPosts"));
    }

    @Test
    @DisplayName("フォロー中あり・?feed=recommended: feedType=recommended でおすすめ記事が返る")
    void index_authenticatedWithFollowees_feedRecommended_returnsRecommended() throws Exception {
        User me = buildUser(1L, "me@example.com");
        Post post = buildPost(20L, buildUser(3L, "popular@example.com"));

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(3L);
        when(postService.findRecommendedFeed()).thenReturn(List.of(post));

        mockMvc.perform(get("/")
                        .param("feed", "recommended")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("feedType", "recommended"))
                .andExpect(model().attribute("hasFollowees", true))
                .andExpect(model().attributeExists("feedPosts"));
    }

    @Test
    @DisplayName("フォロー中あり・?feed=following: feedType=following でフォロイーの記事が返る")
    void index_authenticatedWithFollowees_feedFollowing_returnsFollowing() throws Exception {
        User me = buildUser(1L, "me@example.com");
        Post post = buildPost(10L, buildUser(2L, "other@example.com"));

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(3L);
        when(postService.findFollowingFeed(1L)).thenReturn(List.of(post));

        mockMvc.perform(get("/")
                        .param("feed", "following")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("feedType", "following"))
                .andExpect(model().attributeExists("feedPosts"));
    }

    @Test
    @DisplayName("フォロー中なし: feedType=recommended でおすすめ記事が返る")
    void index_authenticatedWithoutFollowees_returnsRecommended() throws Exception {
        User me = buildUser(1L, "me@example.com");
        Post post = buildPost(20L, buildUser(3L, "popular@example.com"));

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(0L);
        when(postService.findRecommendedFeed()).thenReturn(List.of(post));

        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("feedType", "recommended"))
                .andExpect(model().attribute("hasFollowees", false))
                .andExpect(model().attributeExists("feedPosts"));
    }

    @Test
    @DisplayName("フォロー中なし・?feed=following 指定: hasFollowees=false のため feedType=recommended になる")
    void index_noFollowees_feedFollowingParam_stillReturnsRecommended() throws Exception {
        User me = buildUser(1L, "me@example.com");

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(0L);
        when(postService.findRecommendedFeed()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/")
                        .param("feed", "following")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("feedType", "recommended"));
    }

    @Test
    @DisplayName("フォロー中あり・フォロイーに記事なし: feedType=following かつ feedPosts が空")
    void index_followingWithNoPostsYet_emptyFeed() throws Exception {
        User me = buildUser(1L, "me@example.com");

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(2L);
        when(postService.findFollowingFeed(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("feedType", "following"))
                .andExpect(model().attribute("feedPosts", Collections.emptyList()));
    }

    @Test
    @DisplayName("フォロー中なし・公開記事なし: feedType=recommended かつ feedPosts が空")
    void index_noFolloweesAndNoPosts_emptyRecommended() throws Exception {
        User me = buildUser(1L, "me@example.com");

        when(userService.getUserByEmail("me@example.com")).thenReturn(me);
        when(followService.getFollowingCount(1L)).thenReturn(0L);
        when(postService.findRecommendedFeed()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/")
                        .with(SecurityMockMvcRequestPostProcessors.user("me@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("feedType", "recommended"))
                .andExpect(model().attribute("feedPosts", Collections.emptyList()));
    }
}
