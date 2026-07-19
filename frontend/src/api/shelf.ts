import client from './client'
import type { ShelfEntry, ShelfLists } from '../types'

export function getShelf() {
  return client.get<ShelfLists>('/shelf').then((res) => res.data)
}

export function addToShelf(isbn: string) {
  return client.post<ShelfEntry>('/shelf', { isbn }).then((res) => res.data)
}

export function updateStatus(recordId: number, status: string) {
  return client.patch<ShelfEntry>(`/shelf/${recordId}`, { status }).then((res) => res.data)
}

export function removeFromShelf(recordId: number) {
  return client.delete(`/shelf/${recordId}`)
}
