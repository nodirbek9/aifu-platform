import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { narrowFeedback } from '@/features/coding/model'
import { courseKeys } from '@/features/courses/queries'
import { lessonKeys, useRefreshPathAfterWork } from '@/features/lesson/queries'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import {
  narrowAttempt,
  narrowAttemptAllowance,
  narrowAttemptSummary,
  narrowQuiz,
  toAnswerRequest,
  type Answer,
  type Attempt,
} from './model'

const keys = {
  quiz: (quizId: string, locale: Locale) => ['quiz', quizId, locale] as const,
  attempts: (quizId: string) => ['quiz-attempts', quizId] as const,
  allowance: (quizId: string) => ['quiz-allowance', quizId] as const,
  attemptOfAnyLocale: (attemptId: string) => ['quiz-attempt', attemptId] as const,
  attempt: (attemptId: string, locale: Locale) => ['quiz-attempt', attemptId, locale] as const,
  feedback: (attemptId: string) => ['feedback', 'QUIZ_ATTEMPT', attemptId] as const,
}

export function useQuizQuery(courseId: string, quizId: string, locale: Locale) {
  return useQuery({
    queryKey: keys.quiz(quizId, locale),
    queryFn: async () =>
      narrowQuiz(
        await unwrap(
          api.GET('/api/v1/courses/{courseId}/quizzes/{id}', {
            params: { path: { courseId, id: quizId }, query: { locale } },
          }),
        ),
      ),
  })
}

/** My attempts at the quiz, newest first. One left open past its time comes back already graded. */
export function useAttemptsQuery(quizId: string) {
  return useQuery({
    queryKey: keys.attempts(quizId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/quizzes/{quizId}/attempts/me', {
          params: { path: { quizId }, query: { page: 0, size: 50 } },
        }),
      )
      return (page.content ?? []).map(narrowAttemptSummary).filter((attempt) => attempt !== null)
    },
  })
}

/** How many attempts I used and how many are allowed for me. */
export function useAttemptAllowanceQuery(quizId: string) {
  return useQuery({
    queryKey: keys.allowance(quizId),
    queryFn: async () =>
      narrowAttemptAllowance(
        await unwrap(
          api.GET('/api/v1/quizzes/{quizId}/attempts/me/allowance', {
            params: { path: { quizId } },
          }),
        ),
      ),
    enabled: quizId.length > 0,
  })
}

/**
 * One attempt with its questions and what I answered. While it is open the page keeps the
 * answers itself; this is read again when the window gets the focus back, which also
 * re-measures the clock against the server's.
 */
export function useAttemptQuery(attemptId: string, locale: Locale) {
  return useQuery({
    queryKey: keys.attempt(attemptId, locale),
    queryFn: async () =>
      narrowAttempt(
        await unwrap(
          api.GET('/api/v1/attempts/{attemptId}', {
            params: { path: { attemptId }, query: { locale } },
          }),
        ),
      ),
  })
}

export type StartInput = { activityId?: string }

/** Starts an attempt: the clock of a timed quiz runs from this moment. */
export function useStartAttempt(quizId: string, locale: Locale) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: StartInput) =>
      narrowAttempt(
        await unwrap(
          api.POST('/api/v1/quizzes/{quizId}/attempts', {
            params: { path: { quizId } },
            body: { locale, ...(input.activityId !== undefined && { activityId: input.activityId }) },
          }),
        ),
      ),
    onSuccess: async (attempt) => {
      queryClient.setQueryData(keys.attempt(attempt.id, locale), attempt)
      await queryClient.invalidateQueries({ queryKey: keys.attempts(quizId) })
    },
  })
}

/** Records (or replaces) the answer to one question. Throws `ApiFailure`. */
export async function saveAnswer(attemptId: string, questionId: string, answer: Answer): Promise<void> {
  await unwrap(
    api.POST('/api/v1/attempts/{attemptId}/questions/{questionId}/answer', {
      params: { path: { attemptId, questionId } },
      body: toAnswerRequest(answer),
    }),
  )
}

/**
 * Hands the attempt in. One idempotency key per attempt: a retry after a lost answer returns
 * the same result. The work list and the course plan are stale afterwards.
 */
export function useSubmitAttempt(
  courseId: string,
  attempt: Pick<Attempt, 'id' | 'quizId'>,
  locale: Locale,
  lessonId?: string,
) {
  const queryClient = useQueryClient()
  const refreshPath = useRefreshPathAfterWork(courseId, lessonId)
  return useMutation({
    mutationFn: async () =>
      narrowAttempt(
        await unwrap(
          api.POST('/api/v1/attempts/{attemptId}/submit', {
            params: { path: { attemptId: attempt.id }, header: { 'Idempotency-Key': `quiz-${attempt.id}` } },
          }),
        ),
      ),
    onSuccess: async (submitted) => {
      queryClient.setQueryData(keys.attempt(attempt.id, locale), submitted)
      await Promise.all([
        // the submit answers in the language the attempt was started in; read it in the current one
        queryClient.invalidateQueries({ queryKey: keys.attemptOfAnyLocale(attempt.id) }),
        queryClient.invalidateQueries({ queryKey: keys.attempts(attempt.quizId) }),
        queryClient.invalidateQueries({ queryKey: keys.allowance(attempt.quizId) }),
        queryClient.invalidateQueries({ queryKey: courseKeys.activities() }),
        queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) }),
        ...(lessonId !== undefined && lessonId.length > 0
          ? [queryClient.invalidateQueries({ queryKey: lessonKeys.path(lessonId) })]
          : []),
      ])
      void refreshPath()
    },
  })
}

/** Reads the attempt again from the server — after the time ran out, or after a refused answer. */
export function useRefreshAttempt(attemptId: string, quizId: string, courseId?: string, lessonId?: string) {
  const queryClient = useQueryClient()
  const refreshPath = useRefreshPathAfterWork(courseId, lessonId)
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: keys.attemptOfAnyLocale(attemptId) }),
      queryClient.invalidateQueries({ queryKey: keys.attempts(quizId) }),
      queryClient.invalidateQueries({ queryKey: keys.allowance(quizId) }),
      queryClient.invalidateQueries({ queryKey: courseKeys.activities() }),
      ...(courseId !== undefined && courseId.length > 0
        ? [queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) })]
        : []),
      ...(lessonId !== undefined && lessonId.length > 0
        ? [queryClient.invalidateQueries({ queryKey: lessonKeys.path(lessonId) })]
        : []),
    ]).then(() => {
      void refreshPath()
    })
}

export function useAttemptFeedbackQuery(attemptId: string, enabled: boolean) {
  return useQuery({
    queryKey: keys.feedback(attemptId),
    queryFn: async () => {
      const list = await unwrap(
        api.GET('/api/v1/feedback/me', {
          params: { query: { targetType: 'QUIZ_ATTEMPT', targetId: attemptId } },
        }),
      )
      return list.map(narrowFeedback).filter((item) => item !== null)
    },
    enabled,
  })
}
