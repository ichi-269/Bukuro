# タスクリスト: ユーザーページ

## フェーズ1: リポジトリ・サービス拡張

- [x] PostRepository に findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId) を追加する
- [x] UserService に getUserByUsername(String username): User を追加する

## フェーズ2: コントローラー

- [x] UserController.java を作成する（GET /users/{username}）

## フェーズ3: テンプレート

- [x] user/show.html を作成する（プロフィール + 公開記事一覧）

## フェーズ4: テスト

- [x] UserControllerTest.java を作成する（正常系・404・未認証アクセス）

## 申し送り

### 実装完了日
2026-05-25

### 計画と実績の差分
- 計画通りにフェーズ1〜4を完了
- 実装後バリデーター指摘により追加修正が発生: `UserController` が `PostRepository` を直接 inject していたレイヤー違反を修正
  - `PostService.findPublicByUserId()` を追加し、`UserController` は `PostService` のみに依存する形に変更
  - `UserControllerTest` も `PostRepository` モックを `PostService` モックに差し替え
  - `isOwnPage=true/false` の確認テストケースを2件追加（合計5件）

### 学んだこと
- `@WebMvcTest` で Controller がバリデーターにより修正されると、テストの `@MockBean` も一緒に更新が必要
- `SecurityMockMvcRequestPostProcessors.user()` でメールアドレスを principal の username に設定することで、`isOwnPage` の認証済みシナリオをテスト可能

### 次回への改善提案
- `getUserId()` が DB SELECT を発行している問題（`ShelfController` / `PostController` 共通）は今後 `CustomUserDetails` に userId を持たせることで解消できる
- `Post` エンティティを Thymeleaf に直接渡しているため、`LAZY` 関連でビューレンダリング時に N+1 が発生しうる。DTO への変換を検討する
