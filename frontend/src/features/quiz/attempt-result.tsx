import { useId, useRef } from 'react'
import gsap from 'gsap'
import { useGSAP } from '@gsap/react'
import { MessageSquareText } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { RichText } from '@/shared/ui/rich-text'
import { formatPoints, isAnswered, type Attempt, type Question, type Quiz } from './model'
import { QuestionInput } from './question-input'
import { useAttemptFeedbackQuery } from './queries'
import { AttemptBadge } from './quiz-start'

gsap.registerPlugin(useGSAP)

/** A result shown within this long after the attempt was handed in is "fresh": it is revealed. */
const FRESH_MS = 15_000

type AttemptResultProps = {
  quiz: Quiz
  attempt: Attempt
  courseId: string
}

/**
 * S-06.4 — the result of a finished attempt: the score and whether it passes, then the answers
 * gone through one by one when the teacher's review policy allows it.
 */
export function AttemptResult({ quiz, attempt, courseId }: AttemptResultProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const rootRef = useRef<HTMLDivElement>(null)
  const scoreRef = useRef<HTMLSpanElement>(null)
  const graded = attempt.score !== undefined && attempt.maxScore !== undefined

  // the score counts up once, for the attempt just handed in; instantly under reduced motion
  useGSAP(
    () => {
      const target = scoreRef.current
      if (target === null || attempt.score === undefined || attempt.submittedAt === undefined) return
      if (Date.now() + attempt.clockOffsetMs - Date.parse(attempt.submittedAt) > FRESH_MS) return
      if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
      const final = attempt.score
      const counter = { value: 0 }
      target.textContent = '0'
      gsap
        .timeline({ defaults: { ease: 'power2.out' } })
        .to(counter, {
          value: final,
          duration: 0.9,
          onUpdate: () => {
            target.textContent = formatPoints(Math.round(counter.value))
          },
          onComplete: () => {
            target.textContent = formatPoints(final)
          },
        })
        .from('[data-reveal="outcome"]', { opacity: 0, y: 6, duration: 0.3 }, '-=0.2')
    },
    { scope: rootRef, dependencies: [attempt.id] },
  )

  return (
    <div ref={rootRef} className="contents">
      <header className="grid gap-3">
        <p className="text-caption tracking-caption text-text-muted font-medium uppercase tabular-nums">
          {t('quiz.attemptN', { number: attempt.number })}
          {attempt.submittedAt !== undefined && ` · ${formatDateTime(attempt.submittedAt, locale)}`}
        </p>
        <h1 className="font-display text-display-lg text-text font-semibold">{quiz.title}</h1>
      </header>

      {attempt.closedByTime && <Callout variant="WARNING">{t('quiz.closedByTime')}</Callout>}

      {graded ? (
        <section
          aria-label={t('quiz.resultTitle')}
          className="border-border flex flex-wrap items-end gap-x-10 gap-y-4 border-y py-6"
        >
          <p className="font-display text-text font-semibold tabular-nums">
            <span ref={scoreRef} className="text-display-2xl">
              {formatPoints(attempt.score ?? 0)}
            </span>
            <span className="text-display-lg text-text-muted"> / {formatPoints(attempt.maxScore ?? 0)}</span>
            <span className="sr-only"> {t('task.points')}</span>
          </p>
          <div data-reveal="outcome" className="grid gap-1.5 pb-2">
            <div className="flex items-center gap-3">
              {attempt.percent !== undefined && (
                <span className="font-display text-title text-text font-semibold tabular-nums">
                  {formatPoints(attempt.percent)}%
                </span>
              )}
              <AttemptBadge attempt={attempt} />
            </div>
            <p className="text-small text-text-muted tabular-nums">
              {t('quiz.passMark')}: {formatPoints(quiz.passingPercent)}%
            </p>
          </div>
        </section>
      ) : (
        <Callout variant="INFO">{t('quiz.notGraded')}</Callout>
      )}

      <AttemptFeedback attemptId={attempt.id} />

      {attempt.reviewOpen ? (
        <section className="grid gap-4">
          <h2 className="font-display text-title text-text font-semibold">{t('quiz.reviewTitle')}</h2>
          <ol className="grid gap-4">
            {attempt.questions.map((question, index) => (
              <li key={question.id}>
                <ReviewCard question={question} number={index + 1} />
              </li>
            ))}
          </ol>
        </section>
      ) : (
        graded && (
          <Callout variant="INFO" title={t('quiz.reviewClosedTitle')}>
            {t(`quiz.reviewPolicy.${quiz.revealPolicy}`)}
          </Callout>
        )
      )}

      <div className="flex flex-wrap gap-3">
        <Link
          to={`/student/courses/${courseId}/quizzes/${quiz.id}`}
          className={button({ variant: 'primary' })}
        >
          {t('quiz.toQuizPage')}
        </Link>
        <Link to={`/student/courses/${courseId}?tab=work`} className={button({ variant: 'outline' })}>
          {t('lesson.back')}
        </Link>
      </div>
    </div>
  )
}

function ReviewCard({ question, number }: { question: Question; number: number }) {
  const { t } = useTranslation()
  const promptId = useId()
  const review = question.review
  const answered = isAnswered(question.saved)
  const verdict =
    review === undefined
      ? undefined
      : review.correct
        ? ({ tone: 'success', label: t('quiz.verdictRight') } as const)
        : !answered
          ? ({ tone: 'neutral', label: t('quiz.notAnswered') } as const)
          : review.pointsAwarded > 0
            ? ({ tone: 'warning', label: t('quiz.verdictPartial') } as const)
            : ({ tone: 'danger', label: t('quiz.verdictWrong') } as const)

  return (
    <article
      aria-labelledby={promptId}
      className="border-border bg-surface-raised grid gap-4 rounded-lg border p-4 md:p-6"
    >
      <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
        <p className="text-caption tracking-caption text-text-muted flex-1 font-medium uppercase tabular-nums">
          {t('quiz.questionN', { number })}
        </p>
        {verdict !== undefined && <Badge tone={verdict.tone}>{verdict.label}</Badge>}
        {review !== undefined && (
          <span className="text-small text-text font-semibold tabular-nums">
            {t('task.score', {
              score: formatPoints(review.pointsAwarded),
              max: formatPoints(question.points),
            })}
          </span>
        )}
      </div>
      <h3 id={promptId} className="font-reading text-body text-text font-medium whitespace-pre-line">
        {question.prompt}
      </h3>
      <QuestionInput question={question} answer={question.saved} labelledBy={promptId} />
      {review?.explanation !== undefined && (
        <Callout variant="TIP" title={t('quiz.explanation')}>
          {review.explanation}
        </Callout>
      )}
    </article>
  )
}

/** What the teacher wrote about this attempt. */
function AttemptFeedback({ attemptId }: { attemptId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const feedback = useAttemptFeedbackQuery(attemptId, true)
  if (!feedback.isSuccess || feedback.data.length === 0) return null
  return (
    <section className="grid gap-2">
      <h2 className="text-small text-text inline-flex items-center gap-2 font-semibold">
        <MessageSquareText aria-hidden className="size-4" />
        {t('task.feedback')}
      </h2>
      {feedback.data.map((item) => (
        <div key={item.id} className="border-border bg-surface-raised rounded-md border px-4 py-3">
          <p className="text-caption text-text-muted mb-1 tracking-normal">
            {item.author}
            {item.createdAt !== '' && ` · ${formatDateTime(item.createdAt, locale)}`}
          </p>
          <RichText html={item.html} />
        </div>
      ))}
    </section>
  )
}
