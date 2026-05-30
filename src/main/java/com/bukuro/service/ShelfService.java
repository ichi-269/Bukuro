package com.bukuro.service;

import com.bukuro.dto.BookDto;
import com.bukuro.entity.Book;
import com.bukuro.entity.ReadingRecord;
import com.bukuro.entity.ReadingRecord.ReadingStatus;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.BookRepository;
import com.bukuro.repository.ReadingRecordRepository;
import com.bukuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ReadingRecordRepository readingRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookSearchService bookSearchService;

    @Transactional
    public ReadingRecord addToShelf(Long userId, String isbn) {
        Book book = bookRepository.findByIsbn(isbn).orElseGet(() -> {
            BookDto dto = bookSearchService.searchByIsbn(isbn);
            return bookRepository.save(Book.builder()
                    .isbn(dto.getIsbn())
                    .title(dto.getTitle())
                    .author(dto.getAuthor())
                    .publisher(dto.getPublisher())
                    .coverUrl(dto.getCoverUrl())
                    .build());
        });

        if (readingRecordRepository.existsByUserIdAndBookId(userId, book.getId())) {
            throw new DuplicateRecordException("この本はすでに本棚に登録されています");
        }

        return readingRecordRepository.save(ReadingRecord.builder()
                .user(userRepository.getReferenceById(userId))
                .book(book)
                .status(ReadingStatus.WANT_TO_READ)
                .build());
    }

    @Transactional
    public ReadingRecord updateStatus(Long recordId, ReadingStatus status, Long userId) {
        ReadingRecord record = findRecord(recordId);
        checkOwnership(record, userId);
        record.setStatus(status);
        if (status != ReadingStatus.DONE) {
            record.setRating(null);
        }
        return readingRecordRepository.save(record);
    }

    @Transactional
    public void remove(Long recordId, Long userId) {
        ReadingRecord record = findRecord(recordId);
        checkOwnership(record, userId);
        readingRecordRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public List<ReadingRecord> getShelf(Long userId) {
        return readingRecordRepository.findByUserIdOrderByIdDesc(userId);
    }

    private ReadingRecord findRecord(Long recordId) {
        return readingRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("読書記録が見つかりません: " + recordId));
    }

    private void checkOwnership(ReadingRecord record, Long userId) {
        if (!record.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("この操作は許可されていません");
        }
    }
}
