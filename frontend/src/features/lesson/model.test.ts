import { describe, expect, it } from 'vitest'
import type { components } from '@/shared/api/schema'
import {
  evaluatePollingRetry,
  hasCoursePlanChanged,
  hasLessonPathChanged,
  narrowLesson,
  narrowLessonPath,
  nextActionOf,
} from './model'
type Schemas = components['schemas']

const lesson = (blocks: Schemas['ContentBlockView'][]): Schemas['LessonDetail'] => ({
  id: 'lesson-1',
  code: 'A1',
  sessionType: 'PRACTICAL',
  translations: [
    { locale: 'uz', title: 'Kompilyatorlar' },
    { locale: 'ru', title: 'Компиляторы' },
  ],
  blocks,
})

const text = (position: number, payloadIsFallback: boolean): Schemas['ContentBlockView'] => ({
  id: `text-${position}`,
  position,
  blockType: 'TEXT',
  payload: { html: '<p>…</p>' },
  payloadIsFallback,
})

describe('the untranslated-text mark of a lesson', () => {
  it('is not set by a reference to a task, which has no text of its own', () => {
    const reference: Schemas['ContentBlockView'] = {
      id: 'ref-1',
      position: 2,
      blockType: 'CODING_TASK_REF',
      settings: { refId: 'task-1' },
      payload: {},
      payloadIsFallback: true,
    }
    expect(narrowLesson(lesson([text(1, false), reference]), 'ru').hasFallback).toBe(false)
  })

  it('is set by a text block shown in Uzbek', () => {
    expect(narrowLesson(lesson([text(1, true)]), 'ru').hasFallback).toBe(true)
  })

  it('preserves block positions when narrowing', () => {
    const narrowed = narrowLesson(lesson([text(5, false)]), 'uz')
    expect(narrowed.blocks[0]?.position).toBe(5)
  })
})

describe('narrowLessonPath', () => {
  it('narrows a fully populated path view', () => {
    const path = narrowLessonPath({
      pathEnabled: true,
      open: false,
      steps: { lecture: 'DONE', test: 'TODO', practice: 'NONE' },
      activeReadingSeconds: 300,
      requiredReadingSeconds: 600,
      reachedEnd: true,
      testActivityId: 'a-test',
      practiceActivityId: 'a-practice',
      lockReason: 'PREVIOUS_LESSON_INCOMPLETE',
    })
    expect(path).toEqual({
      pathEnabled: true,
      open: false,
      steps: { lecture: 'DONE', test: 'TODO', practice: 'NONE' },
      activeReadingSeconds: 300,
      requiredReadingSeconds: 600,
      reachedEnd: true,
      testActivityId: 'a-test',
      practiceActivityId: 'a-practice',
      lockReason: 'PREVIOUS_LESSON_INCOMPLETE',
    })
  })

  it('gives safe defaults when the view or its fields are absent', () => {
    expect(narrowLessonPath(undefined)).toEqual({
      pathEnabled: false,
      open: true,
      steps: { lecture: 'NONE', test: 'NONE', practice: 'NONE' },
      activeReadingSeconds: 0,
      requiredReadingSeconds: 0,
      reachedEnd: false,
    })
  })
})

describe('nextActionOf', () => {
  it('points at the first step still to do', () => {
    expect(nextActionOf({ lecture: 'TODO', test: 'TODO', practice: 'TODO' })).toBe('KEEP_READING')
    expect(nextActionOf({ lecture: 'DONE', test: 'TODO', practice: 'TODO' })).toBe('TAKE_TEST')
    expect(nextActionOf({ lecture: 'DONE', test: 'DONE', practice: 'TODO' })).toBe('PRACTICE')
  })

  it('is done when nothing is left, steps without content included', () => {
    expect(nextActionOf({ lecture: 'DONE', test: 'NONE', practice: 'NONE' })).toBe('ALL_DONE')
  })
})

