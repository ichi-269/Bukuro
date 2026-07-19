<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/')
}
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-light bg-white border-bottom">
    <div class="container">
      <router-link class="navbar-brand fw-bold" to="/">Bukuro</router-link>
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNav"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          <template v-if="authStore.user">
            <li class="nav-item">
              <router-link class="nav-link" to="/shelf">本棚</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" :to="`/users/${authStore.user.username}`">マイページ</router-link>
            </li>
            <li class="nav-item">
              <button type="button" class="btn btn-link nav-link" @click="handleLogout">ログアウト</button>
            </li>
          </template>
          <template v-else>
            <li class="nav-item">
              <router-link class="nav-link" to="/login">ログイン</router-link>
            </li>
            <li class="nav-item">
              <router-link class="btn btn-dark btn-sm ms-2" to="/register">新規登録</router-link>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>
