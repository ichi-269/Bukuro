import axios from 'axios'
import router from '../router'
import { useAuthStore } from '../stores/auth'
import type { ApiError } from '../types'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status as number | undefined
    const apiError = error.response?.data as ApiError | undefined

    if (status === 401) {
      const authStore = useAuthStore()
      authStore.clear()
      if (router.currentRoute.value.meta.requiresAuth) {
        router.push({ path: '/login' })
      }
    }

    return Promise.reject(apiError ?? error)
  },
)

export default client
