import client from './client'
import type { HomeFeed } from '../types'

export function getFeed(feed?: 'recommended' | 'following') {
  return client.get<HomeFeed>('/home/feed', { params: feed ? { feed } : {} }).then((res) => res.data)
}
