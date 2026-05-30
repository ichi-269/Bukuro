package com.bukuro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OpenBD利用規約によりユーザーによる書誌情報の編集は禁止
    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500)
    private String author;

    @Column(length = 255)
    private String publisher;

    @Column(name = "cover_url", length = 1000)
    private String coverUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
