import { ArrowRight, GraduationCap } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { PageHeader } from '@/shared/ui/page-header'
import { ProgressBar } from '@/shared/ui/progress'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { flattenPlan, planPercent, type Enrollment } from './model'
import { useCoursePlanQuery, useCourseQuery, useEnrollmentsQuery } from './queries'

/** S-03.1 — the student's courses: what each is, who teaches it, how far the student is. */
export function CoursesPage() {
  const { t } = useTranslation()
  const enrollments = useEnrollmentsQuery()

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.courses')} />
      {enrollments.isPending && (
        <div className="grid gap-5 md:grid-cols-2">
          <CourseCardSkeleton />
          <CourseCardSkeleton />
        </div>
      )}
      {enrollments.isError && (
        <ErrorState
          problem={toApiProblem(enrollments.error)}
          onRetry={() => {
            void enrollments.refetch()
          }}
        />
      )}
      {enrollments.isSuccess && enrollments.data.length === 0 && (
        <EmptyState
          icon={<GraduationCap aria-hidden className="size-7" />}
          title={t('dashboard.noCoursesTitle')}
          body={t('dashboard.noCoursesBody')}
        />
      )}
      {enrollments.isSuccess && enrollments.data.length > 0 && (
        <ul className="grid gap-5 md:grid-cols-2">
          {enrollments.data.map((enrollment) => (
            <li key={enrollment.id} className="flex">
              <CourseCard enrollment={enrollment} />
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

const cardShell = 'border-border bg-surface-raised flex w-full flex-col rounded-lg border'

function CourseCardSkeleton() {
  return (
    <div className={cn(cardShell, 'gap-5 p-5')}>
      <Skeleton className="h-4 w-24" />
      <Skeleton className="h-7 w-3/4" />
      <SkeletonText lines={2} />
      <Skeleton className="h-2 w-full" />
    </div>
  )
}

function CourseCard({ enrollment }: { enrollment: Enrollment }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const course = useCourseQuery(enrollment.courseId, locale)
  const plan = useCoursePlanQuery(enrollment.courseId, locale)

  if (course.isPending || plan.isPending) return <CourseCardSkeleton />
  if (course.isError || plan.isError) {
    return (
      <ErrorState
        className="w-full"
        problem={toApiProblem(course.error ?? plan.error)}
        onRetry={() => {
          void course.refetch()
          void plan.refetch()
        }}
      />
    )
  }

  const lead = course.data.teachers.find((teacher) => teacher.lead) ?? course.data.teachers[0]
  const next = flattenPlan(plan.data).find((lesson) => lesson.id === plan.data.continueLessonId)
  const percent = planPercent(plan.data)
  const courseHref = `/student/courses/${enrollment.courseId}`

  return (
    <article className={cardShell}>
      <div className="grid flex-1 content-start gap-4 p-5">
        <div className="grid gap-1.5">
          <p className="text-caption text-text-muted font-medium uppercase">
            {[course.data.subjectCode, course.data.termName].filter((part) => part !== undefined).join(' · ')}
          </p>
          <h2 className="font-display text-title text-text font-semibold">
            <Link to={courseHref} className="hover:text-primary-hover focus-visible:outline-focus-ring">
              {course.data.title}
            </Link>
          </h2>
          {course.data.titleIsFallback && <FallbackMark />}
        </div>

        <dl className="text-small grid gap-2">
          {lead !== undefined && (
            <div className="flex gap-3">
              <dt className="text-text-muted w-28 shrink-0">{t('courses.teacher')}</dt>
              <dd className="text-text">{lead.fullName}</dd>
            </div>
          )}
          {next !== undefined && (
            <div className="flex gap-3">
              <dt className="text-text-muted w-28 shrink-0">{t('lesson.next')}</dt>
              <dd className="text-text">{next.title}</dd>
            </div>
          )}
        </dl>

        <ProgressBar
          value={percent}
          label={t('dashboard.lessonsProgress', {
            done: plan.data.lessonsCompleted,
            total: plan.data.lessonsTotal,
          })}
        />
      </div>

      <div className="border-border flex items-center justify-between gap-3 border-t px-5 py-3">
        <Link to={courseHref} className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}>
          {t('courses.open')}
        </Link>
        {next !== undefined && (
          <Link
            to={`${courseHref}/lessons/${next.id}`}
            className={button({ variant: 'outline', size: 'sm' })}
          >
            {t('dashboard.continueAction')}
            <ArrowRight aria-hidden className="size-4" />
          </Link>
        )}
      </div>
    </article>
  )
}
