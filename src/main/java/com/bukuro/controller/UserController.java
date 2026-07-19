package com.bukuro.controller;

import com.bukuro.dto.ProfileEditForm;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.service.FollowService;
import com.bukuro.service.PostService;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails principal) {
        String username = userService.getUserByEmail(principal.getUsername()).getUsername();
        return "redirect:/users/" + username;
    }

    @GetMapping("/users/{username}")
    public String show(@PathVariable String username,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        User profileUser = userService.getUserByUsername(username);
        List<Post> posts = postService.findPublicByUserId(profileUser.getId());

        boolean isOwnPage = principal != null &&
                principal.getUsername().equals(profileUser.getEmail());

        boolean isFollowing = false;
        if (principal != null && !isOwnPage) {
            Long currentUserId = userService.getUserByEmail(principal.getUsername()).getId();
            isFollowing = followService.isFollowing(currentUserId, profileUser.getId());
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("posts", posts);
        model.addAttribute("postCount", posts.size());
        model.addAttribute("isOwnPage", isOwnPage);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followerCount", followService.getFollowerCount(profileUser.getId()));
        model.addAttribute("followingCount", followService.getFollowingCount(profileUser.getId()));
        return "user/show";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getUserByEmail(principal.getUsername());
        ProfileEditForm form = new ProfileEditForm();
        form.setUsername(user.getUsername());
        form.setBio(user.getBio());
        model.addAttribute("profileEditForm", form);
        return "user/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails principal,
                              @Valid @ModelAttribute ProfileEditForm form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profileEditForm", form);
            return "user/profile-edit";
        }
        User user = userService.getUserByEmail(principal.getUsername());
        try {
            User updated = userService.updateProfile(user.getId(), form);
            return "redirect:/users/" + updated.getUsername();
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/profile-edit";
        }
    }

    @GetMapping("/users/{username}/followers")
    public String followers(@PathVariable String username, Model model) {
        User profileUser = userService.getUserByUsername(username);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("users", followService.getFollowers(profileUser.getId()));
        return "user/followers";
    }

    @GetMapping("/users/{username}/following")
    public String following(@PathVariable String username, Model model) {
        User profileUser = userService.getUserByUsername(username);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("users", followService.getFollowees(profileUser.getId()));
        return "user/following";
    }
}
