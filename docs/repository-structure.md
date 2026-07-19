# リポジトリ構造定義書 (Repository Structure Document)

## プロジェクト構造

```
bukuro/
├── frontend/                         # Vue 3 SPAプロジェクト（詳細は「フロントエンド構造」参照）
│   ├── src/
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
├── src/
│   ├── main/
│   │   ├── java/com/bukuro/          # Javaソースコード
│   │   │   ├── controller/           # Controllerレイヤー（@RestController）
│   │   │   ├── service/              # Serviceレイヤー
│   │   │   ├── client/               # 外部APIクライアント
│   │   │   ├── repository/           # Repositoryレイヤー
│   │   │   ├── entity/               # JPAエンティティ
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── exception/            # カスタム例外クラス
│   │   │   ├── config/               # Spring設定クラス
│   │   │   └── BukuroApplication.java # アプリケーションエントリポイント
│   │   └── resources/
│   │       ├── static/               # frontendのビルド成果物の出力先（生成物。gitignore対象）
│   │       ├── application.properties         # 共通設定
│   │       └── application-local.properties   # ローカル設定（gitignore対象）
│   └── test/
│       └── java/com/bukuro/
│           ├── service/              # Serviceユニットテスト
│           └── controller/           # Controllerテスト（WebMvcTest）
├── docs/                             # プロジェクトドキュメント
├── .steering/                        # ステアリングファイル（作業計画）
├── .claude/                          # Claude Code設定
├── .devcontainer/                    # 開発コンテナ設定
├── pom.xml                           # Mavenビルド設定（frontend-maven-pluginでfrontend/のビルドも統合）
├── .gitignore
└── README.md
```

---

## Javaパッケージ詳細

### パッケージ基底: `com.bukuro`

---

### `controller/` — Controllerレイヤー

**役割**: HTTPリクエストのルーティング（`/api`プレフィックス）、入力バリデーション、DTOのJSONレスポンス返却

**配置クラス**:
- `HomeController.java` — ホームフィード取得（`GET /api/home/feed`）
- `AuthController.java` — 新規登録・現在ユーザー取得（`POST /api/register`, `GET /api/me`）
- `BookSearchController.java` — ISBN検索・書名検索・書誌確認
- `ShelfController.java` — 本棚管理（一覧・追加・ステータス変更・削除）
- `PostController.java` — 記事作成・編集・削除・詳細、書籍単体取得
- `UserController.java` — ユーザープロフィール取得・編集、フォロワー/フォロー中一覧
- `FollowController.java` — フォロー・アンフォロー
- `GoodController.java` — グッド追加・取り消し
- `GlobalExceptionHandler.java` — 共通エラーハンドリング（`@RestControllerAdvice`、構造化JSONエラーを返却）

**命名規則**: `[機能名]Controller.java`（PascalCase + `Controller`接尾辞）

**依存関係**:
- 依存可能: `service/`, `dto/`, `exception/`
- 依存禁止: `repository/`, `entity/`（Serviceを介してのみアクセス）

**例**:
```
controller/
├── HomeController.java
├── AuthController.java
├── BookSearchController.java
├── ShelfController.java
├── PostController.java
├── UserController.java
├── FollowController.java
├── GoodController.java
└── GlobalExceptionHandler.java
```

**注**: ログイン(`/api/login`)・ログアウト(`/api/logout`)はControllerメソッドではなく、`SecurityConfig`のSpring Security標準フィルタ（`formLogin`/`logout`）で処理する。

---

### `service/` — Serviceレイヤー

**役割**: ビジネスロジック（所有権チェック・重複検証・ステータス遷移）、外部APIコール

**配置クラス**:
- `UserService.java` — ユーザー登録・検索・プロフィール更新
- `BookSearchService.java` — OpenBD APIコール・ISBN書誌情報取得
- `BookTitleSearchService.java` — NDL API書名検索・OpenBDとのフォールバック連携
- `ShelfService.java` — 本棚追加・ステータス更新・削除
- `PostService.java` — 記事CRUD・公開制御・所有権チェック・フィード取得
- `FollowService.java` — フォロー・アンフォロー・フォロワー/フォロー中取得
- `GoodService.java` — グッド追加・取り消し・good_count同期
- `CustomUserDetailsService.java` — Spring Security `UserDetailsService`実装（メールアドレスでの認証）

**命名規則**: `[機能名]Service.java`（PascalCase + `Service`接尾辞）

**依存関係**:
- 依存可能: `repository/`, `entity/`, `dto/`, `exception/`, `client/`
- 依存禁止: `controller/`（上位レイヤーへの逆依存禁止）

---

### `client/` — 外部APIクライアント

**役割**: 外部HTTP APIとの通信を担当する。Serviceから呼ばれる。

