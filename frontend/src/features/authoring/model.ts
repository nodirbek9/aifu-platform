import type { components } from '@/shared/api/schema'
import { DEFAULT_LOCALE, SUPPORTED_LOCALES, isLocale, type Locale } from '@/shared/i18n'
import type { SessionType } from '@/shared/ui/badge'

type Schemas = components['schemas']

export type PublicationStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
export type BlockType = NonNullable<Schemas['ContentBlockView']['blockType']>
export type BlockVisibility = NonNullable<Schemas['ContentBlockView']['visibility']>
export type JsonObject = Record<string, unknown>

/** The texts of one record in each language it exists in. */
export type Texts<T> = Partial<Record<Locale, T>>

function textsOf<T, R extends { locale?: string }>(
  translations: readonly R[] | undefined,
  pick: (row: R) => T,
): Texts<T> {
  const texts: Texts<T> = {}
  for (const row of translations ?? []) {
    if (isLocale(row.locale)) texts[row.locale] = pick(row)
  }
  return texts
}

/** The text in the asked language, else Uzbek, else any language there is. */
export function textIn<T>(texts: Texts<T>, locale: Locale): T | undefined {
  return texts[locale] ?? texts[DEFAULT_LOCALE] ?? SUPPORTED_LOCALES.map((l) => texts[l]).find(Boolean)
}

/** The languages a record still lacks among those the course requires. */
export function missingLocales<T>(texts: Texts<T>, required: readonly Locale[]): Locale[] {
  return required.filter((locale) => texts[locale] === undefined)
}

function asObject(value: unknown): JsonObject {
  return typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as JsonObject) : {}
}

// ------------------------------------------------------------------ the course

export type HourBudgetLine = { sessionType: SessionType; planned: number; inLessons: number }

export type AuthoringCourse = {
  id: string
  subjectId?: string
  requiredLocales: Locale[]
  allocations: { sessionType: SessionType; hours: number }[]
  components: { id: string; code: string; maxPoints: number; parentId?: string }[]
}

export function narrowAuthoringCourse(raw: Schemas['CourseDetail']): AuthoringCourse {
  if (raw.id === undefined) throw new Error('CourseDetail without id')
  const required = (raw.requiredLocales ?? []).filter(isLocale)
  return {
    id: raw.id,
    ...(raw.subjectId !== undefined && { subjectId: raw.subjectId }),
    requiredLocales: required.length > 0 ? required : [DEFAULT_LOCALE],
    allocations: (raw.sessionAllocations ?? []).flatMap((row) =>
      row.sessionType === undefined ? [] : [{ sessionType: row.sessionType, hours: row.allocatedHours ?? 0 }],
    ),
    components: (raw.assessmentComponents ?? [])
      .slice()
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
      .flatMap((row) =>
        row.id === undefined
          ? []
          : [
              {
                id: row.id,
                code: row.code ?? '',
                maxPoints: row.maxPoints ?? 0,
                ...(row.parentComponentId !== undefined && { parentId: row.parentComponentId }),
              },
            ],
      ),
  }
}

/**
 * The hours of each kind of class the syllabus plans against the hours its lessons carry: a line
 * for each kind where the two differ (spec T-04.1, the hour-budget warnings).
 */
export function hourBudget(course: AuthoringCourse, lessons: readonly EditorLesson[]): HourBudgetLine[] {
  const inLessons = new Map<SessionType, number>()
  for (const lesson of lessons) {
    if (lesson.status === 'ARCHIVED') continue
    inLessons.set(lesson.sessionType, (inLessons.get(lesson.sessionType) ?? 0) + (lesson.hours ?? 0))
  }
  const types = new Set<SessionType>([...course.allocations.map((a) => a.sessionType), ...inLessons.keys()])
  return [...types]
    .map((sessionType) => ({
      sessionType,
      planned: course.allocations.find((a) => a.sessionType === sessionType)?.hours ?? 0,
      inLessons: inLessons.get(sessionType) ?? 0,
    }))
    .filter((line) => line.planned !== line.inLessons)
}

// ------------------------------------------------------------------ modules and lessons

export type EditorModule = {
  id: string
  code?: string
  position: number
  status: PublicationStatus
  texts: Texts<{ title: string; description?: string }>
}

