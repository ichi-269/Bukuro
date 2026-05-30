package com.bukuro.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String coverUrl;
}
