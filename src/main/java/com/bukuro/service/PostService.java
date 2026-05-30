package com.bukuro.service;

import com.bukuro.dto.PostForm;
import com.bukuro.entity.Post;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.BookRepository;
import com.bukuro.repository.FollowRepository;
import com.bukuro.repository.PostRepository;
import com.bukuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int FEED_SIZE = 20;

    private final PostRepository postRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional
    public Post create(Long userId, Long bookId, PostForm form) {
        Post post = Post.builder()
                .user(userRepository.getReferenceById(userId))
                .book(bookRepository.findById(bookId)
                        .orElseThrow(() -> new ResourceNotFoundException("書籍が見つかりません: " + bookId)))
                .title(form.getTitle())
                .body(form.getBody())
                .isPublic(Boolean.TRUE.equals(form.getIsPublic()))
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Long postId, PostForm form, Long userId) {
        Post post = findRecord(postId);
        checkOwnership(post, userId);
        post.setTitle(form.getTitle());
        post.setBody(form.getBody());
        post.setPublic(Boolean.TRUE.equals(form.getIsPublic()));
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long postId, Long userId) {
        Post post = findRecord(postId);
        checkOwnership(post, userId);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return findRecord(postId);
    }

    @Transactional(readOnly = true)
    public Post findPublicOrOwn(Long postId, Long currentUserId) {
        Post post = findRecord(postId);
        if (!post.isPublic() && (currentUserId == null || !currentUserId.equals(post.getUser().getId()))) {
            throw new ResourceNotFoundException("記事が見つかりません: " + postId);
        }
        return post;
    }

    @Transactional(readOnly = true)
    public List<Post> findByUserId(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Post> findPublicByUserId(Long userId) {
        return postRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Post> findFollowingFeed(Long userId) {
        List<Long> followeeIds = followRepository.findFolloweeIdsByFollowerId(userId);
        return postRepository.findByUserIdInAndIsPublicTrueOrderByCreatedAtDesc(
                followeeIds, PageRequest.of(0, FEED_SIZE));
    }

    @Transactional(readOnly = true)
    public List<Post> findRecommendedFeed() {
        return postRepository.findByIsPublicTrueOrderByGoodCountDescCreatedAtDesc(
                PageRequest.of(0, FEED_SIZE));
    }

    private Post findRecord(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("記事が見つかりません: " + postId));
    }

    private void checkOwnership(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("この操作は許可されていません");
        }
    }
}
