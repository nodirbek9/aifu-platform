import { useTranslation } from 'react-i18next'
import { Navigate, useParams, useSearchParams } from 'react-router'
import { useActivitiesQuery, useCourseQuery } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { AttemptResult } from './attempt-result'
import { AttemptRunner } from './attempt-runner'
import type { Quiz } from './model'
import { useAttemptQuery, useAttemptsQuery, useQuizQuery } from './queries'
import { QuizFrame } from './quiz-frame'
import { QuizStart } from './quiz-start'

function Loading() {
  return (
    <>
      <Skeleton className="h-10 w-2/3" />
      <SkeletonText lines={6} />
    </>
  )
}

/**
 * S-06 — the quiz. Without `?attempt=` it is the start screen; an attempt left open is resumed
 * at once (its clock is running). With `?attempt=` it is that attempt: being taken while it is
 * open, its result once it is handed in.
 */
export function QuizPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { courseId = '', quizId: paramQuizId = '', activityId: paramActivityId = '' } = useParams()
  const [search] = useSearchParams()
  const attemptId = search.get('attempt')

  const activities = useActivitiesQuery(locale, courseId)
  const resolvedActivity =
    paramActivityId.length > 0
      ? activities.data?.find((activity) => activity.activityId === paramActivityId)
      : undefined

  const effectiveQuizId = paramQuizId.length > 0 ? paramQuizId : (resolvedActivity?.referenceId ?? '')

  const quiz = useQuizQuery(courseId, effectiveQuizId, locale)
  const attempts = useAttemptsQuery(effectiveQuizId)
  const course = useCourseQuery(courseId, locale)

  if (resolvedActivity?.type === 'CODING_TASK' && resolvedActivity.referenceId !== undefined) {
    return <Navigate to={`/student/courses/${courseId}/tasks/${resolvedActivity.referenceId}`} replace />
  }

  const activeActivity =
    resolvedActivity ?? activities.data?.find((activity) => activity.referenceId === effectiveQuizId)
  const lessonId = search.get('lesson') ?? search.get('lessonId') ?? activeActivity?.lessonId

  const coursePath = `/student/courses/${courseId}?tab=work`
  const courseLabel = course.data?.title ?? t('lesson.back')

  if (quiz.isError || attempts.isError) {
    const failed = quiz.isError ? quiz : attempts
    return (
      <QuizFrame backTo={coursePath} backLabel={courseLabel}>
        <ErrorState
          problem={toApiProblem(failed.error)}
          onRetry={() => {
            void failed.refetch()
          }}
        />
      </QuizFrame>
    )
  }
  if (quiz.isPending || attempts.isPending) {
    return (
      <QuizFrame backTo={coursePath} backLabel={courseLabel}>
        <Loading />
      </QuizFrame>
    )
  }

  if (attemptId !== null) {
    return (
      <AttemptView
        key={attemptId}
        quiz={quiz.data}
        attemptId={attemptId}
        courseId={courseId}
        lessonId={lessonId}
      />
    )
  }

  const open = attempts.data.find((attempt) => attempt.status === 'IN_PROGRESS')
  if (open !== undefined) return <Navigate to={{ search: `?attempt=${open.id}` }} replace />

  return (
    <QuizFrame backTo={coursePath} backLabel={courseLabel}>
      <QuizStart
        quiz={quiz.data}
        attempts={attempts.data}
        activity={activeActivity}
        courseId={courseId}
        lessonId={lessonId}
        onAttemptAlreadyOpen={() => {
          void attempts.refetch()
        }}
      />
    </QuizFrame>
  )
}

function AttemptView({
  quiz,
  attemptId,
  courseId,
  lessonId,
}: {
  quiz: Quiz
  attemptId: string
  courseId: string
  lessonId?: string
}) {
  const locale = useLocale()
  const attempt = useAttemptQuery(attemptId, locale)
  const quizPath = `/student/courses/${courseId}/quizzes/${quiz.id}`

  if (attempt.isPending) {
    return (
      <QuizFrame backTo={quizPath} backLabel={quiz.title}>
        <Loading />
      </QuizFrame>
    )
  }
  if (attempt.isError) {
    return (
      <QuizFrame backTo={quizPath} backLabel={quiz.title}>
        <ErrorState
          problem={toApiProblem(attempt.error)}
          onRetry={() => {
            void attempt.refetch()
          }}
        />
      </QuizFrame>
    )
  }
  // an attempt id of another quiz in this address: the quiz's own page is the place to start from
  if (attempt.data.quizId !== quiz.id) return <Navigate to={quizPath} replace />

  if (attempt.data.status === 'IN_PROGRESS') {
    return <AttemptRunner quiz={quiz} attempt={attempt.data} courseId={courseId} lessonId={lessonId} />
  }
  return (
    <QuizFrame backTo={quizPath} backLabel={quiz.title}>
      <AttemptResult quiz={quiz} attempt={attempt.data} courseId={courseId} />
    </QuizFrame>
  )
}
