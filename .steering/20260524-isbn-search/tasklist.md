# タスクリスト: ISBN検索・書誌情報取得

## フェーズ1: 例外クラス

- [x] BookNotFoundException.java を作成する
- [x] ExternalApiException.java を作成する

## フェーズ2: エンティティ・リポジトリ・DTO

- [x] Book.java エンティティを作成する
- [x] BookRepository.java を作成する（findByIsbn）
- [x] BookDto.java を作成する

## フェーズ3: APIクライアント

- [x] client/ ディレクトリを作成し、OpenBdApiClient.java を作成する（RestClient使用）

## フェーズ4: サービス

- [x] BookSearchService.java を作成する（searchByIsbn, ISBN正規化）

## フェーズ5: コントローラー・エラーハンドラー

- [x] GlobalExceptionHandler.java を作成する（@ControllerAdvice）
- [x] BookSearchController.java を作成する（GET/POST /books/search）

## フェーズ6: テンプレート

- [x] book/search.html を作成する（ISBN入力フォーム）
- [x] book/confirm.html を作成する（書誌情報確認・本棚に追加ボタン）

## フェーズ7: テスト

- [x] BookSearchServiceTest.java を作成する（正常系・未発見・タイムアウト）

## 申し送り

<!-- 振り返り時に記入 -->
