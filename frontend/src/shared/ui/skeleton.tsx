import type { HTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'

export type SkeletonProps = HTMLAttributes<HTMLDivElement>

/** A loading placeholder in the shape of the content that will arrive. */
export function Skeleton({ className, ...props }: SkeletonProps) {
  return (
    <div aria-hidden className={cn('bg-surface-sunken animate-pulse rounded-sm', className)} {...props} />
  )
}

/** Text-shaped skeleton lines. */
export function SkeletonText({ lines = 3, className }: { lines?: number; className?: string }) {
  return (
    <div aria-hidden className={cn('grid gap-2.5', className)}>
      {Array.from({ length: lines }, (_, index) => (
        <div
          key={index}
          className={cn(
            'bg-surface-sunken h-3.5 animate-pulse rounded-sm',
            index === lines - 1 ? 'w-2/3' : 'w-full',
          )}
        />
      ))}
    </div>
  )
}
