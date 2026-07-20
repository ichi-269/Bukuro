# タスクリスト

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

### 必須ルール
- **全てのタスクを`[x]`にすること**
- 「時間の都合により別タスクとして実施予定」は禁止
- 「実装が複雑すぎるため後回し」は禁止
- 未完了タスク（`[ ]`）を残したまま作業を終了しない

### 本タスク固有の注意(実行環境の制約)

この開発環境にはAWS CLIの認証情報が存在しない(`aws sts get-caller-identity`不可)。そのため**フェーズ8(ユーザー実施タスク)はClaude Codeのツールでは技術的に実行不可能**であり、design.mdの「前提: 実行環境の制約」に明記済みの既知の制約である。これは「時間の都合」による任意のスキップではなく、認証情報がないという技術的理由によるものなので、フェーズ8はユーザー自身の実行・報告をもって完了とする。フェーズ1〜7(Claude Codeが作成可能な成果物一式)は通常どおり全タスク`[x]`まで完了させること。

### タスクが大きすぎる場合
- タスクを小さなサブタスクに分割
- 分割したサブタスクをこのファイルに追加
- サブタスクを1つずつ完了させる

---

## フェーズ1: アプリのコンテナ化

- [x] Dockerfile作成(マルチステージビルド)
  - [x] ビルドステージ: `maven:3.9-eclipse-temurin-21`で`mvn verify -DskipTests`相当のビルド(フロントエンドビルド含む)
  - [x] 実行ステージ: `eclipse-temurin:21-jre-alpine`でjarのみコピー、`JAVA_TOOL_OPTIONS=-Xmx256m -Xms128m`をデフォルト設定
- [x] `.dockerignore`作成(target/, frontend/node_modules/, .steering/, .git/等を除外)
- [x] `docker build -t bukuro:local .` でビルドが成功することを確認

## フェーズ2: docker-compose / Caddy構成

- [x] `deploy/docker-compose.prod.yml`作成(app / mysql / caddy)
- [x] `deploy/Caddyfile`作成(`:80`で`app:8080`へリバースプロキシ、将来のドメイン導入コメント付き)
- [x] `deploy/.env.example`作成(ECR_REPOSITORY, DB_ROOT_PASSWORD, DB_USERNAME, DB_PASSWORD等のプレースホルダーとコメント。実ファイルの`.env`はEC2上で手動作成しGit管理しないことを明記)
- [x] `docker compose -f deploy/docker-compose.prod.yml config` が構文エラーなく通ることを確認
- [x] ローカルで`docker compose -f deploy/docker-compose.prod.yml up`を実行し、Caddy経由(`http://localhost`)でアプリ画面(200)・API(`/api/me`, 200)が表示されDB接続できることを確認
  - [x] (副産物として発覚した既存バグ修正): `ReadingRecord.rating`がJPA既定でINTEGER型と推論され、`schema.sql`の`TINYINT`定義と不一致のため`ddl-auto=validate`(prod想定)で起動失敗することを発見。エンティティに`@Column(columnDefinition = "TINYINT")`を追加して解消(`src/main/java/com/bukuro/entity/ReadingRecord.java`)

## フェーズ3: EC2セットアップ関連スクリプト

- [x] `deploy/bukuro-app.service`作成(systemdユニット、`docker compose up -d`をoneshot実行)
- [x] `deploy/ec2-init.sh`作成
  - [x] Docker / Docker Composeプラグインのインストール処理
  - [x] swapファイル作成処理(2GBインスタンス対策)
  - [x] `bukuro-app.service`の配置と`systemctl enable`
  - [x] `.env`は自動生成せず、手動作成が必要な旨の案内メッセージを出力(機密情報保護のため)
- [x] `deploy/deploy.sh`作成
  - [x] `set -euo pipefail`
  - [x] S3から`docker-compose.prod.yml` / `Caddyfile` / `schema.sql`を同期
  - [x] ECRログイン → `docker compose pull` → `docker compose up -d` → `docker image prune -f`
  - [x] `bash -n`で構文チェック済み

## フェーズ4: AWSリソース構築手順書

- [x] `deploy/aws-setup-commands.md`作成
  - [x] GitHub OIDC providerの作成コマンド
  - [x] GitHub Actions用IAMロール作成コマンド(信頼ポリシー: 対象リポジトリ限定、権限ポリシー: 対象ECR/EC2/S3限定)
  - [x] EC2用IAMロール・インスタンスプロファイル作成コマンド(SSMコア + ECR読み取り + S3読み取り)
  - [x] ECRリポジトリ作成コマンド(イメージスキャン有効化含む)
  - [x] S3バケット作成コマンド(デプロイ設定配置用)
  - [x] セキュリティグループ作成コマンド(80番のみ許可、22番不可)
  - [x] EC2インスタンス起動コマンド(AMI・インスタンスタイプ・IAMインスタンスプロファイル・セキュリティグループ・EBSサイズ指定)
  - [x] GitHub Secrets/Variablesに設定すべき値一覧(ロールARN、ECRリポジトリURL、S3バケット名、EC2インスタンスID)

## フェーズ5: GitHub Actionsワークフロー

- [x] `.github/workflows/deploy.yml`作成
  - [x] `test`ジョブ(mysqlサービスコンテナ、`mvn verify`、`npm run test`)
  - [x] `build-and-push`ジョブ(OIDC認証、QEMU+buildxでarm64イメージビルド、ECR push、S3へdeploy/一式をsync)
  - [x] `deploy`ジョブ(DescribeInstances→必要なら起動待機・SSM Online待機→SSM SendCommand→結果待機・失敗時ジョブ失敗)
