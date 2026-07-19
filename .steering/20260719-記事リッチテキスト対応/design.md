# 設計書

## アーキテクチャ概要

DBスキーマ・APIは変更しない。`Post.body`は引き続きMarkdown文字列を格納するプレーンなTEXTカラムとして扱う。

- **入力側**: 新規コンポーネント`MarkdownEditor.vue`を作成し、NewView/EditViewの`<textarea>`を置き換える。ツールバー操作でカーソル位置にMarkdown記法を挿入し、編集/プレビューをタブ切り替えする。
- **表示側**: 新規コンポーネント`MarkdownContent.vue`を作成し、ShowView.vueの`<p>{{ post.body }}</p>`を置き換える。Markdown→HTML変換 + サニタイズを内部で行い、結果を`v-html`で描画する。
- **共通ロジック**: `frontend/src/utils/markdown.ts`にMarkdown→安全なHTML文字列へ変換する関数`renderMarkdown(body: string): string`を切り出し、`MarkdownEditor.vue`（プレビュー用）と`MarkdownContent.vue`（詳細表示用）の両方から利用する。

```
[NewView/EditView.vue]
    └─ <MarkdownEditor v-model="form.body" />
           ├─ ツールバー（太字/見出し/箇条書き/リンク）
           ├─ 編集タブ: <textarea>
           └─ プレビュータブ: renderMarkdown(modelValue) を v-html 表示

[ShowView.vue]
    └─ <MarkdownContent :body="post.body" />
           └─ renderMarkdown(body) を v-html 表示

[utils/markdown.ts]
    renderMarkdown(raw: string): string
        1. marked.parse(raw) で Markdown → HTML 文字列に変換
        2. DOMPurify.sanitize(html) でサニタイズして返す
```

## コンポーネント設計

### 1. `utils/markdown.ts`

**責務**:
- Markdown文字列を受け取り、サニタイズ済みのHTML文字列を返す純粋関数を提供する
- `marked`によるパース設定（改行を`<br>`として扱う`breaks: true`など、既存の`white-space: pre-line`表示に近い挙動を踏襲）を一元管理する
- `DOMPurify`によるサニタイズ設定（許可タグ・属性のホワイトリスト）を一元管理する

**実装の要点**:
- 許可タグは装飾目的に限定する: `p, br, strong, em, h2, h3, h4, ul, ol, li, a, blockquote, code, pre`
- `a`タグは`href`属性のみ許可し、`javascript:`スキーム等は`DOMPurify`のデフォルト設定でブロックされることを確認する
- `target`属性は許可せず、外部リンクの`rel="noopener noreferrer"`付与などは本フェーズのスコープ外とする（単純化のため）

### 2. `MarkdownEditor.vue`

**責務**:
- `v-model`で本文文字列を親（NewView/EditView）とやり取りする（既存の`form.body`との結線を変えない）
- ツールバーボタン（太字/見出しH2/見出しH3/箇条書き/リンク）によるMarkdown記法のカーソル位置挿入
- 「編集」「プレビュー」タブの切り替えUI
- 親から渡されるバリデーションエラー表示（`is-invalid`相当）をそのまま透過的に扱えるようpropsで受け取る

**実装の要点**:
- テキスト挿入は`<textarea>`の`selectionStart`/`selectionEnd`を使い、選択範囲を装飾記法で囲む（例: 太字ボタンで選択中テキストを`**`で囲む。選択なしの場合はカーソル位置にプレースホルダ付きで挿入）
- プレビュー描画は`utils/markdown.ts`の`renderMarkdown`を使う
- Bootstrap 5のnav-tabs等、既存デザインパターン（`docs/development-guidelines.md`参照）に合わせる

### 3. `MarkdownContent.vue`

**責務**:
- `body: string`をpropsで受け取り、`renderMarkdown`でサニタイズ済みHTMLに変換して`v-html`で描画する

**実装の要点**:
- `v-html`使用は本コンポーネント内に閉じ込め、他の箇所では引き続き`v-html`を使わない方針を維持する
- 既存のプレーンテキストのみの記事データも、Markdown記法を含まない通常テキストとして問題なく表示されることを確認する（`marked`は非Markdown文字列もそのまま段落として扱うため後方互換）

## データフロー

### 記事作成時にMarkdown本文を入力してプレビュー確認する

```
1. ユーザーがNewView.vueの本文欄（MarkdownEditor）で "**重要**" を入力
2. 「プレビュー」タブをクリック
3. MarkdownEditorが内部でrenderMarkdown(modelValue)を呼び、<strong>重要</strong>を含むHTMLをv-htmlで表示
4. ユーザーが「投稿する」をクリック → 既存通りform.body（Markdown文字列そのもの）をpostsApi.createPostに送信
5. バックエンドは文字列としてそのままDBに保存（変更なし）
```

