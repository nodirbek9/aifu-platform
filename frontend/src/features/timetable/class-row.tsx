import { MapPin, UserRound, UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { formatTimeOfDay, type CampusNow } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { sessionBar } from '@/shared/ui/session-type'
import { classMoment, isHeld, splitCourseTitle, type ClassItem } from './model'

/** Whose timetable it is: a student is told the teacher, a teacher the groups. */
export type TimetableAudience = 'student' | 'teacher'

type ClassRowProps = {
  item: ClassItem
  now: CampusNow
  /** The class going on, or the next one: it carries the "now" / "next" mark. */
  marked?: boolean
  audience?: TimetableAudience
  /** Makes the row a button that opens the class. */
  onOpen?: (item: ClassItem) => void
}

/** One class as a line of a day: time, the colour of its type, what, where, with whom. */
export function ClassRow({ item, now, marked = false, audience = 'student', onOpen }: ClassRowProps) {
  const { t } = useTranslation()
  const held = isHeld(item.status)
  const moment = classMoment(item, now)
  const parsedTitle = splitCourseTitle(item.courseTitle)

  const body = (
    <>
      <div className="text-small w-12 shrink-0 pt-0.5 tabular-nums">
        <div className="text-text font-semibold">{formatTimeOfDay(item.startsAt)}</div>
        <div className="text-text-muted">{formatTimeOfDay(item.endsAt)}</div>
      </div>
      <div
        aria-hidden
        className={cn('w-1 shrink-0 self-stretch rounded-full', sessionBar[item.sessionType])}
      />
      <div className="grid min-w-0 flex-1 gap-1.5">
        <div className="flex flex-wrap items-start justify-between gap-x-3 gap-y-1">
          <div className="min-w-0 flex-1" title={item.courseTitle}>
            <p
              className={cn(
                'font-display text-body text-text line-clamp-2 font-semibold break-words',
                !held && 'line-through',
              )}
            >
              {parsedTitle.subject}
            </p>
            {parsedTitle.termSuffix !== undefined && (
              <p className={cn('text-caption text-text-muted font-normal', !held && 'line-through')}>
                {parsedTitle.termSuffix}
              </p>
            )}
          </div>
          {marked && held && (
            <Badge tone="accent">{moment === 'now' ? t('dashboard.now') : t('dashboard.next')}</Badge>
          )}
          {item.status !== 'SCHEDULED' && (
            <Badge tone={held ? 'info' : 'danger'}>{t(`classStatus.${item.status}`)}</Badge>
          )}
        </div>
        <div className="text-small text-text-muted flex flex-wrap items-center gap-x-4 gap-y-1">
          <SessionTypeBadge type={item.sessionType} label={t(`sessionType.${item.sessionType}`)} />
          {item.roomLabel !== undefined && (
            <span className="inline-flex items-center gap-1.5">
              <MapPin aria-hidden className="size-4" />
              {item.roomLabel}
            </span>
          )}
          {audience === 'student' && item.teacherName !== undefined && (
            <span className="inline-flex min-w-0 items-center gap-1.5">
              <UserRound aria-hidden className="size-4 shrink-0" />
              <span className="truncate">{item.teacherName}</span>
            </span>
          )}
          {audience === 'teacher' && item.groups.length > 0 && (
            <span className="inline-flex min-w-0 items-center gap-1.5">
              <UsersRound aria-hidden className="size-4 shrink-0" />
              <span className="truncate">{item.groups.join(', ')}</span>
            </span>
          )}
          {item.subgroup !== undefined && <span>{t('timetable.subgroup', { name: item.subgroup })}</span>}
        </div>
        {item.changeReason !== undefined && <p className="text-small text-text-muted">{item.changeReason}</p>}
      </div>
    </>
  )

  const rowClass = cn('flex gap-4 py-3 text-left')
  if (onOpen === undefined) return <li className={rowClass}>{body}</li>
  return (
    <li className="-mx-2">
      <button
        type="button"
        onClick={() => {
          onOpen(item)
        }}
        className={cn(
          rowClass,
          'duration-fast ease-standard hover:bg-surface-sunken w-full rounded-sm px-2 transition-[background-color]',
          'focus-visible:outline-focus-ring focus-visible:outline-2',
        )}
      >
        {body}
      </button>
    </li>
  )
}
