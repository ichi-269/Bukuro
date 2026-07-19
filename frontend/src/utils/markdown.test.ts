import { describe, it, expect } from 'vitest'
import { renderMarkdown } from './markdown'

describe('renderMarkdown', () => {
  it('converts bold text', () => {
    expect(renderMarkdown('**重要**')).toContain('<strong>重要</strong>')
  })

  it('converts headings', () => {
    expect(renderMarkdown('## 見出し2')).toContain('<h2>見出し2</h2>')
    expect(renderMarkdown('### 見出し3')).toContain('<h3>見出し3</h3>')
  })

  it('converts bullet lists', () => {
    const html = renderMarkdown('- item1\n- item2')
    expect(html).toContain('<ul>')
    expect(html).toContain('<li>item1</li>')
    expect(html).toContain('<li>item2</li>')
  })

  it('converts links', () => {
    const html = renderMarkdown('[example](https://example.com)')
    expect(html).toContain('<a href="https://example.com">example</a>')
  })

  it('sanitizes script tags', () => {
    const html = renderMarkdown('<script>alert(1)</script>')
    expect(html).not.toContain('<script>')
    expect(html).not.toContain('alert(1)')
  })

  it('sanitizes onerror attribute injection', () => {
    const html = renderMarkdown('<img src=x onerror=alert(1)>')
    expect(html).not.toContain('onerror')
    expect(html).not.toContain('<img')
  })

  it('sanitizes javascript: link scheme', () => {
    const html = renderMarkdown('[click me](javascript:alert(1))')
    expect(html).not.toContain('javascript:')
  })

  it('renders plain text without markdown syntax as a normal paragraph', () => {
    const html = renderMarkdown('これは通常の記事本文です。')
    expect(html).toContain('これは通常の記事本文です。')
    expect(html).not.toContain('<script>')
  })
})
