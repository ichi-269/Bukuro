# タスクリスト: グッド（いいね）機能

## フェーズ1: エンティティ・リポジトリ

- [x] Good.java エンティティを作成する
- [x] GoodRepository.java を作成する
- [x] PostRepository.java に incrementGoodCount / decrementGoodCount を追加する
- [x] schema.sql に goods テーブルを追加する

## フェーズ2: サービス

- [x] GoodService.java を作成する（addGood/removeGood/hasGooded）

## フェーズ3: コントローラー

- [x] GoodController.java を作成する（POST /posts/{postId}/good, /ungood）
- [x] PostController.java を更新する（show に hasGooded をモデルに追加）

## フェーズ4: テンプレート更新

- [x] post/show.html を更新する（グッド/グッド取り消しボタン、sec:authorize 追加）

## フェーズ5: テスト

- [x] GoodServiceTest.java を作成する
- [x] GoodControllerTest.java を作成する

## 申し送り

### 実装完了日
2026-05-25

### 計画と実績の差分
- 計画通りフェーズ1〜5を完了
- バリデーター指摘で追加対応:
  1. SecurityConfig に `/posts/*/good`, `/posts/*/ungood` の明示的 authenticated() ルールを追加
  2. `GoodControllerTest` にフラッシュ属性の検証（`warningMessage`）を追加
  3. `GoodControllerTest` に未認証 ungood のリダイレクトテストを追加
- Follow と異なりグッドは冪等ではなく DuplicateRecordException を投げる設計を採用。UIのトグルボタンで通常は発生しないが、二重投票時に警告メッセージを表示するUXを実現

### 学んだこと
- `@Modifying` クエリと通常の `save()` を同一 `@Transactional` 内で組み合わせることでカウント更新の整合性を保証できる
- `AND p.goodCount > 0` ガードにより DB レベルで負値を防止できる（アプリ側での防御不要）
- フラッシュ属性のテストには `MockMvcResultMatchers.flash()` を使う

### 次回への改善提案
- `PostController` が `BookRepository` に直接依存している（レイヤー違反）は既存問題として残存。`PostService` にブックアクセスを移動する対応が別途必要
- `GoodService.addGood` と `GoodController.good` の `ResourceNotFoundException` ハンドリングの方針を統一するか検討（現状は 404 ページに飛ぶ）
