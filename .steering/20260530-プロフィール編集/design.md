# 設計: プロフィール編集

## 変更ファイル一覧

| ファイル | 変更種別 | 内容 |
|---------|---------|------|
| `dto/ProfileEditForm.java` | 新規 | username・bio のバリデーション付き DTO |
| `service/UserService.java` | 追加 | updateProfile() メソッド |
| `controller/UserController.java` | 追加 | GET/POST /profile/edit エンドポイント |
| `templates/user/profile-edit.html` | 新規 | 編集フォームテンプレート |
| `templates/user/show.html` | 変更 | 自分のページに「プロフィールを編集」ボタン追加 |
| `test/.../UserServiceTest.java` | 追加 | updateProfile テスト |
| `test/.../UserControllerTest.java` | 追加 | /profile/edit エンドポイントテスト |

## ProfileEditForm

```java
@Getter @Setter
public class ProfileEditForm {
    @NotBlank @Size(min=3, max=50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "ユーザー名は英数字・アンダースコアのみ使用できます")
    private String username;

    @Size(max=500, message = "自己紹介は500文字以内で入力してください")
    private String bio;
}
```

## UserService.updateProfile()

```java
@Transactional
public User updateProfile(Long userId, ProfileEditForm form) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("..."));

    // 他ユーザーが使用中のユーザー名は不可
    userRepository.findByUsername(form.getUsername())
            .ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new IllegalStateException("このユーザー名はすでに使用されています: " + form.getUsername());
                }
            });

    user.setUsername(form.getUsername());
    user.setBio(form.getBio() != null && form.getBio().isBlank() ? null : form.getBio());
    return userRepository.save(user);
}
```

## UserController 追加エンドポイント

```java
@GetMapping("/profile/edit")
public String editProfileForm(@AuthenticationPrincipal UserDetails principal, Model model) {
    User user = userService.getUserByEmail(principal.getUsername());
    ProfileEditForm form = new ProfileEditForm();
    form.setUsername(user.getUsername());
    form.setBio(user.getBio());
    model.addAttribute("profileEditForm", form);
    return "user/profile-edit";
}

@PostMapping("/profile/edit")
public String editProfile(@AuthenticationPrincipal UserDetails principal,
                          @Valid @ModelAttribute ProfileEditForm form,
                          BindingResult bindingResult,
                          Model model) {
    if (bindingResult.hasErrors()) {
        return "user/profile-edit";
    }
    User user = userService.getUserByEmail(principal.getUsername());
    try {
        User updated = userService.updateProfile(user.getId(), form);
        return "redirect:/users/" + updated.getUsername();
    } catch (IllegalStateException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "user/profile-edit";
    }
}
```

## セキュリティ

`/profile/edit` は `SecurityConfig` の `.anyRequest().authenticated()` に該当するため、追加設定不要。

## user/show.html の変更

```html
<!-- 変更前 -->
<div th:if="${isOwnPage}">
    <a th:href="@{/shelf}" class="btn btn-outline-dark btn-sm">本棚を見る</a>
</div>

<!-- 変更後 -->
<div th:if="${isOwnPage}" class="d-flex gap-2">
    <a th:href="@{/shelf}" class="btn btn-outline-dark btn-sm">本棚を見る</a>
    <a th:href="@{/profile/edit}" class="btn btn-outline-dark btn-sm">プロフィールを編集</a>
</div>
```
