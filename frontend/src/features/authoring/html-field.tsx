import { useId, useRef } from 'react'
import { Bold, Code, Heading2, Heading3, Italic, List, ListOrdered, Pilcrow, Table } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { input } from '@/shared/ui/input'

type Wrap = { key: string; Icon: typeof Bold; before: string; after: string; block?: boolean }

const WRAPS: readonly Wrap[] = [
  { key: 'paragraph', Icon: Pilcrow, before: '<p>', after: '</p>', block: true },
  { key: 'h2', Icon: Heading2, before: '<h2>', after: '</h2>', block: true },
  { key: 'h3', Icon: Heading3, before: '<h3>', after: '</h3>', block: true },
  { key: 'bold', Icon: Bold, before: '<strong>', after: '</strong>' },
  { key: 'italic', Icon: Italic, before: '<em>', after: '</em>' },
  { key: 'code', Icon: Code, before: '<code>', after: '</code>' },
  { key: 'list', Icon: List, before: '<ul>\n  <li>', after: '</li>\n</ul>', block: true },
  { key: 'orderedList', Icon: ListOrdered, before: '<ol>\n  <li>', after: '</li>\n</ol>', block: true },
  {
    key: 'table',
    Icon: Table,
    before: '<table>\n  <thead><tr><th>',
    after: '</th><th></th></tr></thead>\n  <tbody><tr><td></td><td></td></tr></tbody>\n</table>',
    block: true,
  },
]

type HtmlFieldProps = {
  label: string
  value: string
  onChange: (value: string) => void
  rows?: number
  hint?: string
}

/**
 * Rich text as the server stores it (sanitized HTML), typed with a toolbar that wraps the
 * selection: paragraph, headings, bold, italic, code, lists, a table. The preview beside the
 * editor shows the result as the student sees it.
 */
export function HtmlField({ label, value, onChange, rows = 8, hint }: HtmlFieldProps) {
  const { t } = useTranslation()
  const id = useId()
  const area = useRef<HTMLTextAreaElement>(null)

  const wrap = (item: Wrap) => {
    const element = area.current
    if (element === null) return
    const { selectionStart: start, selectionEnd: end } = element
    const selected = value.slice(start, end)
    const lead = item.block === true && start > 0 && value[start - 1] !== '\n' ? '\n' : ''
    const next = value.slice(0, start) + lead + item.before + selected + item.after + value.slice(end)
    onChange(next)
    const caret = start + lead.length + item.before.length + selected.length
    requestAnimationFrame(() => {
      element.focus()
      element.setSelectionRange(caret, caret)
    })
  }

  return (
    <div className="grid gap-1.5">
      <label htmlFor={id} className="text-small text-text font-medium">
        {label}
      </label>
      <div
        role="toolbar"
        aria-label={t('authoring.formatting')}
        aria-controls={id}
        className="border-border bg-surface-sunken flex flex-wrap gap-0.5 rounded-t-md border border-b-0 p-1"
      >
        {WRAPS.map((item) => (
          <button
            key={item.key}
            type="button"
            title={t(`authoring.format.${item.key}`)}
            aria-label={t(`authoring.format.${item.key}`)}
            onClick={() => {
              wrap(item)
            }}
            className="text-text-muted hover:bg-surface-raised hover:text-text focus-visible:outline-focus-ring grid size-8 place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
          >
            <item.Icon aria-hidden className="size-4" />
          </button>
        ))}
      </div>
      <textarea
        ref={area}
        id={id}
        rows={rows}
        value={value}
        spellCheck
        onChange={(event) => {
          onChange(event.target.value)
        }}
        className={cn(input({}), 'text-small h-auto min-h-32 rounded-t-none py-2 font-mono')}
      />
      {hint !== undefined && <p className="text-caption text-text-muted tracking-normal">{hint}</p>}
    </div>
  )
}
