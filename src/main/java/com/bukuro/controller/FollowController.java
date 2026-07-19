package com.bukuro.controller;

import com.bukuro.service.FollowService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> follow(@PathVariable String username,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long followerId = userService.getUserByEmail(principal.getUsername()).getId();
        Long followeeId = userService.getUserByUsername(username).getId();
        followService.follow(followerId, followeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{username}/unfollow")
    public ResponseEntity<Void> unfollow(@PathVariable String username,
                                         @AuthenticationPrincipal UserDetails principal) {
        Long followerId = userService.getUserByEmail(principal.getUsername()).getId();
        Long followeeId = userService.getUserByUsername(username).getId();
        followService.unfollow(followerId, followeeId);
        return ResponseEntity.noContent().build();
    }
}
