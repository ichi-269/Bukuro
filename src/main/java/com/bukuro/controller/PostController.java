package com.bukuro.controller;

import com.bukuro.dto.BookDto;
import com.bukuro.dto.PostDto;
import com.bukuro.dto.PostForm;
import com.bukuro.entity.Book;
import com.bukuro.entity.Post;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.BookRepository;
import com.bukuro.service.GoodService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final GoodService goodService;

    @GetMapping("/books/{bookId}")
    public BookDto book(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("書籍が見つかりません: " + bookId));
        return BookDto.from(book);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> create(@RequestParam Long bookId,
                                          @Valid @RequestBody PostForm form,
                                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        Post post = postService.create(userId, bookId, form);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(post, userId));
    }

    @GetMapping("/posts/{postId}")
    public PostDto show(@PathVariable Long postId,
                        @AuthenticationPrincipal UserDetails principal) {
        Long currentUserId = principal != null ? getUserId(principal) : null;
        Post post = postService.findPublicOrOwn(postId, currentUserId);
        return toDto(post, currentUserId);
    }

    @PutMapping("/posts/{postId}")
    public PostDto update(@PathVariable Long postId,
                          @Valid @RequestBody PostForm form,
                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        Post post = postService.update(postId, form, userId);
        return toDto(post, userId);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        postService.delete(postId, userId);
        return ResponseEntity.noContent().build();
    }

    private PostDto toDto(Post post, Long currentUserId) {
        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUser().getId());
        boolean hasGooded = currentUserId != null && goodService.hasGooded(currentUserId, post.getId());
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .isPublic(post.isPublic())
                .goodCount(post.getGoodCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .book(BookDto.from(post.getBook()))
                .user(com.bukuro.dto.UserDto.from(post.getUser()))
                .isOwner(isOwner)
                .hasGooded(hasGooded)
                .build();
    }

    private Long getUserId(UserDetails principal) {
        return userService.getUserByEmail(principal.getUsername()).getId();
    }
}
