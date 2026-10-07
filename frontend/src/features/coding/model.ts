import type { components } from '@/shared/api/schema'
import { isEditorLanguage, type EditorLanguage } from '@/shared/editor/languages'
import { VERDICTS, type Verdict } from '@/shared/ui/badge'

type Schemas = components['schemas']

export type TaskLanguage = {
  code: EditorLanguage
  name: string
  version?: string
  starterCode: string
}

export type ExampleTest = { id: string; name: string; stdin: string; expected: string }

export type Task = {
  id: string
  courseId: string
  code?: string
  title: string
  titleIsFallback: boolean
  /** Sanitized HTML. */
  description: string
  inputOutput?: string
  constraints?: string
  maxPoints?: number
  maxAttempts?: number
  timeLimitMs?: number
  memoryLimitKb?: number
  languages: TaskLanguage[]
  examples: ExampleTest[]
}

export function narrowTask(raw: Schemas['CodingTaskStudentDetail']): Task {
  if (raw.id === undefined || raw.courseId === undefined) throw new Error('Coding task without ids')
  const task: Task = {
    id: raw.id,
    courseId: raw.courseId,
    title: raw.title ?? raw.code ?? '',
    titleIsFallback: raw.titleIsFallback === true,
    description: raw.description ?? '',
    // only the three teaching languages have an editor mode; the server offers no others
    languages: (raw.languages ?? []).flatMap((language) => {
      if (language.code === undefined || !isEditorLanguage(language.code)) return []
      const narrowed: TaskLanguage = {
        code: language.code,
        name: language.displayName ?? language.code,
        starterCode: language.starterCode ?? '',
      }
      if (language.versionLabel !== undefined) narrowed.version = language.versionLabel
      return [narrowed]
    }),
    examples: (raw.exampleTestCases ?? [])
      .slice()
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      .flatMap((test) =>
        test.id === undefined
          ? []
          : [
              {
                id: test.id,
                name: test.name ?? '',
                stdin: test.stdin ?? '',
                expected: test.expectedStdout ?? '',
              },
            ],
      ),
  }
  if (raw.code !== undefined) task.code = raw.code
  if (raw.instructions !== undefined) task.inputOutput = raw.instructions
  if (raw.constraintsText !== undefined) task.constraints = raw.constraintsText
  if (raw.maxPoints !== undefined) task.maxPoints = raw.maxPoints
  if (raw.maxAttempts !== undefined) task.maxAttempts = raw.maxAttempts
  if (raw.cpuTimeLimitMs !== undefined) task.timeLimitMs = raw.cpuTimeLimitMs
  if (raw.memoryLimitKb !== undefined) task.memoryLimitKb = raw.memoryLimitKb
  return task
}

export type Hint = { id: string; revealed: boolean; body?: string }

export function narrowHints(raw: readonly Schemas['StudentHintView'][]): Hint[] {
  return raw
    .slice()
    .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    .flatMap((hint) => {
      if (hint.id === undefined) return []
      const narrowed: Hint = { id: hint.id, revealed: hint.revealed === true }
      if (hint.body !== undefined) narrowed.body = hint.body
      return [narrowed]
    })
}

function isVerdict(value: string | undefined): value is Verdict {
  return value !== undefined && (VERDICTS as readonly string[]).includes(value)
}

const NUMBERED_TEST = /^(?:namuna|misol|example|пример|test|тест)\s+(\d+)$/i

/**
 * The number of a test whose name is only a numbered label ("Namuna 2", "Test 3", as the content
 * importer names them), so the screen can say it in the reader's language; `null` for a name a
 * teacher wrote.
 */
export function numberedTest(name: string | undefined): number | null {
  const match = NUMBERED_TEST.exec(name?.trim() ?? '')
  return match?.[1] === undefined ? null : Number(match[1])
}

/** One test of a run or a submission. A hidden test carries no input and no output. */
export type TestOutcome = {
  key: string
  name: string
  passed: boolean
  verdict?: Verdict
  timeMs?: number
  /** Present only for visible (example) tests. */
  detail?: { stdin: string; expected: string; actual: string; stderr: string }
}

function narrowTest(raw: Schemas['TestResultView'] | Schemas['RunTestView'], index: number): TestOutcome {
  const outcome: TestOutcome = {
    key: raw.testCaseId ?? String(index),
    name: raw.name ?? String(index + 1),
    passed: raw.passed === true,
  }
  if (isVerdict(raw.verdict)) outcome.verdict = raw.verdict
  if (raw.timeMs !== undefined) outcome.timeMs = raw.timeMs
  if (raw.expectedStdout !== undefined || raw.stdin !== undefined) {
    outcome.detail = {
      stdin: raw.stdin ?? '',
      expected: raw.expectedStdout ?? '',
      actual: raw.actualStdout ?? '',
      stderr: raw.actualStderr ?? '',
    }
  }
  return outcome
}

