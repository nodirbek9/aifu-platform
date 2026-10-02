import { useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'

type Schemas = components['schemas']

export type Announcement = {
  id: string
  title: string
  /** Sanitized HTML. */
  body: string
  author?: string
  createdAt: string
  /** Set for an announcement of a course; a group's announcement has `groupCode` instead. */
  courseId?: string
  courseTitle?: string
  groupCode?: string
}

function narrow(raw: Schemas['AnnouncementView']): Announcement | null {
  if (raw.id === undefined || raw.title === undefined || raw.createdAt === undefined) return null
  const item: Announcement = { id: raw.id, title: raw.title, body: raw.body ?? '', createdAt: raw.createdAt }
  if (raw.authorName !== undefined) item.author = raw.authorName
  if (raw.courseId !== undefined) item.courseId = raw.courseId
  if (raw.courseTitle !== undefined) item.courseTitle = raw.courseTitle
  if (raw.groupCode !== undefined) item.groupCode = raw.groupCode
  return item
}

const PAGE = { page: 0, size: 50 }

/** The announcements meant for me — of my courses and of my group — newest first. */
export function useMyAnnouncementsQuery(locale: Locale) {
  return useQuery({
    queryKey: ['announcements', 'me', locale],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/announcements/me', { params: { query: { locale, ...PAGE } } }),
      )
      return (page.content ?? []).map(narrow).filter((item) => item !== null)
    },
  })
}

/** The announcements of one course, newest first. */
export function useCourseAnnouncementsQuery(courseId: string, locale: Locale) {
  return useQuery({
    queryKey: ['announcements', 'course', courseId, locale],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/courses/{courseId}/announcements', {
          params: { path: { courseId }, query: { locale, ...PAGE } },
        }),
      )
      return (page.content ?? []).map(narrow).filter((item) => item !== null)
    },
  })
}
