import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import RegisterView from './RegisterView.vue'
import * as authApi from '../../api/auth'

vi.mock('../../api/auth')

function buildRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/login', component: { template: '<div />' } },
      { path: '/register', component: RegisterView },
    ],
  })
}

describe('RegisterView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders field-level validation errors returned by the API', async () => {
    vi.mocked(authApi.register).mockRejectedValue({
      status: 400,
      code: 'VALIDATION_ERROR',
      message: '入力内容に誤りがあります',
      fieldErrors: [
        { field: 'email', message: 'このメールアドレスはすでに登録されています' },
        { field: 'username', message: 'このユーザー名はすでに使用されています' },
      ],
    })

    const router = buildRouter()
    router.push('/register')
    await router.isReady()

    const wrapper = mount(RegisterView, { global: { plugins: [router] } })

    await wrapper.find('form').trigger('submit.prevent')
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('このメールアドレスはすでに登録されています')
    expect(wrapper.text()).toContain('このユーザー名はすでに使用されています')
  })

  it('navigates to the login page with a success flag on successful registration', async () => {
    vi.mocked(authApi.register).mockResolvedValue({
      id: 1,
      username: 'newuser',
      email: 'new@example.com',
      bio: null,
      createdAt: '2026-01-01T00:00:00',
    })

    const router = buildRouter()
    router.push('/register')
    await router.isReady()
    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = mount(RegisterView, { global: { plugins: [router] } })

    await wrapper.find('form').trigger('submit.prevent')
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(pushSpy).toHaveBeenCalledWith({ path: '/login', query: { registered: 'true' } })
  })
})
