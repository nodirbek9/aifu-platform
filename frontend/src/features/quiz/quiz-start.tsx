import { useState } from 'react'
import { Check, ChevronRight, Play } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router'
import type { Activity } from '@/features/courses/model'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from '@/shared/ui/dialog'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { useCoursePlanQuery } from '@/features/courses/queries'
import { formatPoints, type AttemptSummary, type Quiz } from './model'
import { useAttemptAllowanceQuery, useStartAttempt } from './queries'

type QuizStartProps = {
  quiz: Quiz
  attempts: readonly AttemptSummary[]
  /** The activity the quiz is assigned through, when there is one: its deadline. */
  activity: Activity | undefined
  courseId?: string
  lessonId?: string
  onAttemptAlreadyOpen: () => void
}

/** S-06.1 — before the quiz: what it is, the rules, what is left — and the earlier attempts. */
export function QuizStart({ quiz, attempts, activity, courseId, onAttemptAlreadyOpen }: QuizStartProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const navigate = useNavigate()
  const start = useStartAttempt(quiz.id, locale)
  const allowance = useAttemptAllowanceQuery(quiz.id)
  const plan = useCoursePlanQuery(courseId ?? '', locale)
  const isMidterm = courseId !== undefined && courseId.length > 0 && plan.data?.midterm?.quizId === quiz.id
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [problem, setProblem] = useState<ApiProblem | null>(null)

  const used = allowance.data?.attemptsUsed ?? attempts.length
  const allowed = allowance.data?.attemptsAllowed ?? quiz.maxAttempts
  const attemptsLeft = allowed === undefined ? undefined : Math.max(0, allowed - used)
  const exhausted = allowed !== undefined && used >= allowed
  const timed = quiz.timeLimitSeconds !== undefined
  const minutes = quiz.timeLimitSeconds === undefined ? undefined : Math.ceil(quiz.timeLimitSeconds / 60)
  const overdue = activity?.state === 'OVERDUE'

  const facts: { label: string; value: string; danger?: boolean }[] = []
  if (minutes !== undefined) {
    facts.push({ label: t('quiz.timeLimit'), value: t('quiz.minutes', { count: minutes }) })
  }
  facts.push({
    label: t('task.attempts'),
    value:
      allowed === undefined
        ? t('quiz.unlimited')
        : exhausted
          ? `${used} / ${allowed}`
          : t('quiz.attemptNofM', { current: used + 1, total: allowed }),
    danger: exhausted,
  })
  facts.push({ label: t('quiz.passMark'), value: `${formatPoints(quiz.passingPercent)}%` })
  facts.push({ label: t('task.points'), value: formatPoints(quiz.maxPoints) })
  if (activity?.dueAt !== undefined) {
    facts.push({ label: t('task.deadline'), value: formatDateTime(activity.dueAt, locale), danger: overdue })
  }

  const rules = [
    t('quiz.ruleOneQuestion'),
    t('quiz.ruleAutosave'),
    ...(timed ? [t('quiz.ruleTimer'), t('quiz.ruleLeaving')] : [t('quiz.ruleResume')]),
    t(`quiz.reviewPolicy.${quiz.revealPolicy}`),
  ]

  const begin = () => {
    setProblem(null)
    start.mutate(activity === undefined ? {} : { activityId: activity.activityId }, {
      onSuccess: (attempt) => {
        void navigate({ search: `?attempt=${attempt.id}` })
      },
      onError: (error) => {
        setConfirmOpen(false)
        const failure = toApiProblem(error)
        // started in another tab: the list knows the open attempt, and the page resumes it
        if (failure.code === 'ATTEMPT_ALREADY_OPEN') onAttemptAlreadyOpen()
        else setProblem(failure)
      },
    })
  }

  return (
    <>
      <header className="grid gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <p className="text-caption tracking-caption text-text-muted font-medium uppercase">
            {t('activityType.QUIZ')}
          </p>
          {isMidterm && <Badge tone="accent">{t('quiz.midtermBadge')}</Badge>}
        </div>
        <h1 className="font-display text-display-lg text-text font-semibold">{quiz.title}</h1>
        {quiz.titleIsFallback && <FallbackMark />}
        {quiz.description !== undefined && (
          <p className="font-reading text-lead text-text-muted whitespace-pre-line">{quiz.description}</p>
        )}
      </header>

      <dl className="border-border grid grid-cols-2 gap-x-6 gap-y-4 border-y py-5 sm:grid-cols-3">
        {facts.map((fact) => (
          <div key={fact.label} className="grid gap-0.5">
            <dt className="text-caption tracking-caption text-text-muted font-medium uppercase">
              {fact.label}
            </dt>
            <dd
              className={cn(
                'font-display text-title font-semibold tabular-nums',
                fact.danger === true ? 'text-danger' : 'text-text',
              )}
            >
              {fact.value}
            </dd>
          </div>
        ))}
      </dl>

      {quiz.instructions !== undefined && (
        <p className="font-reading text-body text-text whitespace-pre-line">{quiz.instructions}</p>
      )}

      <section className="grid gap-3">
        <h2 className="font-display text-title text-text font-semibold">{t('quiz.rulesTitle')}</h2>
        <ul className="grid gap-2">
          {rules.map((rule) => (
            <li key={rule} className="text-body text-text flex items-start gap-3">
              <Check aria-hidden className="text-success mt-1 size-4 shrink-0" />
              {rule}
            </li>
          ))}
        </ul>
      </section>

      <div className="grid justify-items-start gap-3">
        {exhausted ? (
          <Callout variant="WARNING">{t('quiz.attemptsExhaustedDean')}</Callout>
        ) : (
          <>
            {problem !== null && (
              <p
                role="alert"
                className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
              >
                {t(`errors.${problem.code}`)}
              </p>
            )}
            <Button
              variant="accent"
              size="lg"
              loading={start.isPending && !timed}
              onClick={() => {
                if (timed) setConfirmOpen(true)
                else begin()
              }}
            >
              <Play aria-hidden className="size-4" />
              {used === 0 ? t('quiz.start') : t('quiz.startAgain')}
            </Button>
            {attemptsLeft !== undefined && (
              <p className="text-small text-text-muted tabular-nums">
                {t('task.attemptsLeft', { count: attemptsLeft })}
              </p>
            )}
          </>
        )}
      </div>

      {attempts.length > 0 && (
        <section className="grid gap-2">
          <h2 className="font-display text-title text-text font-semibold">{t('quiz.attemptsTitle')}</h2>
          <ul className="divide-border border-border divide-y border-y">
            {attempts.map((attempt) => (
              <li key={attempt.id}>
                <Link
                  to={{ search: `?attempt=${attempt.id}` }}
                  className={cn(
                    'flex items-center gap-4 py-3',
                    'duration-fast ease-standard hover:bg-surface-sunken -mx-2 rounded-sm px-2 transition-[background-color]',
                    'focus-visible:outline-focus-ring focus-visible:outline-2',
                  )}
                >
                  <span className="text-body text-text font-medium tabular-nums">
                    {t('quiz.attemptN', { number: attempt.number })}
                  </span>
                  <span className="text-small text-text-muted flex-1 tabular-nums">
                    {formatDateTime(attempt.submittedAt ?? attempt.startedAt, locale)}
                  </span>
                  {attempt.score !== undefined && attempt.maxScore !== undefined && (
                    <span className="text-body text-text font-semibold tabular-nums">
                      {formatPoints(attempt.score)} / {formatPoints(attempt.maxScore)}
                    </span>
                  )}
                  <AttemptBadge attempt={attempt} />
                  <ChevronRight aria-hidden className="text-text-muted size-5 shrink-0" />
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <DialogContent>
          <DialogTitle>{t('quiz.startTitle')}</DialogTitle>
          <DialogDescription>{minutes !== undefined && t('quiz.startBody', { minutes })}</DialogDescription>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">{t('quiz.notYet')}</Button>
            </DialogClose>
            <Button variant="accent" loading={start.isPending} onClick={begin}>
              {t('quiz.start')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

export function AttemptBadge({ attempt }: { attempt: Pick<AttemptSummary, 'status' | 'passed'> }) {
  const { t } = useTranslation()
  if (attempt.status === 'IN_PROGRESS') return <Badge tone="info">{t('quiz.status.IN_PROGRESS')}</Badge>
  if (attempt.passed === true) return <Badge tone="success">{t('quiz.passed')}</Badge>
  if (attempt.passed === false) return <Badge tone="danger">{t('quiz.failed')}</Badge>
  return <Badge>{t(`quiz.status.${attempt.status}`)}</Badge>
}
