import { CalendarDays } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { shiftDate, useCampusNow } from '@/shared/lib/dates'
import { SESSION_TYPES } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { sessionBar } from '@/shared/ui/session-type'
import { Skeleton } from '@/shared/ui/skeleton'
import type { Week } from './model'
import { useMyWeekQuery } from './queries'
import { WeekSwitch } from './week-switch'
import { WeekView } from './week-view'

const DATE = /^\d{4}-\d{2}-\d{2}$/

/** S-02 — my timetable: the week of my group, as the server has it after every change. */
export function StudentTimetablePage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const [search, setSearch] = useSearchParams()
  const requested = search.get('date')
  const date = requested !== null && DATE.test(requested) ? requested : undefined
  const week = useMyWeekQuery('student', date, locale)

  const goTo = (next: string | undefined) => {
    setSearch(next === undefined ? {} : { date: next })
  }

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.timetable')} context={week.data?.termName} />

      {week.isPending && (
        <div className="grid gap-4">
          <Skeleton className="h-12 w-72" />
          <Skeleton className="h-96" />
        </div>
      )}
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
          <div className={cn('grid gap-4', week.isPlaceholderData && 'opacity-60')}>
            {week.data.slots.length === 0 ? (
              <EmptyState
                icon={<CalendarDays aria-hidden className="size-7" />}
                title={t('timetable.noTimetableTitle')}
                body={t('timetable.noTimetableBody')}
              />
            ) : (
              <>
                <WeekView
                  week={week.data}
                  audience="student"
                  lessonHref={(courseId, lessonId) => `/student/courses/${courseId}/lessons/${lessonId}`}
                  courseHref={(courseId) => `/student/courses/${courseId}`}
                />
                {!hasClasses(week.data) && (
                  <p className="text-small text-text-muted">{t('timetable.emptyWeek')}</p>
                )}
                <Legend week={week.data} />
              </>
            )}
          </div>
        </>
      )}
    </div>
  )
}

function hasClasses(week: Week): boolean {
  return week.days.some((day) => day.classes.length > 0)
}

/** The colours of the session types that appear in this week, named. */
function Legend({ week }: { week: Week }) {
  const { t } = useTranslation()
  const present = SESSION_TYPES.filter((type) =>
    week.days.some((day) => day.classes.some((item) => item.sessionType === type)),
  )
  if (present.length === 0) return null
  return (
    <ul
      aria-label={t('timetable.legend')}
      className="text-small text-text-muted flex flex-wrap gap-x-5 gap-y-2"
    >
      {present.map((type) => (
        <li key={type} className="flex items-center gap-2">
          <span aria-hidden className={cn('size-3 rounded-full', sessionBar[type])} />
          {t(`sessionType.${type}`)}
        </li>
      ))}
    </ul>
  )
}
