import type { components } from '@/shared/api/schema'
import type { SessionType } from '@/shared/ui/badge'

type Schemas = components['schemas']

export type AttendanceStatus = NonNullable<Schemas['MyRecord']['status']>

export type AttendanceRecord = {
  date: string
  pairNo?: number
  sessionType?: SessionType
  status: AttendanceStatus
  /** What the class was about: the linked lesson, else the topic the teacher wrote. */
  topic?: string
}

export type AttendanceTotals = {
  present: number
  late: number
  absent: number
  excused: number
  /** Classes held and marked: the sum of the four above. */
  held: number
  /** The share attended, as the server counts it. */
  percent: number
}

export type CourseAttendance = {
  courseId: string
  courseTitle: string
  totals: AttendanceTotals
  /** Newest first. */
  records: AttendanceRecord[]
}

export function narrowCourseAttendance(raw: Schemas['MyCourseAttendance']): CourseAttendance | null {
  if (raw.courseId === undefined) return null
  const present = raw.totals?.present ?? 0
  const late = raw.totals?.late ?? 0
  const absent = raw.totals?.absent ?? 0
  const excused = raw.totals?.excused ?? 0
  return {
    courseId: raw.courseId,
    courseTitle: raw.courseTitle ?? raw.subjectCode ?? '',
    totals: {
      present,
      late,
      absent,
      excused,
      held: present + late + absent + excused,
      percent: raw.totals?.percent ?? 0,
    },
    records: (raw.records ?? [])
      .flatMap((record) => {
        if (record.date === undefined || record.status === undefined) return []
        const narrowed: AttendanceRecord = { date: record.date, status: record.status }
        if (record.pairNo !== undefined) narrowed.pairNo = record.pairNo
        if (record.sessionType !== undefined) narrowed.sessionType = record.sessionType
        const topic = record.lessonTitle ?? record.topic
        if (topic !== undefined && topic !== '') narrowed.topic = topic
        return [narrowed]
      })
      .sort((a, b) => b.date.localeCompare(a.date) || (b.pairNo ?? 0) - (a.pairNo ?? 0)),
  }
}

/** A class the student was not at, with or without a reason. */
export function isMissed(status: AttendanceStatus): boolean {
  return status === 'ABSENT' || status === 'EXCUSED'
}
