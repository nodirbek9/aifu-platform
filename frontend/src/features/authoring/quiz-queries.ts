import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import type { PublicationStatus } from './model'

type Schemas = components['schemas']
export type QuizRaw = Schemas['QuizDetail']
export type QuestionRaw = Schemas['QuestionDetail']
export type QuestionType = NonNullable<QuestionRaw['questionType']>
export type MatchMode = Schemas['CreateAnswerKeyRequest']['matchMode']

export const quizKeys = {
  list: (courseId: string) => ['authoring', 'quizzes', courseId] as const,
  quiz: (quizId: string) => ['authoring', 'quiz', quizId] as const,
  questions: (quizId: string) => ['authoring', 'quiz', quizId, 'questions'] as const,
}

export function useCourseQuizzesQuery(courseId: string) {
  return useQuery({
    queryKey: quizKeys.list(courseId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/quizzes', {
          params: { path: { courseId }, query: { page: 0, size: 200 } },
        }),
      )
      return page.content ?? []
    },
  })
}

export function useCreateQuiz(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ code, title, locale }: { code: string; title: string; locale: Locale }) => {
      const quiz = await unwrap(
        api.POST('/api/v1/teacher/courses/{courseId}/quizzes', {
          params: { path: { courseId } },
          body: { code },
        }),
      )
      if (quiz.id === undefined) throw new Error('Quiz without id')
      await unwrap(
        api.PUT('/api/v1/teacher/quizzes/{id}/translations/{locale}', {
          params: { path: { id: quiz.id, locale } },
          body: { title },
        }),
      )
      return quiz.id
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: quizKeys.list(courseId) }),
  })
}

export function useQuizQuery(quizId: string) {
  return useQuery({
    queryKey: quizKeys.quiz(quizId),
    queryFn: () => unwrap(api.GET('/api/v1/teacher/quizzes/{id}', { params: { path: { id: quizId } } })),
  })
}

export function useQuestionsQuery(quizId: string) {
  return useQuery({
    queryKey: quizKeys.questions(quizId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/quizzes/{quizId}/questions', {
          params: { path: { quizId }, query: { page: 0, size: 200 } },
        }),
      )
      return (page.content ?? []).slice().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
    },
    enabled: quizId !== '',
  })
}

function useQuizMutation<V, R>(quizId: string, mutationFn: (variables: V) => Promise<R>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: (result) => {
      if (typeof result === 'object' && result !== null && 'revealPolicy' in result) {
        queryClient.setQueryData(quizKeys.quiz(quizId), result)
      }
      void queryClient.invalidateQueries({ queryKey: ['authoring', 'quizzes'] })
    },
  })
}

export type QuizTexts = { title: string; description: string; instructions: string }

export function useSaveQuizText(quizId: string) {
  return useQuizMutation(quizId, ({ locale, texts }: { locale: Locale; texts: QuizTexts }) =>
    unwrap(
      api.PUT('/api/v1/teacher/quizzes/{id}/translations/{locale}', {
        params: { path: { id: quizId, locale } },
        body: {
          title: texts.title,
          ...(texts.description !== '' && { description: texts.description }),
          ...(texts.instructions !== '' && { instructions: texts.instructions }),
        },
      }),
    ),
  )
}

export type QuizSettings = Schemas['UpdateQuizRequest']

export function useSaveQuizSettings(quizId: string) {
  return useQuizMutation(quizId, (settings: QuizSettings) =>
    unwrap(api.PATCH('/api/v1/teacher/quizzes/{id}', { params: { path: { id: quizId } }, body: settings })),
  )
}

export function useQuizStatus(quizId: string) {
  return useQuizMutation(quizId, (status: PublicationStatus) =>
    unwrap(
      api.PUT('/api/v1/teacher/quizzes/{id}/status', { params: { path: { id: quizId } }, body: { status } }),
    ),
  )
}

/** Changes to questions refresh the question list (options and keys come with each question). */
function useQuestionMutation<V, R>(quizId: string, mutationFn: (variables: V) => Promise<R>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSettled: () => queryClient.invalidateQueries({ queryKey: quizKeys.questions(quizId) }),
  })
}

/** A new question of a type comes with what that type needs to be answerable. */
export function useAddQuestion(quizId: string) {
  return useQuestionMutation(
    quizId,
    async ({ type, prompt, locale }: { type: QuestionType; prompt: string; locale: Locale }) => {
      const question = await unwrap(
        api.POST('/api/v1/teacher/quizzes/{quizId}/questions', {
          params: { path: { quizId } },
          body: { questionType: type, points: 1 },
        }),
      )
      if (question.id === undefined) return question
      await unwrap(
        api.PUT('/api/v1/teacher/questions/{id}/translations/{locale}', {
          params: { path: { id: question.id, locale } },
          body: { prompt },
        }),
      )
      return question
    },
  )
}

