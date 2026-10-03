import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'

type Schemas = components['schemas']
export type Timetable = Schemas['TimetableView']
export type Entry = Schemas['EntryView']
export type EntryRequest = Schemas['EntryRequest']
export type CheckResult = Schemas['CheckResult']
export type LoadRow = Schemas['LoadRow']
export type TimetableException = Schemas['ExceptionView']
export type ExceptionRequest = Schemas['ExceptionRequest']

export type Scope = { kind: 'group' | 'teacher' | 'room'; id: string }

export const builderKeys = {
  timetables: ['admin', 'timetables'] as const,
  entries: (timetableId: string) => ['admin', 'timetable-entries', timetableId] as const,
  load: (timetableId: string) => ['admin', 'timetable-load', timetableId] as const,
  exceptions: (entryId: string) => ['admin', 'timetable-exceptions', entryId] as const,
}

function scopeQuery(scope: Scope | undefined) {
  if (scope === undefined) return {}
  if (scope.kind === 'group') return { groupId: scope.id }
  if (scope.kind === 'teacher') return { teacherId: scope.id }
  return { roomId: scope.id }
}

export function useTimetableListQuery() {
  return useQuery({
    queryKey: builderKeys.timetables,
    queryFn: () => unwrap(api.GET('/api/v1/admin/timetables')),
  })
}

/** The classes of a timetable for one group, teacher or room. */
export function useEntriesQuery(timetableId: string | undefined, scope: Scope | undefined, locale: Locale) {
  return useQuery({
    queryKey: [...builderKeys.entries(timetableId ?? ''), scope, locale],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/timetables/{id}/entries', {
          params: { path: { id: timetableId ?? '' }, query: { ...scopeQuery(scope), locale } },
        }),
      ),
    enabled: timetableId !== undefined && scope !== undefined,
  })
}

export function useLoadPanelQuery(timetableId: string | undefined, scope: Scope | undefined, locale: Locale) {
  return useQuery({
    queryKey: [...builderKeys.load(timetableId ?? ''), scope, locale],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/timetables/{id}/load', {
          params: {
            path: { id: timetableId ?? '' },
            query: {
              ...(scope?.kind === 'group' && { groupId: scope.id }),
              ...(scope?.kind === 'teacher' && { teacherId: scope.id }),
              locale,
            },
          },
        }),
      ),
    enabled: timetableId !== undefined && scope !== undefined && scope.kind !== 'room',
  })
}

function useTimetableMutation<V, R>(mutationFn: (variables: V) => Promise<R>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSettled: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'timetables'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'timetable-entries'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'timetable-load'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'teaching-load'] })
      await queryClient.invalidateQueries({ queryKey: ['timetable'] })
    },
  })
}

export function useCreateTimetable() {
  return useTimetableMutation((input: Schemas['CreateTimetableRequest']) =>
    unwrap(api.POST('/api/v1/admin/timetables', { body: input })),
  )
}

export function useRenameTimetable(id: string) {
  return useTimetableMutation((name: string) =>
    unwrap(api.PUT('/api/v1/admin/timetables/{id}/name', { params: { path: { id } }, body: { name } })),
  )
}

export function usePublishTimetable(id: string) {
  return useTimetableMutation(() =>
    unwrap(api.POST('/api/v1/admin/timetables/{id}/publish', { params: { path: { id } } })),
  )
}

export function useArchiveTimetable(id: string) {
  return useTimetableMutation(() =>
    unwrap(api.POST('/api/v1/admin/timetables/{id}/archive', { params: { path: { id } } })),
  )
}

export function useDeleteTimetable(id: string) {
  return useTimetableMutation(() =>
    unwrap(api.DELETE('/api/v1/admin/timetables/{id}', { params: { path: { id } } })),
  )
}

/** Saves a class: a new one into the timetable, or an existing one by its id. */
export function useSaveEntry(timetableId: string, locale: Locale) {
  return useTimetableMutation(({ id, request }: { id?: string; request: EntryRequest }) =>
    id === undefined
      ? unwrap(
          api.POST('/api/v1/admin/timetables/{id}/entries', {
            params: { path: { id: timetableId }, query: { locale } },
            body: request,
          }),
        )
      : unwrap(
          api.PUT('/api/v1/admin/timetable-entries/{entryId}', {
            params: { path: { entryId: id }, query: { locale } },
            body: request,
          }),
        ),
  )
}

export function useDeleteEntry() {
  return useTimetableMutation((entryId: string) =>
    unwrap(api.DELETE('/api/v1/admin/timetable-entries/{entryId}', { params: { path: { entryId } } })),
  )
}

/** What saving would run into, without saving: the drag preview and the form's live check. */
export function checkEntry(
  timetableId: string,
  request: EntryRequest,
  editedEntryId: string | undefined,
  locale: Locale,
) {
  return unwrap(
    api.POST('/api/v1/admin/timetables/{id}/entries/check', {
      params: {
        path: { id: timetableId },
        query: { ...(editedEntryId !== undefined && { editedEntryId }), locale },
      },
      body: request,
    }),
  )
}

export function useExceptionsQuery(entryId: string | undefined) {
  return useQuery({
    queryKey: builderKeys.exceptions(entryId ?? ''),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/timetable-entries/{entryId}/exceptions', {
          params: { path: { entryId: entryId ?? '' } },
        }),
      ),
    enabled: entryId !== undefined,
  })
}

export function useAddException(entryId: string, locale: Locale) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ExceptionRequest) =>
      unwrap(
        api.POST('/api/v1/admin/timetable-entries/{entryId}/exceptions', {
          params: { path: { entryId }, query: { locale } },
          body: request,
        }),
      ),
    onSettled: async () => {
      await queryClient.invalidateQueries({ queryKey: builderKeys.exceptions(entryId) })
      await queryClient.invalidateQueries({ queryKey: ['timetable'] })
    },
  })
}

export function useDeleteException(entryId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (exceptionId: string) =>
      unwrap(
        api.DELETE('/api/v1/admin/timetable-exceptions/{exceptionId}', { params: { path: { exceptionId } } }),
      ),
    onSettled: () => queryClient.invalidateQueries({ queryKey: builderKeys.exceptions(entryId) }),
  })
}

export function useWeekPreviewQuery(
  timetableId: string | undefined,
  scope: Scope | undefined,
  date: string,
  locale: Locale,
  enabled: boolean,
) {
  return useQuery({
    queryKey: ['admin', 'week-preview', timetableId, scope, date, locale],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/timetables/{id}/week', {
          params: { path: { id: timetableId ?? '' }, query: { ...scopeQuery(scope), date, locale } },
        }),
      ),
    enabled: enabled && timetableId !== undefined && scope !== undefined,
  })
}
