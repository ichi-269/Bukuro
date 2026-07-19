# 開発ガイドライン (Development Guidelines)

## コーディング規約

### 命名規則（Java / Spring Boot）

#### クラス・インターフェース

```java
// ✅ 良い例: 役割が明確なPascalCase
class PostService { }
class ReadingRecordRepository { }
interface BookSearchService { }

// ❌ 悪い例: 曖昧・略語
class PstSvc { }
class Mgr { }
```

| 種別 | 規則 | 例 |
|------|------|-----|
| Controllerクラス | PascalCase + `Controller` | `PostController` |
| Serviceクラス | PascalCase + `Service` | `ShelfService` |
| Repositoryインターフェース | PascalCase + `Repository` | `PostRepository` |
| エンティティクラス | PascalCase（テーブル名から） | `ReadingRecord` |
| DTOクラス | PascalCase + `Dto` または `Form` | `PostForm`, `PostDto` |
| 例外クラス | PascalCase + `Exception` | `BookNotFoundException` |
| 設定クラス | PascalCase + `Config` | `SecurityConfig` |
| 定数 | UPPER_SNAKE_CASE | `OPENBD_API_TIMEOUT` |

#### メソッド・変数

```java
// ✅ 良い例: camelCase、動詞始まりのメソッド
public Post createPost(Long userId, Long bookId, String title, String body) { }
public List<ReadingRecord> findByStatus(ReadingStatus status) { }
boolean isPublic = post.isPublic();

// ❌ 悪い例: 意味不明な変数名
String s = post.getTitle();
int n = records.size();
```

**Boolean変数・メソッドの接頭辞**:
- `is` / `has` / `can` / `should` で始める
- 例: `isPublic`, `hasGoodedBy(userId)`, `canEdit(userId)`

---

### コードフォーマット

- **インデント**: 4スペース（Spring Boot 標準）
- **行の長さ**: 最大120文字
- **中括弧**: K&Rスタイル（開き括弧は同じ行）
- **フォーマッタ**: IntelliJ IDEA の「Google Java Format」または標準フォーマット

```java
// ✅ 良い例
public Post createPost(PostForm form, Long userId) {
    validateOwnership(form.getBookId(), userId);
    Post post = Post.builder()
            .userId(userId)
            .title(form.getTitle())
            .body(form.getBody())
            .isPublic(form.isPublic())
            .build();
    return postRepository.save(post);
}

// ❌ 悪い例: 意味のない分割・インデント不統一
public Post createPost(PostForm form,Long userId)
{
  validateOwnership(form.getBookId(),userId);
  Post post=Post.builder().userId(userId).title(form.getTitle()).build();
  return postRepository.save(post);
}
```

---

### コメント規約

**コメントは「なぜ」を書く。「何をしているか」はコードを読めばわかる。**

```java
// ✅ 良い例: 理由・制約を説明
// OpenBD利用規約によりユーザーによる書誌情報の編集は禁止
// https://openbd.jp/terms/
@Column(name = "title", updatable = false)
private String title;

// good_countはGoodsテーブルとの非正規化。
// グッド操作ごとに同期する（集計クエリによるN+1を避けるため）
private int goodCount;

// ❌ 悪い例: コードをそのまま言葉にしただけ
// goodCountを1増やす
goodCount++;
```

**Javadocは公開API・複雑なビジネスロジックのみ**:

```java
/**
 * ISBN-13またはISBN-10でOpenBD APIを検索する。
 *
 * <p>ISBNはISBN-13に正規化してAPIリクエストを送る。
 * APIタイムアウトは{@code openbd.api.timeout-seconds}プロパティで設定。
 *
 * @param isbn ISBN-13またはISBN-10（ハイフン有無を問わない）
 * @return 書誌情報。OpenBDに存在しない場合は空のOptional
 * @throws ExternalApiException APIタイムアウトまたはHTTPエラーの場合
 */
public Optional<BookDto> searchByIsbn(String isbn) { ... }
```

---

### エラーハンドリング

