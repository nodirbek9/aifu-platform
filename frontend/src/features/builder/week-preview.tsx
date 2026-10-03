import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { narrowWeek } from '@/features/timetable/model'
import { WeekView } from '@/features/timetable/week-view'
import { useLocale } from '@/shared/i18n/use-locale'
import { useCampusNow } from '@/shared/lib/dates'
import { Dialog, DialogContent, DialogDescription, DialogTitle } from '@/shared/ui/dialog'
import { Input } from '@/shared/ui/input'
import { Skeleton } from '@/shared/ui/skeleton'
import { useWeekPreviewQuery, type Scope } from './queries'

/** A-02.7 — a concrete week as the students and teachers will see it: holidays, one-off changes and all. */
export function WeekPreview({
  timetableId,
  scope,
  onClose,
}: {
  timetableId: string
  scope: Scope | undefined
  onClose: () => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const [date, setDate] = useState(now.date)
  const week = useWeekPreviewQuery(timetableId, scope, date, locale, true)
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent className="max-w-6xl">
        <DialogTitle>{t('builder.weekPreview')}</DialogTitle>
        <DialogDescription>{t('builder.weekPreviewBody')}</DialogDescription>
        <Input
          className="w-48"
          label={t('builder.weekOf')}
          type="date"
          value={date}
          onChange={(event) => {
            if (event.target.value !== '') setDate(event.target.value)
          }}
        />
        <div className="max-h-[65vh] overflow-y-auto">
          {week.isPending && <Skeleton className="h-96" />}
          {week.data !== undefined && (
            <WeekView
              week={narrowWeek(week.data)}
              audience="teacher"
              lessonHref={() => '#'}
              courseHref={(courseId) => `/admin/structure/courses/${courseId}`}
            />
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
