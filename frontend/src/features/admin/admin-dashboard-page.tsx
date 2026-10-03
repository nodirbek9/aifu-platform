import { useQuery } from '@tanstack/react-query'
import { AlertTriangle, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { ClassRow } from '@/features/timetable/class-row'
import { classKey, narrowDay } from '@/features/timetable/model'
import { api, unwrap } from '@/shared/api/client'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime, formatLongDay, useCampusNow } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Skeleton } from '@/shared/ui/skeleton'
import { Stat } from '@/shared/ui/stat'
import { useAuditQuery } from './audit-queries'
import { useAllGroupsQuery, useTimetablesQuery } from './reference-queries'
import { useCoursesQuery } from './structure-queries'
import { useUsersQuery } from './users-queries'

/** Sign-ins and session renewals happen all day: they belong to the log, not to "what happened". */
const ROUTINE = new Set(['LOGIN_SUCCESS', 'TOKEN_REFRESH', 'LOGOUT', 'LOGOUT_ALL'])

/**
 * A-01 — the administrator's first screen: how big the university is in the platform, today's
 * classes of the published timetable, the teaching load still not scheduled, and what happened
 * last.
 */
export function AdminDashboardPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { state } = useAuth()
  const now = useCampusNow()
  const students = useUsersQuery({ role: 'STUDENT', status: 'ACTIVE', page: 0 })
  const teachers = useUsersQuery({ role: 'TEACHER', status: 'ACTIVE', page: 0 })
  const groups = useAllGroupsQuery()
  const courses = useCoursesQuery({ query: '', status: 'PUBLISHED', page: 0 })
  const timetables = useTimetablesQuery()
  const published = timetables.data?.find((item) => item.status === 'PUBLISHED')
  const publishedId = published?.id
  const termId = published?.termId

  const today = useQuery({
    queryKey: ['admin', 'week', publishedId, now.date, locale],
    queryFn: async () => {
      const week = await unwrap(
        api.GET('/api/v1/admin/timetables/{id}/week', {
          params: { path: { id: publishedId ?? '' }, query: { date: now.date, locale } },
        }),
      )
      const day = (week.days ?? []).find((item) => item.date === now.date)
      return day === undefined ? null : narrowDay(day)
    },
    enabled: publishedId !== undefined,
  })
  const load = useQuery({
    queryKey: ['admin', 'teaching-load', termId ?? '', locale],
    queryFn: () =>
      unwrap(api.GET('/api/v1/admin/teaching-load', { params: { query: { termId: termId ?? '', locale } } })),
    enabled: termId !== undefined,
  })
  const audit = useAuditQuery({ page: 0, size: 40 })
  const overridden = useAuditQuery({ action: 'TIMETABLE_CONFLICT_OVERRIDDEN', page: 0, size: 1 })

  const unscheduled = (load.data ?? []).filter((row) => (row.remainingHours ?? 0) > 0)
  const remainingHours = unscheduled.reduce((sum, row) => sum + (row.remainingHours ?? 0), 0)

  return (
    <div className="grid grid-cols-1 gap-8">
      <header className="grid grid-cols-1 gap-1.5">
        <p className="text-caption text-text-muted font-medium uppercase">
          {formatLongDay(now.date, locale)}
        </p>
        <h1 className="font-display text-display-lg text-text md:text-display-xl font-semibold wrap-anywhere">
          {state.status === 'authenticated' ? state.user.fullName : ''}
        </h1>
      </header>

      <section aria-label={t('admin.counts')} className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {[
          {
            label: t('admin.countStudents'),
            value: students.data?.totalElements,
            to: '/admin/users?role=STUDENT',
          },
          {
            label: t('admin.countTeachers'),
            value: teachers.data?.totalElements,
            to: '/admin/users?role=TEACHER',
          },
          {
            label: t('admin.countGroups'),
            value: groups.data?.filter((group) => group.status !== 'ARCHIVED').length,
            to: '/admin/reference',
          },
          {
            label: t('admin.countCourses'),
            value: courses.data?.totalElements,
            to: '/admin/structure?tab=courses&status=PUBLISHED',
          },
        ].map((item) => (
          <Link
            key={item.label}
            to={item.to}
            className="border-border bg-surface-raised hover:border-border-strong focus-visible:outline-focus-ring rounded-lg border p-4 focus-visible:outline-2 focus-visible:outline-offset-2"
          >
            <Stat label={item.label} value={item.value ?? '—'} />
          </Link>
        ))}
      </section>

      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-7">
          <CardHeader
            title={t('admin.todayClasses')}
            description={published === undefined ? t('admin.noPublishedTimetable') : published.name}
            action={
              <Link to="/admin/timetable" className={button({ variant: 'ghost', size: 'sm' })}>
                {t('nav.adminTimetable')}
                <ChevronRight aria-hidden className="size-4" />
              </Link>
            }
          />
          <CardContent>
            {today.isPending && publishedId !== undefined && <Skeleton className="h-40" />}
            {today.data?.holiday !== undefined && <p className="text-body text-text">{today.data.holiday}</p>}
            {today.isSuccess &&
              (today.data === null || today.data.classes.length === 0) &&
              today.data?.holiday === undefined && (
                <p className="text-small text-text-muted">{t('admin.noClassesToday')}</p>
              )}
            <ul className="divide-border grid grid-cols-1 divide-y">
              {today.data?.classes.map((item) => (
                <li key={classKey(item)}>
                  <ClassRow item={item} now={now} audience="teacher" />
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>

        <div className="grid min-w-0 grid-cols-1 gap-6 lg:col-span-5">
          <Card>
            <CardHeader title={t('admin.needsAttention')} />
            <CardContent className="grid grid-cols-1 gap-3">
              {load.isPending && termId !== undefined && <Skeleton className="h-16" />}
              {load.isSuccess && (
                <Link
                  to={`/admin/load?term=${termId ?? ''}`}
                  className="hover:bg-surface-sunken focus-visible:outline-focus-ring -mx-2 flex items-center gap-3 rounded-sm px-2 py-2 focus-visible:outline-2"
                >
                  <AlertTriangle
                    aria-hidden
                    className={unscheduled.length > 0 ? 'text-warning size-5' : 'text-success size-5'}
                  />
                  <span className="text-body text-text min-w-0 flex-1">
                    {unscheduled.length > 0
                      ? t('admin.unscheduled', { lines: unscheduled.length, hours: remainingHours })
                      : t('admin.allScheduled')}
                  </span>
                  <ChevronRight aria-hidden className="text-text-muted size-4" />
                </Link>
              )}
              {overridden.isSuccess && (overridden.data.totalElements ?? 0) > 0 && (
                <Link
                  to="/admin/audit?action=TIMETABLE_CONFLICT_OVERRIDDEN"
                  className="hover:bg-surface-sunken focus-visible:outline-focus-ring -mx-2 flex items-center gap-3 rounded-sm px-2 py-2 focus-visible:outline-2"
                >
                  <AlertTriangle aria-hidden className="text-warning size-5" />
                  <span className="text-body text-text min-w-0 flex-1">
                    {t('admin.overridden', { count: overridden.data.totalElements ?? 0 })}
                  </span>
                  <ChevronRight aria-hidden className="text-text-muted size-4" />
                </Link>
              )}
            </CardContent>
          </Card>
          <Card>
            <CardHeader
              title={t('admin.recentEvents')}
              action={
                <Link to="/admin/audit" className={button({ variant: 'ghost', size: 'sm' })}>
                  {t('admin.allEvents')}
                  <ChevronRight aria-hidden className="size-4" />
                </Link>
              }
            />
            <CardContent>
              {audit.isPending && <Skeleton className="h-40" />}
              <ul className="divide-border grid grid-cols-1 divide-y">
                {(audit.data?.content ?? [])
                  .filter((row) => !ROUTINE.has(row.action ?? ''))
                  .slice(0, 8)
                  .map((row) => (
                    <li key={row.id} className="grid grid-cols-1 gap-0.5 py-2">
                      <span className="text-small text-text flex flex-wrap items-center gap-2">
                        {t(`admin.auditAction.${row.action ?? ''}`, row.action ?? '')}
                        {row.outcome !== 'SUCCESS' && (
                          <Badge tone="danger">{t(`admin.auditOutcome.${row.outcome ?? 'FAILURE'}`)}</Badge>
                        )}
                      </span>
                      <span className="text-caption text-text-muted tracking-normal">
                        {[
                          row.actorLabel,
                          row.occurredAt === undefined ? undefined : formatDateTime(row.occurredAt, locale),
                        ]
                          .filter(Boolean)
                          .join(' · ')}
                      </span>
                    </li>
                  ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
