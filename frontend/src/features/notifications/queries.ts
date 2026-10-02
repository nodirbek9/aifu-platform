import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import { narrowNotification } from './model'

const keys = {
  all: ['notifications'] as const,
  unreadCount: ['notifications', 'unread-count'] as const,
  recent: ['notifications', 'recent'] as const,
  page: (page: number, unreadOnly: boolean) => ['notifications', 'page', page, unreadOnly] as const,
}

const UNREAD_POLL_MS = 60_000
export const NOTIFICATIONS_PAGE_SIZE = 20

export function useUnreadCountQuery() {
  return useQuery({
    queryKey: keys.unreadCount,
    queryFn: async () => {
      const view = await unwrap(api.GET('/api/v1/notifications/me/unread-count'))
      return view.count ?? 0
    },
    refetchInterval: UNREAD_POLL_MS,
  })
}

/** The newest few, for the bell's popover. */
export function useRecentNotificationsQuery(enabled: boolean) {
  return useQuery({
    queryKey: keys.recent,
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/notifications/me', { params: { query: { page: 0, size: 6 } } }),
      )
      return (page.content ?? []).map(narrowNotification).filter((item) => item !== null)
    },
    enabled,
  })
}

/** One page of the whole list, newest first. */
export function useNotificationsPageQuery(page: number, unreadOnly: boolean) {
  return useQuery({
    queryKey: keys.page(page, unreadOnly),
    queryFn: async () => {
      const result = await unwrap(
        api.GET('/api/v1/notifications/me', {
          params: { query: { page, size: NOTIFICATIONS_PAGE_SIZE, unreadOnly } },
        }),
      )
      return {
        items: (result.content ?? []).map(narrowNotification).filter((item) => item !== null),
        totalPages: result.totalPages ?? 1,
        total: result.totalElements ?? 0,
      }
    },
    placeholderData: keepPreviousData,
  })
}

/** Marks one as read; every list and the badge follow. */
export function useMarkReadMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      unwrap(api.POST('/api/v1/notifications/{id}/read', { params: { path: { id } } })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: keys.all }),
  })
}

export function useMarkAllReadMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => unwrap(api.POST('/api/v1/notifications/me/read-all')),
    onSuccess: async () => {
      queryClient.setQueryData(keys.unreadCount, 0)
      await queryClient.invalidateQueries({ queryKey: keys.all })
    },
  })
}
