# 設計: ユーザーページ

## 新規作成ファイル

```
src/main/java/com/bukuro/controller/
└── UserController.java

src/main/resources/templates/user/
└── show.html

src/test/java/com/bukuro/controller/
└── UserControllerTest.java
```

## 変更ファイル

- `UserService.java` — `getUserByUsername(String username): User` を追加
- `PostRepository.java` — `findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId)` を追加

## UserController 設計

```java
GET /users/{username}
  → UserService.getUserByUsername(username) or ResourceNotFoundException → 404
  → PostRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(user.id)
  → model: profileUser, posts, postCount
  → user/show.html
```

Note: @AuthenticationPrincipal は null 許容（permitAll）。
モデルに isOwnPage = (currentUserId != null && currentUserId.equals(profileUser.id)) を追加。

## テンプレート設計（user/show.html）

- ページ上部: プロフィールカード（username・bio・登録年月・公開記事数）
- ページ下部: 公開記事カード一覧（書影・タイトル・書籍名・グッド数・日付）
- 記事なし時: 「まだ公開記事はありません」メッセージ
- 記事カード: post.title → /posts/{id} へのリンク

## テスト設計（UserControllerTest）

@WebMvcTest + @Import(SecurityConfig.class) + @MockBean

| テストケース | 検証内容 |
|---|---|
| 存在するユーザー名でアクセス | 200、username が本文に含まれる |
| 存在しないユーザー名でアクセス | 404 |
| 未認証でアクセス | 200（permitAll のため） |
