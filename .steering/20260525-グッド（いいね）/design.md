# 設計: グッド（いいね）機能

## 新規ファイル

### `Good.java` (entity)
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))`
- フィールド: id, user (ManyToOne LAZY), post (ManyToOne LAZY), createdAt
- `@PrePersist` で createdAt を設定

### `GoodRepository.java`
```java
boolean existsByUserIdAndPostId(Long userId, Long postId);
void deleteByUserIdAndPostId(Long userId, Long postId);
```

### `GoodService.java`
- `addGood(Long userId, Long postId)`:
  1. existsByUserIdAndPostId → true なら DuplicateRecordException
  2. save(Good)
  3. postRepository.incrementGoodCount(postId)
- `removeGood(Long userId, Long postId)`:
  1. existsByUserIdAndPostId → false なら return (冪等)
  2. deleteByUserIdAndPostId
  3. postRepository.decrementGoodCount(postId)
- `hasGooded(Long userId, Long postId)`: boolean

### `GoodController.java`
- `POST /posts/{postId}/good` → addGood → redirect to /posts/{postId}
  - DuplicateRecordException はフラッシュメッセージで表示（"すでにグッド済みです"）
- `POST /posts/{postId}/ungood` → removeGood → redirect to /posts/{postId}

## 変更ファイル

### `PostRepository.java`
```java
@Modifying
@Query("UPDATE Post p SET p.goodCount = p.goodCount + 1 WHERE p.id = :postId")
void incrementGoodCount(@Param("postId") Long postId);

@Modifying
@Query("UPDATE Post p SET p.goodCount = p.goodCount - 1 WHERE p.id = :postId AND p.goodCount > 0")
void decrementGoodCount(@Param("postId") Long postId);
```

### `PostController.java`
- `GoodService` を inject
- `show()` に `hasGooded` モデル属性を追加:
  - ログイン済みの場合: `goodService.hasGooded(userId, postId)`
  - 未ログインの場合: `false`

### `post/show.html`
- グッド数表示エリアをインタラクティブに変更
- ログイン済み + 未グッド: 「グッドする」ボタン（POST /posts/{postId}/good）
- ログイン済み + グッド済み: 「グッド済み」ボタン（POST /posts/{postId}/ungood）
- 未ログイン: グッド数の静的表示のみ
- `xmlns:sec` を `<html>` に追加

### `schema.sql`
```sql
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
```

## SecurityConfig
変更不要。`POST /posts/**` は既存の `.anyRequest().authenticated()` でカバー済み。

## エラーハンドリング
- `DuplicateRecordException`: GoodController でキャッチし `RedirectAttributes` にフラッシュメッセージを設定して /posts/{postId} へリダイレクト
- 存在しない postId: `ResourceNotFoundException` → 既存の 404 ハンドラー
