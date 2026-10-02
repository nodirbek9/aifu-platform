import { CalendarDays, MapPin, UserRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { classMoment, currentOrNext, isHeld, type ClassItem } from '@/features/timetable/model'
import { useTodayQuery } from '@/features/timetable/queries'
import { toApiProblem } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { formatTimeOfDay, useCampusNow, type CampusNow } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { sessionBar } from '@/shared/ui/session-type'
import { Skeleton } from '@/shared/ui/skeleton'

/** S-01.1 — today's classes as a timeline; the class going on, or the next one, is the marked one. */
export function TodayBlock() {
  const { t } = useTranslation()
  const today = useTodayQuery()
  const now = useCampusNow()

  return (
    <Card>
      <CardHeader
        title={t('dashboard.todayTitle')}
        action={
          <Link
            to="/student/timetable"
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('dashboard.openTimetable')}
          </Link>
        }
      />
      <CardContent>
        {today.isPending && (
          <div className="grid gap-4">
            <Skeleton className="h-16" />
            <Skeleton className="h-16" />
          </div>
        )}
        {today.isError && (
          <ErrorState
            problem={toApiProblem(today.error)}
            onRetry={() => {
              void today.refetch()
            }}
          />
        )}
        {today.isSuccess && today.data.classes.length === 0 && (
          <EmptyState
            icon={<CalendarDays aria-hidden className="size-7" />}
            title={
              today.data.holiday !== undefined
                ? t('dashboard.holiday', { name: today.data.holiday })
                : t('dashboard.todayEmptyTitle')
            }
            body={t('dashboard.todayEmptyBody')}
            className="border-0 bg-transparent px-0 py-2"
          />
        )}
        {today.isSuccess && today.data.classes.length > 0 && (
          <ol className="divide-border -my-3 divide-y">
            {today.data.classes.map((item) => (
              <ClassRow
                key={`${item.entryId}-${item.status}`}
                item={item}
                now={now}
                marked={currentOrNext(today.data.classes, now)?.entryId === item.entryId}
              />
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  )
}

function ClassRow({ item, now, marked }: { item: ClassItem; now: CampusNow; marked: boolean }) {
  const { t } = useTranslation()
  const held = isHeld(item.status)
  const moment = classMoment(item, now)
  return (
    <li className={cn('flex gap-4 py-3', held && moment === 'past' && 'opacity-60')}>
      <div className="text-small w-12 shrink-0 pt-0.5 tabular-nums">
        <div className="text-text font-semibold">{formatTimeOfDay(item.startsAt)}</div>
        <div className="text-text-muted">{formatTimeOfDay(item.endsAt)}</div>
      </div>
      <div aria-hidden className={cn('w-1 shrink-0 rounded-full', sessionBar[item.sessionType])} />
      <div className="grid min-w-0 flex-1 gap-1.5">
        <div className="flex flex-wrap items-start justify-between gap-x-3 gap-y-1">
          <p className={cn('font-display text-body text-text font-semibold', !held && 'line-through')}>
            {item.courseTitle}
          </p>
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
          {item.teacherName !== undefined && (
            <span className="inline-flex min-w-0 items-center gap-1.5">
              <UserRound aria-hidden className="size-4 shrink-0" />
              <span className="truncate">{item.teacherName}</span>
            </span>
          )}
        </div>
        {item.changeReason !== undefined && <p className="text-small text-text-muted">{item.changeReason}</p>}
      </div>
    </li>
  )
}
