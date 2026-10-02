import type { ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type ScheduleColumn = {
  key: string
  header: ReactNode
  /** Today: the column is tinted. */
  highlighted?: boolean
  /** A day without classes by decree (a holiday): the column is hatched down. */
  off?: boolean
}

export type ScheduleRow = {
  key: string
  header: ReactNode
}

export type ScheduleNow = {
  rowKey: string
  columnKey: string
  /** How far through the row the clock is, 0–1. */
  progress: number
}

export type ScheduleGridProps = {
  /** What the table is, for a screen reader ("Timetable, 19–25 October"). */
  label: string
  /** The header of the first column (the bell times). */
  corner: ReactNode
  columns: readonly ScheduleColumn[]
  rows: readonly ScheduleRow[]
  renderCell: (row: ScheduleRow, column: ScheduleColumn) => ReactNode
  now?: ScheduleNow
  className?: string
}

/**
 * Days across, pairs down — the week as a printed schedule. A real table, so a screen reader
 * reads "Monday, pair 1" before a class. The "now" line crosses today's cell of the current
 * pair at the height the clock is at.
 */
export function ScheduleGrid({
  label,
  corner,
  columns,
  rows,
  renderCell,
  now,
  className,
}: ScheduleGridProps) {
  return (
    <div className={cn('border-border bg-surface-raised overflow-hidden rounded-lg border', className)}>
      <table aria-label={label} className="w-full table-fixed border-separate border-spacing-0">
        <colgroup>
          <col className="w-[4.75rem]" />
          {columns.map((column) => (
            <col key={column.key} />
          ))}
        </colgroup>
        <thead>
          <tr>
            <th
              scope="col"
              className="border-border text-caption tracking-caption text-text-muted border-b px-2 py-3 text-left align-bottom font-medium uppercase"
            >
              {corner}
            </th>
            {columns.map((column) => (
              <th
                key={column.key}
                scope="col"
                aria-current={column.highlighted === true ? 'date' : undefined}
                className={cn(
                  'border-border border-b border-l px-2 py-3 text-left align-bottom font-normal',
                  column.highlighted === true && 'bg-accent-soft',
                )}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={row.key}>
              <th
                scope="row"
                className={cn(
                  'border-border px-2 py-3 text-left align-top font-normal',
                  rowIndex > 0 && 'border-t',
                )}
              >
                {row.header}
              </th>
              {columns.map((column) => {
                const here = now?.rowKey === row.key && now.columnKey === column.key
                return (
                  <td
                    key={column.key}
                    className={cn(
                      'border-border relative h-24 border-l p-1 align-top',
                      rowIndex > 0 && 'border-t',
                      column.highlighted === true && 'bg-accent-soft/40',
                      column.off === true && 'bg-surface-sunken',
                    )}
                  >
                    {renderCell(row, column)}
                    {here && (
                      <span
                        aria-hidden
                        data-schedule-now
                        className="bg-accent pointer-events-none absolute inset-x-0 z-10 h-0.5"
                        style={{ top: `${Math.round(Math.min(1, Math.max(0, now.progress)) * 100)}%` }}
                      >
                        <span className="bg-accent absolute -top-[3px] -left-1 size-2 rounded-full" />
                      </span>
                    )}
                  </td>
                )
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
