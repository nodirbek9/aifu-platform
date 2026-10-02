import { useId } from 'react'
import * as ProgressPrimitive from '@radix-ui/react-progress'
import { cn } from '@/shared/lib/cn'

export type ProgressBarProps = {
  value: number
  max?: number
  label?: string
  className?: string
}

export function ProgressBar({ value, max = 100, label, className }: ProgressBarProps) {
  const percent = Math.min(100, Math.max(0, (value / max) * 100))
  return (
    <div className={cn('flex items-center gap-3', className)}>
      <ProgressPrimitive.Root
        value={value}
        max={max}
        aria-label={label}
        className="bg-surface-sunken relative h-1.5 flex-1 overflow-hidden rounded-full"
      >
        <ProgressPrimitive.Indicator
          className="bg-primary duration-slow ease-emphasized h-full rounded-full transition-[width]"
          style={{ width: `${percent}%` }}
        />
      </ProgressPrimitive.Root>
      {label !== undefined && <span className="text-caption text-text-muted tabular-nums">{label}</span>}
    </div>
  )
}

export type ProgressRingProps = {
  value: number
  max?: number
  size?: number
  label?: string
  className?: string
}

export function ProgressRing({ value, max = 100, size = 56, label, className }: ProgressRingProps) {
  const id = useId()
  const percent = Math.min(1, Math.max(0, value / max))
  const strokeWidth = 4
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  return (
    <div className={cn('relative inline-flex items-center justify-center', className)}>
      <svg
        width={size}
        height={size}
        role="img"
        aria-label={label ?? `${Math.round(percent * 100)}%`}
        className="-rotate-90"
      >
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={strokeWidth}
          className="stroke-surface-sunken"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={circumference * (1 - percent)}
          className="stroke-primary duration-slower ease-emphasized transition-[stroke-dashoffset]"
        />
      </svg>
      <span id={id} className="text-caption text-text absolute font-medium tabular-nums">
        {Math.round(percent * 100)}%
      </span>
    </div>
  )
}
