import { describe, expect, it } from 'vitest'
import type { components } from '@/shared/api/schema'
import { narrowLesson } from './model'
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
})
