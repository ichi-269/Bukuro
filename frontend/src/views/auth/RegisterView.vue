<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as authApi from '../../api/auth'
import type { ApiError } from '../../types'

const form = ref({ email: '', username: '', password: '', passwordConfirm: '' })
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

const router = useRouter()

function errorFor(field: string) {
  return fieldErrors.value[field]
}

async function handleSubmit() {
  fieldErrors.value = {}
  submitting.value = true
  try {
    await authApi.register(form.value)
    router.push({ path: '/login', query: { registered: 'true' } })
  } catch (err) {
    const apiError = err as ApiError
    if (apiError.fieldErrors) {
      fieldErrors.value = Object.fromEntries(apiError.fieldErrors.map((f) => [f.field, f.message]))
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-5">
        <h2 class="mb-4 fw-bold">新規登録</h2>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="email" class="form-label">メールアドレス</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              class="form-control"
              :class="{ 'is-invalid': errorFor('email') }"
              placeholder="example@mail.com"
            />
            <div class="invalid-feedback">{{ errorFor('email') }}</div>
          </div>

          <div class="mb-3">
            <label for="username" class="form-label">ユーザー名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errorFor('username') }"
              placeholder="3〜50文字"
            />
            <div class="invalid-feedback">{{ errorFor('username') }}</div>
          </div>

          <div class="mb-3">
            <label for="password" class="form-label">パスワード</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              class="form-control"
              :class="{ 'is-invalid': errorFor('password') }"
              placeholder="8文字以上"
            />
            <div class="invalid-feedback">{{ errorFor('password') }}</div>
          </div>

          <div class="mb-4">
            <label for="passwordConfirm" class="form-label">パスワード（確認）</label>
            <input
              id="passwordConfirm"
              v-model="form.passwordConfirm"
              type="password"
              class="form-control"
              :class="{ 'is-invalid': errorFor('passwordConfirm') }"
            />
            <div class="invalid-feedback">{{ errorFor('passwordConfirm') }}</div>
          </div>

          <button type="submit" class="btn btn-dark w-100" :disabled="submitting">登録する</button>
        </form>

        <p class="mt-3 text-center text-muted">
          すでにアカウントをお持ちの方は
          <router-link to="/login">こちら</router-link>
        </p>
      </div>
    </div>
  </main>
</template>
