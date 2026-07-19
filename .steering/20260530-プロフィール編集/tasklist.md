# タスクリスト: プロフィール編集

## 申し送り事項

- **実装完了日**: 2026-05-30
- **計画と実績の差分**:
  - エンドポイントパスが設計時に `/profile/edit` と決定されたが、`functional-design.md` に古い `/mypage/edit` が残存していた。implementation-validator の指摘で発覚し、`functional-design.md` を `/profile/edit` に更新した
  - `ProfileEditForm` の `@Pattern` バリデーションが `RegisterForm` に存在しなかった。日本語ユーザー名で登録済みユーザーが編集不能になるリスクがあり、`RegisterForm` にも同じ `@Pattern` を追加して解消した
  - POST ハンドラーのバリデーションエラー時に `model.addAttribute("profileEditForm", form)` が暗黙動作頼みだったため、明示的追加に修正した
- **学んだこと**:
  - DTO のバリデーションルールは登録フォームと編集フォームで必ず揃えること。片方だけに制約を追加すると既存ユーザーが詰まる
  - ステアリング design.md と永続ドキュメント functional-design.md の整合性確認は実装前に行うべき
- **次回への改善提案**:
  - `UserService.register` の重複エラーパステストが不足。次回 UserService に触れる際に追加を検討する
  - `IllegalStateException` より `DuplicateRecordException` 等のカスタム例外を使う統一が望ましい（今回はスコープ外として保留）



- [x] ProfileEditForm: username・bio の DTO を作成する
- [x] UserService: updateProfile() メソッドを追加する
- [x] UserController: GET/POST /profile/edit エンドポイントを追加する
- [x] user/profile-edit.html: 編集フォームテンプレートを作成する
- [x] user/show.html: 自分のページに「プロフィールを編集」ボタンを追加する
- [x] UserServiceTest: updateProfile のテストを追加する
- [x] UserControllerTest: /profile/edit エンドポイントのテストを追加する
