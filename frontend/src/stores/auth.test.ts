import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'
import client from '../api/client'

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

const mockedClient = vi.mocked(client, true)

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchMe sets user when logged in', async () => {
    mockedClient.get.mockResolvedValue({ data: { id: 1, username: 'alice', email: 'a@example.com', bio: null, createdAt: '2026-01-01T00:00:00' } })

    const store = useAuthStore()
    await store.fetchMe()

    expect(store.user?.username).toBe('alice')
    expect(store.initialized).toBe(true)
  })

  it('fetchMe sets user to null when the response body is empty (anonymous)', async () => {
    mockedClient.get.mockResolvedValue({ data: '' })

    const store = useAuthStore()
    await store.fetchMe()

    expect(store.user).toBeNull()
    expect(store.initialized).toBe(true)
  })

  it('login posts form-encoded credentials then refreshes the user', async () => {
    mockedClient.post.mockResolvedValue({ data: { status: 'ok' } })
    mockedClient.get.mockResolvedValue({ data: { id: 2, username: 'bob', email: 'b@example.com', bio: null, createdAt: '2026-01-01T00:00:00' } })

    const store = useAuthStore()
    await store.login('b@example.com', 'password123')

    expect(mockedClient.post).toHaveBeenCalledWith(
      '/login',
      expect.any(URLSearchParams),
      expect.objectContaining({ headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }),
    )
    expect(store.user?.username).toBe('bob')
  })

  it('logout clears the current user', async () => {
    mockedClient.post.mockResolvedValue({ data: { status: 'ok' } })

    const store = useAuthStore()
    store.user = { id: 1, username: 'alice', email: 'a@example.com', bio: null, createdAt: '2026-01-01T00:00:00' }

    await store.logout()

    expect(store.user).toBeNull()
  })

  it('clear resets the user without calling the API', () => {
    const store = useAuthStore()
    store.user = { id: 1, username: 'alice', email: 'a@example.com', bio: null, createdAt: '2026-01-01T00:00:00' }

    store.clear()

    expect(store.user).toBeNull()
  })
})
