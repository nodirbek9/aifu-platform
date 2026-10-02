import { useMemo } from 'react'
import DOMPurify, { type Config } from 'dompurify'
import { cn } from '@/shared/lib/cn'

export type RichTextProps = {
  html: string
  className?: string
}

const PURIFY_CONFIG: Config = {
  USE_PROFILES: { html: true },
  FORBID_TAGS: ['style', 'form', 'input', 'button'],
  FORBID_ATTR: ['style'],
}

/**
 * The only place server HTML is rendered. The server sanitizes already;
 * DOMPurify runs again here, always (aifu-api skill).
 */
export function RichText({ html, className }: RichTextProps) {
  const sanitized = useMemo(() => DOMPurify.sanitize(html, PURIFY_CONFIG), [html])
  return (
    <div
      className={cn(
        'font-reading text-text',
        '[&_h2]:font-display [&_h2]:text-display-lg [&_h2]:mt-8 [&_h2]:mb-3 [&_h2]:font-semibold',
        '[&_h3]:font-display [&_h3]:text-title [&_h3]:mt-6 [&_h3]:mb-2 [&_h3]:font-semibold',
        '[&_p]:my-3 [&_p]:leading-relaxed',
        '[&_a]:text-primary-hover [&_a]:underline [&_a]:underline-offset-2',
        '[&_ol]:my-3 [&_ol]:list-decimal [&_ol]:pl-6 [&_ul]:my-3 [&_ul]:list-disc [&_ul]:pl-6',
        '[&_li]:my-1',
        '[&_strong]:font-semibold',
        '[&_code]:bg-surface-sunken [&_code]:text-small [&_code]:rounded [&_code]:px-1 [&_code]:py-0.5 [&_code]:font-mono',
        '[&_pre]:bg-surface-sunken [&_pre]:text-small [&_pre]:my-4 [&_pre]:overflow-x-auto [&_pre]:rounded-lg [&_pre]:p-4 [&_pre]:font-mono',
        '[&_pre_code]:bg-transparent [&_pre_code]:p-0',
        '[&_blockquote]:border-border-strong [&_blockquote]:text-text-muted [&_blockquote]:my-4 [&_blockquote]:border-l-4 [&_blockquote]:pl-4',
        '[&_table]:text-small [&_table]:my-4 [&_table]:w-full [&_table]:border-collapse',
        '[&_th]:border-border [&_th]:bg-surface-sunken [&_th]:border [&_th]:px-3 [&_th]:py-2 [&_th]:text-left',
        '[&_td]:border-border [&_td]:border [&_td]:px-3 [&_td]:py-2',
        '[&_img]:my-4 [&_img]:rounded-lg',
        '[&_hr]:border-border [&_hr]:my-6',
        className,
      )}
      // Sanitized twice: server-side, and above with DOMPurify.
      dangerouslySetInnerHTML={{ __html: sanitized }}
    />
  )
}
