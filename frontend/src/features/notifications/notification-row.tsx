import {
  Bell,
  BookOpen,
  CalendarClock,
  GraduationCap,
  Megaphone,
  MessageSquareText,
  type LucideIcon,
} from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { courseQuery } from '@/features/courses/queries'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatLongDay, formatRelativeTime } from '@/shared/lib/dates'
import { notificationPath, type NotificationItem, type NotificationType } from './model'
import { useMarkReadMutation } from './queries'
import { usePairName } from '@/features/timetable/use-pair-name'

const ICONS: Partial<Record<NotificationType, LucideIcon>> = {
  ANNOUNCEMENT: Megaphone,
  GRADE_PUBLISHED: GraduationCap,
  QUIZ_GRADED: GraduationCap,
  SUBMISSION_GRADED: GraduationCap,
  TEACHER_FEEDBACK: MessageSquareText,
  TIMETABLE_CHANGED: CalendarClock,
  ENROLLMENT_CREATED: BookOpen,
  LESSON_COMPLETED: BookOpen,
}

type NotificationRowProps = {
  item: NotificationItem
  /** Called after the row was opened (the bell closes its popover). */
  onOpened?: () => void
  dense?: boolean
}

/**
 * One notification: what happened, to what, when. Opening it marks it read and goes to the
 * thing it is about, when the reader's role has a page for it.
 */
export function NotificationRow({ item, onOpened, dense = false }: NotificationRowProps) {
  const { t } = useTranslation()
  const pairName = usePairName()
  const locale = useLocale()
  const navigate = useNavigate()
  const { activeRole } = useAuth()
  const role = activeRole ?? 'STUDENT'
  const markRead = useMarkReadMutation()
  // the course is named by its title; a course the reader cannot open simply stays unnamed
  const course = useQuery({
    ...courseQuery(item.context.courseId ?? '', locale),
    enabled: item.context.courseId !== undefined,
    retry: false,
  })
  const to = notificationPath(item, role)
  const Icon = ICONS[item.type] ?? Bell
  const { context } = item

  const headline =
    item.type === 'ANNOUNCEMENT' && context.title !== undefined
      ? context.title
      : item.type === 'TIMETABLE_CHANGED' && context.kind !== undefined
        ? t(`notification.timetable.${context.kind}`)
        : t(`notification.type.${item.type}`)

  const details: string[] = []
  if (item.type === 'ANNOUNCEMENT') details.push(t('notification.type.ANNOUNCEMENT'))
  if (course.data !== undefined) details.push(course.data.title)
  if (item.type === 'TIMETABLE_CHANGED' && context.date !== undefined) {
    const from = [
      formatLongDay(context.date, locale),
      ...(context.pairNo !== undefined ? [pairName(context.pairNo)] : []),
    ].join(', ')
    details.push(from)
    if (context.newDate !== undefined) {
      const moved = [
        formatLongDay(context.newDate, locale),
        ...(context.newPairNo !== undefined ? [pairName(context.newPairNo)] : []),
        ...(context.newRoom !== undefined ? [context.newRoom] : []),
      ].join(', ')
      details.push(`→ ${moved}`)
    }
  }
  if (item.type === 'GRADE_PUBLISHED' && context.finalScore !== undefined) {
    details.push(t('notification.totalNow', { score: Number(context.finalScore) }))
  }

  const open = () => {
    if (!item.read) markRead.mutate(item.id)
    if (to !== null) void navigate(to)
    onOpened?.()
  }

  const body = (
    <>
      <span
        className={cn(
          'mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full',
          item.read ? 'bg-surface-sunken text-text-muted' : 'bg-accent-soft text-accent-text',
        )}
      >
        <Icon aria-hidden className="size-4" />
      </span>
      <span className="grid min-w-0 flex-1 gap-0.5 text-left">
        <span className={cn('text-small text-text', !item.read && 'font-semibold')}>{headline}</span>
        {details.length > 0 && (
          <span className={cn('text-small text-text-muted', dense && 'line-clamp-2')}>
            {details.join(' · ')}
          </span>
        )}
        {!dense && context.reason !== undefined && (
          <span className="text-small text-text-muted">{context.reason}</span>
        )}
        <span className="text-caption text-text-muted tracking-normal">
          {formatRelativeTime(item.createdAt, locale)}
        </span>
      </span>
      {!item.read && (
        <span className="bg-accent mt-2 size-2 shrink-0 rounded-full">
          <span className="sr-only">{t('notification.unread')}</span>
        </span>
      )}
    </>
  )

  const rowClass = cn('flex w-full items-start gap-3 rounded-sm', dense ? 'px-4 py-2.5' : 'px-2 py-3')
  if (to === null && item.read) return <div className={rowClass}>{body}</div>
  return (
    <button
      type="button"
      onClick={open}
      className={cn(
        rowClass,
        'duration-fast ease-standard hover:bg-surface-sunken transition-[background-color]',
        'focus-visible:outline-focus-ring focus-visible:outline-2',
      )}
    >
      {body}
    </button>
  )
}
