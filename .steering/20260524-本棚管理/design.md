# 設計: 本棚管理

## 新規作成ファイル

```
src/main/java/com/bukuro/
├── entity/
│   └── ReadingRecord.java       # reading_records テーブルマッピング + ReadingStatus enum
├── repository/
│   └── ReadingRecordRepository.java
├── exception/
│   └── DuplicateRecordException.java
├── service/
│   └── ShelfService.java
└── controller/
    └── ShelfController.java

src/main/resources/templates/shelf/
└── index.html                   # 本棚一覧（3タブ）

src/main/resources/db/
└── schema.sql                   # DDL参照用（自動実行しない）

src/test/java/com/bukuro/service/
└── ShelfServiceTest.java
```

## 変更ファイル

- `UserService.java` — `getUserByEmail(String email): User` を追加

## データモデル

### ReadingRecord エンティティ

```
reading_records
  id          BIGINT PK AUTO_INCREMENT
  user_id     BIGINT FK → users.id NOT NULL
  book_id     BIGINT FK → books.id NOT NULL
  status      ENUM('WANT_TO_READ','READING','DONE') NOT NULL
  rating      TINYINT NULL  (1〜5、DONEのみ)
  started_at  DATE NULL
  finished_at DATE NULL
  UNIQUE (user_id, book_id)
```

### ReadingStatus enum（ReadingRecord の内部クラス）

```java
public enum ReadingStatus { WANT_TO_READ, READING, DONE }
```

## ShelfService 設計

```java
addToShelf(Long userId, String isbn)
  → BookRepository.findByIsbn(isbn)
    → なければ BookSearchService.searchByIsbn(isbn) → Book保存
  → ReadingRecordRepository.existsByUserIdAndBookId?
    → あれば DuplicateRecordException
  → save(ReadingRecord{status=WANT_TO_READ})

updateStatus(Long recordId, ReadingStatus status, Long userId)
  → findById(recordId).orElseThrow(ResourceNotFoundException)
  → checkOwnership(record, userId)
  → status が DONE 以外 → rating=null, startedAt/finishedAt は保持
  → record.setStatus(status) → save

remove(Long recordId, Long userId)
  → findById(recordId).orElseThrow(ResourceNotFoundException)
  → checkOwnership(record, userId)
  → delete

getShelf(Long userId)
  → findByUserIdOrderByIdDesc(userId)
```

## ShelfController 設計

```java
POST /books/add
  → ShelfService.addToShelf(userId, isbn)
  → 成功: redirect /shelf
  → DuplicateRecordException: redirect /shelf?error=duplicate
  → BookNotFoundException / ExternalApiException: redirect /books/search?error=...

GET /shelf
  → ShelfService.getShelf(userId)
  → 3グループ(WANT_TO_READ / READING / DONE)に分類してモデルへ
  → shelf/index.html

POST /shelf/{recordId}/status
  → ShelfService.updateStatus(recordId, status, userId)
  → redirect /shelf

POST /shelf/{recordId}/delete
  → ShelfService.remove(recordId, userId)
  → redirect /shelf
```

## テンプレート設計（shelf/index.html）

- Bootstrap タブ（3 タブ: 読みたい / 読書中 / 読了）
- 各タブ: ReadingRecord リスト → Book 情報表示
- ステータス変更: セレクトボックス + 送信ボタン（POST フォーム）
- 削除: POST フォーム（削除ボタン）
- 本棚が空: 「本を追加してください」リンク表示

## ResourceNotFoundException 追加

`GlobalExceptionHandler` で `ResourceNotFoundException` → 404 を追加済みにする必要がある。  
現状はコメントのみ（「将来追加」扱い）なので、今回の実装で `ResourceNotFoundException` クラスと GlobalExceptionHandler のハンドリングを追加する。

## DDL（参照用）

```sql
CREATE TABLE reading_records (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    book_id     BIGINT       NOT NULL,
    status      ENUM('WANT_TO_READ','READING','DONE') NOT NULL,
    rating      TINYINT      NULL,
    started_at  DATE         NULL,
    finished_at DATE         NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_book (user_id, book_id),
    CONSTRAINT fk_rr_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_rr_book FOREIGN KEY (book_id) REFERENCES books(id)
);
```
