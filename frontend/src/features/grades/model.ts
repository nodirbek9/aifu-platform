import type { components } from '@/shared/api/schema'

type Schemas = components['schemas']

export type ComponentKind = NonNullable<Schemas['ComponentGradeView']['kind']>
export type GradeStatus = 'PUBLISHED' | 'REVISED' | 'VOIDED'
export type GradeSource = NonNullable<Schemas['GradeEntryView']['source']>

export type ComponentGrade = {
  id: string
  /** Present on a part of a larger component (attendance within JN). */
  parentId?: string
  code: string
  kind: ComponentKind
  maxPoints: number
  earned: number
  /** Something in it — or in one of its parts — has been graded. */
  graded: boolean
  minPassPercent?: number
  blocksFinal: boolean
  /** Whether the earned share reaches the pass mark; absent without a pass mark or before grading. */
  gatePassed?: boolean
}

export type GradeLine = {
  id: string
  score: number
  maxScore: number
  status: GradeStatus
  source: GradeSource
  /** When the grade was given. */
  at: string
  revision: number
  /** The graded work, when the grade came from one. */
  title?: string
  componentCode?: string
  voidReason?: string
}

export type Gradebook = {
  enrollmentId: string
  /** The running total of the course, out of 100. */
  finalScore?: number
  finalSymbol?: string
  /** Every top-level component is graded: the score and its grade are the result of the course. */
  complete: boolean
  academicDebt: boolean
  /** In the scheme's own order: each top-level component followed by its parts. */
  components: ComponentGrade[]
  /** The grades that count now, newest first. */
  entries: GradeLine[]
  /** Every grade there ever was, newest first. */
  history: GradeLine[]
}

function narrowLine(raw: Schemas['GradeEntryView']): GradeLine | null {
  if (raw.id === undefined || raw.score === undefined || raw.maxScore === undefined) return null
  if (raw.status !== 'PUBLISHED' && raw.status !== 'REVISED' && raw.status !== 'VOIDED') return null
  const line: GradeLine = {
    id: raw.id,
    score: raw.score,
    maxScore: raw.maxScore,
    status: raw.status,
    source: raw.source ?? 'MANUAL',
    at: raw.evaluatedAt ?? raw.publishedAt ?? '',
    revision: raw.revision ?? 1,
  }
  if (raw.title !== undefined && raw.title !== '') line.title = raw.title
  if (raw.componentCode !== undefined) line.componentCode = raw.componentCode
  if (raw.voidReason !== undefined && raw.voidReason !== '') line.voidReason = raw.voidReason
  return line
}

export function narrowGradebook(raw: Schemas['EnrollmentGradeSummary']): Gradebook {
  if (raw.enrollmentId === undefined) throw new Error('Grade summary without an enrollment')
  const book: Gradebook = {
    enrollmentId: raw.enrollmentId,
    complete: raw.complete === true,
    academicDebt: raw.academicDebt === true,
    components: (raw.components ?? []).flatMap((component) => {
      if (component.id === undefined || component.kind === undefined) return []
      const narrowed: ComponentGrade = {
        id: component.id,
        code: component.code ?? '',
        kind: component.kind,
        maxPoints: component.maxPoints ?? 0,
        earned: component.earned ?? 0,
        graded: component.graded === true,
        blocksFinal: component.blocksFinal === true,
      }
      if (component.parentComponentId !== undefined) narrowed.parentId = component.parentComponentId
      if (component.minPassPercent !== undefined) narrowed.minPassPercent = component.minPassPercent
      if (component.gatePassed !== undefined) narrowed.gatePassed = component.gatePassed
      return [narrowed]
    }),
    entries: (raw.entries ?? []).map(narrowLine).filter((line) => line !== null),
    history: (raw.history ?? []).map(narrowLine).filter((line) => line !== null),
  }
  if (raw.finalScore !== undefined) book.finalScore = raw.finalScore
  if (raw.finalGradeSymbol !== undefined) book.finalSymbol = raw.finalGradeSymbol
  return book
}

export type Admission = 'granted' | 'denied' | 'pending' | 'none'

/**
 * Where the student stands with the final exam, read off the gates the server evaluated:
 * refused as soon as one gate is failed, granted once every gate is passed, pending while a
 * gate component is not graded yet. `none` when the scheme has no gate.
 */
export function admission(components: readonly ComponentGrade[]): Admission {
  const gates = components.filter(
    (component) => component.blocksFinal && component.minPassPercent !== undefined,
  )
  if (gates.length === 0) return 'none'
  if (gates.some((gate) => gate.gatePassed === false)) return 'denied'
  return gates.every((gate) => gate.gatePassed === true) ? 'granted' : 'pending'
}

/** The sum of what the top-level components are worth: 100 in the university's scheme. */
export function totalMax(components: readonly ComponentGrade[]): number {
  return components
    .filter((component) => component.parentId === undefined)
    .reduce((sum, component) => sum + component.maxPoints, 0)
}

/** The corrections of the history: everything in it that no longer counts. */
export function hasHistory(book: Pick<Gradebook, 'history'>): boolean {
  return book.history.some((line) => line.status !== 'PUBLISHED')
}

/** 7 → "7", 2.5 → "2.5": a score as people write it. */
export function formatScore(value: number): string {
  return String(Math.round(value * 100) / 100)
}
