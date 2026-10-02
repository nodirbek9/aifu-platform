import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'
import { pickTranslation, readString } from '@/shared/lib/translations'
import type { SessionType } from '@/shared/ui/badge'
import { addHeadingIds, sanitizeHtml, type Heading } from '@/shared/ui/rich-text'

type Schemas = components['schemas']

export type CalloutVariant = 'INFO' | 'TIP' | 'WARNING' | 'DANGER'
export type MediaKind = 'IMAGE' | 'DIAGRAM' | 'VIDEO' | 'AUDIO' | 'FILE'
export type RefKind = 'QUIZ' | 'CODING_TASK' | 'ACTIVITY'

type BlockBase = { id: string; fallback: boolean }

export type CodeExample = BlockBase & { language: string; code: string; explanation?: string }

/** One block of a lesson, narrowed from the untyped `settings` / `payload` of the API. */
export type LessonBlock =
  | (BlockBase & { kind: 'text'; html: string; caseStudy: boolean })
  | (BlockBase & { kind: 'callout'; variant: CalloutVariant; html: string })
  | (BlockBase & { kind: 'code'; examples: CodeExample[] })
  | (BlockBase & { kind: 'media'; media: MediaKind; fileId: string; caption?: string })
  | (BlockBase & { kind: 'link'; url: string; label: string; note?: string })
  | (BlockBase & { kind: 'embed'; src: string; caption?: string })
  | (BlockBase & { kind: 'ref'; ref: RefKind; refId: string })

export type Lesson = {
  id: string
  code?: string
  title: string
  titleIsFallback: boolean
  sessionType: SessionType
  hours?: number
  readingMinutes: number
  blocks: LessonBlock[]
  /** True when any block is shown in a language other than the one asked for. */
  hasFallback: boolean
}

const CALLOUT_VARIANTS: readonly string[] = ['INFO', 'TIP', 'WARNING', 'DANGER']
function isCalloutVariant(value: string | undefined): value is CalloutVariant {
  return value !== undefined && CALLOUT_VARIANTS.includes(value)
}

/** Links leave the platform only over http(s): nothing a block says can run script. */
function safeUrl(value: string | undefined): string | undefined {
  if (value === undefined) return undefined
  try {
    const url = new URL(value)
    return url.protocol === 'https:' || url.protocol === 'http:' ? url.toString() : undefined
  } catch {
    return undefined
  }
}

function embedSrc(provider: string | undefined, externalId: string | undefined): string | undefined {
  if (externalId === undefined || !/^[\w-]+$/.test(externalId)) return undefined
  if (provider === 'YOUTUBE') return `https://www.youtube-nocookie.com/embed/${externalId}`
  if (provider === 'VIMEO') return `https://player.vimeo.com/video/${externalId}`
  return undefined
}

type Narrowed =
  Exclude<LessonBlock, { kind: 'code' }> | (BlockBase & { kind: 'code-example'; example: CodeExample })

