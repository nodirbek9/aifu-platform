import { GraduationCap } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import type { Enrollment } from '@/features/courses/model'
import { useCourseQuery, useEnrollmentsQuery } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Card, CardContent } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { CourseGradebook } from './course-gradebook'
import { useGradebookQuery } from './queries'

/** S-08 — my grades, course by course; each course loads on its own. */
export function GradesPage() {
  const { t } = useTranslation()
  const enrollments = useEnrollmentsQuery()

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.grades')} />
      {enrollments.isPending && (
        <div className="grid gap-4">
          <Skeleton className="h-64" />
          <Skeleton className="h-64" />
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
      {enrollments.isSuccess &&
        enrollments.data.map((enrollment) => <CourseBlock key={enrollment.id} enrollment={enrollment} />)}
    </div>
  )
}

function CourseBlock({ enrollment }: { enrollment: Enrollment }) {
  const locale = useLocale()
  const course = useCourseQuery(enrollment.courseId, locale)
  const book = useGradebookQuery(enrollment.id, locale)

  return (
    <Card>
      <CardContent className="grid gap-5 pt-5">
        <h2 className="font-display text-title text-text font-semibold">
          <Link
            to={`/student/courses/${enrollment.courseId}?tab=grades`}
            className="focus-visible:outline-focus-ring rounded-sm hover:underline focus-visible:outline-2 focus-visible:outline-offset-2"
          >
            {course.data?.title ?? enrollment.subjectCode}
          </Link>
        </h2>
        {book.isPending && <SkeletonText lines={5} />}
        {book.isError && (
          <ErrorState
            problem={toApiProblem(book.error)}
            onRetry={() => {
              void book.refetch()
            }}
          />
        )}
        {book.isSuccess && <CourseGradebook book={book.data} />}
      </CardContent>
    </Card>
  )
}
