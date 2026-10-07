import { useState } from 'react'
import type { UseQueryResult } from '@tanstack/react-query'
import { Megaphone, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { RichText } from '@/shared/ui/rich-text'
import { Skeleton } from '@/shared/ui/skeleton'
import { ConfirmDialog } from '@/features/authoring/confirm-dialog'
import { useDeleteAnnouncement } from '@/features/teaching/queries'
import { Button } from '@/shared/ui/button'
import type { Announcement } from './queries'

type AnnouncementListProps = {
  announcements: UseQueryResult<Announcement[]>
  /** Say which course or group each one is for (a list that spans them). */
  withAudience?: boolean
  /** Show at most this many (the dashboard shows the newest two). */
  limit?: number
  /** Offer to take back what the reader may delete (the announcements page). */
  manage?: boolean
}

/** Announcements as short notices: title, who wrote it and when, the text. */
export function AnnouncementList({
  announcements,
  withAudience = false,
  limit,
  manage = false,
}: AnnouncementListProps) {
  const { t } = useTranslation()
  const remove = useDeleteAnnouncement()
  const [deleting, setDeleting] = useState<Announcement>()

  if (announcements.isPending) {
    return (
      <div className="grid gap-4">
        <Skeleton className="h-24" />
        <Skeleton className="h-24" />
      </div>
    )
  }
  if (announcements.isError) {
    return (
      <ErrorState
        problem={toApiProblem(announcements.error)}
        onRetry={() => {
          void announcements.refetch()
        }}
      />
    )
  }
  if (announcements.data.length === 0) {
    return (
      <EmptyState
        icon={<Megaphone aria-hidden className="size-7" />}
        title={t('announcements.emptyTitle')}
        body={t('announcements.emptyBody')}
      />
    )
  }
  const shown = limit === undefined ? announcements.data : announcements.data.slice(0, limit)
  return (
    <>
      <ul className="grid gap-4">
        {shown.map((item) => (
          <li key={item.id}>
            <AnnouncementCard
              item={item}
              withAudience={withAudience}
              {...(manage && item.mine && { onDelete: () => setDeleting(item) })}
            />
          </li>
        ))}
      </ul>
      <ConfirmDialog
        open={deleting !== undefined}
        title={t('teaching.deleteAnnouncementTitle')}
        body={t('teaching.deleteAnnouncementBody')}
        confirmLabel={t('teaching.delete')}
        pending={remove.isPending}
        onOpenChange={(next) => {
          if (!next) setDeleting(undefined)
        }}
        onConfirm={() => {
          if (deleting === undefined) return
          remove.mutate(deleting.id, {
            onSettled: () => {
              setDeleting(undefined)
            },
          })
        }}
      />
    </>
  )
}

function AnnouncementCard({
  item,
  withAudience,
  onDelete,
}: {
  item: Announcement
  withAudience: boolean
  onDelete?: () => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const wide = item.audience === 'EVERYONE' || item.audience === 'STUDENTS' || item.audience === 'TEACHERS'
  const audience = wide
    ? t(`announcements.audience.${item.audience ?? 'EVERYONE'}`)
    : (item.courseTitle ?? item.groupCode)
  return (
    <article className="border-border bg-surface-raised grid gap-2 rounded-lg border px-5 py-4">
      <header className="grid gap-1">
        <div className="flex flex-wrap items-start justify-between gap-x-4 gap-y-1">
          <h3 className="font-display text-body text-text font-semibold">{item.title}</h3>
          <span className="flex items-center gap-2">
            {(withAudience || wide) && audience !== undefined && (
              <Badge tone={wide ? 'accent' : 'neutral'}>{audience}</Badge>
            )}
            {onDelete !== undefined && (
              <Button variant="ghost" size="sm" onClick={onDelete}>
                <Trash2 aria-hidden className="size-4" />
                {t('teaching.delete')}
              </Button>
            )}
          </span>
        </div>
        <p className="text-caption text-text-muted tracking-normal">
          {item.author !== undefined && `${item.author} · `}
          {formatRelativeTime(item.createdAt, locale)}
        </p>
      </header>
      <RichText html={item.body} className="text-small" />
    </article>
  )
}
