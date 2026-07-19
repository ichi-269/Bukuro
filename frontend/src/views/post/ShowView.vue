<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as postsApi from '../../api/posts'
import * as goodApi from '../../api/good'
import { useAuthStore } from '../../stores/auth'
import type { ApiError, Post } from '../../types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const postId = Number(route.params.id)

const post = ref<Post | null>(null)
const warningMessage = ref<string | null>(null)

async function load() {
  post.value = await postsApi.getPost(postId)
}

onMounted(load)

async function toggleGood() {
  if (!post.value) return
  try {
    if (post.value.hasGooded) {
      await goodApi.removeGood(postId)
    } else {
      await goodApi.addGood(postId)
    }
    await load()
  } catch (err) {
    warningMessage.value = (err as ApiError).message ?? '操作に失敗しました'
  }
}

async function handleDelete() {
  if (!confirm('この記事を削除しますか？')) return
  await postsApi.deletePost(postId)
  router.push('/shelf')
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('ja-JP', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <main v-if="post" class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card mb-4">
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

        <div class="mb-3 d-flex justify-content-between align-items-start">
          <div>
            <h1 class="fw-bold mb-2">{{ post.title }}</h1>
            <div class="text-muted small">
              <span>{{ post.user.username }}</span> ·
              <span>{{ formatDate(post.createdAt) }}</span>
              <span v-if="post.updatedAt !== post.createdAt">（{{ formatDate(post.updatedAt) }}更新）</span>
            </div>
          </div>
          <span v-if="post.isPublic" class="badge bg-dark">公開</span>
          <span v-else class="badge bg-secondary">非公開</span>
        </div>

        <div class="card mb-4">
          <div class="card-body">
            <p class="mb-0" style="white-space: pre-line">{{ post.body }}</p>
          </div>
        </div>

        <div class="mb-4 d-flex align-items-center gap-3">
          <div v-if="!authStore.user" class="text-muted small">👍 {{ post.goodCount }}</div>
          <div v-else>
            <button type="button" class="btn btn-sm" :class="post.hasGooded ? 'btn-dark' : 'btn-outline-dark'" @click="toggleGood">
              👍 {{ post.hasGooded ? 'グッド済み' : 'グッドする' }}
            </button>
            <span class="text-muted small ms-1">{{ post.goodCount }}</span>
          </div>
        </div>

        <div v-if="warningMessage" class="alert alert-warning py-2 mb-3">{{ warningMessage }}</div>

        <div v-if="post.isOwner" class="d-flex gap-2 mb-4">
          <router-link :to="`/posts/${post.id}/edit`" class="btn btn-outline-secondary">編集する</router-link>
          <button type="button" class="btn btn-outline-danger" @click="handleDelete">削除する</button>
        </div>

        <router-link to="/shelf" class="btn btn-link ps-0">← 本棚に戻る</router-link>
      </div>
    </div>
  </main>
</template>
