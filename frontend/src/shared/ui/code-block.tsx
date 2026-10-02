import { useState, type ReactNode } from 'react'
import { Check, Copy } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type CodeBlockProps = {
  code: string
  language?: string
  copyLabel: string
  copiedLabel: string
  className?: string
  /** Replaces the plain listing (a highlighted one); the plain one is the fallback while it loads. */
  body?: ReactNode
  /** A control shown in the header before "copy" (the lesson's "Try it"). */
  action?: ReactNode
}

/** Read-only code with line numbers and copy. The editable CodeMirror editor is F2. */
export function CodeBlock({
  code,
  language,
  copyLabel,
  copiedLabel,
  className,
  body,
  action,
}: CodeBlockProps) {
  const [copied, setCopied] = useState(false)
  const lines = code.replace(/\n$/, '').split('\n')

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(code)
      setCopied(true)
      window.setTimeout(() => {
        setCopied(false)
      }, 1600)
    } catch {
      /* clipboard unavailable */
    }
  }

  return (
    <div className={cn('border-border bg-surface-sunken overflow-hidden rounded-lg border', className)}>
      <div className="border-border flex items-center justify-between border-b px-3.5 py-1.5">
        <span className="text-caption tracking-caption text-text-muted font-medium uppercase">
          {language ?? 'code'}
        </span>
        <div className="flex items-center gap-1">
          {action}
          <button
            type="button"
            onClick={() => {
              void copy()
            }}
            aria-label={copied ? copiedLabel : copyLabel}
            className={cn(
              'text-text-muted relative flex size-8 items-center justify-center rounded-sm',
              'duration-fast hover:bg-surface-raised hover:text-text transition-[background-color,color]',
              'focus-visible:outline-focus-ring focus-visible:outline-2',
            )}
          >
            <Copy
              aria-hidden
              className={cn(
                'duration-normal absolute size-4 transition-[opacity,scale]',
                copied ? 'scale-50 opacity-0' : 'scale-100 opacity-100',
              )}
            />
            <Check
              aria-hidden
              className={cn(
                'text-success duration-normal absolute size-4 transition-[opacity,scale]',
                copied ? 'scale-100 opacity-100' : 'scale-50 opacity-0',
              )}
            />
          </button>
        </div>
      </div>
      {body ?? (
        <pre className="text-small text-text overflow-x-auto p-3.5 font-mono leading-6">
          <code>
            {lines.map((line, index) => (
              <span key={index} className="grid grid-cols-[2.5rem_1fr]">
                <span aria-hidden className="text-text-muted/60 pr-3 text-right select-none">
                  {index + 1}
                </span>
                <span>{line === '' ? ' ' : line}</span>
              </span>
            ))}
          </code>
        </pre>
      )}
    </div>
  )
}
