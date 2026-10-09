import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { ChangeEnrollmentStatusInput, StudentEnrollmentFilter } from './student-enrollment-model'
import { structureKeys } from './structure-queries'

export const studentEnrollmentKeys = {
  user: (userId: string) => ['admin', 'student-enrollments', userId] as const,
  page: (userId: string, filter: StudentEnrollmentFilter) =>
    [...studentEnrollmentKeys.user(userId), filter] as const,
}

export function useStudentEnrollmentsQuery(userId: string, filter: StudentEnrollmentFilter = {}) {
  const query = { ...filter, page: filter.page ?? 0, size: filter.size ?? 25 }
  return useQuery({
    queryKey: studentEnrollmentKeys.page(userId, query),
    queryFn: () => unwrap(api.GET('/api/v1/admin/enrollments', { params: { query: { ...query, userId } } })),
    placeholderData: (previousData, previousQuery) =>
      previousQuery?.queryKey[2] === userId ? previousData : undefined,
    enabled: userId !== '',
  })
}

export function useChangeEnrollmentStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status, reason }: ChangeEnrollmentStatusInput) =>
      unwrap(
        api.PUT('/api/v1/admin/enrollments/{id}/status', {
          params: {
            path: { id },
            query: { status, ...(reason !== undefined && reason !== '' && { reason }) },
          },
        }),
      ),
    onSettled: async (_data, _error, { studentUserId, courseId }) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: studentEnrollmentKeys.user(studentUserId) }),
        queryClient.invalidateQueries({ queryKey: structureKeys.enrollments(courseId) }),
        queryClient.invalidateQueries({ queryKey: ['admin', 'problems'] }),
      ])
    },
  })
}
