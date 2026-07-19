import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '../api/client'
import type { Me } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<Me | null>(null)
  const initialized = ref(false)

  async function fetchMe() {
    const response = await client.get<Me | ''>('/me')
    user.value = response.data === '' ? null : (response.data as Me)
    initialized.value = true
  }

  async function login(email: string, password: string) {
    const params = new URLSearchParams()
    params.set('username', email)
    params.set('password', password)
    await client.post('/login', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })
    await fetchMe()
  }

  async function logout() {
    await client.post('/logout')
    clear()
  }

  function clear() {
    user.value = null
  }

  return { user, initialized, fetchMe, login, logout, clear }
})
