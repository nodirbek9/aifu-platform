import type { ReactNode } from 'react'
import { Inbox } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type EmptyStateProps = {
  title: ReactNode
  body?: ReactNode
  action?: ReactNode
  icon?: ReactNode
  className?: string
}

/** Empty always offers the next action; never a blank area. */
export function EmptyState({ title, body, action, icon, className }: EmptyStateProps) {
  return (
    <div
      className={cn(
        'border-border-strong bg-surface-raised flex flex-col items-start gap-3 rounded-lg border border-dashed px-6 py-10',
        className,
      )}
    >
      <div className="text-text-muted">{icon ?? <Inbox aria-hidden className="size-7" />}</div>
      <div className="grid gap-1">
        <p className="font-display text-title text-text font-semibold">{title}</p>
        {body !== undefined && <p className="text-small text-text-muted max-w-prose">{body}</p>}
      </div>
      {action !== undefined && <div className="mt-1">{action}</div>}
    </div>
  )
}
