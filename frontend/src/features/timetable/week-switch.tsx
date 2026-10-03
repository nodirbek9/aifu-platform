import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateRange } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { IconButton } from '@/shared/ui/icon-button'
import type { Week } from './model'

type WeekSwitchProps = {
  week: Week
  /** The week on screen is the one today falls in. */
  isCurrent: boolean
  onPrevious: () => void
  onNext: () => void
  onToday: () => void
}

/**
 * S-02.2 — which week this is and the way to another: the dates, the number of the week in the
 * term, and odd / even as a chip. The number and the parity are the server's.
 */
export function WeekSwitch({ week, isCurrent, onPrevious, onNext, onToday }: WeekSwitchProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  return (
    <div className="flex flex-wrap items-center gap-x-4 gap-y-3">
      <div data-print-hidden className="flex items-center gap-1">
        <IconButton variant="outline" aria-label={t('timetable.previousWeek')} onClick={onPrevious}>
          <ChevronLeft aria-hidden className="size-5" />
        </IconButton>
        <IconButton variant="outline" aria-label={t('timetable.nextWeek')} onClick={onNext}>
          <ChevronRight aria-hidden className="size-5" />
        </IconButton>
      </div>
      <div className="grid min-w-0 gap-0.5">
        <p aria-live="polite" className="font-display text-title text-text font-semibold">
          {formatDateRange(week.weekStart, week.weekEnd, locale)}
        </p>
        <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
          {week.weekNumber !== undefined && (
            <span>{t('timetable.weekNumber', { number: week.weekNumber })}</span>
          )}
          {week.parity !== undefined && (
            <Badge tone={week.parity === 'ODD' ? 'info' : 'accent'}>
              {t(`timetable.parity.${week.parity}`)}
            </Badge>
          )}
        </p>
      </div>
      <div className="flex-1" />
      {!isCurrent && (
        <Button variant="outline" onClick={onToday}>
          {t('timetable.thisWeek')}
        </Button>
      )}
    </div>
  )
}