export function narrowModule(raw: Schemas['ModuleDetail']): EditorModule | null {
  if (raw.id === undefined || raw.status === undefined) return null
  return {
    id: raw.id,
    ...(raw.code !== undefined && { code: raw.code }),
    position: raw.position ?? 0,
    status: raw.status,
    texts: textsOf(raw.translations, (row) => ({
      title: row.title ?? '',
      ...(row.description !== undefined && { description: row.description }),
    })),
  }
}

export type EditorBlock = {
  id: string
  type: BlockType
  position: number
  visibility: BlockVisibility
  required: boolean
  fileId?: string
  settings: JsonObject
  payloads: Texts<JsonObject>
}

export function narrowBlock(raw: Schemas['ContentBlockView']): EditorBlock | null {
  if (raw.id === undefined || raw.blockType === undefined) return null
  return {
    id: raw.id,
    type: raw.blockType,
    position: raw.position ?? 0,
    visibility: raw.visibility ?? 'ALL',
    required: raw.required ?? false,
    ...(raw.fileId !== undefined && { fileId: raw.fileId }),
    settings: asObject(raw.settings),
    payloads: textsOf(raw.translations, (row) => asObject(row.payload)),
  }
}

export type EditorLesson = {
  id: string
  moduleId: string
  code?: string
  position: number
  status: PublicationStatus
  sessionType: SessionType
  hours?: number
  estimatedMinutes?: number
  texts: Texts<{ title: string; summary?: string }>
  blocks: EditorBlock[]
  /** Lessons of the course to be done before this one */
  prerequisites: string[]
  /** Learning outcomes of the subject this lesson serves */
  outcomes: string[]
}

export function narrowEditorLesson(raw: Schemas['LessonDetail']): EditorLesson | null {
  if (raw.id === undefined || raw.moduleId === undefined || raw.status === undefined) return null
  if (raw.sessionType === undefined) return null
  return {
    id: raw.id,
    moduleId: raw.moduleId,
    ...(raw.code !== undefined && { code: raw.code }),
    position: raw.position ?? 0,
    status: raw.status,
    sessionType: raw.sessionType,
    ...(raw.allocatedHours !== undefined && { hours: raw.allocatedHours }),
    ...(raw.estimatedMinutes !== undefined && { estimatedMinutes: raw.estimatedMinutes }),
    texts: textsOf(raw.translations, (row) => ({
      title: row.title ?? '',
      ...(row.summary !== undefined && { summary: row.summary }),
    })),
    blocks: (raw.blocks ?? [])
      .map(narrowBlock)
      .filter((block) => block !== null)
      .sort((a, b) => a.position - b.position),
    prerequisites: raw.prerequisiteLessonIds ?? [],
    outcomes: raw.learningOutcomeIds ?? [],
  }
}

/** A title to show for a record in the interface language, or its code when it has none yet. */
export function titleOf(record: { code?: string; texts: Texts<{ title: string }> }, locale: Locale): string {
  const title = textIn(record.texts, locale)?.title
  return title !== undefined && title !== '' ? title : (record.code ?? '—')
}

/**
 * The lesson as the student will see it in one language: each block's payload in that language
 * (else Uzbek, else any), flagged as a fallback when it is not the asked one — the shape the
 * student renderer reads.
 */
export function previewLesson(lesson: EditorLesson, locale: Locale): Schemas['LessonDetail'] {
  return {
    id: lesson.id,
    moduleId: lesson.moduleId,
    sessionType: lesson.sessionType,
    ...(lesson.code !== undefined && { code: lesson.code }),
    ...(lesson.estimatedMinutes !== undefined && { estimatedMinutes: lesson.estimatedMinutes }),
    translations: SUPPORTED_LOCALES.flatMap((l) => {
      const text = lesson.texts[l]
      return text === undefined ? [] : [{ locale: l, title: text.title }]
    }),
    blocks: lesson.blocks
      .filter((block) => block.visibility !== 'TEACHER_ONLY' && block.type !== 'LESSON_PLAN')
      .map((block) => {
        const payload = textIn(block.payloads, locale)
        return {
          id: block.id,
          blockType: block.type,
          position: block.position,
          settings: block.settings,
          ...(block.fileId !== undefined && { fileId: block.fileId }),
          ...(payload !== undefined && { payload }),
          payloadIsFallback: block.payloads[locale] === undefined,
        }
      }),
  }
}

