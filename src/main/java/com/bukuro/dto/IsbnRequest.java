package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsbnRequest {

    @NotBlank(message = "ISBNを入力してください")
    private String isbn;
}
