package com.bukuro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private List<FieldErrorItem> fieldErrors;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldErrorItem {
        private String field;
        private String message;
    }
}
