import type { ReactNode } from 'react'
import { BookOpen, CalendarClock, MapPin, UserRound, UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatLongDay, formatTimeOfDay, formatWeekdayShort } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { Sheet, SheetBody, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { SkeletonText } from '@/shared/ui/skeleton'
import { isHeld, type ClassItem } from './model'
import { useClassTopicQuery } from './queries'

type ClassSheetProps = {
  /** The class shown; `undefined` closes the sheet. */
  item: ClassItem | undefined
  /** The other half of a moved class within the same week: where it went, or where it came from. */
  twin: ClassItem | undefined
  onClose: () => void
  /** Where the linked lesson opens, for whoever is looking. */
  lessonHref: (courseId: string, lessonId: string) => string
  /** Where the course opens. */
  courseHref: (courseId: string) => string
  /** What else can be done with the class (a teacher starts the class session from here). */
  actions?: (item: ClassItem) => ReactNode
}

/** S-02.4 — one class in full: when, where, with whom, what it is about, and what changed. */
export function ClassSheet({ item, twin, onClose, lessonHref, courseHref, actions }: ClassSheetProps) {
  return (
    <Sheet
      open={item !== undefined}
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <SheetContent>
        {item !== undefined && (
          <ClassDetails
            item={item}
            twin={twin}
            lessonHref={lessonHref}
            courseHref={courseHref}
            actions={actions}
          />
        )}
      </SheetContent>
    </Sheet>
  )
}

type ClassDetailsProps = Omit<ClassSheetProps, 'item' | 'onClose'> & { item: ClassItem }

function ClassDetails({ item, twin, lessonHref, courseHref, actions }: ClassDetailsProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const topic = useClassTopicQuery(item.entryId, item.date, locale)
  const held = isHeld(item.status)
  const slotOfTwin =
    twin === undefined
      ? undefined
      : `${formatWeekdayShort(twin.date, locale)}, ${formatTimeOfDay(twin.startsAt)}${
          twin.roomLabel !== undefined ? `, ${twin.roomLabel}` : ''
        }`

  return (
    <>
      <SheetHeader className="pr-14">
        <SheetTitle>{item.courseTitle}</SheetTitle>
        <SheetDescription>{formatLongDay(item.date, locale)}</SheetDescription>
      </SheetHeader>
      <SheetBody className="grid content-start gap-5">
        <div className="flex flex-wrap items-center gap-2">
          <SessionTypeBadge type={item.sessionType} label={t(`sessionType.${item.sessionType}`)} />
          {item.status !== 'SCHEDULED' && (
            <Badge tone={held ? 'info' : 'danger'}>{t(`classStatus.${item.status}`)}</Badge>
          )}
          {item.subgroup !== undefined && <Badge>{t('timetable.subgroup', { name: item.subgroup })}</Badge>}
        </div>

        {item.status !== 'SCHEDULED' && (item.changeReason !== undefined || slotOfTwin !== undefined) && (
          <Callout variant={held ? 'INFO' : 'WARNING'}>
            {item.changeReason}
            {slotOfTwin !== undefined && (
              <span className="block font-medium">
                {item.status === 'MOVED_OUT'
                  ? t('timetable.movedTo', { slot: slotOfTwin })
                  : t('timetable.movedFrom', { slot: slotOfTwin })}
              </span>
            )}
          </Callout>
        )}

        <dl className="text-body grid gap-3">
          <Fact icon={<CalendarClock aria-hidden className="size-5" />} label={t('timetable.time')}>
            <span className="tabular-nums">
              {formatTimeOfDay(item.startsAt)}–{formatTimeOfDay(item.endsAt)}
            </span>
            {item.pairNo !== undefined && (
              <span className="text-text-muted"> · {t('timetable.pair', { number: item.pairNo })}</span>
            )}
          </Fact>
          {item.roomLabel !== undefined && (
            <Fact icon={<MapPin aria-hidden className="size-5" />} label={t('timetable.room')}>
              {item.roomLabel}
            </Fact>
          )}
          {item.teacherName !== undefined && (
            <Fact icon={<UserRound aria-hidden className="size-5" />} label={t('courses.teacher')}>
              {item.teacherName}
            </Fact>
          )}
          {item.groups.length > 0 && (
            <Fact icon={<UsersRound aria-hidden className="size-5" />} label={t('timetable.groups')}>
              {item.groups.join(', ')}
            </Fact>
          )}
        </dl>

        <section className="border-border grid gap-2 border-t pt-5">
          <h3 className="text-caption tracking-caption text-text-muted font-medium uppercase">
            {t('timetable.topic')}
          </h3>
          {topic.isPending && <SkeletonText lines={2} />}
          {topic.isSuccess && (
            <>
              {(topic.data.lessonTitle ?? topic.data.topic) !== undefined ? (
                <p className="font-reading text-body text-text">
                  {topic.data.lessonTitle ?? topic.data.topic}
                </p>
              ) : (
                <p className="text-small text-text-muted">{t('timetable.topicNone')}</p>
              )}
              {topic.data.lessonId !== undefined && item.courseId !== undefined && (
                <Link
                  to={lessonHref(item.courseId, topic.data.lessonId)}
                  className={button({ variant: 'primary', class: 'justify-self-start' })}
                >
                  <BookOpen aria-hidden className="size-4" />
                  {t('timetable.openLesson')}
                </Link>
              )}
            </>
          )}
          {topic.isError && <p className="text-small text-text-muted">{t('timetable.topicNone')}</p>}
        </section>

        <div className="flex flex-wrap gap-2">
          {actions?.(item)}
          {item.courseId !== undefined && (
            <Link to={courseHref(item.courseId)} className={button({ variant: 'outline' })}>
              {t('courses.open')}
            </Link>
          )}
        </div>
      </SheetBody>
    </>
  )
}

function Fact({ icon, label, children }: { icon: ReactNode; label: string; children: ReactNode }) {
  return (
    <div className="flex items-start gap-3">
      <dt className="text-text-muted mt-0.5 shrink-0" title={label}>
        {icon}
        <span className="sr-only">{label}</span>
      </dt>
      <dd className="text-text min-w-0">{children}</dd>
    </div>
  )
}
