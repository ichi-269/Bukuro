# 設計: ホーム画面の機能拡充

## フロー

```
GET / (パラメータなし)
  → フォロイーあり: feedType=following, フォロー中フィード表示
  → フォロイーなし: feedType=recommended, おすすめフィード表示

GET /?feed=recommended
  → フォロイーあり: feedType=recommended, おすすめフィード表示
  → フォロイーなし: feedType=recommended (変化なし)

GET /?feed=following
  → フォロイーあり: feedType=following (デフォルトと同じ)
  → フォロイーなし: feedType=recommended (フォロイーなしは常にrecommended)
```

## 変更ファイル

### `PostService.java`

既存の `findFeedForUser` は内部で following/recommended を自動判定していたが、
HomeController が自分でフィードタイプを決定できるよう、2メソッドを追加分離する。

```java
// 追加: フォロイーの記事（降順）
public List<Post> findFollowingFeed(Long userId)

// 追加: おすすめ（グッド数降順）
public List<Post> findRecommendedFeed()
```

`findFeedForUser(Long userId)` は削除し、上記2メソッドを利用するよう HomeController を変更する。

### `HomeController.java`

```java
@GetMapping("/")
public String index(
    @AuthenticationPrincipal UserDetails principal,
    @RequestParam(required = false) String feed,
    Model model
)
```

- `hasFollowees`（boolean）をモデルに追加
- feedType 決定ロジック:
  - `!hasFollowees` → always "recommended"
  - `hasFollowees && "recommended".equals(feed)` → "recommended"
  - それ以外（hasFollowees） → "following"
- feedType に応じて `findFollowingFeed` または `findRecommendedFeed` を呼ぶ

### `home/index.html`

- `hasFollowees` が true のときのみタブUIを表示
  ```html
  <ul class="nav nav-tabs mb-3">
    <li><a href="/?feed=following" class="nav-link [active if following]">フォロー中</a></li>
    <li><a href="/?feed=recommended" class="nav-link [active if recommended]">おすすめ</a></li>
  </ul>
  ```
- フィード見出し（`h4`）は削除し、タブ見出しで代替
- `hasFollowees` が false の場合は見出しのみ（既存と同じ）

## テスト変更

`HomeControllerTest`:
- `findFeedForUser` → `findFollowingFeed` / `findRecommendedFeed` にモック差し替え
- 新規: `?feed=recommended` でフォロイーありでもおすすめが返るケース
- 新規: `?feed=following` でフォロイーありでフォロー中フィードが返るケース
- 新規: `hasFollowees` モデル属性の検証
