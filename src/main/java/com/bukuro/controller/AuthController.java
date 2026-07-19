package com.bukuro.controller;

import com.bukuro.dto.ErrorResponse;
import com.bukuro.dto.MeDto;
import com.bukuro.dto.RegisterForm;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterForm form, BindingResult result) {
        // @Validエラーがない場合のみDB重複チェックとパスワード確認を実行
        if (!result.hasErrors()) {
            if (!form.isPasswordConfirmed()) {
                result.rejectValue("passwordConfirm", "error.passwordConfirm", "パスワードが一致しません");
            }
            if (userService.existsByEmail(form.getEmail())) {
                result.rejectValue("email", "error.email", "このメールアドレスはすでに登録されています");
            }
            if (userService.existsByUsername(form.getUsername())) {
                result.rejectValue("username", "error.username", "このユーザー名はすでに使用されています");
            }
        }

        if (result.hasErrors()) {
            var fieldErrors = result.getFieldErrors().stream()
                    .map(fieldError -> ErrorResponse.FieldErrorItem.builder()
                            .field(fieldError.getField())
                            .message(fieldError.getDefaultMessage())
                            .build())
                    .toList();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code("VALIDATION_ERROR")
                    .message("入力内容に誤りがあります")
                    .fieldErrors(fieldErrors)
                    .build());
        }

        MeDto created = MeDto.from(userService.register(form));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<MeDto> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(MeDto.from(userService.getUserByEmail(principal.getUsername())));
    }
}
