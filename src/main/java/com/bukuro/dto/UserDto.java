package com.bukuro.dto;

import com.bukuro.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String bio;
    private LocalDateTime createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
