import { useRef, useState, type KeyboardEvent } from 'react'
import { Download, History } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { admission, formatScore, type ComponentGrade, type GradeLine } from '@/features/grades/model'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { cn } from '@/shared/lib/cn'
import { downloadXlsx } from '@/shared/lib/xlsx'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Sheet, SheetBody, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { Skeleton } from '@/shared/ui/skeleton'
import { useCourseGradebookQuery, useRecordGrade, useVoidGrade, type GradebookRow } from './queries'

type Column = Pick<ComponentGrade, 'id' | 'code' | 'maxPoints' | 'parentId'>

/** The columns of the gradebook: the scheme of the course, each part after its component. */
function columnsOf(rows: readonly GradebookRow[]): Column[] {
  return (rows[0]?.book.components ?? []).map(({ id, code, maxPoints, parentId }) => ({
    id,
    code,
    maxPoints,
    ...(parentId !== undefined && { parentId }),
  }))
}

/**
 * T-08 — the gradebook of a course: students × the components of its assessment scheme, the
 * final score, grade and admission as the server computes them. A cell opens the grades behind
 * it: record a grade, void one with a reason, see the history (grades are never erased).
 * Arrow keys move between cells; Enter opens one.
 */
export function TeacherGradebook({ courseId, courseTitle }: { courseId: string; courseTitle: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const gradebook = useCourseGradebookQuery(courseId, locale)
  const [group, setGroup] = useState('all')
  const [open, setOpen] = useState<{ studentId: string; componentId: string }>()
  const grid = useRef<HTMLTableElement>(null)

  if (gradebook.isPending) return <Skeleton className="h-96" />
  if (gradebook.isError) {
    return (
      <ErrorState
        problem={toApiProblem(gradebook.error)}
        onRetry={() => {
          void gradebook.refetch()
        }}
      />
    )
  }

  const all = gradebook.data
  const groups = [
    ...new Set(all.flatMap((row) => (row.groupCode === undefined ? [] : [row.groupCode]))),
  ].sort()
  const rows = group === 'all' ? all : all.filter((row) => row.groupCode === group)
  const columns = columnsOf(all)
  const openRow = all.find((row) => row.studentId === open?.studentId)
  const openComponent = openRow?.book.components.find((component) => component.id === open?.componentId)

  const moveFocus = (event: KeyboardEvent<HTMLTableElement>) => {
    const cell = (event.target as HTMLElement).closest<HTMLElement>('[data-cell]')
    if (cell === null) return
    const [row, col] = (cell.dataset.cell ?? '0:0').split(':').map(Number)
    if (row === undefined || col === undefined) return
    const delta: Record<string, [number, number]> = {
      ArrowUp: [-1, 0],
      ArrowDown: [1, 0],
      ArrowLeft: [0, -1],
      ArrowRight: [0, 1],
    }
    const step = delta[event.key]
    if (step === undefined) return
    event.preventDefault()
    grid.current?.querySelector<HTMLElement>(`[data-cell="${row + step[0]}:${col + step[1]}"]`)?.focus()
  }

  const exportExcel = () => {
    const header = [
      t('gradebook.student'),
      t('profile.group'),
      ...columns.map((column) => `${column.code} (${formatScore(column.maxPoints)})`),
      t('grades.finalScore'),
      t('grades.finalGrade'),
    ]
    const body = rows.map((row) => [
      row.fullName,
      row.groupCode ?? '',
      ...columns.map((column) => {
        const component = row.book.components.find((item) => item.id === column.id)
        return component?.graded === true ? Math.round(component.earned * 100) / 100 : null
      }),
      row.book.finalScore ?? null,
      row.book.finalSymbol ?? '',
    ])
    downloadXlsx(`${courseTitle} — ${t('gradebook.title')}`, [
      { name: t('gradebook.title'), rows: [header, ...body] },
    ])
  }

  return (
    <div className="grid gap-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="flex flex-wrap items-end gap-3">
          <Select
            className="w-48"
            label={t('profile.group')}
            value={group}
            onValueChange={setGroup}
            options={[
              { value: 'all', label: t('gradebook.allGroups') },
              ...groups.map((code) => ({ value: code, label: code })),
            ]}
          />
          <p className="text-small text-text-muted pb-2">{t('gradebook.keyboardHint')}</p>
        </div>
        <Button variant="outline" onClick={exportExcel} disabled={rows.length === 0}>
          <Download aria-hidden className="size-4" />
          {t('teaching.exportExcel')}
        </Button>
      </div>

      {rows.length === 0 ? (
        <EmptyState title={t('gradebook.empty')} body={t('gradebook.emptyBody')} />
      ) : (
        <div className="border-border max-h-[70vh] overflow-auto rounded-lg border">
          <table
            ref={grid}
            className="text-small w-full border-separate border-spacing-0"
            onKeyDown={moveFocus}
          >
            <caption className="sr-only">{t('gradebook.caption', { course: courseTitle })}</caption>
            <thead className="bg-surface-sunken sticky top-0 z-20">
              <tr>
                <th
                  scope="col"
                  className="bg-surface-sunken border-border text-text sticky left-0 z-30 min-w-52 border-b px-3 py-2 text-left font-semibold"
                >
                  {t('gradebook.student')}
                </th>
                {columns.map((column) => (
                  <th
                    key={column.id}
                    scope="col"
                    className={cn(
                      'border-border text-text border-b px-3 py-2 text-right font-semibold whitespace-nowrap tabular-nums',
                      column.parentId !== undefined && 'text-text-muted font-medium',
                    )}
                  >
                    {column.code}
                    <span className="text-caption text-text-muted block font-normal">
                      {t('gradebook.outOf', { max: formatScore(column.maxPoints) })}
                    </span>
                  </th>
                ))}
                <th
                  scope="col"
                  className="border-border text-text border-b px-3 py-2 text-right font-semibold"
                >
                  {t('grades.finalScore')}
                </th>
                <th
                  scope="col"
                  className="border-border text-text border-b px-3 py-2 text-left font-semibold"
                >
                  {t('gradebook.standing')}
                </th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row, rowIndex) => {
                const standing = admission(row.book.components)
                return (
                  <tr key={row.studentId} className="hover:bg-surface-sunken/60">
                    <th
                      scope="row"
                      className="bg-surface-raised border-border text-text sticky left-0 z-10 border-b px-3 py-2 text-left font-medium"
                    >
                      {row.fullName}
                      {row.groupCode !== undefined && (
                        <span className="text-caption text-text-muted block font-normal">
                          {row.groupCode}
                        </span>
                      )}
                    </th>
                    {columns.map((column, colIndex) => {
                      const component = row.book.components.find((item) => item.id === column.id)
                      return (
                        <td key={column.id} className="border-border border-b p-0 text-right">
                          <button
                            type="button"
                            data-cell={`${rowIndex}:${colIndex}`}
                            tabIndex={rowIndex === 0 && colIndex === 0 ? 0 : -1}
                            aria-label={t('gradebook.cellLabel', {
                              student: row.fullName,
                              component: column.code,
                              value:
                                component?.graded === true
                                  ? `${formatScore(component.earned)} / ${formatScore(column.maxPoints)}`
                                  : t('gradebook.notGraded'),
                            })}
                            onClick={() => {
                              setOpen({ studentId: row.studentId, componentId: column.id })
                            }}
                            className={cn(
                              'focus-visible:outline-focus-ring hover:bg-surface-sunken h-11 w-full px-3 tabular-nums focus-visible:outline-2 focus-visible:-outline-offset-2',
                              component?.gatePassed === false && 'text-danger font-semibold',
                              component?.graded !== true && 'text-text-muted',
                            )}
                          >
                            {component?.graded === true ? formatScore(component.earned) : '—'}
                          </button>
                        </td>
                      )
                    })}
                    <td className="border-border text-text border-b px-3 py-2 text-right font-semibold tabular-nums">
                      {row.book.finalScore === undefined ? '—' : formatScore(row.book.finalScore)}
                      {row.book.finalSymbol !== undefined && row.book.complete && (
                        <span className="text-caption text-text-muted ml-1">({row.book.finalSymbol})</span>
                      )}
                    </td>
                    <td className="border-border border-b px-3 py-2">
                      <span className="flex flex-wrap gap-1">
                        {standing !== 'none' && (
                          <Badge
                            tone={
                              standing === 'granted'
                                ? 'success'
                                : standing === 'denied'
                                  ? 'danger'
                                  : 'neutral'
                            }
                          >
                            {t(`gradebook.admission.${standing}`)}
                          </Badge>
                        )}
                        {row.book.academicDebt && <Badge tone="danger">{t('gradebook.debt')}</Badge>}
                      </span>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <Sheet
        open={open !== undefined}
        onOpenChange={(next) => {
          if (!next) setOpen(undefined)
        }}
      >
        <SheetContent>
          {openRow !== undefined && openComponent !== undefined && (
            <GradeCell courseId={courseId} row={openRow} component={openComponent} />
          )}
        </SheetContent>
      </Sheet>
    </div>
  )
}

function GradeCell({
  courseId,
  row,
  component,
}: {
  courseId: string
  row: GradebookRow
  component: ComponentGrade
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const record = useRecordGrade(courseId)
  const voidGrade = useVoidGrade(courseId)
  const [score, setScore] = useState('')
  const [max, setMax] = useState(formatScore(component.maxPoints))
  const [voiding, setVoiding] = useState<string>()
  const [reason, setReason] = useState('')
  const current = row.book.entries.filter((line) => line.componentCode === component.code)
  const history = row.book.history.filter(
    (line) => line.componentCode === component.code && line.status !== 'PUBLISHED',
  )
  const scoreValue = Number(score)
  const maxValue = Number(max)
  const valid =
    score !== '' && Number.isFinite(scoreValue) && maxValue > 0 && scoreValue >= 0 && scoreValue <= maxValue

  const line = (entry: GradeLine, canVoid: boolean) => (
    <li key={entry.id} className="border-border grid gap-1 rounded-md border px-3 py-2">
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-body text-text font-semibold tabular-nums">
          {formatScore(entry.score)} / {formatScore(entry.maxScore)}
        </span>
        <Badge
          tone={entry.status === 'PUBLISHED' ? 'success' : entry.status === 'VOIDED' ? 'danger' : 'neutral'}
        >
          {t(`grades.status.${entry.status}`)}
        </Badge>
        <span className="text-caption text-text-muted">{t(`gradebook.source.${entry.source}`)}</span>
        {canVoid && (
          <Button
            variant="ghost"
            size="sm"
            className="ml-auto"
            onClick={() => {
              setVoiding(entry.id)
              setReason('')
            }}
          >
            {t('gradebook.void')}
          </Button>
        )}
      </div>
      <p className="text-caption text-text-muted tracking-normal">
        {entry.title !== undefined && `${entry.title} · `}
        {entry.at !== '' && formatDateTime(entry.at, locale)}
        {entry.revision > 1 && ` · ${t('grades.revision', { number: entry.revision })}`}
      </p>
      {entry.voidReason !== undefined && (
        <p className="text-small text-text">{t('gradebook.voidReason', { reason: entry.voidReason })}</p>
      )}
      {voiding === entry.id && (
        <form
          className="grid gap-2 pt-1"
          onSubmit={(event) => {
            event.preventDefault()
            if (reason.trim() === '') return
            voidGrade.mutate(
              { entryId: entry.id, reason: reason.trim() },
              {
                onSuccess: () => {
                  setVoiding(undefined)
                },
              },
            )
          }}
        >
          <Input
            label={t('gradebook.reason')}
            value={reason}
            onChange={(event) => {
              setReason(event.target.value)
            }}
          />
          <div className="flex gap-2">
            <Button
              type="submit"
              variant="danger"
              size="sm"
              disabled={reason.trim() === ''}
              loading={voidGrade.isPending}
            >
              {t('gradebook.confirmVoid')}
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setVoiding(undefined)
              }}
            >
              {t('common.cancel')}
            </Button>
          </div>
        </form>
      )}
    </li>
  )

  return (
    <>
      <SheetHeader>
        <SheetTitle>
          {row.fullName} · {component.code}
        </SheetTitle>
        <SheetDescription>
          {component.graded
            ? t('gradebook.earnedOf', {
                earned: formatScore(component.earned),
                max: formatScore(component.maxPoints),
              })
            : t('gradebook.notGraded')}
        </SheetDescription>
      </SheetHeader>
      <SheetBody className="grid content-start gap-6">
        <form
          className="grid gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            if (!valid) return
            record.mutate(
              {
                enrollmentId: row.book.enrollmentId,
                componentId: component.id,
                score: scoreValue,
                maxScore: maxValue,
              },
              {
                onSuccess: () => {
                  setScore('')
                },
              },
            )
          }}
        >
          <h3 className="font-display text-h3 text-text font-semibold">{t('gradebook.record')}</h3>
          <p className="text-small text-text-muted">{t('gradebook.recordHint')}</p>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label={t('gradebook.score')}
              type="number"
              min={0}
              step="any"
              inputMode="decimal"
              value={score}
              onChange={(event) => {
                setScore(event.target.value)
              }}
            />
            <Input
              label={t('gradebook.maxScore')}
              type="number"
              min={0}
              step="any"
              value={max}
              onChange={(event) => {
                setMax(event.target.value)
              }}
            />
          </div>
          {(record.isError || voidGrade.isError) && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(record.error ?? voidGrade.error).code}`)}
            </p>
          )}
          <Button type="submit" className="justify-self-start" disabled={!valid} loading={record.isPending}>
            {t('gradebook.save')}
          </Button>
        </form>

        <section className="grid gap-2">
          <h3 className="font-display text-h3 text-text font-semibold">{t('grades.entriesTitle')}</h3>
          {current.length === 0 ? (
            <p className="text-small text-text-muted">{t('gradebook.noEntries')}</p>
          ) : (
            <ul className="grid gap-2">{current.map((entry) => line(entry, true))}</ul>
          )}
        </section>
        {history.length > 0 && (
          <section className="grid gap-2">
            <h3 className="font-display text-h3 text-text flex items-center gap-2 font-semibold">
              <History aria-hidden className="size-4" />
              {t('grades.historyTitle')}
            </h3>
            <ul className="grid gap-2">{history.map((entry) => line(entry, false))}</ul>
          </section>
        )}
      </SheetBody>
    </>
  )
}
