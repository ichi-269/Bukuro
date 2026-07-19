import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarkdownContent from './MarkdownContent.vue'

describe('MarkdownContent', () => {
  it('renders bold and heading markdown as HTML', () => {
    const wrapper = mount(MarkdownContent, { props: { body: '## 見出し\n\n**太字**' } })
    expect(wrapper.find('h2').text()).toBe('見出し')
    expect(wrapper.find('strong').text()).toBe('太字')
  })

  it('renders plain text bodies without markdown syntax', () => {
    const wrapper = mount(MarkdownContent, { props: { body: 'ただの本文です' } })
    expect(wrapper.text()).toContain('ただの本文です')
  })

  it('does not render script tags injected in the body', () => {
    const wrapper = mount(MarkdownContent, { props: { body: '<script>alert(1)</script>' } })
    expect(wrapper.find('script').exists()).toBe(false)
  })
})