export type JobStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

export function isFinished(status: JobStatus): boolean {
  return status === 'COMPLETED' || status === 'FAILED' || status === 'CANCELLED'
}

/** An ungraded run: with custom input (stdout, stderr) or over the examples (tests). */
export type Run = {
  id: string
  mode: 'CUSTOM' | 'EXAMPLES'
  status: JobStatus
  verdict?: Verdict
  stdout: string
  stderr: string
  compileOutput: string
  timeMs?: number
  memoryKb?: number
  testsPassed?: number
  testsTotal?: number
  tests: TestOutcome[]
  /** The platform itself failed to run the code. */
  platformError: boolean
}

export function narrowRun(raw: Schemas['RunView']): Run {
  if (raw.id === undefined) throw new Error('RunView without an id')
  const status = raw.status ?? 'QUEUED'
  const run: Run = {
    id: raw.id,
    mode: raw.mode === 'EXAMPLES' ? 'EXAMPLES' : 'CUSTOM',
    status,
    stdout: raw.stdout ?? '',
    stderr: raw.stderr ?? '',
    compileOutput: raw.compileOutput ?? '',
    tests: (raw.tests ?? [])
      .slice()
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      .map(narrowTest),
    platformError: status === 'FAILED',
  }
  if (isVerdict(raw.verdict)) run.verdict = raw.verdict
  if (raw.timeMs !== undefined) run.timeMs = raw.timeMs
  if (raw.memoryKb !== undefined) run.memoryKb = raw.memoryKb
  if (raw.testsPassed !== undefined) run.testsPassed = raw.testsPassed
  if (raw.testsTotal !== undefined) run.testsTotal = raw.testsTotal
  return run
}

export type SubmissionSummary = {
  id: string
  attemptNumber: number
  language: string
  status: JobStatus
  verdict?: Verdict
  score?: number
  maxScore?: number
  submittedAt: string
}

export function narrowSubmissionSummary(
  raw: Schemas['SubmissionSummary'] | Schemas['SubmissionDetail'],
): SubmissionSummary | null {
  if (raw.id === undefined || raw.submittedAt === undefined) return null
  const summary: SubmissionSummary = {
    id: raw.id,
    attemptNumber: raw.attemptNumber ?? 0,
    language: raw.languageCode ?? '',
    status: raw.status ?? 'QUEUED',
    submittedAt: raw.submittedAt,
  }
  if (isVerdict(raw.verdict)) summary.verdict = raw.verdict
  if (raw.score !== undefined) summary.score = raw.score
  if (raw.maxScore !== undefined) summary.maxScore = raw.maxScore
  return summary
}

export type Submission = SubmissionSummary & {
  sourceCode: string
  compileOutput: string
  testsPassed?: number
  testsTotal?: number
  tests: TestOutcome[]
}

export function narrowSubmission(raw: Schemas['SubmissionDetail']): Submission {
  const summary = narrowSubmissionSummary(raw)
  if (summary === null) throw new Error('SubmissionDetail without id or time')
  const submission: Submission = {
    ...summary,
    sourceCode: raw.sourceCode ?? '',
    compileOutput: raw.compileOutput ?? '',
    tests: (raw.testResults ?? []).map(narrowTest),
  }
  if (raw.testsPassed !== undefined) submission.testsPassed = raw.testsPassed
  if (raw.testsTotal !== undefined) submission.testsTotal = raw.testsTotal
  return submission
}

export type Feedback = { id: string; author: string; html: string; createdAt: string }

export function narrowFeedback(raw: Schemas['MyFeedbackView']): Feedback | null {
  if (raw.id === undefined || raw.bodySanitizedHtml === undefined) return null
  return {
    id: raw.id,
    author: raw.authorName ?? '',
    html: raw.bodySanitizedHtml,
    createdAt: raw.createdAt ?? '',
  }
}

/* Drafts: the student's own code, per task and language, kept in this browser (API guide §6). */

function draftKey(taskId: string, language: string): string {
  return `aifu-draft:${taskId}:${language}`
}

export function readDraft(taskId: string, language: string): string | null {
  try {
    return localStorage.getItem(draftKey(taskId, language))
  } catch {
    return null
  }
}

export function writeDraft(taskId: string, language: string, code: string): void {
  try {
    localStorage.setItem(draftKey(taskId, language), code)
  } catch {
    /* storage full or unavailable — the code stays in the editor until the page is left */
  }
}

export function clearDraft(taskId: string, language: string): void {
  try {
    localStorage.removeItem(draftKey(taskId, language))
  } catch {
    /* nothing to clear */
  }
}

/** The polling rhythm of a queued job: every second at first, every two after ten seconds. */
export function pollInterval(status: JobStatus | undefined, startedAt: number, now: number): number | false {
  if (status !== undefined && isFinished(status)) return false
  return now - startedAt < 10_000 ? 1000 : 2000
}
