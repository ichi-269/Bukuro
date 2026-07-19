import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/home/IndexView.vue') },
    { path: '/login', name: 'login', component: () => import('../views/auth/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('../views/auth/RegisterView.vue') },
    {
      path: '/mypage',
      redirect: () => {
        const authStore = useAuthStore()
        return authStore.user ? `/users/${authStore.user.username}` : '/login'
      },
    },
    {
      path: '/books/search',
      name: 'books-search',
      component: () => import('../views/book/SearchView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/shelf',
      name: 'shelf',
      component: () => import('../views/shelf/IndexView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/posts/new',
      name: 'posts-new',
      component: () => import('../views/post/NewView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/posts/:id/edit',
      name: 'posts-edit',
      component: () => import('../views/post/EditView.vue'),
      meta: { requiresAuth: true },
    },
    { path: '/posts/:id', name: 'posts-show', component: () => import('../views/post/ShowView.vue') },
    {
      path: '/profile/edit',
      name: 'profile-edit',
      component: () => import('../views/user/ProfileEditView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users/:username/followers',
      name: 'users-followers',
      component: () => import('../views/user/FollowersView.vue'),
    },
    {
      path: '/users/:username/following',
      name: 'users-following',
      component: () => import('../views/user/FollowingView.vue'),
    },
    { path: '/users/:username', name: 'users-show', component: () => import('../views/user/ShowView.vue') },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../components/NotFound.vue') },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (!authStore.initialized) {
    await authStore.fetchMe()
  }
  if (to.meta.requiresAuth && !authStore.user) {
    return { path: '/login' }
  }
  return true
})

export default router
