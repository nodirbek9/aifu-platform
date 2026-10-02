import { Award, CircleCheck, CircleDashed, CircleX } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDate } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ProgressBar } from '@/shared/ui/progress'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import {
  admission,
  formatScore,
  hasHistory,
  totalMax,
  type ComponentGrade,
  type GradeLine,
  type GradeStatus,
  type Gradebook,
} from './model'

const SYMBOLS = ['5', '4', '3', '2'] as const
type Symbol = (typeof SYMBOLS)[number]

function isSymbol(value: string | undefined): value is Symbol {
  return SYMBOLS.some((symbol) => symbol === value)
}

const statusTone = {
  PUBLISHED: 'success',
  REVISED: 'neutral',
  VOIDED: 'danger',
} as const satisfies Record<GradeStatus, 'success' | 'neutral' | 'danger'>

/**
 * S-08 — the grades of one course as an official record: the running total, where the student
 * stands with the final exam, the scheme (JN / ON / YaN and their parts) with earned and maximum
 * points and the pass marks, then the grades themselves and the history of their changes.
 */
export function CourseGradebook({ book }: { book: Gradebook }) {
  const { t } = useTranslation()
  const max = totalMax(book.components)
  const standing = admission(book.components)

  if (book.components.length === 0 && book.entries.length === 0) {
    return (
      <EmptyState
        icon={<Award aria-hidden className="size-7" />}
        title={t('dashboard.gradesEmptyTitle')}
        body={t('dashboard.gradesEmptyBody')}
      />
    )
  }

  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-end gap-x-10 gap-y-4">
        <div className="grid gap-1">
          <p className="text-caption tracking-caption text-text-muted font-medium uppercase">
            {book.complete ? t('grades.finalScore') : t('grades.runningTotal')}
          </p>
          <p className="font-display text-text font-semibold tabular-nums">
            <span className="text-display-xl">{formatScore(book.finalScore ?? 0)}</span>
            {max > 0 && <span className="text-title text-text-muted"> / {formatScore(max)}</span>}
          </p>
        </div>
        <div className="grid min-w-0 flex-1 gap-2 pb-1">
          {book.complete && isSymbol(book.finalSymbol) ? (
            <p className="text-body text-text">
              {t('grades.finalGrade')}:{' '}
              <span className="font-display font-semibold">
                {book.finalSymbol} ({t(`grades.symbol.${book.finalSymbol}`)})
              </span>
            </p>
          ) : (
            <p className="text-small text-text-muted">{t('grades.finalPending')}</p>
          )}
          {standing !== 'none' && (
            <p
              className={cn(
                'text-small flex items-center gap-2 font-medium',
                standing === 'granted' && 'text-success',
                standing === 'denied' && 'text-danger',
                standing === 'pending' && 'text-text-muted',
              )}
            >
              {standing === 'granted' && <CircleCheck aria-hidden className="size-4 shrink-0" />}
              {standing === 'denied' && <CircleX aria-hidden className="size-4 shrink-0" />}
              {standing === 'pending' && <CircleDashed aria-hidden className="size-4 shrink-0" />}
              {t(`grades.admission.${standing}`)}
            </p>
          )}
          {book.complete && book.academicDebt && (
            <p className="text-small text-danger font-medium">{t('grades.academicDebt')}</p>
          )}
        </div>
      </div>

      {book.components.length > 0 && (
        <Table>
          <TableHead>
            <tr>
              <TableHeaderCell>{t('courses.component')}</TableHeaderCell>
              <TableHeaderCell numeric>{t('grades.earned')}</TableHeaderCell>
              <TableHeaderCell className="hidden w-40 sm:table-cell">
                <span className="sr-only">{t('grades.share')}</span>
              </TableHeaderCell>
              <TableHeaderCell className="hidden sm:table-cell">{t('courses.passMark')}</TableHeaderCell>
            </tr>
          </TableHead>
          <TableBody>
            {book.components.map((component) => (
              <ComponentRow key={component.id} component={component} />
            ))}
          </TableBody>
        </Table>
      )}

      {book.entries.length > 0 && (
        <section className="grid gap-2">
          <h3 className="font-display text-body text-text font-semibold">{t('grades.entriesTitle')}</h3>
          <ul className="divide-border border-border divide-y border-y">
            {book.entries.map((line) => (
              <GradeRow key={line.id} line={line} components={book.components} />
            ))}
          </ul>
        </section>
      )}

      {hasHistory(book) && (
        <details className="group">
          <summary
            className={cn(
              'text-small text-primary-hover cursor-pointer rounded-sm font-medium underline-offset-4 hover:underline',
              'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
            )}
          >
            {t('grades.historyTitle')}
          </summary>
          <ul className="divide-border border-border mt-3 divide-y border-y">
            {book.history.map((line) => (
              <GradeRow key={line.id} line={line} components={book.components} withStatus />
            ))}
          </ul>
        </details>
      )}
    </div>
  )
}

