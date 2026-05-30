package com.bukuro.controller;

import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.service.GoodService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class GoodController {

    private final GoodService goodService;
    private final UserService userService;

    @PostMapping("/posts/{postId}/good")
    public String good(@PathVariable Long postId,
                       @AuthenticationPrincipal UserDetails principal,
                       RedirectAttributes redirectAttributes) {
        Long userId = userService.getUserByEmail(principal.getUsername()).getId();
        try {
            goodService.addGood(userId, postId);
        } catch (DuplicateRecordException e) {
            redirectAttributes.addFlashAttribute("warningMessage", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/posts/{postId}/ungood")
    public String ungood(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getUserByEmail(principal.getUsername()).getId();
        goodService.removeGood(userId, postId);
        return "redirect:/posts/" + postId;
    }
}
