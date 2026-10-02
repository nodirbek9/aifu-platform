import { Award, CalendarCheck2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { AnnouncementList } from '@/features/announcements/announcement-list'
import { useMyAnnouncementsQuery } from '@/features/announcements/queries'
import { ActivityRow } from '@/features/courses/activity-row'
import type { Enrollment } from '@/features/courses/model'
import { useActivitiesQuery, useRecentGrades } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDate } from '@/shared/lib/dates'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton } from '@/shared/ui/skeleton'

const SHOWN = 5

function RowsSkeleton() {
  return (
    <div className="grid gap-3">
      <Skeleton className="h-12" />
      <Skeleton className="h-12" />
      <Skeleton className="h-12" />
    </div>
  )
}

/** S-01.3 — the next open quizzes and tasks with a due date; what is overdue comes first, in red. */
export function DeadlinesBlock() {
  const { t } = useTranslation()
  const locale = useLocale()
  const activities = useActivitiesQuery(locale)

  const open = (activities.data ?? [])
    .filter((activity) => activity.dueAt !== undefined && activity.state !== 'COMPLETED')
    .sort((a, b) => (a.dueAt ?? '').localeCompare(b.dueAt ?? ''))
    .slice(0, SHOWN)

  return (
    <Card>
      <CardHeader
        title={t('dashboard.deadlinesTitle')}
        action={
          <Link
            to="/student/assignments"
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('dashboard.allAssignments')}
          </Link>
        }
      />
      <CardContent>
        {activities.isPending && <RowsSkeleton />}
        {activities.isError && (
          <ErrorState
            problem={toApiProblem(activities.error)}
            onRetry={() => {
              void activities.refetch()
            }}
          />
        )}
        {activities.isSuccess && open.length === 0 && (
          <EmptyState
            icon={<CalendarCheck2 aria-hidden className="size-7" />}
            title={t('dashboard.deadlinesEmptyTitle')}
            body={t('dashboard.deadlinesEmptyBody')}
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {open.length > 0 && (
          <ul className="divide-border -my-3 divide-y">
            {open.map((activity) => (
              <li key={activity.activityId}>
                <ActivityRow activity={activity} withCourse />
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

/** S-01.4 — the newest published grades across the student's courses. */
export function RecentGradesBlock({ enrollments }: { enrollments: readonly Enrollment[] | undefined }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const recent = useRecentGrades(enrollments, SHOWN)

  return (
    <Card>
      <CardHeader
        title={t('dashboard.gradesTitle')}
        action={
          <Link
            to="/student/grades"
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('dashboard.allGrades')}
          </Link>
        }
      />
      <CardContent>
        {recent.pending && <RowsSkeleton />}
        {!recent.pending && recent.error !== undefined && recent.error !== null && (
          <ErrorState problem={toApiProblem(recent.error)} onRetry={recent.refetch} />
        )}
        {!recent.pending && !recent.error && recent.grades.length === 0 && (
          <EmptyState
            icon={<Award aria-hidden className="size-7" />}
            title={t('dashboard.gradesEmptyTitle')}
            body={t('dashboard.gradesEmptyBody')}
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {recent.grades.length > 0 && (
          <ul className="divide-border -my-3 divide-y">
            {recent.grades.map((grade) => (
              <li key={grade.id} className="flex items-center justify-between gap-4 py-3">
                <div className="grid min-w-0 gap-0.5">
                  <span className="text-body text-text font-medium">{grade.title ?? grade.subjectCode}</span>
                  <span className="text-small text-text-muted">
                    {grade.title !== undefined && `${grade.subjectCode} · `}
                    {t(`gradeSource.${grade.source}`)} · {formatDate(grade.publishedAt, locale)}
                  </span>
                </div>
                <span className="font-display text-title text-text font-semibold tabular-nums">
                  {grade.score}
                  <span className="text-small text-text-muted font-sans font-normal">
                    {' '}
                    / {grade.maxScore}
                  </span>
                </span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

/** The newest announcements of my courses and my group; the rest is one click away. */
export function AnnouncementsBlock() {
  const { t } = useTranslation()
  const locale = useLocale()
  const announcements = useMyAnnouncementsQuery(locale)
  // nothing announced: the block would be an empty frame under the rest of the day
  if (announcements.isSuccess && announcements.data.length === 0) return null
  return (
    <Card>
      <CardHeader
        title={t('announcements.title')}
        action={
          <Link to="/announcements" className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}>
            {t('announcements.all')}
          </Link>
        }
      />
      <CardContent>
        <AnnouncementList announcements={announcements} withAudience limit={2} />
      </CardContent>
    </Card>
  )
}
