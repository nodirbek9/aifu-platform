import { Check } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { Tooltip } from './tooltip'

export type StepStatus = 'DONE' | 'TODO' | 'NONE'

export type LessonSteps = {
  lecture: StepStatus
  test: StepStatus
  practice: StepStatus
}

export type StepKey = 'lecture' | 'test' | 'practice'

const STEP_KEYS: readonly StepKey[] = ['lecture', 'test', 'practice']

export type StepDotsProps = {
  steps: LessonSteps
  className?: string
  size?: 'sm' | 'md'
}

export function StepDots({ steps, className, size = 'md' }: StepDotsProps) {
  const { t } = useTranslation()

  return (
    <div
      role="group"
      aria-label={t('steps.groupLabel')}
      className={cn('inline-flex items-center gap-1.5', className)}
    >
      {STEP_KEYS.map((key) => {
        const status = steps[key]
        const statusText =
          status === 'DONE'
            ? t('steps.statusDone')
            : status === 'TODO'
              ? t('steps.statusTodo')
              : t('steps.statusNone')
        const stepName = t(`steps.${key}`)
        const label = t('steps.tooltip', { step: stepName, status: statusText })

        return (
          <Tooltip key={key} content={label}>
            <span
              tabIndex={0}
              aria-label={label}
              className={cn(
                'focus-visible:outline-focus-ring inline-flex shrink-0 items-center justify-center rounded-full transition-colors focus-visible:outline-2 focus-visible:outline-offset-2',
                size === 'sm' && 'size-4',
                size === 'md' && 'size-5',
                status === 'DONE' && 'bg-success text-white',
                status === 'TODO' && 'border-primary bg-surface-raised text-primary border-2',
                status === 'NONE' && 'border-border-strong/60 border border-dashed bg-transparent opacity-40',
              )}
            >
              {status === 'DONE' && (
                <Check
                  aria-hidden
                  className={cn('stroke-[2.5]', size === 'sm' && 'size-2.5', size === 'md' && 'size-3')}
                />
              )}
            </span>
          </Tooltip>
        )
      })}
    </div>
  )
}
