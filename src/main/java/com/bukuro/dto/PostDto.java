package com.bukuro.dto;

import com.bukuro.entity.Post;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PostDto {

    private Long id;
    private String title;
    private String body;
    @Getter(onMethod_ = @JsonProperty("isPublic"))
    private boolean isPublic;
    private int goodCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookDto book;
    private UserDto user;
    @Getter(onMethod_ = @JsonProperty("isOwner"))
    private Boolean isOwner;
    private Boolean hasGooded;

    public static PostDto from(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .isPublic(post.isPublic())
                .goodCount(post.getGoodCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .book(BookDto.from(post.getBook()))
                .user(UserDto.from(post.getUser()))
                .build();
    }
}
