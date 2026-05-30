package com.bukuro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // フォロー・アンフォロー・グッド操作は認証必須（permitAllルールより先に評価させる）
                .requestMatchers(org.springframework.http.HttpMethod.POST,
                        "/users/*/follow", "/users/*/unfollow",
                        "/posts/*/good", "/posts/*/ungood").authenticated()
                .requestMatchers(
                        "/", "/login", "/register",
                        "/users/**", "/books/{bookId}",
                        "/css/**", "/js/**", "/images/**").permitAll()
                // 記事詳細（公開）のみ未認証でアクセス可。作成・編集・削除は認証必須
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/posts/{postId:[\\d]+}").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(org.springframework.security.config.Customizer.withDefaults());

        return http.build();
    }
}
