import { useRef } from 'react'
import gsap from 'gsap'
import { useGSAP } from '@gsap/react'
import { CheckCircle2, EyeOff, History, Loader2, MessageSquareText, RotateCcw, XCircle } from 'lucide-react'
import type { TFunction } from 'i18next'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { languageName } from '@/shared/editor/languages'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/dates'
import { PendingBadge, VerdictBadge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { RichText } from '@/shared/ui/rich-text'
import { SkeletonText } from '@/shared/ui/skeleton'
import {
  isFinished,
  numberedTest,
  type Run,
  type Submission,
  type SubmissionSummary,
  type TestOutcome,
} from './model'
import { useFeedbackQuery } from './queries'

gsap.registerPlugin(useGSAP)

/** A queued or running job: the live status line. */
export function JobProgress({ status }: { status: 'QUEUED' | 'RUNNING' }) {
  const { t } = useTranslation()
  return (
    <div role="status" className="text-small text-text-muted flex items-center gap-3">
      <Loader2 aria-hidden className="text-info size-5 animate-spin" />
      <PendingBadge label={t(`task.status.${status}`)} />
      <span>{t(`task.statusHint.${status}`)}</span>
    </div>
  )
}

/** The platform could not run the code: say so plainly — it is not the student's mistake. */
function PlatformFailure({ onRetry }: { onRetry: () => void }) {
  const { t } = useTranslation()
  return (
    <div
      role="alert"
      className="border-border bg-danger-soft grid justify-items-start gap-3 rounded-md border p-4"
    >
      <p className="text-body text-text font-medium">{t('task.platformFailedTitle')}</p>
      <p className="text-small text-text-muted">{t('task.platformFailedBody')}</p>
      <Button variant="outline" size="sm" onClick={onRetry}>
        <RotateCcw aria-hidden className="size-4" />
        {t('task.runAgain')}
      </Button>
    </div>
  )
}

function Stream({ label, text, tone }: { label: string; text: string; tone?: 'danger' }) {
  return (
    <div className="grid gap-1">
      <p className="text-caption text-text-muted font-medium uppercase">{label}</p>
      <pre
        className={cn(
          'border-border bg-surface-sunken text-small max-h-64 overflow-auto rounded-md border p-3 font-mono whitespace-pre-wrap',
          tone === 'danger' ? 'text-danger' : 'text-text',
        )}
      >
        {text === '' ? ' ' : text}
      </pre>
    </div>
  )
}

function Metrics({ timeMs, memoryKb }: { timeMs: number | undefined; memoryKb: number | undefined }) {
  const { t } = useTranslation()
  if (timeMs === undefined && memoryKb === undefined) return null
  return (
    <p className="text-small text-text-muted tabular-nums">
      {timeMs !== undefined && t('task.timeUsed', { ms: timeMs })}
      {timeMs !== undefined && memoryKb !== undefined && ' · '}
      {memoryKb !== undefined && t('task.memoryUsed', { mb: (memoryKb / 1024).toFixed(1) })}
    </p>
  )
}

type RunResultProps = { run: Run; onRetry: () => void }

/** The outcome of "Run": the program's own output, errors, time and memory. Never graded. */
export function CustomRunResult({ run, onRetry }: RunResultProps) {
  const { t } = useTranslation()
  if (!isFinished(run.status)) return <JobProgress status={run.status === 'RUNNING' ? 'RUNNING' : 'QUEUED'} />
  if (run.platformError) return <PlatformFailure onRetry={onRetry} />
  return (
    <div className="grid gap-4">
      <div className="flex flex-wrap items-center gap-3">
        {run.verdict !== undefined && (
          <VerdictBadge verdict={run.verdict} label={t(`verdict.${run.verdict}`)} />
        )}
        <Metrics timeMs={run.timeMs} memoryKb={run.memoryKb} />
      </div>
      {run.compileOutput !== '' && (
        <Stream label={t('task.compileOutput')} text={run.compileOutput} tone="danger" />
      )}
      {run.verdict !== 'COMPILATION_ERROR' && <Stream label={t('task.stdout')} text={run.stdout} />}
      {run.stderr !== '' && <Stream label={t('task.stderr')} text={run.stderr} tone="danger" />}
    </div>
  )
}

/** The outcome of "Run examples": each visible test, and for a failed one what differed. */
export function ExamplesRunResult({ run, onRetry }: RunResultProps) {
  const { t } = useTranslation()
  if (!isFinished(run.status)) return <JobProgress status={run.status === 'RUNNING' ? 'RUNNING' : 'QUEUED'} />
  if (run.platformError) return <PlatformFailure onRetry={onRetry} />
  return (
    <div className="grid gap-4">
      <div className="flex flex-wrap items-center gap-3">
        {run.verdict !== undefined && (
          <VerdictBadge verdict={run.verdict} label={t(`verdict.${run.verdict}`)} />
        )}
        {run.testsTotal !== undefined && (
          <span className="text-small text-text-muted tabular-nums">
            {t('task.testsPassed', { passed: run.testsPassed ?? 0, total: run.testsTotal })}
          </span>
        )}
      </div>
      {run.compileOutput !== '' && (
        <Stream label={t('task.compileOutput')} text={run.compileOutput} tone="danger" />
      )}
      <TestList tests={run.tests} />
    </div>
  )
}

/** A numbered test in the reader's language: an example is the one whose data is shown. */
function testLabel(test: TestOutcome, t: TFunction): string {
  const number = numberedTest(test.name)
  if (number === null) return test.name
  return t(test.detail === undefined ? 'authoring.testNumber' : 'task.exampleNumber', { number })
}

function TestList({ tests, animate = false }: { tests: readonly TestOutcome[]; animate?: boolean }) {
  const { t } = useTranslation()
  if (tests.length === 0) return null
  return (
    <ul className="grid gap-2">
      {tests.map((test) => (
        <li
          key={test.key}
          data-reveal={animate ? 'test' : undefined}
          className="border-border bg-surface-raised grid gap-3 rounded-md border px-3.5 py-2.5"
        >
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
            {test.passed ? (
              <CheckCircle2 aria-hidden className="text-success size-5 shrink-0" />
            ) : (
              <XCircle aria-hidden className="text-danger size-5 shrink-0" />
            )}
            <span className="text-small text-text font-medium">{testLabel(test, t)}</span>
            <span className="sr-only">{test.passed ? t('task.testPassed') : t('task.testFailed')}</span>
            {!test.passed && test.verdict !== undefined && (
              <VerdictBadge verdict={test.verdict} label={t(`verdict.${test.verdict}`)} />
            )}
            {test.detail === undefined && (
              <span className="text-caption text-text-muted inline-flex items-center gap-1 tracking-normal">
                <EyeOff aria-hidden className="size-3.5" />
                {t('task.hiddenTest')}
              </span>
            )}
            {test.timeMs !== undefined && (
              <span className="text-caption text-text-muted ml-auto tracking-normal tabular-nums">
                {t('task.timeUsed', { ms: test.timeMs })}
              </span>
            )}
          </div>
          {!test.passed && test.detail !== undefined && (
            <div className="grid gap-3 sm:grid-cols-3">
              <Stream label={t('task.input')} text={test.detail.stdin} />
              <Stream label={t('task.expectedOutput')} text={test.detail.expected} />
              <Stream label={t('task.actualOutput')} text={test.detail.actual} tone="danger" />
              {test.detail.stderr !== '' && (
                <div className="sm:col-span-3">
                  <Stream label={t('task.stderr')} text={test.detail.stderr} tone="danger" />
                </div>
              )}
            </div>
          )}
        </li>
      ))}
    </ul>
  )
}

type SubmissionResultProps = {
  submission: Submission
  /** True for the submission the student has just sent: its result is revealed step by step. */
  reveal: boolean
  onRetry: () => void
}

/**
 * The verdict of a graded submission. For a fresh one the tests tick in one by one and the
 * verdict lands last (brief §8, moment 5) — instantly under reduced motion.
 */
export function SubmissionResult({ submission, reveal, onRetry }: SubmissionResultProps) {
  const { t } = useTranslation()
  const rootRef = useRef<HTMLDivElement>(null)
  const finished = isFinished(submission.status)

  useGSAP(
    () => {
      if (!reveal || !finished) return
      if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
      gsap
        .timeline({ defaults: { ease: 'power2.out' } })
        .from('[data-reveal="test"]', { opacity: 0, x: -8, duration: 0.25, stagger: 0.14 })
        .from(
          '[data-reveal="verdict"]',
          { opacity: 0, scale: 0.92, duration: 0.4, ease: 'back.out(1.6)' },
          '+=0.1',
        )
        .from('[data-reveal="score"]', { opacity: 0, y: 6, duration: 0.3 }, '-=0.15')
    },
    { scope: rootRef, dependencies: [reveal, finished, submission.id] },
  )

  if (!finished) return <JobProgress status={submission.status === 'RUNNING' ? 'RUNNING' : 'QUEUED'} />
  if (submission.status !== 'COMPLETED') return <PlatformFailure onRetry={onRetry} />

  return (
    <div ref={rootRef} className="grid gap-4">
      <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
        <div data-reveal="verdict">
          {submission.verdict !== undefined && (
            <VerdictBadge
              verdict={submission.verdict}
              label={t(`verdict.${submission.verdict}`)}
              className="text-small px-3 py-1.5"
            />
          )}
        </div>
        <p data-reveal="score" className="text-small text-text-muted tabular-nums">
          {submission.testsTotal !== undefined &&
            t('task.testsPassed', { passed: submission.testsPassed ?? 0, total: submission.testsTotal })}
          {submission.score !== undefined && submission.maxScore !== undefined && (
            <>
              {' · '}
              <span className="text-text font-semibold">
                {t('task.score', { score: submission.score, max: submission.maxScore })}
              </span>
            </>
          )}
        </p>
      </div>
      {submission.compileOutput !== '' && (
        <Stream label={t('task.compileOutput')} text={submission.compileOutput} tone="danger" />
      )}
      <TestList tests={submission.tests} animate={reveal} />
      <SubmissionFeedback submissionId={submission.id} />
    </div>
  )
}

/** S-05.8 — what the teacher wrote about this submission. */
function SubmissionFeedback({ submissionId }: { submissionId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const feedback = useFeedbackQuery(submissionId)
  if (!feedback.isSuccess || feedback.data.length === 0) return null
  return (
    <section className="grid gap-2">
      <h3 className="text-small text-text inline-flex items-center gap-2 font-semibold">
        <MessageSquareText aria-hidden className="size-4" />
        {t('task.feedback')}
      </h3>
      {feedback.data.map((item) => (
        <div key={item.id} className="border-border bg-surface-raised rounded-md border px-4 py-3">
          <p className="text-caption text-text-muted mb-1 tracking-normal">
            {item.author}
            {item.createdAt !== '' && ` · ${formatDateTime(item.createdAt, locale)}`}
          </p>
          <RichText html={item.html} className="text-body font-sans" />
        </div>
      ))}
    </section>
  )
}

type HistoryListProps = {
  history: {
    isPending: boolean
    isError: boolean
    error: unknown
    refetch: () => unknown
    data?: { items: SubmissionSummary[] }
  }
  selectedId: string | undefined
  onSelect: (submission: SubmissionSummary) => void
}

/** S-05.7 — every attempt: when, in which language, with what verdict and score. */
export function HistoryList({ history, selectedId, onSelect }: HistoryListProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  if (history.isPending) return <SkeletonText lines={3} />
  if (history.isError) {
    return (
      <div role="alert" className="grid justify-items-start gap-2">
        <p className="text-small text-text-muted">{t(`errors.${toApiProblem(history.error).code}`)}</p>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            void history.refetch()
          }}
        >
          {t('common.retry')}
        </Button>
      </div>
    )
  }
  const items = history.data?.items ?? []
  if (items.length === 0) {
    return (
      <div className="text-small text-text-muted flex items-center gap-3 py-2">
        <History aria-hidden className="size-5" />
        {t('task.historyEmpty')}
      </div>
    )
  }
  return (
    <ul className="divide-border border-border divide-y rounded-md border">
      {items.map((item) => (
        <li key={item.id}>
          <button
            type="button"
            aria-pressed={item.id === selectedId}
            onClick={() => {
              onSelect(item)
            }}
            className={cn(
              'text-small flex w-full flex-wrap items-center gap-x-4 gap-y-1 px-3.5 py-2.5 text-left',
              'duration-fast ease-standard hover:bg-surface-sunken transition-[background-color]',
              'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:-outline-offset-2',
              item.id === selectedId && 'bg-surface-sunken',
            )}
          >
            <span className="text-text-muted w-8 tabular-nums">#{item.attemptNumber}</span>
            <span className="text-text w-14 font-medium">{languageName(item.language)}</span>
            {item.verdict !== undefined ? (
              <VerdictBadge verdict={item.verdict} label={t(`verdict.${item.verdict}`)} />
            ) : (
              <PendingBadge label={t(`task.status.${item.status === 'RUNNING' ? 'RUNNING' : 'QUEUED'}`)} />
            )}
            <span className="text-text-muted ml-auto tabular-nums">
              {item.score !== undefined &&
                item.maxScore !== undefined &&
                `${item.score} / ${item.maxScore} · `}
              {formatDateTime(item.submittedAt, locale)}
            </span>
          </button>
        </li>
      ))}
    </ul>
  )
}