**カスタム例外クラスで意図を明確にする**:

```java
// ✅ 良い例: 例外クラスで状況を表現
public Post findPublicPost(Long postId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    if (!post.isPublic()) {
        throw new AccessDeniedException("非公開記事へのアクセス: " + postId);
    }
    return post;
}

// ❌ 悪い例: 汎用Exceptionで情報が失われる
public Post findPublicPost(Long postId) {
    return postRepository.findById(postId).orElse(null); // nullチェック漏れのリスク
}
```

**Spring `@RestControllerAdvice` で一元ハンドリング（構造化JSONエラーを返却）**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder().status(404).code("RESOURCE_NOT_FOUND").message(ex.getMessage()).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder().status(403).code("ACCESS_DENIED").message(ex.getMessage()).build());
    }
}
```

---

### セキュリティ規約

**他ユーザーのリソース操作を必ずServiceレイヤーでチェック**:

```java
// ✅ 良い例: Serviceで所有権チェック
public void deletePost(Long postId, Long currentUserId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    if (!post.getUserId().equals(currentUserId)) {
        throw new AccessDeniedException("他ユーザーの記事は削除できません");
    }
    postRepository.delete(post);
}
```

**機密情報をコードにハードコードしない**:

```java
// ✅ 良い例: @Valueで環境変数から注入
@Value("${spring.datasource.password}")
private String dbPassword;  // application.properties → 環境変数で解決

// ❌ 悪い例: ハードコード
private String dbPassword = "secret123";  // 絶対にしない
```

**Vueテンプレートのデフォルトエスケープを必ず使う**:

```html
<!-- ✅ 良い例: テンプレート補間 {{ }} は自動エスケープ（XSS対策） -->
<p>{{ post.body }}</p>

<!-- ❌ 悪い例: v-html はエスケープなし（XSS脆弱性） -->
<p v-html="post.body"></p>  <!-- ユーザー入力には使わない -->
```

---

### Spring固有の規約

**Controllerは薄く保つ（ビジネスロジックをServiceへ）**:

```java
// ✅ 良い例: Controllerはルーティング + DTO変換のみ
@PostMapping("/posts")
public ResponseEntity<PostDto> createPost(@RequestParam Long bookId,
                                          @Valid @RequestBody PostForm form,
                                          @AuthenticationPrincipal UserDetails principal) {
    Long userId = userService.getUserByEmail(principal.getUsername()).getId();
    Post post = postService.create(userId, bookId, form);
    return ResponseEntity.status(HttpStatus.CREATED).body(PostDto.from(post));
}

// ❌ 悪い例: Controllerにビジネスロジック
@PostMapping("/posts")
public ResponseEntity<PostDto> createPost(@RequestBody PostForm form, @AuthenticationPrincipal UserDetails user) {
    // 重複チェック、ドメインロジックをControllerに書かない
    if (postRepository.existsByUserIdAndBookId(...)) { ... }
}
```

**JPAエンティティをレスポンスに直接返さない（DTOに変換する）**:

```java
// ✅ 良い例: DTOに変換してから返す
public PostDto findPostById(Long postId, Long currentUserId) {
    Post post = postRepository.findById(postId)...;
    boolean hasGooded = goodRepository.existsByUserIdAndPostId(currentUserId, postId);
    return PostDto.from(post, hasGooded);
}

// ❌ 悪い例: エンティティをそのままレスポンスに返す（遅延ロードの問題・過剰情報の露出）
@GetMapping("/posts/{id}")
public Post show(@PathVariable Long id) {
    return postRepository.findById(id).orElseThrow(); // password等のフィールドが意図せずJSON化されるリスク
}
```

---

## フロントエンド（Vue / TypeScript）の規約

命名規則（ファイル・コンポーネント名等）は`docs/repository-structure.md`を参照。ここではコンポーネント設計・状態管理・API通信の方針のみ記載する。

### コンポーネント設計

- 1ルート = 1ビューコンポーネント（`views/[機能名]/[画面名]View.vue`）。複数ページで使い回すUI要素のみ`components/`に切り出す
- ビジネスロジック（バリデーション・所有権に基づく表示分岐等）はコンポーネント内かAPIレスポンスのフラグ（`isOwner`等）に委譲し、独自の権限判定ロジックをフロントエンド側で新たに作らない（バックエンドのServiceレイヤーが真実の情報源）
- `<script setup lang="ts">` + Composition APIを標準とする

```vue
<!-- ✅ 良い例: 型付きpropsとAPIレスポンスの型をそのまま利用 -->
<script setup lang="ts">
import type { Post } from '../../types'
defineProps<{ post: Post }>()
</script>

