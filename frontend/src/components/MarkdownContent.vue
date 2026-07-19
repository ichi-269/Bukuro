<script setup lang="ts">
import { computed } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const props = defineProps<{
  body: string
}>()

const html = computed(() => renderMarkdown(props.body))
</script>

<template>
  <div class="markdown-content" v-html="html"></div>
</template>
