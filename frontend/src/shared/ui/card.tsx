import type { HTMLAttributes, ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type CardProps = HTMLAttributes<HTMLDivElement>

export function Card({ className, ...props }: CardProps) {
  return <div className={cn('border-border bg-surface-raised rounded-lg border', className)} {...props} />
}

export type CardHeaderProps = {
  title: ReactNode
  description?: ReactNode
  action?: ReactNode
  className?: string
}

export function CardHeader({ title, description, action, className }: CardHeaderProps) {
  return (
    <div className={cn('flex items-start justify-between gap-4 px-5 pt-4 pb-3', className)}>
      <div className="grid gap-0.5">
        <h3 className="font-display text-title text-text font-semibold">{title}</h3>
        {description !== undefined && <p className="text-small text-text-muted">{description}</p>}
      </div>
      {action !== undefined && <div className="shrink-0">{action}</div>}
    </div>
  )
}

export type CardContentProps = HTMLAttributes<HTMLDivElement>

export function CardContent({ className, ...props }: CardContentProps) {
  return <div className={cn('px-5 pb-5', className)} {...props} />
}