<!-- ❌ 悪い例: any型でAPIレスポンスを受け取る -->
<script setup lang="ts">
defineProps<{ post: any }>()
</script>
```

### 状態管理（Pinia）

- アプリ全体で共有が必要な状態（現在ログイン中のユーザー等）のみPiniaストアに置く。ページ固有の状態（フォーム入力値等）はコンポーネントローカルの`ref`/`reactive`で持つ
- ストアからAPIを直接呼び出す場合は`api/`配下のモジュールを経由する（コンポーネントから`axios`を直接importしない）

### API通信

- エンドポイントごとに`api/[リソース名].ts`を作成し、コンポーネントは`axios`ではなくこのモジュール経由で呼び出す
- レスポンス/エラーの型は`types/index.ts`のバックエンドDTO対応インターフェースを使用し、`any`は使わない

```typescript
// ✅ 良い例: api/posts.ts に集約
export function getPost(postId: number) {
  return client.get<Post>(`/posts/${postId}`).then((res) => res.data)
}

// ❌ 悪い例: コンポーネント内で直接axiosを呼ぶ
import axios from 'axios'
const res = await axios.get(`/api/posts/${postId}`)
```

---

## Git運用ルール

### ブランチ戦略（Git Flow）

```
main（本番環境）
└── develop（開発・統合）
    ├── feature/[機能名]  ← 新機能開発
    ├── fix/[修正内容]    ← バグ修正
    └── docs/[対象]       ← ドキュメント修正
