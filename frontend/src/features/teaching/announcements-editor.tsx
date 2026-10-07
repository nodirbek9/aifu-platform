import { useState } from 'react'
import { Send, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toHtml, useCourseAnnouncementsQuery } from '@/features/announcements/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { RichText } from '@/shared/ui/rich-text'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import { useDeleteAnnouncement, useGroupsQuery, usePostAnnouncement } from './queries'

/**
 * T-11 — announcements of a course: write one to the whole course or to one of its groups;
 * the students get a notification. The author can take one back.
 */
export function AnnouncementsEditor({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()
  const announcements = useCourseAnnouncementsQuery(courseId, locale)
  const groups = useGroupsQuery()
  const post = usePostAnnouncement()
  const remove = useDeleteAnnouncement()
  const [audience, setAudience] = useState<'course' | 'group'>('course')
  const [groupId, setGroupId] = useState<string>()
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [error, setError] = useState<string>()
  const [deleting, setDeleting] = useState<string>()

  const canPost =
    title.trim() !== '' && body.trim() !== '' && (audience === 'course' || groupId !== undefined)

  const submit = () => {
    setError(undefined)
    post.mutate(
      {
        ...(audience === 'course' ? { courseId } : { groupId }),
        title: title.trim(),
        body: toHtml(body),
      },
      {
        onSuccess: () => {
          setTitle('')
          setBody('')
          toast({ tone: 'success', title: t('teaching.announcementPosted') })
        },
        onError: (failure) => {
          setError(t(`errors.${toApiProblem(failure).code}`))
        },
      },
    )
  }

  return (
    <div className="grid items-start gap-6 lg:grid-cols-12">
      <Card className="lg:col-span-5">
        <CardHeader title={t('teaching.newAnnouncement')} description={t('teaching.announcementHint')} />
        <CardContent>
          <form
            className="grid gap-4"
            onSubmit={(event) => {
              event.preventDefault()
              if (canPost) submit()
            }}
          >
            <SegmentedControl
              aria-label={t('teaching.audience')}
              value={audience}
              onValueChange={(value) => {
                setAudience(value === 'group' ? 'group' : 'course')
              }}
              options={[
                { value: 'course', label: t('teaching.toCourse') },
                { value: 'group', label: t('teaching.toGroup') },
              ]}
            />
            {audience === 'group' && (
              <Select
                label={t('profile.group')}
                value={groupId}
                placeholder={t('teaching.chooseGroup')}
                onValueChange={setGroupId}
                options={(groups.data ?? []).map((group) => ({ value: group.id, label: group.code }))}
              />
            )}
            <Input
              label={t('teaching.announcementTitle')}
              value={title}
              maxLength={200}
              onChange={(event) => {
                setTitle(event.target.value)
              }}
            />
            <Textarea
              label={t('teaching.announcementBody')}
              rows={6}
              value={body}
              onChange={(event) => {
                setBody(event.target.value)
              }}
            />
            {error !== undefined && (
              <p
                role="alert"
                className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
              >
                {error}
              </p>
            )}
            <Button type="submit" className="justify-self-start" disabled={!canPost} loading={post.isPending}>
              <Send aria-hidden className="size-4" />
              {t('teaching.post')}
            </Button>
          </form>
        </CardContent>
      </Card>

      <section className="grid gap-4 lg:col-span-7">
        {announcements.isPending && <Skeleton className="h-40" />}
        {announcements.isError && (
          <ErrorState
            problem={toApiProblem(announcements.error)}
            onRetry={() => {
              void announcements.refetch()
            }}
          />
        )}
        {announcements.isSuccess && announcements.data.length === 0 && (
          <EmptyState title={t('announcements.emptyTitle')} body={t('teaching.announcementsEmptyBody')} />
        )}
        {announcements.data?.map((item) => (
          <article
            key={item.id}
            className="border-border bg-surface-raised grid gap-2 rounded-lg border px-5 py-4"
          >
            <header className="flex flex-wrap items-start justify-between gap-2">
              <div className="grid gap-0.5">
                <h3 className="font-display text-body text-text font-semibold">{item.title}</h3>
                <p className="text-caption text-text-muted tracking-normal">
                  {item.author !== undefined && `${item.author} · `}
                  {formatRelativeTime(item.createdAt, locale)}
                </p>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setDeleting(item.id)
                }}
              >
                <Trash2 aria-hidden className="size-4" />
                {t('teaching.delete')}
              </Button>
            </header>
            <RichText html={item.body} className="text-small" />
          </article>
        ))}
      </section>

      <Dialog
        open={deleting !== undefined}
        onOpenChange={(open) => {
          if (!open) setDeleting(undefined)
        }}
      >
        <DialogContent>
          <DialogTitle>{t('teaching.deleteAnnouncementTitle')}</DialogTitle>
          <DialogDescription>{t('teaching.deleteAnnouncementBody')}</DialogDescription>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">{t('task.resetCancel')}</Button>
            </DialogClose>
            <Button
              variant="danger"
              loading={remove.isPending}
              onClick={() => {
                if (deleting === undefined) return
                remove.mutate(deleting, {
                  onSuccess: () => {
                    setDeleting(undefined)
                  },
                  onError: (failure) => {
                    setDeleting(undefined)
                    toast({ tone: 'danger', title: t(`errors.${toApiProblem(failure).code}`) })
                  },
                })
              }}
            >
              {t('teaching.delete')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
