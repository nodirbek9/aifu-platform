import { narrowClass, type ClassItem } from '@/features/timetable/model'
import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'
import { pickTranslation, readString } from '@/shared/lib/translations'
import type { SessionType } from '@/shared/ui/badge'

type Schemas = components['schemas']

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED'
export const ATTENDANCE_STATUSES: readonly AttendanceStatus[] = ['PRESENT', 'LATE', 'ABSENT', 'EXCUSED']

export type TeacherCourse = {
  id: string
  title: string
  subjectCode: string
  cohortCode?: string
  termName?: string
  status: string
}

export function narrowTeacherCourse(raw: Schemas['CourseSummary']): TeacherCourse | null {
  if (raw.id === undefined) return null
  const course: TeacherCourse = {
    id: raw.id,
    title: raw.title ?? raw.subjectCode ?? '',
    subjectCode: raw.subjectCode ?? '',
    status: raw.status ?? 'PUBLISHED',
  }
  if (raw.cohortCode !== undefined) course.cohortCode = raw.cohortCode
  if (raw.termName !== undefined) course.termName = raw.termName
  return course
}

export type LessonRef = { id: string; title: string; sessionType?: SessionType; published: boolean }

function narrowLessonRef(raw: Schemas['LessonRef'] | undefined): LessonRef | undefined {
  if (raw?.id === undefined) return undefined
  const ref: LessonRef = { id: raw.id, title: raw.title ?? '', published: raw.published !== false }
  if (raw.sessionType !== undefined) ref.sessionType = raw.sessionType
  return ref
}

export type RosterStudent = {
  userId: string
  fullName: string
  studentNumber?: string
  groupCode?: string
  status?: AttendanceStatus
  note?: string
}

export type AttendanceTotals = {
  present: number
  late: number
  absent: number
  excused: number
  unmarked: number
  percent?: number
}

function narrowTotals(raw: Schemas['Totals'] | undefined): AttendanceTotals {
  const totals: AttendanceTotals = {
    present: raw?.present ?? 0,
    late: raw?.late ?? 0,
    absent: raw?.absent ?? 0,
    excused: raw?.excused ?? 0,
    unmarked: raw?.unmarked ?? 0,
  }
  if (raw?.percent !== undefined) totals.percent = raw.percent
  return totals
}

export type ClassSession = {
  info: ClassItem
  held: boolean
  canMarkAttendance: boolean
  lesson?: LessonRef
  suggestedLesson?: LessonRef
  topic: string
  notes: string
  markedAt?: string
  students: RosterStudent[]
  totals: AttendanceTotals
}

export function narrowClassSession(raw: Schemas['ClassSessionView']): ClassSession {
  const info = raw.classInfo === undefined ? null : narrowClass(raw.classInfo)
  if (info === null) throw new Error('Class session without its class')
  const session: ClassSession = {
    info,
    held: raw.held !== false,
    canMarkAttendance: raw.canMarkAttendance === true,
    topic: raw.topic ?? '',
    notes: raw.notes ?? '',
    students: (raw.students ?? []).flatMap((row) => {
      if (row.userId === undefined) return []
      const student: RosterStudent = { userId: row.userId, fullName: row.fullName ?? '' }
      if (row.studentNumber !== undefined) student.studentNumber = row.studentNumber
      if (row.groupCode !== undefined) student.groupCode = row.groupCode
      if (row.status !== undefined) student.status = row.status
      if (row.note !== undefined && row.note !== '') student.note = row.note
      return [student]
    }),
    totals: narrowTotals(raw.totals),
  }
  const lesson = narrowLessonRef(raw.lesson)
  if (lesson !== undefined) session.lesson = lesson
  const suggested = narrowLessonRef(raw.suggestedLesson)
  if (suggested !== undefined) session.suggestedLesson = suggested
  if (raw.attendanceMarkedAt !== undefined) session.markedAt = raw.attendanceMarkedAt
  return session
}

/** The counts of a roster as it is on screen, so they follow every tap before the server answers. */
export function countMarks(students: readonly Pick<RosterStudent, 'status'>[]): AttendanceTotals {
  const totals: AttendanceTotals = { present: 0, late: 0, absent: 0, excused: 0, unmarked: 0 }
  for (const student of students) {
    if (student.status === 'PRESENT') totals.present += 1
    else if (student.status === 'LATE') totals.late += 1
    else if (student.status === 'ABSENT') totals.absent += 1
    else if (student.status === 'EXCUSED') totals.excused += 1
    else totals.unmarked += 1
  }
  return totals
}

export type PlannedClass = {
  info: ClassItem
  weekNumber?: number
  lesson?: LessonRef
  topic?: string
  attendanceMarked: boolean
}

export function narrowPlannedClass(raw: Schemas['PlannedClassView']): PlannedClass | null {
  const info = raw.classInfo === undefined ? null : narrowClass(raw.classInfo)
  if (info === null) return null
  const planned: PlannedClass = { info, attendanceMarked: raw.attendanceMarked === true }
  if (raw.weekNumber !== undefined) planned.weekNumber = raw.weekNumber
  const lesson = narrowLessonRef(raw.lesson)
  if (lesson !== undefined) planned.lesson = lesson
  if (raw.topic !== undefined && raw.topic !== '') planned.topic = raw.topic
  return planned
}

