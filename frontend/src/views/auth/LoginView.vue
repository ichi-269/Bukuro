<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const email = ref('')
const password = ref('')
const errorMessage = ref<string | null>(null)
const submitting = ref(false)

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const justRegistered = route.query.registered === 'true'

async function handleSubmit() {
  errorMessage.value = null
  submitting.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/')
  } catch {
    errorMessage.value = 'メールアドレスまたはパスワードが正しくありません。'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-5">
        <h2 class="mb-4 fw-bold">ログイン</h2>

        <div v-if="justRegistered" class="alert alert-success" role="alert">
          登録が完了しました。ログインしてください。
        </div>
        <div v-if="errorMessage" class="alert alert-danger" role="alert">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="username" class="form-label">メールアドレス</label>
            <input
              id="username"
              v-model="email"
              type="email"
              class="form-control"
              placeholder="example@mail.com"
              required
            />
          </div>

          <div class="mb-4">
            <label for="password" class="form-label">パスワード</label>
            <input id="password" v-model="password" type="password" class="form-control" required />
          </div>

          <button type="submit" class="btn btn-dark w-100" :disabled="submitting">ログイン</button>
        </form>

        <p class="mt-3 text-center text-muted">
          アカウントをお持ちでない方は
          <router-link to="/register">新規登録</router-link>
        </p>
      </div>
    </div>
  </main>
</template>