**配置クラス**:
- `OpenBdApiClient.java` — OpenBD HTTP通信クライアント（ISBNからの書誌情報取得）
- `NdlApiClient.java` — 国立国会図書館(NDL) HTTP通信クライアント（書名からの候補検索）

**命名規則**: `[サービス名]ApiClient.java`（PascalCase + `ApiClient`接尾辞）

**依存関係**:
- 依存可能: `dto/`, `exception/`
- 依存禁止: `repository/`, `service/`, `controller/`

---

### `repository/` — Repositoryレイヤー

**役割**: Spring Data JPA Repositoryインターフェース。DB操作（CRUD・カスタムクエリ）

**配置インターフェース**:
- `UserRepository.java` — `findByEmail`, `findByUsername`
- `BookRepository.java` — `findByIsbn`
- `ReadingRecordRepository.java` — `findByUserIdAndStatus`, `findByUserIdAndBookId`
- `PostRepository.java` — 公開記事フィード・ユーザー記事一覧クエリ
- `FollowRepository.java` — `existsByFollowerIdAndFolloweeId`, フォロー関係取得
- `GoodRepository.java` — `existsByUserIdAndPostId`

**命名規則**: `[エンティティ名]Repository.java`（PascalCase + `Repository`接尾辞）

**依存関係**:
- 依存可能: `entity/`
- 依存禁止: `service/`, `controller/`

---

### `entity/` — JPAエンティティ

**役割**: DBテーブルとJavaクラスのマッピング（`@Entity`アノテーション）

**配置クラス**:
- `User.java` — usersテーブル
- `Book.java` — booksテーブル
- `ReadingRecord.java` — reading_recordsテーブル
- `Post.java` — postsテーブル
- `Follow.java` — followsテーブル
- `Good.java` — goodsテーブル
- `ReadingRecord.ReadingStatus` — `WANT_TO_READ` / `READING` / `DONE` Enum（`ReadingRecord.java`内のネストEnum）

**命名規則**: `[テーブル名をPascalCase].java`

**依存関係**:
- 依存可能: なし（純粋なデータクラス）
- 依存禁止: `service/`, `controller/`, `repository/`

---

### `dto/` — Data Transfer Objects

**役割**: レイヤー間のデータ転送。Entityをそのままコントローラーに渡さないための分離層

**配置クラス**:
- `BookDto.java` — 書誌情報（ID・ISBN・タイトル・著者・書影URL）
- `PostDto.java` — 記事表示用（書誌情報・投稿者・グッド済み/所有者フラグ含む）
- `UserDto.java` — ユーザー公開情報（メールアドレスを含まない）
- `MeDto.java` — 自分自身のユーザー情報（メールアドレスを含む。`/api/me`, `/api/register`, `/api/profile/edit`用）
- `UserProfileDto.java` — ユーザープロフィールページ表示用（プロフィール + 公開記事一覧 + フォロー状態）
- `ShelfEntryDto.java` — 本棚エントリ表示用（書誌情報 + 読書状況 + 紐づく記事ID）
- `HomeFeedDto.java` — ホームフィード表示用（フィード種別 + 記事一覧）
- `ErrorResponse.java` — 共通エラーレスポンス（status・code・message・fieldErrors）
- `IsbnRequest.java` / `TitleSearchRequest.java` / `BookConfirmRequest.java` — 書籍検索系リクエストボディ
- `ShelfStatusUpdateRequest.java` — 本棚ステータス更新リクエストボディ
- `RegisterForm.java` — 新規登録リクエストボディ（`@Valid`用）
- `PostForm.java` — 記事作成・編集リクエストボディ
- `ProfileEditForm.java` — プロフィール編集リクエストボディ

**命名規則**:
- 表示用: `[対象]Dto.java`
- リクエストボディ用: `[対象]Form.java` または `[対象]Request.java`

**依存関係**:
- 依存可能: `controller/`, `service/`（渡す先のレイヤー）
- 依存禁止: `repository/`（DBアクセス層との直接結合を禁止）

---

### `exception/` — カスタム例外クラス

**役割**: ビジネスロジック上のエラーを型で表現する

**配置クラス**:
- `BookNotFoundException.java` — ISBNで本が見つからない場合
- `DuplicateRecordException.java` — 重複本棚登録・重複グッド
- `AccessDeniedException.java` — 他ユーザーのリソースへのアクセス
- `ResourceNotFoundException.java` — 記事・ユーザーが存在しない場合
- `ExternalApiException.java` — OpenBD APIエラー・タイムアウト

**命名規則**: `[エラー内容]Exception.java`（PascalCase + `Exception`接尾辞）

