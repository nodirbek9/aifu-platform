import type { Role } from '@/features/auth/model'
import type { components } from '@/shared/api/schema'

type Schemas = components['schemas']
type NotificationView = Schemas['NotificationView']

export type NotificationType = NonNullable<NotificationView['type']>
export type TimetableChangeKind = 'CANCELLED' | 'MOVED' | 'REPLACED'

/** What a notification is about, as the contract types it; every field is there or not by type. */
export type NotificationContext = {
  courseId?: string
  announcementId?: string
  title?: string
  quizId?: string
  codingTaskId?: string
  kind?: TimetableChangeKind
  date?: string
  pairNo?: number
  newDate?: string
  newPairNo?: number
  newRoom?: string
  reason?: string
  finalScore?: string
}

export type NotificationItem = {
  id: string
  type: NotificationType
  createdAt: string
  read: boolean
  context: NotificationContext
}

function isChangeKind(value: string | undefined): value is TimetableChangeKind {
  return value === 'CANCELLED' || value === 'MOVED' || value === 'REPLACED'
}

export function narrowNotification(raw: NotificationView): NotificationItem | null {
  if (raw.id === undefined || raw.type === undefined || raw.createdAt === undefined) return null
  const source = raw.context ?? {}
  const context: NotificationContext = {}
  if (source.courseId !== undefined) context.courseId = source.courseId
  if (source.announcementId !== undefined) context.announcementId = source.announcementId
  if (source.title !== undefined && source.title !== '') context.title = source.title
  if (source.quizId !== undefined) context.quizId = source.quizId
  if (source.codingTaskId !== undefined) context.codingTaskId = source.codingTaskId
  if (isChangeKind(source.kind)) context.kind = source.kind
  if (source.date !== undefined) context.date = source.date
  if (source.pairNo !== undefined) context.pairNo = source.pairNo
  if (source.newDate !== undefined) context.newDate = source.newDate
  if (source.newPairNo !== undefined) context.newPairNo = source.newPairNo
  if (source.newRoom !== undefined) context.newRoom = source.newRoom
  if (source.reason !== undefined && source.reason !== '') context.reason = source.reason
  if (source.finalScore !== undefined) context.finalScore = source.finalScore
  return { id: raw.id, type: raw.type, createdAt: raw.createdAt, read: raw.readAt !== undefined, context }
}

/**
 * Where a notification leads, for the role the reader is in. A teacher's and an administrator's
 * own pages arrive in later phases: until then only what exists is linked, never a dead address.
 */
export function notificationPath(
  item: Pick<NotificationItem, 'type' | 'context'>,
  role: Role,
): string | null {
  const { context } = item
  if (item.type === 'ANNOUNCEMENT') return '/announcements'
  if (item.type === 'TIMETABLE_CHANGED') {
    const date = context.newDate ?? context.date
    const section = role === 'ADMIN' ? null : role === 'TEACHER' ? '/teacher/timetable' : '/student/timetable'
    if (section === null) return null
    return date === undefined ? section : `${section}?date=${date}`
  }
  if (role !== 'STUDENT') return null
  switch (item.type) {
    case 'ENROLLMENT_CREATED':
      return context.courseId === undefined ? '/student/courses' : `/student/courses/${context.courseId}`
    case 'GRADE_PUBLISHED':
      return '/student/grades'
    case 'TEACHER_FEEDBACK':
    case 'SUBMISSION_GRADED':
    case 'QUIZ_GRADED':
      if (context.courseId === undefined) return null
      if (context.codingTaskId !== undefined)
        return `/student/courses/${context.courseId}/tasks/${context.codingTaskId}`
      if (context.quizId !== undefined)
        return `/student/courses/${context.courseId}/quizzes/${context.quizId}`
      return `/student/courses/${context.courseId}?tab=work`
    case 'DEADLINE_REMINDER':
      return '/student/assignments'
    case 'LESSON_COMPLETED':
      return context.courseId === undefined ? null : `/student/courses/${context.courseId}`
    default:
      return null
  }
}
