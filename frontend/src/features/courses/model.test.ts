import { describe, expect, it } from 'vitest'
import type { components } from '@/shared/api/schema'
import { componentKindOf, narrowLessonSteps, narrowMidterm } from './model'

type Schemas = components['schemas']

describe('narrowLessonSteps', () => {
  it('keeps the state of each step', () => {
    const raw: Schemas['LessonStepsView'] = { lecture: 'DONE', test: 'TODO', practice: 'NONE' }
    expect(narrowLessonSteps(raw)).toEqual({ lecture: 'DONE', test: 'TODO', practice: 'NONE' })
  })

  it('treats an absent step, or all of them, as NONE', () => {
    expect(narrowLessonSteps(undefined)).toEqual({ lecture: 'NONE', test: 'NONE', practice: 'NONE' })
    expect(narrowLessonSteps({ lecture: 'DONE' })).toEqual({
      lecture: 'DONE',
      test: 'NONE',
      practice: 'NONE',
    })
  })
})

describe('narrowMidterm', () => {
  it('is absent when the course has no midterm', () => {
    expect(narrowMidterm(undefined)).toBeUndefined()
  })

  it('narrows the standing of the student', () => {
    const midterm = narrowMidterm({
      activityId: 'a1',
      quizId: 'q1',
      opensAt: '2026-11-02T00:00:00Z',
      afterLessonId: 'l8',
      passed: false,
      attemptsUsed: 1,
      attemptsAllowed: 2,
    })
    expect(midterm).toEqual({
      activityId: 'a1',
      quizId: 'q1',
      opensAt: '2026-11-02T00:00:00Z',
      afterLessonId: 'l8',
      passed: false,
      attemptsUsed: 1,
      attemptsAllowed: 2,
    })
  })

  it('counts no attempts when none is reported', () => {
    expect(narrowMidterm({ passed: true })).toEqual({ passed: true, attemptsUsed: 0 })
  })
})

describe('componentKindOf', () => {
  it('names what a scheme component is', () => {
    expect(componentKindOf('JN')).toBe('CONTINUOUS')
    expect(componentKindOf('ON')).toBe('MIDTERM')
    expect(componentKindOf('YaN')).toBe('FINAL')
  })

  it('knows nothing of an unknown code or of no code', () => {
    expect(componentKindOf('X1')).toBeUndefined()
    expect(componentKindOf(undefined)).toBeUndefined()
  })
})
