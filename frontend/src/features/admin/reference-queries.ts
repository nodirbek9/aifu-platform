import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'

type Schemas = components['schemas']
export type Group = Schemas['GroupView']
export type GroupInput = Schemas['GroupRequest']
export type GroupStudent = Schemas['GroupStudentView']
export type Room = Schemas['RoomView']
export type RoomInput = Schemas['RoomRequest']
export type RoomType = RoomInput['roomType']
export type PairSlot = Schemas['PairSlotView']
export type PairSlotInput = Schemas['PairSlotRequest']
export type Holiday = Schemas['HolidayView']
export type AcademicYear = Schemas['AcademicYearView']
export type Term = Schemas['TermView']
export type Timetable = Schemas['TimetableView']

export const referenceKeys = {
  // the teacher's views cache their own shape under ['groups']: the admin's raw lists live apart
  groups: ['admin', 'groups'] as const,
  groupStudents: (id: string) => ['admin', 'groups', id, 'students'] as const,
  rooms: ['admin', 'rooms'] as const,
  slots: ['admin', 'pair-slots'] as const,
  holidays: ['admin', 'holidays'] as const,
  years: ['admin', 'academic-years'] as const,
  terms: (yearId: string) => ['admin', 'academic-years', yearId, 'terms'] as const,
  timetables: ['admin', 'timetables'] as const,
  programmes: ['reference', 'programmes'] as const,
  departments: ['reference', 'departments'] as const,
}

/** A mutation that refreshes the given lists when it settles. */
export function useRefreshingMutation<V, R>(
  keys: readonly (readonly unknown[])[],
  mutationFn: (variables: V) => Promise<R>,
) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSettled: async () => {
      await Promise.all(keys.map((key) => queryClient.invalidateQueries({ queryKey: key })))
    },
  })
}

// ------------------------------------------------------------------ groups

export function useAllGroupsQuery() {
  return useQuery({ queryKey: referenceKeys.groups, queryFn: () => unwrap(api.GET('/api/v1/groups')) })
}

export function useGroupStudentsListQuery(groupId: string | undefined) {
  return useQuery({
    queryKey: referenceKeys.groupStudents(groupId ?? ''),
    queryFn: () =>
      unwrap(api.GET('/api/v1/groups/{id}/students', { params: { path: { id: groupId ?? '' } } })),
    enabled: groupId !== undefined,
  })
}

export function useSaveGroup() {
  return useRefreshingMutation(
    [referenceKeys.groups, ['groups']],
    ({ id, input }: { id?: string; input: GroupInput }) =>
      id === undefined
        ? unwrap(api.POST('/api/v1/admin/groups', { body: input }))
        : unwrap(api.PUT('/api/v1/admin/groups/{id}', { params: { path: { id } }, body: input })),
  )
}

export function useArchiveGroup() {
  return useRefreshingMutation(
    [referenceKeys.groups, ['groups']],
    ({ id, archived }: { id: string; archived: boolean }) =>
      unwrap(
        api.PUT('/api/v1/admin/groups/{id}/archived', {
          params: { path: { id }, query: { value: archived } },
        }),
      ),
  )
}

export function useDeleteGroup() {
  return useRefreshingMutation([referenceKeys.groups, ['groups']], (id: string) =>
    unwrap(api.DELETE('/api/v1/admin/groups/{id}', { params: { path: { id } } })),
  )
}

export function useAddGroupStudents(groupId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (userIds: string[]) =>
      unwrap(
        api.POST('/api/v1/admin/groups/{id}/students', {
          params: { path: { id: groupId } },
          body: { userIds },
        }),
      ),
    onSettled: async (_data, _error, userIds) => {
      // Group prefixes include both old and new rosters, even when the old group is unknown.
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: referenceKeys.groups }),
        queryClient.invalidateQueries({ queryKey: ['groups'] }),
        ...userIds.map((userId) => queryClient.invalidateQueries({ queryKey: ['admin', 'users', userId] })),
      ])
    },
  })
}

export function useRemoveGroupStudent(groupId: string) {
  return useRefreshingMutation([referenceKeys.groups, ['groups']], (userId: string) =>
    unwrap(
      api.DELETE('/api/v1/admin/groups/{id}/students/{userId}', {
        params: { path: { id: groupId, userId } },
      }),
    ),
  )
}

// ------------------------------------------------------------------ rooms, pairs, holidays

export function useRoomsQuery() {
  return useQuery({ queryKey: referenceKeys.rooms, queryFn: () => unwrap(api.GET('/api/v1/admin/rooms')) })
}

