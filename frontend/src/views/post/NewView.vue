<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as booksApi from '../../api/books'
import * as postsApi from '../../api/posts'
import MarkdownEditor from '../../components/MarkdownEditor.vue'
import type { ApiError, Book } from '../../types'

const route = useRoute()
const router = useRouter()

const bookId = Number(route.query.bookId)
const book = ref<Book | null>(null)
const form = ref({ title: '', body: '', isPublic: false })
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

onMounted(async () => {
  book.value = await booksApi.getBook(bookId)
})

function errorFor(field: string) {
  return fieldErrors.value[field]
}

async function handleSubmit() {
  fieldErrors.value = {}
  submitting.value = true
  try {
    const post = await postsApi.createPost(bookId, form.value)
    router.push(`/posts/${post.id}`)
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
      <div class="col-md-8">
        <h2 class="fw-bold mb-4">記事を書く</h2>

        <div v-if="book" class="card mb-4">
          <div class="card-body">
            <div class="d-flex gap-3 align-items-center">
              <img
                v-if="book.coverUrl"
                :src="book.coverUrl"
                alt="書影"
                class="img-thumbnail"
                style="width: 60px; height: auto"
              />
              <div
                v-else
                class="bg-light d-flex align-items-center justify-content-center flex-shrink-0"
                style="width: 60px; height: 85px"
              >
                <span class="text-muted" style="font-size: 0.65rem">No Cover</span>
              </div>
              <div>
                <div class="fw-bold">{{ book.title }}</div>
                <div class="text-muted small">{{ book.author }}</div>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="title" class="form-label fw-semibold">タイトル <span class="text-danger">*</span></label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errorFor('title') }"
              maxlength="255"
              placeholder="記事のタイトルを入力してください"
            />
            <div class="invalid-feedback">{{ errorFor('title') }}</div>
          </div>

          <div class="mb-3">
            <label for="body" class="form-label fw-semibold">本文 <span class="text-danger">*</span></label>
            <MarkdownEditor
              id="body"
              v-model="form.body"
              :rows="12"
              placeholder="読書の感想や考えを書いてください"
              :error="errorFor('body')"
            />
          </div>

          <div class="mb-4">
            <div class="form-check">
              <input id="isPublic" v-model="form.isPublic" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="isPublic">公開する</label>
            </div>
            <div class="form-text">チェックを外すと非公開（草稿）として保存されます</div>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-dark btn-lg" :disabled="submitting">投稿する</button>
            <router-link to="/shelf" class="btn btn-outline-secondary btn-lg">キャンセル</router-link>
          </div>
        </form>
      </div>
    </div>
  </main>
</template>
