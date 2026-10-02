import { useEffect, useRef } from 'react'
import { Timer } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { clockTone, formatClock } from './model'
import { useRemainingSeconds, type Clocked } from './use-remaining-seconds'

type CountdownProps = {
  attempt: Clocked
  /** Called once, when the time is over. */
  onElapsed?: () => void
  className?: string
}

/**
 * The quiz clock. Calm until the last two minutes, then the warning colour, then red for the
 * last half minute. A screen reader hears it only at five minutes and at one minute.
 */
export function Countdown({ attempt, onElapsed, className }: CountdownProps) {
  const { t } = useTranslation()
  const remaining = useRemainingSeconds(attempt)
  const elapsed = useRef(false)
  const whenElapsed = useRef(onElapsed)

  useEffect(() => {
    whenElapsed.current = onElapsed
  }, [onElapsed])

  useEffect(() => {
    if (remaining === 0 && !elapsed.current) {
      elapsed.current = true
      whenElapsed.current?.()
    }
  }, [remaining])

  if (remaining === undefined) return null
  const tone = clockTone(remaining)
  const spoken = remaining <= 60 ? t('quiz.oneMinuteLeft') : remaining <= 300 ? t('quiz.fiveMinutesLeft') : ''

  return (
    <>
      <span
        role="timer"
        aria-label={t('quiz.timeLeft')}
        className={cn(
          'text-body flex h-9 shrink-0 items-center gap-1.5 rounded-sm px-2.5 font-semibold tabular-nums',
          'duration-normal ease-standard transition-[background-color,color]',
          tone === 'calm' && 'bg-surface-sunken text-text',
          tone === 'warning' && 'bg-warning-soft text-warning',
          tone === 'danger' && 'bg-danger-soft text-danger',
          className,
        )}
      >
        <Timer aria-hidden className="size-4" />
        {formatClock(remaining)}
      </span>
      <span role="status" className="sr-only">
        {spoken}
      </span>
    </>
  )
}
