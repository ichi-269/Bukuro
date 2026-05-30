# 設計: 書名で検索する機能

## フロー

```
[書名入力] → POST /books/search/title
    → NdlApiClient.searchByTitle(keyword) → NDL OpenSearch API
    → 候補一覧表示 (book/title-results)
    → ユーザーが選択 → GET /books/search/confirm?isbn=XXX
    → OpenBdApiClient.searchByIsbn(isbn) で書影補完
    → book/confirm (既存) → /books/add (既存)
```

## 新規ファイル

### `NdlApiClient.java`
- `searchByTitle(String keyword): List<BookDto>`
- NDL OpenSearch API: `https://iss.ndl.go.jp/api/opensearch?title={keyword}&cnt=20&mediatype=1`
  - `mediatype=1`: 図書のみ（雑誌除外）
  - レスポンス: XML（RSS形式）
- XML パース: `RestClient` でレスポンスを `String` 取得後、`javax.xml.parsers` / JAXB で解析
  - 各 `<item>` から `<title>`, `<author>`, `<isbn>` (dc:identifier) を抽出
  - ISBN のない item は除外
- タイムアウト設定: `openbd.api.timeout-seconds` を流用

### `BookTitleSearchService.java`
- `searchByTitle(String keyword): List<BookDto>`
  - NdlApiClient を呼び出し
  - ISBN あり候補のみ返す
- `getBookWithCover(String isbn): BookDto`
  - OpenBdApiClient.searchByIsbn でまず書影補完を試みる
  - OpenBD にデータなければ NDL 検索結果の書誌情報をそのまま使う（coverUrl=null）

### `book/title-results.html`（新規テンプレート）
- 検索キーワードとヒット件数を表示
- 候補リスト: 書名・著者・ISBN をカード形式で表示
- 各候補に「この本を選択」ボタン → `GET /books/search/confirm?isbn=XXX`

## 変更ファイル

### `BookSearchController.java`
- `POST /books/search/title` 追加: キーワード受け取り → 候補一覧表示
- `GET /books/search/confirm` 追加: ISBN 受け取り → OpenBD補完 → `book/confirm` 表示

### `book/search.html`
- 書名検索フォームをタブまたはセクションとして追加
  - `<form action="/books/search/title" method="post">`
  - `name="keyword"` のテキスト入力

### `SecurityConfig.java`
- `/books/search/title` (POST) と `/books/search/confirm` (GET) は既存の `.anyRequest().authenticated()` でカバーされるため変更不要

## XML パース方針

NDL API は RSS XML を返す。Spring の `RestClient` で `String` として受け取り、`DocumentBuilder` (javax.xml) でパースする。
JAXB は依存追加が必要なため、標準 DOM パースを採用。

```
<rss>
  <channel>
    <item>
      <title>書名</title>
      <author>著者</author>  ← dc:creator
      <dc:identifier>ISBN:9784XXXXXXXXX</dc:identifier>
    </item>
    ...
  </channel>
</rss>
```
