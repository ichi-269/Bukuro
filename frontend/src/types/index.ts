export interface Book {
  id: number | null
  isbn: string
  title: string
  author: string
  publisher: string | null
  coverUrl: string | null
}

export interface User {
  id: number
  username: string
  bio: string | null
  createdAt: string
}

export interface Me {
  id: number
  username: string
  email: string
  bio: string | null
  createdAt: string
}

export type ReadingStatus = 'WANT_TO_READ' | 'READING' | 'DONE'

export interface ShelfEntry {
  id: number
  book: Book
  status: ReadingStatus
  rating: number | null
  startedAt: string | null
  finishedAt: string | null
  postId: number | null
}

export interface ShelfLists {
  wantToRead: ShelfEntry[]
  reading: ShelfEntry[]
  done: ShelfEntry[]
}

export interface Post {
  id: number
  title: string
  body: string
  isPublic: boolean
  goodCount: number
  createdAt: string
  updatedAt: string
  book: Book
  user: User
  isOwner: boolean | null
  hasGooded: boolean | null
}

export interface UserProfile {
  profileUser: User
  posts: Post[]
  postCount: number
  isOwnPage: boolean
  isFollowing: boolean
  followerCount: number
  followingCount: number
}

export interface HomeFeed {
  feedType: 'recommended' | 'following'
  hasFollowees: boolean
  posts: Post[]
}

export interface FieldError {
  field: string
  message: string
}

export interface ApiError {
  status: number
  code: string
  message: string
  fieldErrors?: FieldError[]
}
