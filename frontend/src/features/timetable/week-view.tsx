import { useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateRange, formatTimeOfDay, formatWeekdayShort, useCampusNow } from '@/shared/lib/dates'
import { useMediaQuery } from '@/shared/lib/use-media-query'
import { EmptyState } from '@/shared/ui/empty-state'
import { ScheduleGrid } from '@/shared/ui/schedule-grid'
import { sessionBar, sessionTint } from '@/shared/ui/session-type'
import { ClassRow, type TimetableAudience } from './class-row'
import { ClassSheet } from './class-sheet'
import {
  classKey,
  currentOrNext,
  isHeld,
  nowMark,
  shownDays,
  slotOf,
  type ClassItem,
  type Day,
  type Week,
} from './model'

type WeekViewProps = {
  week: Week
  audience: TimetableAudience
  lessonHref: (courseId: string, lessonId: string) => string
  courseHref: (courseId: string) => string
  /** Extra actions of the class sheet (the teacher's "start class"). */
  actions?: (item: ClassItem) => ReactNode
}

/**
 * A week of classes: the grid of days × pairs from tablet width up, a day strip with the day's
 * list on a phone. Used by the student's and the teacher's timetable alike.
 */
export function WeekView({ week, audience, lessonHref, courseHref, actions }: WeekViewProps) {
  const isWide = useMediaQuery('(min-width: 48rem)')
  const [opened, setOpened] = useState<ClassItem>()
  const now = useCampusNow()
  const days = shownDays(week)

  // the other slot of a moved class, when both fall in this week
  const twin =
    opened !== undefined && (opened.status === 'MOVED_OUT' || opened.status === 'MOVED_IN')
      ? week.days
          .flatMap((day) => day.classes)
          .find(
            (item) =>
              item.entryId === opened.entryId &&
              item.status === (opened.status === 'MOVED_OUT' ? 'MOVED_IN' : 'MOVED_OUT'),
          )
      : undefined

  return (
    <>
      {isWide ? (
        <WeekGrid week={week} days={days} audience={audience} onOpen={setOpened} />
      ) : (
        <DayList key={week.weekStart} days={days} today={now.date} audience={audience} onOpen={setOpened} />
      )}
      <ClassSheet
        item={opened}
        twin={twin}
        onClose={() => {
          setOpened(undefined)
        }}
        lessonHref={lessonHref}
        courseHref={courseHref}
        actions={actions}
      />
    </>
  )
}

type PartProps = {
  days: readonly Day[]
  audience: TimetableAudience
  onOpen: (item: ClassItem) => void
}

function WeekGrid({ week, days, audience, onOpen }: PartProps & { week: Week }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const mark = nowMark(week, now)

  return (
    <ScheduleGrid
      label={t('timetable.gridLabel', { range: formatDateRange(week.weekStart, week.weekEnd, locale) })}
      corner={t('timetable.pairColumn')}
      now={
        mark === undefined
          ? undefined
          : { rowKey: String(mark.pairNo), columnKey: mark.date, progress: mark.progress }
      }
      columns={days.map((day) => ({
        key: day.date,
        highlighted: day.date === now.date,
        off: day.holiday !== undefined,
        header: (
          <div className="grid gap-0.5">
            <span className="flex items-baseline gap-2">
              <span className="text-caption tracking-caption text-text-muted font-medium uppercase">
                {formatWeekdayShort(day.date, locale)}
              </span>
              <span
                className={cn(
                  'font-display text-title font-semibold tabular-nums',
                  day.date === now.date ? 'text-accent-text' : 'text-text',
                )}
              >
                {Number(day.date.slice(8))}
              </span>
            </span>
            {day.holiday !== undefined && (
              <span className="text-caption text-text-muted tracking-normal">{day.holiday}</span>
            )}
          </div>
        ),
      }))}
      rows={week.slots.map((slot) => ({
        key: String(slot.pairNo),
        header: (
          <div className="grid tabular-nums">
            <span className="font-display text-title text-text font-semibold">{slot.pairNo}</span>
            <span className="sr-only">{t('timetable.pair', { number: slot.pairNo })}</span>
            <span className="text-caption text-text-muted tracking-normal">
              {formatTimeOfDay(slot.startsAt)}
            </span>
            <span className="text-caption text-text-muted tracking-normal">
              {formatTimeOfDay(slot.endsAt)}
            </span>
          </div>
        ),
      }))}
      renderCell={(row, column) => {
        const day = days.find((item) => item.date === column.key)
        const classes = (day?.classes ?? []).filter(
          (item) => String(slotOf(item, week.slots)?.pairNo) === row.key,
        )
        if (classes.length === 0) return null
        return (
          <div className="grid gap-1">
            {classes.map((item) => (
              <ClassCard key={classKey(item)} item={item} audience={audience} onOpen={onOpen} />
            ))}
          </div>
        )
      }}
    />
  )
}

