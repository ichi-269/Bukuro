package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostForm {

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    private String title;

    @NotBlank(message = "本文を入力してください")
    private String body;

    private Boolean isPublic;
}
