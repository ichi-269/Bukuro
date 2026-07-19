package com.bukuro.controller;

import com.bukuro.service.GoodService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class GoodController {

    private final GoodService goodService;
    private final UserService userService;

    @PostMapping("/{postId}/good")
    public ResponseEntity<Void> good(@PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getUserByEmail(principal.getUsername()).getId();
        goodService.addGood(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/ungood")
    public ResponseEntity<Void> ungood(@PathVariable Long postId,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.getUserByEmail(principal.getUsername()).getId();
        goodService.removeGood(userId, postId);
        return ResponseEntity.noContent().build();
    }
}
