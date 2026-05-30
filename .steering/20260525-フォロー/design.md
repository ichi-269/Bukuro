# 設計: フォロー機能

## 新規ファイル

### `Follow.java` (entity)
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followee_id"}))`
- フィールド: id, follower (ManyToOne LAZY), followee (ManyToOne LAZY), createdAt
- `@PrePersist` で createdAt を設定

### `FollowRepository.java`
```java
boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
long countByFolloweeId(Long followeeId);   // フォロワー数
long countByFollowerId(Long followerId);   // フォロー中数
@Query("SELECT f.followee.id FROM Follow f WHERE f.follower.id = :followerId")
List<Long> findFolloweeIdsByFollowerId(@Param("followerId") Long followerId);
```

### `FollowService.java`
- `follow(Long followerId, Long followeeId)`: 自己フォロー禁止チェック → 冪等insert
- `unfollow(Long followerId, Long followeeId)`: 冪等delete
- `isFollowing(Long followerId, Long followeeId)`: boolean
- `getFollowerCount(Long userId)`: long
- `getFollowingCount(Long userId)`: long

### `FollowController.java`
- `POST /users/{username}/follow` → `followService.follow(currentUserId, targetUserId)` → redirect to `/users/{username}`
- `POST /users/{username}/unfollow` → `followService.unfollow(currentUserId, targetUserId)` → redirect to `/users/{username}`
- 両エンドポイントとも認証必須（SecurityConfig で設定）

## 変更ファイル

### `SecurityConfig.java`
- `/users/*/follow` と `/users/*/unfollow` を認証必須に追加（`.anyRequest().authenticated()` で既にカバーされているが、明示するため POST のみ確認）
- 実際には `POST /users/{username}/follow` は `.anyRequest().authenticated()` でカバー済み → SecurityConfig の変更不要

### `UserController.java`
- `FollowService` を inject
- モデルに `followerCount`, `followingCount`, `isFollowing`（認証済みかつ他人のページの場合）を追加

### `user/show.html`
- フォロワー数・フォロー中数をプロフィールカードに表示
- 認証済みかつ自分のページでない場合にフォロー/アンフォローボタン（POSTフォーム）を表示

### `PostRepository.java`
- `List<Post> findByUserIdInAndIsPublicTrueOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable)`
- `List<Post> findTop20ByIsPublicTrueOrderByGoodCountDescCreatedAtDesc()` (フォロー0人時)

### `PostService.java`
- `findFeedForUser(Long userId)`: フォロイーの記事 or 人気公開記事を20件返す

### `HomeController.java`
- 認証状態に応じてフィード or 静的画面を返す

### `home/index.html`
- ログイン済み時にフィード記事カード一覧を表示するセクションを追加

## エラーハンドリング
- 自己フォロー: `IllegalArgumentException` → GlobalExceptionHandler の 500 catch → ログに記録（ボタンで発生することは通常ない）
- 対象ユーザー不在: UserService.getUserByUsername が `ResourceNotFoundException` → 404

## スキーマ追加 (schema.sql)
```sql
CREATE TABLE IF NOT EXISTS follows (
    id           BIGINT    NOT NULL AUTO_INCREMENT,
    follower_id  BIGINT    NOT NULL,
    followee_id  BIGINT    NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_follow (follower_id, followee_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users(id),
    CONSTRAINT fk_follow_followee FOREIGN KEY (followee_id) REFERENCES users(id),
    INDEX idx_follow_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