function narrowBlock(raw: Schemas['ContentBlockView']): Narrowed | null {
  if (raw.id === undefined || raw.blockType === undefined) return null
  const base: BlockBase = { id: raw.id, fallback: raw.payloadIsFallback === true }
  const { payload, settings } = raw
  switch (raw.blockType) {
    case 'TEXT':
    case 'TABLE':
    case 'CASE_STUDY': {
      const html = readString(payload, 'html')
      return html === undefined
        ? null
        : { ...base, kind: 'text', html, caseStudy: raw.blockType === 'CASE_STUDY' }
    }
    case 'CALLOUT': {
      const html = readString(payload, 'html')
      const variant = readString(settings, 'variant')
      return html === undefined
        ? null
        : { ...base, kind: 'callout', html, variant: isCalloutVariant(variant) ? variant : 'INFO' }
    }
    case 'CODE_EXAMPLE': {
      const code = readString(payload, 'code')
      if (code === undefined) return null
      const example: CodeExample = { ...base, code, language: readString(settings, 'language') ?? 'text' }
      const explanation = readString(payload, 'explanation')
      if (explanation !== undefined) example.explanation = explanation
      return { ...base, kind: 'code-example', example }
    }
    case 'IMAGE':
    case 'DIAGRAM':
    case 'VIDEO':
    case 'AUDIO':
    case 'FILE': {
      if (raw.fileId === undefined) return null
      const block: Narrowed = { ...base, kind: 'media', media: raw.blockType, fileId: raw.fileId }
      const caption = readString(payload, 'caption')
      if (caption !== undefined) block.caption = caption
      return block
    }
    case 'LINK': {
      const url = safeUrl(readString(settings, 'url'))
      if (url === undefined) return null
      const block: Narrowed = { ...base, kind: 'link', url, label: readString(payload, 'label') ?? url }
      const note = readString(payload, 'note')
      if (note !== undefined) block.note = note
      return block
    }
    case 'EMBED': {
      const src = embedSrc(readString(settings, 'provider'), readString(settings, 'externalId'))
      if (src === undefined) return null
      const block: Narrowed = { ...base, kind: 'embed', src }
      const caption = readString(payload, 'caption')
      if (caption !== undefined) block.caption = caption
      return block
    }
    case 'QUIZ_REF':
    case 'CODING_TASK_REF':
    case 'ACTIVITY_REF': {
      const refId = readString(settings, 'refId')
      if (refId === undefined) return null
      const ref: RefKind =
        raw.blockType === 'QUIZ_REF'
          ? 'QUIZ'
          : raw.blockType === 'CODING_TASK_REF'
            ? 'CODING_TASK'
            : 'ACTIVITY'
      return { ...base, kind: 'ref', ref, refId }
    }
    case 'LESSON_PLAN':
      // Teachers only; the student endpoint never sends it.
      return null
  }
}

/** Consecutive code examples are one example in several languages: one block with tabs. */
function groupCode(blocks: readonly Narrowed[]): LessonBlock[] {
  const grouped: LessonBlock[] = []
  for (const block of blocks) {
    if (block.kind !== 'code-example') {
      grouped.push(block)
      continue
    }
    const previous = grouped[grouped.length - 1]
    if (
      previous?.kind === 'code' &&
      !previous.examples.some((item) => item.language === block.example.language)
    ) {
      previous.examples.push(block.example)
      previous.fallback = previous.fallback || block.fallback
    } else {
      grouped.push({ id: block.id, fallback: block.fallback, kind: 'code', examples: [block.example] })
    }
  }
  return grouped
}

const WORDS_PER_MINUTE = 160

function readingMinutes(blocks: readonly LessonBlock[]): number {
  let words = 0
  for (const block of blocks) {
    if (block.kind === 'text' || block.kind === 'callout') {
      words += block.html
        .replace(/<[^>]+>/g, ' ')
        .split(/\s+/)
        .filter(Boolean).length
    } else if (block.kind === 'code') {
      // code is read slower than prose: count each line as a handful of words
      words += (block.examples[0]?.code.split('\n').length ?? 0) * 6
    }
  }
  return Math.max(1, Math.round(words / WORDS_PER_MINUTE))
}

export function narrowLesson(raw: Schemas['LessonDetail'], locale: Locale): Lesson {
  if (raw.id === undefined || raw.sessionType === undefined)
    throw new Error('LessonDetail without id or type')
  const translation = pickTranslation(raw.translations, locale)
  const blocks = groupCode(
    (raw.blocks ?? [])
      .slice()
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      .map(narrowBlock)
      .filter((block) => block !== null),
  )
  const lesson: Lesson = {
    id: raw.id,
    title: translation?.value.title ?? raw.code ?? '',
    titleIsFallback: translation?.isFallback ?? false,
    sessionType: raw.sessionType,
    readingMinutes: raw.estimatedMinutes ?? readingMinutes(blocks),
    blocks,
    hasFallback: (translation?.isFallback ?? false) || blocks.some((block) => block.fallback),
  }
  if (raw.code !== undefined) lesson.code = raw.code
  if (raw.allocatedHours !== undefined) lesson.hours = raw.allocatedHours
  return lesson
}

/** The id prefix of the headings of one text block; the renderer and the outline share it. */
export function headingPrefix(blockId: string): string {
  return `s-${blockId.slice(0, 8)}`
}

/** The table of contents: every h2 / h3 of the lesson's text blocks, in order. */
export function lessonOutline(blocks: readonly LessonBlock[]): Heading[] {
  return blocks.flatMap((block) =>
    block.kind === 'text' ? addHeadingIds(sanitizeHtml(block.html), headingPrefix(block.id)).headings : [],
  )
}
