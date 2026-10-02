import { ArrowRight, Award, ClipboardList } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useParams, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDate } from '@/shared/lib/dates'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { ProgressRing } from '@/shared/ui/progress'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { ActivityRow } from './activity-row'
import { CoursePlanList } from './course-plan-list'
import { CourseSyllabus } from './course-syllabus'
import { planPercent, type Course, type CoursePlan } from './model'
import {
  useActivitiesQuery,
  useCoursePlanQuery,
  useCourseQuery,
  useEnrollmentsQuery,
  useGradesQuery,
} from './queries'

const TABS = ['plan', 'syllabus', 'work', 'grades'] as const
type TabKey = (typeof TABS)[number]

function isTab(value: string | null): value is TabKey {
  return value !== null && (TABS as readonly string[]).includes(value)
}

/** S-03.2 — one course: its plan, its syllabus, the student's work and grades in it. */
export function CoursePage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { courseId = '' } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const course = useCourseQuery(courseId, locale)
  const plan = useCoursePlanQuery(courseId, locale)
  const requested = searchParams.get('tab')
  const tab: TabKey = isTab(requested) ? requested : 'plan'

  if (course.isPending) return <CoursePageSkeleton />
  if (course.isError) {
    return (
      <ErrorState
        problem={toApiProblem(course.error)}
        onRetry={() => {
          void course.refetch()
        }}
      />
    )
  }

  return (
    <div className="grid gap-6">
      <Breadcrumb
        items={[{ label: t('nav.courses'), to: '/student/courses' }, { label: course.data.title }]}
      />
      <CourseHeader course={course.data} plan={plan.data} />

      <Tabs
        value={tab}
        onValueChange={(value) => {
          setSearchParams(value === 'plan' ? {} : { tab: value }, { replace: true })
        }}
      >
        <TabsList>
          <TabsTrigger value="plan">{t('courses.tabPlan')}</TabsTrigger>
          <TabsTrigger value="syllabus">{t('courses.tabSyllabus')}</TabsTrigger>
          <TabsTrigger value="work">{t('courses.tabWork')}</TabsTrigger>
          <TabsTrigger value="grades">{t('courses.tabGrades')}</TabsTrigger>
        </TabsList>

        <TabsContent value="plan" className="pt-6">
          {plan.isPending && <SkeletonText lines={6} />}
          {plan.isError && (
            <ErrorState
              problem={toApiProblem(plan.error)}
              onRetry={() => {
                void plan.refetch()
              }}
            />
          )}
          {plan.isSuccess && <CoursePlanList plan={plan.data} />}
        </TabsContent>
        <TabsContent value="syllabus" className="pt-6">
          <CourseSyllabus course={course.data} />
        </TabsContent>
        <TabsContent value="work" className="pt-6">
          <CourseWork courseId={courseId} />
        </TabsContent>
        <TabsContent value="grades" className="pt-6">
          <CourseGrades courseId={courseId} />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function CoursePageSkeleton() {
  return (
    <div className="grid gap-6">
      <Skeleton className="h-4 w-48" />
      <Skeleton className="h-10 w-2/3" />
      <Skeleton className="h-10 w-80" />
      <SkeletonText lines={6} />
    </div>
  )
}

function CourseHeader({ course, plan }: { course: Course; plan: CoursePlan | undefined }) {
  const { t } = useTranslation()
  const facts = [
    course.subjectCode,
    course.termName,
    course.credits !== undefined ? t('courses.credits', { count: course.credits }) : undefined,
    course.totalHours !== undefined ? t('courses.hours', { count: course.totalHours }) : undefined,
  ].filter((fact) => fact !== undefined)
  const percent = plan !== undefined ? planPercent(plan) : 0

  return (
    <header className="flex flex-wrap items-start justify-between gap-x-10 gap-y-5">
      <div className="grid min-w-0 flex-1 basis-80 gap-3">
        <div className="grid gap-1.5">
          <p className="text-caption text-text-muted font-medium uppercase">{facts.join(' · ')}</p>
          <h1 className="font-display text-display-lg text-text md:text-display-xl font-semibold">
            {course.title}
          </h1>
          {course.titleIsFallback && <FallbackMark />}
        </div>
        {course.teachers.length > 0 && (
          <p className="text-small text-text-muted">
            <span className="text-text font-medium">{t('courses.teachers')}:</span>{' '}
            {course.teachers
              .map((teacher) =>
                teacher.lead ? `${teacher.fullName} (${t('courses.lead')})` : teacher.fullName,
              )
              .join(', ')}
          </p>
        )}
      </div>

      {plan !== undefined && (
        <div className="flex shrink-0 items-center gap-4">
          <ProgressRing value={percent} size={64} label={`${percent}%`} />
          <div className="grid gap-2">
            <p className="text-small text-text-muted tabular-nums">
              {t('dashboard.lessonsProgress', { done: plan.lessonsCompleted, total: plan.lessonsTotal })}
            </p>
            {plan.continueLessonId !== undefined && (
              <Link
                to={`/student/courses/${course.id}/lessons/${plan.continueLessonId}`}
                className={button({ variant: 'accent' })}
              >
                {t('dashboard.continueAction')}
                <ArrowRight aria-hidden className="size-4" />
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  )
}

function CourseWork({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const activities = useActivitiesQuery(locale, courseId)

  if (activities.isPending) return <SkeletonText lines={5} />
  if (activities.isError) {
    return (
      <ErrorState
        problem={toApiProblem(activities.error)}
        onRetry={() => {
          void activities.refetch()
        }}
      />
    )
  }
  if (activities.data.length === 0) {
    return (
      <EmptyState
        icon={<ClipboardList aria-hidden className="size-7" />}
        title={t('courses.workEmptyTitle')}
        body={t('dashboard.deadlinesEmptyBody')}
      />
    )
  }
  return (
    <ul className="divide-border border-border bg-surface-raised divide-y rounded-lg border px-5">
      {activities.data.map((activity) => (
        <li key={activity.activityId}>
          <ActivityRow activity={activity} />
        </li>
      ))}
    </ul>
  )
}

function CourseGrades({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const enrollments = useEnrollmentsQuery()
  const enrollment = enrollments.data?.find((item) => item.courseId === courseId)
  const grades = useGradesQuery(enrollment)

  if (enrollments.isPending || (enrollment !== undefined && grades.isPending))
    return <SkeletonText lines={4} />
  if (enrollments.isError || grades.isError) {
    return (
      <ErrorState
        problem={toApiProblem(enrollments.error ?? grades.error)}
        onRetry={() => {
          void enrollments.refetch()
          void grades.refetch()
        }}
      />
    )
  }
  if (grades.data === undefined || grades.data.length === 0) {
    return (
      <EmptyState
        icon={<Award aria-hidden className="size-7" />}
        title={t('dashboard.gradesEmptyTitle')}
        body={t('dashboard.gradesEmptyBody')}
      />
    )
  }
  return (
    <ul className="divide-border border-border bg-surface-raised divide-y rounded-lg border px-5">
      {grades.data.map((grade) => (
        <li key={grade.id} className="flex items-center justify-between gap-4 py-3">
          <div className="grid gap-0.5">
            <span className="text-body text-text font-medium">{t(`gradeSource.${grade.source}`)}</span>
            <span className="text-small text-text-muted">{formatDate(grade.publishedAt, locale)}</span>
          </div>
          <span className="font-display text-title text-text font-semibold tabular-nums">
            {grade.score}
            <span className="text-small text-text-muted font-sans font-normal"> / {grade.maxScore}</span>
          </span>
        </li>
      ))}
    </ul>
  )
}
