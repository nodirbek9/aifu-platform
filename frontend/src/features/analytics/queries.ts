import { useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'

const key = (courseId: string, part: string) => ['teacher', 'analytics', courseId, part] as const

export function useCompletionQuery(courseId: string) {
  return useQuery({
    queryKey: key(courseId, 'completion'),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/analytics/completion', {
          params: { path: { courseId } },
        }),
      ),
  })
}

export function useLessonAnalyticsQuery(courseId: string) {
  return useQuery({
    queryKey: key(courseId, 'lessons'),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/analytics/lessons', { params: { path: { courseId } } }),
      ),
  })
}

export function useQuizAnalyticsQuery(courseId: string) {
  return useQuery({
    queryKey: key(courseId, 'quizzes'),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/analytics/quizzes', { params: { path: { courseId } } }),
      ),
  })
}

export function useTaskAnalyticsQuery(courseId: string) {
  return useQuery({
    queryKey: key(courseId, 'tasks'),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/analytics/coding-tasks', {
          params: { path: { courseId } },
        }),
      ),
  })
}

/** Item analysis: how many answered each question of a quiz right. */
export function useQuestionAnalyticsQuery(quizId: string | undefined) {
  return useQuery({
    queryKey: ['teacher', 'analytics', 'quiz', quizId],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/teacher/quizzes/{quizId}/analytics/questions', {
          params: { path: { quizId: quizId ?? '' } },
        }),
      ),
    enabled: quizId !== undefined,
  })
}
