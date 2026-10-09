import { keepPreviousData, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'
import { builderKeys } from '../builder/queries'
import { structureKeys } from './structure-queries'
import { PAGE_SIZE, userKeys, type UserFilter } from './users-queries'

type Schemas = components['schemas']
type Course = Schemas['CourseDetail']
type TeacherCoursesInput = { teacherId: string; termId: string; locale: Locale }

export const teacherWorkKeys = {
  courses: ({ teacherId, termId, locale }: TeacherCoursesInput) =>
    [...structureKeys.courses, 'teacher-work', teacherId, termId, locale] as const,
  week: (timetableId: string, teacherId: string, date: string, locale: Locale) =>
    ['admin', 'week-preview', timetableId, { kind: 'teacher', id: teacherId }, date, locale] as const,
}

export function useTeacherUsersQuery(filter: Omit<UserFilter, 'role'>, enabled = true) {
  return useQuery({
    queryKey: [...userKeys.list, { ...filter, role: 'TEACHER' }],
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/v1/admin/users', {
          signal,
          params: { query: { ...filter, role: 'TEACHER', size: PAGE_SIZE, sort: ['fullName,asc'] } },
        }),
      ),
    placeholderData: keepPreviousData,
    enabled,
  })
}

/** Inspect every catalogue page, including courses with no timetable entries. */
export async function readTeacherCourses(
  client: QueryClient,
  input: TeacherCoursesInput,
  signal: AbortSignal,
): Promise<Course[]> {
  const controller = new AbortController()
  const workSignal = AbortSignal.any([signal, controller.signal])
  const assigned: Course[] = []
  const inspected = new Set<string>()
  try {
    let page = 0
    let hasNext = true
    while (hasNext) {
      workSignal.throwIfAborted()
      const catalogue = await unwrap(
        api.GET('/api/v1/admin/courses', {
          signal: workSignal,
          params: { query: { page, size: 100, termId: input.termId, locale: input.locale } },
        }),
      )
      const ids = (catalogue.content ?? []).flatMap((course) => {
        if (course.id === undefined || inspected.has(course.id)) return []
        inspected.add(course.id)
        return [course.id]
      })
      const details: Course[] = []
      let next = 0
      await Promise.all(
        Array.from({ length: Math.min(4, ids.length) }, async () => {
          while (next < ids.length) {
            workSignal.throwIfAborted()
            const index = next++
            const id = ids[index]
            if (id === undefined) continue
            details[index] = await client.query({
              queryKey: structureKeys.course(id),
              queryFn: ({ signal: detailSignal }) =>
                unwrap(
                  api.GET('/api/v1/admin/courses/{id}', {
                    signal: AbortSignal.any([workSignal, detailSignal]),
                    params: { path: { id } },
                  }),
                ),
              staleTime: 60_000,
            })
          }
        }),
      )
      assigned.push(...details.filter((course) => course.teachers?.some((t) => t.userId === input.teacherId)))
      hasNext = catalogue.hasNext ?? page + 1 < (catalogue.totalPages ?? 0)
      page++
    }
    return assigned
  } catch (error) {
    controller.abort()
    throw error
  }
}

export function useTeacherCoursesQuery(teacherId: string, termId: string | undefined, locale: Locale) {
  const client = useQueryClient()
  const input = { teacherId, termId: termId ?? '', locale }
  return useQuery({
    queryKey: teacherWorkKeys.courses(input),
    queryFn: ({ signal }) => readTeacherCourses(client, input, signal),
    enabled: teacherId !== '' && termId !== undefined && termId !== '',
  })
}

export function useTeacherTimetablesQuery() {
  return useQuery({
    queryKey: builderKeys.timetables,
    queryFn: ({ signal }) => unwrap(api.GET('/api/v1/admin/timetables', { signal })),
  })
}

export function useTeacherTermLoadQuery(timetableId: string | undefined, teacherId: string, locale: Locale) {
  return useQuery({
    queryKey: [...builderKeys.load(timetableId ?? ''), { kind: 'teacher', id: teacherId }, locale],
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/v1/admin/timetables/{id}/load', {
          signal,
          params: { path: { id: timetableId ?? '' }, query: { teacherId, locale } },
        }),
      ),
    enabled: timetableId !== undefined && timetableId !== '' && teacherId !== '',
  })
}

/** An occurrence is the server's entry/date pair, never a group or a name. */
export function teacherWeeklyHours(week: Schemas['WeekView'], teacherId: string): number {
  const occurrences = new Set<string>()
  for (const day of week.days ?? []) {
    for (const lesson of day.classes ?? []) {
      if (
        lesson.teacherUserId !== teacherId ||
        (lesson.status !== 'SCHEDULED' && lesson.status !== 'MOVED_IN' && lesson.status !== 'REPLACED') ||
        lesson.entryId === undefined ||
        lesson.date === undefined
      )
        continue
      occurrences.add(JSON.stringify([lesson.entryId, lesson.date]))
    }
  }
  return occurrences.size * 2
}

export function useTeacherWeekQuery(
  timetableId: string | undefined,
  teacherId: string,
  date: string,
  locale: Locale,
) {
  return useQuery({
    queryKey: teacherWorkKeys.week(timetableId ?? '', teacherId, date, locale),
    queryFn: ({ signal }) =>
      unwrap(
        api.GET('/api/v1/admin/timetables/{id}/week', {
          signal,
          params: { path: { id: timetableId ?? '' }, query: { teacherId, date, locale } },
        }),
      ),
    enabled: timetableId !== undefined && timetableId !== '' && teacherId !== '' && date !== '',
  })
}
