# タスクリスト: 全体デザイン修正

## 申し送り

### 実装完了日
2026-05-30

### 計画と実績の差分
- `home/index.html` のログイン済みセクションの `btn-primary` が当初の対象ファイルリストに漏れていたが、グレップで検出して修正した。差分はそれのみ。

### 学んだこと
- `hero-btn-primary` はカスタムCSSクラス（ボタン形状用）であり、Bootstrapの `btn-primary` ではないため変更不要。クラス名のサフィックスだけで判断しないこと。
- グレップによる変更漏れチェックを実装後に行うことで、確実な網羅性を担保できた。

### 次回への改善提案
- Bootstrap のカラーユーティリティを全面的にCSSカスタムプロパティ（`--bs-btn-bg` 等）でオーバーライドすれば、テンプレートを触らずにサイト全体の配色を一括変更できる。スケールするプロジェクトでは検討の価値あり。

- [x] main.css: ナビバーブランド色とフォーカス色を白黒ベースに変更
- [x] layout/base.html: 新規登録ボタンを btn-dark に変更
- [x] auth/login.html: ログインボタンを btn-dark に変更
- [x] auth/register.html: 登録するボタンを btn-dark に変更
- [x] book/search.html: ボタンを btn-dark / btn-outline-dark に変更
- [x] book/confirm.html: 本棚に追加するボタンを btn-dark に変更
- [x] book/title-results.html: この本を選択ボタンを btn-outline-dark に変更
- [x] shelf/index.html: ボタンを btn-dark / btn-outline-dark に変更
- [x] post/new.html: 投稿するボタンを btn-dark に変更
- [x] post/edit.html: 更新するボタンを btn-dark に変更
- [x] post/show.html: ボタンとバッジを白黒に変更
- [x] user/show.html: ボタンを btn-dark / btn-outline-dark に変更
- [x] error/*.html: ホームへ戻るボタンを btn-dark に変更（4ファイル）
