package com.bukuro.dto;

import com.bukuro.entity.ReadingRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ShelfEntryDto {

    private Long id;
    private BookDto book;
    private String status;
    private Integer rating;
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private Long postId;

    public static ShelfEntryDto from(ReadingRecord record, Long postId) {
        return ShelfEntryDto.builder()
                .id(record.getId())
                .book(BookDto.from(record.getBook()))
                .status(record.getStatus().name())
                .rating(record.getRating())
                .startedAt(record.getStartedAt())
                .finishedAt(record.getFinishedAt())
                .postId(postId)
                .build();
    }
}
