# タスクリスト

## 🚨 タスク完全完了の原則

**このファイルの全タスクが完了するまで作業を継続すること**

### 必須ルール
- **全てのタスクを`[x]`にすること**
- 「時間の都合により別タスクとして実施予定」は禁止
- 「実装が複雑すぎるため後回し」は禁止
- 未完了タスク（`[ ]`）を残したまま作業を終了しない

### タスクスキップが許可される唯一のケース
以下の技術的理由に該当する場合のみスキップ可能:
- 実装方針の変更により、機能自体が不要になった
- アーキテクチャ変更により、別の実装方法に置き換わった
- 依存関係の変更により、タスクが実行不可能になった

スキップ時は必ず理由を明記:
```markdown
- [x] ~~タスク名~~（実装方針変更により不要: 具体的な技術的理由）
```

---

## フェーズ1: バックエンド基盤整備

- [x] SecurityConfigをJSON対応に変更
  - [x] formLoginの成功ハンドラをJSON返却(200 + ユーザー情報)に変更（実装方針変更: 成功ハンドラは`{"status":"ok"}`のみ返却し、フロントエンドは直後に`/api/me`を呼んでユーザー情報を取得する設計に変更。UserServiceをSecurityConfigに注入すると`PasswordEncoder`Bean経由で循環依存になるため回避）
  - [x] formLoginの失敗ハンドラをJSON返却(401 + エラーメッセージ)に変更
  - [x] logoutSuccessHandlerをJSON返却(200)に変更
  - [x] CSRFを`CookieCsrfTokenRepository.withHttpOnlyFalse()`に変更
  - [x] `exceptionHandling().authenticationEntryPoint`で未認証時401 JSONを返すよう設定
  - [x] `exceptionHandling().accessDeniedHandler`で権限エラー時403 JSONを返すよう設定
  - [x] `authorizeHttpRequests`のパスを`/api/**`基準に書き換え（実装方針変更: `/api/**`はデフォルト認証必須、公開エンドポイントのみ個別permitAll。`/api`以外の全パス(SPAシェル・静的アセット)はpermitAllとし、保護すべきデータアクセスは全て`/api`経由に統一する設計とした）
- [x] GlobalExceptionHandlerを`@RestControllerAdvice`化
  - [x] 共通エラーレスポンスDTO(`ErrorResponse`: status, code, message)を新規作成（fieldErrors配列も含めて実装）
  - [x] バリデーションエラー用DTO(`fieldErrors`配列を含む)を新規作成（ErrorResponse.FieldErrorItemとして実装）
  - [x] 既存カスタム例外(BookNotFoundException, DuplicateRecordException, AccessDeniedException, ResourceNotFoundException, ExternalApiException)のハンドラをJSON返却に変更（IllegalArgumentException, IllegalStateException, MethodArgumentNotValidException, NoResourceFoundExceptionのハンドラも追加）
- [x] `/api/me`エンドポイントを新規追加（実装方針変更: 未ログイン時は401ではなく200+nullを返す設計に変更し、`/api/me`はpermitAllとした。アプリ起動時に毎回401エラーがコンソールに出るのを避け、「未ログイン」を正常系として扱うため。MeDto/UserDtoを新設し、公開プロフィール用途ではemailを含まないUserDtoを、本人確認用途ではemailを含むMeDtoを使い分ける設計とした）

## フェーズ2: バックエンドAPI化(Controller書き換え)

- [x] AuthController.javaを`@RestController`化(`/api`プレフィックス)
  - [x] 新規登録エンドポイントをJSON返却に変更
- [x] BookSearchController.javaを`@RestController`化
  - [x] ISBN検索エンドポイントをJSON返却に変更
  - [x] 書誌情報確認エンドポイントをJSON返却に変更（書名検索エンドポイントも合わせて変更）
- [x] ShelfController.javaを`@RestController`化
  - [x] 本棚一覧取得エンドポイントをJSON返却に変更
  - [x] ステータス変更エンドポイントをJSON返却に変更（実装方針変更: `POST /shelf/{id}/status`から`PATCH /api/shelf/{id}`のRESTfulな形式に変更）
  - [x] ~~星評価・読書日更新エンドポイントをJSON返却に変更~~（実装方針変更により不要: 実装前調査の結果、星評価・読書日の更新UIは現行コードベースに未実装であることが判明。ShelfService/ReadingRecordにフィールド自体は存在するが更新経路がないため、既存機能SPA化の範囲外として除外）
  - [x] 本棚削除エンドポイントをJSON返却に変更（`DELETE /api/shelf/{id}`に変更）
