# 設計: ユーザー認証

## ディレクトリ構造

```
src/main/java/com/bukuro/
├── BukuroApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── HomeController.java
│   └── AuthController.java
├── dto/
│   └── RegisterForm.java
├── entity/
│   └── User.java
├── repository/
│   └── UserRepository.java
└── service/
    ├── UserService.java
    └── CustomUserDetailsService.java

src/main/resources/
├── templates/
│   ├── layout/
│   │   └── base.html
│   ├── home/
│   │   └── index.html
│   └── auth/
│       ├── register.html
│       └── login.html
├── static/
│   └── css/
│       └── main.css
├── application.properties
└── application-local.properties.example

src/test/java/com/bukuro/
├── service/
│   └── UserServiceTest.java
└── controller/
    └── AuthControllerTest.java
```

## 主要クラス設計

### User エンティティ
- id (BIGINT PK AUTO_INCREMENT)
- username (VARCHAR(50) NOT NULL UNIQUE)
- email (VARCHAR(255) NOT NULL UNIQUE)
- password (VARCHAR(255) NOT NULL) ← BCryptハッシュ
- bio (TEXT NULL)
- created_at (TIMESTAMP NOT NULL)

### RegisterForm DTO
- email: @NotBlank @Email
- username: @NotBlank @Size(min=3, max=50)
- password: @NotBlank @Size(min=8)
- passwordConfirm: @NotBlank（password一致チェック）

### SecurityConfig
- permitAll: GET /, /login, /register, /posts/**, /users/**, /books/**（書籍詳細）
- authenticated: その他
- loginPage: /login
- defaultSuccessUrl: /
- CSRF: 有効

### CustomUserDetailsService
- UserDetailsService実装
- loadUserByUsername(email) でUserを検索
- UserDetailsをimplementsした内部クラスまたはwrapperで返す

## DDL（application-local.properties.exampleで参照）

```sql
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## バリデーション方針

- メールアドレス重複チェック: UserServiceで行い、BindingResultにフィールドエラーとして追加
- パスワード確認一致チェック: RegisterFormの@AssertTrueまたはControllerで行う
- エラー発生時はフォームを再表示（リダイレクトしない）