/** Moves one item of a list up or down by one place; the list is unchanged at either end. */
export function moved<T>(items: readonly T[], index: number, delta: -1 | 1): T[] {
  const target = index + delta
  if (index < 0 || target < 0 || target >= items.length) return items.slice()
  const next = items.slice()
  const [item] = next.splice(index, 1)
  if (item !== undefined) next.splice(target, 0, item)
  return next
}

/** Moves the item at `from` to `to` (drag and drop). */
export function movedTo<T>(items: readonly T[], from: number, to: number): T[] {
  if (from === to || from < 0 || to < 0 || from >= items.length || to >= items.length) return items.slice()
  const next = items.slice()
  const [item] = next.splice(from, 1)
  if (item !== undefined) next.splice(to, 0, item)
  return next
}

// ------------------------------------------------------------------ block payloads

export type TimelineStep = { minutes: number; title: string; notes?: string }

/** Reads the timeline of a lesson plan payload, ignoring what is malformed. */
export function readTimeline(payload: JsonObject): TimelineStep[] {
  const raw = payload.timeline
  if (!Array.isArray(raw)) return []
  return raw.flatMap((step) => {
    const object = asObject(step)
    const minutes = typeof object.minutes === 'number' ? object.minutes : 0
    const title = typeof object.title === 'string' ? object.title : ''
    const notes = typeof object.notes === 'string' ? object.notes : undefined
    return [{ minutes, title, ...(notes !== undefined && { notes }) }]
  })
}

/** The block types a teacher adds, grouped as the "add a block" menu shows them. */
export const BLOCK_GROUPS: readonly { key: string; types: readonly BlockType[] }[] = [
  { key: 'text', types: ['TEXT', 'CALLOUT', 'CODE_EXAMPLE', 'TABLE', 'CASE_STUDY'] },
  { key: 'media', types: ['IMAGE', 'DIAGRAM', 'VIDEO', 'AUDIO', 'FILE', 'EMBED', 'LINK'] },
  { key: 'practice', types: ['QUIZ_REF', 'CODING_TASK_REF', 'ACTIVITY_REF'] },
  { key: 'teacher', types: ['LESSON_PLAN'] },
]

export const FILE_BLOCKS: readonly BlockType[] = ['IMAGE', 'DIAGRAM', 'VIDEO', 'AUDIO', 'FILE']
export const REF_BLOCKS: readonly BlockType[] = ['QUIZ_REF', 'CODING_TASK_REF', 'ACTIVITY_REF']

export function isFileBlock(type: BlockType): boolean {
  return FILE_BLOCKS.includes(type)
}

export function isRefBlock(type: BlockType): boolean {
  return REF_BLOCKS.includes(type)
}

/** What a block needs before the server accepts it: its settings, and a file for a media block. */
export function initialSettings(type: BlockType): JsonObject {
  switch (type) {
    case 'CALLOUT':
      return { variant: 'INFO' }
    case 'CODE_EXAMPLE':
      return { language: 'python' }
    case 'EMBED':
      return { provider: 'YOUTUBE', externalId: '' }
    case 'LINK':
      return { url: '' }
    default:
      return {}
  }
}

/** A YouTube or Vimeo address pasted whole is reduced to the video's id, which is what is stored. */
export function videoIdFrom(value: string): string {
  const trimmed = value.trim()
  const youtube = /(?:youtu\.be\/|v=|embed\/|shorts\/)([\w-]{6,})/.exec(trimmed)
  if (youtube?.[1] !== undefined) return youtube[1]
  const vimeo = /vimeo\.com\/(?:video\/)?(\d+)/.exec(trimmed)
  if (vimeo?.[1] !== undefined) return vimeo[1]
  return trimmed
}

export function acceptFor(type: BlockType): string | undefined {
  switch (type) {
    case 'IMAGE':
    case 'DIAGRAM':
      return 'image/*'
    case 'VIDEO':
      return 'video/*'
    case 'AUDIO':
      return 'audio/*'
    default:
      return undefined
  }
}
