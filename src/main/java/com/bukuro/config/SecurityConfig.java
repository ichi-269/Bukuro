package com.bukuro.config;

import com.bukuro.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.io.IOException;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // フォロー・アンフォロー・グッド操作は認証必須（permitAllルールより先に評価させる）
                .requestMatchers(HttpMethod.POST,
                        "/api/users/*/follow", "/api/users/*/unfollow",
                        "/api/posts/*/good", "/api/posts/*/ungood").authenticated()
                // 記事詳細（公開）のみ未認証でアクセス可
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId:[\\d]+}").permitAll()
                .requestMatchers(
                        "/api/login", "/api/register", "/api/me",
                        "/api/users/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                // /api 以外は SPA シェル（index.html）・静的アセットのため常に配信可能。
                // 保護すべきデータは全て /api 経由となるため、認可はここでは行わない
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/login")
                .successHandler((request, response, authentication) ->
                        writeJson(response, HttpStatus.OK, Map.of("status", "ok")))
                .failureHandler((request, response, exception) ->
                        writeJson(response, HttpStatus.UNAUTHORIZED, ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .code("AUTHENTICATION_FAILED")
                                .message("メールアドレスまたはパスワードが正しくありません")
                                .build()))
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                        writeJson(response, HttpStatus.OK, Map.of("status", "ok")))
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                        writeJson(response, HttpStatus.UNAUTHORIZED, ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .code("UNAUTHENTICATED")
                                .message("ログインが必要です")
                                .build()))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        writeJson(response, HttpStatus.FORBIDDEN, ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .code("ACCESS_DENIED")
                                .message("この操作は許可されていません")
                                .build()))
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
