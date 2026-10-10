import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { narrowWeek } from '@/features/timetable/model'
import { WeekView } from '@/features/timetable/week-view'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { useCampusNow } from '@/shared/lib/dates'
import { Dialog, DialogContent, DialogDescription, DialogTitle } from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Skeleton } from '@/shared/ui/skeleton'
import { OneOffChanges } from './one-off-changes'
import { useWeekPreviewQuery, type Scope, type Timetable } from './queries'

/** A-02.7 — a concrete week as the students and teachers will see it: holidays, one-off changes and all. */
export function WeekPreview({
  timetableId,
  scope,
  status,
  sourceClasses,
  onCreateSource,
  onClose,
}: {
  timetableId: string
  scope: Scope | undefined
  status: Timetable['status']
  sourceClasses: readonly { value: string; label: string }[]
  onCreateSource: () => void
  onClose: () => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const [date, setDate] = useState(now.date)
  const [adding, setAdding] = useState(false)
  const canAdd = status === 'DRAFT' || status === 'PUBLISHED'
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
        <div className="grid max-h-96 min-w-0 grid-cols-1 gap-4 overflow-y-auto">
          {canAdd && sourceClasses.length === 0 && (
            <EmptyState
              title={t('builder.noSourceClass')}
              action={
                <Button variant="outline" onClick={onCreateSource}>
                  {status === 'DRAFT' ? t('builder.addClass') : t('builder.copyToDraft')}
                </Button>
              }
            />
          )}
          {canAdd && sourceClasses.length > 0 && !adding && (
            <Button
              className="justify-self-start"
              onClick={() => {
                setAdding(true)
              }}
            >
              {t('builder.addSingleClass')}
            </Button>
          )}
          {canAdd && sourceClasses.length > 0 && adding && (
            <OneOffChanges key={date} initialKind="ADD" selectedDate={date} sourceClasses={sourceClasses} />
          )}
          {week.isLoading && <Skeleton className="h-96" />}
          {week.isError && (
            <ErrorState
              problem={toApiProblem(week.error)}
              onRetry={() => {
                void week.refetch()
              }}
            />
          )}
          {week.isSuccess && (
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