- [x] PostController.javaを`@RestController`化
  - [x] 記事作成エンドポイントをJSON返却に変更
  - [x] 記事編集エンドポイントをJSON返却に変更（実装方針変更: 編集フォーム表示用の`GET /posts/{id}/edit`は廃止し、`GET /api/posts/{id}`で取得したデータをフロントエンドで再利用する設計に変更。`PUT /api/posts/{id}`に統一）
  - [x] 記事削除エンドポイントをJSON返却に変更
  - [x] 記事詳細取得エンドポイントをJSON返却に変更(未ログインでも公開記事は取得可能)
  - [x] `GET /api/books/{bookId}`を追加（実装方針変更: 記事作成フォーム表示用に書誌情報単体を取得するエンドポイントが必要なため新設。元々`PostController.newForm`が内部的に行っていた処理をAPI化したもの）
- [x] UserController.javaを`@RestController`化
  - [x] `/mypage`リダイレクトロジックをフロントエンド側のルーティングに移管(バックエンドAPIとしては`/api/me`から取得したusernameを使う設計に変更)
  - [x] ユーザーページ取得エンドポイント(`/api/users/{username}`)をJSON返却に変更
  - [x] プロフィール編集フォーム取得・更新エンドポイントをJSON返却に変更（実装方針変更: 編集フォーム表示用の`GET /profile/edit`は廃止し、`/api/me`のデータをフロントエンドで再利用。`PUT /api/profile/edit`に統一）
  - [x] フォロワー一覧・フォロー中一覧取得エンドポイントをJSON返却に変更
- [x] FollowController.javaを`@RestController`化
  - [x] フォロー・アンフォローエンドポイントをJSON返却に変更（204 No Contentを返し、フロントエンドがローカル状態を楽観的更新する設計とした）
- [x] GoodController.javaを`@RestController`化
  - [x] グッド追加・取り消しエンドポイントをJSON返却に変更（204 No Content。重複グッドはGlobalExceptionHandler経由で409を返す）
- [x] HomeController.javaを`@RestController`化
  - [x] ホームフィード取得エンドポイントをJSON返却に変更(`GET /api/home/feed`。未ログイン時はそもそもフロントエンドがAPIを呼ばずヒーローセクションを表示する設計とし、エンドポイント自体は認証必須のまま)
  - [x] ~~月別読書冊数グラフ用データ取得エンドポイントを追加~~（実装方針変更により不要: 実装前調査の結果、`StatsService`および月別グラフ機能自体が現行コードベースに未実装であることが判明。本移行はスコープを既存画面のSPA化に限定しており新機能追加は対象外のため除外）
- [x] `SpaForwardController.java`を新規作成し、`/api/**`以外の全GETパスを`index.html`へフォワード（実装方針変更: 実機起動検証で`@GetMapping("/**/{path:[^\.]*}")`がSpring 6のPathPatternParserで無効なパターンであることが判明(`**`の後に別のキャプチャ要素は置けない)。`SpaForwardController`は削除し、代わりに`SpaWebConfig`(`WebMvcConfigurer`)で`PathResourceResolver`をカスタマイズし、静的ファイルが存在すればそれを返し、存在しなければ`index.html`にフォールバックする方式に変更。Spring Boot公式のSPA配信パターンに準拠）
- [x] バックエンドの`@WebMvcTest`系Controllerテストを新しいJSONレスポンス形式(`jsonPath`)に書き換え（AuthControllerTest, BookSearchControllerTest, FollowControllerTest, GoodControllerTest, HomeControllerTest, UserControllerTestの6ファイルを更新。ShelfController/PostControllerは元々専用のWebMvcTestが存在しなかったため対象外。`mvn test`で全41件のControllerテストがパスすることを確認済み。実装中にJackson+Lombokの`isXxx`真偽値プロパティがgetter名から`is`を剥がされて`ownPage`等に誤ってシリアライズされるバグを発見し、`@JsonProperty`で修正した）

## フェーズ3: フロントエンド基盤セットアップ