**依存関係**:
- 依存可能: `controller/`, `service/`, `client/`（スローする側のレイヤー）
- 依存禁止: `repository/`（例外クラス自体は純粋なデータクラスに近い）

---

### `config/` — Spring設定クラス

**役割**: Spring Beanの設定・カスタマイズ

**配置クラス**:
- `SecurityConfig.java` — Spring Security設定（認可ルール・CSRF・PasswordEncoder・JSON認証ハンドラ）
- `SpaWebConfig.java` — 静的リソース配信設定（`WebMvcConfigurer`）。`/api`以外のパスで静的ファイルが存在しない場合に`index.html`へフォールバックし、Vue Routerのクライアントサイドルーティングに委譲する
- `CsrfCookieFilter.java` — CSRFトークンの遅延解決を回避し、`XSRF-TOKEN` Cookieを毎リクエスト確実に発行するためのフィルタ

**命名規則**: `[対象]Config.java`（フィルタクラスのみ`[対象]Filter.java`）

---

## フロントエンド構造（`frontend/`）

```
frontend/
├── src/
│   ├── main.ts                  # エントリポイント（Pinia・Vue Router登録）
│   ├── App.vue                  # ルートコンポーネント（Navbar/Footer + <router-view>）
│   ├── style.css                # アプリ共通スタイル（旧main.cssを移植）
│   ├── router/
│   │   └── index.ts             # 全画面分のルート定義・認証ガード
│   ├── stores/
│   │   └── auth.ts              # Piniaストア（現在ユーザー・ログイン状態）
│   ├── api/
│   │   ├── client.ts            # axios共通インスタンス（CSRF・401ハンドリング）
│   │   ├── auth.ts / books.ts / shelf.ts / posts.ts
│   │   └── users.ts / follow.ts / good.ts / home.ts
│   ├── types/
│   │   └── index.ts             # バックエンドDTOに対応する型定義
│   ├── components/
│   │   ├── Navbar.vue / Footer.vue / NotFound.vue / ErrorAlert.vue
│   └── views/
│       ├── home/IndexView.vue
│       ├── auth/{Login,Register}View.vue
│       ├── book/SearchView.vue         # ISBN検索・書名検索・確認をウィザード形式で1画面に統合
│       ├── shelf/IndexView.vue
│       ├── post/{New,Edit,Show}View.vue
│       └── user/{Show,ProfileEdit,Followers,Following}View.vue
├── index.html                   # SPAシェル（ビルド時にsrc/main/resources/staticへ出力）
├── vite.config.ts               # build.outDir・開発時/apiプロキシ・Vitest設定
└── package.json
```

**命名規則**:
- ビューコンポーネント: `[画面の役割]View.vue`（PascalCase）、機能別サブディレクトリに配置
- 共通コンポーネント: `[役割].vue`（PascalCase）、`components/`直下に配置
- テストファイル: `[対象].test.ts`（対象ファイルと同じディレクトリに配置）

---

## 静的ファイル配信（`src/main/resources/static/`）

`frontend/`のビルド成果物（`index.html`・`assets/*.js`・`assets/*.css`）が出力される場所。**手書きファイルは配置しない**（`vite.config.ts`の`build.outDir`が直接このディレクトリを指し、ビルドごとに中身が置き換わる。`.gitignore`対象）。

Bootstrap 5.xはCDNではなく`frontend/package.json`のnpm依存として導入し、Viteでバンドルする。

---

## 設定ファイル

### `application.properties`（バージョン管理に含める）

```properties
# データソース（プレースホルダー）
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# OpenBD APIタイムアウト
openbd.api.timeout-seconds=3
```

### `application-local.properties`（`.gitignore` で除外）

ローカル開発用の実際の接続情報を記載する。バージョン管理に含めない。

---

## テスト構造

### バックエンド（`src/test/java/com/bukuro/`）

```
test/java/com/bukuro/
├── client/
│   └── NdlApiClientTest.java
├── service/
│   ├── UserServiceTest.java
│   ├── ShelfServiceTest.java         # 重複登録・ステータス遷移テスト
│   ├── PostServiceTest.java          # 所有権チェック・公開制御テスト
│   ├── FollowServiceTest.java
│   ├── GoodServiceTest.java          # 重複グッド防止テスト
│   ├── BookSearchServiceTest.java    # OpenBD APIモックテスト
│   └── BookTitleSearchServiceTest.java
└── controller/
    ├── AuthControllerTest.java       # 登録・現在ユーザー取得のWebMvcTest
    ├── BookSearchControllerTest.java
    ├── FollowControllerTest.java
    ├── GoodControllerTest.java
    ├── HomeControllerTest.java
    └── UserControllerTest.java
```

**命名規則**: `[テスト対象クラス名]Test.java`

