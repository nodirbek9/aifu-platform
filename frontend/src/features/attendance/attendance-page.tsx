import { UserCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatLongDay } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { Card, CardContent } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { ProgressRing } from '@/shared/ui/progress'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Skeleton } from '@/shared/ui/skeleton'
import { isMissed, type AttendanceRecord, type AttendanceStatus, type CourseAttendance } from './model'
import { useMyAttendanceQuery } from './queries'

const statusTone = {
  PRESENT: 'success',
  LATE: 'warning',
  EXCUSED: 'info',
  ABSENT: 'danger',
} as const satisfies Record<AttendanceStatus, 'success' | 'warning' | 'info' | 'danger'>

/** S-10 — my attendance: per course what was held, what I attended, and every class I missed. */
export function AttendancePage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const attendance = useMyAttendanceQuery(locale)
  const [search, setSearch] = useSearchParams()
  const missedOnly = search.get('show') === 'missed'

  return (
    <div className="grid gap-6">
      <PageHeader
        title={t('nav.attendance')}
        action={
          <SegmentedControl
            aria-label={t('attendance.show')}
            value={missedOnly ? 'missed' : 'all'}
            onValueChange={(value) => {
              setSearch(value === 'missed' ? { show: 'missed' } : {}, { replace: true })
            }}
            options={[
              { value: 'all', label: t('assignments.all') },
              { value: 'missed', label: t('attendance.missed') },
            ]}
          />
        }
      />

      {attendance.isPending && (
        <div className="grid gap-4">
          <Skeleton className="h-48" />
          <Skeleton className="h-48" />
        </div>
      )}
      {attendance.isError && (
        <ErrorState
          problem={toApiProblem(attendance.error)}
          onRetry={() => {
            void attendance.refetch()
          }}
        />
      )}
      {attendance.isSuccess && attendance.data.length === 0 && (
        <EmptyState
          icon={<UserCheck aria-hidden className="size-7" />}
          title={t('attendance.emptyTitle')}
          body={t('attendance.emptyBody')}
        />
      )}
      {attendance.isSuccess &&
        attendance.data.map((course) => (
          <CourseBlock key={course.courseId} course={course} missedOnly={missedOnly} />
        ))}
    </div>
  )
}

function CourseBlock({ course, missedOnly }: { course: CourseAttendance; missedOnly: boolean }) {
  const { t } = useTranslation()
  const { totals } = course
  const records = missedOnly ? course.records.filter((record) => isMissed(record.status)) : course.records
  const counts = [
    { label: t('attendance.held'), value: totals.held },
    { label: t('attendance.status.PRESENT'), value: totals.present },
    { label: t('attendance.status.LATE'), value: totals.late },
    { label: t('attendance.status.EXCUSED'), value: totals.excused },
    { label: t('attendance.status.ABSENT'), value: totals.absent, danger: totals.absent > 0 },
  ]

  return (
    <Card>
      <CardContent className="grid gap-5 pt-5">
        <header className="flex flex-wrap items-center gap-x-6 gap-y-4">
          <ProgressRing value={totals.percent} label={t('attendance.percent', { percent: totals.percent })} />
          <div className="grid min-w-0 flex-1 gap-2">
            <h2 className="font-display text-title text-text font-semibold">
              <Link
                to={`/student/courses/${course.courseId}`}
                className="focus-visible:outline-focus-ring rounded-sm hover:underline focus-visible:outline-2 focus-visible:outline-offset-2"
              >
                {course.courseTitle}
              </Link>
            </h2>
            <dl className="flex flex-wrap gap-x-6 gap-y-2">
              {counts.map((count) => (
                <div key={count.label} className="grid">
                  <dt className="text-caption text-text-muted font-medium uppercase">{count.label}</dt>
                  <dd
                    className={cn(
                      'font-display text-body font-semibold tabular-nums',
                      count.danger === true ? 'text-danger' : 'text-text',
                    )}
                  >
                    {count.value}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        </header>

        {records.length === 0 ? (
          <p className="text-small text-text-muted border-border border-t pt-4">
            {missedOnly ? t('attendance.noneMissed') : t('attendance.noneHeld')}
          </p>
        ) : (
          <ol className="divide-border border-border divide-y border-t">
            {records.map((record) => (
              <RecordRow
                key={`${record.date}:${record.pairNo ?? 0}:${record.sessionType ?? ''}`}
                record={record}
              />
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  )
}

function RecordRow({ record }: { record: AttendanceRecord }) {
  const { t } = useTranslation()
  const locale = useLocale()
  return (
    <li className="flex flex-wrap items-start gap-x-4 gap-y-1 py-3">
      <div className="grid min-w-0 flex-1 gap-1">
        <p className="text-small text-text font-medium">
          {formatLongDay(record.date, locale)}
          {record.pairNo !== undefined && (
            <span className="text-text-muted font-normal">
              {' '}
              · {t('timetable.pair', { number: record.pairNo })}
            </span>
          )}
        </p>
        {record.topic !== undefined && <p className="text-small text-text-muted">{record.topic}</p>}
      </div>
      <div className="flex shrink-0 items-center gap-2">
        {record.sessionType !== undefined && (
          <SessionTypeBadge type={record.sessionType} label={t(`sessionType.${record.sessionType}`)} />
        )}
        <Badge tone={statusTone[record.status]}>{t(`attendance.status.${record.status}`)}</Badge>
      </div>
    </li>
  )
}