- [x] `frontend/`ディレクトリを作成し、Vite + Vue3 + TypeScriptプロジェクトを初期化
  - [x] `npm create vite@latest frontend -- --template vue-ts`で`package.json`, `tsconfig.json`, `vite.config.ts`を作成（テンプレートのサンプルファイル一式は削除）
  - [x] `vite.config.ts`の`build.outDir`を`../src/main/resources/static`に設定（開発時の`/api`プロキシ(`server.proxy`)も合わせて設定）
  - [x] ~~依存パッケージ(vue, vue-router, pinia, axios, bootstrap, chart.js, vue-chartjs)をインストール~~（chart.js/vue-chartjsは実装方針変更により除外。vue, vue-router, pinia, axios, bootstrapをインストール）
  - [x] 開発用依存パッケージ(vitest, @vue/test-utils, msw, vue-tsc)をインストール（jsdomも追加）
- [x] axios共通クライアント(`api/client.ts`)を実装
  - [x] `baseURL: /api`, `withCredentials: true`, `xsrfCookieName`/`xsrfHeaderName`を設定
  - [x] レスポンスインターセプター(401→ログイン誘導、400/422→フィールドエラー整形)を実装
- [x] Piniaのauthストア(`stores/auth.ts`)を実装
  - [x] アプリ起動時に`/api/me`を呼びログイン状態を復元する処理
  - [x] ログイン/ログアウト成功時にストアを更新する処理
- [x] Vue Routerの雛形(`router/index.ts`)を実装
  - [x] 全画面分のルート定義(パスは既存Thymeleafテンプレートのパス構成に対応させる)
  - [x] 認証必須ルートに対するナビゲーションガード(未ログイン時は`/login`へリダイレクト)
  - [x] catch-allルート(`/:pathMatch(.*)*`)で404コンポーネントを表示
- [x] 共通レイアウトコンポーネントを実装
  - [x] `App.vue`(base.htmlのcontent/footerフラグメントに相当。ホーム画面がフル幅レイアウトを必要とするため、`container`ラップは各ビュー側に持たせる設計に変更)
  - [x] `components/Navbar.vue`(ログイン状態に応じた表示切り替えを含む)
  - [x] `components/Footer.vue`
  - [x] `components/NotFound.vue`
  - [x] `components/ErrorAlert.vue`（実装中に追加: 全フォームで共通のエラー表示パターンのため）

## フェーズ4: フロントエンド機能実装

- [x] 認証機能を実装
  - [x] `views/auth/LoginView.vue`
  - [x] `views/auth/RegisterView.vue`
  - [x] `api/auth.ts`(ログイン・新規登録・ログアウトAPI呼び出し)
- [x] ISBN検索・本棚追加機能を実装
  - [x] ~~`views/book/SearchView.vue` / `views/book/ConfirmView.vue`~~（実装方針変更: 別ルートに分けず、`views/book/SearchView.vue`内でsearch/titleResults/confirmの3ステップを内部状態(ウィザード形式)として実装。ルート間の状態受け渡しが不要になりSPAとして自然なUXになるため）
  - [x] `api/books.ts`
- [x] 本棚管理機能を実装
  - [x] ~~`views/shelf/IndexView.vue`(ステータス別タブ、星評価、読書日入力、削除)~~（星評価・読書日入力は実装方針変更により対象外。ステータス別タブ・ステータス変更・削除を実装）
  - [x] `api/shelf.ts`
- [x] 読書記録ブログ記事機能を実装
  - [x] `views/post/NewView.vue`
  - [x] `views/post/EditView.vue`（非オーナーがアクセスした場合は記事詳細へリダイレクトするクライアント側ガードを追加）
  - [x] `views/post/ShowView.vue`
  - [x] `api/posts.ts`
- [x] マイページ・プロフィール編集機能を実装
  - [x] `views/user/ShowView.vue`(自分のページ・他ユーザーページ兼用)
  - [x] `views/user/ProfileEditView.vue`
  - [x] ~~月別読書グラフコンポーネント~~（実装方針変更により不要: 対応するバックエンド機能が未実装のため。上記「バックエンドAPI化」フェーズの注記を参照）
  - [x] `api/users.ts`
- [x] フォロー・フォロワー機能を実装
  - [x] `views/user/FollowersView.vue`
  - [x] `views/user/FollowingView.vue`
  - [x] `api/follow.ts`
