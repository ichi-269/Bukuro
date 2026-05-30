package com.bukuro.controller;

import com.bukuro.dto.BookDto;
import com.bukuro.exception.BookNotFoundException;
import com.bukuro.exception.ExternalApiException;
import com.bukuro.service.BookSearchService;
import com.bukuro.service.BookTitleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookSearchController {

    private final BookSearchService bookSearchService;
    private final BookTitleSearchService bookTitleSearchService;

    @GetMapping("/books/search")
    public String searchForm() {
        return "book/search";
    }

    @PostMapping("/books/search")
    public String search(@RequestParam String isbn, Model model) {
        if (isbn == null || isbn.isBlank()) {
            model.addAttribute("errorMessage", "ISBNを入力してください");
            return "book/search";
        }

        try {
            BookDto book = bookSearchService.searchByIsbn(isbn);
            model.addAttribute("book", book);
            return "book/confirm";
        } catch (BookNotFoundException e) {
            model.addAttribute("errorMessage", "この書籍はOpenBDに登録されていません。ISBNを確認してください。");
            model.addAttribute("isbn", isbn);
            return "book/search";
        } catch (ExternalApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isbn", isbn);
            return "book/search";
        }
    }

    @PostMapping("/books/search/title")
    public String searchByTitle(@RequestParam String keyword, Model model) {
        if (keyword == null || keyword.isBlank()) {
            model.addAttribute("errorMessage", "書名を入力してください");
            return "book/search";
        }

        try {
            List<BookDto> candidates = bookTitleSearchService.searchByTitle(keyword);
            model.addAttribute("keyword", keyword);
            model.addAttribute("candidates", candidates);
            return "book/title-results";
        } catch (ExternalApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("keyword", keyword);
            return "book/search";
        }
    }

    @PostMapping("/books/search/confirm")
    public String confirmFromTitle(
            @RequestParam String isbn,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            Model model) {

        if (isbn == null || isbn.isBlank()) {
            model.addAttribute("errorMessage", "ISBNが指定されていません");
            return "book/search";
        }

        try {
            BookDto book = bookTitleSearchService.getBookWithCover(isbn, title, author, publisher);
            model.addAttribute("book", book);
            return "book/confirm";
        } catch (ExternalApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "book/search";
        }
    }
}
