<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as usersApi from '../../api/users'
import { useAuthStore } from '../../stores/auth'
import type { ApiError } from '../../types'

const authStore = useAuthStore()
const router = useRouter()

const form = ref({ username: '', bio: '' })
const fieldErrors = ref<Record<string, string>>({})
const errorMessage = ref<string | null>(null)
const submitting = ref(false)

onMounted(() => {
  if (authStore.user) {
    form.value = { username: authStore.user.username, bio: authStore.user.bio ?? '' }
  }
})

function errorFor(field: string) {
  return fieldErrors.value[field]
}

async function handleSubmit() {
  fieldErrors.value = {}
  errorMessage.value = null
  submitting.value = true
  try {
    const updated = await usersApi.updateProfile(form.value)
    if (authStore.user) {
      authStore.user.username = updated.username
      authStore.user.bio = updated.bio
    }
    router.push(`/users/${updated.username}`)
  } catch (err) {
    const apiError = err as ApiError
    if (apiError.fieldErrors) {
      fieldErrors.value = Object.fromEntries(apiError.fieldErrors.map((f) => [f.field, f.message]))
    } else {
      errorMessage.value = apiError.message ?? '更新に失敗しました'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <h2 class="fw-bold mb-4">プロフィール編集</h2>

        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="username" class="form-label fw-bold">ユーザー名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errorFor('username') }"
            />
            <div v-if="errorFor('username')" class="invalid-feedback">{{ errorFor('username') }}</div>
          </div>

          <div class="mb-3">
            <label for="bio" class="form-label fw-bold">自己紹介</label>
            <textarea
              id="bio"
              v-model="form.bio"
              class="form-control"
              :class="{ 'is-invalid': errorFor('bio') }"
              rows="4"
              placeholder="自己紹介を入力してください（任意）"
            ></textarea>
            <div v-if="errorFor('bio')" class="invalid-feedback">{{ errorFor('bio') }}</div>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-dark" :disabled="submitting">保存する</button>
            <router-link to="/mypage" class="btn btn-outline-secondary">キャンセル</router-link>
          </div>
        </form>
      </div>
    </div>
  </main>
</template>
