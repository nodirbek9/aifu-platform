import { useCallback, useId, useState } from 'react'
import { ArrowLeft, ArrowRight, Check, CircleAlert, Flag, Loader2, LogOut, Send } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useSearchParams } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
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
import { LocaleControl } from '@/shared/ui/locale-control'
import { Countdown } from './countdown'
import {
  formatClock,
  isAnswered,
  isSavable,
  readFlags,
  writeFlags,
  type Answer,
  type Attempt,
  type Question,
  type Quiz,
} from './model'
import { QuestionInput } from './question-input'
import { useRefreshAttempt, useSubmitAttempt } from './queries'
import { useAnswerSaver, type SaveStatus } from './use-answer-saver'
import { useRemainingSeconds } from './use-remaining-seconds'

type AttemptRunnerProps = {
  quiz: Quiz
  attempt: Attempt
  courseId: string
}

/**
 * S-06.2 — an open attempt: one question on the screen, the navigator beside it, answers saved
 * as the student goes, the clock in the header. At zero the attempt hands itself in.
 */
export function AttemptRunner({ quiz, attempt, courseId }: AttemptRunnerProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { changeLocale } = useAuth()
  const navigate = useNavigate()
  const promptId = useId()
  const [search, setSearch] = useSearchParams()
  const questions = attempt.questions

  const [answers, setAnswers] = useState<Record<string, Answer>>(() =>
    Object.fromEntries(questions.map((question) => [question.id, question.saved])),
  )
  const [flags, setFlags] = useState(() => readFlags(attempt.id))
  const [finishOpen, setFinishOpen] = useState(false)
  const [leaveOpen, setLeaveOpen] = useState(false)
  const [finishing, setFinishing] = useState(false)
  const [timeUp, setTimeUp] = useState(false)
  const [problem, setProblem] = useState<ApiProblem | 'unsaved' | null>(null)

  const refresh = useRefreshAttempt(attempt.id, attempt.quizId)
  const submit = useSubmitAttempt(courseId, attempt, locale)
  const saver = useAnswerSaver(
    attempt.id,
    // the server says the attempt is over: read it again, the result takes this screen's place
    useCallback(() => {
      void refresh()
    }, [refresh]),
  )

  const requested = Number(search.get('q') ?? '1')
  const index = Number.isInteger(requested) ? Math.min(Math.max(requested, 1), questions.length) - 1 : 0
  const question = questions[index]
  const answeredCount = questions.filter((item) => isAnswered(answers[item.id] ?? item.saved)).length
  const unanswered = questions.flatMap((item, position) =>
    isAnswered(answers[item.id] ?? item.saved) ? [] : [position],
  )

  const goTo = (position: number) => {
    setSearch(
      (previous) => {
        const next = new URLSearchParams(previous)
        next.set('q', String(position + 1))
        return next
      },
      { replace: true },
    )
    window.scrollTo({ top: 0 })
  }

  const change = (target: Question, answer: Answer, settled: boolean) => {
    setAnswers((previous) => ({ ...previous, [target.id]: answer }))
    if (isSavable(answer)) saver.save(target.id, answer, settled)
  }

  const toggleFlag = (questionId: string) => {
    const next = flags.includes(questionId) ? flags.filter((id) => id !== questionId) : [...flags, questionId]
    setFlags(next)
    writeFlags(attempt.id, next)
  }

  const handIn = () => {
    submit.mutate(undefined, {
      onSuccess: () => {
        writeFlags(attempt.id, [])
      },
      onError: (error) => {
        setFinishing(false)
        setProblem(toApiProblem(error))
        // the server may have closed the attempt by time already: its own state decides
        void refresh()
      },
    })
  }

  const finish = async () => {
    setProblem(null)
    setFinishing(true)
    const saved = await saver.flush()
    if (!saved) {
      setFinishing(false)
      setProblem('unsaved')
      return
    }
    handIn()
  }

  const elapsed = () => {
    setTimeUp(true)
    setFinishOpen(false)
    setLeaveOpen(false)
    // nothing more can be saved after the time: the server grades what it has
    handIn()
  }

  if (question === undefined) {
    return (
      <div className="mx-auto w-full max-w-xl p-6">
        <Callout variant="WARNING">{t('quiz.noQuestions')}</Callout>
      </div>
    )
  }
  const answer = answers[question.id] ?? question.saved
  const flagged = flags.includes(question.id)
  const last = index === questions.length - 1

  return (
    <div className="bg-surface flex min-h-dvh flex-col">
      <header className="border-border bg-surface sticky top-0 z-20 flex h-14 shrink-0 items-center gap-2 border-b px-2 md:px-6">
        <Button
          variant="ghost"
          size="sm"
          className="shrink-0"
          onClick={() => {
            setLeaveOpen(true)
          }}
        >
          <LogOut aria-hidden className="size-4 rotate-180" />
          <span className="sr-only sm:not-sr-only">{t('quiz.leave')}</span>
        </Button>
        <p className="text-small text-text hidden min-w-0 flex-1 truncate font-medium md:block">
          {quiz.title}
        </p>
        <div className="flex-1 md:hidden" />
        <SaveIndicator
          status={saver.status}
          onRetry={() => {
            void saver.retry()
          }}
        />
        <Countdown attempt={attempt} onElapsed={elapsed} />
        <LocaleControl value={locale} onChange={changeLocale} />
      </header>

      <div className="max-w-app mx-auto flex w-full flex-1 flex-col gap-6 px-4 py-4 md:px-6 md:py-8 lg:flex-row-reverse lg:gap-10">
        <aside className="lg:w-64 lg:shrink-0">
          <div className="grid gap-4 lg:sticky lg:top-22">
            <Navigator questions={questions} answers={answers} flags={flags} current={index} onGo={goTo} />
            <p className="text-small text-text-muted tabular-nums">
              {t('quiz.answeredCount', { answered: answeredCount, total: questions.length })}
            </p>
            <Button
              variant="accent"
              className="hidden lg:inline-flex"
              onClick={() => {
                setProblem(null)
                setFinishOpen(true)
              }}
            >
              <Send aria-hidden className="size-4" />
              {t('quiz.finish')}
            </Button>
          </div>
        </aside>

        <main className="flex min-w-0 flex-1 flex-col gap-8">
          <section aria-labelledby={promptId} className="max-w-lesson grid gap-5">
            <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1">
              <p className="text-caption tracking-caption text-text-muted font-medium uppercase tabular-nums">
                {t('quiz.questionOf', { number: index + 1, total: questions.length })}
                {' · '}
                {t('quiz.points', { count: question.points })}
              </p>
              <Button
                variant="ghost"
                size="sm"
                aria-pressed={flagged}
                className={cn(flagged && 'text-accent-text')}
                onClick={() => {
                  toggleFlag(question.id)
                }}
              >
                <Flag aria-hidden className={cn('size-4', flagged && 'fill-current')} />
                {flagged ? t('quiz.unflag') : t('quiz.flag')}
              </Button>
            </div>
            <h1 id={promptId} className="font-reading text-lead text-text font-medium whitespace-pre-line">
              {question.prompt}
            </h1>
            <p className="text-small text-text-muted">{t(`quiz.typeHint.${question.type}`)}</p>
            {question.hint !== undefined && <Callout variant="TIP">{question.hint}</Callout>}
            <QuestionInput
              key={question.id}
              question={question}
              answer={answer}
              labelledBy={promptId}
              onChange={(next, settled) => {
                change(question, next, settled)
              }}
            />
          </section>

          <div
            className={cn(
              'border-border bg-surface pb-safe sticky bottom-0 -mx-4 mt-auto flex items-center gap-3 border-t px-4 pt-3',
              'md:max-w-lesson md:static md:mx-0 md:mt-0 md:border-0 md:px-0 md:pt-0',
            )}
          >
            <Button
              variant="outline"
              disabled={index === 0}
              onClick={() => {
                goTo(index - 1)
              }}
            >
              <ArrowLeft aria-hidden className="size-4" />
              {t('common.previous')}
            </Button>
            <div className="flex-1" />
            {last ? (
              <Button
                variant="accent"
                onClick={() => {
                  setProblem(null)
                  setFinishOpen(true)
                }}
              >
                <Send aria-hidden className="size-4" />
                {t('quiz.finish')}
              </Button>
            ) : (
              <Button
                onClick={() => {
                  goTo(index + 1)
                }}
              >
                {t('common.next')}
                <ArrowRight aria-hidden className="size-4" />
              </Button>
            )}
          </div>
        </main>
      </div>

      <Dialog
        open={finishOpen}
        onOpenChange={(open) => {
          if (!finishing) setFinishOpen(open)
        }}
      >
        <DialogContent>
          <DialogTitle>{t('quiz.finishTitle')}</DialogTitle>
          <DialogDescription>{t('quiz.finishBody')}</DialogDescription>
          <div className="mt-4 grid gap-3">
            <p className="text-body text-text tabular-nums">
              {t('quiz.answeredCount', { answered: answeredCount, total: questions.length })}
            </p>
            {unanswered.length > 0 && (
              <div className="grid gap-2">
                <p className="text-small text-warning flex items-center gap-2 font-medium">
                  <CircleAlert aria-hidden className="size-4" />
                  {t('quiz.unansweredTitle')}
                </p>
                <div className="flex flex-wrap gap-2">
                  {unanswered.map((position) => (
                    <Button
                      key={position}
                      variant="outline"
                      size="sm"
                      className="tabular-nums"
                      aria-label={t('quiz.goToQuestion', { number: position + 1 })}
                      onClick={() => {
                        setFinishOpen(false)
                        goTo(position)
                      }}
                    >
                      {position + 1}
                    </Button>
                  ))}
                </div>
              </div>
            )}
            <TimeLeftLine attempt={attempt} />
            {problem !== null && (
              <p
                role="alert"
                className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
              >
                {problem === 'unsaved' ? t('quiz.unsavedAnswers') : t(`errors.${problem.code}`)}
              </p>
            )}
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline" disabled={finishing}>
                {t('quiz.keepWorking')}
              </Button>
            </DialogClose>
            <Button
              variant="accent"
              loading={finishing}
              onClick={() => {
                void finish()
              }}
            >
              {t('quiz.finish')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={leaveOpen} onOpenChange={setLeaveOpen}>
        <DialogContent>
          <DialogTitle>{t('quiz.leaveTitle')}</DialogTitle>
          <DialogDescription>
            {attempt.expiresAt === undefined ? t('quiz.leaveBody') : t('quiz.leaveBodyTimed')}
          </DialogDescription>
          <div className="mt-4">
            <TimeLeftLine attempt={attempt} />
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">{t('quiz.stay')}</Button>
            </DialogClose>
            <Button
              onClick={() => {
                void saver.flush()
                void navigate(`/student/courses/${courseId}?tab=work`)
              }}
            >
              {t('quiz.leave')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {timeUp && (
        <div
          role="alertdialog"
          aria-modal="true"
          aria-labelledby={`${promptId}-time-up`}
          className="bg-surface fixed inset-0 z-50 flex items-center justify-center p-6"
        >
          <div className="grid max-w-md justify-items-center gap-4 text-center">
            <h2 id={`${promptId}-time-up`} className="font-display text-display-lg text-text font-semibold">
              {t('quiz.timeUpTitle')}
            </h2>
            {problem === null || problem === 'unsaved' ? (
              <p className="text-body text-text-muted flex items-center gap-2">
                <Loader2 aria-hidden className="size-5 animate-spin" />
                {t('quiz.timeUpBody')}
              </p>
            ) : (
              <>
                <p role="alert" className="text-body text-danger">
                  {t(`errors.${problem.code}`)}
                </p>
                <Button
                  variant="outline"
                  onClick={() => {
                    setProblem(null)
                    handIn()
                  }}
                >
                  {t('common.retry')}
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

/** "Time left: 12:34" — in the dialogs, where the header's clock is behind the overlay. */
function TimeLeftLine({ attempt }: { attempt: Attempt }) {
  const { t } = useTranslation()
  const remaining = useRemainingSeconds(attempt)
  if (remaining === undefined) return null
  return (
    <p className="text-small text-text-muted tabular-nums">
      {t('quiz.timeLeft')}: <span className="text-text font-semibold">{formatClock(remaining)}</span>
    </p>
  )
}

function SaveIndicator({ status, onRetry }: { status: SaveStatus; onRetry: () => void }) {
  const { t } = useTranslation()
  return (
    <div role="status" className="text-small text-text-muted flex shrink-0 items-center gap-1.5">
      {status === 'saving' && (
        <>
          <Loader2 aria-hidden className="size-4 animate-spin" />
          <span className="sr-only sm:not-sr-only">{t('quiz.saving')}</span>
        </>
      )}
      {status === 'saved' && (
        <>
          <Check aria-hidden className="text-success size-4" />
          <span>{t('quiz.saved')}</span>
        </>
      )}
      {status === 'error' && (
        <>
          <CircleAlert aria-hidden className="text-danger size-4" />
          <span className="text-danger font-medium">{t('quiz.notSaved')}</span>
          <Button variant="outline" size="sm" onClick={onRetry}>
            {t('common.retry')}
          </Button>
        </>
      )}
    </div>
  )
}

type NavigatorProps = {
  questions: readonly Question[]
  answers: Readonly<Record<string, Answer>>
  flags: readonly string[]
  current: number
  onGo: (position: number) => void
}

/** Every question as a numbered square: where I am, what is answered, what I marked. */
function Navigator({ questions, answers, flags, current, onGo }: NavigatorProps) {
  const { t } = useTranslation()
  return (
    <nav aria-label={t('quiz.navigator')}>
      <ol className="-mx-4 flex gap-2 overflow-x-auto px-4 py-1 lg:mx-0 lg:flex-wrap lg:overflow-visible lg:px-0">
        {questions.map((question, position) => {
          const answered = isAnswered(answers[question.id] ?? question.saved)
          const flagged = flags.includes(question.id)
          const state = [
            answered ? t('quiz.stateAnswered') : t('quiz.stateUnanswered'),
            ...(flagged ? [t('quiz.stateFlagged')] : []),
          ].join(', ')
          return (
            <li key={question.id} className="shrink-0">
              <button
                type="button"
                aria-current={position === current ? 'step' : undefined}
                aria-label={`${t('quiz.goToQuestion', { number: position + 1 })} — ${state}`}
                onClick={() => {
                  onGo(position)
                }}
                className={cn(
                  'text-small relative flex size-11 items-center justify-center rounded-sm border font-semibold tabular-nums',
                  'duration-fast ease-standard transition-[background-color,border-color,color,box-shadow]',
                  'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                  answered
                    ? 'border-primary bg-primary text-primary-foreground hover:bg-primary-hover'
                    : 'border-border-strong bg-surface-raised text-text hover:bg-surface-sunken',
                  'aria-[current=step]:outline-primary aria-[current=step]:outline-2 aria-[current=step]:outline-offset-2',
                )}
              >
                {position + 1}
                {flagged && (
                  <span
                    aria-hidden
                    className="bg-accent border-surface absolute -top-1 -right-1 size-3 rounded-full border-2"
                  />
                )}
              </button>
            </li>
          )
        })}
      </ol>
    </nav>
  )
}
