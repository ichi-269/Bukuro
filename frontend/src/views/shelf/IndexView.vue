<script setup lang="ts">
import { onMounted, ref } from 'vue'
import * as shelfApi from '../../api/shelf'
import type { ApiError, ShelfEntry, ShelfLists } from '../../types'

const shelf = ref<ShelfLists>({ wantToRead: [], reading: [], done: [] })
const errorMessage = ref<string | null>(null)

async function load() {
  shelf.value = await shelfApi.getShelf()
}

onMounted(load)

async function changeStatus(entry: ShelfEntry, status: string) {
  try {
    await shelfApi.updateStatus(entry.id, status)
    await load()
  } catch (err) {
    errorMessage.value = (err as ApiError).message ?? 'ステータスの更新に失敗しました'
  }
}

async function remove(entry: ShelfEntry) {
  if (!confirm('本棚から削除しますか？')) return
  try {
    await shelfApi.removeFromShelf(entry.id)
    await load()
  } catch (err) {
    errorMessage.value = (err as ApiError).message ?? '削除に失敗しました'
  }
}

</script>

<template>
  <main class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold mb-0">本棚</h2>
      <router-link to="/books/search" class="btn btn-dark">本を追加する</router-link>
    </div>

    <div v-if="errorMessage" class="alert alert-warning alert-dismissible fade show" role="alert">
      {{ errorMessage }}
      <button type="button" class="btn-close" @click="errorMessage = null"></button>
    </div>

    <ul class="nav nav-tabs mb-4" id="shelfTabs" role="tablist">
      <li class="nav-item" role="presentation">
        <button class="nav-link active" id="want-tab" data-bs-toggle="tab" data-bs-target="#want" type="button" role="tab">
          読みたい <span class="badge bg-secondary ms-1">{{ shelf.wantToRead.length }}</span>
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="reading-tab" data-bs-toggle="tab" data-bs-target="#reading" type="button" role="tab">
          読書中 <span class="badge bg-secondary ms-1">{{ shelf.reading.length }}</span>
        </button>
      </li>
      <li class="nav-item" role="presentation">
        <button class="nav-link" id="done-tab" data-bs-toggle="tab" data-bs-target="#done" type="button" role="tab">
          読了 <span class="badge bg-secondary ms-1">{{ shelf.done.length }}</span>
        </button>
      </li>
    </ul>

    <div class="tab-content" id="shelfTabsContent">
      <div class="tab-pane fade show active" id="want" role="tabpanel">
        <div v-if="shelf.wantToRead.length === 0" class="text-center text-muted py-5">
          <p>読みたい本はまだありません。</p>
          <router-link to="/books/search" class="btn btn-outline-dark">本を追加する</router-link>
        </div>
        <div v-for="entry in shelf.wantToRead" :key="entry.id" class="card mb-3">
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-auto">
                <img
                  v-if="entry.book.coverUrl"
                  :src="entry.book.coverUrl"
                  alt="書影"
                  class="img-thumbnail"
                  style="width: 70px; height: auto"
                />
                <div
                  v-else
                  class="bg-light d-flex align-items-center justify-content-center"
                  style="width: 70px; height: 100px"
                >
                  <span class="text-muted" style="font-size: 0.65rem">No Cover</span>
                </div>
              </div>
              <div class="col">
                <h6 class="fw-bold mb-1">
                  <router-link v-if="entry.postId" :to="`/posts/${entry.postId}`" class="text-decoration-none text-dark">
                    {{ entry.book.title }}
                  </router-link>
                  <span v-else>{{ entry.book.title }}</span>
                </h6>
                <p class="text-muted small mb-1">{{ entry.book.author }}</p>
                <p class="text-muted small mb-0">{{ entry.book.publisher }}</p>
              </div>
              <div class="col-auto d-flex gap-2 align-items-center flex-wrap justify-content-end">
                <router-link
                  v-if="!entry.postId"
                  :to="`/posts/new?bookId=${entry.book.id}`"
                  class="btn btn-outline-dark btn-sm"
                >
                  記事を書く
                </router-link>
                <router-link v-else :to="`/posts/${entry.postId}/edit`" class="btn btn-outline-secondary btn-sm">
                  編集する
                </router-link>
                <div class="d-flex gap-2 align-items-center">
                  <select
                    class="form-select form-select-sm"
                    style="width: auto"
                    :value="entry.status"
                    @change="changeStatus(entry, ($event.target as HTMLSelectElement).value)"
                  >
                    <option value="WANT_TO_READ">読みたい</option>
                    <option value="READING">読書中</option>
                    <option value="DONE">読了</option>
                  </select>
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm" @click="remove(entry)">削除</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="reading" role="tabpanel">
        <div v-if="shelf.reading.length === 0" class="text-center text-muted py-5">
          <p>読書中の本はありません。</p>
        </div>
        <div v-for="entry in shelf.reading" :key="entry.id" class="card mb-3">
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-auto">
                <img
                  v-if="entry.book.coverUrl"
                  :src="entry.book.coverUrl"
                  alt="書影"
                  class="img-thumbnail"
                  style="width: 70px; height: auto"
                />
                <div
                  v-else
                  class="bg-light d-flex align-items-center justify-content-center"
                  style="width: 70px; height: 100px"
                >
                  <span class="text-muted" style="font-size: 0.65rem">No Cover</span>
                </div>
              </div>
              <div class="col">
                <h6 class="fw-bold mb-1">
                  <router-link v-if="entry.postId" :to="`/posts/${entry.postId}`" class="text-decoration-none text-dark">
                    {{ entry.book.title }}
                  </router-link>
                  <span v-else>{{ entry.book.title }}</span>
                </h6>
                <p class="text-muted small mb-1">{{ entry.book.author }}</p>
                <p class="text-muted small mb-0">{{ entry.book.publisher }}</p>
              </div>
              <div class="col-auto d-flex gap-2 align-items-center flex-wrap justify-content-end">
                <router-link
                  v-if="!entry.postId"
                  :to="`/posts/new?bookId=${entry.book.id}`"
                  class="btn btn-outline-dark btn-sm"
                >
                  記事を書く
                </router-link>
                <router-link v-else :to="`/posts/${entry.postId}/edit`" class="btn btn-outline-secondary btn-sm">
                  編集する
                </router-link>
                <select
                  class="form-select form-select-sm"
                  style="width: auto"
                  :value="entry.status"
                  @change="changeStatus(entry, ($event.target as HTMLSelectElement).value)"
                >
                  <option value="WANT_TO_READ">読みたい</option>
                  <option value="READING">読書中</option>
                  <option value="DONE">読了</option>
                </select>
                <button type="button" class="btn btn-outline-danger btn-sm" @click="remove(entry)">削除</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tab-pane fade" id="done" role="tabpanel">
        <div v-if="shelf.done.length === 0" class="text-center text-muted py-5">
          <p>読了した本はありません。</p>
        </div>
        <div v-for="entry in shelf.done" :key="entry.id" class="card mb-3">
          <div class="card-body">
            <div class="row g-3 align-items-center">
              <div class="col-auto">
                <img
                  v-if="entry.book.coverUrl"
                  :src="entry.book.coverUrl"
                  alt="書影"
                  class="img-thumbnail"
                  style="width: 70px; height: auto"
                />
                <div
                  v-else
                  class="bg-light d-flex align-items-center justify-content-center"
                  style="width: 70px; height: 100px"
                >
                  <span class="text-muted" style="font-size: 0.65rem">No Cover</span>
                </div>
              </div>
              <div class="col">
                <h6 class="fw-bold mb-1">
                  <router-link v-if="entry.postId" :to="`/posts/${entry.postId}`" class="text-decoration-none text-dark">
                    {{ entry.book.title }}
                  </router-link>
                  <span v-else>{{ entry.book.title }}</span>
                </h6>
                <p class="text-muted small mb-1">{{ entry.book.author }}</p>
                <p class="text-muted small mb-0">{{ entry.book.publisher }}</p>
              </div>
              <div class="col-auto d-flex gap-2 align-items-center flex-wrap justify-content-end">
                <router-link
                  v-if="!entry.postId"
                  :to="`/posts/new?bookId=${entry.book.id}`"
                  class="btn btn-outline-dark btn-sm"
                >
                  記事を書く
                </router-link>
                <router-link v-else :to="`/posts/${entry.postId}/edit`" class="btn btn-outline-secondary btn-sm">
                  編集する
                </router-link>
                <select
                  class="form-select form-select-sm"
                  style="width: auto"
                  :value="entry.status"
                  @change="changeStatus(entry, ($event.target as HTMLSelectElement).value)"
                >
                  <option value="WANT_TO_READ">読みたい</option>
                  <option value="READING">読書中</option>
                  <option value="DONE">読了</option>
                </select>
                <button type="button" class="btn btn-outline-danger btn-sm" @click="remove(entry)">削除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
