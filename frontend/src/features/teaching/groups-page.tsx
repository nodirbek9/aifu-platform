import { Download, UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { downloadXlsx } from '@/shared/lib/xlsx'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useGroupStudentsQuery, useGroupsQuery, type Group } from './queries'

/** T-10 — the groups and their rosters; a roster exports to Excel. */
export function GroupsPage() {
  const { t } = useTranslation()
  const groups = useGroupsQuery()
  const [search, setSearch] = useSearchParams()
  const chosen = groups.data?.find((group) => group.id === search.get('group')) ?? groups.data?.[0]

  return (
    <div className="grid gap-6">
      <PageHeader title={t('nav.groups')} />
      {groups.isPending && <Skeleton className="h-64" />}
      {groups.isError && (
        <ErrorState
          problem={toApiProblem(groups.error)}
          onRetry={() => {
            void groups.refetch()
          }}
        />
      )}
      {groups.isSuccess && groups.data.length === 0 && (
        <EmptyState icon={<UsersRound aria-hidden className="size-7" />} title={t('teaching.noGroups')} />
      )}
      {groups.isSuccess && groups.data.length > 0 && (
        <div className="grid items-start gap-6 md:grid-cols-12">
          <nav aria-label={t('nav.groups')} className="md:col-span-4">
            <ul className="grid gap-2">
              {groups.data.map((group) => (
                <li key={group.id}>
                  <button
                    type="button"
                    aria-current={group.id === chosen?.id ? 'true' : undefined}
                    onClick={() => {
                      setSearch({ group: group.id }, { replace: true })
                    }}
                    className={cn(
                      'border-border bg-surface-raised flex w-full items-center justify-between gap-3 rounded-md border px-4 py-3 text-left',
                      'hover:border-border-strong focus-visible:outline-focus-ring focus-visible:outline-2',
                      'aria-[current=true]:border-primary aria-[current=true]:ring-primary aria-[current=true]:ring-1',
                    )}
                  >
                    <span className="grid">
                      <span className="text-body text-text font-semibold">{group.code}</span>
                      {group.curator !== undefined && (
                        <span className="text-small text-text-muted">
                          {t('teaching.curator', { name: group.curator })}
                        </span>
                      )}
                    </span>
                    <span className="text-small text-text-muted tabular-nums">
                      {t('teaching.studentsCount', { count: group.studentCount })}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          </nav>
          <div className="md:col-span-8">{chosen !== undefined && <Roster group={chosen} />}</div>
        </div>
      )}
    </div>
  )
}

function Roster({ group }: { group: Group }) {
  const { t } = useTranslation()
  const students = useGroupStudentsQuery(group.id)
  return (
    <Card>
      <CardHeader
        title={group.code}
        description={
          group.courseYear === undefined ? undefined : t('teaching.courseYear', { year: group.courseYear })
        }
        action={
          students.isSuccess && students.data.length > 0 ? (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                downloadXlsx(group.code, [
                  {
                    name: group.code,
                    rows: [
                      ['#', t('teaching.student'), t('profile.studentNumber'), t('auth.identifier')],
                      ...students.data.map((student, index) => [
                        index + 1,
                        student.fullName,
                        student.studentNumber ?? '',
                        student.username ?? '',
                      ]),
                    ],
                  },
                ])
              }}
            >
              <Download aria-hidden className="size-4" />
              {t('teaching.exportExcel')}
            </Button>
          ) : undefined
        }
      />
      <CardContent>
        {students.isPending && <SkeletonText lines={6} />}
        {students.isError && (
          <ErrorState
            problem={toApiProblem(students.error)}
            onRetry={() => {
              void students.refetch()
            }}
          />
        )}
        {students.isSuccess && (
          <Table>
            <TableHead>
              <tr>
                <TableHeaderCell className="w-10">#</TableHeaderCell>
                <TableHeaderCell>{t('teaching.student')}</TableHeaderCell>
                <TableHeaderCell>{t('profile.studentNumber')}</TableHeaderCell>
              </tr>
            </TableHead>
            <TableBody>
              {students.data.map((student, index) => (
                <TableRow key={student.userId}>
                  <TableCell className="text-text-muted tabular-nums">{index + 1}</TableCell>
                  <TableCell className="text-text font-medium">{student.fullName}</TableCell>
                  <TableCell className="text-text-muted tabular-nums">
                    {student.studentNumber ?? '—'}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
