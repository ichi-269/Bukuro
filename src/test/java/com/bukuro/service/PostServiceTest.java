package com.bukuro.service;

import com.bukuro.dto.PostForm;
import com.bukuro.entity.Book;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.BookRepository;
import com.bukuro.repository.FollowRepository;
import com.bukuro.repository.PostRepository;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private PostService postService;

    private User buildUser(Long id) {
        return User.builder().id(id).email("user" + id + "@example.com").username("user" + id).build();
    }

    private Book buildBook(Long id) {
        return Book.builder().id(id).isbn("9784000000000").title("本").author("著者").build();
    }

    @Test
    @DisplayName("create で正常に Post が保存される")
    void create_validInput_savesPost() {
        PostForm form = new PostForm();
        form.setTitle("テスト記事");
        form.setBody("本文");
        form.setIsPublic(true);

        User user = buildUser(1L);
        Book book = buildBook(2L);

        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post result = postService.create(1L, 2L, form);

        assertThat(result.getTitle()).isEqualTo("テスト記事");
        assertThat(result.isPublic()).isTrue();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("create で存在しない bookId を指定すると ResourceNotFoundException が発生する")
    void create_bookNotFound_throwsResourceNotFoundException() {
        PostForm form = new PostForm();
        form.setTitle("タイトル");
        form.setBody("本文");

        when(userRepository.getReferenceById(1L)).thenReturn(buildUser(1L));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(1L, 99L, form))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update で自分の記事を更新できる")
    void update_ownPost_updatesSuccessfully() {
        User user = buildUser(1L);
        Post post = Post.builder().id(1L).user(user).book(buildBook(1L))
                .title("旧タイトル").body("旧本文").isPublic(false).build();

        PostForm form = new PostForm();
        form.setTitle("新タイトル");
        form.setBody("新本文");
        form.setIsPublic(true);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Post result = postService.update(1L, form, 1L);

        assertThat(result.getTitle()).isEqualTo("新タイトル");
        assertThat(result.isPublic()).isTrue();
    }

    @Test
    @DisplayName("update で他ユーザーの記事を更新しようとすると AccessDeniedException が発生する")
    void update_otherUsersPost_throwsAccessDeniedException() {
        User owner = buildUser(10L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("タイトル").body("本文").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.update(1L, new PostForm(), 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("delete で自分の記事を削除できる")
    void delete_ownPost_deletesSuccessfully() {
        User user = buildUser(1L);
        Post post = Post.builder().id(1L).user(user).book(buildBook(1L))
                .title("タイトル").body("本文").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.delete(1L, 1L);

        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("delete で他ユーザーの記事を削除しようとすると AccessDeniedException が発生する")
    void delete_otherUsersPost_throwsAccessDeniedException() {
        User owner = buildUser(10L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("タイトル").body("本文").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(1L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("findPublicOrOwn で公開記事は誰でも取得できる")
    void findPublicOrOwn_publicPost_returnsPost() {
        User owner = buildUser(1L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("公開記事").body("本文").isPublic(true).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.findPublicOrOwn(1L, null);
        assertThat(result.getTitle()).isEqualTo("公開記事");
    }

    @Test
    @DisplayName("findPublicOrOwn で非公開記事は本人だけ取得できる")
    void findPublicOrOwn_privatePost_ownerCanAccess() {
        User owner = buildUser(1L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("非公開記事").body("本文").isPublic(false).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.findPublicOrOwn(1L, 1L);
        assertThat(result.getTitle()).isEqualTo("非公開記事");
    }

    @Test
    @DisplayName("findPublicOrOwn で非公開記事に他ユーザーがアクセスすると ResourceNotFoundException が発生する")
    void findPublicOrOwn_privatePost_otherUserGetNotFound() {
        User owner = buildUser(1L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("非公開記事").body("本文").isPublic(false).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.findPublicOrOwn(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findPublicOrOwn で非公開記事に未認証ユーザーがアクセスすると ResourceNotFoundException が発生する")
    void findPublicOrOwn_privatePost_unauthenticatedGetNotFound() {
        User owner = buildUser(1L);
        Post post = Post.builder().id(1L).user(owner).book(buildBook(1L))
                .title("非公開記事").body("本文").isPublic(false).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.findPublicOrOwn(1L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("存在しない postId で findById を呼ぶと ResourceNotFoundException が発生する")
    void findById_notFound_throwsResourceNotFoundException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findFollowingFeed でフォロイーの公開記事を返す")
    void findFollowingFeed_withFollowees_returnsFolloweesPosts() {
        User user = buildUser(1L);
        Post post = Post.builder().id(1L).user(user).book(buildBook(1L))
                .title("フォロイーの記事").body("本文").isPublic(true).build();

        when(followRepository.findFolloweeIdsByFollowerId(1L)).thenReturn(List.of(2L, 3L));
        when(postRepository.findByUserIdInAndIsPublicTrueOrderByCreatedAtDesc(
                eq(List.of(2L, 3L)), any(Pageable.class))).thenReturn(List.of(post));

        List<Post> result = postService.findFollowingFeed(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("フォロイーの記事");
    }

    @Test
    @DisplayName("findRecommendedFeed でグッド数降順の公開記事を返す")
    void findRecommendedFeed_returnsPopularPosts() {
        User user = buildUser(1L);
        Post post = Post.builder().id(2L).user(user).book(buildBook(1L))
                .title("人気記事").body("本文").isPublic(true).build();

        when(postRepository.findByIsPublicTrueOrderByGoodCountDescCreatedAtDesc(
                any(Pageable.class))).thenReturn(List.of(post));

        List<Post> result = postService.findRecommendedFeed();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("人気記事");
    }
}