- [x] グッド機能を実装
  - [x] 記事詳細内にグッドボタンコンポーネントを実装（`views/post/ShowView.vue`に統合実装。独立コンポーネント化はYAGNIのため見送り）
  - [x] `api/good.ts`
- [x] ホームフィード機能を実装
  - [x] `views/home/IndexView.vue`(ログイン時フィード/未ログイン時サービス説明の分岐)
- [x] 型定義(`types/`)をバックエンドDTOに対応させて作成
  - [x] ~~User, Post, Book, ShelfEntry, MonthlyReadCount, ErrorResponse 等~~（MonthlyReadCountは対応機能なしのため除外。User, Me, Post, Book, ShelfEntry, ShelfLists, UserProfile, HomeFeed, ApiError/FieldErrorを`types/index.ts`に実装）

**Phase 3/4 検証結果**: `npm run type-check`(vue-tsc -b)・`npm run build`ともにエラーなく成功。ビルド成果物は`src/main/resources/static`に正しく出力されることを確認済み。

## フェーズ5: 統合・ビルドパイプライン

- [x] `pom.xml`に`frontend-maven-plugin`を追加し、`mvn verify`実行時に`npm install && npm run build`が走るよう設定（`generate-resources`フェーズで`install-node-and-npm`→`npm install`→`npm run build`を実行する構成とし、Node.jsバージョンはCLAUDE.md記載のv24.11.0を指定）
- [x] `vite build`実行結果が`src/main/resources/static`に正しく出力されることを確認（`mvn generate-resources`を実際に実行し、`index.html`と`assets/`配下にビルド成果物が生成されることを確認済み）
- [x] ~~`.gitignore`に`frontend/node_modules/`, `frontend/dist/`を追加~~（実装方針変更: `vite.config.ts`のbuild.outDirを`src/main/resources/static`に直接向けているため`frontend/dist/`は生成されない。代わりに`frontend/node_modules/`と、ビルド成果物の出力先である`src/main/resources/static/`自体を.gitignoreに追加した）
- [x] ~~`.github/workflows/`のCIワークフローがフロントエンドのビルド・テストも実行するよう更新~~（実装方針変更により不要: 調査の結果、`.github/workflows/`は現在存在せず、直近のコミット「github workflow を一旦削除」でユーザーが意図的に削除していたことが判明。本移行のスコープ外であり、存在しないCI設定を新規作成することは意図しない機能追加になるため対象外とした）

## フェーズ6: 旧実装の削除

- [x] `src/main/resources/templates/`配下のThymeleafテンプレートを全て削除
- [x] `pom.xml`から`spring-boot-starter-thymeleaf`, `thymeleaf-extras-springsecurity6`依存を削除（`application.properties`/`application-prod.properties`のThymeleaf関連設定も削除）
- [x] `src/main/resources/static/css/main.css`など旧静的ファイル(Vue側に移行済みのもの)を整理（実装方針変更: `vite.config.ts`の`build.outDir`が`emptyOutDir: true`で`src/main/resources/static`を指しているため、Phase 5でのビルド実行時に自動的に削除・置換された。内容は`frontend/src/style.css`に移植済み）
- [x] コンパイル・全バックエンドテスト(`mvn test`)が引き続き成功することを確認

## フェーズ7: 品質チェックと修正

