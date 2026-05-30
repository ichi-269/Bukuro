package com.bukuro.service;

import com.bukuro.dto.RegisterForm;
import com.bukuro.entity.User;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalStateException("このメールアドレスはすでに登録されています: " + form.getEmail());
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalStateException("このユーザー名はすでに使用されています: " + form.getUsername());
        }
        User user = User.builder()
                .email(form.getEmail())
                .username(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))
                .build();
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + email));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + username));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
