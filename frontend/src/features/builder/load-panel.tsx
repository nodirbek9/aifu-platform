import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { ProgressBar } from '@/shared/ui/progress'
import { Skeleton } from '@/shared/ui/skeleton'
import { useLoadPanelQuery, type Scope } from './queries'

/**
 * A-02.6 — the load of the group or teacher on the grid: planned hours against the hours the
 * timetable gives, per course and kind of class, the gap first.
 */
export function LoadPanel({
  timetableId,
  scope,
}: {
  timetableId: string | undefined
  scope: Scope | undefined
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const load = useLoadPanelQuery(timetableId, scope, locale)
  if (scope?.kind === 'room')
    return <p className="text-small text-text-muted">{t('builder.loadNotForRooms')}</p>
  if (load.isPending) return <Skeleton className="h-32" />
  if (load.isError) {
    return (
      <p role="alert" className="text-small text-danger">
        {t(`errors.${toApiProblem(load.error).code}`)}
      </p>
    )
  }
  if (load.data.length === 0) return <p className="text-small text-text-muted">{t('builder.noLoadLines')}</p>
  const rows = load.data.slice().sort((a, b) => (b.remainingHours ?? 0) - (a.remainingHours ?? 0))
  const remaining = rows.reduce((sum, row) => sum + Math.max(0, row.remainingHours ?? 0), 0)
  const excess = (row: (typeof rows)[number]) =>
    Math.max(0, (row.scheduledHours ?? 0) - (row.plannedHours ?? 0))
  const overLines = rows.filter((row) => excess(row) > 0).length
  return (
    <div className="grid grid-cols-1 gap-3">
      <p className={cn('text-small font-semibold', remaining > 0 ? 'text-warning' : 'text-success')}>
        {remaining > 0 ? t('builder.unscheduledHours', { hours: remaining }) : t('admin.allScheduled')}
      </p>
      {overLines > 0 && (
        <p role="status" className="text-small text-danger font-semibold">
          {t('builder.overScheduledLines', { lines: overLines })}
        </p>
      )}
      <ul className="grid grid-cols-1 gap-3">
        {rows.map((row) => (
          <li
            key={row.itemId ?? `${row.courseId ?? ''}-${row.groupId ?? ''}-${row.sessionType ?? ''}`}
            className="grid grid-cols-1 gap-1"
          >
            <span className="text-small text-text truncate font-medium">{row.courseTitle}</span>
            <span className="text-caption text-text-muted tracking-normal">
              {[t(`sessionType.${row.sessionType ?? 'LECTURE'}`), row.groupCode, row.teacherName]
                .filter(Boolean)
                .join(' · ')}
            </span>
            <ProgressBar
              value={Math.min(row.scheduledHours ?? 0, Math.max(1, row.plannedHours ?? 0))}
              max={Math.max(1, row.plannedHours ?? 0)}
              barClassName={excess(row) > 0 ? 'bg-danger' : undefined}
              label={t('builder.hoursOf', {
                scheduled: row.scheduledHours ?? 0,
                planned: row.plannedHours ?? 0,
              })}
            />
            {excess(row) > 0 && (
              <span className="text-caption text-danger tracking-normal">
                {t('builder.overScheduledBy', { hours: excess(row) })}
              </span>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
