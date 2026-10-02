import type { SessionType } from './badge'

/** The strong step of each session type as a fill — the bar at the left of a class or a lesson. */
export const sessionBar = {
  LECTURE: 'bg-session-lecture-strong',
  SEMINAR: 'bg-session-seminar-strong',
  PRACTICAL: 'bg-session-practical-strong',
  LABORATORY: 'bg-session-laboratory-strong',
  INDEPENDENT_STUDY: 'bg-session-independent-strong',
  COURSE_WORK: 'bg-session-coursework-strong',
} as const satisfies Record<SessionType, string>

/** The tint of each session type — the background of a timetable cell. */
export const sessionTint = {
  LECTURE: 'bg-session-lecture-tint',
  SEMINAR: 'bg-session-seminar-tint',
  PRACTICAL: 'bg-session-practical-tint',
  LABORATORY: 'bg-session-laboratory-tint',
  INDEPENDENT_STUDY: 'bg-session-independent-tint',
  COURSE_WORK: 'bg-session-coursework-tint',
} as const satisfies Record<SessionType, string>
