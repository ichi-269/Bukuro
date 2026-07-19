package com.bukuro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileDto {

    private UserDto profileUser;
    private List<PostDto> posts;
    private int postCount;
    @Getter(onMethod_ = @JsonProperty("isOwnPage"))
    private boolean isOwnPage;
    @Getter(onMethod_ = @JsonProperty("isFollowing"))
    private boolean isFollowing;
    private long followerCount;
    private long followingCount;
}
