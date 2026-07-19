package com.bukuro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class HomeFeedDto {

    private String feedType;
    private boolean hasFollowees;
    private List<PostDto> posts;
}
