# 要求仕様: 全体デザイン修正

## 背景

ログアウト時のホーム画面（20260530-デザイン修正）をシンプルな白黒ベースにリデザインした。
その方針を全ての画面に統一的に適用する。

## 機能要件

1. 全テンプレートのボタン配色を白黒ベースに統一する
   - `btn-primary`（Bootstrap青）→ `btn-dark`（黒）
   - `btn-outline-primary`（青アウトライン）→ `btn-outline-dark`（黒アウトライン）
   - 削除などの破壊的操作の `btn-outline-danger` は現状維持（UX上の警告色として必要）
   - アラートメッセージ（`alert-success` / `alert-danger`）は現状維持（状態フィードバック）
2. `post/show.html` の公開バッジを白黒に変更
   - `badge bg-success`（緑）→ `badge bg-dark`（黒）
3. `main.css` のフォーカス色を白黒ベースに変更
   - `#4a90e2`（青）→ グレー系のニュートラルカラー
4. ナビバーのブランド色を更新する
   - `#2c3e50`（ダークブルー）→ `#111`（ほぼ黒）

## 対象ファイル

- `src/main/resources/templates/layout/base.html`
- `src/main/resources/templates/auth/login.html`
- `src/main/resources/templates/auth/register.html`
- `src/main/resources/templates/book/search.html`
- `src/main/resources/templates/book/confirm.html`
- `src/main/resources/templates/book/title-results.html`
- `src/main/resources/templates/shelf/index.html`
- `src/main/resources/templates/post/new.html`
- `src/main/resources/templates/post/edit.html`
- `src/main/resources/templates/post/show.html`
- `src/main/resources/templates/user/show.html`
- `src/main/resources/templates/error/400.html`
- `src/main/resources/templates/error/403.html`
- `src/main/resources/templates/error/404.html`
- `src/main/resources/templates/error/500.html`
- `src/main/resources/static/css/main.css`

## 非機能要件

- 機能・レイアウトの変更は行わない（色のみ変更）
- JSは変更しない
