<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as postsApi from '../../api/posts'
import MarkdownEditor from '../../components/MarkdownEditor.vue'
import type { ApiError, Post } from '../../types'

const route = useRoute()
const router = useRouter()
const postId = Number(route.params.id)

const post = ref<Post | null>(null)
const form = ref({ title: '', body: '', isPublic: false })
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

onMounted(async () => {
  const fetched = await postsApi.getPost(postId)
  if (!fetched.isOwner) {
    router.replace(`/posts/${postId}`)
    return
  }
  post.value = fetched
  form.value = { title: fetched.title, body: fetched.body, isPublic: fetched.isPublic }
})

function errorFor(field: string) {
  return fieldErrors.value[field]
}

async function handleSubmit() {
  fieldErrors.value = {}
  submitting.value = true
  try {
    await postsApi.updatePost(postId, form.value)
    router.push(`/posts/${postId}`)
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
        <h2 class="fw-bold mb-4">記事を編集</h2>

        <div v-if="post" class="card mb-4">
          <div class="card-body">
            <div class="d-flex gap-3 align-items-center">
              <img
                v-if="post.book.coverUrl"
                :src="post.book.coverUrl"
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
                <div class="fw-bold">{{ post.book.title }}</div>
                <div class="text-muted small">{{ post.book.author }}</div>
              </div>
            </div>
          </div>
        </div>

        <form v-if="post" @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="title" class="form-label fw-semibold">タイトル <span class="text-danger">*</span></label>
            <input
              id="title"
              v-model="form.title"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errorFor('title') }"
              maxlength="255"
            />
            <div class="invalid-feedback">{{ errorFor('title') }}</div>
          </div>

          <div class="mb-3">
            <label for="body" class="form-label fw-semibold">本文 <span class="text-danger">*</span></label>
            <MarkdownEditor id="body" v-model="form.body" :rows="12" :error="errorFor('body')" />
          </div>

          <div class="mb-4">
            <div class="form-check">
              <input id="isPublic" v-model="form.isPublic" class="form-check-input" type="checkbox" />
              <label class="form-check-label" for="isPublic">公開する</label>
            </div>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-dark btn-lg" :disabled="submitting">更新する</button>
            <router-link :to="`/posts/${postId}`" class="btn btn-outline-secondary btn-lg">キャンセル</router-link>
          </div>
        </form>
      </div>
    </div>
  </main>
</template>
