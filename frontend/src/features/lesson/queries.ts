import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { courseKeys } from '@/features/courses/queries'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import { narrowLesson, narrowLessonPath } from './model'

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
