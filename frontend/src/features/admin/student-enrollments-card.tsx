import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Badge } from '@/shared/ui/badge'
import { Button, button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { Pagination } from '@/shared/ui/pagination'
import { Skeleton } from '@/shared/ui/skeleton'
import { EnrollmentStatusDialog, type EditableEnrollment } from './enrollment-status-dialog'
import { enrollmentTransitions, type EnrollmentSummary } from './student-enrollment-model'
import { useStudentEnrollmentsQuery } from './student-enrollment-queries'
import { useAdminCourseQuery } from './structure-queries'
import type { UserDetail } from './users-queries'

type StudentEnrollmentsCardProps = { user: UserDetail }

function isEditable(row: EnrollmentSummary): row is EditableEnrollment {
  return (
    row.id !== undefined &&
    row.courseId !== undefined &&
    row.studentUserId !== undefined &&
    row.status !== undefined
  )
}

/** A-05 / 7.1-F3: all enrollment pages, including terminal records. */
export function StudentEnrollmentsCard({ user }: StudentEnrollmentsCardProps) {
  const { t } = useTranslation()
  const [params, setParams] = useSearchParams()
  const rawPage = Number(params.get('enrollmentPage') ?? 0)
  const page = Number.isSafeInteger(rawPage) && rawPage >= 0 ? rawPage : 0
  const enrollments = useStudentEnrollmentsQuery(user.id ?? '', { page, size: 25 })
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const rows = enrollments.data?.content ?? []
  const selected = rows.find((row) => row.id === selectedId)
  const changePage = (next: number) => {
    setSelectedId(null)
    setParams((previous) => {
      const nextParams = new URLSearchParams(previous)
      if (next === 0) nextParams.delete('enrollmentPage')
      else nextParams.set('enrollmentPage', String(next))
      return nextParams
    })
  }
  return (
    <Card>
      <CardHeader title={t('admin.studentCard.coursesTitle')} />
      <CardContent className="grid grid-cols-1 gap-4" aria-busy={enrollments.isFetching}>
        {enrollments.isPending ? (
          <Skeleton className="h-48" />
        ) : enrollments.isError ? (
          <ErrorState problem={toApiProblem(enrollments.error)} onRetry={() => void enrollments.refetch()} />
        ) : (
          <>
            {rows.length === 0 ? (
              <EmptyState
                title={t('admin.studentCard.noEnrollments')}
                action={
                  <Link
                    to="/admin/structure?tab=courses"
                    className={button({ variant: 'outline', size: 'sm' })}
                  >
                    {t('admin.studentCard.openCourses')}
                  </Link>
                }
              />
            ) : (
              <ul className="divide-border grid grid-cols-1 divide-y">
                {rows.map((row, index) => (
                  <li key={row.id ?? index} className="grid grid-cols-1 gap-3 py-4 first:pt-0">
                    {row.courseId !== undefined ? (
                      <EnrollmentCourseTitle row={row} courseId={row.courseId} />
                    ) : (
                      <p className="text-body text-text font-medium wrap-anywhere">{row.subjectCode}</p>
                    )}
                    <p className="text-small text-text-muted wrap-anywhere">
                      {[row.subjectCode, row.cohortCode, row.termName].filter(Boolean).join(' · ')}
                    </p>
                    <div className="flex flex-wrap items-center gap-3">
                      {row.status !== undefined && <Badge>{t(`admin.enrollmentStatus.${row.status}`)}</Badge>}
                      {isEditable(row) && enrollmentTransitions(row.status).length > 0 && (
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={enrollments.isPlaceholderData}
                          onClick={() => setSelectedId(row.id)}
                        >
                          {t('admin.studentCard.changeEnrollmentStatus')}
                        </Button>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            )}
            <Pagination
              page={page}
              totalPages={enrollments.data?.totalPages ?? 0}
              onPageChange={changePage}
            />
          </>
        )}
        {selected !== undefined && isEditable(selected) && (
          <EnrollmentStatusDialog
            key={selected.id}
            enrollment={selected}
            onClose={() => setSelectedId(null)}
          />
        )}
      </CardContent>
    </Card>
  )
}

type EnrollmentCourseTitleProps = { row: EnrollmentSummary; courseId: string }

/** Only visible rows mount these existing, cached detail reads (at most one page). */
function EnrollmentCourseTitle({ row, courseId }: EnrollmentCourseTitleProps) {
  const locale = useLocale()
  const course = useAdminCourseQuery(courseId)
  const translation = course.data?.translations?.find((item) => item.locale === locale)
  const fallback = course.data?.translations?.find((item) => item.title)
  const title = translation?.title || fallback?.title || row.subjectCode || courseId
  return (
    <div className="grid grid-cols-1 gap-2">
      {course.isPending ? (
        <Skeleton className="h-6" />
      ) : (
        <Link
          to={`/admin/structure/courses/${courseId}`}
          className="text-body text-primary focus-visible:outline-focus-ring rounded-sm font-medium wrap-anywhere underline-offset-4 hover:underline focus-visible:outline-2"
        >
          {title}
        </Link>
      )}
      {course.isError && (
        <ErrorState problem={toApiProblem(course.error)} onRetry={() => void course.refetch()} />
      )}
      {!translation?.title && fallback?.title !== undefined && <FallbackMark />}
    </div>
  )
}
