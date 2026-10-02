import type { HTMLAttributes, TableHTMLAttributes, TdHTMLAttributes, ThHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'

/**
 * Print-table discipline: sticky header, tabular figures, right-aligned numbers,
 * row hover. On a phone it scrolls inside its own container — never the page.
 */
export function Table({ className, ...props }: TableHTMLAttributes<HTMLTableElement>) {
  return (
    <div className="border-border w-full overflow-x-auto rounded-lg border">
      <table
        className={cn('bg-surface-raised text-small text-text w-full border-collapse', className)}
        {...props}
      />
    </div>
  )
}

export function TableHead({ className, ...props }: HTMLAttributes<HTMLTableSectionElement>) {
  return (
    <thead
      className={cn(
        'bg-surface-sunken text-caption tracking-caption text-text-muted sticky top-0 z-10 text-left font-medium uppercase',
        className,
      )}
      {...props}
    />
  )
}

export function TableBody({ className, ...props }: HTMLAttributes<HTMLTableSectionElement>) {
  return <tbody className={cn('divide-border divide-y', className)} {...props} />
}

export function TableRow({ className, ...props }: HTMLAttributes<HTMLTableRowElement>) {
  return (
    <tr
      className={cn('duration-fast hover:bg-surface-sunken/60 transition-[background-color]', className)}
      {...props}
    />
  )
}

export type TableHeaderCellProps = ThHTMLAttributes<HTMLTableCellElement> & {
  numeric?: boolean
}

export function TableHeaderCell({ className, numeric = false, ...props }: TableHeaderCellProps) {
  return (
    <th
      scope="col"
      className={cn('px-4 py-2.5 font-medium whitespace-nowrap', numeric && 'text-right', className)}
      {...props}
    />
  )
}

export type TableCellProps = TdHTMLAttributes<HTMLTableCellElement> & {
  numeric?: boolean
}

export function TableCell({ className, numeric = false, ...props }: TableCellProps) {
  return (
    <td
      className={cn('px-4 py-3 align-middle', numeric && 'text-right tabular-nums', className)}
      {...props}
    />
  )
}
