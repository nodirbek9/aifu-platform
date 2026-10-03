import { useState } from 'react'
import { CalendarDays, ChevronRight, MessageSquareText, Play, TriangleAlert } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { useWaitingCountQuery } from '@/features/review/queries'
import { ClassRow } from '@/features/timetable/class-row'
import { classKey, currentOrNext, isHeld } from '@/features/timetable/model'
import { useNextClassQuery, useTodayQuery } from '@/features/timetable/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime, formatLongDay, formatTimeOfDay, useCampusNow } from '@/shared/lib/dates'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton } from '@/shared/ui/skeleton'
import type { TeacherCourse } from './model'
import {
  useAtRiskQuery,
  useCourseRosterQuery,
  useTeacherActivitiesQuery,
  useTeacherCoursesQuery,
} from './queries'

const WEEK_MS = 7 * 24 * 60 * 60 * 1000

/**
 * T-01 — what a teacher sees first: today's classes with the way into each, who is falling
 * behind, what closes this week, and the courses.
 */
export function TeacherDashboardPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { state } = useAuth()
  const now = useCampusNow()
  const courses = useTeacherCoursesQuery()
  if (state.status !== 'authenticated') return null

  return (
    <div className="grid gap-8">
      <header className="grid gap-1.5">
        <p className="text-caption text-text-muted font-medium uppercase">
          {formatLongDay(now.date, locale)}
        </p>
        <h1 className="font-display text-display-lg text-text md:text-display-xl font-semibold">
          {state.user.fullName}
        </h1>
      </header>

      <div className="grid items-start gap-6 lg:grid-cols-12">
        <section aria-label={t('dashboard.todayTitle')} className="lg:col-span-7">
          <TodayClasses />
        </section>
        <section aria-label={t('teaching.coursesTitle')} className="lg:col-span-5">
          <Card>
            <CardHeader
              title={t('teaching.coursesTitle')}
              action={
                <Link
                  to="/teacher/courses"
                  className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
                >
                  {t('teaching.allCourses')}
                </Link>
              }
            />
            <CardContent>
              {courses.isPending && <Skeleton className="h-32" />}
              {courses.isError && (
                <ErrorState
                  problem={toApiProblem(courses.error)}
                  onRetry={() => {
                    void courses.refetch()
                  }}
                />
              )}
              {courses.isSuccess && (
                <ul className="divide-border -my-2 divide-y">
                  {courses.data.map((course) => (
                    <li key={course.id}>
                      <Link
                        to={`/teacher/courses/${course.id}`}
                        className="hover:bg-surface-sunken focus-visible:outline-focus-ring -mx-2 flex items-center gap-3 rounded-sm px-2 py-2.5 focus-visible:outline-2"
                      >
                        <span className="grid min-w-0 flex-1">
                          <span className="text-body text-text truncate font-medium">{course.title}</span>
                          <span className="text-small text-text-muted">
                            {[course.subjectCode, course.cohortCode].filter(Boolean).join(' · ')}
                          </span>
                        </span>
                        <ChevronRight aria-hidden className="text-text-muted size-5 shrink-0" />
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </section>
        <section aria-label={t('teaching.atRiskTitle')} className="lg:col-span-7">
          <Card>
            <CardHeader title={t('teaching.atRiskTitle')} description={t('teaching.atRiskBody')} />
            <CardContent className="grid gap-4">
              {courses.isPending && <Skeleton className="h-24" />}
              {courses.data?.map((course) => (
                <AtRiskOfCourse key={course.id} course={course} />
              ))}
            </CardContent>
          </Card>
        </section>
        <div className="grid content-start gap-6 lg:col-span-5">
          <section aria-label={t('review.waitingTitle')}>
            <Card>
              <CardHeader title={t('review.waitingTitle')} description={t('review.waitingBody')} />
              <CardContent className="grid gap-1">
                {courses.isPending && <Skeleton className="h-16" />}
                {courses.data?.map((course) => (
                  <WaitingOfCourse key={course.id} course={course} />
                ))}
                <p className="text-small text-text-muted hidden only:block">{t('review.nothingWaiting')}</p>
              </CardContent>
            </Card>
          </section>
          <section aria-label={t('teaching.deadlinesTitle')}>
            <Card>
              <CardHeader title={t('teaching.deadlinesTitle')} />
              <CardContent className="grid gap-1">
                {courses.isPending && <Skeleton className="h-24" />}
                {courses.data?.map((course) => (
                  <DeadlinesOfCourse key={course.id} course={course} />
                ))}
                <p className="text-small text-text-muted hidden only:block">{t('teaching.noDeadlines')}</p>
              </CardContent>
            </Card>
          </section>
        </div>
      </div>
    </div>
  )
}

function TodayClasses() {
  const { t } = useTranslation()
  const locale = useLocale()
  const today = useTodayQuery('teacher')
  const next = useNextClassQuery('teacher')
  const now = useCampusNow()

  return (
    <Card>
      <CardHeader
        title={t('dashboard.todayTitle')}
        action={
          <Link
            to="/teacher/timetable"
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('dashboard.openTimetable')}
          </Link>
        }
      />
      <CardContent>
        {today.isPending && <Skeleton className="h-16" />}
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
            body={
              next.data !== undefined && next.data !== null
                ? t('dashboard.nextClass', {
                    day: formatLongDay(next.data.date, locale),
                    time: formatTimeOfDay(next.data.startsAt),
                    title: next.data.courseTitle,
                  })
                : t('dashboard.todayEmptyBody')
            }
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {today.isSuccess && today.data.classes.length > 0 && (
          <ol className="divide-border -my-3 divide-y">
            {today.data.classes.map((item) => (
              <li key={classKey(item)} className="flex flex-wrap items-center gap-x-4">
                <ol className="min-w-0 flex-1">
                  <ClassRow
                    item={item}
                    now={now}
                    audience="teacher"
                    marked={currentOrNext(today.data.classes, now) === item}
                  />
                </ol>
                {isHeld(item.status) && (
                  <Link
                    to={`/teacher/classes/${item.entryId}/${item.date}`}
                    className={button({ variant: 'primary', size: 'sm', class: 'mb-3 sm:mb-0' })}
                  >
                    <Play aria-hidden className="size-4" />
                    {t('teaching.startClass')}
                  </Link>
                )}
              </li>
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  )
}

/** The submissions of a course still waiting for the teacher's word: one line, into the queue. */
function WaitingOfCourse({ course }: { course: TeacherCourse }) {
  const { t } = useTranslation()
  const waiting = useWaitingCountQuery(course.id)
  if (!waiting.isSuccess || waiting.data === 0) return null
  return (
    <Link
      to={`/teacher/courses/${course.id}?tab=review`}
      className="hover:bg-surface-sunken focus-visible:outline-focus-ring flex items-center gap-3 rounded-md px-2 py-2 focus-visible:outline-2"
    >
      <MessageSquareText aria-hidden className="text-info size-4 shrink-0" />
      <span className="text-body text-text min-w-0 flex-1 truncate">{course.title}</span>
      <span className="text-small text-text font-semibold tabular-nums">
        {t('review.waitingCount', { count: waiting.data })}
      </span>
      <ChevronRight aria-hidden className="text-text-muted size-4" />
    </Link>
  )
}

function AtRiskOfCourse({ course }: { course: TeacherCourse }) {
  const { t } = useTranslation()
  const atRisk = useAtRiskQuery(course.id)
  const roster = useCourseRosterQuery(course.id)
  if (!atRisk.isSuccess) return null
  const names = new Map((roster.data ?? []).map((row) => [row.studentId, row.fullName]))
  const shown = atRisk.data
    .slice()
    .sort((a, b) => a.completionPercent - b.completionPercent)
    .slice(0, 3)
  if (shown.length === 0) return null
  return (
    <div className="grid gap-1">
      <h3 className="text-small text-text-muted font-medium">{course.title}</h3>
      <ul className="divide-border divide-y">
        {shown.map((student) => (
          <li key={student.studentId} className="flex items-center gap-3 py-2">
            <TriangleAlert aria-hidden className="text-warning size-4 shrink-0" />
            <span className="text-body text-text min-w-0 flex-1 truncate">
              {names.get(student.studentId) ?? '—'}
            </span>
            <span className="text-small text-text-muted tabular-nums">
              {t('teaching.completion', { percent: Math.round(student.completionPercent) })}
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}

function DeadlinesOfCourse({ course }: { course: TeacherCourse }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const activities = useTeacherActivitiesQuery(course.id, locale)
  const [now] = useState(() => Date.now())
  const closing = (activities.data ?? [])
    .filter((activity) => {
      if (activity.dueAt === undefined || activity.status !== 'PUBLISHED') return false
      const due = Date.parse(activity.dueAt)
      return due >= now && due - now <= WEEK_MS
    })
    .sort((a, b) => (a.dueAt ?? '').localeCompare(b.dueAt ?? ''))
  if (closing.length === 0) return null
  return (
    <ul className="divide-border divide-y">
      {closing.map((activity) => (
        <li key={activity.id} className="grid gap-0.5 py-2.5">
          <span className="text-body text-text font-medium">{activity.title}</span>
          <span className="text-small text-text-muted">
            {course.title} · {t('dashboard.due', { date: formatDateTime(activity.dueAt ?? '', locale) })}
          </span>
        </li>
      ))}
    </ul>
  )
}