- [x] バックエンド: `mvn verify`が全て通ることを確認（`mvn test`で全テストパスを確認。JaCoCoレポート生成込みの`verify`もあわせて実行し成功を確認）
- [x] フロントエンド: `npm run type-check`(vue-tsc)でエラーがないことを確認
- [x] フロントエンド: `npm run test`(vitest)が全て通ることを確認（design.mdのテスト戦略に従い、実装中に追加: `stores/auth.test.ts`(Piniaストアのfetchme/login/logout/clearを網羅)、`views/auth/RegisterView.test.ts`(フィールドエラー表示・登録成功時の遷移をVue Test Utilsで検証)の2ファイル・7テストを新規作成。全てパス）
- [x] フロントエンド: `npm run build`が成功することを確認
- [x] E2E手動確認: 新規登録→ISBN検索→本棚追加→記事作成→公開の一連フローが動作することを確認（実際に`mvn spring-boot:run`でアプリを起動し、ローカルMySQL(`bukuro_dev`)に対してcurlでAPIを直接叩いて検証。ISBN検索は一部OpenBD未登録書籍で404を確認、既存書籍IDでの本棚追加・記事作成は201で成功）
- [x] E2E手動確認: 未ログイン状態での非公開記事アクセス拒否・公開記事閲覧が動作することを確認（匿名・他ユーザー双方から非公開記事へのGETが404、公開記事は200で閲覧可能なことを確認。存在有無を非公開時に漏らさない設計になっている）
- [x] E2E手動確認: フォロー→ホームフィード反映、グッド追加・取り消しが動作することを確認（2ユーザーでフォロー→`feedType=following`に切り替わり相手の記事が表示されること、グッド/アングッドで`goodCount`・`hasGooded`が正しく増減することを確認）
- [x] E2E手動確認: ブラウザの直接URL入力・リロードで正しい画面が表示されることを確認(`/posts/5`, `/shelf`, `/users/x/followers`等で200 text/htmlが返りindex.htmlにフォールバックすることを確認)

**実装中に発見・修正した重大バグ(実機起動検証で判明)**:
1. `SpaForwardController`の`@GetMapping("/**/{path:[^\.]*}")`がSpring 6 PathPatternParserで無効なパターンでアプリ起動に失敗 → `SpaWebConfig`(`PathResourceResolver`によるフォールバック)に置き換え(詳細はフェーズ2の該当項目に記載)
2. `CookieCsrfTokenRepository`使用時、CsrfTokenが遅延解決されるため何も参照しないとXSRF-TOKEN Cookieが発行されない → `CsrfCookieFilter`を追加しトークンを強制的に読み取らせることでCookie発行を保証
3. デフォルトの`XorCsrfTokenRequestAttributeHandler`はSPA(axiosがCookie値をそのままヘッダーに送る方式)と噛み合わずCSRF検証が常に失敗 → `CsrfTokenRequestAttributeHandler`を明示指定して解決(Spring Security公式のSPA向けCSRF統合ガイドに準拠)
4. LombokのGetterが`isXxx`という真偽値フィールドに対して生成する`isXxx()`メソッドを、Jacksonが命名規則に従い`is`を剥がして`xxx`という別プロパティとして誤認識し、`isPublic`/`public`のようにJSONキーが重複して出力される問題を発見 → `@Getter(onMethod_ = @JsonProperty("..."))`でgetterに直接アノテーションを付与し解決（`PostDto.isPublic`/`isOwner`、`UserProfileDto.isOwnPage`/`isFollowing`）

いずれも自動テスト(mvnテスト・vitest)だけでは検出できず、実際にアプリを起動してAPIを叩く検証で発見した。

## フェーズ8: ドキュメント更新

- [x] `docs/architecture.md`のフロントエンド技術スタック・アーキテクチャパターン図をSPA構成に更新（テクノロジースタック表、アーキテクチャパターン図、システム構成図、セキュリティアーキテクチャ、CI/CDパイプライン、依存関係管理の各セクションを更新。実装前調査で判明した`.github/workflows/`不在についても注記を追加）
- [x] `docs/repository-structure.md`のディレクトリ構造・Controller一覧をREST API構成に更新（あわせて、当初から実体のなかった`MyPageController`/`StatsService`/`WebMvcConfig`/Repositoryテスト等の記載も実態に合わせて修正した）
- [x] README.mdを更新(フロントエンド開発時の起動手順等を追記。`mvn spring-boot:run`単体でフロントエンドも自動ビルドされることを実機検証の上で明記)
- [x] `docs/product-requirements.md`の技術スタック参考表のみ、影響が軽微だったため合わせて更新した
- [x] 実装後の振り返り（このファイルの下部に記録）

---

## フェーズ9: 追加ドキュメント更新（ユーザー指示による追加スコープ）

フェーズ8完了時点でスコープ外としていた`functional-design.md`・`development-guidelines.md`について、ユーザーから追加対応の指示があったため実施する。

