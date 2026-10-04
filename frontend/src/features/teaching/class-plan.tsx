import { CalendarRange, Check, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { isHeld } from '@/features/timetable/model'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatLongDay, formatTimeOfDay } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useClassPlanQuery } from './queries'

/**
 * T-04.3 — the calendar-thematic plan: every class of the term by date, with the topic it has
 * or still needs. Every held class opens its class page, where the topic is set: the course's
 * teachers work together, so a seminar teacher can see and fill in the lecture's page too.
 */
export function ClassPlan({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const plan = useClassPlanQuery(courseId, locale)

  if (plan.isPending) return <Skeleton className="h-96" />
  if (plan.isError) {
    return (
      <ErrorState
        problem={toApiProblem(plan.error)}
        onRetry={() => {
          void plan.refetch()
        }}
      />
    )
  }
  if (plan.data.length === 0) {
    return (
      <EmptyState
        icon={<CalendarRange aria-hidden className="size-7" />}
        title={t('teaching.planEmptyTitle')}
        body={t('teaching.planEmptyBody')}
      />
    )
  }
  const withTopic = plan.data.filter((row) => row.lesson !== undefined || row.topic !== undefined).length

  return (
    <div className="grid gap-3">
      <p className="text-small text-text-muted">
        {t('teaching.planSummary', { total: plan.data.length, withTopic })}
      </p>
      <Table>
        <TableHead>
          <tr>
            <TableHeaderCell className="w-12">{t('teaching.week')}</TableHeaderCell>
            <TableHeaderCell>{t('timetable.time')}</TableHeaderCell>
            <TableHeaderCell>{t('timetable.topic')}</TableHeaderCell>
            <TableHeaderCell className="hidden md:table-cell">{t('teaching.attendance')}</TableHeaderCell>
            <TableHeaderCell className="w-8">
              <span className="sr-only">{t('teaching.open')}</span>
            </TableHeaderCell>
          </tr>
        </TableHead>
        <TableBody>
          {plan.data.map((row) => {
            const held = isHeld(row.info.status)
            return (
              <TableRow
                key={`${row.info.entryId}:${row.info.date}`}
                className={cn(!held && 'bg-surface-sunken')}
              >
                <TableCell className="text-text-muted tabular-nums">{row.weekNumber ?? '—'}</TableCell>
                <TableCell className="whitespace-nowrap">
                  <span className="grid">
                    <span className="text-text">{formatLongDay(row.info.date, locale)}</span>
                    <span className="text-text-muted flex items-center gap-2">
                      {formatTimeOfDay(row.info.startsAt)}
                      <SessionTypeBadge
                        type={row.info.sessionType}
                        label={t(`sessionType.${row.info.sessionType}`)}
                      />
                      {row.info.groups.join(', ')}
                    </span>
                  </span>
                </TableCell>
                <TableCell>
                  {!held ? (
                    <Badge tone="danger">{t(`classStatus.${row.info.status}`)}</Badge>
                  ) : row.lesson !== undefined ? (
                    <span className="text-text">{row.lesson.title}</span>
                  ) : row.topic !== undefined ? (
                    <span className="text-text">{row.topic}</span>
                  ) : (
                    <span className="text-text-muted">{t('teaching.noTopicYet')}</span>
                  )}
                </TableCell>
                <TableCell className="hidden md:table-cell">
                  {row.attendanceMarked && (
                    <span className="text-success inline-flex items-center gap-1">
                      <Check aria-hidden className="size-4" />
                      {t('teaching.taken')}
                    </span>
                  )}
                </TableCell>
                <TableCell>
                  {held && (
                    <Link
                      to={`/teacher/classes/${row.info.entryId}/${row.info.date}`}
                      aria-label={t('teaching.openClass', { date: formatLongDay(row.info.date, locale) })}
                      className="text-text-muted hover:text-text focus-visible:outline-focus-ring flex size-9 items-center justify-center rounded-sm focus-visible:outline-2"
                    >
                      <ChevronRight aria-hidden className="size-5" />
                    </Link>
                  )}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
