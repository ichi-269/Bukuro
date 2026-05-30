# タスクリスト: ユーザー認証

## フェーズ1: プロジェクト基盤

- [x] pom.xml を作成する（Spring Boot 3.3.x, Security, JPA, Thymeleaf, MySQL, Lombok, Validation, TestContainers）
- [x] BukuroApplication.java を作成する
- [x] application.properties を作成する（DB接続はプレースホルダー）
- [x] application-local.properties.example を作成する
- [x] .gitignore を作成する

## フェーズ2: エンティティ・リポジトリ

- [x] User.java エンティティを作成する
- [x] UserRepository.java を作成する（findByEmail, findByUsername）

## フェーズ3: DTOとサービス

- [x] RegisterForm.java を作成する（バリデーションアノテーション付き）
- [x] UserService.java を作成する（register, existsByEmail, existsByUsername）
- [x] CustomUserDetailsService.java を作成する（UserDetailsService実装）

## フェーズ4: Spring Security設定

- [x] SecurityConfig.java を作成する（認可ルール・BCrypt・ログイン設定）

## フェーズ5: Controller

- [x] HomeController.java を作成する（GET /）
- [x] AuthController.java を作成する（GET/POST /register, GET /login）

## フェーズ6: Thymeleafテンプレート

- [x] layout/base.html を作成する（共通レイアウト・ナビゲーション）
- [x] home/index.html を作成する（ホーム・未ログイン画面）
- [x] auth/register.html を作成する（新規登録フォーム）
- [x] auth/login.html を作成する（ログインフォーム）
- [x] error/403.html, 404.html, 500.html を作成する
- [x] static/css/main.css を作成する（最小スタイル）

## フェーズ7: テスト

- [x] UserServiceTest.java を作成する（register重複メール・重複ユーザー名テスト）
- [x] AuthControllerTest.java を作成する（登録フォーム表示・登録成功・バリデーションエラーテスト）

## 申し送り

**実装完了日**: 2026-05-24

### 計画と実績の差分

- 計画通り全フェーズ実装完了
- 追加対応: バリデーション順序の修正（@Valid失敗時にDB重複チェックをスキップ）
- 追加対応: SecurityConfig の `/posts/**` を `/posts/{postId}` GET のみpermitAllに絞り込み
- 追加対応: DaoAuthenticationProvider の二重設定問題を解消（SecurityConfig簡素化 + @SpringBootApplicationで UserDetailsServiceAutoConfiguration を除外）
- 追加対応: CustomUserDetailsService のエラーメッセージからメールアドレスを除去（情報漏洩防止）
- テスト追加: 重複ユーザー名テスト、未認証アクセスの認証ガードテストを追加

### 学んだこと

- `@WebMvcTest` はデフォルトで `@Configuration` クラスを読まないため、カスタム `SecurityFilterChain` を使うテストには `@Import(SecurityConfig.class)` が必要
- Spring Boot の `UserDetailsServiceAutoConfiguration` は `UserDetailsService` Bean が存在しても `InMemoryUserDetailsManager` を生成する場合がある → `exclude` で除外が必要
- `DefaultLoginPageGeneratingFilter` は `formLogin().loginPage(...)` 設定時に無効化される。`@WebMvcTest` でこれを確かめるには `SecurityConfig` をImportする必要がある

### 次のステップ（次の機能実装 → ISBN検索・書誌情報取得）

- 次の機能: `/add-feature ISBN検索・書誌情報取得`
- 前提として `Book` エンティティと `BookRepository` が必要
- `client/` パッケージに `OpenBdApiClient` を作成する（repository-structure.md の設計通り）
- `RestClient` または `WebClient` を使用して OpenBD API を呼び出す
- `BookSearchService` → `BookSearchController` → `book/search.html` / `book/confirm.html` の順に実装

