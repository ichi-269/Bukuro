package com.bukuro.controller;

import com.bukuro.dto.BookConfirmRequest;
import com.bukuro.dto.BookDto;
import com.bukuro.dto.IsbnRequest;
import com.bukuro.dto.TitleSearchRequest;
import com.bukuro.service.BookSearchService;
import com.bukuro.service.BookTitleSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookSearchController {

    private final BookSearchService bookSearchService;
    private final BookTitleSearchService bookTitleSearchService;

    @PostMapping("/search")
    public BookDto search(@Valid @RequestBody IsbnRequest request) {
        return bookSearchService.searchByIsbn(request.getIsbn());
    }

    @PostMapping("/search/title")
    public List<BookDto> searchByTitle(@Valid @RequestBody TitleSearchRequest request) {
        return bookTitleSearchService.searchByTitle(request.getKeyword());
    }

    @PostMapping("/search/confirm")
    public BookDto confirmFromTitle(@Valid @RequestBody BookConfirmRequest request) {
        return bookTitleSearchService.getBookWithCover(
                request.getIsbn(), request.getTitle(), request.getAuthor(), request.getPublisher());
    }
}
