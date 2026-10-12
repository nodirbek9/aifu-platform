import { ChevronRight, ClipboardList, Code2, ListChecks } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { activityPath, type Activity, type ActivityState } from './model'

const stateTone = {
  UPCOMING: 'neutral',
  TODO: 'neutral',
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
  OVERDUE: 'danger',
} as const satisfies Record<ActivityState, 'neutral' | 'info' | 'success' | 'danger'>

function ActivityIcon({ type }: { type: Activity['type'] }) {
  const Icon = type === 'CODING_TASK' ? Code2 : type === 'QUIZ' ? ListChecks : ClipboardList
  return <Icon aria-hidden className="text-text-muted mt-0.5 size-5 shrink-0" />
}

type ActivityRowProps = {
  activity: Activity
  /** Show the course the activity belongs to (lists that span courses). */
  withCourse?: boolean
}

/** One quiz or task in a list: what it is, when it is due, where it stands — and a way in. */
export function ActivityRow({ activity, withCourse = false }: ActivityRowProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const to = activityPath(activity)
  const overdue = activity.state === 'OVERDUE'
  // A quiz that was handed in and did not pass is "not passed", never "in progress".
  const failed =
    activity.state === 'IN_PROGRESS' && activity.type === 'QUIZ' && activity.attemptsUsed > 0
  const shownState = failed ? 'FAILED' : activity.state

  const body = (
    <>
      <ActivityIcon type={activity.type} />
      <div className="grid min-w-0 flex-1 gap-1">
        <p className="text-body text-text font-medium">{activity.title}</p>
        <p className="text-small text-text-muted">
          {t(`activityType.${activity.type}`)}
          {withCourse && ` · ${activity.courseTitle}`}
        </p>
      </div>
      <div className="grid shrink-0 justify-items-end gap-1">
        <Badge tone={failed ? 'danger' : stateTone[activity.state]}>
          {t(`activityState.${shownState}`)}
        </Badge>
        {activity.dueAt !== undefined && activity.state !== 'COMPLETED' && (
          <span
            className={cn(
              'text-caption tracking-normal tabular-nums',
              overdue ? 'text-danger' : 'text-text-muted',
            )}
          >
            {t('dashboard.due', { date: formatDateTime(activity.dueAt, locale) })}
          </span>
        )}
        {activity.bestScore !== undefined && activity.maxPoints !== undefined && (
          <span className="text-caption text-text-muted tracking-normal tabular-nums">
            {activity.bestScore} / {activity.maxPoints}
          </span>
        )}
      </div>
      {to !== null && <ChevronRight aria-hidden className="text-text-muted mt-0.5 size-5 shrink-0" />}
    </>
  )

  const rowClass = 'flex items-start gap-3 py-3'
  if (to === null) return <div className={rowClass}>{body}</div>
  return (
    <Link
      to={to}
      className={cn(
        rowClass,
        'duration-fast ease-standard hover:bg-surface-sunken -mx-2 rounded-sm px-2 transition-[background-color]',
        'focus-visible:outline-focus-ring focus-visible:outline-2',
      )}
    >
      {body}
    </Link>
  )
}
