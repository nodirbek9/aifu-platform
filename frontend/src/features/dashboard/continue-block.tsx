import { ArrowRight, BookOpenCheck, GraduationCap } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { flattenPlan, planPercent, type Enrollment } from '@/features/courses/model'
import { useCoursePlanQuery, useCourseQuery, useLastCourseId } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { SessionTypeBadge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { ProgressRing } from '@/shared/ui/progress'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'

type ContinueBlockProps = {
  /** Undefined while the enrollments are loading. */
  enrollments: readonly Enrollment[] | undefined
}

/** S-01.2 — the course the student last worked in, its progress, and the lesson to open next. */
export function ContinueBlock({ enrollments }: ContinueBlockProps) {
  const { t } = useTranslation()
  const last = useLastCourseId(enrollments)

  return (
    <Card>
      <CardHeader title={t('dashboard.continueTitle')} />
      <CardContent>
        {last.pending && <ContinueSkeleton />}
        {!last.pending && last.courseId === undefined && (
          <EmptyState
            icon={<GraduationCap aria-hidden className="size-7" />}
            title={t('dashboard.noCoursesTitle')}
            body={t('dashboard.noCoursesBody')}
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {!last.pending && last.courseId !== undefined && <ContinueCourse courseId={last.courseId} />}
      </CardContent>
    </Card>
  )
}

function ContinueSkeleton() {
  return (
    <div className="flex items-center gap-5">
      <Skeleton className="size-16 rounded-full" />
      <SkeletonText lines={3} className="flex-1" />
    </div>
  )
}

function ContinueCourse({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const course = useCourseQuery(courseId, locale)
  const plan = useCoursePlanQuery(courseId, locale)

  if (course.isPending || plan.isPending) return <ContinueSkeleton />
  if (course.isError || plan.isError) {
    return (
      <ErrorState
        problem={toApiProblem(course.error ?? plan.error)}
        onRetry={() => {
          void course.refetch()
          void plan.refetch()
        }}
      />
    )
  }

  const next = flattenPlan(plan.data).find((lesson) => lesson.id === plan.data.continueLessonId)
  const percent = planPercent(plan.data)

  return (
    <div className="grid gap-5">
      <div className="flex items-center gap-5">
        <ProgressRing value={percent} size={64} label={`${percent}%`} />
        <div className="grid min-w-0 gap-1">
          <Link
            to={`/student/courses/${courseId}`}
            className="font-display text-title text-text hover:text-primary-hover font-semibold"
          >
            {course.data.title}
          </Link>
          <p className="text-small text-text-muted tabular-nums">
            {t('dashboard.lessonsProgress', {
              done: plan.data.lessonsCompleted,
              total: plan.data.lessonsTotal,
            })}
          </p>
        </div>
      </div>

      {next === undefined ? (
        <div className="text-small text-text-muted flex items-start gap-3">
          <BookOpenCheck aria-hidden className="text-success mt-0.5 size-5 shrink-0" />
          <p>
            <span className="text-text font-medium">{t('dashboard.continueDoneTitle')}.</span>{' '}
            {t('dashboard.continueDoneBody')}
          </p>
        </div>
      ) : (
        <div className="border-border bg-surface grid gap-3 rounded-md border p-4">
          <div className="grid gap-1.5">
            <div className="text-caption text-text-muted font-medium uppercase">{t('lesson.next')}</div>
            <p className="font-reading text-body text-text">{next.title}</p>
            <div>
              <SessionTypeBadge type={next.sessionType} label={t(`sessionType.${next.sessionType}`)} />
            </div>
          </div>
          <Link
            to={`/student/courses/${courseId}/lessons/${next.id}`}
            className={button({ variant: 'accent', class: 'justify-self-start' })}
          >
            {t('dashboard.continueAction')}
            <ArrowRight aria-hidden className="size-4" />
          </Link>
        </div>
      )}
    </div>
  )
}
