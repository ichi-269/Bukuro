# リポジトリ構造定義書 (Repository Structure Document)

## プロジェクト構造

```
bukuro/
├── src/
│   ├── main/
│   │   ├── java/com/bukuro/          # Javaソースコード
│   │   │   ├── controller/           # Controllerレイヤー
│   │   │   ├── service/              # Serviceレイヤー
│   │   │   ├── client/               # 外部APIクライアント
│   │   │   ├── repository/           # Repositoryレイヤー
│   │   │   ├── entity/               # JPAエンティティ
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── exception/            # カスタム例外クラス
│   │   │   ├── config/               # Spring設定クラス
│   │   │   └── BukuroApplication.java # アプリケーションエントリポイント
│   │   └── resources/
│   │       ├── templates/            # Thymeleafテンプレート
│   │       ├── static/               # 静的ファイル（CSS/JS/画像）
│   │       ├── application.properties         # 共通設定
│   │       └── application-local.properties   # ローカル設定（gitignore対象）
│   └── test/
│       └── java/com/bukuro/
│           ├── service/              # Serviceユニットテスト
│           ├── repository/           # Repository統合テスト
│           └── controller/           # Controllerテスト（WebMvcTest）
├── docs/                             # プロジェクトドキュメント
├── .steering/                        # ステアリングファイル（作業計画）
├── .claude/                          # Claude Code設定
├── .devcontainer/                    # 開発コンテナ設定
├── pom.xml                           # Mavenビルド設定
├── .gitignore
└── README.md
```

---

## Javaパッケージ詳細

### パッケージ基底: `com.bukuro`

---

### `controller/` — Controllerレイヤー

**役割**: HTTPリクエストのルーティング、入力バリデーション、Thymeleafテンプレートへのモデル渡し

**配置クラス**:
- `HomeController.java` — ホーム画面（未ログイン / ログイン済み）
- `AuthController.java` — 新規登録・ログイン
- `BookSearchController.java` — ISBN検索・書籍確認
- `ShelfController.java` — 本棚管理（一覧・ステータス変更・削除）
- `PostController.java` — 記事作成・編集・削除・詳細
- `MyPageController.java` — マイページ・プロフィール編集
- `UserController.java` — 他ユーザーページ
- `FollowController.java` — フォロー・アンフォロー
- `GoodController.java` — グッド追加・取り消し
- `GlobalExceptionHandler.java` — 共通エラーハンドリング（`@ControllerAdvice`）

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
├── MyPageController.java
├── UserController.java
├── FollowController.java
├── GoodController.java
└── GlobalExceptionHandler.java
```

---

### `service/` — Serviceレイヤー

**役割**: ビジネスロジック（所有権チェック・重複検証・ステータス遷移）、外部APIコール

**配置クラス**:
- `UserService.java` — ユーザー登録・検索
- `BookSearchService.java` — OpenBD APIコール・書誌情報取得
- `ShelfService.java` — 本棚追加・ステータス更新・削除
- `PostService.java` — 記事CRUD・公開制御・所有権チェック
- `FollowService.java` — フォロー・アンフォロー
- `GoodService.java` — グッド追加・取り消し・good_count同期
- `StatsService.java` — 月別読書冊数集計

**命名規則**: `[機能名]Service.java`（PascalCase + `Service`接尾辞）

**依存関係**:
- 依存可能: `repository/`, `entity/`, `dto/`, `exception/`, `client/`
- 依存禁止: `controller/`（上位レイヤーへの逆依存禁止）

---

### `client/` — 外部APIクライアント

**役割**: 外部HTTP APIとの通信を担当する。Serviceから呼ばれる。

**配置クラス**:
- `OpenBdApiClient.java` — OpenBD HTTP通信クライアント（書誌情報取得）

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
- `ReadingStatus.java` — `WANT_TO_READ` / `READING` / `DONE` Enum

**命名規則**: `[テーブル名をPascalCase].java`

**依存関係**:
- 依存可能: なし（純粋なデータクラス）
- 依存禁止: `service/`, `controller/`, `repository/`

---

### `dto/` — Data Transfer Objects

**役割**: レイヤー間のデータ転送。Entityをそのままコントローラーに渡さないための分離層

**配置クラス**:
- `BookDto.java` — ISBN検索結果（タイトル・著者・書影URL）
- `PostDto.java` — 記事表示用（書誌情報・グッド済みフラグ含む）
- `UserProfileDto.java` — ユーザープロフィール表示用
- `ShelfEntryDto.java` — 本棚エントリ表示用（書誌情報 + 読書状況）
- `MonthlyReadCountDto.java` — 月別読書冊数グラフ用
- `RegisterForm.java` — 新規登録フォームバインディング（`@Valid`用）
- `PostForm.java` — 記事作成・編集フォームバインディング

**命名規則**:
- 表示用: `[対象]Dto.java`
- フォームバインディング用: `[対象]Form.java`

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
- `SecurityConfig.java` — Spring Security設定（認可ルール・CSRF・PasswordEncoder）
- `WebMvcConfig.java` — MVC設定（静的リソース等）

**命名規則**: `[対象]Config.java`

---

## Thymeleafテンプレート構造

```
resources/templates/
├── layout/
│   └── base.html                # 共通レイアウト（ヘッダー・フッター・ナビ）
├── home/
│   ├── index.html               # ホーム（未ログイン）
│   └── feed.html                # ホーム（ログイン済みフィード）
├── auth/
│   ├── register.html            # 新規登録フォーム
│   └── login.html               # ログインフォーム
├── book/
│   ├── search.html              # ISBN検索フォーム
│   ├── confirm.html             # 書誌情報確認画面
│   └── detail.html              # 本の詳細（公開記事一覧）
├── shelf/
│   └── index.html               # 本棚一覧（ステータス別タブ）
├── post/
│   ├── new.html                 # 記事作成フォーム
│   ├── edit.html                # 記事編集フォーム
│   └── detail.html              # 記事詳細
├── mypage/
│   ├── index.html               # マイページ（記事一覧 + グラフ）
│   └── edit.html                # プロフィール編集
├── user/
│   └── profile.html             # 他ユーザーページ
└── error/
    ├── 403.html                 # アクセス拒否
    ├── 404.html                 # ページ未発見
    └── 500.html                 # サーバーエラー
