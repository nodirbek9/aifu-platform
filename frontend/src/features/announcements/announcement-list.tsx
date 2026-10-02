import type { UseQueryResult } from '@tanstack/react-query'
import { Megaphone } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { RichText } from '@/shared/ui/rich-text'
import { Skeleton } from '@/shared/ui/skeleton'
import type { Announcement } from './queries'

type AnnouncementListProps = {
  announcements: UseQueryResult<Announcement[]>
  /** Say which course or group each one is for (a list that spans them). */
  withAudience?: boolean
  /** Show at most this many (the dashboard shows the newest two). */
  limit?: number
}

/** Announcements as short notices: title, who wrote it and when, the text. */
export function AnnouncementList({ announcements, withAudience = false, limit }: AnnouncementListProps) {
  const { t } = useTranslation()

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
    <ul className="grid gap-4">
      {shown.map((item) => (
        <li key={item.id}>
          <AnnouncementCard item={item} withAudience={withAudience} />
        </li>
      ))}
    </ul>
  )
}

function AnnouncementCard({ item, withAudience }: { item: Announcement; withAudience: boolean }) {
  const locale = useLocale()
  const audience = item.courseTitle ?? item.groupCode
  return (
    <article className="border-border bg-surface-raised grid gap-2 rounded-lg border px-5 py-4">
      <header className="grid gap-1">
        <div className="flex flex-wrap items-start justify-between gap-x-4 gap-y-1">
          <h3 className="font-display text-body text-text font-semibold">{item.title}</h3>
          {withAudience && audience !== undefined && <Badge>{audience}</Badge>}
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
