import client from './client'

export function addGood(postId: number) {
  return client.post(`/posts/${postId}/good`)
}

export function removeGood(postId: number) {
  return client.post(`/posts/${postId}/ungood`)
}
