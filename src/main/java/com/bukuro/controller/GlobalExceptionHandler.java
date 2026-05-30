package com.bukuro.controller;

import com.bukuro.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // BookNotFoundException / ExternalApiException は各Controllerでローカルハンドリング済み
    // （検索フォームへ戻してエラーメッセージ表示という UX を維持するため）

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBadRequest() {
        return "error/400";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoStaticResource(HttpServletResponse response) throws java.io.IOException {
        response.sendError(HttpStatus.NOT_FOUND.value());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound() {
        return "error/404";
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleAccessDenied() {
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("errorMessage", "予期しないエラーが発生しました");
        return "error/500";
    }
}
