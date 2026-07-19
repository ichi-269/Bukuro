<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as usersApi from '../../api/users'
import * as followApi from '../../api/follow'
import { useAuthStore } from '../../stores/auth'
import type { UserProfile } from '../../types'

const route = useRoute()
const authStore = useAuthStore()
const profile = ref<UserProfile | null>(null)

async function load() {
  profile.value = await usersApi.getUserProfile(route.params.username as string)
}

onMounted(load)
watch(() => route.params.username, load)

async function toggleFollow() {
  if (!profile.value) return
  const username = profile.value.profileUser.username
  if (profile.value.isFollowing) {
    await followApi.unfollow(username)
    profile.value.isFollowing = false
    profile.value.followerCount -= 1
  } else {
    await followApi.follow(username)
    profile.value.isFollowing = true
    profile.value.followerCount += 1
  }
}

function formatMonth(value: string) {
  const date = new Date(value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
}
</script>

<template>
  <main v-if="profile" class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card mb-5">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h2 class="fw-bold mb-1">{{ profile.profileUser.username }}</h2>
                <p class="text-muted small mb-2">登録: {{ formatMonth(profile.profileUser.createdAt) }}</p>
                <p v-if="profile.profileUser.bio" class="mb-0">{{ profile.profileUser.bio }}</p>
                <div class="mt-2 d-flex gap-3">
                  <router-link
                    :to="`/users/${profile.profileUser.username}/followers`"
                    class="text-decoration-none text-muted small"
                  >
                    フォロワー <strong>{{ profile.followerCount }}</strong>
                  </router-link>
                  <router-link
                    :to="`/users/${profile.profileUser.username}/following`"
                    class="text-decoration-none text-muted small"
                  >
                    フォロー中 <strong>{{ profile.followingCount }}</strong>
                  </router-link>
                </div>
              </div>
              <div class="text-end">
                <div class="fw-bold fs-4">{{ profile.postCount }}</div>
                <div class="text-muted small">公開記事</div>
              </div>
            </div>
            <div class="mt-3 d-flex gap-2">
              <div v-if="profile.isOwnPage" class="d-flex gap-2">
                <router-link to="/shelf" class="btn btn-outline-dark btn-sm">本棚を見る</router-link>
                <router-link to="/profile/edit" class="btn btn-outline-dark btn-sm">プロフィールを編集</router-link>
              </div>
              <div v-else-if="authStore.user">
                <button
                  type="button"
                  class="btn btn-sm"
                  :class="profile.isFollowing ? 'btn-outline-secondary' : 'btn-dark'"
                  @click="toggleFollow"
                >
                  {{ profile.isFollowing ? 'フォロー中' : 'フォローする' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <h4 class="fw-bold mb-3">公開記事</h4>

        <div v-if="profile.posts.length === 0" class="text-center text-muted py-5">
          <p>まだ公開記事はありません。</p>
        </div>

        <div v-for="post in profile.posts" :key="post.id" class="card mb-3">
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
                <p class="text-muted small mb-0">{{ post.book.title }}</p>
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
  </main>
</template>
