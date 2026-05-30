package com.bukuro.controller;

import com.bukuro.entity.Post;
import com.bukuro.entity.ReadingRecord;
import com.bukuro.entity.ReadingRecord.ReadingStatus;
import com.bukuro.exception.BookNotFoundException;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.exception.ExternalApiException;
import com.bukuro.service.PostService;
import com.bukuro.service.ShelfService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;
    private final UserService userService;
    private final PostService postService;

    @PostMapping("/books/add")
    public String add(@RequestParam String isbn,
                      @AuthenticationPrincipal UserDetails principal,
                      RedirectAttributes redirectAttributes) {
        Long userId = getUserId(principal);
        try {
            shelfService.addToShelf(userId, isbn);
            return "redirect:/shelf";
        } catch (DuplicateRecordException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/shelf";
        } catch (BookNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "この書籍はOpenBDに登録されていません。ISBNを確認してください。");
            return "redirect:/books/search";
        } catch (ExternalApiException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books/search";
        }
    }

    @GetMapping("/shelf")
    public String shelf(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = getUserId(principal);
        List<ReadingRecord> records = shelfService.getShelf(userId);

        model.addAttribute("wantToRead",
                records.stream().filter(r -> r.getStatus() == ReadingStatus.WANT_TO_READ)
                        .collect(Collectors.toList()));
        model.addAttribute("reading",
                records.stream().filter(r -> r.getStatus() == ReadingStatus.READING)
                        .collect(Collectors.toList()));
        model.addAttribute("done",
                records.stream().filter(r -> r.getStatus() == ReadingStatus.DONE)
                        .collect(Collectors.toList()));

        Map<Long, Long> postIdByBookId = postService.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        p -> p.getBook().getId(),
                        Post::getId,
                        (existing, newer) -> existing
                ));
        model.addAttribute("postIdByBookId", postIdByBookId);

        return "shelf/index";
    }

    @PostMapping("/shelf/{recordId}/status")
    public String updateStatus(@PathVariable Long recordId,
                               @RequestParam String status,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes redirectAttributes) {
        ReadingStatus readingStatus;
        try {
            readingStatus = ReadingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効なステータスです");
            return "redirect:/shelf";
        }
        Long userId = getUserId(principal);
        shelfService.updateStatus(recordId, readingStatus, userId);
        return "redirect:/shelf";
    }

    @PostMapping("/shelf/{recordId}/delete")
    public String delete(@PathVariable Long recordId,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes redirectAttributes) {
        Long userId = getUserId(principal);
        shelfService.remove(recordId, userId);
        return "redirect:/shelf";
    }

    private Long getUserId(UserDetails principal) {
        return userService.getUserByEmail(principal.getUsername()).getId();
    }
}
