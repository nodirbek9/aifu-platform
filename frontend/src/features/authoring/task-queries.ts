import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import type { PublicationStatus } from './model'

type Schemas = components['schemas']
export type CodingTaskRaw = Schemas['CodingTaskDetail']
export type TestCaseRaw = Schemas['TestCaseView']
export type VerificationRaw = Schemas['VerificationView']

export const taskKeys = {
  list: (courseId: string) => ['authoring', 'tasks', courseId] as const,
  task: (taskId: string) => ['authoring', 'task', taskId] as const,
  verification: (taskId: string) => ['authoring', 'task', taskId, 'verification'] as const,
}

/** Every coding task of the course, any status. */
export function useCourseTasksQuery(courseId: string) {
  return useQuery({
    queryKey: taskKeys.list(courseId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/coding-tasks', {
          params: { path: { courseId }, query: { page: 0, size: 200 } },
        }),
      )
      return page.content ?? []
    },
  })
}

export function useCreateTask(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      code,
      title,
      description,
      locale,
    }: {
      code: string
      title: string
      description: string
      locale: Locale
    }) => {
      const task = await unwrap(
        api.POST('/api/v1/teacher/courses/{courseId}/coding-tasks', {
          params: { path: { courseId } },
          body: { code },
        }),
      )
      if (task.id === undefined) throw new Error('Task without id')
      await unwrap(
        api.PUT('/api/v1/teacher/coding-tasks/{id}/translations/{locale}', {
          params: { path: { id: task.id, locale } },
          body: { title, description },
        }),
      )
      return task.id
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: taskKeys.list(courseId) }),
  })
}

export function useTaskQuery(taskId: string) {
  return useQuery({
    queryKey: taskKeys.task(taskId),
    queryFn: () => unwrap(api.GET('/api/v1/teacher/coding-tasks/{id}', { params: { path: { id: taskId } } })),
  })
}

/** After a change the task is read again: hints, languages and tests come back in their own calls. */
function useTaskMutation<V, R>(taskId: string, mutationFn: (variables: V) => Promise<R>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: (result) => {
      if (isTaskDetail(result)) queryClient.setQueryData(taskKeys.task(taskId), result)
    },
    onSettled: (result) => {
      if (!isTaskDetail(result)) void queryClient.invalidateQueries({ queryKey: taskKeys.task(taskId) })
      void queryClient.invalidateQueries({ queryKey: ['authoring', 'tasks'] })
    },
  })
}

function isTaskDetail(value: unknown): value is CodingTaskRaw {
  return typeof value === 'object' && value !== null && 'testCases' in value && 'languages' in value
}

export type TaskTexts = {
  title: string
  description: string
  instructions: string
  constraintsText: string
  explanation: string
}

export function useSaveTaskText(taskId: string) {
  return useTaskMutation(taskId, ({ locale, texts }: { locale: Locale; texts: TaskTexts }) =>
    unwrap(
      api.PUT('/api/v1/teacher/coding-tasks/{id}/translations/{locale}', {
        params: { path: { id: taskId, locale } },
        body: {
          title: texts.title,
          ...(texts.description !== '' && { description: texts.description }),
          ...(texts.instructions !== '' && { instructions: texts.instructions }),
          ...(texts.constraintsText !== '' && { constraintsText: texts.constraintsText }),
          ...(texts.explanation !== '' && { explanation: texts.explanation }),
        },
      }),
    ),
  )
}

export type TaskSettings = Schemas['UpdateCodingTaskRequest']

export function useSaveTaskSettings(taskId: string) {
  return useTaskMutation(taskId, (settings: TaskSettings) =>
    unwrap(
      api.PATCH('/api/v1/teacher/coding-tasks/{id}', { params: { path: { id: taskId } }, body: settings }),
    ),
  )
}

export function useTaskStatus(taskId: string) {
  return useTaskMutation(taskId, (status: PublicationStatus) =>
    unwrap(
      api.PUT('/api/v1/teacher/coding-tasks/{id}/status', {
        params: { path: { id: taskId } },
        body: { status },
      }),
    ),
  )
}

export function useAttachLanguage(taskId: string) {
  return useTaskMutation(
    taskId,
    ({ code, starterCode, solutionCode }: { code: string; starterCode: string; solutionCode: string }) =>
      unwrap(
        api.POST('/api/v1/teacher/coding-tasks/{id}/languages/{languageCode}', {
          params: { path: { id: taskId, languageCode: code } },
          body: { starterCode, solutionCode },
        }),
      ),
  )
}

