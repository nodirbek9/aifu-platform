import { beforeEach, describe, expect, it } from 'vitest'
import {
  clearDraft,
  isFinished,
  narrowRun,
  narrowSubmission,
  numberedTest,
  pollInterval,
  readDraft,
  writeDraft,
} from './model'

describe('polling a queued job', () => {
  it('asks every second at first and every two after ten seconds', () => {
    expect(pollInterval('QUEUED', 0, 3_000)).toBe(1000)
    expect(pollInterval('RUNNING', 0, 9_999)).toBe(1000)
    expect(pollInterval('RUNNING', 0, 10_000)).toBe(2000)
  })

  it('stops once the job is finished, however it ended', () => {
    for (const status of ['COMPLETED', 'FAILED', 'CANCELLED'] as const) {
      expect(isFinished(status)).toBe(true)
      expect(pollInterval(status, 0, 1_000)).toBe(false)
    }
  })

  it('keeps asking while nothing is known yet', () => {
    expect(pollInterval(undefined, 0, 500)).toBe(1000)
  })
})

describe('drafts', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('are kept per task and per language', () => {
    writeDraft('task-1', 'python', 'print(1)')
    writeDraft('task-1', 'java', 'class Main {}')
    writeDraft('task-2', 'python', 'print(2)')
    expect(readDraft('task-1', 'python')).toBe('print(1)')
    expect(readDraft('task-1', 'java')).toBe('class Main {}')
    expect(readDraft('task-2', 'python')).toBe('print(2)')
    expect(readDraft('task-2', 'cpp')).toBeNull()
  })

  it('a reset removes only that language of that task', () => {
    writeDraft('task-1', 'python', 'print(1)')
    writeDraft('task-1', 'java', 'class Main {}')
    clearDraft('task-1', 'python')
    expect(readDraft('task-1', 'python')).toBeNull()
    expect(readDraft('task-1', 'java')).toBe('class Main {}')
  })
})

describe('what the server sends, narrowed', () => {
  it('a failed run is a platform failure, not a verdict about the code', () => {
    const run = narrowRun({ id: 'r1', mode: 'CUSTOM', status: 'FAILED', verdict: 'INTERNAL_ERROR' })
    expect(run.platformError).toBe(true)
    expect(run.stdout).toBe('')
  })

  it('a hidden test carries no input or output; a visible one does', () => {
    const submission = narrowSubmission({
      id: 's1',
      submittedAt: '2026-10-02T12:00:00Z',
      status: 'COMPLETED',
      verdict: 'PARTIAL',
      testResults: [
        {
          testCaseId: 'a',
          name: 'Namuna 1',
          passed: true,
          stdin: '3',
          expectedStdout: '9',
          actualStdout: '9\n',
        },
        { testCaseId: 'b', name: 'Test 2', passed: false, verdict: 'WRONG_ANSWER' },
      ],
    })
    expect(submission.tests[0]?.detail).toEqual({ stdin: '3', expected: '9', actual: '9\n', stderr: '' })
    expect(submission.tests[1]?.detail).toBeUndefined()
    expect(submission.tests[1]?.verdict).toBe('WRONG_ANSWER')
  })

  it('an unknown verdict from a newer server is left out rather than guessed', () => {
    const submission = narrowSubmission({
      id: 's2',
      submittedAt: '2026-10-02T12:00:00Z',
      testResults: [{ testCaseId: 'a', passed: false, verdict: 'SOMETHING_NEW' }],
    })
    expect(submission.tests[0]?.verdict).toBeUndefined()
  })
})

describe('test names', () => {
  it('a numbered label is read as its number, so it can be said in any language', () => {
    expect(numberedTest('Namuna 2')).toBe(2)
    expect(numberedTest('Test 13')).toBe(13)
    expect(numberedTest(' Пример 1 ')).toBe(1)
  })

  it('a name a teacher wrote is kept', () => {
    expect(numberedTest('Manfiy sonlar')).toBeNull()
    expect(numberedTest('Test of big values')).toBeNull()
    expect(numberedTest(undefined)).toBeNull()
  })
})
