# Bukuro — 読書ブログ＆本棚サービス

ISBNを入力するだけで書誌情報を自動取得し、ブログ形式で読書感想を記録・公開できる Web アプリケーションです。

## 機能

| カテゴリ | 機能 |
|----------|------|
| 本棚 | ISBN / 書名検索による書誌情報自動取得、読みたい・読書中・読了のステータス管理 |
| 記事 | 本に紐づけたブログ記事の作成・編集・削除、公開 / 非公開切り替え |
| フィード | フォロー中ユーザーの最新記事、おすすめ記事タブ |
| ソーシャル | ユーザーページ、フォロー / アンフォロー、グッド（いいね） |

## 技術スタック

構成はバックエンド(REST API)とフロントエンド(SPA)に分離しており、単一のSpring Bootサービスとしてデプロイされます。

| レイヤー | 技術 |
|----------|------|
| バックエンド | Java 21 / Spring Boot 3.3 / Spring Security / Spring Data JPA |
| フロントエンド | Vue 3 / TypeScript / Vite / Vue Router / Pinia / axios / Bootstrap 5.3 |
| データベース | MySQL 8 |
| 書誌情報 API | [OpenBD](https://openbd.jp/) / [国立国会図書館 NDL Search API](https://ndlsearch.ndl.go.jp/) |
| テスト | JUnit 5 / Mockito（バックエンド）、Vitest / Vue Test Utils（フロントエンド） |
| ビルド | Maven（`frontend-maven-plugin`経由でフロントエンドのビルドも統合） |

## セットアップ

### 必要環境

- Java 21 以上
- MySQL 8.0 以上
- Maven
- Node.js 24 系（フロントエンドを個別に開発する場合のみ。バックエンドと合わせてビルドする場合は`mvn`が自動取得するため不要）

### 1. リポジトリのクローン

```bash
git clone https://github.com/YOUR_USERNAME/Bukuro.git
cd Bukuro
```

### 2. データベースの作成

```sql
CREATE DATABASE bukuro_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

スキーマの適用:

```bash
mysql -u root -p bukuro_dev < src/main/resources/db/schema.sql
```

### 3. ローカル設定ファイルの作成

`src/main/resources/application-local.properties` を作成し、DB 接続情報を記述します（このファイルは `.gitignore` に含まれています）。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bukuro_dev?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. 起動

バックエンド・フロントエンドを合わせて1コマンドで起動できます（`mvn`がフロントエンドのビルドも自動実行します）。

```bash
mvn spring-boot:run
```

ブラウザで http://localhost:8080 にアクセスしてください。

#### フロントエンドを個別に開発する場合

Vueコンポーネントを編集しながらホットリロードで確認したい場合は、バックエンドとフロントエンドを別々に起動します。

```bash
# ターミナル1: バックエンド(API)を起動
mvn spring-boot:run

# ターミナル2: フロントエンドの開発サーバーを起動（/api への通信は自動的にlocalhost:8080へプロキシされる）
cd frontend
npm install
npm run dev
```

ブラウザで http://localhost:5173 にアクセスしてください。

## テストの実行

```bash
# バックエンド
mvn test

# フロントエンド
cd frontend
npm run test        # ユニットテスト（Vitest）
npm run type-check   # 型チェック（vue-tsc）
```

## 本番デプロイ

Render / Railway へのデプロイを想定しています。以下の環境変数をデプロイ先のダッシュボードで設定してください。

| 環境変数 | 説明 |
|----------|------|
| `DB_URL` | JDBC 接続 URL（例: `jdbc:mysql://host:3306/bukuro`） |
| `DB_USERNAME` | DB ユーザー名 |
| `DB_PASSWORD` | DB パスワード |

`application.properties` はこれらの環境変数を参照する設定になっています。

## 外部 API の利用について

本アプリは書誌情報の取得に以下の外部 API を使用しています。

- **[OpenBD API](https://openbd.jp/)** — ISBN から書影・著者・出版社情報を取得
- **[国立国会図書館 NDL Search API](https://ndlsearch.ndl.go.jp/file/ndlsearch/api/api_conditions.pdf)** — 書名キーワードから書籍候補を検索

OpenBD のデータは「本の販促・紹介目的」での利用規約に従い、書誌情報のユーザー編集機能は設けていません。

## ライセンス

MIT
