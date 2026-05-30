# 設計: フォロワー・フォロー一覧

## 変更ファイル一覧

| ファイル | 変更種別 | 内容 |
|---------|---------|------|
| `FollowRepository.java` | 追加 | User を返す JPQL クエリ2件 |
| `FollowService.java` | 追加 | getFollowers / getFollowees メソッド |
| `UserController.java` | 追加 | GET /users/{username}/followers・/following エンドポイント |
| `user/followers.html` | 新規 | フォロワー一覧テンプレート |
| `user/following.html` | 新規 | フォロー中一覧テンプレート |
| `user/show.html` | 変更 | フォロワー数・フォロー中数をリンク化 |
| `FollowServiceTest.java` | 追加 | getFollowers / getFollowees テスト |
| `UserControllerTest.java` | 追加 | /followers・/following エンドポイントテスト |

## FollowRepository

```java
@Query("SELECT f.follower FROM Follow f WHERE f.followee.id = :userId")
List<User> findFollowersByFolloweeId(@Param("userId") Long userId);

@Query("SELECT f.followee FROM Follow f WHERE f.follower.id = :userId")
List<User> findFolloweesByFollowerId(@Param("userId") Long userId);
```

## FollowService

```java
@Transactional(readOnly = true)
public List<User> getFollowers(Long userId) {
    return followRepository.findFollowersByFolloweeId(userId);
}

@Transactional(readOnly = true)
public List<User> getFollowees(Long userId) {
    return followRepository.findFolloweesByFollowerId(userId);
}
```

## UserController

```java
@GetMapping("/users/{username}/followers")
public String followers(@PathVariable String username, Model model) {
    User profileUser = userService.getUserByUsername(username);
    model.addAttribute("profileUser", profileUser);
    model.addAttribute("users", followService.getFollowers(profileUser.getId()));
    model.addAttribute("listType", "followers");
    return "user/followers";
}

@GetMapping("/users/{username}/following")
public String following(@PathVariable String username, Model model) {
    User profileUser = userService.getUserByUsername(username);
    model.addAttribute("profileUser", profileUser);
    model.addAttribute("users", followService.getFollowees(profileUser.getId()));
    model.addAttribute("listType", "following");
    return "user/following";
}
```

## テンプレート構成

- `user/followers.html` — 「{username} のフォロワー」見出し＋ユーザーリスト
- `user/following.html` — 「{username} がフォロー中」見出し＋ユーザーリスト
- 各行: ユーザー名 → `/users/{username}` リンク
- 空状態: 「まだフォロワーがいません。」/ 「まだフォロー中のユーザーがいません。」

## user/show.html の変更

```html
<!-- Before -->
<span class="text-muted small">
    フォロワー <strong th:text="${followerCount}"></strong>
</span>
<span class="text-muted small">
    フォロー中 <strong th:text="${followingCount}"></strong>
</span>

<!-- After -->
<a th:href="@{'/users/' + ${profileUser.username} + '/followers'}"
   class="text-decoration-none text-muted small">
    フォロワー <strong th:text="${followerCount}"></strong>
</a>
<a th:href="@{'/users/' + ${profileUser.username} + '/following'}"
   class="text-decoration-none text-muted small">
    フォロー中 <strong th:text="${followingCount}"></strong>
</a>
```
