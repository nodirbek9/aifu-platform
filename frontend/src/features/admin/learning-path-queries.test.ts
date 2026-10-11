import { describe, expect, it } from 'vitest'
import {
  buildLearningPathConfigRequest,
  campusDatetimeLocalToIso,
  isoToCampusDatetimeLocal,
  resolveGrantTarget,
  validateGrantReason,
} from './learning-path-queries'

describe('learning-path helpers', () => {
  describe('isoToCampusDatetimeLocal', () => {
    it('returns empty string for empty or undefined input', () => {
      expect(isoToCampusDatetimeLocal(undefined)).toBe('')
      expect(isoToCampusDatetimeLocal('')).toBe('')
      expect(isoToCampusDatetimeLocal('invalid-date')).toBe('')
    })

    it('converts UTC instant to Asia/Tashkent (UTC+5) local string', () => {
      // 2026-10-18T19:00:00Z is 2026-10-19 00:00:00 in Asia/Tashkent
      expect(isoToCampusDatetimeLocal('2026-10-18T19:00:00Z')).toBe('2026-10-19T00:00')
      // 2026-10-19T04:30:00Z is 2026-10-19 09:30:00 in Asia/Tashkent
      expect(isoToCampusDatetimeLocal('2026-10-19T04:30:00Z')).toBe('2026-10-19T09:30')
    })

    it('converts Tashkent ISO string with offset directly', () => {
      expect(isoToCampusDatetimeLocal('2026-10-19T00:00:00+05:00')).toBe('2026-10-19T00:00')
    })
  })

  describe('campusDatetimeLocalToIso', () => {
    it('returns undefined for empty input', () => {
      expect(campusDatetimeLocalToIso(undefined)).toBeUndefined()
      expect(campusDatetimeLocalToIso('')).toBeUndefined()
      expect(campusDatetimeLocalToIso('   ')).toBeUndefined()
    })

    it('formats local string to Tashkent ISO format with +05:00 offset', () => {
      expect(campusDatetimeLocalToIso('2026-10-19T00:00')).toBe('2026-10-19T00:00:00+05:00')
      expect(campusDatetimeLocalToIso('2026-10-25T14:30')).toBe('2026-10-25T14:30:00+05:00')
    })

    it('returns undefined on malformed local string', () => {
      expect(campusDatetimeLocalToIso('not-a-date')).toBeUndefined()
    })
  })

  describe('validateGrantReason', () => {
    it('rejects reasons shorter than 5 chars or whitespace only', () => {
      expect(validateGrantReason('')).toBe(false)
      expect(validateGrantReason('    ')).toBe(false)
      expect(validateGrantReason('four')).toBe(false)
      expect(validateGrantReason('  abc ')).toBe(false)
    })

    it('accepts reasons with at least 5 non-whitespace chars', () => {
      expect(validateGrantReason('12345')).toBe(true)
      expect(validateGrantReason('Illness during midterm')).toBe(true)
      expect(validateGrantReason('  Power outage  ')).toBe(true)
    })
  })

  describe('buildLearningPathConfigRequest', () => {
    it('builds minimal request when disabled and no midterm fields', () => {
      const result = buildLearningPathConfigRequest({
        enabled: false,
      })
      expect(result).toEqual({
        enabled: false,
      })
    })

    it('builds full request with midterm fields and formatted date', () => {
      const result = buildLearningPathConfigRequest({
        enabled: true,
        midtermActivityId: 'act-123',
        midtermAfterLessonId: 'les-456',
        midtermOpensAtLocal: '2026-10-19T00:00',
      })
      expect(result).toEqual({
        enabled: true,
        midtermActivityId: 'act-123',
        midtermAfterLessonId: 'les-456',
        midtermOpensAt: '2026-10-19T00:00:00+05:00',
      })
    })

    it('omits empty string ids', () => {
      const result = buildLearningPathConfigRequest({
        enabled: true,
        midtermActivityId: '',
        midtermAfterLessonId: '',
        midtermOpensAtLocal: '',
      })
      expect(result).toEqual({
        enabled: true,
      })
    })
  })

  describe('resolveGrantTarget', () => {
    const courseId = 'course-1'
    const validEnrollment = { id: 'enr-1', courseId: 'course-1', studentFullName: 'Student One' }
    const validQuiz = { id: 'quiz-1', courseId: 'course-1', title: 'Quiz 1' }
    const otherCourseEnrollment = { id: 'enr-2', courseId: 'course-2', studentFullName: 'Student Two' }
    const otherCourseQuiz = { id: 'quiz-2', courseId: 'course-2', title: 'Quiz 2' }

    it('returns valid when neither enrollment nor quiz target is specified', () => {
      const result = resolveGrantTarget({
        courseId,
      })
      expect(result).toEqual({
        valid: true,
        enrollmentId: undefined,
        quizId: undefined,
      })
    })

    it('returns valid when targeted enrollment and quiz match courseId', () => {
      const result = resolveGrantTarget({
        courseId,
        targetEnrollmentId: 'enr-1',
        targetQuizId: 'quiz-1',
        enrollment: validEnrollment,
        quizzes: [validQuiz],
      })
      expect(result).toEqual({
        valid: true,
        enrollmentId: 'enr-1',
        quizId: 'quiz-1',
      })
    })

    it('returns valid when quiz is resolved via single quiz lookup with matching courseId', () => {
      const result = resolveGrantTarget({
        courseId,
        targetEnrollmentId: 'enr-1',
        targetQuizId: 'quiz-1',
        enrollment: validEnrollment,
        quizzes: [],
        quiz: validQuiz,
      })
      expect(result).toEqual({
        valid: true,
        enrollmentId: 'enr-1',
        quizId: 'quiz-1',
      })
    })

    it('rejects missing or unresolved target enrollment', () => {
      const result = resolveGrantTarget({
        courseId,
        targetEnrollmentId: 'enr-1',
        enrollment: null,
      })
      expect(result).toEqual({
        valid: false,
        error: 'ENROLLMENT_NOT_FOUND',
      })
    })

    it('rejects enrollment whose courseId does not match courseId', () => {
      const result = resolveGrantTarget({
        courseId,
        targetEnrollmentId: 'enr-2',
        enrollment: otherCourseEnrollment,
      })
      expect(result).toEqual({
        valid: false,
        error: 'ENROLLMENT_COURSE_MISMATCH',
      })
    })

    it('rejects quiz when not found in course quizzes or single quiz lookup', () => {
      const result = resolveGrantTarget({
        courseId,
        targetQuizId: 'quiz-unknown',
        quizzes: [validQuiz],
        quiz: null,
      })
      expect(result).toEqual({
        valid: false,
        error: 'QUIZ_NOT_FOUND',
      })
    })

    it('rejects quiz when quiz.courseId does not match courseId in quizzes list', () => {
      const result = resolveGrantTarget({
        courseId,
        targetQuizId: 'quiz-2',
        quizzes: [otherCourseQuiz],
      })
      expect(result).toEqual({
        valid: false,
        error: 'QUIZ_COURSE_MISMATCH',
      })
    })

    it('rejects quiz when single quiz lookup courseId does not match courseId', () => {
      const result = resolveGrantTarget({
        courseId,
        targetQuizId: 'quiz-2',
        quizzes: [],
        quiz: otherCourseQuiz,
      })
      expect(result).toEqual({
        valid: false,
        error: 'QUIZ_COURSE_MISMATCH',
      })
    })

    it('rejects a single quiz lookup that carries no courseId', () => {
      const result = resolveGrantTarget({
        courseId,
        targetQuizId: 'quiz-2',
        quizzes: [],
        quiz: { id: 'quiz-2' },
      })
      expect(result).toEqual({ valid: false, error: 'QUIZ_COURSE_MISMATCH' })
    })
  })
})
