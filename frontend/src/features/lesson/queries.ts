import { useCallback } from 'react'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { CoursePlan } from '@/features/courses/model'
import { courseKeys } from '@/features/courses/queries'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import {
  evaluatePollingRetry,
  hasCoursePlanChanged,
  hasLessonPathChanged,
  narrowLesson,
  narrowLessonPath,
  type LessonPath,
} from './model'

export const lessonKeys = {
  detail: (lessonId: string, locale: Locale) => ['lesson', lessonId, locale] as const,
  path: (lessonId: string) => ['lesson-path', lessonId] as const,
}

export function useLessonQuery(lessonId: string, locale: Locale) {
  return useQuery({
    queryKey: lessonKeys.detail(lessonId, locale),
    queryFn: async () =>
      narrowLesson(
        await unwrap(
          api.GET('/api/v1/lessons/{lessonId}', { params: { path: { lessonId }, query: { locale } } }),
        ),
        locale,
      ),
    // Switching the language keeps the text on screen (and the reading position) until the
    // other language arrives (spec S-04.6).
    placeholderData: keepPreviousData,
  })
}

export function useLessonPathQuery(lessonId: string, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: lessonKeys.path(lessonId),
    queryFn: async () =>
      narrowLessonPath(
        await unwrap(api.GET('/api/v1/lessons/{lessonId}/path', { params: { path: { lessonId } } })),
      ),
    enabled: options?.enabled ?? true,
  })
}

export type ReadingBeatPayload = {
  maxBlockPosition: number
  atEnd: boolean
}

export function useRecordReadingBeat(courseId: string, lessonId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: ReadingBeatPayload) =>
      narrowLessonPath(
        await unwrap(
          api.POST('/api/v1/lessons/{lessonId}/reading', {
            params: { path: { lessonId } },
            body: payload,
          }),
        ),
      ),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: lessonKeys.path(lessonId) }),
        queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) }),
      ])
    },
  })
}

type ProgressStatus = 'IN_PROGRESS' | 'COMPLETED'

/** Records that the lesson was opened or finished; the plan and the work list follow. */
export function useRecordLessonProgress(courseId: string, lessonId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (status: ProgressStatus) =>
      unwrap(
        api.POST('/api/v1/lessons/{lessonId}/progress', { params: { path: { lessonId } }, body: { status } }),
      ),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) }),
        queryClient.invalidateQueries({ queryKey: courseKeys.progress(courseId) }),
      ])
    },
  })
}

/** A file of a lesson. The download needs the token, so it is fetched as a blob (API guide §4). */
export function useFileBlobQuery(fileId: string, enabled = true) {
  return useQuery({
    queryKey: ['file', fileId],
    queryFn: async () => {
      const [detail, blob] = await Promise.all([
        unwrap(api.GET('/api/v1/files/{id}', { params: { path: { id: fileId } } })),
        unwrap(api.GET('/api/v1/files/{id}/content', { params: { path: { id: fileId } }, parseAs: 'blob' })),
      ])
      return { blob, name: detail.originalFilename ?? fileId }
    },
    enabled,
    staleTime: Infinity,
  })
}

/**
 * Re-polls the lesson path and course plan after work (quiz submit or coding task verdict).
 * Bounded retry up to 5 refetches, 2 seconds apart, stopping early as soon as the response differs.
 */
export function useRefreshPathAfterWork(courseId?: string, lessonId?: string) {
  const queryClient = useQueryClient()
  return useCallback(
    async (overrideLessonId?: string) => {
      const activeLessonId = overrideLessonId ?? lessonId

      const baselinePlan =
        courseId !== undefined && courseId.length > 0
          ? queryClient.getQueriesData<CoursePlan>({ queryKey: courseKeys.planOfCourse(courseId) })[0]?.[1]
          : undefined
      const baselinePath =
        activeLessonId !== undefined && activeLessonId.length > 0
          ? queryClient.getQueryData<LessonPath>(lessonKeys.path(activeLessonId))
          : undefined

      await Promise.all([
        ...(activeLessonId !== undefined && activeLessonId.length > 0
          ? [queryClient.invalidateQueries({ queryKey: lessonKeys.path(activeLessonId) })]
          : []),
        ...(courseId !== undefined && courseId.length > 0
          ? [queryClient.invalidateQueries({ queryKey: courseKeys.planOfCourse(courseId) })]
          : []),
      ])

      const immediatePlan =
        courseId !== undefined && courseId.length > 0
          ? queryClient.getQueriesData<CoursePlan>({ queryKey: courseKeys.planOfCourse(courseId) })[0]?.[1]
          : undefined
      const immediatePath =
        activeLessonId !== undefined && activeLessonId.length > 0
          ? queryClient.getQueryData<LessonPath>(lessonKeys.path(activeLessonId))
          : undefined

      const immediateChanged =
        (activeLessonId !== undefined && activeLessonId.length > 0
          ? hasLessonPathChanged(baselinePath, immediatePath)
          : false) ||
        (courseId !== undefined && courseId.length > 0
          ? hasCoursePlanChanged(baselinePlan, immediatePlan)
          : false)

      if (immediateChanged) return

      for (let attempt = 1; attempt <= 5; attempt++) {
        await new Promise((resolve) => setTimeout(resolve, 2000))
        await Promise.all([
          ...(activeLessonId !== undefined && activeLessonId.length > 0
            ? [queryClient.refetchQueries({ queryKey: lessonKeys.path(activeLessonId) })]
            : []),
          ...(courseId !== undefined && courseId.length > 0
            ? [queryClient.refetchQueries({ queryKey: courseKeys.planOfCourse(courseId) })]
            : []),
        ])

        const currentPlan =
          courseId !== undefined && courseId.length > 0
            ? queryClient.getQueriesData<CoursePlan>({ queryKey: courseKeys.planOfCourse(courseId) })[0]?.[1]
            : undefined
        const currentPath =
          activeLessonId !== undefined && activeLessonId.length > 0
            ? queryClient.getQueryData<LessonPath>(lessonKeys.path(activeLessonId))
            : undefined

        const hasChanged =
          (activeLessonId !== undefined && activeLessonId.length > 0
            ? hasLessonPathChanged(baselinePath, currentPath)
            : false) ||
          (courseId !== undefined && courseId.length > 0
            ? hasCoursePlanChanged(baselinePlan, currentPlan)
            : false)

        const decision = evaluatePollingRetry(attempt, 5, hasChanged)
        if (decision.shouldStop) {
          break
        }
      }
    },
    [queryClient, courseId, lessonId],
  )
}
