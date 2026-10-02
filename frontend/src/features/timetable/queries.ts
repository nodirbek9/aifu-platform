import { useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import { narrowDay } from './model'

export const timetableKeys = {
  today: ['timetable', 'today'] as const,
}

/** Today's classes of the signed-in student (by group), on the campus calendar. */
export function useTodayQuery() {
  return useQuery({
    queryKey: timetableKeys.today,
    queryFn: async () => narrowDay(await unwrap(api.GET('/api/v1/timetable/today'))),
    // a class may be cancelled or moved during the day
    refetchInterval: 5 * 60_000,
  })
}
