package com.bukuro.controller;

import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final FollowService followService;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails principal,
                        @RequestParam(required = false) String feed,
                        Model model) {
        if (principal != null) {
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

            model.addAttribute("feedPosts", "recommended".equals(feedType)
                    ? postService.findRecommendedFeed()
                    : postService.findFollowingFeed(userId));
            model.addAttribute("feedType", feedType);
            model.addAttribute("hasFollowees", hasFollowees);
        }
        return "home/index";
    }
}
