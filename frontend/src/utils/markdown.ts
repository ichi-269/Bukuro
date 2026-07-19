import { Marked } from 'marked'
import DOMPurify from 'dompurify'

const marked = new Marked({
  gfm: true,
  breaks: true,
})

const ALLOWED_TAGS = ['p', 'br', 'strong', 'em', 'h2', 'h3', 'h4', 'ul', 'ol', 'li', 'a', 'blockquote', 'code', 'pre']
const ALLOWED_ATTR = ['href']

export function renderMarkdown(raw: string): string {
  const html = marked.parse(raw, { async: false })
  return DOMPurify.sanitize(html, { ALLOWED_TAGS, ALLOWED_ATTR })
}
