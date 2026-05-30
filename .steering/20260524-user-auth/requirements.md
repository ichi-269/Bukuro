# 要求内容: ユーザー認証

## 機能概要

メールアドレスとパスワードによる新規登録・ログイン・ログアウト機能。
Spring Security 6.x によるセッションベース認証。

## 受け入れ条件（PRDより）

- [ ] メールアドレス＋パスワードで新規登録できる
- [ ] 登録済みユーザーがログイン・ログアウトできる
- [ ] パスワードはBCrypt(strength=12)でハッシュ化して保存される
- [ ] 未ログイン状態でもホームページ・記事詳細・ユーザーページは閲覧できる
- [ ] ログインが必要な操作には認証ガードがかかる

## スコープ

今回の実装範囲:
1. Mavenプロジェクト基盤（pom.xml・application.properties・BukuroApplication.java）
2. Userエンティティ・UserRepository
3. UserService（register / findByEmail）
4. CustomUserDetailsService（Spring Security連携）
5. AuthController（登録フォーム・登録処理・ログインフォーム）
6. SecurityConfig（認可ルール・CSRF・BCrypt）
7. RegisterForm DTO（バリデーション付き）
8. Thymeleafテンプレート（base.html・register.html・login.html・ホーム未ログイン画面）
9. UserServiceTest（ユニットテスト）
10. AuthControllerTest（WebMvcTest）

## スコープ外

- ログイン後のフィード画面（Phase 2以降）
- プロフィール編集・マイページ（本棚機能と一緒に実装）
