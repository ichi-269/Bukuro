# タスクリスト: 書名で検索する機能

## フェーズ1: バックエンド

- [x] NdlApiClient.java を作成する（NDL OpenSearch API 呼び出し・XML パース）
- [x] BookTitleSearchService.java を作成する（書名検索 + OpenBD 書影補完）
- [x] BookSearchController.java に POST /books/search/title と POST /books/search/confirm を追加する

## フェーズ2: テンプレート

- [x] book/title-results.html を作成する（候補一覧表示）
- [x] book/search.html に書名検索フォームを追加する

## フェーズ3: テスト

- [x] NdlApiClientTest.java を作成する（XMLパースのユニットテスト）
- [x] BookTitleSearchServiceTest.java を作成する
- [x] BookSearchControllerTest.java に書名検索エンドポイントのテストを追加する

## 申し送り

### 実装完了日
2026-05-29

### 計画と実績の差分
- エンドポイント: design.md の `GET /books/search/confirm?isbn=XXX` → 実装は `POST /books/search/confirm`（isbn/title/author/publisher をフォーム送信）。セッション不要でNDLデータのフォールバックができるため実装の方が優れていた。
- NDL URL: design.md の `iss.ndl.go.jp`（旧）→ 実装は `ndlsearch.ndl.go.jp`（現行）
- `mediatype=1` パラメータ: design.md に記載があったが、事前調査で新NDLシステムでは動作しないことを確認済みのため、`<category>図書</category>` のクライアント側フィルタを採用。
- テストケース: 計画より7ケース多い（バリデーション境界ケース追加）
- セキュリティ対応で `parseXml` に XXE 無効化を追加（設計外だったが必須対応）
- `ndl.api.timeout-seconds` プロパティを `application.properties` に追加（`openbd.api.timeout-seconds` の流用をやめた）

### 学んだこと
- NDL OpenSearch API は `mediatype=1` が新システムで機能しないため、クライアント側で `<category>図書</category>` フィルタが必要
- `DocumentBuilderFactory` の XXE は明示的に無効化しないとデフォルトで有効なため、外部 XML をパースするときは必ず設定が必要
- `Boolean isPublic`（ラッパー型）は Lombok で `getIsPublic()`/`setIsPublic()` が生成されるが、テストコードは `setPublic()` と書いてしまいがち。今回 PostServiceTest の既存バグも合わせて修正した。
- `parseXml` を package-private にするだけで HTTP モックなしで XML パースのユニットテストが書けるシンプルな設計

### 次回への改善提案
- NDL API の `mediatype` パラメータの動作は定期的に再確認する（仕様変更の可能性あり）
- design.md の NDL API URL とエンドポイント設計を実装に合わせて更新しておく