export type JournalSession = {
  /** The column's key: its session once there is one, else the class (entry, date, pair). */
  id: string
  /** Marks are kept by session; a class nothing was saved for has none. */
  sessionId?: string
  /** Whether attendance was marked in the class. */
  taken: boolean
  entryId?: string
  date: string
  pairNo?: number
  sessionType?: SessionType
  groups: string[]
  subgroup?: string
  title?: string
}

export type JournalStudent = {
  userId: string
  fullName: string
  studentNumber?: string
  groupCode?: string
  marks: Partial<Record<string, AttendanceStatus>>
  totals: AttendanceTotals
}

export type Journal = { sessions: JournalSession[]; students: JournalStudent[] }

export function narrowJournal(raw: Schemas['JournalView']): Journal {
  return {
    sessions: (raw.sessions ?? [])
      .flatMap((session) => {
        if (session.date === undefined) return []
        if (session.sessionId === undefined && session.entryId === undefined) return []
        const narrowed: JournalSession = {
          id: session.sessionId ?? `${session.entryId ?? ''}:${session.date}:${session.pairNo ?? 0}`,
          taken: session.attendanceTaken === true,
          date: session.date,
          groups: (session.groups ?? []).flatMap((group) => (group.code === undefined ? [] : [group.code])),
        }
        if (session.sessionId !== undefined) narrowed.sessionId = session.sessionId
        if (session.entryId !== undefined) narrowed.entryId = session.entryId
        if (session.pairNo !== undefined) narrowed.pairNo = session.pairNo
        if (session.sessionType !== undefined) narrowed.sessionType = session.sessionType
        if (session.subgroup !== undefined) narrowed.subgroup = session.subgroup
        const title = session.lessonTitle ?? session.topic
        if (title !== undefined && title !== '') narrowed.title = title
        return [narrowed]
      })
      .sort((a, b) => a.date.localeCompare(b.date) || (a.pairNo ?? 0) - (b.pairNo ?? 0)),
    students: (raw.students ?? []).flatMap((row) => {
      if (row.userId === undefined) return []
      const student: JournalStudent = {
        userId: row.userId,
        fullName: row.fullName ?? '',
        marks: row.marks ?? {},
        totals: narrowTotals(row.totals),
      }
      if (row.studentNumber !== undefined) student.studentNumber = row.studentNumber
      if (row.groupCode !== undefined) student.groupCode = row.groupCode
      return [student]
    }),
  }
}

/* ------------------------------------------------------------ the teacher-only lesson plan */

export type PlanStep = { minutes: number; title: string; notes?: string }

export type LessonPlan = {
  objectives?: string
  timeline: PlanStep[]
  materials?: string
  questions?: string
  homework?: string
}

function narrowTimeline(value: unknown): PlanStep[] {
  if (!Array.isArray(value)) return []
  return value.flatMap((step: unknown) => {
    const title = readString(step, 'title')
    if (title === undefined) return []
    const minutesRaw =
      typeof step === 'object' && step !== null ? (step as Record<string, unknown>).minutes : undefined
    const minutes = typeof minutesRaw === 'number' ? minutesRaw : Number(minutesRaw ?? 0)
    const narrowed: PlanStep = { minutes: Number.isFinite(minutes) ? minutes : 0, title }
    const notes = readString(step, 'notes')
    if (notes !== undefined) narrowed.notes = notes
    return [narrowed]
  })
}

/** The LESSON_PLAN blocks of a lesson, merged: what the teacher needs at the desk, never shown to students. */
export function lessonPlanOf(raw: Schemas['LessonDetail']): LessonPlan | null {
  const blocks = (raw.blocks ?? []).filter((block) => block.blockType === 'LESSON_PLAN')
  if (blocks.length === 0) return null
  const plan: LessonPlan = { timeline: [] }
  for (const block of blocks) {
    const payload = block.payload
    const objectives = readString(payload, 'objectives')
    if (objectives !== undefined) plan.objectives = objectives
    const materials = readString(payload, 'materials')
    if (materials !== undefined) plan.materials = materials
    const questions = readString(payload, 'questions')
    if (questions !== undefined) plan.questions = questions
    const homework = readString(payload, 'homework')
    if (homework !== undefined) plan.homework = homework
    const timeline =
      typeof payload === 'object' && payload !== null
        ? (payload as Record<string, unknown>).timeline
        : undefined
    plan.timeline.push(...narrowTimeline(timeline))
  }
  return plan
}

/* ------------------------------------------------------------ lessons of a course, for picking */

export type CourseLesson = {
  id: string
  title: string
  code?: string
  sessionType?: SessionType
  module: string
}

export function lessonTitle(raw: Schemas['LessonDetail'], locale: Locale): string {
  return pickTranslation(raw.translations, locale)?.value.title ?? raw.code ?? ''
}

/** Whole academic hours of held pairs: a pair is two hours. */
export const HOURS_PER_PAIR = 2