/** A class as a card of the grid: the tint and the bar of its type, what, where, with whom. */
function ClassCard({
  item,
  audience,
  onOpen,
}: {
  item: ClassItem
  audience: TimetableAudience
  onOpen: (item: ClassItem) => void
}) {
  const { t } = useTranslation()
  const held = isHeld(item.status)
  const who = audience === 'student' ? item.teacherName : item.groups.join(', ')
  return (
    <button
      type="button"
      onClick={() => {
        onOpen(item)
      }}
      className={cn(
        'relative grid w-full gap-0.5 overflow-hidden rounded-sm py-1.5 pr-2 pl-3 text-left',
        'duration-fast ease-standard hover:shadow-menu transition-[box-shadow,opacity]',
        'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-1',
        held ? sessionTint[item.sessionType] : 'bg-surface-sunken opacity-80',
      )}
    >
      <span aria-hidden className={cn('absolute inset-y-0 left-0 w-1', sessionBar[item.sessionType])} />
      <span
        className={cn(
          'text-small text-text line-clamp-3 leading-snug font-semibold break-words hyphens-auto',
          !held && 'text-text-muted line-through',
        )}
      >
        {item.courseTitle}
      </span>
      <span className="text-caption text-text-muted tracking-normal">
        {t(`sessionType.${item.sessionType}`)}
        {item.roomLabel !== undefined && ` · ${item.roomLabel}`}
        {item.subgroup !== undefined && ` · ${t('timetable.subgroup', { name: item.subgroup })}`}
      </span>
      {who !== undefined && who !== '' && (
        <span className="text-caption text-text-muted truncate tracking-normal">{who}</span>
      )}
      {item.status !== 'SCHEDULED' && (
        <span className={cn('text-caption font-medium tracking-normal', held ? 'text-info' : 'text-danger')}>
          {t(`classStatus.${item.status}`)}
        </span>
      )}
    </button>
  )
}

/** S-02.3 — a phone: the days as a strip, the chosen day's classes as a list. */
function DayList({ days, today, audience, onOpen }: PartProps & { today: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const now = useCampusNow()
  const [selected, setSelected] = useState(
    () =>
      (days.find((day) => day.date === today) ?? days.find((day) => day.classes.length > 0) ?? days[0])?.date,
  )
  const day = days.find((item) => item.date === selected)

  return (
    <div className="grid gap-4">
      <div role="group" aria-label={t('timetable.days')} className="flex gap-1.5">
        {days.map((item) => (
          <button
            key={item.date}
            type="button"
            aria-pressed={item.date === selected}
            aria-current={item.date === today ? 'date' : undefined}
            onClick={() => {
              setSelected(item.date)
            }}
            className={cn(
              'border-border bg-surface-raised relative grid h-14 min-w-0 flex-1 place-content-center justify-items-center rounded-md border',
              'duration-fast ease-standard transition-[background-color,border-color,color]',
              'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
              'aria-pressed:border-primary aria-pressed:bg-primary aria-pressed:text-primary-foreground',
            )}
          >
            <span className="text-caption tracking-normal opacity-80">
              {formatWeekdayShort(item.date, locale)}
            </span>
            <span className="font-display text-body font-semibold tabular-nums">
              {Number(item.date.slice(8))}
            </span>
            {item.classes.some((entry) => isHeld(entry.status)) && (
              <span
                aria-hidden
                className={cn(
                  'absolute bottom-1 size-1 rounded-full',
                  item.date === selected ? 'bg-primary-foreground' : 'bg-accent',
                )}
              />
            )}
          </button>
        ))}
      </div>

      {day?.holiday !== undefined && (
        <p className="bg-surface-sunken text-small text-text rounded-md px-4 py-3 font-medium">
          {t('timetable.holiday')}: {day.holiday}
        </p>
      )}
      {day !== undefined && day.classes.length > 0 && (
        <ol className="divide-border divide-y">
          {day.classes.map((item) => (
            <ClassRow
              key={classKey(item)}
              item={item}
              now={now}
              audience={audience}
              marked={day.date === now.date && currentOrNext(day.classes, now) === item}
              onOpen={onOpen}
            />
          ))}
        </ol>
      )}
      {day?.classes.length === 0 && day.holiday === undefined && (
        <EmptyState title={t('timetable.noClassesDay')} className="border-0 bg-transparent px-0 py-6" />
      )}
    </div>
  )
}