export type QuestionTexts = { prompt: string; hint: string; explanation: string }

export function useSaveQuestionText(quizId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, locale, texts }: { id: string; locale: Locale; texts: QuestionTexts }) =>
      unwrap(
        api.PUT('/api/v1/teacher/questions/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: {
            prompt: texts.prompt,
            ...(texts.hint !== '' && { hint: texts.hint }),
            ...(texts.explanation !== '' && { explanation: texts.explanation }),
          },
        }),
      ),
    onSuccess: (question) => {
      queryClient.setQueryData<QuestionRaw[]>(quizKeys.questions(quizId), (list) =>
        list?.map((item) => (item.id === question.id ? question : item)),
      )
    },
  })
}

export function useSaveQuestionFields(quizId: string) {
  return useQuestionMutation(quizId, ({ id, points }: { id: string; points: number }) =>
    unwrap(api.PATCH('/api/v1/teacher/questions/{id}', { params: { path: { id } }, body: { points } })),
  )
}

export function useDeleteQuestion(quizId: string) {
  return useQuestionMutation(quizId, (id: string) =>
    unwrap(api.DELETE('/api/v1/teacher/questions/{id}', { params: { path: { id } } })),
  )
}

export function useReorderQuestions(quizId: string) {
  return useQuestionMutation(quizId, (orderedQuestionIds: string[]) =>
    unwrap(
      api.PUT('/api/v1/teacher/quizzes/{quizId}/questions/reorder', {
        params: { path: { quizId } },
        body: { orderedQuestionIds },
      }),
    ),
  )
}

export function useAddOption(quizId: string) {
  return useQuestionMutation(
    quizId,
    async ({
      questionId,
      isCorrect,
      matchKey,
      text,
      locale,
    }: {
      questionId: string
      isCorrect: boolean
      matchKey?: string
      text: string
      locale: Locale
    }) => {
      const option = await unwrap(
        api.POST('/api/v1/teacher/questions/{id}/options', {
          params: { path: { id: questionId } },
          body: { isCorrect, ...(matchKey !== undefined && { matchKey }) },
        }),
      )
      if (option.id !== undefined && text !== '') {
        await unwrap(
          api.PUT('/api/v1/teacher/options/{optionId}/translations/{locale}', {
            params: { path: { optionId: option.id, locale } },
            body: { text },
          }),
        )
      }
      return option
    },
  )
}

export function useSaveOptionText() {
  return useMutation({
    mutationFn: ({
      optionId,
      locale,
      text,
      feedback,
    }: {
      optionId: string
      locale: Locale
      text: string
      feedback: string
    }) =>
      unwrap(
        api.PUT('/api/v1/teacher/options/{optionId}/translations/{locale}', {
          params: { path: { optionId, locale } },
          body: { text, ...(feedback !== '' && { feedback }) },
        }),
      ),
  })
}

export function useSaveOptionFields(quizId: string) {
  return useQuestionMutation(
    quizId,
    ({ optionId, isCorrect, matchKey }: { optionId: string; isCorrect: boolean; matchKey?: string }) =>
      unwrap(
        api.PATCH('/api/v1/teacher/options/{optionId}', {
          params: { path: { optionId } },
          body: { isCorrect, ...(matchKey !== undefined && { matchKey }) },
        }),
      ),
  )
}

export function useDeleteOption(quizId: string) {
  return useQuestionMutation(quizId, (optionId: string) =>
    unwrap(api.DELETE('/api/v1/teacher/options/{optionId}', { params: { path: { optionId } } })),
  )
}

export function useReorderOptions(quizId: string) {
  return useQuestionMutation(
    quizId,
    ({ questionId, orderedOptionIds }: { questionId: string; orderedOptionIds: string[] }) =>
      unwrap(
        api.PUT('/api/v1/teacher/questions/{id}/options/reorder', {
          params: { path: { id: questionId } },
          body: { orderedOptionIds },
        }),
      ),
  )
}

export function useAddAnswerKey(quizId: string) {
  return useQuestionMutation(
    quizId,
    ({
      questionId,
      expectedValue,
      matchMode,
      tolerance,
      locale,
    }: {
      questionId: string
      expectedValue: string
      matchMode: MatchMode
      tolerance?: number
      locale?: Locale
    }) =>
      unwrap(
        api.POST('/api/v1/teacher/questions/{id}/answer-keys', {
          params: { path: { id: questionId } },
          body: {
            expectedValue,
            matchMode,
            ...(tolerance !== undefined && { tolerance }),
            ...(locale !== undefined && { locale }),
          },
        }),
      ),
  )
}

export function useDeleteAnswerKey(quizId: string) {
  return useQuestionMutation(quizId, (keyId: string) =>
    unwrap(api.DELETE('/api/v1/teacher/answer-keys/{keyId}', { params: { path: { keyId } } })),
  )
}
