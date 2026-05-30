package com.bukuro.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {

    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(min = 3, max = 50, message = "ユーザー名は3〜50文字で入力してください")
    private String username;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;

    @NotBlank(message = "確認用パスワードを入力してください")
    private String passwordConfirm;

    public boolean isPasswordConfirmed() {
        return password != null && password.equals(passwordConfirm);
    }
}
