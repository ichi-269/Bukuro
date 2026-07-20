# 要求内容

## 概要

Bukuroのデプロイ先を、現在docs/architecture.mdに記載のRender/Railway想定からAWSに変更する。EC2インスタンス1台にアプリ(Spring Boot)とDB(MySQL)を同居させ、インスタンスは基本停止・必要時のみ起動する運用とし、GitHub mainブランチへのpushで自動デプロイされる仕組みを構築する。

## 背景

- 個人開発規模のサービスであり、常時稼働のPaaS/マネージドDBよりも、使う時だけ課金されるEC2運用の方がコストを抑えられる。
- GitHubで開発しているため、mainマージ後に手動でデプロイ作業をしたくない。
- インスタンスを停止している間もEBSボリューム上のデータ(DB含む)は保持されるため、「基本停止・起動したら即復旧」が成立する。

## 実装対象の機能

### 1. EC2上でのアプリ+DB同居構成
- Docker Composeで「Spring Bootアプリコンテナ」「MySQLコンテナ」「Caddy(リバースプロキシ+自動HTTPS)コンテナ」を1台のEC2上に構成する。
- MySQLのデータディレクトリはEBS上の永続ボリュームにマウントし、インスタンスをstop/startしてもデータが消えないようにする。
- docker composeをsystemdサービス化し、EC2起動(OS起動)時に自動でコンテナ群が立ち上がるようにする。

### 2. GitHub Actionsによる自動デプロイ
- mainブランチへのpushをトリガーに、GitHub Actionsで以下を実行する:
  1. `mvn verify`によるビルド・テスト(フロントエンドのビルド・vitestを含む)
  2. DockerイメージのビルドとAmazon ECRへのpush
  3. EC2インスタンスが停止中の場合は`aws ec2 start-instances`で起動し、起動完了を待機する
  4. AWS SSM Send-Commandで、EC2上で最新イメージのpullとdocker composeの再起動を実行する
  5. デプロイ完了後もインスタンスは稼働状態のまま維持する(停止に戻さない)
- 認証はGitHub Actions OIDCフェデレーション経由のIAMロールを使用し、長期的なAWSアクセスキーをGitHub Secretsに保存しない。

### 3. アクセス方法
- 独自ドメイン・Elastic IPは用意せず、EC2起動の都度AWSコンソール/CLIでパブリックIPを確認してアクセスする運用とする。
- HTTPS終端はEC2上のCaddyコンテナが自動取得する証明書(Let's Encrypt)で行う。ただしIPが起動の都度変わるため、証明書取得方式(IPベースでは通常のLet's Encrypt HTTP-01は使えない)は設計フェーズで詳細を検討する。

## 受け入れ条件

### EC2上でのアプリ+DB同居構成
- [ ] EC2インスタンスを`stop`→`start`しても、DBのデータが失われない
- [ ] EC2インスタンスを`start`しただけで(SSH等の手動操作なしに)アプリ・DBが自動起動し、サービスが利用可能になる

### GitHub Actionsによる自動デプロイ
- [ ] mainブランチへpushすると、GitHub Actionsが自動的にビルド・テスト・デプロイを実行する
- [ ] デプロイ実行時にEC2が停止中でも、自動的に起動されてデプロイが完了する
- [ ] デプロイに使うAWS認証情報はOIDC経由のIAMロールであり、静的なアクセスキーをGitHubに保存しない
- [ ] デプロイ失敗時、GitHub Actionsのジョブが失敗ステータスになりログで原因を確認できる

### アクセス方法
- [ ] EC2起動後、パブリックIPを確認してブラウザからアプリにアクセスできる

## 成功指標

- mainへのpushからサービスに最新版が反映されるまで、手動のSSH操作が不要である
- EC2を意図的に停止した状態でも追加コストがほぼ発生しない(EBS分のみ)

## スコープ外

以下はこのフェーズでは実装しません:

- 独自ドメインの取得・DNS設定(Route53等)
- Elastic IPによるIP固定
- Application Load Balancer / Auto Scaling等の冗長化構成
- 本番相当のモニタリング・アラート基盤(CloudWatch詳細監視等)
- ステージング環境の構築(まずは本番相当の単一環境のみ)

## 参照ドキュメント

- `docs/architecture.md` - アーキテクチャ設計書(現在Render/Railway想定。本タスクでAWS EC2構成に更新する)
- `docs/development-guidelines.md` - 開発ガイドライン
- `README.md` - 現在の本番デプロイ手順(Render/Railway向け環境変数の説明を含む)
