import client from './client'
import type { Me, User, UserProfile } from '../types'

export function getUserProfile(username: string) {
  return client.get<UserProfile>(`/users/${username}`).then((res) => res.data)
}

export function updateProfile(payload: { username: string; bio: string }) {
  return client.put<Me>('/profile/edit', payload).then((res) => res.data)
}

export function getFollowers(username: string) {
  return client.get<User[]>(`/users/${username}/followers`).then((res) => res.data)
}

export function getFollowing(username: string) {
  return client.get<User[]>(`/users/${username}/following`).then((res) => res.data)
}
