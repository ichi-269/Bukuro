import client from './client'
import type { Me } from '../types'

export interface RegisterPayload {
  email: string
  username: string
  password: string
  passwordConfirm: string
}

export function register(payload: RegisterPayload) {
  return client.post<Me>('/register', payload).then((res) => res.data)
}
