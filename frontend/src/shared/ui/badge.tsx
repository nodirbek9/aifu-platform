import type { ReactNode } from 'react'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  FileWarning,
  Gauge,
  type Info,
  MemoryStick,
  Timer,
  XCircle,
} from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export const badge = tv({
  base: 'inline-flex items-center gap-1.5 rounded-sm px-2 py-0.5 text-caption font-medium tracking-normal',
  variants: {
    tone: {
      neutral: 'bg-surface-sunken text-text-muted',
      success: 'bg-success-soft text-success',
      warning: 'bg-warning-soft text-warning',
      danger: 'bg-danger-soft text-danger',
      info: 'bg-info-soft text-info',
      accent: 'bg-accent-soft text-accent-text',
      lecture: 'bg-session-lecture-tint text-session-lecture-strong',
      seminar: 'bg-session-seminar-tint text-session-seminar-strong',
      practical: 'bg-session-practical-tint text-session-practical-strong',
      laboratory: 'bg-session-laboratory-tint text-session-laboratory-strong',
      independent: 'bg-session-independent-tint text-session-independent-strong',
      coursework: 'bg-session-coursework-tint text-session-coursework-strong',
    },
  },
  defaultVariants: { tone: 'neutral' },
})

export type BadgeProps = VariantProps<typeof badge> & {
  children: ReactNode
  className?: string
}

type BadgeTone = NonNullable<VariantProps<typeof badge>['tone']>

export function Badge({ tone, children, className }: BadgeProps) {
  return <span className={badge({ tone, class: className })}>{children}</span>
}

export const SESSION_TYPES = [
  'LECTURE',
  'SEMINAR',
  'PRACTICAL',
  'LABORATORY',
  'INDEPENDENT_STUDY',
  'COURSE_WORK',
] as const
export type SessionType = (typeof SESSION_TYPES)[number]

const sessionTone = {
  LECTURE: 'lecture',
  SEMINAR: 'seminar',
  PRACTICAL: 'practical',
  LABORATORY: 'laboratory',
  INDEPENDENT_STUDY: 'independent',
  COURSE_WORK: 'coursework',
} as const satisfies Record<SessionType, BadgeTone>

export type SessionTypeBadgeProps = {
  type: SessionType
  label: string
  className?: string
}

export function SessionTypeBadge({ type, label, className }: SessionTypeBadgeProps) {
  return <span className={badge({ tone: sessionTone[type], class: className })}>{label}</span>
}

export const VERDICTS = [
  'OK',
  'ACCEPTED',
  'PARTIAL',
  'WRONG_ANSWER',
  'COMPILATION_ERROR',
  'RUNTIME_ERROR',
  'TIME_LIMIT_EXCEEDED',
  'MEMORY_LIMIT_EXCEEDED',
  'OUTPUT_LIMIT_EXCEEDED',
  'INTERNAL_ERROR',
] as const
export type Verdict = (typeof VERDICTS)[number]

type VerdictMeta = {
  tone: BadgeTone
  Icon: typeof Info
}

const verdictMeta = {
  OK: { tone: 'success', Icon: CheckCircle2 },
  ACCEPTED: { tone: 'success', Icon: CheckCircle2 },
  PARTIAL: { tone: 'warning', Icon: AlertTriangle },
  WRONG_ANSWER: { tone: 'danger', Icon: XCircle },
  COMPILATION_ERROR: { tone: 'danger', Icon: FileWarning },
  RUNTIME_ERROR: { tone: 'danger', Icon: AlertTriangle },
  TIME_LIMIT_EXCEEDED: { tone: 'danger', Icon: Timer },
  MEMORY_LIMIT_EXCEEDED: { tone: 'danger', Icon: MemoryStick },
  OUTPUT_LIMIT_EXCEEDED: { tone: 'danger', Icon: Gauge },
  INTERNAL_ERROR: { tone: 'danger', Icon: XCircle },
} satisfies Record<Verdict, VerdictMeta>

export type VerdictBadgeProps = {
  verdict: Verdict
  label: string
  className?: string
}

export function VerdictBadge({ verdict, label, className }: VerdictBadgeProps) {
  const meta: VerdictMeta = verdictMeta[verdict]
  return (
    <span className={badge({ tone: meta.tone, class: cn('font-mono', className) })}>
      <meta.Icon aria-hidden className="size-3.5" />
      {label}
    </span>
  )
}

/** The "queued / running" marker while a run or submission is in flight. */
export function PendingBadge({ label, className }: { label: string; className?: string }) {
  return (
    <span className={badge({ tone: 'info', class: className })}>
      <Clock3 aria-hidden className="size-3.5" />
      {label}
    </span>
  )
}
