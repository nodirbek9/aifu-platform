import { BellOff } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Pagination } from '@/shared/ui/pagination'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Skeleton } from '@/shared/ui/skeleton'
import { NotificationRow } from './notification-row'
import { useMarkAllReadMutation, useNotificationsPageQuery, useUnreadCountQuery } from './queries'

/** S-09 — every notification, newest first; unread ones marked, each leading to what it is about. */
export function NotificationsPage() {
  const { t } = useTranslation()
  const [search, setSearch] = useSearchParams()
  const unreadOnly = search.get('show') === 'unread'
  const requestedPage = Number(search.get('page') ?? '1')
  const page = Number.isInteger(requestedPage) && requestedPage > 0 ? requestedPage - 1 : 0
  const list = useNotificationsPageQuery(page, unreadOnly)
  const unread = useUnreadCountQuery()
  const markAllRead = useMarkAllReadMutation()

  const show = (next: { unreadOnly: boolean; page: number }) => {
    const params: Record<string, string> = {}
    if (next.unreadOnly) params.show = 'unread'
    if (next.page > 0) params.page = String(next.page + 1)
    setSearch(params)
  }

  return (
    <div className="max-w-lesson grid gap-6">
      <PageHeader
        title={t('common.notifications')}
        context={
          unread.data !== undefined && unread.data > 0
            ? t('notification.unreadCount', { count: unread.data })
            : undefined
        }
        action={
          (unread.data ?? 0) > 0 ? (
            <Button
              variant="outline"
              loading={markAllRead.isPending}
              onClick={() => {
                markAllRead.mutate()
              }}
            >
              {t('common.markAllRead')}
            </Button>
          ) : undefined
        }
      />

      <SegmentedControl
        aria-label={t('notification.show')}
        className="justify-self-start"
        value={unreadOnly ? 'unread' : 'all'}
        onValueChange={(value) => {
          show({ unreadOnly: value === 'unread', page: 0 })
        }}
        options={[
          { value: 'all', label: t('assignments.all') },
          { value: 'unread', label: t('notification.onlyUnread') },
        ]}
      />

      {list.isPending && (
        <div className="grid gap-3">
          <Skeleton className="h-14" />
          <Skeleton className="h-14" />
          <Skeleton className="h-14" />
        </div>
      )}
      {list.isError && (
        <ErrorState
          problem={toApiProblem(list.error)}
          onRetry={() => {
            void list.refetch()
          }}
        />
      )}
      {list.isSuccess && list.data.items.length === 0 && (
        <EmptyState
          icon={<BellOff aria-hidden className="size-7" />}
          title={unreadOnly ? t('notification.noneUnread') : t('common.noNotifications')}
          action={
            unreadOnly ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  show({ unreadOnly: false, page: 0 })
                }}
              >
                {t('notification.showAll')}
              </Button>
            ) : undefined
          }
        />
      )}
      {list.isSuccess && list.data.items.length > 0 && (
        <>
          <ul
            className={cn(
              'divide-border border-border divide-y border-y',
              list.isPlaceholderData && 'opacity-60',
            )}
          >
            {list.data.items.map((item) => (
              <li key={item.id}>
                <NotificationRow item={item} />
              </li>
            ))}
          </ul>
          <Pagination
            page={page}
            totalPages={list.data.totalPages}
            onPageChange={(next) => {
              show({ unreadOnly, page: next })
            }}
          />
        </>
      )}
    </div>
  )
}
