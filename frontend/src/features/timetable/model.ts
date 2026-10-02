import type { components } from '@/shared/api/schema'
import type { CampusNow } from '@/shared/lib/dates'
import type { SessionType } from '@/shared/ui/badge'

type Schemas = components['schemas']
export type ClassStatus = NonNullable<Schemas['ClassView']['status']>

export type ClassItem = {
  entryId: string
  date: string
  pairNo?: number
  startsAt: string
  endsAt: string
  courseId?: string
  courseTitle: string
  sessionType: SessionType
  teacherName?: string
  roomLabel?: string
  groups: string[]
  subgroup?: string
  status: ClassStatus
  changeReason?: string
}

export function narrowClass(raw: Schemas['ClassView']): ClassItem | null {
  if (
    raw.entryId === undefined ||
    raw.date === undefined ||
    raw.startsAt === undefined ||
    raw.endsAt === undefined ||
    raw.sessionType === undefined
  ) {
    return null
  }
  const item: ClassItem = {
    entryId: raw.entryId,
    date: raw.date,
    startsAt: raw.startsAt,
    endsAt: raw.endsAt,
    courseTitle: raw.courseTitle ?? raw.subjectCode ?? '',
    sessionType: raw.sessionType,
    groups: (raw.groups ?? []).flatMap((group) => (group.code !== undefined ? [group.code] : [])),
    status: raw.status ?? 'SCHEDULED',
  }
  if (raw.pairNo !== undefined) item.pairNo = raw.pairNo
  if (raw.courseId !== undefined) item.courseId = raw.courseId
  if (raw.teacherName !== undefined) item.teacherName = raw.teacherName
  if (raw.roomLabel !== undefined) item.roomLabel = raw.roomLabel
  if (raw.subgroup !== undefined) item.subgroup = raw.subgroup
  if (raw.changeReason !== undefined) item.changeReason = raw.changeReason
  return item
}

export type Day = {
  date: string
  holiday?: string
  classes: ClassItem[]
}

export function narrowDay(raw: Schemas['DayView']): Day {
  const day: Day = {
    date: raw.date ?? '',
    classes: (raw.classes ?? [])
      .map(narrowClass)
      .filter((item) => item !== null)
      .sort((a, b) => a.startsAt.localeCompare(b.startsAt)),
  }
  if (raw.holiday !== undefined) day.holiday = raw.holiday
  return day
}

/** A cancelled class, a holiday and the old slot of a moved class are not held (API guide §4). */
export function isHeld(status: ClassStatus): boolean {
  return status === 'SCHEDULED' || status === 'REPLACED' || status === 'MOVED_IN'
}

export type ClassMoment = 'past' | 'now' | 'upcoming'

/** Where a class stands against the campus clock. The server's dates and times compare as text. */
export function classMoment(
  item: Pick<ClassItem, 'date' | 'startsAt' | 'endsAt'>,
  now: CampusNow,
): ClassMoment {
  if (item.date !== now.date) return item.date < now.date ? 'past' : 'upcoming'
  if (now.time >= item.endsAt) return 'past'
  return now.time >= item.startsAt ? 'now' : 'upcoming'
}

/** The class to point at: the one going on, else the first one still ahead — only among those held. */
export function currentOrNext(classes: readonly ClassItem[], now: CampusNow): ClassItem | undefined {
  const held = classes.filter((item) => isHeld(item.status))
  return (
    held.find((item) => classMoment(item, now) === 'now') ??
    held.find((item) => classMoment(item, now) === 'upcoming')
  )
}