function ComponentRow({ component }: { component: ComponentGrade }) {
  const { t } = useTranslation()
  const part = component.parentId !== undefined
  const gate =
    component.minPassPercent === undefined ? null : (
      <span className="flex flex-wrap items-center gap-2">
        <span className="text-text-muted tabular-nums">
          <span className="sm:hidden">{t('courses.passMark')}: </span>
          {formatScore(component.minPassPercent)}%
        </span>
        {component.gatePassed === true && <Badge tone="success">{t('grades.gatePassed')}</Badge>}
        {component.gatePassed === false && <Badge tone="danger">{t('grades.gateFailed')}</Badge>}
      </span>
    )
  return (
    <TableRow>
      <TableCell className={cn(part && 'pl-9')}>
        <span className={cn('text-text', !part && 'font-semibold')}>
          {t(`assessmentKind.${component.kind}`)}
        </span>
        {!part && component.code !== '' && <span className="text-text-muted"> · {component.code}</span>}
        {/* on a phone the pass mark sits under the name: the table has no room for its column */}
        {gate !== null && <div className="text-caption mt-1 tracking-normal sm:hidden">{gate}</div>}
      </TableCell>
      <TableCell numeric className="whitespace-nowrap">
        {component.graded ? (
          <span className={cn('text-text', !part && 'font-semibold')}>{formatScore(component.earned)}</span>
        ) : (
          <span className="text-text-muted">—</span>
        )}
        <span className="text-text-muted"> / {formatScore(component.maxPoints)}</span>
      </TableCell>
      <TableCell className="hidden sm:table-cell">
        {component.graded && (
          <ProgressBar
            value={component.earned}
            max={component.maxPoints}
            barClassName={component.gatePassed === false ? 'bg-danger' : undefined}
          />
        )}
      </TableCell>
      <TableCell className="hidden whitespace-nowrap sm:table-cell">
        {gate ?? <span className="sr-only">{t('grades.noGate')}</span>}
      </TableCell>
    </TableRow>
  )
}

function GradeRow({
  line,
  components,
  withStatus = false,
}: {
  line: GradeLine
  components: readonly ComponentGrade[]
  withStatus?: boolean
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const component = components.find((item) => item.code === line.componentCode)
  const counts = line.status === 'PUBLISHED'
  return (
    <li className="flex flex-wrap items-center gap-x-4 gap-y-1 py-3">
      <div className="grid min-w-0 flex-1 gap-0.5">
        <p className="text-body text-text font-medium">
          {line.title ??
            (component !== undefined
              ? t(`assessmentKind.${component.kind}`)
              : t(`gradeSource.${line.source}`))}
        </p>
        <p className="text-small text-text-muted">
          {line.title !== undefined &&
            component !== undefined &&
            `${t(`assessmentKind.${component.kind}`)} · `}
          {t(`gradeSource.${line.source}`)}
          {line.at !== '' && ` · ${formatDate(line.at, locale)}`}
          {line.revision > 1 && ` · ${t('grades.revision', { number: line.revision })}`}
        </p>
        {line.voidReason !== undefined && <p className="text-small text-text-muted">{line.voidReason}</p>}
      </div>
      {withStatus && <Badge tone={statusTone[line.status]}>{t(`grades.status.${line.status}`)}</Badge>}
      <span
        className={cn(
          'font-display text-title font-semibold tabular-nums',
          counts ? 'text-text' : 'text-text-muted line-through',
        )}
      >
        {formatScore(line.score)}
        <span className="text-small font-sans font-normal"> / {formatScore(line.maxScore)}</span>
      </span>
    </li>
  )
}
