# タスクリスト: ホームフィード

## フェーズ1: HomeController 更新

- [x] HomeController.java を更新する（FollowService inject、feedType をモデルに追加）

## フェーズ2: テンプレート更新

- [x] home/index.html を更新する（feedType でヘッダー・空状態メッセージを分岐）

## フェーズ3: テスト

- [x] HomeControllerTest.java を作成する（未認証・フォロー中あり・フォロー中なし）

## 申し送り

### 実装完了日
2026-05-26

### 計画と実績の差分
- 計画通り3フェーズ（HomeController更新 → テンプレート更新 → テスト）で完了
- テストケースは設計書の3件から5件に増加（空フィードの境界ケースを追加）
- 差分なし、スコープ追加のみ

### 学んだこと
- `feedType` の判定ロジックは HomeController に置き、PostService は feedPosts の取得のみに専念させる分離が有効。PostService.findFeedForUser はフォロイーの有無を返さないため、HomeController が別途 `getFollowingCount()` を呼ぶ設計になっているが、これは許容範囲内の冗長性
- `th:if="${feedType != 'following'}"` のような否定条件は null 時の挙動が曖昧になるため、`th:if="${feedType == 'recommended'}"` のような肯定条件に統一する

### 次回への改善提案
- フィードのページネーション（現状は上位20件固定）
- フォロー中フィードが空の場合におすすめ記事にフォールバックする UX の検討
