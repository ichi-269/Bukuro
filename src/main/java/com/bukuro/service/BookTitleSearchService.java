package com.bukuro.service;

import com.bukuro.client.NdlApiClient;
import com.bukuro.client.OpenBdApiClient;
import com.bukuro.dto.BookDto;
import com.bukuro.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookTitleSearchService {

    private final NdlApiClient ndlApiClient;
    private final OpenBdApiClient openBdApiClient;

    public List<BookDto> searchByTitle(String keyword) {
        return ndlApiClient.searchByTitle(keyword);
    }

    public BookDto getBookWithCover(String isbn, String ndlTitle, String ndlAuthor, String ndlPublisher) {
        try {
            return openBdApiClient.searchByIsbn(isbn)
                    .orElseGet(() -> buildFromNdl(isbn, ndlTitle, ndlAuthor, ndlPublisher));
        } catch (ExternalApiException e) {
            log.warn("OpenBD lookup failed for ISBN {}, falling back to NDL data: {}", isbn, e.getMessage());
            return buildFromNdl(isbn, ndlTitle, ndlAuthor, ndlPublisher);
        }
    }

    private BookDto buildFromNdl(String isbn, String title, String author, String publisher) {
        return BookDto.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .publisher(publisher)
                .coverUrl(null)
                .build();
    }
}
