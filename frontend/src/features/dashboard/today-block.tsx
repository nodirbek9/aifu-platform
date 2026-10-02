import { CalendarDays } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { ClassRow } from '@/features/timetable/class-row'
import { classKey, currentOrNext } from '@/features/timetable/model'
import { useNextClassQuery, useTodayQuery } from '@/features/timetable/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatLongDay, formatTimeOfDay, useCampusNow } from '@/shared/lib/dates'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton } from '@/shared/ui/skeleton'

/** S-01.1 — today's classes as a timeline; the class going on, or the next one, is the marked one. */
export function TodayBlock() {
  const { t } = useTranslation()
  const today = useTodayQuery()
  const now = useCampusNow()

  return (
    <Card>
      <CardHeader
        title={t('dashboard.todayTitle')}
        action={
          <Link
            to="/student/timetable"
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('dashboard.openTimetable')}
          </Link>
        }
      />
      <CardContent>
        {today.isPending && (
          <div className="grid gap-4">
            <Skeleton className="h-16" />
            <Skeleton className="h-16" />
          </div>
        )}
        {today.isError && (
          <ErrorState
            problem={toApiProblem(today.error)}
            onRetry={() => {
              void today.refetch()
            }}
          />
        )}
        {today.isSuccess && today.data.classes.length === 0 && (
          <EmptyState
            icon={<CalendarDays aria-hidden className="size-7" />}
            title={
              today.data.holiday !== undefined
                ? t('dashboard.holiday', { name: today.data.holiday })
                : t('dashboard.todayEmptyTitle')
            }
            body={<NextClass />}
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {today.isSuccess && today.data.classes.length > 0 && (
          <ol className="divide-border -my-3 divide-y">
            {today.data.classes.map((item) => (
              <ClassRow
                key={classKey(item)}
                item={item}
                now={now}
                marked={currentOrNext(today.data.classes, now) === item}
              />
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  )
}

/** S-01.1 — on a day without classes: when the next one is. */
function NextClass() {
  const { t } = useTranslation()
  const locale = useLocale()
  const next = useNextClassQuery()
  if (next.data === undefined || next.data === null) return t('dashboard.todayEmptyBody')
  return t('dashboard.nextClass', {
    day: formatLongDay(next.data.date, locale),
    time: formatTimeOfDay(next.data.startsAt),
    title: next.data.courseTitle,
  })
}
