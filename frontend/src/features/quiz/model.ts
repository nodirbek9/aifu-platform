import type { components } from '@/shared/api/schema'

type Schemas = components['schemas']

export const QUESTION_TYPES = [
  'SINGLE_CHOICE',
  'MULTIPLE_CHOICE',
  'TRUE_FALSE',
  'SHORT_ANSWER',
  'NUMERIC',
  'ORDERING',
  'MATCHING',
] as const
export type QuestionType = (typeof QUESTION_TYPES)[number]

export type RevealPolicy = NonNullable<Schemas['QuizDetail']['revealPolicy']>

export type Quiz = {
  id: string
  courseId: string
  title: string
  titleIsFallback: boolean
  description?: string
  instructions?: string
  maxPoints: number
  passingPercent: number
  timeLimitSeconds?: number
  maxAttempts?: number
  revealPolicy: RevealPolicy
}

export function narrowQuiz(raw: Schemas['QuizDetail']): Quiz {
  if (raw.id === undefined || raw.courseId === undefined) throw new Error('Quiz without ids')
  const quiz: Quiz = {
    id: raw.id,
    courseId: raw.courseId,
    title: raw.title ?? raw.code ?? '',
    titleIsFallback: raw.titleIsFallback === true,
    maxPoints: raw.maxPoints ?? 0,
    passingPercent: raw.passingPercent ?? 0,
    revealPolicy: raw.revealPolicy ?? 'NEVER',
  }
  if (raw.description !== undefined && raw.description !== '') quiz.description = raw.description
  if (raw.instructions !== undefined && raw.instructions !== '') quiz.instructions = raw.instructions
  if (raw.timeLimitSeconds !== undefined) quiz.timeLimitSeconds = raw.timeLimitSeconds
  if (raw.maxAttempts !== undefined) quiz.maxAttempts = raw.maxAttempts
  return quiz
}

export type AttemptStatus = NonNullable<Schemas['AttemptDetail']['status']>

export type AttemptSummary = {
  id: string
  number: number
  status: AttemptStatus
  startedAt: string
  submittedAt?: string
  score?: number
  maxScore?: number
  percent?: number
  passed?: boolean
}

export function narrowAttemptSummary(raw: Schemas['AttemptSummary']): AttemptSummary | null {
  if (raw.id === undefined || raw.status === undefined || raw.startedAt === undefined) return null
  const summary: AttemptSummary = {
    id: raw.id,
    number: raw.attemptNumber ?? 1,
    status: raw.status,
    startedAt: raw.startedAt,
  }
  if (raw.submittedAt !== undefined) summary.submittedAt = raw.submittedAt
  if (raw.score !== undefined) summary.score = raw.score
  if (raw.maxScore !== undefined) summary.maxScore = raw.maxScore
  if (raw.percent !== undefined) summary.percent = raw.percent
  if (raw.passed !== undefined) summary.passed = raw.passed
  return summary
}

/* ------------------------------------------------------------------ answers */

export type Pair = readonly [string, string]

/** What the student has put down for one question; one shape per way of answering. */
export type Answer =
  | { kind: 'choice'; optionIds: readonly string[] }
  | { kind: 'text'; text: string }
  | { kind: 'number'; text: string }
  /** Empty until the student has arranged (or confirmed) the order. */
  | { kind: 'order'; optionIds: readonly string[] }
  | { kind: 'pairs'; pairs: readonly Pair[] }

export function emptyAnswer(type: QuestionType): Answer {
  switch (type) {
    case 'SINGLE_CHOICE':
    case 'MULTIPLE_CHOICE':
    case 'TRUE_FALSE':
      return { kind: 'choice', optionIds: [] }
    case 'SHORT_ANSWER':
      return { kind: 'text', text: '' }
    case 'NUMERIC':
      return { kind: 'number', text: '' }
    case 'ORDERING':
      return { kind: 'order', optionIds: [] }
    case 'MATCHING':
      return { kind: 'pairs', pairs: [] }
  }
}

/** "2", "2,5", "−3.14" → a number; anything else is not a number yet. */
export function parseNumber(text: string): number | null {
  const normalized = text.trim().replace(',', '.').replace('−', '-')
  if (!/^-?\d+(\.\d+)?$/.test(normalized)) return null
  return Number(normalized)
}

export function isAnswered(answer: Answer): boolean {
  switch (answer.kind) {
    case 'choice':
    case 'order':
      return answer.optionIds.length > 0
    case 'text':
      return answer.text.trim() !== ''
    case 'number':
      return parseNumber(answer.text) !== null
    case 'pairs':
      return answer.pairs.length > 0
  }
}

/** A number the student is still typing ("2,", "abc") cannot be saved; everything else can. */
export function isSavable(answer: Answer): boolean {
  return answer.kind !== 'number' || answer.text.trim() === '' || parseNumber(answer.text) !== null
}

/**
 * The request that records an answer. The order of an ordering answer and the pairs of a
 * matching answer travel as ordinals: a place in the sequence, or a number shared by a pair.
 */
