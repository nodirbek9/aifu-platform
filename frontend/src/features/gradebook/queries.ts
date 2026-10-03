import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { narrowGradebook, type Gradebook } from '@/features/grades/model'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'

export type GradebookRow = {
  studentId: string
  fullName: string
  groupCode?: string
  book: Gradebook
}

const key = (courseId: string) => ['teacher', 'gradebook', courseId] as const

/** Every student of the course with every component: the teacher's gradebook (T-08). */
export function useCourseGradebookQuery(courseId: string, locale: Locale) {
  return useQuery({
    queryKey: [...key(courseId), locale],
    queryFn: async () => {
      const rows = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/gradebook', {
          params: { path: { courseId }, query: { locale } },
        }),
      )
      return rows.flatMap((row): GradebookRow[] => {
        if (row.studentUserId === undefined || row.grades === undefined) return []
        return [
          {
            studentId: row.studentUserId,
            fullName: row.studentFullName ?? '',
            ...(row.groupCode !== undefined && { groupCode: row.groupCode }),
            book: narrowGradebook(row.grades),
          },
        ]
      })
    },
  })
}

export type NewGrade = { enrollmentId: string; componentId: string; score: number; maxScore: number }

export function useRecordGrade(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (grade: NewGrade) =>
      unwrap(
        api.POST('/api/v1/teacher/enrollments/{id}/grades', {
          params: { path: { id: grade.enrollmentId } },
          body: { assessmentComponentId: grade.componentId, score: grade.score, maxScore: grade.maxScore },
        }),
      ),
    onSettled: () => queryClient.invalidateQueries({ queryKey: key(courseId) }),
  })
}

export function useVoidGrade(courseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ entryId, reason }: { entryId: string; reason: string }) =>
      unwrap(
        api.POST('/api/v1/teacher/grades/{id}/void', { params: { path: { id: entryId } }, body: { reason } }),
      ),
    onSettled: () => queryClient.invalidateQueries({ queryKey: key(courseId) }),
  })
}
