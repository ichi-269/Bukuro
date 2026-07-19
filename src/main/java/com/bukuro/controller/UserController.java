package com.bukuro.controller;

import com.bukuro.dto.MeDto;
import com.bukuro.dto.PostDto;
import com.bukuro.dto.ProfileEditForm;
import com.bukuro.dto.UserDto;
import com.bukuro.dto.UserProfileDto;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;

    @GetMapping("/api/users/{username}")
    public UserProfileDto show(@PathVariable String username,
                               @AuthenticationPrincipal UserDetails principal) {
        User profileUser = userService.getUserByUsername(username);
        List<Post> posts = postService.findPublicByUserId(profileUser.getId());

        boolean isOwnPage = principal != null &&
                principal.getUsername().equals(profileUser.getEmail());

        boolean isFollowing = false;
        if (principal != null && !isOwnPage) {
            Long currentUserId = userService.getUserByEmail(principal.getUsername()).getId();
            isFollowing = followService.isFollowing(currentUserId, profileUser.getId());
        }

        return UserProfileDto.builder()
                .profileUser(UserDto.from(profileUser))
                .posts(posts.stream().map(PostDto::from).toList())
                .postCount(posts.size())
                .isOwnPage(isOwnPage)
                .isFollowing(isFollowing)
                .followerCount(followService.getFollowerCount(profileUser.getId()))
                .followingCount(followService.getFollowingCount(profileUser.getId()))
                .build();
    }

    @PutMapping("/api/profile/edit")
    public MeDto editProfile(@AuthenticationPrincipal UserDetails principal,
                             @Valid @RequestBody ProfileEditForm form) {
        User user = userService.getUserByEmail(principal.getUsername());
        User updated = userService.updateProfile(user.getId(), form);
        return MeDto.from(updated);
    }

    @GetMapping("/api/users/{username}/followers")
    public List<UserDto> followers(@PathVariable String username) {
        User profileUser = userService.getUserByUsername(username);
        return followService.getFollowers(profileUser.getId()).stream().map(UserDto::from).toList();
    }

    @GetMapping("/api/users/{username}/following")
    public List<UserDto> following(@PathVariable String username) {
        User profileUser = userService.getUserByUsername(username);
        return followService.getFollowees(profileUser.getId()).stream().map(UserDto::from).toList();
    }
}