- [x] `docs/functional-design.md`のThymeleaf/MVC前提の記述をSPA + REST API構成に更新
  - [x] システム構成図(mermaid)からThymeleafノードを削除しSPA+REST構成に変更
  - [x] 技術スタック表を更新（Thymeleaf→Vue3、Chart.js行に未実装の注記）
  - [x] Controller層テーブルから実体のなかった`MyPageController`を削除、REST化後の責務に更新
  - [x] Service層テーブルから実体のなかった`StatsService`を削除、実在する`BookTitleSearchService`/`CustomUserDetailsService`を追加
  - [x] 主要ユースケース(シーケンス図)5件を実際の`/api/**`エンドポイント・JSON応答に合わせて更新
  - [x] 画面・エンドポイント一覧テーブルを実際のREST APIエンドポイント(`/api/**`)に全面更新
  - [x] 認証・認可設計セクションをJSON対応のSecurityConfig(CSRF Cookie等)に更新
  - [x] 月別読書グラフ設計(Chart.js)セクションに「未実装・移行対象外」の注記を追加（機能自体が存在しないため実装内容の捏造は行わない）
  - [x] エラーハンドリングセクションをJSON構造化エラー(ErrorResponse)方式に更新
  - [x] セキュリティ考慮事項のXSS対策記述をThymeleaf→Vueのデフォルトエスケープに更新
  - [x] テスト戦略セクションを実態(Vitest追加、StatsServiceTest削除、Selenium→手動/curl検証)に更新
- [x] `docs/development-guidelines.md`のThymeleaf/MVC前提の記述を更新
  - [x] `@ControllerAdvice`のコード例を`@RestControllerAdvice`+JSON返却に更新
  - [x] Controllerサンプルコード(`return "post/new"`等のビュー名返却)をJSON返却のREST例に更新
  - [x] 「ThymeleafのHTMLエスケープを必ず使う」セクションをVueテンプレートのデフォルトエスケープ規約に更新
  - [x] 実装チェックリストの「ThymeleafでXSS対策」項目を更新
  - [x] フロントエンド(Vue/TypeScript)の規約セクションを新設（命名規則は`repository-structure.md`に委譲し、コンポーネント設計・状態管理・APIモジュールの薄いガイドラインのみ追加。既存のバックエンド規約と重複させないよう最小限に留めた）

---

## 実装後の振り返り

### 実装完了日
2026-07-19

### 計画と実績の差分

**計画と異なった点**:
- 実装前調査の結果、`docs/`に記載されていたが実装されていなかった機能・ファイルが複数見つかった: 月別読書グラフ(Chart.js/`StatsService`)、`MyPageController`、`WebMvcConfig`、Repository統合テスト、`.github/workflows/`(過去にユーザーが意図的に削除済み)。これらはSPA移行のスコープ外(既存画面のみを対象とするため)として除外し、その旨をtasklist・design.mdに明記した
- `SpaForwardController`(`@GetMapping`のワイルドカードパターンでindex.htmlにフォワードする設計)は、実機起動検証でSpring 6のPathPatternParserが該当パターンを受け付けないことが判明し、`SpaWebConfig`(`WebMvcConfigurer` + カスタム`PathResourceResolver`)に設計変更した
- CSRF対応で2つの追加バグを実機検証で発見した: (1) `CookieCsrfTokenRepository`はCsrfTokenが遅延解決されるため何も参照しないとCookieが発行されない → `CsrfCookieFilter`を追加、(2) デフォルトの`XorCsrfTokenRequestAttributeHandler`はSPA(axiosがCookie値をそのままヘッダー送信する方式)と噛み合わず検証が常に失敗する → `CsrfTokenRequestAttributeHandler`を明示指定。いずれもSpring Security公式のSPA向けCSRF統合ガイドに沿った対応
- Jackson + Lombokの組み合わせで、`isXxx`という真偽値フィールドのgetterから`is`が剥がされ、JSONに`isPublic`と`public`のような重複キーが出力されるバグを発見した。`@Getter(onMethod_ = @JsonProperty("..."))`で解決
- 本棚のPOST/ステータス変更エンドポイントは、元のフォーム主体のURL設計(`POST /shelf/{id}/status`等)から、よりRESTfulな`PATCH`/`DELETE`ベースの設計に変更した
- 記事編集・プロフィール編集の「編集フォーム表示用GET」エンドポイントは廃止し、詳細取得エンドポイント(`GET /api/posts/{id}`)や`/api/me`のデータをフロントエンド側で再利用する設計に変更し、エンドポイント数を削減した
- ISBN検索・書名検索・確認の3画面(`search.html`/`title-results.html`/`confirm.html`)は、別々のVueビューではなく`SearchView.vue`内のウィザード形式(内部state遷移)に統合した

