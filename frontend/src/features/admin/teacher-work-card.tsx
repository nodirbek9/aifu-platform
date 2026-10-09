import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import type { components } from '@/shared/api/schema'
import { useLocale } from '@/shared/i18n/use-locale'
import { campusNow, formatDateRange } from '@/shared/lib/dates'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Stat } from '@/shared/ui/stat'
import { useAcademicYearsQuery, useTermsQuery } from './reference-queries'
import {
  teacherWeeklyHours,
  useTeacherCoursesQuery,
  useTeacherTermLoadQuery,
  useTeacherTimetablesQuery,
  useTeacherWeekQuery,
} from './teacher-work-queries'
import type { UserDetail } from './users-queries'

type TeacherWorkCardProps = { user: UserDetail }
type TermWorkProps = { teacherId: string; term: components['schemas']['TermView'] }

/** 7.2-F2: assignments include unscheduled courses; weekly and term hours stay separate. */
export function TeacherWorkCard({ user }: TeacherWorkCardProps) {
  const { t } = useTranslation()
  const years = useAcademicYearsQuery()
  const [yearId, setYearId] = useState<string>()
  const [termId, setTermId] = useState<string>()
  const year =
    years.data?.find((item) => item.id === yearId) ??
    years.data?.find((item) => item.current) ??
    years.data?.[0]
  const terms = useTermsQuery(year?.id)
  const term = terms.data?.find((item) => item.id === termId) ?? terms.data?.[0]
  const referenceAction = (
    <Link to="/admin/reference" className={button({ variant: 'outline', size: 'sm' })}>
      {t('nav.adminReference')}
    </Link>
  )

  return (
    <Card>
      <CardHeader title={t('admin.teacherWork.tab')} />
      <CardContent className="grid grid-cols-1 gap-6">
        {years.isPending ? (
          <Skeleton className="h-32" />
        ) : years.isError ? (
          <ErrorState problem={toApiProblem(years.error)} onRetry={() => void years.refetch()} />
        ) : year?.id === undefined ? (
          <EmptyState title={t('admin.academicYear')} body={t('authoring.choose')} action={referenceAction} />
        ) : (
          <>
            <Select
              label={t('admin.academicYear')}
              value={year.id}
              onValueChange={(value) => {
                setYearId(value)
                setTermId(undefined)
              }}
              options={(years.data ?? []).flatMap((item) =>
                item.id && item.code ? [{ value: item.id, label: item.code }] : [],
              )}
            />
            {terms.isPending ? (
              <Skeleton className="h-32" />
            ) : terms.isError ? (
              <ErrorState problem={toApiProblem(terms.error)} onRetry={() => void terms.refetch()} />
            ) : term?.id === undefined ? (
              <EmptyState title={t('admin.term')} body={t('authoring.choose')} action={referenceAction} />
            ) : (
              <>
                <Select
                  label={t('admin.term')}
                  value={term.id}
                  onValueChange={setTermId}
                  options={(terms.data ?? []).flatMap((item) =>
                    item.id && item.name ? [{ value: item.id, label: item.name }] : [],
                  )}
                />
                <TermWork key={`${user.id ?? ''}-${term.id}`} teacherId={user.id ?? ''} term={term} />
              </>
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}

function TermWork({ teacherId, term }: TermWorkProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const courses = useTeacherCoursesQuery(teacherId, term.id, locale)
  const timetables = useTeacherTimetablesQuery()
  const [timetableId, setTimetableId] = useState<string>()
  const [date, setDate] = useState(() => {
    const today = campusNow().date
    if (term.startsOn && today < term.startsOn) return term.startsOn
    if (term.endsOn && today > term.endsOn) return term.endsOn
    return today
  })
  const choices = (timetables.data ?? []).filter((item) => item.termId === term.id && item.id !== undefined)
  const timetable =
    choices.find((item) => item.id === timetableId) ??
    choices.find((item) => item.status === 'PUBLISHED') ??
    choices[0]
  const week = useTeacherWeekQuery(timetable?.id, teacherId, date, locale)
  const load = useTeacherTermLoadQuery(timetable?.id, teacherId, locale)
  const timetablePath =
    timetable?.id === undefined
      ? '/admin/timetable'
      : `/admin/timetable?${new URLSearchParams({ tt: timetable.id, view: 'teacher', id: teacherId })}`
  const openTimetable = (
    <Link to={timetablePath} className={button({ variant: 'outline', size: 'sm' })}>
      {t('admin.teacherWork.openTimetable')}
    </Link>
  )

  return (
    <>
      <section aria-label={t('admin.teacherWork.courses')} className="grid grid-cols-1 gap-3">
        <h3 className="text-title text-text font-semibold">{t('admin.teacherWork.courses')}</h3>
        {courses.isPending ? (
          <div aria-label={t('admin.teacherWork.loadingCourses')}>
            <Skeleton className="h-24" />
          </div>
        ) : courses.isError ? (
          <ErrorState problem={toApiProblem(courses.error)} onRetry={() => void courses.refetch()} />
        ) : courses.data.length === 0 ? (
          <EmptyState
            title={t('admin.teacherWork.noCourses')}
            action={
              <Link to="/admin/structure" className={button({ variant: 'outline', size: 'sm' })}>
                {t('admin.studentCard.openCourses')}
              </Link>
            }
          />
        ) : (
          <ul className="divide-border divide-y">
            {courses.data.map((course, index) => {
              const translation =
                course.translations?.find((item) => item.locale === locale) ?? course.translations?.[0]
              return (
                <li key={course.id ?? index} className="grid grid-cols-1 gap-1 py-3">
                  {course.id ? (
                    <Link
                      to={`/admin/structure/courses/${course.id}`}
                      className="text-body text-primary font-medium underline-offset-4 hover:underline"
                    >
                      {translation?.title ?? course.subjectCode ?? '—'}
                    </Link>
                  ) : (
                    <span>{translation?.title ?? course.subjectCode ?? '—'}</span>
                  )}
                  <span className="text-small text-text-muted wrap-anywhere">
                    {[course.subjectCode, course.cohortCode, course.termName].filter(Boolean).join(' · ')}
                  </span>
                </li>
              )
            })}
          </ul>
        )}
      </section>
      {timetables.isPending ? (
        <Skeleton className="h-40" />
      ) : timetables.isError ? (
        <ErrorState problem={toApiProblem(timetables.error)} onRetry={() => void timetables.refetch()} />
      ) : timetable?.id === undefined ? (
        <EmptyState title={t('admin.teacherWork.chooseTimetable')} action={openTimetable} />
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Select
              label={t('admin.teacherWork.chooseTimetable')}
              value={timetable.id}
              onValueChange={setTimetableId}
              options={choices.flatMap((item) =>
                item.id
                  ? [
                      {
                        value: item.id,
                        label: `${item.name ?? item.termName ?? '—'} · ${t(`builder.status.${item.status ?? 'DRAFT'}`)}`,
                      },
                    ]
                  : [],
              )}
            />
            <Input
              label={t('admin.date')}
              type="date"
              value={date}
              min={term.startsOn}
              max={term.endsOn}
              onChange={(event) => setDate(event.target.value)}
            />
          </div>
          <section aria-label={t('admin.teacherWork.weeklyHours')} className="grid grid-cols-1 gap-3">
            {date === '' ? (
              <p className="text-small text-text-muted">{t('admin.date')}</p>
            ) : week.isPending ? (
              <Skeleton className="h-24" />
            ) : week.isError ? (
              <ErrorState problem={toApiProblem(week.error)} onRetry={() => void week.refetch()} />
            ) : (
              <Stat
                label={t('admin.teacherWork.weeklyHours')}
                value={teacherWeeklyHours(week.data, teacherId)}
                hint={
                  week.data.weekStart && week.data.weekEnd
                    ? formatDateRange(week.data.weekStart, week.data.weekEnd, locale)
                    : undefined
                }
              />
            )}
          </section>
          <section aria-label={t('admin.teacherWork.termLoad')} className="grid grid-cols-1 gap-3">
            <h3 className="text-title text-text font-semibold">{t('admin.teacherWork.termLoad')}</h3>
            {load.isPending ? (
              <Skeleton className="h-24" />
            ) : load.isError ? (
              <ErrorState problem={toApiProblem(load.error)} onRetry={() => void load.refetch()} />
            ) : load.data.length === 0 ? (
              <EmptyState title={t('admin.noLoad')} action={openTimetable} />
            ) : (
              <dl className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                {(['plannedHours', 'scheduledHours', 'remainingHours'] as const).map((field) => {
                  const label =
                    field === 'plannedHours'
                      ? t('admin.planned')
                      : field === 'scheduledHours'
                        ? t('admin.scheduled')
                        : t('admin.remaining')
                  const values = load.data.map((row) => row[field])
                  const total = values.every((value) => value !== undefined)
                    ? values.reduce((sum, value) => sum + value, 0)
                    : '—'
                  return (
                    <div key={field} className="grid grid-cols-1 gap-1">
                      <dt className="text-small text-text-muted">{label}</dt>
                      <dd className="font-display text-title text-text font-semibold tabular-nums">
                        {total}
                      </dd>
                    </div>
                  )
                })}
              </dl>
            )}
          </section>
          <div>{openTimetable}</div>
        </>
      )}
    </>
  )
}
