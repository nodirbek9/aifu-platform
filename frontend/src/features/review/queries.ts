import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'

type Schemas = components['schemas']
export type ReviewItem = Schemas['ReviewQueueItem']
export type TeacherSubmission = Schemas['SubmissionDetail']
export type TeacherFeedback = Schemas['FeedbackView']

export type ReviewFilter = {
  taskId?: string
  verdict?: string
  groupCode?: string
  needsFeedback: boolean
  latestOnly: boolean
  page: number
}

export const reviewKeys = {
  queue: (courseId: string) => ['teacher', 'review-queue', courseId] as const,
  submission: (id: string) => ['teacher', 'submission', id] as const,
  feedback: (id: string) => ['teacher', 'feedback', 'SUBMISSION', id] as const,
}

const PAGE_SIZE = 20

/** T-07 — the submissions of a course, newest first, with the queue's filters. */
export function useReviewQueueQuery(courseId: string, filter: ReviewFilter, locale: Locale) {
  return useQuery({
    queryKey: [...reviewKeys.queue(courseId), filter, locale],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/submissions', {
          params: {
            path: { courseId },
            query: {
              ...(filter.taskId !== undefined && { taskId: filter.taskId }),
              ...(filter.verdict !== undefined && { verdict: filter.verdict }),
              ...(filter.groupCode !== undefined && { groupCode: filter.groupCode }),
              needsFeedback: filter.needsFeedback,
              latestOnly: filter.latestOnly,
              page: filter.page,
              size: PAGE_SIZE,
              locale,
            },
          },
        }),
      ),
    placeholderData: keepPreviousData,
  })
}

/** How many latest submissions of a course still wait for the teacher's word (the dashboard count). */
export function useWaitingCountQuery(courseId: string) {
  return useQuery({
    queryKey: [...reviewKeys.queue(courseId), 'waiting'],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/submissions', {
          params: { path: { courseId }, query: { needsFeedback: true, latestOnly: true, page: 0, size: 1 } },
        }),
      )
      return page.totalElements ?? 0
    },
  })
}

export function useTeacherSubmissionQuery(taskId: string, submissionId: string) {
  return useQuery({
    queryKey: reviewKeys.submission(submissionId),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/coding-tasks/{codingTaskId}/submissions/{id}', {
          params: { path: { codingTaskId: taskId, id: submissionId } },
        }),
      ),
  })
}

export function useSubmissionFeedbackQuery(submissionId: string) {
  return useQuery({
    queryKey: reviewKeys.feedback(submissionId),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/feedback', {
          params: { query: { targetType: 'SUBMISSION', targetId: submissionId } },
        }),
      ),
  })
}

export type Visibility = NonNullable<Schemas['CreateFeedbackRequest']['visibility']>

export function useWriteFeedback(submissionId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ body, visibility }: { body: string; visibility: Visibility }) =>
      unwrap(
        api.POST('/api/v1/teacher/feedback', {
          body: { targetType: 'SUBMISSION', targetId: submissionId, bodyMarkdown: body, visibility },
        }),
      ),
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: reviewKeys.feedback(submissionId) })
      void queryClient.invalidateQueries({ queryKey: ['teacher', 'review-queue'] })
    },
  })
}
