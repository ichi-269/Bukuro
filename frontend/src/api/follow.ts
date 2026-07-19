import client from './client'

export function follow(username: string) {
  return client.post(`/users/${username}/follow`)
}

export function unfollow(username: string) {
  return client.post(`/users/${username}/unfollow`)
}