```

**命名規則**: `[画面の役割].html`（小文字 + kebab-case）

---

## 静的ファイル構造

```
resources/static/
├── css/
│   └── main.css                 # アプリ共通スタイル（Bootstrap補完）
├── js/
│   └── shelf-chart.js           # 読書グラフ（Chart.js）の描画スクリプト
└── images/
    └── no-cover.png             # 書影なし時のデフォルト画像
```

**外部ライブラリ（CDN経由で読み込み、静的ファイルとして管理しない）**:
- Bootstrap 5.x
- Chart.js 4.x

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

```
test/java/com/bukuro/
├── service/
│   ├── ShelfServiceTest.java        # 重複登録・ステータス遷移テスト
│   ├── PostServiceTest.java         # 所有権チェック・公開制御テスト
│   ├── GoodServiceTest.java         # 重複グッド防止テスト
│   ├── BookSearchServiceTest.java   # OpenBD APIモックテスト
│   └── StatsServiceTest.java        # 月別集計ロジックテスト
├── repository/
│   ├── ReadingRecordRepositoryTest.java  # UNIQUE制約・クエリ統合テスト
│   ├── PostRepositoryTest.java           # 公開記事フィルタリングテスト
│   ├── FollowRepositoryTest.java         # フォロー関係クエリテスト
│   └── GoodRepositoryTest.java           # UNIQUE制約テスト
└── controller/
    ├── AuthControllerTest.java      # 認証フローのWebMvcTest
    └── PostControllerTest.java      # 記事CRUD のWebMvcTest
```

**命名規則**: `[テスト対象クラス名]Test.java`

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
| Thymeleafテンプレート | `templates/[機能名]/` | `[画面名].html` | `shelf/index.html` |

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

### Thymeleafテンプレート名

- 小文字 + ハイフン区切り（kebab-case）
- 機能別サブディレクトリに配置
- 例: `post/detail.html`, `shelf/index.html`

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
