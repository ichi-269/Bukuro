<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as booksApi from '../../api/books'
import * as shelfApi from '../../api/shelf'
import type { ApiError, Book } from '../../types'

type Step = 'search' | 'titleResults' | 'confirm'

const step = ref<Step>('search')
const errorMessage = ref<string | null>(null)
const isbn = ref('')
const keyword = ref('')
const candidates = ref<Book[]>([])
const book = ref<Book | null>(null)
const submitting = ref(false)

const router = useRouter()

function errorText(err: unknown, fallback: string) {
  const apiError = err as ApiError
  return apiError.message ?? fallback
}

async function handleIsbnSearch() {
  errorMessage.value = null
  if (!isbn.value.trim()) {
    errorMessage.value = 'ISBNを入力してください'
    return
  }
  try {
    book.value = await booksApi.searchByIsbn(isbn.value)
    step.value = 'confirm'
  } catch (err) {
    errorMessage.value = errorText(err, 'この書籍はOpenBDに登録されていません。ISBNを確認してください。')
  }
}

async function handleTitleSearch() {
  errorMessage.value = null
  if (!keyword.value.trim()) {
    errorMessage.value = '書名を入力してください'
    return
  }
  try {
    candidates.value = await booksApi.searchByTitle(keyword.value)
    step.value = 'titleResults'
  } catch (err) {
    errorMessage.value = errorText(err, '接続エラーが発生しました')
  }
}

async function selectCandidate(candidate: Book) {
  errorMessage.value = null
  try {
    book.value = await booksApi.confirmFromTitle({
      isbn: candidate.isbn,
      title: candidate.title,
      author: candidate.author,
      publisher: candidate.publisher ?? undefined,
    })
    step.value = 'confirm'
  } catch (err) {
    errorMessage.value = errorText(err, '接続エラーが発生しました')
  }
}

async function addToShelf() {
  if (!book.value) return
  submitting.value = true
  try {
    await shelfApi.addToShelf(book.value.isbn)
    router.push('/shelf')
  } catch (err) {
    errorMessage.value = errorText(err, '本棚への追加に失敗しました')
  } finally {
    submitting.value = false
  }
}

function backToSearch() {
  step.value = 'search'
  errorMessage.value = null
  book.value = null
}
</script>

<template>
  <main class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-7">
        <div v-if="step === 'search'">
          <h2 class="mb-4 fw-bold">本を検索する</h2>
          <div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>

          <div class="card mb-4">
            <div class="card-body">
              <h5 class="card-title fw-semibold mb-3">書名で検索</h5>
              <p class="text-muted small mb-3">書名の一部を入力すると国立国会図書館から候補を取得します。</p>
              <form @submit.prevent="handleTitleSearch">
                <div class="mb-3">
                  <label for="keyword" class="form-label">書名キーワード</label>
                  <input
                    id="keyword"
                    v-model="keyword"
                    type="text"
                    class="form-control"
                    placeholder="例: 人月の神話"
                    autocomplete="off"
                  />
                </div>
                <button type="submit" class="btn btn-dark w-100">候補を検索する</button>
              </form>
            </div>
          </div>

          <div class="card">
            <div class="card-body">
              <h5 class="card-title fw-semibold mb-3">ISBNで検索</h5>
              <p class="text-muted small mb-3">本の裏表紙にあるISBNバーコードの番号を入力してください。</p>
              <form @submit.prevent="handleIsbnSearch">
                <div class="mb-3">
                  <label for="isbn" class="form-label">ISBN</label>
                  <input
                    id="isbn"
                    v-model="isbn"
                    type="text"
                    class="form-control"
                    placeholder="例: 9784774192178"
                    maxlength="17"
                    autocomplete="off"
                  />
                  <div class="form-text">ハイフンあり・なし、ISBN-10・ISBN-13どちらも入力できます</div>
                </div>
                <button type="submit" class="btn btn-outline-dark w-100">書誌情報を取得する</button>
              </form>
            </div>
          </div>
        </div>

        <div v-else-if="step === 'titleResults'">
          <h2 class="mb-1 fw-bold">書名検索結果</h2>
          <p class="text-muted mb-4">
            「{{ keyword }}」の検索結果
            <span class="ms-2 text-secondary">（{{ candidates.length }}件）</span>
          </p>

          <div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>

          <div v-if="candidates.length === 0" class="alert alert-info">
            該当する図書が見つかりませんでした。別のキーワードで検索してください。
          </div>

          <div v-for="candidate in candidates" :key="candidate.isbn" class="card mb-3">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start gap-3">
                <div class="flex-grow-1">
                  <h6 class="fw-bold mb-1">{{ candidate.title }}</h6>
                  <div v-if="candidate.author" class="text-muted small mb-1">{{ candidate.author }}</div>
                  <div v-if="candidate.publisher" class="text-muted small mb-1">{{ candidate.publisher }}</div>
                  <div class="text-muted small font-monospace">ISBN: {{ candidate.isbn }}</div>
                </div>
                <button type="button" class="btn btn-outline-dark btn-sm flex-shrink-0" @click="selectCandidate(candidate)">
                  この本を選択
                </button>
              </div>
            </div>
          </div>

          <button type="button" class="btn btn-link ps-0" @click="backToSearch">← 検索に戻る</button>
        </div>

        <div v-else-if="step === 'confirm' && book">
          <h2 class="mb-4 fw-bold">書誌情報の確認</h2>
          <p class="text-muted mb-4">取得した書誌情報を確認してください。この情報は変更できません。</p>

          <div v-if="errorMessage" class="alert alert-danger" role="alert">{{ errorMessage }}</div>

          <div class="card">
            <div class="card-body p-4">
              <div class="row g-3">
                <div class="col-auto">
                  <img
                    v-if="book.coverUrl"
                    :src="book.coverUrl"
                    alt="書影"
                    class="img-thumbnail"
                    style="width: 100px; height: auto"
                  />
                  <div
                    v-else
                    class="bg-light d-flex align-items-center justify-content-center"
                    style="width: 100px; height: 140px"
                  >
                    <span class="text-muted small">No Cover</span>
                  </div>
                </div>
                <div class="col">
                  <h5 class="fw-bold mb-1">{{ book.title }}</h5>
                  <p class="text-muted mb-1">{{ book.author }}</p>
                  <p class="text-muted small mb-1">{{ book.publisher }}</p>
                  <p class="text-muted small font-monospace">ISBN: {{ book.isbn }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-4 d-flex gap-2">
            <button type="button" class="btn btn-dark btn-lg flex-grow-1" :disabled="submitting" @click="addToShelf">
              本棚に追加する
            </button>
            <button type="button" class="btn btn-outline-secondary btn-lg" @click="backToSearch">別の本を検索</button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
