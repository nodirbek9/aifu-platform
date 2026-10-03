import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'

export type AuditAction = NonNullable<components['schemas']['AuditLogView']['action']>

export function useAuditQuery(filter: {
  action?: AuditAction
  entityType?: string
  from?: string
  to?: string
  page: number
  size?: number
}) {
  return useQuery({
    queryKey: ['admin', 'audit', filter],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/audit-log', {
          params: {
            query: {
              ...(filter.action !== undefined && { action: filter.action }),
              ...(filter.entityType !== undefined &&
                filter.entityType !== '' && { entityType: filter.entityType }),
              ...(filter.from !== undefined && filter.from !== '' && { from: `${filter.from}T00:00:00Z` }),
              ...(filter.to !== undefined && filter.to !== '' && { to: `${filter.to}T23:59:59Z` }),
              page: filter.page,
              size: filter.size ?? 30,
              sort: ['occurredAt,desc'],
            },
          },
        }),
      ),
    placeholderData: keepPreviousData,
  })
}
