import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { AttendanceJournal } from './attendance-journal'
import { useTeacherCoursesQuery } from './queries'

/** T-10 — the attendance journal of any of my courses; the course is kept in the address. */
export function JournalPage() {
  const { t } = useTranslation()
  const courses = useTeacherCoursesQuery()
  const [search, setSearch] = useSearchParams()
  const chosen = courses.data?.find((course) => course.id === search.get('course')) ?? courses.data?.[0]

  return (
    <div className="grid gap-6">
      <PageHeader
        title={t('nav.teacherJournal')}
        action={
          courses.isSuccess && courses.data.length > 0 ? (
            <Select
              className="w-72"
              label={t('teaching.course')}
              value={chosen?.id}
              onValueChange={(value) => {
                setSearch({ course: value }, { replace: true })
              }}
              options={courses.data.map((course) => ({ value: course.id, label: course.title }))}
            />
          ) : undefined
        }
      />
      {courses.isPending && <Skeleton className="h-96" />}
      {courses.isError && (
        <ErrorState
          problem={toApiProblem(courses.error)}
          onRetry={() => {
            void courses.refetch()
          }}
        />
      )}
      {chosen !== undefined && (
        <AttendanceJournal key={chosen.id} courseId={chosen.id} courseTitle={chosen.title} />
      )}
    </div>
  )
}
