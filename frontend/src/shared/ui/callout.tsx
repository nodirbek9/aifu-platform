import type { ReactNode } from 'react'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'
import { AlertTriangle, Info, Lightbulb, OctagonAlert } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export const callout = tv({
  base: 'flex gap-3 rounded-md border border-border border-l-4 px-4 py-3',
  variants: {
    variant: {
      INFO: 'border-l-info bg-callout-info-surface',
      TIP: 'border-l-success bg-callout-tip-surface',
      WARNING: 'border-l-warning bg-callout-warning-surface',
      DANGER: 'border-l-danger bg-callout-danger-surface',
    },
  },
  defaultVariants: { variant: 'INFO' },
})

const calloutIcon = {
  INFO: { Icon: Info, className: 'text-info' },
  TIP: { Icon: Lightbulb, className: 'text-success' },
  WARNING: { Icon: AlertTriangle, className: 'text-warning' },
  DANGER: { Icon: OctagonAlert, className: 'text-danger' },
} as const

export type CalloutProps = VariantProps<typeof callout> & {
  title?: ReactNode
  children: ReactNode
  className?: string
}

/** Distinct by icon and colour bar, never by a loud background (brief §9). */
export function Callout({ variant = 'INFO', title, children, className }: CalloutProps) {
  const { Icon, className: iconClass } = calloutIcon[variant ?? 'INFO']
  return (
    <aside className={cn(callout({ variant }), className)}>
      <Icon aria-hidden className={cn('mt-0.5 size-5 shrink-0', iconClass)} />
      <div className="grid gap-1">
        {title !== undefined && <div className="text-small text-text font-semibold">{title}</div>}
        <div className="text-small text-text [&_a]:text-primary-hover [&_a]:underline">{children}</div>
      </div>
    </aside>
  )
}
