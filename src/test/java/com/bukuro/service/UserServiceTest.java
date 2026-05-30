package com.bukuro.service;

import com.bukuro.dto.RegisterForm;
import com.bukuro.entity.User;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("正常な入力で register を呼ぶと User が保存される")
    void register_validForm_savesUser() {
        // Given
        RegisterForm form = new RegisterForm();
        form.setEmail("test@example.com");
        form.setUsername("testuser");
        form.setPassword("password123");
        form.setPasswordConfirm("password123");

        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashedpw");
        User savedUser = User.builder()
                .id(1L).email("test@example.com").username("testuser")
                .password("$2a$hashedpw").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.register(form);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("existsByEmail は UserRepository に委譲する")
    void existsByEmail_delegatesToRepository() {
        // Given
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When / Then
        assertThat(userService.existsByEmail("existing@example.com")).isTrue();
        assertThat(userService.existsByEmail("new@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByUsername は UserRepository に委譲する")
    void existsByUsername_delegatesToRepository() {
        // Given
        when(userRepository.existsByUsername("takenuser")).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // When / Then
        assertThat(userService.existsByUsername("takenuser")).isTrue();
        assertThat(userService.existsByUsername("newuser")).isFalse();
    }
}
