import { BookOpen, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { Badge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Skeleton } from '@/shared/ui/skeleton'
import { useTeacherCoursesQuery } from './queries'

/** The courses I teach, each the way into its plan, journal, content and grades. */
export function TeacherCoursesPage() {
  const { t } = useTranslation()
  const courses = useTeacherCoursesQuery()

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.teacherClasses')} />
      {courses.isPending && (
        <div className="grid gap-4 md:grid-cols-2">
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
        </div>
      )}
      {courses.isError && (
        <ErrorState
          problem={toApiProblem(courses.error)}
          onRetry={() => {
            void courses.refetch()
          }}
        />
      )}
      {courses.isSuccess && courses.data.length === 0 && (
        <EmptyState
          icon={<BookOpen aria-hidden className="size-7" />}
          title={t('teaching.noCoursesTitle')}
          body={t('teaching.noCoursesBody')}
        />
      )}
      {courses.isSuccess && courses.data.length > 0 && (
        <ul className="grid gap-4 md:grid-cols-2">
          {courses.data.map((course) => (
            <li key={course.id}>
              <Link
                to={`/teacher/courses/${course.id}`}
                className="border-border bg-surface-raised hover:border-border-strong focus-visible:outline-focus-ring flex h-full items-start gap-4 rounded-lg border p-5 transition-[border-color] focus-visible:outline-2"
              >
                <span className="grid min-w-0 flex-1 gap-1.5">
                  <span className="text-caption tracking-caption text-text-muted font-medium uppercase">
                    {[course.subjectCode, course.termName].filter(Boolean).join(' · ')}
                  </span>
                  <span className="font-display text-title text-text font-semibold">{course.title}</span>
                  <span className="flex flex-wrap gap-2">
                    {course.cohortCode !== undefined && <Badge>{course.cohortCode}</Badge>}
                    {course.status !== 'PUBLISHED' && (
                      <Badge tone="warning">{t(`teaching.courseStatus.${course.status}`)}</Badge>
                    )}
                  </span>
                </span>
                <ChevronRight aria-hidden className="text-text-muted mt-1 size-5 shrink-0" />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
