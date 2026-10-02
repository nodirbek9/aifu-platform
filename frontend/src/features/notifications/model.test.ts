import { describe, expect, it } from 'vitest'
import { narrowNotification, notificationPath } from './model'

describe('where a notification leads', () => {
  it('a moved class opens the timetable on the week it moved to', () => {
    const item = {
      type: 'TIMETABLE_CHANGED' as const,
      context: { date: '2026-10-19', newDate: '2026-10-20' },
    }
    expect(notificationPath(item, 'STUDENT')).toBe('/student/timetable?date=2026-10-20')
    expect(notificationPath(item, 'TEACHER')).toBe('/teacher/timetable?date=2026-10-20')
  })

  it('a cancelled class opens the week it was in', () => {
    const item = { type: 'TIMETABLE_CHANGED' as const, context: { date: '2026-10-16' } }
    expect(notificationPath(item, 'STUDENT')).toBe('/student/timetable?date=2026-10-16')
  })

  it("feedback opens the work it is about: the task, the quiz, else the course's work", () => {
    expect(
      notificationPath({ type: 'TEACHER_FEEDBACK', context: { courseId: 'c', codingTaskId: 't' } }, 'STUDENT'),
    ).toBe('/student/courses/c/tasks/t')
    expect(
      notificationPath({ type: 'TEACHER_FEEDBACK', context: { courseId: 'c', quizId: 'q' } }, 'STUDENT'),
    ).toBe('/student/courses/c/quizzes/q')
    expect(notificationPath({ type: 'TEACHER_FEEDBACK', context: { courseId: 'c' } }, 'STUDENT')).toBe(
      '/student/courses/c?tab=work',
    )
  })

  it('a grade opens the grades; an announcement the announcements, for anyone', () => {
    expect(notificationPath({ type: 'GRADE_PUBLISHED', context: {} }, 'STUDENT')).toBe('/student/grades')
    expect(notificationPath({ type: 'ANNOUNCEMENT', context: {} }, 'ADMIN')).toBe('/announcements')
  })

  it('leads nowhere rather than to a page the role does not have', () => {
    expect(notificationPath({ type: 'GRADE_PUBLISHED', context: {} }, 'TEACHER')).toBeNull()
    expect(notificationPath({ type: 'ACCOUNT_STATUS_CHANGED', context: {} }, 'STUDENT')).toBeNull()
  })
})

describe('what the server sends, narrowed', () => {
  it('reads the typed context and whether it was read', () => {
    const item = narrowNotification({
      id: 'n',
      type: 'TIMETABLE_CHANGED',
      createdAt: '2026-10-01T10:00:00Z',
      context: { courseId: 'c', kind: 'MOVED', date: '2026-10-19', newDate: '2026-10-20', newPairNo: 3 },
    })
    expect(item?.read).toBe(false)
    expect(item?.context).toEqual({
      courseId: 'c',
      kind: 'MOVED',
      date: '2026-10-19',
      newDate: '2026-10-20',
      newPairNo: 3,
    })
  })

  it('a change kind of a newer server is dropped; a notification without context still shows', () => {
    const item = narrowNotification({
      id: 'n',
      type: 'TIMETABLE_CHANGED',
      createdAt: '2026-10-01T10:00:00Z',
      readAt: '2026-10-01T11:00:00Z',
      context: { kind: 'SPLIT' },
    })
    expect(item?.context.kind).toBeUndefined()
    expect(item?.read).toBe(true)
    expect(narrowNotification({ id: 'n', type: 'ANNOUNCEMENT', createdAt: 'x' })?.context).toEqual({})
  })
})