export function useSaveRoom() {
  return useRefreshingMutation([referenceKeys.rooms], ({ id, input }: { id?: string; input: RoomInput }) =>
    id === undefined
      ? unwrap(api.POST('/api/v1/admin/rooms', { body: input }))
      : unwrap(api.PUT('/api/v1/admin/rooms/{id}', { params: { path: { id } }, body: input })),
  )
}

export function useDeleteRoom() {
  return useRefreshingMutation([referenceKeys.rooms], (id: string) =>
    unwrap(api.DELETE('/api/v1/admin/rooms/{id}', { params: { path: { id } } })),
  )
}

export function usePairSlotsQuery() {
  return useQuery({
    queryKey: referenceKeys.slots,
    queryFn: () => unwrap(api.GET('/api/v1/admin/pair-slots')),
  })
}

export function useSavePairSlot() {
  return useRefreshingMutation(
    // every timetable names its pairs from the bell schedule
    [referenceKeys.slots, ['timetable']],
    ({ id, input }: { id?: string; input: PairSlotInput }) =>
      id === undefined
        ? unwrap(api.POST('/api/v1/admin/pair-slots', { body: input }))
        : unwrap(api.PUT('/api/v1/admin/pair-slots/{id}', { params: { path: { id } }, body: input })),
  )
}

export function useDeletePairSlot() {
  return useRefreshingMutation([referenceKeys.slots, ['timetable']], (id: string) =>
    unwrap(api.DELETE('/api/v1/admin/pair-slots/{id}', { params: { path: { id } } })),
  )
}

export function useHolidaysQuery() {
  return useQuery({
    queryKey: referenceKeys.holidays,
    queryFn: () => unwrap(api.GET('/api/v1/admin/holidays')),
  })
}

export function useAddHoliday() {
  return useRefreshingMutation([referenceKeys.holidays], (input: { date: string; name: string }) =>
    unwrap(api.POST('/api/v1/admin/holidays', { body: input })),
  )
}

export function useDeleteHoliday() {
  return useRefreshingMutation([referenceKeys.holidays], (id: string) =>
    unwrap(api.DELETE('/api/v1/admin/holidays/{id}', { params: { path: { id } } })),
  )
}

// ------------------------------------------------------------------ years, terms, parity

export function useAcademicYearsQuery() {
  return useQuery({
    queryKey: referenceKeys.years,
    queryFn: () => unwrap(api.GET('/api/v1/admin/academic-years')),
  })
}

export function useTermsQuery(yearId: string | undefined) {
  return useQuery({
    queryKey: referenceKeys.terms(yearId ?? ''),
    queryFn: () =>
      unwrap(api.GET('/api/v1/admin/academic-years/{id}/terms', { params: { path: { id: yearId ?? '' } } })),
    enabled: yearId !== undefined,
  })
}

export function useCreateAcademicYear() {
  return useRefreshingMutation([referenceKeys.years], (input: Schemas['CreateAcademicYearRequest']) =>
    unwrap(api.POST('/api/v1/admin/academic-years', { body: input })),
  )
}

export function useMakeYearCurrent() {
  return useRefreshingMutation([referenceKeys.years], (id: string) =>
    unwrap(api.PUT('/api/v1/admin/academic-years/{id}/current', { params: { path: { id } } })),
  )
}

export function useCreateTerm(yearId: string) {
  return useRefreshingMutation([referenceKeys.terms(yearId)], (input: Schemas['CreateTermRequest']) =>
    unwrap(
      api.POST('/api/v1/admin/academic-years/{id}/terms', { params: { path: { id: yearId } }, body: input }),
    ),
  )
}

export function useTimetablesQuery() {
  return useQuery({
    queryKey: referenceKeys.timetables,
    queryFn: () => unwrap(api.GET('/api/v1/admin/timetables')),
  })
}

export function useSetParity() {
  return useRefreshingMutation(
    [referenceKeys.timetables],
    ({ termId, parity }: { termId: string; parity: 'ODD' | 'EVEN' }) =>
      unwrap(
        api.PUT('/api/v1/admin/terms/{termId}/first-week-parity', {
          params: { path: { termId } },
          body: { firstWeekParity: parity },
        }),
      ),
  )
}

export function useProgrammesQuery() {
  return useQuery({
    queryKey: referenceKeys.programmes,
    queryFn: () => unwrap(api.GET('/api/v1/reference/programmes')),
    staleTime: Infinity,
  })
}

export function useDepartmentsQuery() {
  return useQuery({
    queryKey: referenceKeys.departments,
    queryFn: () => unwrap(api.GET('/api/v1/reference/departments')),
    staleTime: Infinity,
  })
}
