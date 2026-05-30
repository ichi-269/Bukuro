package com.bukuro.service;

import com.bukuro.client.OpenBdApiClient;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final OpenBdApiClient openBdApiClient;

    public BookDto searchByIsbn(String isbn) {
        String normalizedIsbn = normalizeIsbn(isbn);
        if (!isValidIsbnFormat(normalizedIsbn)) {
            throw new BookNotFoundException(normalizedIsbn);
        }
        return openBdApiClient.searchByIsbn(normalizedIsbn)
                .orElseThrow(() -> new BookNotFoundException(normalizedIsbn));
    }

    // ハイフン・スペースを除去してISBNを正規化する
    String normalizeIsbn(String isbn) {
        return isbn.replaceAll("[\\s\\-]", "");
    }

    // ISBN-10 または ISBN-13 の桁数チェック
    private static boolean isValidIsbnFormat(String isbn) {
        return isbn.matches("\\d{10}") || isbn.matches("\\d{13}");
    }
}
