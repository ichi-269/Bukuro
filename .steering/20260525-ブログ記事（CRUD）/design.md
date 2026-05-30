# 設計: ブログ記事（CRUD）

## 新規作成ファイル

```
src/main/java/com/bukuro/
├── entity/
│   └── Post.java
├── repository/
│   └── PostRepository.java
├── dto/
│   └── PostForm.java              # フォームバインディング用 DTO
├── service/
│   └── PostService.java
└── controller/
    └── PostController.java

src/main/resources/templates/post/
├── new.html     # 記事作成フォーム
├── show.html    # 記事詳細
└── edit.html    # 記事編集フォーム

src/test/java/com/bukuro/service/
└── PostServiceTest.java
```

## 変更ファイル

- `src/main/resources/db/schema.sql` — posts DDL 追加
- `src/main/resources/templates/shelf/index.html` — 各書籍カードに「記事を書く」リンク追加

## Post エンティティ

```java
@Entity @Table(name = "posts")
public class Post {
    @Id @GeneratedValue BIGINT id
    @ManyToOne(LAZY) User user                    // user_id FK
    @ManyToOne(LAZY) Book book                    // book_id FK
    @Column(nullable=false, length=255) String title
    @Column(nullable=false, columnDefinition="TEXT") String body
    @Column(name="is_public", nullable=false) boolean isPublic = false
    @Column(name="good_count", nullable=false) int goodCount = 0
    @Column(name="created_at", nullable=false, updatable=false) LocalDateTime createdAt
    @Column(name="updated_at", nullable=false) LocalDateTime updatedAt
    @PrePersist: createdAt = updatedAt = now()
    @PreUpdate:  updatedAt = now()
}
```

## PostForm DTO（バリデーション付き）

```java
public class PostForm {
    @NotBlank @Size(max=255) String title
    @NotBlank String body
    boolean isPublic
}
```

## PostService メソッド設計

```java
create(Long userId, Long bookId, PostForm form): Post
  → Book と User を ReferenceById で取得
  → Post 保存

update(Long postId, PostForm form, Long userId): Post
  → findById → checkOwnership
  → フィールド更新 → save

delete(Long postId, Long userId)
  → findById → checkOwnership
  → delete

findById(Long postId): Post
  → findById or throw ResourceNotFoundException

findPublicOrOwn(Long postId, Long userId): Post
  → findById
  → if !post.isPublic && (userId == null || !userId.equals(post.user.id))
       throw ResourceNotFoundException（存在を隠す）
  → return post

findByUserId(Long userId): List<Post>
  → findByUserIdOrderByCreatedAtDesc
```

## PostController 設計

```java
GET  /posts/new?bookId={bookId}
  → BookRepository.findById(bookId) or throw ResourceNotFoundException
  → model: book, PostForm
  → post/new.html

POST /posts  (bookId hidden field, PostForm)
  → @Valid PostForm → BindingResult
  → if errors: 再表示（book も model に追加）
  → PostService.create(userId, bookId, form)
  → redirect /posts/{id}

GET  /posts/{postId}
  → PostService.findPublicOrOwn(postId, userId or null)
  → post/show.html

GET  /posts/{postId}/edit
  → PostService.findById → checkOwnership（他人ならAccessDenied → 403）
  → PostForm を Post から生成
  → post/edit.html

POST /posts/{postId}/edit  (PostForm)
  → @Valid → BindingResult
  → if errors: 再表示
  → PostService.update
  → redirect /posts/{postId}

POST /posts/{postId}/delete
  → PostService.delete(postId, userId)
  → redirect /shelf
```

## テンプレート設計

### post/new.html
- 書籍情報（カード形式：書影、タイトル、著者）
- フォーム：title (input), body (textarea, rows=10), isPublic (checkbox)
- 送信ボタン「投稿する」
- bookId を hidden field

### post/show.html
- 書籍情報カード
- 記事タイトル（h2）
- 公開/非公開バッジ
- 本文（pre-line で改行保持）
- 著者名 + 投稿日
- グッド数（静的表示、0）
- 本人に編集/削除ボタン表示（sec:authorize は使わず userId 比較）

### post/edit.html
- フォーム：title, body, isPublic（pre-fill）

## DDL（schema.sql 追記）

```sql
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
    INDEX idx_posts_user_created (user_id, created_at DESC),
    INDEX idx_posts_public_created (is_public, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