export function toAnswerRequest(answer: Answer): Schemas['RecordAnswerRequest'] {
  switch (answer.kind) {
    case 'choice':
      return { selectedOptions: answer.optionIds.map((optionId) => ({ optionId })) }
    case 'order':
      return { selectedOptions: answer.optionIds.map((optionId, ordinal) => ({ optionId, ordinal })) }
    case 'pairs':
      return {
        selectedOptions: answer.pairs.flatMap((pair, ordinal) =>
          pair.map((optionId) => ({ optionId, ordinal })),
        ),
      }
    case 'text': {
      const text = answer.text.trim()
      return text === '' ? {} : { responseText: text }
    }
    case 'number': {
      const value = parseNumber(answer.text)
      return value === null ? {} : { responseNumber: value }
    }
  }
}

/** Pairs a with b; either of them leaves the pair it was in. */
export function addPair(pairs: readonly Pair[], a: string, b: string): Pair[] {
  if (a === b) return [...pairs]
  return [...pairs.filter((pair) => !pair.includes(a) && !pair.includes(b)), [a, b]]
}

export function removePair(pairs: readonly Pair[], optionId: string): Pair[] {
  return pairs.filter((pair) => !pair.includes(optionId))
}

/** Moves the option at `index` one place up (-1) or down (+1); out of range changes nothing. */
export function moveOption(optionIds: readonly string[], index: number, direction: -1 | 1): string[] {
  const next = [...optionIds]
  const target = index + direction
  const moved = next[index]
  const displaced = next[target]
  if (moved === undefined || displaced === undefined) return next
  next[index] = displaced
  next[target] = moved
  return next
}

/* ---------------------------------------------------------------- questions */

export type Option = {
  id: string
  text: string
  /** The three below exist only in an open review. */
  correct?: boolean
  correctPosition?: number
  pairKey?: string
}

export type QuestionReview = {
  correct: boolean
  pointsAwarded: number
  explanation?: string
  /** An accepted answer of a typed question. */
  correctAnswer?: string
}

export type Question = {
  id: string
  position: number
  type: QuestionType
  prompt: string
  hint?: string
  points: number
  /** In the order this student is shown them. */
  options: Option[]
  saved: Answer
  review?: QuestionReview
}

function isQuestionType(value: string | undefined): value is QuestionType {
  return QUESTION_TYPES.some((type) => type === value)
}

function savedAnswer(raw: Schemas['ServedQuestionView'], type: QuestionType): Answer {
  const selected = raw.selectedOptionIds ?? []
  switch (type) {
    case 'SINGLE_CHOICE':
    case 'MULTIPLE_CHOICE':
    case 'TRUE_FALSE':
      return { kind: 'choice', optionIds: selected }
    case 'ORDERING':
      // the server lists them by ordinal: that is the order the student left them in
      return { kind: 'order', optionIds: selected }
    case 'MATCHING': {
      // by ordinal as well, and a pair shares one: neighbours are pairs
      const pairs: Pair[] = []
      for (let index = 0; index + 1 < selected.length; index += 2) {
        const first = selected[index]
        const second = selected[index + 1]
        if (first !== undefined && second !== undefined) pairs.push([first, second])
      }
      return { kind: 'pairs', pairs }
    }
    case 'SHORT_ANSWER':
      return { kind: 'text', text: raw.responseText ?? '' }
    case 'NUMERIC':
      return { kind: 'number', text: raw.responseNumber === undefined ? '' : String(raw.responseNumber) }
  }
}

export function narrowQuestion(raw: Schemas['ServedQuestionView']): Question | null {
  // a question type added by a newer server has no input here: it is left out, not guessed
  if (raw.questionId === undefined || !isQuestionType(raw.questionType)) return null
  const question: Question = {
    id: raw.questionId,
    position: raw.servedPosition ?? 0,
    type: raw.questionType,
    prompt: raw.prompt ?? '',
    points: raw.pointsPossible ?? 0,
    options: (raw.options ?? []).flatMap((option) => {
      if (option.id === undefined) return []
      const narrowed: Option = { id: option.id, text: option.text ?? '' }
      if (option.correct !== undefined) narrowed.correct = option.correct
      if (option.correctPosition !== undefined) narrowed.correctPosition = option.correctPosition
      if (option.pairKey !== undefined) narrowed.pairKey = option.pairKey
      return [narrowed]
    }),
    saved: savedAnswer(raw, raw.questionType),
  }
  if (raw.hint !== undefined && raw.hint !== '') question.hint = raw.hint
  if (raw.isCorrect !== undefined) {
    const review: QuestionReview = { correct: raw.isCorrect, pointsAwarded: raw.pointsAwarded ?? 0 }
    if (raw.explanation !== undefined && raw.explanation !== '') review.explanation = raw.explanation
    if (raw.correctAnswer !== undefined) review.correctAnswer = raw.correctAnswer
    question.review = review
  }
  return question
}

/* ----------------------------------------------------------------- attempts */

