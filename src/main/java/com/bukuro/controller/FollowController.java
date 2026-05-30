package com.bukuro.controller;

import com.bukuro.service.FollowService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    @PostMapping("/users/{username}/follow")
    public String follow(@PathVariable String username,
                         @AuthenticationPrincipal UserDetails principal) {
        Long followerId = userService.getUserByEmail(principal.getUsername()).getId();
        Long followeeId = userService.getUserByUsername(username).getId();
        followService.follow(followerId, followeeId);
        return "redirect:/users/" + username;
    }

    @PostMapping("/users/{username}/unfollow")
    public String unfollow(@PathVariable String username,
                           @AuthenticationPrincipal UserDetails principal) {
        Long followerId = userService.getUserByEmail(principal.getUsername()).getId();
        Long followeeId = userService.getUserByUsername(username).getId();
        followService.unfollow(followerId, followeeId);
        return "redirect:/users/" + username;
    }
}
