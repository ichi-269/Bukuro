import client from './client'
import type { Post } from '../types'

export interface PostFormPayload {
  title: string
  body: string
  isPublic: boolean
}

export function createPost(bookId: number, form: PostFormPayload) {
  return client.post<Post>('/posts', form, { params: { bookId } }).then((res) => res.data)
}

export function getPost(postId: number) {
  return client.get<Post>(`/posts/${postId}`).then((res) => res.data)
}

export function updatePost(postId: number, form: PostFormPayload) {
  return client.put<Post>(`/posts/${postId}`, form).then((res) => res.data)
}

export function deletePost(postId: number) {
  return client.delete(`/posts/${postId}`)
}
