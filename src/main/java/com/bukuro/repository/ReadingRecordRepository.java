package com.bukuro.repository;

import com.bukuro.entity.ReadingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {

    List<ReadingRecord> findByUserIdOrderByIdDesc(Long userId);

    Optional<ReadingRecord> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