### フロントエンド（`frontend/src/`）

対象ファイルと同じディレクトリに`[対象].test.ts`として配置する（例: `stores/auth.test.ts`, `views/auth/RegisterView.test.ts`）。Vitest + Vue Test Utilsを使用し、APIモジュールは`vi.mock`でモックする。

---

## ファイル配置規則まとめ

### ソースファイル

| ファイル種別 | 配置先 | 命名規則 | 例 |
|------------|--------|---------|-----|
| Controllerクラス | `controller/` | `[機能名]Controller.java` | `PostController.java` |
| Serviceクラス | `service/` | `[機能名]Service.java` | `ShelfService.java` |
| 外部APIクライアント | `client/` | `[サービス名]ApiClient.java` | `OpenBdApiClient.java` |
| Repositoryインターフェース | `repository/` | `[エンティティ名]Repository.java` | `PostRepository.java` |
| JPAエンティティ | `entity/` | `[テーブル名をPascalCase].java` | `ReadingRecord.java` |
| DTOクラス | `dto/` | `[対象]Dto.java` / `[対象]Form.java` | `PostForm.java` |
| 例外クラス | `exception/` | `[エラー内容]Exception.java` | `BookNotFoundException.java` |
| 設定クラス | `config/` | `[対象]Config.java` | `SecurityConfig.java` |
| Vueビューコンポーネント | `frontend/src/views/[機能名]/` | `[画面名]View.vue` | `shelf/IndexView.vue` |
| Vue共通コンポーネント | `frontend/src/components/` | `[役割].vue` | `Navbar.vue` |

### テストファイル

| テスト種別 | 配置先 | 命名規則 | 例 |
|-----------|--------|---------|-----|
| Serviceユニットテスト | `test/.../service/` | `[対象]Test.java` | `PostServiceTest.java` |
| Repository統合テスト | `test/.../repository/` | `[対象]Test.java` | `PostRepositoryTest.java` |
| ControllerテストWebMvc | `test/.../controller/` | `[対象]Test.java` | `PostControllerTest.java` |

---

## 依存関係のルール

```
Controller
    ↓ (OK)
Service
    ↓ (OK)
Repository
    ↓ (OK)
Entity / DB
```

**禁止される依存**:
- `Repository` → `Service` ❌
- `Service` → `Controller` ❌
- `Controller` → `Repository`（直接アクセス） ❌
- `Entity` → その他レイヤー ❌

---

## 命名規則

### Javaクラス名

| 種別 | 規則 | 例 |
|------|------|-----|
| Controller | PascalCase + `Controller` | `PostController` |
| Service | PascalCase + `Service` | `PostService` |
| Repository | PascalCase + `Repository` | `PostRepository` |
| Entity | PascalCase（テーブル名から） | `ReadingRecord` |
| DTO | PascalCase + `Dto` または `Form` | `PostForm`, `PostDto` |
| Exception | PascalCase + `Exception` | `BookNotFoundException` |
| Config | PascalCase + `Config` | `SecurityConfig` |
| Enum | PascalCase（値はUPPER_SNAKE_CASE） | `ReadingStatus.WANT_TO_READ` |

### フロントエンド（TypeScript / Vue）

| 種別 | 規則 | 例 |
|------|------|-----|
| Vueビューコンポーネント | PascalCase + `View.vue` | `ShowView.vue` |
| Vue共通コンポーネント | PascalCase | `Navbar.vue` |
| Piniaストア | camelCase（`use[対象]Store`としてexport） | `stores/auth.ts` → `useAuthStore` |
| APIモジュール | camelCase（リソース名の複数形が基本） | `api/posts.ts`, `api/shelf.ts` |
| 型定義 | PascalCase（`types/index.ts`に集約） | `PostDto`に対応する`Post`インターフェース |

---

## 特殊ディレクトリ

### `.steering/`（ステアリングファイル）

**役割**: 特定の開発作業における「今回何をするか」を定義

**構造**:
```
.steering/
└── [YYYYMMDD]-[task-name]/
    ├── requirements.md      # 今回の作業の要求内容
    ├── design.md            # 変更内容の設計
    └── tasklist.md          # タスクリスト
```

**命名規則**: `20250115-add-post-feature` 形式

### `.devcontainer/`

**役割**: 開発コンテナ設定（Java 21 + Maven + MySQL）

---

## 除外設定

### `.gitignore`

```
# Maven
target/
*.class

# Frontend（Viteビルド成果物。mvn generate-resourcesで自動生成される）
frontend/node_modules/
src/main/resources/static/

# ローカル設定（DB接続情報等）
src/main/resources/application-local.properties

# IDE
.idea/
*.iml
.vscode/

# OS
.DS_Store

# ログ
*.log
```
