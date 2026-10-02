import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { components } from '@/shared/api/schema'

type NotificationView = components['schemas']['NotificationView']
export type NotificationType = NonNullable<NotificationView['type']>

/** One notification, narrowed at the edge: what the list needs, guaranteed. */
export type NotificationItem = {
  id: string
  type: NotificationType
  createdAt: string
  read: boolean
}

function narrow(raw: NotificationView): NotificationItem | null {
  if (raw.id === undefined || raw.type === undefined || raw.createdAt === undefined) return null
  return { id: raw.id, type: raw.type, createdAt: raw.createdAt, read: raw.readAt !== undefined }
}

const keys = {
  unreadCount: ['notifications', 'unread-count'] as const,
  recent: ['notifications', 'recent'] as const,
}

const UNREAD_POLL_MS = 60_000

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

export function useRecentNotificationsQuery(enabled: boolean) {
  return useQuery({
    queryKey: keys.recent,
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/notifications/me', { params: { query: { page: 0, size: 6 } } }),
      )
      return (page.content ?? []).map(narrow).filter((item) => item !== null)
    },
    enabled,
  })
}

export function useMarkAllReadMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => unwrap(api.POST('/api/v1/notifications/me/read-all')),
    onSuccess: async () => {
      queryClient.setQueryData(keys.unreadCount, 0)
      await queryClient.invalidateQueries({ queryKey: keys.recent })
    },
  })
}
