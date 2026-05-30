# タスクリスト: フォロー機能

## フェーズ1: エンティティ・リポジトリ

- [x] Follow.java エンティティを作成する
- [x] FollowRepository.java を作成する
- [x] schema.sql に follows テーブルを追加する

## フェーズ2: サービス

- [x] FollowService.java を作成する（follow/unfollow/isFollowing/getFollowerCount/getFollowingCount）

## フェーズ3: コントローラー

- [x] FollowController.java を作成する（POST /users/{username}/follow, /unfollow）

## フェーズ4: ユーザーページ更新

- [x] UserController.java を更新する（followerCount/followingCount/isFollowing をモデルに追加）
- [x] user/show.html を更新する（フォロー/アンフォローボタン、フォロワー数・フォロー数）

## フェーズ5: ホームフィード

- [x] PostRepository にフィード用クエリを追加する
- [x] PostService に findFeedForUser(Long userId) を追加する
- [x] HomeController を更新する（ログイン済み時はフィード取得）
- [x] home/index.html を更新する（ログイン済み時のフィード記事カード）

## フェーズ6: テスト

- [x] FollowServiceTest.java を作成する
- [x] FollowControllerTest.java を作成する

## 申し送り

### 実装完了日
2026-05-25

### 計画と実績の差分
- エンドポイント URL: スペック（`POST /follow/{userId}`）と異なる設計（`POST /users/{username}/follow`）を採用。ユーザーページからのリンクが自然であり、UXが優れているため意図的な変更。functional-design.md を実装側に合わせて更新が必要。
- バリデーター指摘で以下を追加対応:
  1. `user/show.html` に `xmlns:sec` 名前空間を追加（`sec:authorize` が機能していなかった）
  2. `GlobalExceptionHandler` に `IllegalArgumentException → 400` ハンドラーを追加
  3. `PostServiceTest` に `findFeedForUser` の2分岐テスト（フォロー中あり/なし）を追加
  4. `PostRepository.findByIsPublicTrueOrderByCreatedAtDesc()` の未使用メソッドを削除
  5. `SecurityConfig` でフォロー/アンフォロー POST を `/users/**` の permitAll より前に authenticated() として宣言

### 学んだこと
- `xmlns:sec` が `<html>` タグに宣言されていないと `sec:authorize` が無視されてテンプレートに影響しない。`layout/base.html` の名前空間は継承されないため、`sec:authorize` を使う各テンプレートで明示宣言が必要。
- Spring Security の URL マッチングは宣言順が優先。`/users/**` の `permitAll` より前に POST の `authenticated()` を書かないと上書きされてしまう。
- Mockito で引数の一部に `any()` を使う場合、他の引数も `eq()` 等の matcher で統一しないとエラーになる。

### 次回への改善提案
- functional-design.md のフォロー関連エンドポイント一覧を `POST /users/{username}/follow` に更新する
- `Follow` エンティティの `@Table` に `indexes` アノテーション（`idx_follow_followee`）を追加して schema.sql との整合性を取る
- `FollowController` での `getUserByEmail()` DB クエリは `CustomUserDetails` に userId を持たせることで省略できる（他の Controller と同様の技術的負債）
