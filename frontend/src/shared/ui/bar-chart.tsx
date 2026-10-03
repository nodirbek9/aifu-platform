import { useId, useState, type ReactNode } from 'react'
import { BarChart3, Table2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'

const bar = tv({
  base: 'duration-slow ease-emphasized h-full rounded-sm transition-[width]',
  variants: {
    tone: {
      primary: 'bg-primary',
      accent: 'bg-accent-strong',
      success: 'bg-success',
      warning: 'bg-warning',
      danger: 'bg-danger',
      info: 'bg-info',
    },
  },
  defaultVariants: { tone: 'primary' },
})

export type BarTone = NonNullable<VariantProps<typeof bar>['tone']>

export type BarDatum = {
  key: string
  label: ReactNode
  value: number
  /** What the bar reads as: "12 of 30", "64 %". */
  display: string
  tone?: BarTone
}

export type BarChartProps = {
  title: string
  /** One sentence saying what the chart shows — read before the chart, never only in it. */
  summary: ReactNode
  data: readonly BarDatum[]
  /** The full length of a bar; the largest value when absent. */
  max?: number
  /** Column headings of the table alternative. */
  columns: [string, string]
  className?: string
}

/**
 * Horizontal bars in the design system's colours, with a text summary and the same numbers as a
 * table: the table is what assistive technology reads, and anyone can switch to it.
 */
export function BarChart({ title, summary, data, max, columns, className }: BarChartProps) {
  const { t } = useTranslation()
  const [asTable, setAsTable] = useState(false)
  const id = useId()
  const top = max ?? Math.max(1, ...data.map((datum) => datum.value))

  return (
    <figure aria-labelledby={`${id}-title`} className={cn('grid gap-3', className)}>
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="grid gap-0.5">
          <h3 id={`${id}-title`} className="font-display text-title text-text font-semibold">
            {title}
          </h3>
          <figcaption className="text-small text-text-muted max-w-prose">{summary}</figcaption>
        </div>
        <button
          type="button"
          aria-pressed={asTable}
          onClick={() => {
            setAsTable(!asTable)
          }}
          className="text-small text-text-muted hover:text-text focus-visible:outline-focus-ring inline-flex h-8 items-center gap-1.5 rounded-sm px-2 focus-visible:outline-2 focus-visible:outline-offset-2"
        >
          {asTable ? <BarChart3 aria-hidden className="size-4" /> : <Table2 aria-hidden className="size-4" />}
          {asTable ? t('chart.showChart') : t('chart.showTable')}
        </button>
      </div>
      {!asTable && (
        <ul aria-hidden className="grid gap-2">
          {data.map((datum) => (
            <li key={datum.key} className="grid grid-cols-[minmax(0,12rem)_1fr_auto] items-center gap-3">
              <span className="text-small text-text truncate">{datum.label}</span>
              <span className="bg-surface-sunken h-3 overflow-hidden rounded-sm">
                <span
                  className={cn(bar({ tone: datum.tone }), 'block')}
                  style={{ width: `${Math.max(0, Math.min(100, (datum.value / top) * 100))}%` }}
                />
              </span>
              <span className="text-caption text-text-muted tabular-nums">{datum.display}</span>
            </li>
          ))}
        </ul>
      )}
      {/* a table never shrinks below its content: the hiding box around it clips it instead */}
      <div className={asTable ? undefined : 'sr-only'}>
        <table className="text-small w-full">
          <thead>
            <tr className="border-border border-b">
              <th scope="col" className="text-text py-1.5 pr-3 text-left font-semibold">
                {columns[0]}
              </th>
              <th scope="col" className="text-text py-1.5 text-right font-semibold">
                {columns[1]}
              </th>
            </tr>
          </thead>
          <tbody>
            {data.map((datum) => (
              <tr key={datum.key} className="border-border border-b">
                <th scope="row" className="text-text py-1.5 pr-3 text-left font-normal">
                  {datum.label}
                </th>
                <td className="text-text py-1.5 text-right tabular-nums">{datum.display}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </figure>
  )
}
