import type { ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type StatProps = {
  label: ReactNode
  value: ReactNode
  hint?: ReactNode
  className?: string
}

/** A single number that matters — tabular figures, display face. */
export function Stat({ label, value, hint, className }: StatProps) {
  return (
    <div className={cn('grid gap-1', className)}>
      <div className="text-caption tracking-caption text-text-muted font-medium uppercase">{label}</div>
      <div className="font-display text-display-lg text-text font-semibold tabular-nums">{value}</div>
      {hint !== undefined && <div className="text-small text-text-muted">{hint}</div>}
    </div>
  )
}