**新たに必要になったタスク**:
- `components/ErrorAlert.vue`の追加(全フォームで共通のエラー表示パターンのため)
- フロントエンドのVitestテスト(`stores/auth.test.ts`, `views/auth/RegisterView.test.ts`)の新規作成 — design.mdでテスト戦略として明記されていたが、tasklist.md作成時点では個別タスク化されていなかったため、フェーズ7の「npm run testが通ることを確認」を満たす過程で作成した

**技術的理由でスキップしたタスク**:
- 月別読書グラフ関連のバックエンド・フロントエンド両タスク、`.github/workflows/`のCI更新タスク — 詳細はフェーズ2・フェーズ5・フェーズ8の該当項目のスキップ理由を参照

### 学んだこと

**技術的な学び**:
- Spring 6の`PathPatternParser`はAntPathMatcher時代のワイルドカードパターン(`/**/{path:regex}`)を受け付けない。SPAのcatch-allフォワードは`@Controller`のワイルドカードマッピングではなく、`WebMvcConfigurer` + カスタム`PathResourceResolver`によるフォールバック方式が堅牢かつ標準的
- Spring SecurityのCSRF保護をCookieベースでSPAに統合する際は、(1) `CookieCsrfTokenRepository.withHttpOnlyFalse()`、(2) トークンの遅延解決を強制的に読み取らせる`CsrfCookieFilter`、(3) `CsrfTokenRequestAttributeHandler`の明示指定、の3点セットが必要。公式ドキュメントを読まずに(1)だけで進めると一見動きそうで実際は動かない
- Lombokの`@Getter`が生成する`isXxx()`メソッドとJacksonの命名規則(`is`プレフィックスを剥がしてプロパティ名にする)の組み合わせは、フィールド名が`isXxx`の形になっていると剥がした結果と元のフィールド名が一致せず、プロパティが重複登録される。DTOをThymeleafモデルからJSON APIに変換する際に初めて顕在化する典型的な落とし穴
- `mvn spring-boot:run`を直接実行しても、`spring-boot:run`ゴールが内部的にコンパイルフェーズまでのライフサイクルをフォークするため、`generate-resources`フェーズ(=frontend-maven-pluginによるフロントエンドビルド)は自動的に実行される。事前に`mvn generate-resources`を手動実行する必要はない(実機検証で確認)
- IDEのLombokアノテーションプロセッサが壊れている環境では、IDE診断を信用せず`mvn compile`/`mvn test`を都度実行してground truthとする判断が有効だった

**プロセス上の改善点**:
- requirements.md/design.md/tasklist.mdの3点セットを1ファイルずつ承認を得てから作成したことで、大規模な作業の前に方向性のズレ(フレームワーク選定・移行方針・認証方式)を早期に確定できた
- 実装前にdocsとコードベースの実態を照合したことで、存在しない機能(月別グラフ等)を移行対象に含めてしまうミスを未然に防げた
- テストコードだけでは検出できないバグ(SPAフォールバックのパターン構文エラー、CSRF Cookie未発行、CSRF検証失敗、JSON重複キー)が複数あり、実際にアプリを起動してcurlでAPIを叩くE2E検証が不可欠だった。バックエンドのユニット/WebMvcTestは全てグリーンだったが、それだけでは実運用上のバグを見逃していた

### 次回への改善提案
- Spring Security・CSRF・Jackson×Lombokのような「フレームワーク間の組み合わせで初めて顕在化する」問題は、設計段階で疑わしい箇所を洗い出し、実装直後に軽くAPIを叩いて検証するステップをtasklist.mdに明示的に組み込むと手戻りが減る
- ドキュメント(docs/)とコードベースの実態が乖離しているケースが今回複数見つかった。定期的にdocsとコードの整合性を確認する棚卸しタスクがあると良い
- 今回はバックエンドAPI化とフロントエンド実装をおおむね順番通り進めたが、実際にはSpaWebConfig/CSRF関連の修正のように「フロントエンドが動き始めて初めて発覚するバックエンドの不備」があったため、フロントエンドの主要導線(ログイン・本棚・記事作成)が動く早い段階で一度E2E疎通確認を挟むと、後工程での手戻りをさらに減らせる
