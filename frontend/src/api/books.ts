import client from './client'
import type { Book } from '../types'

export function searchByIsbn(isbn: string) {
  return client.post<Book>('/books/search', { isbn }).then((res) => res.data)
}

export function searchByTitle(keyword: string) {
  return client.post<Book[]>('/books/search/title', { keyword }).then((res) => res.data)
}

export function confirmFromTitle(payload: { isbn: string; title?: string; author?: string; publisher?: string }) {
  return client.post<Book>('/books/search/confirm', payload).then((res) => res.data)
}

export function getBook(bookId: number) {
  return client.get<Book>(`/books/${bookId}`).then((res) => res.data)
}
