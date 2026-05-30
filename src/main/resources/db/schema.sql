-- Bukuro データベーススキーマ
-- このファイルは参照用 DDL です。自動実行はされません。
-- 開発環境やマイグレーション時に手動で適用してください。

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    bio        TEXT         NULL,
    created_at TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS books (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    isbn       VARCHAR(13)   NOT NULL,
    title      VARCHAR(500)  NOT NULL,
    author     VARCHAR(500)  NOT NULL,
    publisher  VARCHAR(255)  NULL,
    cover_url  VARCHAR(1000) NULL,
    created_at TIMESTAMP     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_books_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reading_records (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    book_id     BIGINT       NOT NULL,
    status      ENUM('WANT_TO_READ','READING','DONE') NOT NULL,
    rating      TINYINT      NULL,
    started_at  DATE         NULL,
    finished_at DATE         NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_book (user_id, book_id),
    CONSTRAINT fk_rr_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_rr_book FOREIGN KEY (book_id) REFERENCES books(id),
    INDEX idx_rr_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS posts (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    book_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    is_public  BOOLEAN      NOT NULL DEFAULT FALSE,
    good_count INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_posts_book FOREIGN KEY (book_id) REFERENCES books(id),
    INDEX idx_posts_user_created (user_id, created_at),
    INDEX idx_posts_public_created (is_public, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS follows (
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    follower_id BIGINT    NOT NULL,
    followee_id BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_follow (follower_id, followee_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT fk_follow_followee FOREIGN KEY (followee_id) REFERENCES users(id),
    INDEX idx_follow_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods (
    id         BIGINT    NOT NULL AUTO_INCREMENT,
    user_id    BIGINT    NOT NULL,
    post_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_good (user_id, post_id),
    CONSTRAINT fk_good_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_good_post FOREIGN KEY (post_id) REFERENCES posts(id),
    INDEX idx_good_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
