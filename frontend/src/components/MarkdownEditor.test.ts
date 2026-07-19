import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarkdownEditor from './MarkdownEditor.vue'

describe('MarkdownEditor', () => {
  it('inserts bold markers when the bold button is clicked', async () => {
    const wrapper = mount(MarkdownEditor, { props: { modelValue: '' } })
    const buttons = wrapper.findAll('button')
    const boldButton = buttons.find((b) => b.attributes('title') === '太字')!
    await boldButton.trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]?.[0]).toBe('**太字**')
  })

  it('inserts a heading prefix when the H2 button is clicked', async () => {
    const wrapper = mount(MarkdownEditor, { props: { modelValue: '' } })
    const buttons = wrapper.findAll('button')
    const h2Button = buttons.find((b) => b.attributes('title') === '見出し2')!
    await h2Button.trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]?.[0]).toBe('## ')
  })

  it('inserts a bullet list prefix when the list button is clicked', async () => {
    const wrapper = mount(MarkdownEditor, { props: { modelValue: '' } })
    const buttons = wrapper.findAll('button')
    const listButton = buttons.find((b) => b.attributes('title') === '箇条書き')!
    await listButton.trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]?.[0]).toBe('- ')
  })

  it('switches between edit and preview tabs and renders markdown in preview', async () => {
    const wrapper = mount(MarkdownEditor, { props: { modelValue: '**強調**' } })

    expect(wrapper.find('textarea').attributes('style') ?? '').not.toContain('display: none')

    const tabs = wrapper.findAll('.nav-link')
    const previewTab = tabs.find((t) => t.text() === 'プレビュー')!
    await previewTab.trigger('click')

    expect(wrapper.find('textarea').attributes('style')).toContain('display: none')
    expect(wrapper.find('.markdown-editor-preview strong').text()).toBe('強調')
  })
})
