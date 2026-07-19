package com.bukuro.dto;

import com.bukuro.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MeDto {

    private Long id;
    private String username;
    private String email;
    private String bio;
    private LocalDateTime createdAt;

    public static MeDto from(User user) {
        return MeDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
