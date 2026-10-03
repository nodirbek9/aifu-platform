import { useState } from 'react'
import { Download, UserCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDate, formatWeekdayShort } from '@/shared/lib/dates'
import { downloadXlsx, type Cell } from '@/shared/lib/xlsx'
import { Button } from '@/shared/ui/button'
import { DatePicker } from '@/shared/ui/date-picker'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import type { AttendanceStatus, Journal } from './model'
import { useGroupsQuery, useJournalQuery, type JournalFilter } from './queries'

const markTone = {
  PRESENT: 'text-success',
  LATE: 'text-warning',
  ABSENT: 'text-danger bg-danger-soft',
  EXCUSED: 'text-info',
} as const satisfies Record<AttendanceStatus, string>

/** The filter with one field set, or left out when there is no value. */
function withValue(
  filter: JournalFilter,
  key: keyof JournalFilter,
  value: string | undefined,
): JournalFilter {
  const entries = Object.entries(filter).filter(([name]) => name !== key)
  return Object.fromEntries(value === undefined ? entries : [...entries, [key, value]])
}

type AttendanceJournalProps = {
  courseId: string
  courseTitle: string
}

/**
 * T-10 — the attendance journal of a course: students down, the classes whose attendance was
 * taken across, totals and percentage at the end; filter by group and period; export to Excel.
 */
