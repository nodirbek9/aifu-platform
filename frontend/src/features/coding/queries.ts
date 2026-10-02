import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { courseKeys } from '@/features/courses/queries'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import {
  narrowFeedback,
  narrowHints,
  narrowRun,
  narrowSubmission,
  narrowSubmissionSummary,
  narrowTask,
  pollInterval,
} from './model'

const keys = {
  task: (taskId: string, locale: Locale) => ['coding-task', taskId, locale] as const,
  hints: (taskId: string, locale: Locale) => ['coding-task-hints', taskId, locale] as const,
  run: (runId: string) => ['run', runId] as const,
  submission: (submissionId: string) => ['submission', submissionId] as const,
  history: (taskId: string) => ['submissions', taskId] as const,
  feedback: (submissionId: string) => ['feedback', 'SUBMISSION', submissionId] as const,
}

export function useTaskQuery(courseId: string, taskId: string, locale: Locale) {
  return useQuery({
    queryKey: keys.task(taskId, locale),
    queryFn: async () =>
      narrowTask(
        await unwrap(
          api.GET('/api/v1/courses/{courseId}/coding-tasks/{id}', {
            params: { path: { courseId, id: taskId }, query: { locale } },
          }),
        ),
      ),
  })
}

export function useHintsQuery(taskId: string, locale: Locale) {
  return useQuery({
    queryKey: keys.hints(taskId, locale),
    queryFn: async () =>
      narrowHints(
        await unwrap(
          api.GET('/api/v1/coding-tasks/{codingTaskId}/hints', {
            params: { path: { codingTaskId: taskId }, query: { locale } },
          }),
        ),
      ),
  })
}

/** Opens the next hint; the server records it and answers the whole list. */
export function useRevealHint(taskId: string, locale: Locale) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (hintId: string) =>
      narrowHints(
        await unwrap(
          api.POST('/api/v1/coding-tasks/{codingTaskId}/hints/{hintId}/reveal', {
            params: { path: { codingTaskId: taskId, hintId }, query: { locale } },
          }),
        ),
      ),
    onSuccess: (hints) => {
      queryClient.setQueryData(keys.hints(taskId, locale), hints)
    },
  })
}

export type RunInput = {
  /** Absent for a free run (a lesson example). */
  taskId?: string
  language: string
  source: string
  stdin?: string
  mode: 'CUSTOM' | 'EXAMPLES'
}

/** Queues an ungraded run. It never counts as an attempt (API guide §4). */
export function useStartRun() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: RunInput) => {
      const run = narrowRun(
        await unwrap(
          api.POST('/api/v1/runs', {
            body: {
              languageCode: input.language,
              sourceCode: input.source,
              mode: input.mode,
              ...(input.taskId !== undefined && { codingTaskId: input.taskId }),
              ...(input.stdin !== undefined && input.stdin !== '' && { stdin: input.stdin }),
            },
          }),
        ),
      )
      return { run, startedAt: Date.now() }
    },
    onSuccess: ({ run }) => {
      queryClient.setQueryData(keys.run(run.id), run)
    },
  })
}

/** Follows a run until it is finished. Polling stops by itself, and when the page is left. */
export function useRunQuery(runId: string | undefined, startedAt: number) {
  return useQuery({
    queryKey: keys.run(runId ?? ''),
    queryFn: async () =>
      narrowRun(await unwrap(api.GET('/api/v1/runs/{id}', { params: { path: { id: runId ?? '' } } }))),
    enabled: runId !== undefined,
    refetchInterval: (query) => pollInterval(query.state.data?.status, startedAt, Date.now()),
    staleTime: Infinity,
  })
}

export type SubmitInput = { taskId: string; language: string; source: string; idempotencyKey: string }

/** Submits a solution for grading. One idempotency key per click: a retry never submits twice. */
export function useSubmit() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (input: SubmitInput) => {
      const submission = narrowSubmission(
        await unwrap(
          api.POST('/api/v1/submissions', {
            params: { header: { 'Idempotency-Key': input.idempotencyKey } },
            body: { codingTaskId: input.taskId, languageCode: input.language, sourceCode: input.source },
          }),
        ),
      )
      return { submission, startedAt: Date.now() }
    },
    onSuccess: async ({ submission }, input) => {
      queryClient.setQueryData(keys.submission(submission.id), submission)
      await queryClient.invalidateQueries({ queryKey: keys.history(input.taskId) })
    },
  })
}

export function useSubmissionQuery(submissionId: string | undefined, startedAt: number) {
  return useQuery({
    queryKey: keys.submission(submissionId ?? ''),
    queryFn: async () =>
      narrowSubmission(
        await unwrap(api.GET('/api/v1/submissions/{id}', { params: { path: { id: submissionId ?? '' } } })),
      ),
    enabled: submissionId !== undefined,
    refetchInterval: (query) => pollInterval(query.state.data?.status, startedAt, Date.now()),
    staleTime: Infinity,
  })
}

/** Every attempt at the task, newest first, with how many there are in all. */
export function useHistoryQuery(taskId: string) {
  return useQuery({
    queryKey: keys.history(taskId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/coding-tasks/{codingTaskId}/submissions/me', {
          params: { path: { codingTaskId: taskId }, query: { page: 0, size: 50 } },
        }),
      )
      return {
        total: page.totalElements ?? 0,
        items: (page.content ?? []).map(narrowSubmissionSummary).filter((item) => item !== null),
      }
    },
  })
}

export function useFeedbackQuery(submissionId: string | undefined) {
  return useQuery({
    queryKey: keys.feedback(submissionId ?? ''),
    queryFn: async () => {
      const list = await unwrap(
        api.GET('/api/v1/feedback/me', {
          params: { query: { targetType: 'SUBMISSION', targetId: submissionId ?? '' } },
        }),
      )
      return list.map(narrowFeedback).filter((item) => item !== null)
    },
    enabled: submissionId !== undefined,
  })
}

/** After a graded submission finishes, the work list, the plan and the history are stale. */
export function useRefreshAfterGrading(courseId: string, taskId: string) {
  const queryClient = useQueryClient()
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: keys.history(taskId) }),
      queryClient.invalidateQueries({ queryKey: courseKeys.activities() }),
      queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) }),
    ])
}
