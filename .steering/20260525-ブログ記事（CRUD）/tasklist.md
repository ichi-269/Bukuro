# タスクリスト: ブログ記事（CRUD）

## フェーズ1: エンティティ・リポジトリ

- [x] Post.java エンティティを作成する
- [x] PostRepository.java を作成する

## フェーズ2: DTO

- [x] PostForm.java を作成する（@NotBlank, @Size バリデーション付き）

## フェーズ3: サービス

- [x] PostService.java を作成する（create / update / delete / findById / findPublicOrOwn / findByUserId）

## フェーズ4: コントローラー

- [x] PostController.java を作成する（GET /posts/new, POST /posts, GET /posts/{id}, GET /posts/{id}/edit, POST /posts/{id}/edit, POST /posts/{id}/delete）

## フェーズ5: テンプレート

- [x] post/new.html を作成する（書籍情報＋記事作成フォーム）
- [x] post/show.html を作成する（記事詳細：本文・著者・書籍情報・編集/削除ボタン）
- [x] post/edit.html を作成する（記事編集フォーム）

## フェーズ6: 既存ファイル更新

- [x] shelf/index.html に「記事を書く」リンクを追加する
- [x] db/schema.sql に posts DDL を追加する

## フェーズ7: テスト

- [x] PostServiceTest.java を作成する（create・update・delete・findPublicOrOwn の正常系・異常系）

## 申し送り

### 実装完了日
2026-05-25

### 実装したファイル
- 新規: Post, PostRepository, PostForm, PostService, PostController, post/new.html, post/show.html, post/edit.html, PostServiceTest
- 変更: shelf/index.html（「記事を書く」リンク追加）, db/schema.sql（posts DDL 追加）

### バリデータで検出した主要修正
1. `PostController.update` でバリデーションエラー早期返却時に認可チェックがスキップされていた → `getUserId()` をバリデーションチェック前に移動し、エラー時も所有者チェックを実施
2. `PostController.editForm` で `AccessDeniedException` を FQCN で記述していた（インポート漏れを FQCN 回避） → import 追加して解消

### 既知の技術的負債（将来対応）
- `PostController.bookRepository` 直接注入 → PostService に移動するとアーキテクチャが整合する
- `getUserId()` 毎回 DB SELECT → ShelfController と同じ問題（CustomUserDetails 化で解消可能）
- `post.show.html` の `updatedAt != createdAt` 比較 → `LocalDateTime.equals()` で値比較のため動作するが `isEdited()` convenience メソッドが意図を明確にする
- 「記事を書く」リンクが「読みたい」ステータスでも表示される → 仕様上の制限を設けるか否か要判断

### 次のフィーチャー候補
- マイページ（自分の記事一覧・StatsService / Chart.js 月別グラフ）
- ユーザーページ（他ユーザーの公開記事一覧・フォロー機能）
- グッド機能（GoodService）
