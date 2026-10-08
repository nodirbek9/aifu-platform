import { beforeEach, describe, expect, it } from 'vitest'
import {
  addPair,
  clockTone,
  emptyAnswer,
  formatClock,
  formatPoints,
  isAnswered,
  isSavable,
  moveOption,
  narrowAttempt,
  narrowAttemptAllowance,
  narrowQuestion,
  parseNumber,
  readFlags,
  remainingSeconds,
  removePair,
  toAnswerRequest,
  writeFlags,
} from './model'

describe('the clock', () => {
  const attempt = { expiresAt: '2026-10-02T12:10:00Z', clockOffsetMs: 0 }
  const at = (iso: string) => Date.parse(iso)

  it('counts whole seconds, rounded up, and never below zero', () => {
    expect(remainingSeconds(attempt, at('2026-10-02T12:00:00Z'))).toBe(600)
    expect(remainingSeconds(attempt, at('2026-10-02T12:09:59.200Z'))).toBe(1)
    expect(remainingSeconds(attempt, at('2026-10-02T12:10:00Z'))).toBe(0)
    expect(remainingSeconds(attempt, at('2026-10-02T12:30:00Z'))).toBe(0)
  })

  it("is measured on the server's clock: a computer five minutes slow still shows the true time", () => {
    const slowComputer = { ...attempt, clockOffsetMs: 5 * 60_000 }
    expect(remainingSeconds(slowComputer, at('2026-10-02T11:55:00Z'))).toBe(600)
  })

  it('an attempt without a time limit has no clock', () => {
    expect(remainingSeconds({ clockOffsetMs: 0 }, Date.now())).toBeUndefined()
  })

  it('reads the offset from the server time of the answer', () => {
    const narrowed = narrowAttempt(
      { id: 'a', quizId: 'q', status: 'IN_PROGRESS', serverTime: '2026-10-02T12:00:00Z' },
      at('2026-10-02T11:58:00Z'),
    )
    expect(narrowed.clockOffsetMs).toBe(120_000)
  })

  it('is written as m:ss, and with hours when there are any', () => {
    expect(formatClock(754)).toBe('12:34')
    expect(formatClock(9)).toBe('0:09')
    expect(formatClock(3723)).toBe('1:02:03')
  })

  it('warns in the last two minutes and turns red in the last half minute', () => {
    expect(clockTone(121)).toBe('calm')
    expect(clockTone(120)).toBe('warning')
    expect(clockTone(31)).toBe('warning')
    expect(clockTone(30)).toBe('danger')
  })
})

describe('answers', () => {
  it('a number is accepted with a comma, a dot or a typographic minus — and nothing else', () => {
    expect(parseNumber(' 2 ')).toBe(2)
    expect(parseNumber('3,5')).toBe(3.5)
    expect(parseNumber('−3.14')).toBe(-3.14)
    expect(parseNumber('2,')).toBeNull()
    expect(parseNumber('ikki')).toBeNull()
    expect(parseNumber('')).toBeNull()
  })

  it('a half-typed number is neither answered nor sent; an emptied one is sent, to clear the answer', () => {
    expect(isSavable({ kind: 'number', text: '2,' })).toBe(false)
    expect(isAnswered({ kind: 'number', text: '2,' })).toBe(false)
    expect(isSavable({ kind: 'number', text: '' })).toBe(true)
    expect(toAnswerRequest({ kind: 'number', text: '' })).toEqual({})
    expect(toAnswerRequest({ kind: 'number', text: '2,5' })).toEqual({ responseNumber: 2.5 })
  })

  it('a typed answer is sent trimmed: the checker compares it as it is', () => {
    expect(toAnswerRequest({ kind: 'text', text: '  print ' })).toEqual({ responseText: 'print' })
    expect(isAnswered({ kind: 'text', text: '   ' })).toBe(false)
  })

  it('an order travels as places in the sequence, pairs as a number shared by the two', () => {
    expect(toAnswerRequest({ kind: 'order', optionIds: ['b', 'a'] })).toEqual({
      selectedOptions: [
        { optionId: 'b', ordinal: 0 },
        { optionId: 'a', ordinal: 1 },
      ],
    })
    expect(
      toAnswerRequest({
        kind: 'pairs',
        pairs: [
          ['a', 'x'],
          ['b', 'y'],
        ],
      }),
    ).toEqual({
      selectedOptions: [
        { optionId: 'a', ordinal: 0 },
        { optionId: 'x', ordinal: 0 },
        { optionId: 'b', ordinal: 1 },
        { optionId: 'y', ordinal: 1 },
      ],
    })
  })

  it('pairing an item again takes it out of the pair it was in', () => {
    const pairs = addPair(
      [
        ['a', 'x'],
        ['b', 'y'],
      ],
      'a',
      'y',
    )
    expect(pairs).toEqual([['a', 'y']])
    expect(removePair(pairs, 'y')).toEqual([])
    expect(addPair([], 'a', 'a')).toEqual([])
  })

  it('moving past either end changes nothing', () => {
    expect(moveOption(['a', 'b', 'c'], 1, -1)).toEqual(['b', 'a', 'c'])
    expect(moveOption(['a', 'b', 'c'], 0, -1)).toEqual(['a', 'b', 'c'])
    expect(moveOption(['a', 'b', 'c'], 2, 1)).toEqual(['a', 'b', 'c'])
  })

  it('an untouched ordering question is not answered', () => {
    expect(isAnswered(emptyAnswer('ORDERING'))).toBe(false)
  })
})

