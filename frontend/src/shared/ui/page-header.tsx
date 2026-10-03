import type { ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type PageHeaderProps = {
  title: ReactNode
  context?: ReactNode
  action?: ReactNode
  className?: string
}

/** Title in the display face, one line of context, the one primary action at right. */
export function PageHeader({ title, context, action, className }: PageHeaderProps) {
  return (
    <header className={cn('flex flex-wrap items-end justify-between gap-x-6 gap-y-3', className)}>
      <div className="grid min-w-0 gap-1">
        <h1 className="font-display text-display-lg text-text font-semibold">{title}</h1>
        {context !== undefined && <p className="text-small text-text-muted">{context}</p>}
      </div>
      {action !== undefined && <div className="max-w-full min-w-0">{action}</div>}
    </header>
  )
}
