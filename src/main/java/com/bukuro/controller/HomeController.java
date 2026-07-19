package com.bukuro.controller;

import com.bukuro.dto.HomeFeedDto;
import com.bukuro.dto.PostDto;
import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final FollowService followService;

    @GetMapping("/feed")
    public HomeFeedDto feed(@AuthenticationPrincipal UserDetails principal,
                            @RequestParam(required = false) String feed) {
        Long userId = userService.getUserByEmail(principal.getUsername()).getId();
        boolean hasFollowees = followService.getFollowingCount(userId) > 0;

        String feedType;
        if (!hasFollowees) {
            feedType = "recommended";
        } else if ("recommended".equals(feed)) {
            feedType = "recommended";
        } else {
            feedType = "following";
        }

        var posts = ("recommended".equals(feedType)
                ? postService.findRecommendedFeed()
                : postService.findFollowingFeed(userId)).stream()
                .map(PostDto::from)
                .toList();

        return HomeFeedDto.builder()
                .feedType(feedType)
                .hasFollowees(hasFollowees)
                .posts(posts)
                .build();
    }
}
