# タスクリスト: フォロワー・フォロー一覧

## 申し送り

### 実装完了日
2026-05-30

### 計画と実績の差分
- 設計書に `listType` モデル属性（"followers"/"following"）を追加する案があったが、テンプレートでは不使用のため実装を省いた。design.md 上の記述と乖離あり。
- バリデーターの指摘で `/following` エンドポイントの404テストが漏れており、実装後に追加した。

### 学んだこと
- `SecurityConfig` の `permitAll` が `/users/**` に設定済みであれば、`/users/{username}/followers` 等の新規 GET エンドポイントは追加設定不要で未認証アクセスが許可される。
- 対称なエンドポイント（/followers・/following）に対してはテストも対称にそろえること。片方だけのテストになりやすいので注意。

### 次回への改善提案
- 一覧にユーザー名のみ表示しているが、フォローボタンを合わせて表示すると UX が向上する（認証済みユーザーに対してのみ）。

- [x] FollowRepository: User を返す JPQL クエリを2件追加する
- [x] FollowService: getFollowers / getFollowees メソッドを追加する
- [x] UserController: /followers と /following エンドポイントを追加する
- [x] user/followers.html: フォロワー一覧テンプレートを作成する
- [x] user/following.html: フォロー中一覧テンプレートを作成する
- [x] user/show.html: フォロワー数・フォロー中数をリンク化する
- [x] FollowServiceTest: getFollowers / getFollowees のテストを追加する
- [x] UserControllerTest: /followers・/following エンドポイントのテストを追加する
