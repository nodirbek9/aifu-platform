import { Suspense, lazy, useId, useRef, useState } from 'react'
import { Bold, Code, Heading2, Heading3, Italic, List, ListOrdered, Pilcrow, Table } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { input } from '@/shared/ui/input'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Skeleton } from '@/shared/ui/skeleton'

const VisualEditor = lazy(() => import('./visual-editor'))

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

/** The elements the visual editor keeps; anything else would be dropped by it, so HTML stays. */
const VISUAL_TAGS = new Set([
  'p',
  'h2',
  'h3',
  'strong',
  'b',
  'em',
  'i',
  'code',
  'pre',
  'ul',
  'ol',
  'li',
  'a',
  'br',
  'hr',
  'blockquote',
  's',
  'table',
  'thead',
  'tbody',
  'tr',
  'th',
  'td',
  'colgroup',
  'col',
])

function fitsVisualEditor(html: string): boolean {
  for (const match of html.matchAll(/<\s*([a-zA-Z][a-zA-Z0-9]*)/g)) {
    if (!VISUAL_TAGS.has((match[1] ?? '').toLowerCase())) return false
  }
  return true
}

type HtmlFieldProps = {
  label: string
  value: string
  onChange: (value: string) => void
  rows?: number
  hint?: string
}

/**
 * Rich text as the server stores it (sanitized HTML). Typed either as the student will see it
 * (the visual editor, loaded on demand) or as HTML with a toolbar that wraps the selection. Text
 * with elements the visual editor would drop (a self-check's answers) opens as HTML.
 */
export function HtmlField({ label, value, onChange, rows = 8, hint }: HtmlFieldProps) {
  const { t } = useTranslation()
  const id = useId()
  const labelId = `${id}-label`
  const area = useRef<HTMLTextAreaElement>(null)
  const visualPossible = fitsVisualEditor(value)
  const [mode, setMode] = useState<'visual' | 'html'>(visualPossible ? 'visual' : 'html')
  const shown = visualPossible ? mode : 'html'

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
    <div className="grid grid-cols-1 gap-1.5">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <label id={labelId} htmlFor={id} className="text-small text-text font-medium">
          {label}
        </label>
        <SegmentedControl
          size="sm"
          aria-label={t('authoring.editMode')}
          value={shown}
          onValueChange={(value) => {
            if (value === 'visual' && visualPossible) setMode('visual')
            if (value === 'html') setMode('html')
          }}
          options={[
            { value: 'visual', label: t('authoring.modeVisual'), disabled: !visualPossible },
            { value: 'html', label: 'HTML' },
          ]}
        />
      </div>
      {shown === 'visual' ? (
        <Suspense fallback={<Skeleton className="h-48" />}>
          <VisualEditor id={id} labelledBy={labelId} value={value} onChange={onChange} />
        </Suspense>
      ) : (
        <>
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
                className="text-text-muted hover:bg-surface-raised hover:text-text focus-visible:outline-focus-ring grid size-8 grid-cols-1 place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
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
        </>
      )}
      {!visualPossible && (
        <p className="text-caption text-text-muted tracking-normal">{t('authoring.htmlOnly')}</p>
      )}
      {hint !== undefined && <p className="text-caption text-text-muted tracking-normal">{hint}</p>}
    </div>
  )
}