describe('hasLessonPathChanged', () => {
  const base = {
    pathEnabled: true,
    open: true,
    steps: { lecture: 'DONE' as const, test: 'TODO' as const, practice: 'TODO' as const },
    activeReadingSeconds: 300,
    requiredReadingSeconds: 300,
    reachedEnd: true,
  }

  it('detects step change from TODO to DONE', () => {
    const after = {
      ...base,
      steps: { lecture: 'DONE' as const, test: 'DONE' as const, practice: 'TODO' as const },
    }
    expect(hasLessonPathChanged(base, after)).toBe(true)
  })

  it('detects lock status change', () => {
    const locked = { ...base, open: false, lockReason: 'PREVIOUS_LESSON_INCOMPLETE' as const }
    expect(hasLessonPathChanged(locked, base)).toBe(true)
  })

  it('returns false when relevant properties are identical', () => {
    const same = { ...base, activeReadingSeconds: 315 }
    expect(hasLessonPathChanged(base, same)).toBe(false)
  })

  it('returns false when after is missing', () => {
    expect(hasLessonPathChanged(base, undefined)).toBe(false)
  })
})

describe('hasCoursePlanChanged', () => {
  const basePlan = {
    courseId: 'c1',
    enrollmentId: 'e1',
    lessonsTotal: 10,
    lessonsCompleted: 3,
    pathEnabled: true,
    modules: [
      {
        id: 'm1',
        title: 'Module 1',
        lessons: [
          {
            id: 'l1',
            title: 'Lesson 1',
            sessionType: 'LECTURE' as const,
            status: 'COMPLETED' as const,
            locked: false,
            waived: false,
            steps: { lecture: 'DONE' as const, test: 'DONE' as const, practice: 'NONE' as const },
            prerequisiteLessonIds: [],
            activitiesTotal: 1,
            activitiesCompleted: 1,
          },
          {
            id: 'l2',
            title: 'Lesson 2',
            sessionType: 'PRACTICAL' as const,
            status: 'IN_PROGRESS' as const,
            locked: false,
            waived: false,
            steps: { lecture: 'DONE' as const, test: 'TODO' as const, practice: 'TODO' as const },
            prerequisiteLessonIds: [],
            activitiesTotal: 2,
            activitiesCompleted: 0,
          },
        ],
      },
    ],
  }

  it('detects when lessonsCompleted increases', () => {
    const updated = { ...basePlan, lessonsCompleted: 4 }
    expect(hasCoursePlanChanged(basePlan, updated)).toBe(true)
  })

  it('detects when midterm is passed', () => {
    const withMidterm = {
      ...basePlan,
      midterm: { passed: false, attemptsUsed: 1 },
    }
    const passedMidterm = {
      ...basePlan,
      midterm: { passed: true, attemptsUsed: 1 },
    }
    expect(hasCoursePlanChanged(withMidterm, passedMidterm)).toBe(true)
  })

  it('detects when an individual lesson step updates in modules', () => {
    const updated = {
      ...basePlan,
      modules: basePlan.modules.map((module) => ({
        ...module,
        lessons: module.lessons.map((lesson) =>
          lesson.id === 'l2'
            ? {
                ...lesson,
                steps: { lecture: 'DONE' as const, test: 'DONE' as const, practice: 'TODO' as const },
              }
            : lesson,
        ),
      })),
    }
    expect(hasCoursePlanChanged(basePlan, updated)).toBe(true)
  })

  it('returns false when plan content is identical', () => {
    expect(hasCoursePlanChanged(basePlan, { ...basePlan })).toBe(false)
  })
})

describe('evaluatePollingRetry', () => {
  it('stops immediately when change is detected', () => {
    expect(evaluatePollingRetry(1, 5, true)).toEqual({ shouldStop: true, hasChanged: true })
    expect(evaluatePollingRetry(3, 5, true)).toEqual({ shouldStop: true, hasChanged: true })
  })

  it('continues polling when change is not detected before maxAttempts', () => {
    expect(evaluatePollingRetry(1, 5, false)).toEqual({ shouldStop: false, hasChanged: false })
    expect(evaluatePollingRetry(4, 5, false)).toEqual({ shouldStop: false, hasChanged: false })
  })

  it('stops polling when maxAttempts is reached without change', () => {
    expect(evaluatePollingRetry(5, 5, false)).toEqual({ shouldStop: true, hasChanged: false })
    expect(evaluatePollingRetry(6, 5, false)).toEqual({ shouldStop: true, hasChanged: false })
  })
})
