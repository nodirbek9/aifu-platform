import { useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'

type Schemas = components['schemas']

export type Audience = NonNullable<Schemas['AnnouncementView']['audience']>

/** Paragraphs typed as plain text become the HTML the server stores (and sanitizes). */
export function toHtml(text: string): string {
  const escape = (value: string) =>
    value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
  return text
    .split(/\n{2,}/)
    .map((paragraph) => paragraph.trim())
    .filter((paragraph) => paragraph !== '')
    .map((paragraph) => `<p>${escape(paragraph).replace(/\n/g, '<br>')}</p>`)
    .join('')
}

export type Announcement = {
  id: string
  audience?: Audience
  /** The reader may take it back: its author, or the study office. */
  mine: boolean
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
  const item: Announcement = {
    id: raw.id,
    title: raw.title,
    body: raw.body ?? '',
    createdAt: raw.createdAt,
    mine: raw.mine === true,
  }
  if (raw.audience !== undefined) item.audience = raw.audience
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
