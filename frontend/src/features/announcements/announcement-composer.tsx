import { useState } from 'react'
import { Send } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useCoursesQuery } from '@/features/admin/structure-queries'
import { useAuth } from '@/features/auth/auth-context'
import { useGroupsQuery, usePostAnnouncement, useTeacherCoursesQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import { toHtml, type Audience } from './queries'

/** What each role may write to: the study office reaches everyone, a teacher their courses and groups. */
const AUDIENCES = {
  ADMIN: ['EVERYONE', 'STUDENTS', 'TEACHERS', 'GROUP', 'COURSE'],
  TEACHER: ['COURSE', 'GROUP'],
} as const satisfies Record<string, readonly Audience[]>

/**
 * T-11 — a new announcement from the announcements page: who it is for, a title, the text. Its
 * readers get a notification. A teacher writes to a course they teach or a group they curate (the
 * server checks); the study office also to everyone, to all students or to all teachers.
 */
export function AnnouncementComposer({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { activeRole } = useAuth()
  const admin = activeRole === 'ADMIN'
  const choices = AUDIENCES[admin ? 'ADMIN' : 'TEACHER']
  const [audience, setAudience] = useState<Audience>(choices[0])
  const [courseId, setCourseId] = useState<string>()
  const [groupId, setGroupId] = useState<string>()
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const [error, setError] = useState<string>()
  const post = usePostAnnouncement()
  const groups = useGroupsQuery()
  const myCourses = useTeacherCoursesQuery()
  const allCourses = useCoursesQuery({ query: '', status: 'PUBLISHED', page: 0, size: 100 })
  const courses = admin
    ? (allCourses.data?.content ?? []).map((course) => ({ id: course.id ?? '', title: course.title ?? '' }))
    : (myCourses.data ?? []).map((course) => ({ id: course.id, title: course.title }))

  const target =
    audience === 'COURSE' ? courseId !== undefined : audience === 'GROUP' ? groupId !== undefined : true
  const canPost = title.trim() !== '' && body.trim() !== '' && target

  const close = () => {
    setTitle('')
    setBody('')
    setError(undefined)
    onClose()
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) close()
      }}
    >
      <DialogContent className="max-w-xl">
        <DialogTitle>{t('teaching.newAnnouncement')}</DialogTitle>
        <DialogDescription>{t('announcements.composeBody')}</DialogDescription>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (!canPost) return
            setError(undefined)
            post.mutate(
              {
                audience,
                ...(audience === 'COURSE' && courseId !== undefined && { courseId }),
                ...(audience === 'GROUP' && groupId !== undefined && { groupId }),
                title: title.trim(),
                body: toHtml(body),
              },
              {
                onSuccess: () => {
                  toast({ tone: 'success', title: t('teaching.announcementPosted') })
                  close()
                },
                onError: (failure) => {
                  setError(t(`errors.${toApiProblem(failure).code}`))
                },
              },
            )
          }}
        >
          <Select
            label={t('teaching.audience')}
            value={audience}
            onValueChange={(value) => {
              const next = choices.find((item) => item === value)
              if (next !== undefined) setAudience(next)
            }}
            options={choices.map((item) => ({ value: item, label: t(`announcements.audience.${item}`) }))}
          />
          {audience === 'COURSE' && (
            <Select
              label={t('admin.course')}
              value={courseId}
              placeholder={t('authoring.choose')}
              onValueChange={setCourseId}
              options={courses.map((course) => ({ value: course.id, label: course.title }))}
            />
          )}
          {audience === 'GROUP' && (
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
          <DialogFooter>
            <Button type="submit" disabled={!canPost} loading={post.isPending}>
              <Send aria-hidden className="size-4" />
              {t('teaching.post')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
