package com.bukuro.service;

import com.bukuro.dto.ProfileEditForm;
import com.bukuro.dto.RegisterForm;
import com.bukuro.entity.User;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @DisplayName("updateProfile: 正常なフォームでユーザー情報が更新される")
    void updateProfile_validForm_updatesUser() {
        // Given
        User user = User.builder().id(1L).email("test@example.com").username("oldname").build();
        ProfileEditForm form = new ProfileEditForm();
        form.setUsername("newname");
        form.setBio("新しい自己紹介");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.updateProfile(1L, form);

        // Then
        assertThat(result.getUsername()).isEqualTo("newname");
        assertThat(result.getBio()).isEqualTo("新しい自己紹介");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateProfile: 他ユーザーが使用中のユーザー名は IllegalStateException")
    void updateProfile_duplicateUsername_throwsIllegalStateException() {
        // Given
        User currentUser = User.builder().id(1L).username("myname").build();
        User otherUser = User.builder().id(2L).username("takenname").build();
        ProfileEditForm form = new ProfileEditForm();
        form.setUsername("takenname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("takenname")).thenReturn(Optional.of(otherUser));

        // When / Then
        assertThatThrownBy(() -> userService.updateProfile(1L, form))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("takenname");
    }

    @Test
    @DisplayName("updateProfile: 自分の現在のユーザー名はそのまま使用できる")
    void updateProfile_sameUsername_succeeds() {
        // Given
        User user = User.builder().id(1L).username("myname").build();
        ProfileEditForm form = new ProfileEditForm();
        form.setUsername("myname");
        form.setBio(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("myname")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.updateProfile(1L, form);

        // Then
        assertThat(result.getUsername()).isEqualTo("myname");
        assertThat(result.getBio()).isNull();
    }

    @Test
    @DisplayName("updateProfile: 存在しないユーザーIDは ResourceNotFoundException")
    void updateProfile_userNotFound_throwsResourceNotFoundException() {
        // Given
        ProfileEditForm form = new ProfileEditForm();
        form.setUsername("anyname");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.updateProfile(99L, form))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
