# 設計: 本棚タイトルリンク（記事確認）

## 変更箇所

`src/main/resources/templates/shelf/index.html` — `bookCard` フラグメントのタイトル部分のみ

## 変更前後

```html
<!-- Before -->
<h6 class="fw-bold mb-1" th:text="${record.book.title}"></h6>

<!-- After -->
<h6 class="fw-bold mb-1">
    <a th:if="${postIdByBookId.containsKey(record.book.id)}"
       th:href="@{'/posts/' + ${postIdByBookId.get(record.book.id)}}"
       class="text-decoration-none text-dark"
       th:text="${record.book.title}"></a>
    <span th:unless="${postIdByBookId.containsKey(record.book.id)}"
          th:text="${record.book.title}"></span>
</h6>
```

## 根拠

- `postIdByBookId` は `ShelfController` がモデルに追加済み（既存）
- 記事なし → `<span>` でプレーンテキスト維持
- 記事あり → `<a>` でリンク化。`text-decoration-none text-dark` で見た目はほぼ変わらず、ホバー時に下線が出るなど微細な視覚的フィードバックあり
