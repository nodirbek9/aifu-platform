import { useState } from 'react'
import { UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Skeleton } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useAtRiskQuery, useCourseRosterQuery } from './queries'

/** The students of a course, with where each stands: completion, failed attempts, debt. */
export function CourseRoster({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const roster = useCourseRosterQuery(courseId)
  const atRisk = useAtRiskQuery(courseId)
  const [filter, setFilter] = useState('')

  if (roster.isPending) return <Skeleton className="h-64" />
  if (roster.isError) {
    return (
      <ErrorState
        problem={toApiProblem(roster.error)}
        onRetry={() => {
          void roster.refetch()
        }}
      />
    )
  }
  if (roster.data.length === 0) {
    return (
      <EmptyState icon={<UsersRound aria-hidden className="size-7" />} title={t('teaching.noStudents')} />
    )
  }
  const risk = new Map((atRisk.data ?? []).map((row) => [row.studentId, row]))
  const shown = roster.data
    .filter((row) => row.fullName.toLowerCase().includes(filter.trim().toLowerCase()))
    .sort((a, b) => a.fullName.localeCompare(b.fullName))

  return (
    <div className="grid gap-4">
      <div className="max-w-xs">
        <Input
          aria-label={t('teaching.findStudent')}
          placeholder={t('teaching.findStudent')}
          value={filter}
          onChange={(event) => {
            setFilter(event.target.value)
          }}
        />
      </div>
      <Table>
        <TableHead>
          <tr>
            <TableHeaderCell>{t('teaching.student')}</TableHeaderCell>
            <TableHeaderCell numeric>{t('teaching.completionColumn')}</TableHeaderCell>
            <TableHeaderCell numeric className="hidden sm:table-cell">
              {t('teaching.failedAttempts')}
            </TableHeaderCell>
          </tr>
        </TableHead>
        <TableBody>
          {shown.map((row) => {
            const standing = risk.get(row.studentId)
            return (
              <TableRow key={row.enrollmentId}>
                <TableCell className="text-text font-medium">{row.fullName}</TableCell>
                <TableCell numeric className="tabular-nums">
                  {standing === undefined ? '—' : `${Math.round(standing.completionPercent)}%`}
                </TableCell>
                <TableCell numeric className="hidden tabular-nums sm:table-cell">
                  {standing === undefined
                    ? '—'
                    : standing.failedQuizAttempts + standing.unsuccessfulSubmissions}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
