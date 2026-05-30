package com.bukuro.controller;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final GoodService goodService;

    @GetMapping("/posts/new")
    public String newForm(@RequestParam Long bookId, Model model) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("書籍が見つかりません: " + bookId));
        model.addAttribute("book", book);
        model.addAttribute("postForm", new PostForm());
        return "post/new";
    }

    @PostMapping("/posts")
    public String create(@RequestParam Long bookId,
                         @Valid @ModelAttribute("postForm") PostForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("書籍が見つかりません: " + bookId));
            model.addAttribute("book", book);
            return "post/new";
        }
        Long userId = getUserId(principal);
        Post post = postService.create(userId, bookId, form);
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{postId}")
    public String show(@PathVariable Long postId,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Long currentUserId = principal != null ? getUserId(principal) : null;
        Post post = postService.findPublicOrOwn(postId, currentUserId);
        boolean hasGooded = currentUserId != null && goodService.hasGooded(currentUserId, postId);
        model.addAttribute("post", post);
        model.addAttribute("isOwner", currentUserId != null && currentUserId.equals(post.getUser().getId()));
        model.addAttribute("hasGooded", hasGooded);
        return "post/show";
    }

    @GetMapping("/posts/{postId}/edit")
    public String editForm(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
        Long userId = getUserId(principal);
        Post post = postService.findById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("この操作は許可されていません");
        }
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setBody(post.getBody());
        form.setIsPublic(post.isPublic());
        model.addAttribute("post", post);
        model.addAttribute("postForm", form);
        return "post/edit";
    }

    @PostMapping("/posts/{postId}/edit")
    public String update(@PathVariable Long postId,
                         @Valid @ModelAttribute("postForm") PostForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Long userId = getUserId(principal);
        if (bindingResult.hasErrors()) {
            Post post = postService.findById(postId);
            if (!post.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("この操作は許可されていません");
            }
            model.addAttribute("post", post);
            return "post/edit";
        }
        postService.update(postId, form, userId);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/delete")
    public String delete(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetails principal) {
        Long userId = getUserId(principal);
        postService.delete(postId, userId);
        return "redirect:/shelf";
    }

    private Long getUserId(UserDetails principal) {
        return userService.getUserByEmail(principal.getUsername()).getId();
    }
}
