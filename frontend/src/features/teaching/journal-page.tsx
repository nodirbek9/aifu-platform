import { UserCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { AttendanceJournal } from './attendance-journal'
import { useJournalChoicesQuery } from './queries'

/**
 * T-10 — the attendance journal, opened the way a teacher thinks of it: a group, then the subject
 * taught to it, then the period. The group and the subject are kept in the address.
 */
export function JournalPage() {
  const { t } = useTranslation()
  const choices = useJournalChoicesQuery()
  const [search, setSearch] = useSearchParams()
  const group = choices.data?.find((item) => item.groupId === search.get('group')) ?? choices.data?.[0]
  const course = group?.courses.find((item) => item.id === search.get('course')) ?? group?.courses[0]

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.teacherJournal')} context={t('teaching.journalBody')} />
      {choices.isPending && <Skeleton className="h-96" />}
      {choices.isError && (
        <ErrorState
          problem={toApiProblem(choices.error)}
          onRetry={() => {
            void choices.refetch()
          }}
        />
      )}
      {choices.isSuccess && choices.data.length === 0 && (
        <EmptyState
          icon={<UserCheck aria-hidden className="size-7" />}
          title={t('teaching.journalNoGroupsTitle')}
          body={t('teaching.journalNoGroupsBody')}
        />
      )}
      {choices.isSuccess && group !== undefined && (
        <div className="flex flex-wrap items-end gap-3">
          <Select
            className="w-48"
            label={t('profile.group')}
            value={group.groupId}
            onValueChange={(value) => {
              setSearch({ group: value }, { replace: true })
            }}
            options={choices.data.map((item) => ({ value: item.groupId, label: item.groupCode }))}
          />
          <Select
            className="w-80"
            label={t('teaching.subject')}
            value={course?.id}
            onValueChange={(value) => {
              setSearch({ group: group.groupId, course: value }, { replace: true })
            }}
            options={group.courses.map((item) => ({ value: item.id, label: item.title }))}
          />
        </div>
      )}
      {group !== undefined && course !== undefined && (
        <AttendanceJournal
          key={`${group.groupId}:${course.id}`}
          courseId={course.id}
          courseTitle={`${course.title} — ${group.groupCode}`}
          groupId={group.groupId}
        />
      )}
    </div>
  )
}
