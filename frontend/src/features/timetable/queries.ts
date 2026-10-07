import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { narrowClass, narrowDay, narrowWeek } from './model'

type ClassView = components['schemas']['ClassView']

/** Whose week "my week" is, for someone who both studies and teaches. */
export type TimetableSide = 'student' | 'teacher'

export const timetableKeys = {
  today: (side: TimetableSide) => ['timetable', 'today', side] as const,
  next: (side: TimetableSide) => ['timetable', 'next', side] as const,
  week: (side: TimetableSide, date: string | undefined, locale: Locale) =>
    ['timetable', 'week', side, date ?? 'current', locale] as const,
  topic: (entryId: string, date: string, locale: Locale) => ['class-topic', entryId, date, locale] as const,
}

/** Today's classes on the campus calendar: a student's by group, a teacher's own. */
export function useTodayQuery(side: TimetableSide = 'student') {
  return useQuery({
    queryKey: timetableKeys.today(side),
    queryFn: async () =>
      narrowDay(await unwrap(api.GET('/api/v1/timetable/today', { params: { query: { as: side } } }))),
    // a class may be cancelled or moved during the day
    refetchInterval: 5 * 60_000,
  })
}

/** My next class that will really be held, looking four weeks ahead; `null` when there is none. */
export function useNextClassQuery(side: TimetableSide = 'student') {
  const locale = useLocale()
  return useQuery({
    queryKey: [...timetableKeys.next(side), locale],
    queryFn: async () => {
      // 204, and so nothing to read, when no class is ahead
      const next: ClassView | undefined = await unwrap(
        api.GET('/api/v1/timetable/next', { params: { query: { as: side, locale } } }),
      )
      return next === undefined ? null : narrowClass(next)
    },
  })
}

/**
 * My week: the one `date` falls in, or the current one. The server has already applied the
 * odd / even week, holidays and one-off changes; nothing of that is computed here.
 */
export function useMyWeekQuery(side: TimetableSide, date: string | undefined, locale: Locale) {
  return useQuery({
    queryKey: timetableKeys.week(side, date, locale),
    queryFn: async () =>
      narrowWeek(
        await unwrap(
          api.GET('/api/v1/timetable/me', {
            params: { query: { as: side, locale, ...(date !== undefined && { date }) } },
          }),
        ),
      ),
    // switching the week keeps the grid on screen until the next one arrives
    placeholderData: keepPreviousData,
    refetchInterval: 5 * 60_000,
  })
}

export type ClassTopic = { lessonId?: string; lessonTitle?: string; topic?: string }

/** What a class is about: the lesson its teacher linked, when there is one. */
export function useClassTopicQuery(entryId: string | undefined, date: string | undefined, locale: Locale) {
  return useQuery({
    queryKey: timetableKeys.topic(entryId ?? '', date ?? '', locale),
    queryFn: async (): Promise<ClassTopic> => {
      const raw = await unwrap(
        api.GET('/api/v1/class-sessions/{entryId}/{date}/topic', {
          params: { path: { entryId: entryId ?? '', date: date ?? '' }, query: { locale } },
        }),
      )
      const topic: ClassTopic = {}
      if (raw.lessonId !== undefined) topic.lessonId = raw.lessonId
      if (raw.lessonTitle !== undefined) topic.lessonTitle = raw.lessonTitle
      if (raw.topic !== undefined && raw.topic !== '') topic.topic = raw.topic
      return topic
    },
    enabled: entryId !== undefined && date !== undefined,
  })
}
