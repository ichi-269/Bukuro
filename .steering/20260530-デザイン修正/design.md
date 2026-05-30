# 設計: デザイン修正（ログアウト時の画面）

## 構造変更

現在の `<main class="container mt-5">` はログアウト時とログイン時を同じコンテナで扱っているため分離する。

```
Before:
<main class="container mt-5">
  <div> ← 共通ヒーロー (見出し + 状態別ボタン)
  <div sec:authorize="isAnonymous()"> ← 3カラムカード
  <div sec:authorize="isAuthenticated()"> ← フィード

After:
<main>
  <section sec:authorize="isAnonymous()" class="hero-section">  ← 新ヒーロー
  <div sec:authorize="isAuthenticated()" class="container mt-5"> ← フィード（変更なし）
```

## ヒーローデザイン

- ビューポート高さいっぱい（ナビバー除く）のセクション、縦方向中央揃え
- アイキャッチ: 小文字 "READING JOURNAL" スタイルのラベル
- 見出し: `読んだ本を、<br>丁寧に記録する。` ― 大きく、タイトなレタースペーシング
- 説明文: 1〜2行、薄い色、小さめ
- CTA:
  - プライマリ: `btn btn-dark` + 角丸ピル形状
  - セカンダリ: テキストリンク（ボタンではない）

## CSS 追加（main.css）

```css
.hero-section {
    min-height: calc(100vh - 56px);
    display: flex;
    align-items: center;
    padding: 4rem 0;
}
.hero-eyebrow { font-size: 0.72rem; letter-spacing: 0.25em; text-transform: uppercase; color: #aaa; margin-bottom: 2rem; }
.hero-title { font-size: clamp(2.4rem, 5vw, 3.5rem); font-weight: 700; letter-spacing: -0.03em; line-height: 1.2; color: #111; margin-bottom: 1.5rem; }
.hero-body { color: #777; font-size: 1rem; line-height: 1.8; margin-bottom: 2.5rem; }
.hero-cta { display: flex; align-items: center; justify-content: center; gap: 1.75rem; flex-wrap: wrap; }
.hero-btn-primary { padding: 0.65rem 2.5rem; border-radius: 2rem; font-size: 0.95rem; font-weight: 500; }
.hero-link-secondary { color: #888; text-decoration: none; font-size: 0.9rem; transition: color 0.15s; }
.hero-link-secondary:hover { color: #333; }
```
