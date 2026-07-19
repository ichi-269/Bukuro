package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileEditForm {

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(min = 3, max = 50, message = "ユーザー名は3〜50文字で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "ユーザー名は英数字・アンダースコアのみ使用できます")
    private String username;

    @Size(max = 500, message = "自己紹介は500文字以内で入力してください")
    private String bio;
}
