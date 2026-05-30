# タスクリスト: ホーム画面の機能拡充

## フェーズ1: バックエンド

- [x] PostService.java に findFollowingFeed / findRecommendedFeed を追加し findFeedForUser を削除する
- [x] HomeController.java を feed パラメータ対応・hasFollowees モデル追加に変更する

## フェーズ2: テンプレート

- [x] home/index.html にタブUIを追加し hasFollowees で表示制御する

## フェーズ3: テスト

- [x] HomeControllerTest.java を新しいサービスメソッドに合わせて更新・テスト追加する

## 申し送り

### 実装完了日
2026-05-30

### 計画と実績の差分
- タスクリストから `PostServiceTest` の更新が漏れており、バリデーターが検出した。追加で修正済み。
- テンプレートの空状態メッセージの重複 `th:if` も合わせて整理した。

### 学んだこと
- `PostService.findFeedForUser` のように「判断＋取得」が混在していたメソッドを分離すると、テストと Controller の両方で意図が明確になる。
- メソッド削除時は、削除メソッドを呼ぶテストも必ずセットで更新する必要がある（タスクリストに明示すべき）。
- Thymeleaf タブは `th:classappend` で active クラスを動的に付与するだけでシンプルに実装できる。

### 次回への改善提案
- `FollowService.hasAnyFollowee(Long userId)` を追加して `getFollowingCount > 0` より意図を明確にする（パフォーマンス改善も兼ねる）
- `functional-design.md` のシーケンス図（PostService のメソッド）を実装に合わせて更新する