### 記事詳細画面での表示

```
1. ShowView.vueがpostsApi.getPost()で記事を取得（post.bodyはMarkdown文字列）
2. <MarkdownContent :body="post.body" /> がrenderMarkdownでHTML変換・サニタイズ
3. v-htmlでレンダリングされた装飾済み本文が表示される
```

## エラーハンドリング戦略

新規のエラークラスは不要。既存のフィールドバリデーションエラー（`fieldErrors`）の表示ロジックはNewView/EditView側に残し、`MarkdownEditor`はpropsで受け取ったエラーメッセージを表示するのみとする。

Markdownパース自体は例外を投げない前提（`marked`はパースエラーで例外を投げず、パースできない部分はプレーンテキスト扱いになる）。

## テスト戦略

### ユニットテスト

- `utils/markdown.ts`の`renderMarkdown`関数
  - 太字・見出し・箇条書き・リンクが期待通りのHTMLタグに変換されること
  - `<script>alert(1)</script>`や`<img src=x onerror=alert(1)>`等の入力がサニタイズされ、スクリプトが実行されない形（タグ除去 or エスケープ）になること
  - プレーンテキストのみの入力がそのまま段落として表示されること（後方互換）

### 統合テスト（コンポーネントテスト）

- `MarkdownEditor.vue`: ツールバーボタン押下でtextareaの値に期待した記法が挿入されること、編集/プレビュータブ切り替えが機能すること
- `MarkdownContent.vue`: propsの`body`に応じて期待したHTML構造がレンダリングされること

## 依存ライブラリ

Markdown→HTML変換とサニタイズのため、以下を新規追加する。

```json
{
  "dependencies": {
    "marked": "^16.x",
    "dompurify": "^3.x"
  },
  "devDependencies": {
    "@types/dompurify": "^3.x"
  }
}
```

`docs/architecture.md`のフロントエンド依存パッケージ表に上記2ライブラリを追記する（実装完了後にドキュメント更新タスクとして実施）。

## ディレクトリ構造

```
frontend/src/
├── components/
│   ├── MarkdownEditor.vue   (新規)
│   └── MarkdownContent.vue  (新規)
├── utils/
│   └── markdown.ts          (新規)
└── views/post/
    ├── NewView.vue    (変更: textarea → MarkdownEditor)
    ├── EditView.vue   (変更: textarea → MarkdownEditor)
    └── ShowView.vue   (変更: <p> → MarkdownContent)
```

`frontend/src/components/`が未作成の場合は新規作成する。

## 実装の順序

1. `marked` / `dompurify`をfrontendに追加
2. `utils/markdown.ts`の`renderMarkdown`実装 + ユニットテスト
3. `MarkdownContent.vue`実装 + テスト、ShowView.vueへ組み込み
4. `MarkdownEditor.vue`実装（ツールバー・タブ切り替え） + テスト
5. NewView.vue / EditView.vueへ`MarkdownEditor`を組み込み、既存の`form.body`結線・バリデーション表示を維持
6. 手動確認（作成・編集・詳細表示・既存記事の後方互換表示）
7. `docs/architecture.md`の依存パッケージ表を更新

## セキュリティ考慮事項

- `docs/architecture.md`の既存方針は「XSS対策: Vueテンプレート補間のデフォルトHTMLエスケープ」「`v-html`はユーザー入力に使わない」（`docs/development-guidelines.md`）だが、本機能はMarkdownレンダリングのため`v-html`が必須となる。**`DOMPurify`による厳格なサニタイズを`MarkdownContent.vue`・プレビュー双方で必ず経由させることで安全性を担保する**。この方針変更を`docs/architecture.md`に明記する。
- `v-html`の使用箇所は`MarkdownEditor.vue`（プレビュー）と`MarkdownContent.vue`の2箇所に限定し、他のコンポーネントでは使用しない。
- サニタイズ後のHTMLであっても、ユニットテストで代表的なXSSペイロード（`<script>`, `onerror`, `javascript:`リンク等）が無害化されることを検証する。

## パフォーマンス考慮事項

- 記事本文の想定文字数は小〜中規模（ブログ記事程度）であり、`marked`のパースはクライアントサイドで都度実行しても問題ない負荷と想定する
- プレビュータブ切り替え時のみレンダリングし、編集中（textareaへの入力都度）はレンダリングしない設計とし、タイピング時の無駄な再計算を避ける

## 将来の拡張性

- 画像アップロード・コードブロックのシンタックスハイライトは、`renderMarkdown`の変換ロジックとサニタイズのホワイトリストを拡張する形で対応可能
- WYSIWYGエディタへの移行が将来必要になった場合も、保存形式がMarkdown文字列のままであるため、`MarkdownEditor.vue`の内部実装のみ差し替えれば良い
