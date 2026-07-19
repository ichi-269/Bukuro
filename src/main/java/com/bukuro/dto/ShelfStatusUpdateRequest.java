package com.bukuro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShelfStatusUpdateRequest {

    @NotBlank(message = "ステータスを指定してください")
    private String status;
}
