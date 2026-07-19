<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const props = withDefaults(
  defineProps<{
    modelValue: string
    id?: string
    rows?: number
    placeholder?: string
    error?: string
  }>(),
  {
    id: 'markdown-editor',
    rows: 12,
    placeholder: '',
    error: undefined,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const activeTab = ref<'edit' | 'preview'>('edit')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const previewHtml = computed(() => renderMarkdown(props.modelValue))

function focusSelection(start: number, end: number) {
  nextTick(() => {
    const textarea = textareaRef.value
    if (!textarea) return
    textarea.focus()
    textarea.setSelectionRange(start, end)
  })
}

function wrapSelection(prefix: string, suffix: string, placeholder: string) {
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = props.modelValue
  const selected = value.slice(start, end) || placeholder
  const newValue = value.slice(0, start) + prefix + selected + suffix + value.slice(end)
  emit('update:modelValue', newValue)
  focusSelection(start + prefix.length, start + prefix.length + selected.length)
}

function prefixCurrentLine(prefix: string) {
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const value = props.modelValue
  const lineStart = value.lastIndexOf('\n', start - 1) + 1
  const newValue = value.slice(0, lineStart) + prefix + value.slice(lineStart)
  emit('update:modelValue', newValue)
  focusSelection(start + prefix.length, start + prefix.length)
}

function insertBold() {
  wrapSelection('**', '**', '太字')
}

function insertHeading(level: 2 | 3) {
  prefixCurrentLine('#'.repeat(level) + ' ')
}

function insertBulletList() {
  prefixCurrentLine('- ')
}

function insertLink() {
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const value = props.modelValue
  const text = value.slice(start, end) || 'リンクテキスト'
  const url = 'https://'
  const markdownLink = `[${text}](${url})`
  const newValue = value.slice(0, start) + markdownLink + value.slice(end)
  emit('update:modelValue', newValue)
  const urlStart = start + text.length + 3
  focusSelection(urlStart, urlStart + url.length)
}

function onInput(event: Event) {
  emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
}
</script>

<template>
  <div class="markdown-editor">
    <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
      <div class="btn-group" role="group" aria-label="書式ツールバー">
        <button type="button" class="btn btn-outline-secondary btn-sm" title="太字" @click="insertBold">
          <strong>B</strong>
        </button>
        <button type="button" class="btn btn-outline-secondary btn-sm" title="見出し2" @click="insertHeading(2)">H2</button>
        <button type="button" class="btn btn-outline-secondary btn-sm" title="見出し3" @click="insertHeading(3)">H3</button>
        <button type="button" class="btn btn-outline-secondary btn-sm" title="箇条書き" @click="insertBulletList">
          リスト
        </button>
        <button type="button" class="btn btn-outline-secondary btn-sm" title="リンク" @click="insertLink">リンク</button>
      </div>

      <ul class="nav nav-tabs card-header-tabs border-0">
        <li class="nav-item">
          <button
            type="button"
            class="nav-link"
            :class="{ active: activeTab === 'edit' }"
            @click="activeTab = 'edit'"
          >
            編集
          </button>
        </li>
        <li class="nav-item">
          <button
            type="button"
            class="nav-link"
            :class="{ active: activeTab === 'preview' }"
            @click="activeTab = 'preview'"
          >
            プレビュー
          </button>
        </li>
      </ul>
    </div>

    <textarea
      v-show="activeTab === 'edit'"
      :id="id"
      ref="textareaRef"
      :value="modelValue"
      class="form-control"
      :class="{ 'is-invalid': error }"
      :rows="rows"
      :placeholder="placeholder"
      @input="onInput"
    ></textarea>

    <div
      v-show="activeTab === 'preview'"
      class="form-control markdown-editor-preview"
      :style="{ minHeight: `${rows * 1.7}em` }"
    >
      <div class="markdown-content" v-html="previewHtml"></div>
    </div>

    <div v-if="error" class="invalid-feedback d-block">{{ error }}</div>
  </div>
</template>
