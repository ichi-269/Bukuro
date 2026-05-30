# 設計: ホームフィード

## 変更ファイル

### `HomeController.java`
- `FollowService` を inject
- ログイン済みの場合:
  1. `userId` を取得
  2. `followService.getFollowingCount(userId) > 0` でフィード種別を判定
  3. `feedType` を model に追加: `"following"` または `"recommended"`
  4. `feedPosts` は既存の `postService.findFeedForUser(userId)` で取得（変更なし）

### `home/index.html`
- `feedType` の値でフィードヘッダーを分岐:
  - `"following"`: 「フォロー中の最新記事」
  - `"recommended"`: 「おすすめ記事」
- 空状態メッセージを `feedType` で分岐:
  - `"following"` の空: 「フォロー中のユーザーがまだ記事を書いていません」
  - `"recommended"` の空: 「まだ公開記事がありません」（初期状態では非常にまれ）

### `HomeControllerTest.java`（新規）
- `@WebMvcTest(HomeController.class)` + `@Import(SecurityConfig.class)`
- MockBean: UserService, PostService, FollowService, CustomUserDetailsService
- テストケース:
  1. 未認証アクセス → 200 OK、feedPosts モデルなし
  2. フォロー中あり（ログイン済み）→ feedType="following", feedPosts あり
  3. フォロー中なし（ログイン済み）→ feedType="recommended"