describe('what the server sends, narrowed', () => {
  it('a matching answer comes back as neighbours and is read as pairs', () => {
    const question = narrowQuestion({
      questionId: 'q',
      questionType: 'MATCHING',
      options: [{ id: 'a' }, { id: 'x' }, { id: 'b' }, { id: 'y' }],
      selectedOptionIds: ['a', 'x', 'b', 'y'],
    })
    expect(question?.saved).toEqual({
      kind: 'pairs',
      pairs: [
        ['a', 'x'],
        ['b', 'y'],
      ],
    })
  })

  it('a question type of a newer server is left out, not guessed', () => {
    expect(narrowQuestion({ questionId: 'q', questionType: 'ESSAY' as never })).toBeNull()
  })

  it('the review is open only when the server says whether answers were right', () => {
    const closed = narrowAttempt({
      id: 'a',
      quizId: 'q',
      status: 'GRADED',
      questions: [{ questionId: 'q1', questionType: 'NUMERIC', responseNumber: 2 }],
    })
    expect(closed.reviewOpen).toBe(false)
    expect(closed.questions[0]?.saved).toEqual({ kind: 'number', text: '2' })

    const open = narrowAttempt({
      id: 'a',
      quizId: 'q',
      status: 'GRADED',
      questions: [{ questionId: 'q1', questionType: 'NUMERIC', isCorrect: false, correctAnswer: '2' }],
    })
    expect(open.reviewOpen).toBe(true)
    expect(open.questions[0]?.review).toEqual({ correct: false, pointsAwarded: 0, correctAnswer: '2' })
  })

  it('an attempt handed in at the very moment its time ended was closed by the clock', () => {
    const closed = narrowAttempt({
      id: 'a',
      quizId: 'q',
      status: 'GRADED',
      expiresAt: '2026-10-02T12:10:00Z',
      submittedAt: '2026-10-02T12:10:00Z',
    })
    expect(closed.closedByTime).toBe(true)
    const inTime = narrowAttempt({
      id: 'a',
      quizId: 'q',
      status: 'GRADED',
      expiresAt: '2026-10-02T12:10:00Z',
      submittedAt: '2026-10-02T12:04:00Z',
    })
    expect(inTime.closedByTime).toBe(false)
  })

  it('questions are shown in the order they were served', () => {
    const attempt = narrowAttempt({
      id: 'a',
      quizId: 'q',
      status: 'IN_PROGRESS',
      questions: [
        { questionId: 'second', questionType: 'NUMERIC', servedPosition: 1 },
        { questionId: 'first', questionType: 'NUMERIC', servedPosition: 0 },
      ],
    })
    expect(attempt.questions.map((question) => question.id)).toEqual(['first', 'second'])
  })
})

describe('points and marks', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('a score is written as people write it', () => {
    expect(formatPoints(7)).toBe('7')
    expect(formatPoints(2.5)).toBe('2.5')
    expect(formatPoints(66.67)).toBe('66.67')
  })

  it('marks are kept per attempt and removed when cleared', () => {
    writeFlags('attempt-1', ['q2', 'q5'])
    expect(readFlags('attempt-1')).toEqual(['q2', 'q5'])
    expect(readFlags('attempt-2')).toEqual([])
    writeFlags('attempt-1', [])
    expect(localStorage.length).toBe(0)
  })
})

describe('narrowAttemptAllowance', () => {
  it('narrows a fully populated allowance view', () => {
    expect(narrowAttemptAllowance({ attemptsUsed: 1, attemptsAllowed: 3 })).toEqual({
      attemptsUsed: 1,
      attemptsAllowed: 3,
    })
  })

  it('handles unlimited attempts when attemptsAllowed is absent', () => {
    expect(narrowAttemptAllowance({ attemptsUsed: 2 })).toEqual({
      attemptsUsed: 2,
    })
  })

  it('provides safe defaults when raw view is undefined or null', () => {
    expect(narrowAttemptAllowance(undefined)).toEqual({
      attemptsUsed: 0,
    })
    expect(narrowAttemptAllowance(null)).toEqual({
      attemptsUsed: 0,
    })
  })
})
