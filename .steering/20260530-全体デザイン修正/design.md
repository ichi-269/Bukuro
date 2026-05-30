# 設計: 全体デザイン修正

## 方針

20260530-デザイン修正（ログアウト時ヒーロー画面）で確立した「白黒ベース・シンプル」の方向性を
全テンプレートに横断適用する。変更はクラス名の置換のみで、レイアウトは一切変更しない。

## 変更マッピング

| 変更前 | 変更後 | 適用箇所 |
|--------|--------|----------|
| `btn-primary` | `btn-dark` | ログイン・登録・投稿・本棚追加・エラーページ等 |
| `btn-outline-primary` | `btn-outline-dark` | ISBN検索・書名選択・本棚操作・本棚を見るリンク等 |
| `badge bg-success` | `badge bg-dark` | post/show.html の「公開」バッジ |
| `.navbar-brand color: #2c3e50` | `color: #111` | main.css |
| `form-control:focus border-color: #4a90e2` | `border-color: #888` | main.css |
| `form-control:focus box-shadow: rgba(74,144,226,0.25)` | `rgba(0,0,0,0.08)` | main.css |

## 維持するもの

- `btn-outline-danger`, `btn-outline-secondary`, `btn-link` — 変更しない
- `alert-success`, `alert-danger`, `alert-warning`, `alert-info` — 変更しない（フィードバック色として機能的に必要）
- `badge bg-secondary` — グレーなので既に中立

## ファイル別変更詳細

### layout/base.html
- 新規登録ボタン: `btn-primary` → `btn-dark`

### auth/login.html
- ログインボタン: `btn-primary` → `btn-dark`

### auth/register.html
- 登録するボタン: `btn-primary` → `btn-dark`

### book/search.html
- 候補を検索するボタン: `btn-primary` → `btn-dark`
- 書誌情報を取得するボタン: `btn-outline-primary` → `btn-outline-dark`

### book/confirm.html
- 本棚に追加するボタン: `btn-primary` → `btn-dark`

### book/title-results.html
- この本を選択ボタン: `btn-outline-primary` → `btn-outline-dark`

### shelf/index.html
- 本を追加するボタン（ヘッダー）: `btn-primary` → `btn-dark`
- 本を追加するボタン（空状態）: `btn-outline-primary` → `btn-outline-dark`
- 記事を書くボタン: `btn-outline-primary` → `btn-outline-dark`

### post/new.html
- 投稿するボタン: `btn-primary` → `btn-dark`

### post/edit.html
- 更新するボタン: `btn-primary` → `btn-dark`

### post/show.html
- グッドするボタン: `btn-outline-primary` → `btn-outline-dark`
- グッド済みボタン: `btn-primary` → `btn-dark`
- 公開バッジ: `badge bg-success` → `badge bg-dark`

### user/show.html
- 本棚を見るボタン: `btn-outline-primary` → `btn-outline-dark`
- フォローするボタン: `btn-primary` → `btn-dark`

### error/400.html, 403.html, 404.html, 500.html
- ホームへ戻るボタン: `btn-primary` → `btn-dark`

### main.css
- `.navbar-brand` color: `#2c3e50` → `#111`
- `form-control:focus` border-color/box-shadow: 青 → ニュートラルグレー
