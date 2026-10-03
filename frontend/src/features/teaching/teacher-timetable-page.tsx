import { Printer } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useSearchParams } from 'react-router'
import { isHeld } from '@/features/timetable/model'
import { useMyWeekQuery } from '@/features/timetable/queries'
import { WeekSwitch } from '@/features/timetable/week-switch'
import { WeekView } from '@/features/timetable/week-view'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { shiftDate, useCampusNow } from '@/shared/lib/dates'
import { Button } from '@/shared/ui/button'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Skeleton } from '@/shared/ui/skeleton'
import { HOURS_PER_PAIR } from './model'

const DATE = /^\d{4}-\d{2}-\d{2}$/

/**
 * T-02 — my weekly timetable: the classes I teach with their groups and rooms, the hours of the
 * week, toq / juft, and a page that prints cleanly on A4 landscape. A class opens its class page.
 */
export function TeacherTimetablePage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const navigate = useNavigate()
  const [search, setSearch] = useSearchParams()
  const requested = search.get('date')
  const date = requested !== null && DATE.test(requested) ? requested : undefined
  const week = useMyWeekQuery('teacher', date, locale)

  const goTo = (next: string | undefined) => {
    setSearch(next === undefined ? {} : { date: next })
  }
  const pairs =
    week.data?.days.flatMap((day) => day.classes).filter((item) => isHeld(item.status)).length ?? 0

  return (
    <div className="grid gap-6 print:gap-3">
      <PageHeader
        title={t('nav.timetable')}
        context={
          week.data === undefined
            ? undefined
            : t('teaching.weekLoad', { pairs, hours: pairs * HOURS_PER_PAIR })
        }
        action={
          <Button
            variant="outline"
            className="print:hidden"
            onClick={() => {
              window.print()
            }}
          >
            <Printer aria-hidden className="size-4" />
            {t('teaching.print')}
          </Button>
        }
      />
      {week.isPending && <Skeleton className="h-96" />}
      {week.isError && (
        <ErrorState
          problem={toApiProblem(week.error)}
          onRetry={() => {
            void week.refetch()
          }}
        />
      )}
      {week.isSuccess && (
        <>
          <WeekSwitch
            week={week.data}
            isCurrent={week.data.weekStart <= now.date && now.date <= week.data.weekEnd}
            onPrevious={() => {
              goTo(shiftDate(week.data.weekStart, -7))
            }}
            onNext={() => {
              goTo(shiftDate(week.data.weekStart, 7))
            }}
            onToday={() => {
              goTo(undefined)
            }}
          />
          <div className={cn(week.isPlaceholderData && 'opacity-60')}>
            <WeekView
              week={week.data}
              audience="teacher"
              lessonHref={(_courseId, lessonId) => `/teacher/lessons/${lessonId}/present`}
              courseHref={(courseId) => `/teacher/courses/${courseId}`}
              onOpen={(item) => {
                void navigate(`/teacher/classes/${item.entryId}/${item.date}`)
              }}
            />
          </div>
        </>
      )}
    </div>
  )
}