export function useSaveLanguageCode() {
  return useMutation({
    mutationFn: ({
      taskLanguageId,
      starterCode,
      solutionCode,
    }: {
      taskLanguageId: string
      starterCode: string
      solutionCode: string
    }) =>
      unwrap(
        api.PATCH('/api/v1/teacher/coding-task-languages/{taskLanguageId}', {
          params: { path: { taskLanguageId } },
          body: { starterCode, solutionCode },
        }),
      ),
  })
}

export function useDetachLanguage(taskId: string) {
  return useTaskMutation(taskId, (taskLanguageId: string) =>
    unwrap(
      api.DELETE('/api/v1/teacher/coding-task-languages/{taskLanguageId}', {
        params: { path: { taskLanguageId } },
      }),
    ),
  )
}

export function useAddTestCase(taskId: string) {
  return useTaskMutation(taskId, ({ stdin, expectedStdout }: { stdin: string; expectedStdout: string }) =>
    unwrap(
      api.POST('/api/v1/teacher/coding-tasks/{id}/test-cases', {
        params: { path: { id: taskId } },
        body: { stdin, expectedStdout },
      }),
    ),
  )
}

export type TestCaseFields = Schemas['UpdateTestCaseRequest']

export function useSaveTestCase() {
  return useMutation({
    mutationFn: ({ id, fields }: { id: string; fields: TestCaseFields }) =>
      unwrap(
        api.PATCH('/api/v1/teacher/coding-task-test-cases/{testCaseId}', {
          params: { path: { testCaseId: id } },
          body: fields,
        }),
      ),
  })
}

export function useDeleteTestCase(taskId: string) {
  return useTaskMutation(taskId, (id: string) =>
    unwrap(
      api.DELETE('/api/v1/teacher/coding-task-test-cases/{testCaseId}', {
        params: { path: { testCaseId: id } },
      }),
    ),
  )
}

export function useAddHint(taskId: string) {
  return useTaskMutation(
    taskId,
    async ({ penaltyPercent, body, locale }: { penaltyPercent: number; body: string; locale: Locale }) => {
      const hint = await unwrap(
        api.POST('/api/v1/teacher/coding-tasks/{id}/hints', {
          params: { path: { id: taskId } },
          body: { penaltyPercent },
        }),
      )
      if (hint.id !== undefined) {
        await unwrap(
          api.PUT('/api/v1/teacher/coding-task-hints/{hintId}/translations/{locale}', {
            params: { path: { hintId: hint.id, locale } },
            body: { body },
          }),
        )
      }
      return hint
    },
  )
}

export function useSaveHintText() {
  return useMutation({
    mutationFn: ({ hintId, locale, body }: { hintId: string; locale: Locale; body: string }) =>
      unwrap(
        api.PUT('/api/v1/teacher/coding-task-hints/{hintId}/translations/{locale}', {
          params: { path: { hintId, locale } },
          body: { body },
        }),
      ),
  })
}

export function useDeleteHint(taskId: string) {
  return useTaskMutation(taskId, (hintId: string) =>
    unwrap(api.DELETE('/api/v1/teacher/coding-task-hints/{hintId}', { params: { path: { hintId } } })),
  )
}

/** The last verification of the reference solutions; polled while a run is queued or running. */
export function useVerificationQuery(taskId: string) {
  return useQuery({
    queryKey: taskKeys.verification(taskId),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/coding-tasks/{codingTaskId}/verification', {
          params: { path: { codingTaskId: taskId } },
        }),
      ),
    refetchInterval: (query) =>
      (query.state.data?.runs ?? []).some((run) => run.status === 'QUEUED' || run.status === 'RUNNING')
        ? 1500
        : false,
  })
}

export function useVerify(taskId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () =>
      unwrap(
        api.POST('/api/v1/teacher/coding-tasks/{codingTaskId}/verification', {
          params: { path: { codingTaskId: taskId } },
        }),
      ),
    onSuccess: (view) => {
      queryClient.setQueryData(taskKeys.verification(taskId), view)
    },
  })
}

/** The languages the platform runs: what a task can be attached to. */
export function useEnabledLanguagesQuery() {
  return useQuery({
    queryKey: ['languages'],
    queryFn: async () => unwrap(api.GET('/api/v1/programming-languages')),
    staleTime: Infinity,
  })
}
