<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import * as homeApi from '../../api/home'
import type { HomeFeed } from '../../types'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const feed = ref<HomeFeed | null>(null)

async function loadFeed() {
  if (!authStore.user) return
  const feedParam = route.query.feed === 'recommended' ? 'recommended' : undefined
  feed.value = await homeApi.getFeed(feedParam)
}

onMounted(loadFeed)
watch(() => route.query.feed, loadFeed)

function switchTab(type: 'following' | 'recommended') {
  router.push({ path: '/', query: type === 'recommended' ? { feed: 'recommended' } : {} })
}
</script>

<template>
  <section v-if="!authStore.user" class="hero-section">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5 text-center">
          <p class="hero-eyebrow">Reading Journal</p>
          <h1 class="hero-title">Bukuro</h1>
          <p class="hero-body">日々の読書の記録</p>
          <div class="hero-cta">
            <router-link to="/register" class="btn btn-dark hero-btn-primary">無料で始める</router-link>
            <router-link to="/login" class="hero-link-secondary">ログイン</router-link>
          </div>
        </div>
      </div>
    </div>
  </section>

  <div v-else class="container mt-5">
    <div class="row justify-content-center mb-4">
      <div class="col-md-8 text-center">
        <router-link to="/books/search" class="btn btn-dark me-2">本を登録する</router-link>
        <router-link to="/shelf" class="btn btn-outline-secondary">本棚を見る</router-link>
      </div>
    </div>

    <div v-if="feed" class="row justify-content-center">
      <div class="col-md-8">
        <div v-if="feed.hasFollowees">
          <ul class="nav nav-tabs mb-3">
            <li class="nav-item">
              <button
                type="button"
                class="nav-link"
                :class="{ active: feed.feedType === 'following' }"
                @click="switchTab('following')"
              >
                フォロー中
              </button>
            </li>
            <li class="nav-item">
              <button
                type="button"
                class="nav-link"
                :class="{ active: feed.feedType === 'recommended' }"
                @click="switchTab('recommended')"
              >
                おすすめ
              </button>
            </li>
          </ul>
        </div>
        <div v-else>
          <h4 class="fw-bold mb-3">おすすめ記事</h4>
        </div>

        <div v-if="feed.posts.length === 0" class="text-center text-muted py-5">
          <div v-if="feed.feedType === 'following'">
            <p>フォロー中のユーザーがまだ記事を書いていません。</p>
            <p class="small">
              <router-link to="/books/search">本を登録して記事を書く</router-link>か、
              気になるユーザーをフォローしてみましょう。
            </p>
          </div>
          <p v-if="feed.feedType === 'recommended'">
            まだ公開記事がありません。<br />
            <router-link to="/books/search">最初の1冊を登録して記事を書いてみましょう。</router-link>
          </p>
        </div>

        <div v-for="post in feed.posts" :key="post.id" class="card mb-3">
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-auto">
                <img
                  v-if="post.book.coverUrl"
                  :src="post.book.coverUrl"
                  alt="書影"
                  class="img-thumbnail"
                  style="width: 50px; height: auto"
                />
                <div
                  v-else
                  class="bg-light d-flex align-items-center justify-content-center"
                  style="width: 50px; height: 70px"
                >
                  <span class="text-muted" style="font-size: 0.6rem">No Cover</span>
                </div>
              </div>
              <div class="col">
                <router-link :to="`/posts/${post.id}`" class="text-decoration-none text-dark">
                  <h6 class="fw-bold mb-1">{{ post.title }}</h6>
                </router-link>
                <p class="text-muted small mb-0">
                  <router-link :to="`/users/${post.user.username}`" class="text-decoration-none text-muted">
                    {{ post.user.username }}
                  </router-link>
                  ・<span>{{ post.book.title }}</span>
                </p>
              </div>
              <div class="col-auto text-end">
                <div class="text-muted small">👍 {{ post.goodCount }}</div>
                <div class="text-muted small">{{ new Date(post.createdAt).toLocaleDateString('ja-JP') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