export function AttendanceJournal({ courseId, courseTitle }: AttendanceJournalProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const groups = useGroupsQuery()
  const [filter, setFilter] = useState<JournalFilter>({})
  const journal = useJournalQuery(courseId, filter, locale)

  const exportXlsx = (data: Journal) => {
    const header: Cell[] = [
      t('teaching.student'),
      t('profile.group'),
      ...data.sessions.map(
        (session) => `${session.date}${session.pairNo === undefined ? '' : ` #${session.pairNo}`}`,
      ),
      t('attendance.status.PRESENT'),
      t('attendance.status.LATE'),
      t('attendance.status.ABSENT'),
      t('attendance.status.EXCUSED'),
      '%',
    ]
    const rows: Cell[][] = data.students.map((student) => [
      student.fullName,
      student.groupCode ?? '',
      ...data.sessions.map((session) => {
        const mark = student.marks[session.id]
        return mark === undefined ? '' : t(`teaching.markShort.${mark}`)
      }),
      student.totals.present,
      student.totals.late,
      student.totals.absent,
      student.totals.excused,
      student.totals.percent ?? null,
    ])
    downloadXlsx(`${courseTitle} — ${t('teaching.journal')}`, [
      { name: t('teaching.journal'), rows: [header, ...rows] },
    ])
  }

  return (
    <div className="grid gap-4">
      <div className="flex flex-wrap items-end gap-3">
        <Select
          className="w-48"
          label={t('profile.group')}
          value={filter.groupId ?? 'all'}
          onValueChange={(value) => {
            setFilter((current) => withValue(current, 'groupId', value === 'all' ? undefined : value))
          }}
          options={[
            { value: 'all', label: t('assignments.all') },
            ...(groups.data ?? []).map((group) => ({ value: group.id, label: group.code })),
          ]}
        />
        <DatePicker
          label={t('teaching.from')}
          value={filter.from ?? ''}
          onChange={(event) => {
            const value = event.target.value
            setFilter((current) => withValue(current, 'from', value === '' ? undefined : value))
          }}
        />
        <DatePicker
          label={t('teaching.to')}
          value={filter.to ?? ''}
          onChange={(event) => {
            const value = event.target.value
            setFilter((current) => withValue(current, 'to', value === '' ? undefined : value))
          }}
        />
        <div className="flex-1" />
        {journal.isSuccess && journal.data.sessions.length > 0 && (
          <Button
            variant="outline"
            onClick={() => {
              exportXlsx(journal.data)
            }}
          >
            <Download aria-hidden className="size-4" />
            {t('teaching.exportExcel')}
          </Button>
        )}
      </div>

      {journal.isPending && <Skeleton className="h-64" />}
      {journal.isError && (
        <ErrorState
          problem={toApiProblem(journal.error)}
          onRetry={() => {
            void journal.refetch()
          }}
        />
      )}
      {journal.isSuccess && journal.data.sessions.length === 0 && (
        <EmptyState
          icon={<UserCheck aria-hidden className="size-7" />}
          title={t('teaching.journalEmptyTitle')}
          body={t('teaching.journalEmptyBody')}
        />
      )}
      {journal.isSuccess && journal.data.sessions.length > 0 && (
        <div className="border-border bg-surface-raised max-h-160 overflow-auto rounded-lg border">
          <table className="text-small w-max min-w-full border-separate border-spacing-0">
            <thead className="bg-surface-sunken sticky top-0 z-20">
              <tr>
                <th
                  scope="col"
                  className="bg-surface-sunken border-border text-caption tracking-caption text-text-muted sticky left-0 z-30 border-r border-b px-3 py-2 text-left font-medium uppercase"
                >
                  {t('teaching.student')}
                </th>
                {journal.data.sessions.map((session) => (
                  <th
                    key={session.id}
                    scope="col"
                    title={session.title}
                    className="border-border border-b px-1 py-2 text-center font-normal"
                  >
                    {session.entryId === undefined ? (
                      <SessionHead date={session.date} pairNo={session.pairNo} />
                    ) : (
                      <Link
                        to={`/teacher/classes/${session.entryId}/${session.date}`}
                        className="hover:bg-surface focus-visible:outline-focus-ring block rounded-sm px-1 focus-visible:outline-2"
                      >
                        <SessionHead date={session.date} pairNo={session.pairNo} />
                      </Link>
                    )}
                  </th>
                ))}
                <th
                  scope="col"
                  className="border-border text-caption text-text-muted border-b border-l px-3 py-2 text-right font-medium"
                >
                  %
                </th>
              </tr>
            </thead>
            <tbody>
              {journal.data.students.map((student) => (
                <tr key={student.userId} className="hover:bg-surface-sunken/60">
                  <th
                    scope="row"
                    className="bg-surface-raised border-border sticky left-0 z-10 border-r border-b px-3 py-2 text-left font-normal whitespace-nowrap"
                  >
                    <span className="text-text font-medium">{student.fullName}</span>
                    {student.groupCode !== undefined && (
                      <span className="text-caption text-text-muted ml-2 tracking-normal">
                        {student.groupCode}
                      </span>
                    )}
                  </th>
                  {journal.data.sessions.map((session) => {
                    const mark = student.marks[session.id]
                    return (
                      <td key={session.id} className="border-border border-b px-1 py-2 text-center">
                        {mark === undefined ? (
                          <span className="text-text-muted" aria-label={t('teaching.unmarked')}>
                            ·
                          </span>
                        ) : (
                          <span
                            title={t(`attendance.status.${mark}`)}
                            className={cn('inline-block min-w-7 rounded-sm font-semibold', markTone[mark])}
                          >
                            {t(`teaching.markShort.${mark}`)}
                          </span>
                        )}
                      </td>
                    )
                  })}
                  <td
                    className={cn(
                      'border-border border-b border-l px-3 py-2 text-right font-semibold tabular-nums',
                      (student.totals.percent ?? 100) < 75 ? 'text-danger' : 'text-text',
                    )}
                  >
                    {student.totals.percent === undefined ? '—' : `${Math.round(student.totals.percent)}%`}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <p className="text-small text-text-muted">{t('teaching.journalLegend')}</p>
    </div>
  )
}

function SessionHead({ date, pairNo }: { date: string; pairNo: number | undefined }) {
  const locale = useLocale()
  return (
    <span className="grid leading-tight tabular-nums">
      <span className="text-caption text-text-muted tracking-normal">{formatWeekdayShort(date, locale)}</span>
      <span className="text-text font-semibold">
        {formatDate(`${date}T12:00:00Z`, locale).replace(/ \d{4}$/, '')}
      </span>
      {pairNo !== undefined && (
        <span className="text-caption text-text-muted tracking-normal">#{pairNo}</span>
      )}
    </span>
  )
}