```

**運用ルール**:
- `main`: 本番デプロイ可能な安定版のみ
- `develop`: 次期リリースに向けた開発コードを統合
- `feature/*` / `fix/*`: `develop` から分岐し、PRで `develop` へマージ
- `main` への直接コミット禁止。すべてPRレビューを経由する
- `feature → develop`: Squash merge 推奨（コミット履歴をクリーンに保つ）

---

### コミットメッセージ規約（Conventional Commits）

**フォーマット**:
```
<type>(<scope>): <subject>

<body>（任意）

<footer>（任意）
```

**Type一覧**:

| Type | 用途 |
|------|------|
| `feat` | 新機能追加 |
| `fix` | バグ修正 |
| `docs` | ドキュメント変更 |
| `refactor` | リファクタリング（機能変更なし） |
| `test` | テスト追加・修正 |
| `chore` | ビルド設定・依存関係更新 |
| `style` | フォーマット修正（コードの動作に影響なし） |

**良い例**:
```
feat(post): 記事の公開/非公開切り替え機能を追加

PostServiceにupdateVisibilityメソッドを追加。
記事の所有者のみが切り替え可能。

Closes #42
```

**悪い例**:
```
update  （何を更新したかわからない）
fix bug  （どんなバグか不明）
作業中   （コミットは完成したものにする）
```

---

### PRテンプレート

```markdown
## 変更の種類
- [ ] 新機能 (feat)
- [ ] バグ修正 (fix)
- [ ] リファクタリング (refactor)
- [ ] ドキュメント (docs)
- [ ] その他 (chore)

## 変更内容
### 何を変更したか
[簡潔な説明]

### なぜ変更したか
[背景・理由]

### どのように変更したか
- [変更点1]
- [変更点2]

## テスト
- [ ] ユニットテスト追加・更新
- [ ] 統合テスト追加・更新
- [ ] 手動テスト実施

## 動作確認
[どのように動作確認したか]

## 関連Issue
Closes #[番号]

## レビューポイント
[レビュアーに特に見てほしい点]
```

**PR作成前のチェックリスト**:
- [ ] `mvn test` がすべてパスする
- [ ] 他ユーザーのリソースへのアクセス制御を確認した
- [ ] 機密情報がコードにハードコードされていない
- [ ] 競合が解決されている

---

## テスト戦略

### テストピラミッド

```
       /\
      /E2E\        少（手動テスト）
     /------\
    / 統合   \     中（TestContainers + Repository）
   /----------\
  / ユニット   \   多（JUnit 5 + Mockito）
 /--------------\
```

**目標比率**:
- ユニットテスト（Serviceレイヤー）: 70%
- 統合テスト（Repositoryレイヤー）: 20%
- E2Eテスト: 10%（手動）

### ユニットテストの書き方

**Given-When-Then パターン + JUnit 5**:

```java
@ExtendWith(MockitoExtension.class)
class ShelfServiceTest {

    @Mock
    private ReadingRecordRepository readingRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ShelfService shelfService;

    @Test
    @DisplayName("同じ本を2回本棚に追加しようとするとDuplicateRecordExceptionが発生する")
    void addToShelf_duplicateBook_throwsDuplicateRecordException() {
        // Given
        Long userId = 1L;
        Long bookId = 10L;
        when(readingRecordRepository.existsByUserIdAndBookId(userId, bookId))
                .thenReturn(true);

        // When / Then
        assertThrows(DuplicateRecordException.class,
                () -> shelfService.addToShelf(userId, bookId));
    }
}
```

**テストメソッド名の命名規則**:

`[メソッド名]_[条件]_[期待結果]`

```java
// ✅ 良い例
@Test void createPost_validData_returnsCreatedPost() { }
@Test void deletePost_notOwner_throwsAccessDeniedException() { }
@Test void addGood_duplicateGood_throwsDuplicateRecordException() { }

// ❌ 悪い例
@Test void test1() { }
@Test void testCreatePost() { }
@Test void shouldWork() { }
```

### 統合テスト（TestContainers）

```java
@SpringBootTest
@Testcontainers
class PostRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("is_public=trueの記事のみが公開フィードに表示される")
    void findPublicPosts_returnsOnlyPublicPosts() {
        // Given: 公開1件・非公開1件を保存
        Post publicPost = Post.builder()
                .userId(1L).bookId(1L)
                .title("公開記事").body("本文")
                .isPublic(true).goodCount(0).build();
        Post privatePost = Post.builder()
                .userId(1L).bookId(1L)
                .title("非公開記事").body("本文")
                .isPublic(false).goodCount(0).build();
        postRepository.saveAll(List.of(publicPost, privatePost));

        // When
        List<Post> result = postRepository.findByIsPublicTrueOrderByCreatedAtDesc(PageRequest.of(0, 20));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isPublic()).isTrue();
    }
}
```

### カバレッジ目標

| レイヤー | 目標 |
|---------|------|
| Serviceレイヤー | 80%以上（ブランチカバレッジ） |
| Repositoryレイヤー | 主要クエリを全てカバー |
| Controllerレイヤー | 認証ガードのテストを含む主要フロー |

---

## コードレビュー基準

### レビューポイント

**機能性**:
- [ ] 要件（PRD・functional-design.md）を満たしているか
- [ ] エッジケースが考慮されているか
- [ ] エラーハンドリングが適切か

**セキュリティ（最重要）**:
- [ ] 他ユーザーのリソースへのアクセス制御がServiceレイヤーで実装されているか
- [ ] 機密情報がハードコードされていないか
- [ ] SQLインジェクション・XSSへの対策がされているか
- [ ] OpenBDの書誌情報をユーザーが編集できないようになっているか

**可読性**:
- [ ] 命名が明確か（略語・一文字変数がないか）
- [ ] 「なぜそうするか」が不明な箇所にコメントがあるか
- [ ] Controllerが薄く保たれているか（ビジネスロジックがServiceに移っているか）

**パフォーマンス**:
- [ ] N+1クエリが発生していないか（ループ内でのDB呼び出し）
- [ ] ページネーションが適用されているか（一覧系）

### レビューコメントの書き方

**優先度を明示する**:

```
[必須] セキュリティ: 他ユーザーの記事を削除できてしまいます。
       Serviceレイヤーで userId チェックを追加してください。

[推奨] N+1: ループ内で goodRepository.existsByUserIdAndPostId を
       呼んでいます。postIds をまとめて一括クエリにしましょう。

[提案] この変数名 `f` → `postForm` にするとより読みやすいと思います。

[質問] この goodCount の更新がトランザクション内に入っている理由を
       教えてください。
```

---

## 開発環境セットアップ

### 必要なツール

| ツール | バージョン | 備考 |
|--------|-----------|------|
| devcontainer（推奨） | 最新 | Java 21 + Maven + MySQL を含む統一環境 |
| Java | 21（LTS） | devcontainer未使用時 |
| Maven | 3.9.x | devcontainer未使用時 |
| MySQL | 8.0 | devcontainer未使用時 |
| IntelliJ IDEA | 最新 | 推奨IDE |

### devcontainer を使ったセットアップ（推奨）

```bash
# 1. リポジトリのクローン
git clone <repository-url>
cd bukuro

# 2. VS Code でdevcontainerを開く（Reopen in Container）
# または IntelliJ IDEA の Dev Containers プラグインを使用

# 3. ローカル設定ファイルを作成
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
# DB接続情報を編集

# 4. アプリ起動
mvn spring-boot:run
```

### ローカル環境セットアップ（devcontainer未使用）

```bash
# 1. リポジトリクローン
git clone <repository-url>
cd bukuro

# 2. MySQLにDBとユーザーを作成
mysql -u root -p
> CREATE DATABASE bukuro_dev CHARACTER SET utf8mb4;
> CREATE USER 'bukuro'@'localhost' IDENTIFIED BY 'password';
> GRANT ALL ON bukuro_dev.* TO 'bukuro'@'localhost';

# 3. ローカル設定を作成
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
# spring.datasource.url / username / password を設定

# 4. 依存関係のダウンロード・起動
mvn spring-boot:run
```

### 主要コマンド

```bash
# アプリ起動
mvn spring-boot:run

# テスト実行
mvn test

# テスト実行 + JaCoCoカバレッジレポート生成（target/site/jacoco/index.html）
mvn verify

# ビルド（jar生成）
mvn package -DskipTests

# 特定クラスのテストのみ実行
mvn test -Dtest=PostServiceTest
```

### JaCoCoカバレッジ設定

`pom.xml` に以下を追加してカバレッジ計測を有効にする:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <includes>
                            <include>com.bukuro.service.*</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 実装チェックリスト

新機能実装完了前に以下を確認する:

### コード品質
- [ ] 命名が明確・一貫している（略語・一文字変数なし）
- [ ] Controllerにビジネスロジックが含まれていない
- [ ] JPAエンティティをそのままAPIレスポンスに返していない（DTOに変換）
- [ ] マジックナンバーが定数に切り出されている

### セキュリティ
- [ ] 他ユーザーのリソースへのアクセス制御が実装されている
- [ ] 機密情報がコードにハードコードされていない
- [ ] VueでXSS対策（テンプレート補間`{{ }}`を使用、`v-html`はユーザー入力に使わない）

### テスト
- [ ] Serviceのユニットテストが追加・更新されている
- [ ] DB制約・カスタムクエリを変更した場合、Repositoryの統合テストが追加・更新されている
- [ ] 認証ガードを変更した場合、Controllerのテスト（`@WebMvcTest`）が追加・更新されている
- [ ] `mvn verify` が全てパスし、Serviceレイヤーのカバレッジが80%以上を維持している

### データ
- [ ] DB変更がある場合、マイグレーションスクリプトが用意されている
- [ ] 一覧系のエンドポイントにページネーションが適用されている
