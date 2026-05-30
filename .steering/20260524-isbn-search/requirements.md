# 要求内容: ISBN検索・書誌情報取得

## 機能概要

ISBNを入力してOpenBD APIから書誌情報（タイトル・著者・出版社・書影）を取得し、
確認画面に表示するまでのフロー。「本棚に追加」ボタンは配置するが、
実際の本棚追加ロジックは次の機能（本棚管理）で実装する。

## 受け入れ条件（PRDより）

- [ ] ISBN-13またはISBN-10を入力フォームに入力できる
- [ ] OpenBD APIからタイトル・著者・出版社・書影URLを取得して画面に表示する
- [ ] 該当書籍が見つからない場合はエラーメッセージを表示する
- [ ] OpenBD APIがタイムアウトした場合はエラーメッセージを表示してサービスは継続稼働する
- [ ] APIから取得した書誌情報はユーザーが編集できない（OpenBD利用規約準拠）

## スコープ

今回の実装範囲:
1. 例外クラス: `BookNotFoundException`, `ExternalApiException`
2. DTO: `BookDto`（ISBN検索結果表示用）
3. エンティティ: `Book` + `BookRepository`
4. クライアント: `OpenBdApiClient`（`client/`パッケージ）
5. サービス: `BookSearchService`
6. コントローラー: `BookSearchController`（GET/POST /books/search）
7. 共通エラーハンドラー: `GlobalExceptionHandler`
8. テンプレート: `book/search.html`, `book/confirm.html`
9. テスト: `BookSearchServiceTest`

## スコープ外

- POST /books/add（本棚への追加）→ 次の「本棚管理」フィーチャーで実装
