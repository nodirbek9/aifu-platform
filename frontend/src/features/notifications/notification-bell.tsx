import { useState } from 'react'
import { Bell } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { Button, button } from '@/shared/ui/button'
import { IconButton } from '@/shared/ui/icon-button'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'
import { Skeleton } from '@/shared/ui/skeleton'
import { NotificationRow } from './notification-row'
import { useMarkAllReadMutation, useRecentNotificationsQuery, useUnreadCountQuery } from './queries'

const MAX_BADGE = 99

/** The bell of the top bar: unread count (polled) and the newest notifications in a popover. */
export function NotificationBell() {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const unread = useUnreadCountQuery()
  const recent = useRecentNotificationsQuery(open)
  const markAllRead = useMarkAllReadMutation()

  const count = unread.data ?? 0
  const label = count > 0 ? t('notification.bellUnread', { count }) : t('common.notifications')

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <IconButton aria-label={label} className="relative">
          <Bell aria-hidden className="size-5" />
          {count > 0 && (
            <span
              aria-hidden
              className={cn(
                'absolute top-0.5 right-0.5 flex h-5 min-w-5 items-center justify-center rounded-full px-1',
                'bg-accent-strong text-caption text-accent-foreground leading-none font-semibold tracking-normal tabular-nums',
              )}
            >
              {count > MAX_BADGE ? `${MAX_BADGE}+` : count}
            </span>
          )}
        </IconButton>
      </PopoverTrigger>
      <PopoverContent className="w-[min(24rem,calc(100vw-2rem))] p-0">
        <div className="border-border flex items-center justify-between gap-3 border-b px-4 py-3">
          <h2 className="font-display text-body text-text font-semibold">{t('common.notifications')}</h2>
          {count > 0 && (
            <Button
              variant="link"
              size="sm"
              className="h-auto px-0"
              loading={markAllRead.isPending}
              onClick={() => {
                markAllRead.mutate()
              }}
            >
              {t('common.markAllRead')}
            </Button>
          )}
        </div>

        {recent.isPending && (
          <div className="grid gap-3 p-4">
            <Skeleton className="h-9" />
            <Skeleton className="h-9" />
            <Skeleton className="h-9" />
          </div>
        )}

        {recent.isError && (
          <div className="grid justify-items-start gap-2 p-4">
            <p className="text-small text-text-muted">{t(`errors.${toApiProblem(recent.error).code}`)}</p>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                void recent.refetch()
              }}
            >
              {t('common.retry')}
            </Button>
          </div>
        )}

        {recent.isSuccess && recent.data.length === 0 && (
          <p className="text-small text-text-muted px-4 py-8 text-center">{t('common.noNotifications')}</p>
        )}

        {recent.isSuccess && recent.data.length > 0 && (
          <ul className="max-h-96 overflow-y-auto py-1">
            {recent.data.map((item) => (
              <li key={item.id}>
                <NotificationRow
                  item={item}
                  dense
                  onOpened={() => {
                    setOpen(false)
                  }}
                />
              </li>
            ))}
          </ul>
        )}

        <div className="border-border border-t px-4 py-2">
          <Link
            to="/notifications"
            onClick={() => {
              setOpen(false)
            }}
            className={button({ variant: 'link', size: 'sm', class: 'h-auto px-0' })}
          >
            {t('notification.all')}
          </Link>
        </div>
      </PopoverContent>
    </Popover>
  )
}