- [x] YAML構文を確認(`actionlint`は環境に未導入のため`python3 -c "import yaml; yaml.safe_load(...)"`で構文解析、および手動レビューで確認)

## フェーズ6: ドキュメント更新

- [x] `docs/architecture.md`の「システム構成(デプロイ構成)」「デプロイ先」「バックアップ戦略」「リソース使用量」「CI/CDパイプライン」「機密情報の管理方針」セクションをAWS EC2構成に書き換え(Render/Railwayの記述はゼロ件になったことを`grep`で確認済み)
- [x] `README.md`の「本番デプロイ」セクションをAWS EC2向け(環境変数の設定場所が`.env`であること等)に更新

## フェーズ7: 品質チェックと修正

- [x] `mvn verify` が通ることを確認(既存テストに影響がないこと。EXIT_CODE=0)
- [x] `docker build -t bukuro:local .` が通ることを確認(最終確認、EXIT=0)
- [x] `docker compose -f deploy/docker-compose.prod.yml config` が通ることを確認(最終確認、"compose config OK")

## フェーズ8: ユーザー実施タスク(Claude Code実行環境では技術的に実行不可)

この環境にはAWS認証情報がないため、以下はユーザー自身のAWSアカウントで実行する。`deploy/aws-setup-commands.md`のコマンドを使用する。

- [ ] AWSリソース一式の作成(OIDC provider / IAMロール2種 / ECRリポジトリ / S3バケット / セキュリティグループ / EC2インスタンス)
- [ ] EC2インスタンス上での初回セットアップ実行(`ec2-init.sh`実行、`.env`作成)
- [ ] GitHub Secrets/Variablesの設定
- [ ] mainへのpushで`.github/workflows/deploy.yml`が成功し、ブラウザからアプリにアクセスできることの確認
- [ ] EC2の`stop-instances`→`start-instances`後もデータが保持され、手動操作なしにサービスが復帰することの確認

---

## 実装後の振り返り

### 実装完了日
2026-07-20(フェーズ1〜7。フェーズ8はユーザー実施待ち)

### 計画と実績の差分

**計画と異なった点**:
- design.md策定時点ではEC2アーキテクチャ(arm64 `t4g.small`)とGitHub Actionsランナー(amd64)の差異への言及がなかった。実装時に、Dockerイメージがamd64でビルドされるとGravitonインスタンス上で動作しない問題に気づき、`build-and-push`ジョブに`docker/setup-qemu-action` + `docker/setup-buildx-action`を追加し、`platforms: linux/arm64`でクロスビルドする方針に変更した(ビルド時間はQEMUエミュレーションのため増加するが、コストの安いGraviton運用を維持する判断)

**新たに必要になったタスク**:
- ローカルでの`docker compose up`検証中に、MySQL 8のデフォルト認証方式(`caching_sha2_password`)でJDBC接続が`Public Key Retrieval is not allowed`エラーになることが判明し、`docker-compose.prod.yml`のJDBC URLに`allowPublicKeyRetrieval=true`を追加した
- 同じくローカル検証中に、`ReadingRecord.rating`(JPAエンティティ)と`schema.sql`の列型不一致(`INTEGER` vs `TINYINT`)により`ddl-auto=validate`(prod想定)で起動失敗する既存バグを発見し、エンティティに`@Column(columnDefinition = "TINYINT")`を追加して解消した。ローカル開発では`ddl-auto=update`を使っており今まで表面化していなかった

**技術的理由でスキップしたタスク**（該当する場合のみ）:
- フェーズ8の各タスク
  - スキップ理由: この開発環境にAWS認証情報が存在せず、実AWSリソースの作成・確認はClaude Codeのツールから実行不可能なため(design.md「前提: 実行環境の制約」に既知の制約として明記済み)
  - 代替実装: フェーズ4で作成した`deploy/aws-setup-commands.md`の手順に従い、ユーザー自身が実行・確認する

**⚠️ 注意**: 「時間の都合」「難しい」などの理由でスキップしたタスクはここに記載しないこと。全タスク完了が原則。

### 学んだこと

**技術的な学び**:
- MySQL公式イメージ + mysql-connector-j 8.xの組み合わせでは、`useSSL=false`かつ`allowPublicKeyRetrieval`未指定だと`caching_sha2_password`認証で接続できない。本番相当のDocker検証を実際に動かして初めて気づけた問題であり、「compose configの構文チェックだけ」では発見できなかった
- `ddl-auto=validate`(prod)は、開発でよく使う`ddl-auto=update`では検出できないスキーマとエンティティの型不一致を検出する。本番相当プロファイルでの起動確認は、たとえインフラタスクの中であっても価値がある

**プロセス上の改善点**:
- ステアリングファイル(requirements→design→tasklist)を先に1つずつ承認を得てから実装に入ったことで、「EC2停止中のpushはどう扱うか」「ドメインを用意するか」といった仕様の分岐点を実装前に確定でき、手戻りがなかった
- 「ローカルでdocker compose upして実際に200が返ることを確認する」をtasklistのタスクとして明示していたことで、机上のYAML/Dockerfileレビューだけでは見つからなかった2件のバグ(MySQL認証・スキーマ型不一致)を実装中に検出できた

### 次回への改善提案
- 次にAWS上での動作確認(フェーズ8)を行う際は、初回`deploy.sh`実行時にログを確認し、特にMySQLの初回起動(`docker-entrypoint-initdb.d`によるschema.sql適用)が成功しているかを重点的に見ること
- ドメインを取得した場合は、design.mdの「将来の拡張性」に記載の通り`deploy/Caddyfile`の1行変更で自動HTTPS化できる。あわせてRoute53連携も検討するとよい
