import { useMemo } from 'react'
import DOMPurify, { type Config } from 'dompurify'
import { cn } from '@/shared/lib/cn'

export type RichTextProps = {
  html: string
  className?: string
  /** Gives every h2 / h3 the id `<prefix>-<n>`, so a table of contents can link to them. */
  headingIdPrefix?: string
}

const PURIFY_CONFIG: Config = {
  USE_PROFILES: { html: true },
  FORBID_TAGS: ['style', 'form', 'input', 'button'],
  FORBID_ATTR: ['style'],
}

/** The server sanitizes already; DOMPurify runs again here, always (aifu-api skill). */
export function sanitizeHtml(html: string): string {
  return DOMPurify.sanitize(html, PURIFY_CONFIG)
}

export type Heading = {
  id: string
  text: string
  level: 2 | 3
}

/**
 * Numbers the h2 / h3 headings of sanitized HTML with ids and lists them. The same input and
 * prefix always give the same ids, so the outline built from a text matches the rendered text.
 */
export function addHeadingIds(sanitized: string, prefix: string): { html: string; headings: Heading[] } {
  const container = new DOMParser().parseFromString(`<div>${sanitized}</div>`, 'text/html').body
    .firstElementChild
  if (container === null) return { html: sanitized, headings: [] }
  const headings: Heading[] = []
  container.querySelectorAll('h2, h3').forEach((element, index) => {
    const id = `${prefix}-${index + 1}`
    element.id = id
    headings.push({ id, text: (element.textContent ?? '').trim(), level: element.tagName === 'H2' ? 2 : 3 })
  })
  return { html: container.innerHTML, headings }
}

/** The reading typography of server HTML: shared by the page and the visual editor. */
export const richTextClasses = cn(
  'font-reading text-reading-text',
  '[&_h2]:font-display [&_h2]:text-display-md md:[&_h2]:text-display-lg [&_h2]:mt-10 [&_h2]:mb-3 [&_h2]:scroll-mt-24 [&_h2]:font-semibold [&_h2]:text-text',
  '[&_h3]:font-display [&_h3]:text-title [&_h3]:mt-7 [&_h3]:mb-2 [&_h3]:scroll-mt-24 [&_h3]:font-semibold [&_h3]:text-text',
  '[&>:first-child]:mt-0 [&>:last-child]:mb-0',
  '[&_p]:my-[1em]',
  '[&_a]:text-primary-hover [&_a]:underline [&_a]:underline-offset-2',
  '[&_ol]:my-4 [&_ol]:list-decimal [&_ol]:pl-6 [&_ul]:my-4 [&_ul]:list-disc [&_ul]:pl-6',
  '[&_li]:my-1.5 [&_li]:pl-1.5',
  '[&_strong]:font-semibold [&_strong]:text-text',
  '[&_code]:bg-code-surface [&_code]:border-border [&_code]:text-small [&_code]:rounded-full [&_code]:border [&_code]:px-2 [&_code]:py-0.5 [&_code]:font-mono',
  '[&_pre]:bg-code-surface [&_pre]:border-border [&_pre]:text-small [&_pre]:my-5 [&_pre]:overflow-x-auto [&_pre]:rounded-lg [&_pre]:border [&_pre]:p-4 [&_pre]:font-mono',
  '[&_pre_code]:border-none [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_pre_code]:rounded-none',
  '[&_blockquote]:border-border-strong [&_blockquote]:text-text-muted [&_blockquote]:my-5 [&_blockquote]:border-l-4 [&_blockquote]:pl-4',
  '[&_table]:text-small [&_table]:my-5 [&_table]:w-full [&_table]:border-collapse [&_table]:font-sans [&_table]:tabular-nums',
  '[&_th]:border-border [&_th]:bg-surface-sunken [&_th]:border [&_th]:px-3 [&_th]:py-2 [&_th]:text-left [&_th]:font-semibold [&_th]:text-text',
  '[&_td]:border-border [&_td]:border [&_td]:px-3 [&_td]:py-2',
  '[&_img]:my-5 [&_img]:rounded-lg',
  '[&_hr]:border-border [&_hr]:my-8',
  // a self-check question: the answer opens under it
  '[&_details]:border-border [&_details]:bg-surface-raised [&_details]:my-3 [&_details]:rounded-md [&_details]:border [&_details]:px-4 [&_details]:py-3',
  '[&_summary]:text-text [&_summary]:cursor-pointer [&_summary]:font-sans [&_summary]:font-medium',
  '[&_details[open]>summary]:mb-2',
  // a wide table scrolls inside itself on a phone instead of widening the page
  'max-sm:[&_table]:block max-sm:[&_table]:overflow-x-auto',
)

/** The only place server HTML is rendered: sanitized, then set in the reading typography. */
export function RichText({ html, className, headingIdPrefix }: RichTextProps) {
  const prepared = useMemo(() => {
    const sanitized = sanitizeHtml(html)
    return headingIdPrefix === undefined ? sanitized : addHeadingIds(sanitized, headingIdPrefix).html
  }, [html, headingIdPrefix])
  return (
    <div
      className={cn(richTextClasses, className)}
      // Sanitized twice: by the server on write, and by sanitizeHtml above.
      dangerouslySetInnerHTML={{ __html: prepared }}
    />
  )
}
