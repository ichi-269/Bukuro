# 設計: ISBN検索・書誌情報取得

## ディレクトリ構造（追加分）

```
src/main/java/com/bukuro/
├── client/
│   └── OpenBdApiClient.java         ← NEW
├── controller/
│   ├── BookSearchController.java    ← NEW
│   └── GlobalExceptionHandler.java  ← NEW
├── dto/
│   └── BookDto.java                 ← NEW
├── entity/
│   └── Book.java                    ← NEW
├── exception/
│   ├── BookNotFoundException.java   ← NEW
│   └── ExternalApiException.java    ← NEW
├── repository/
│   └── BookRepository.java          ← NEW
└── service/
    └── BookSearchService.java       ← NEW

src/main/resources/templates/book/
    ├── search.html                  ← NEW
    └── confirm.html                 ← NEW

src/test/java/com/bukuro/service/
    └── BookSearchServiceTest.java   ← NEW
```

## OpenBD API レスポンス構造

```json
[
  {
    "summary": {
      "isbn": "9784...",
      "title": "書名",
      "author": "著者名",
      "publisher": "出版社",
      "cover": "https://cover.openbd.jp/..."
    }
  }
]
```

取得できない場合: `[null]` または空配列 `[]` が返る。

## クラス設計

### BookDto
- isbn: String
- title: String
- author: String
- publisher: String
- coverUrl: String（null許容）

### Book エンティティ
- id (BIGINT PK)
- isbn (VARCHAR(13) UNIQUE)
- title (VARCHAR(500))
- author (VARCHAR(500))
- publisher (VARCHAR(255) NULL)
- cover_url (VARCHAR(1000) NULL)
- created_at (TIMESTAMP)

### OpenBdApiClient
- `RestClient` を使用（Spring 6.1+ / Spring Boot 3.2+）
- タイムアウト: `${openbd.api.timeout-seconds}` プロパティ（3秒）
- `searchByIsbn(String isbn): Optional<BookDto>`
- エンドポイント: `GET https://api.openbd.jp/v1/get?isbn={isbn}`
- Jacksonで `OpenBdResponse` にデシリアライズ後 `BookDto` に変換

### BookSearchService
- `searchByIsbn(String isbn): BookDto`
  - ISBNを正規化（ハイフン除去）
  - OpenBdApiClient を呼び出す
  - 未発見時: `BookNotFoundException` をスロー
  - API失敗時: `ExternalApiException` をスロー（ExternalApiException はランタイム）

### BookSearchController
- `GET /books/search` → `book/search` テンプレート
- `POST /books/search` → `BookSearchService.searchByIsbn()` → `book/confirm` テンプレート
  - エラー時: モデルにエラーメッセージ追加してフォームを再表示

### GlobalExceptionHandler (@ControllerAdvice)
- `BookNotFoundException` → 404ページ
- `AccessDeniedException` → 403ページ
- `Exception` → 500ページ

## ISBN正規化

- ハイフン・スペースを除去
- ISBN-10 (10桁) は OpenBD API にそのまま渡してOK（OpenBD側が対応）
- ISBN-13 (13桁) も同様

## RestClient設定

`OpenBdApiClient` 内で `RestClient.builder()` を使い、タイムアウトは
`ClientHttpRequestFactorySettings` で設定。`@Value` でタイムアウト秒数を注入。
