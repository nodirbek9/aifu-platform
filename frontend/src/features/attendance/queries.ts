import { useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import { narrowCourseAttendance } from './model'

/** My attendance, one block per course. */
export function useMyAttendanceQuery(locale: Locale) {
  return useQuery({
    queryKey: ['attendance', 'me', locale],
    queryFn: async () => {
      const list = await unwrap(api.GET('/api/v1/attendance/me', { params: { query: { locale } } }))
      return list.map(narrowCourseAttendance).filter((course) => course !== null)
    },
  })
}
