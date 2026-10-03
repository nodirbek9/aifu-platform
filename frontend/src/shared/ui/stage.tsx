import type { ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type StageProps = {
  /** How much larger than the page everything is drawn: type, code, spacing alike. */
  zoom: number
  children: ReactNode
  className?: string
}

/**
 * Content magnified for a projector. The whole subtree is scaled as one — text, code, images and
 * gaps keep their proportions — and it is always the dark theme: high contrast on a wall.
 */
export function Stage({ zoom, children, className }: StageProps) {
  return (
    <div className={cn('dark bg-surface text-text', className)}>
      <div style={{ zoom }}>{children}</div>
    </div>
  )
}
