import { queryOptions, useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import { narrowGradebook } from './model'

function gradebookQuery(enrollmentId: string, locale: Locale) {
  return queryOptions({
    queryKey: ['gradebook', enrollmentId, locale],
    queryFn: async () =>
      narrowGradebook(
        await unwrap(
          api.GET('/api/v1/enrollments/{id}/grades', {
            params: { path: { id: enrollmentId }, query: { locale } },
          }),
        ),
      ),
  })
}

/** My grades in one course: the scheme with earned points, the entries, the history. */
export function useGradebookQuery(enrollmentId: string | undefined, locale: Locale) {
  return useQuery({ ...gradebookQuery(enrollmentId ?? '', locale), enabled: enrollmentId !== undefined })
}
