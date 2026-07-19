package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookConfirmRequest {

    @NotBlank(message = "ISBNが指定されていません")
    private String isbn;

    private String title;
    private String author;
    private String publisher;
}
