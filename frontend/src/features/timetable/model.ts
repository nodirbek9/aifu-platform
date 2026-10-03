import type { components } from '@/shared/api/schema'
import { weekdayOf, type CampusNow } from '@/shared/lib/dates'
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
  subjectCode?: string
  sessionType: SessionType
  teacherName?: string
  teacherId?: string
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
  if (raw.subjectCode !== undefined) item.subjectCode = raw.subjectCode
  if (raw.teacherName !== undefined) item.teacherName = raw.teacherName
  if (raw.teacherUserId !== undefined) item.teacherId = raw.teacherUserId
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

export type PairSlot = { pairNo: number; startsAt: string; endsAt: string }

export type Week = {
  /** Monday and Sunday of the week, as campus dates. */
  weekStart: string
  weekEnd: string
  /** The number of the week in the term and whether it is odd or even — the server's count. */
  weekNumber?: number
  parity?: 'ODD' | 'EVEN'
  termName?: string
  slots: PairSlot[]
  days: Day[]
}

export function narrowWeek(raw: Schemas['WeekView']): Week {
  const week: Week = {
    weekStart: raw.weekStart ?? '',
    weekEnd: raw.weekEnd ?? '',
    slots: (raw.slots ?? [])
      .flatMap((slot) =>
        slot.pairNo === undefined || slot.startsAt === undefined || slot.endsAt === undefined
          ? []
          : [{ pairNo: slot.pairNo, startsAt: slot.startsAt, endsAt: slot.endsAt }],
      )
      .sort((a, b) => a.pairNo - b.pairNo),
    days: (raw.days ?? []).map(narrowDay).sort((a, b) => a.date.localeCompare(b.date)),
  }
  if (raw.weekNumber !== undefined) week.weekNumber = raw.weekNumber
  if (raw.parity === 'ODD' || raw.parity === 'EVEN') week.parity = raw.parity
  if (raw.termName !== undefined) week.termName = raw.termName
  return week
}

/** Monday to Saturday; Sunday only in a week that has a class on it. */
export function shownDays(week: Pick<Week, 'days'>): Day[] {
  return week.days.filter((day) => weekdayOf(day.date) !== 7 || day.classes.length > 0)
}

/** The pair a class sits in: by its number, else by its start time. */
export function slotOf(
  item: Pick<ClassItem, 'pairNo' | 'startsAt'>,
  slots: readonly PairSlot[],
): PairSlot | undefined {
  return (
    slots.find((slot) => slot.pairNo === item.pairNo) ??
    slots.find((slot) => slot.startsAt === item.startsAt) ??
    slots.find((slot) => slot.endsAt > item.startsAt)
  )
}

/** A class as one row of a list or one card of a cell; the old and the new slot of a moved class differ. */
export function classKey(item: Pick<ClassItem, 'entryId' | 'date' | 'status' | 'startsAt'>): string {
  return `${item.entryId}:${item.date}:${item.startsAt}:${item.status}`
}

function seconds(time: string): number {
  const [hours = '0', minutes = '0', rest = '0'] = time.split(':')
  return Number(hours) * 3600 + Number(minutes) * 60 + Number(rest)
}

export type NowMark = { date: string; pairNo: number; progress: number }

/**
 * Where the "now" line goes in a week: on today's column, in the pair going on at the height the
 * clock is at, or at the top of the next pair during a break. Nothing outside the bell schedule,
 * and nothing in a week that is not this one.
 */
export function nowMark(week: Pick<Week, 'days' | 'slots'>, now: CampusNow): NowMark | undefined {
  if (!week.days.some((day) => day.date === now.date)) return undefined
  const first = week.slots[0]
  if (first === undefined || now.time < first.startsAt) return undefined
  for (const slot of week.slots) {
    if (now.time < slot.startsAt) return { date: now.date, pairNo: slot.pairNo, progress: 0 }
    if (now.time < slot.endsAt) {
      const progress =
        (seconds(now.time) - seconds(slot.startsAt)) / (seconds(slot.endsAt) - seconds(slot.startsAt))
      return { date: now.date, pairNo: slot.pairNo, progress }
    }
  }
  return undefined
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
