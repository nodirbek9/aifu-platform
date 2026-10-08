import { describe, expect, it } from 'vitest'
import type { components } from '@/shared/api/schema'
import { narrowLesson, narrowLessonPath, nextActionOf } from './model'
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
