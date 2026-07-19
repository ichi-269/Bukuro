package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TitleSearchRequest {

    @NotBlank(message = "書名を入力してください")
    private String keyword;
}
