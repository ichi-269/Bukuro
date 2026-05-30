# タスクリスト: 本棚管理

## フェーズ1: 例外クラス

- [x] ResourceNotFoundException.java を作成する
- [x] DuplicateRecordException.java を作成する
- [x] GlobalExceptionHandler に ResourceNotFoundException → 404 ハンドリングを追加する

## フェーズ2: エンティティ・リポジトリ

- [x] ReadingRecord.java エンティティを作成する（ReadingStatus enum 含む）
- [x] ReadingRecordRepository.java を作成する

## フェーズ3: サービス

- [x] UserService に getUserByEmail(String email): User を追加する
- [x] ShelfService.java を作成する（addToShelf / updateStatus / remove / getShelf）

## フェーズ4: コントローラー

- [x] ShelfController.java を作成する（POST /books/add, GET /shelf, POST /shelf/{id}/status, POST /shelf/{id}/delete）

## フェーズ5: テンプレート

- [x] shelf/index.html を作成する（3タブ: 読みたい / 読書中 / 読了）

## フェーズ6: DDL参照ファイル

- [x] src/main/resources/db/schema.sql に reading_records DDL を追加する

## フェーズ7: テスト

- [x] ShelfServiceTest.java を作成する（addToShelf正常系・重複・ステータス遷移・削除・権限チェック）

## 申し送り

### 実装完了日
2026-05-25

### 実装したファイル
- 新規: ResourceNotFoundException, DuplicateRecordException, ReadingRecord, ReadingRecordRepository, ShelfService, ShelfController, shelf/index.html, db/schema.sql, ShelfServiceTest
- 変更: GlobalExceptionHandler（404追加）, UserService（getUserByEmail追加）

### 計画と実績の差分
- `createdAt` フィールドを `ReadingRecord` に追加（バリデータ指摘で計画外追加）
- `ShelfServiceTest` をバリデータ指摘で9ケース → 12ケースに増強

### バリデータで検出した主要修正
1. `ReadingStatus.valueOf(status)` の `IllegalArgumentException` が未処理だった → try/catch でリダイレクトに変更
2. `ReadingRecord` に `createdAt` がなかった → エンティティと schema.sql 両方に追加
3. テスト不足ケース（remove例外系 × 2, addToShelf BookNotFound）を追加

### 既知の技術的負債（将来対応）
- `getUserId()` がリクエストごとに `SELECT users WHERE email = ?` を発行している
  → `CustomUserDetailsService` を `CustomUserDetails`（userId保持）に改修することで解消可能
- `findByUserIdOrderByIdDesc()` で全件取得 → ページネーション未実装（ユーザー単位なのでMVPでは許容）
- `ReadingRecord` エンティティをViewに直接渡している（LazyInitializationException リスク）
  → DTO変換（ReadingRecordDto）が理想だが OpenSessionInView が有効な現状は問題なし

### 次のフィーチャー候補
- ブログ記事作成（PostController / PostService / PostRepository）
- マイページ（StatsService / Chart.js 月別グラフ）
- ユーザーページ / フォロー機能