export type Attempt = {
  id: string
  quizId: string
  number: number
  status: AttemptStatus
  startedAt: string
  expiresAt?: string
  submittedAt?: string
  score?: number
  maxScore?: number
  percent?: number
  passed?: boolean
  /** The clock handed the attempt in, not the student. */
  closedByTime: boolean
  /** The teacher's review policy lets this attempt's answers be gone through. */
  reviewOpen: boolean
  /** Server clock minus this computer's clock, in ms, measured when the answer arrived. */
  clockOffsetMs: number
  questions: Question[]
}

export function narrowAttempt(raw: Schemas['AttemptDetail'], receivedAt: number = Date.now()): Attempt {
  if (raw.id === undefined || raw.quizId === undefined || raw.status === undefined) {
    throw new Error('Attempt without id or status')
  }
  const questions = (raw.questions ?? [])
    .map(narrowQuestion)
    .filter((question) => question !== null)
    .sort((a, b) => a.position - b.position)
  const attempt: Attempt = {
    id: raw.id,
    quizId: raw.quizId,
    number: raw.attemptNumber ?? 1,
    status: raw.status,
    startedAt: raw.startedAt ?? '',
    closedByTime:
      raw.status !== 'IN_PROGRESS' && raw.expiresAt !== undefined && raw.expiresAt === raw.submittedAt,
    reviewOpen: questions.some((question) => question.review !== undefined),
    clockOffsetMs: raw.serverTime === undefined ? 0 : Date.parse(raw.serverTime) - receivedAt,
    questions,
  }
  if (raw.expiresAt !== undefined) attempt.expiresAt = raw.expiresAt
  if (raw.submittedAt !== undefined) attempt.submittedAt = raw.submittedAt
  if (raw.score !== undefined) attempt.score = raw.score
  if (raw.maxScore !== undefined) attempt.maxScore = raw.maxScore
  if (raw.percent !== undefined) attempt.percent = raw.percent
  if (raw.passed !== undefined) attempt.passed = raw.passed
  return attempt
}

/** 7 → "7", 2.5 → "2.5", 66.67 → "66.67": a score as people write it. */
export function formatPoints(value: number): string {
  return String(Math.round(value * 100) / 100)
}

/* -------------------------------------------------------------------- clock */

/**
 * Whole seconds left on the server's clock; `undefined` for an attempt without a time limit.
 * Rounded up, so the display shows 0:01 through the last second and 0:00 only when it is over.
 */
export function remainingSeconds(
  attempt: Pick<Attempt, 'expiresAt' | 'clockOffsetMs'>,
  now: number,
): number | undefined {
  if (attempt.expiresAt === undefined) return undefined
  const left = Date.parse(attempt.expiresAt) - (now + attempt.clockOffsetMs)
  return Math.max(0, Math.ceil(left / 1000))
}

/** 754 → "12:34"; an hour and more → "1:02:03". */
export function formatClock(seconds: number): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const rest = seconds % 60
  return hours > 0 ? `${hours}:${pad(minutes)}:${pad(rest)}` : `${minutes}:${pad(rest)}`
}

export type ClockTone = 'calm' | 'warning' | 'danger'

/** The clock turns to the warning colour in the last two minutes, and to red in the last thirty seconds. */
export function clockTone(seconds: number): ClockTone {
  if (seconds <= 30) return 'danger'
  if (seconds <= 120) return 'warning'
  return 'calm'
}

/** The moments worth saying aloud to a screen reader: five minutes, one minute, time is up. */
export const CLOCK_ANNOUNCEMENTS = [300, 60, 0] as const

/* -------------------------------------------- flags: "come back to this one" */

function flagsKey(attemptId: string): string {
  return `aifu-quiz-flags:${attemptId}`
}

/** Questions the student marked to come back to. Kept in this browser; the server has no use for them. */
export function readFlags(attemptId: string): string[] {
  try {
    const stored: unknown = JSON.parse(localStorage.getItem(flagsKey(attemptId)) ?? '[]')
    return Array.isArray(stored) ? stored.filter((item) => typeof item === 'string') : []
  } catch {
    return []
  }
}

export function writeFlags(attemptId: string, flags: readonly string[]): void {
  try {
    if (flags.length === 0) localStorage.removeItem(flagsKey(attemptId))
    else localStorage.setItem(flagsKey(attemptId), JSON.stringify(flags))
  } catch {
    // a full or blocked storage loses the marks, never the answers
  }
}

/* ---------------------------------------------------------------- allowance */

export type AttemptAllowance = {
  attemptsUsed: number
  attemptsAllowed?: number
}

export function narrowAttemptAllowance(
  raw: Schemas['AttemptAllowanceView'] | undefined | null,
): AttemptAllowance {
  const allowance: AttemptAllowance = {
    attemptsUsed: raw?.attemptsUsed ?? 0,
  }
  if (raw?.attemptsAllowed !== undefined) {
    allowance.attemptsAllowed = raw.attemptsAllowed
  }
  return allowance
}
