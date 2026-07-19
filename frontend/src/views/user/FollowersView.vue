<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as usersApi from '../../api/users'
import type { User } from '../../types'

const route = useRoute()
const username = ref(route.params.username as string)
const users = ref<User[]>([])

async function load() {
  username.value = route.params.username as string
  users.value = await usersApi.getFollowers(username.value)
}

onMounted(load)
watch(() => route.params.username, load)
</script>

<template>
  <main class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="mb-4">
          <router-link :to="`/users/${username}`" class="text-decoration-none text-muted small">
            ← {{ username }} のページへ
          </router-link>
          <h2 class="fw-bold mt-2 mb-0">フォロワー</h2>
          <p class="text-muted small mb-0">{{ username }} さんをフォローしているユーザー</p>
        </div>

        <div v-if="users.length === 0" class="text-center text-muted py-5">
          <p>まだフォロワーがいません。</p>
        </div>

        <div v-else>
          <div v-for="user in users" :key="user.id" class="d-flex align-items-center py-3 border-bottom">
            <router-link :to="`/users/${user.username}`" class="text-decoration-none text-dark fw-semibold">
              {{ user.username }}
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
