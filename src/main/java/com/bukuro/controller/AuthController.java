package com.bukuro.controller;

import com.bukuro.dto.RegisterForm;
import com.bukuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result) {
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
            return "auth/register";
        }

        userService.register(form);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }
}
