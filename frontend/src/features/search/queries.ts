import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { Role } from '@/features/auth/model'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'

export type SearchHitType = 'COURSE' | 'LESSON' | 'SUBJECT'

export type SearchHit = {
  type: SearchHitType
  id: string
  /** The course a hit is opened in; absent for a subject. */
  courseId?: string
  title: string
}

const MIN_LENGTH = 2

/** Whether the text is worth asking the server about. */
export function isSearchable(text: string): boolean {
  return text.trim().length >= MIN_LENGTH
}

/**
 * Search over what I can open: my courses and their lessons (the server decides what is mine).
 * Words are matched from their beginning, so it answers while a word is still being typed.
 */
export function useSearchQuery(text: string, locale: Locale) {
  const query = text.trim()
  return useQuery({
    queryKey: ['search', query, locale],
    queryFn: async () => {
      const hits = await unwrap(
        api.GET('/api/v1/search', { params: { query: { q: query, locale, limit: 12 } } }),
      )
      return hits.flatMap((hit): SearchHit[] => {
        const type = hit.resultType
        if (hit.entityId === undefined || (type !== 'COURSE' && type !== 'LESSON' && type !== 'SUBJECT'))
          return []
        const narrowed: SearchHit = { type, id: hit.entityId, title: hit.title ?? '' }
        if (hit.courseId !== undefined) narrowed.courseId = hit.courseId
        return [narrowed]
      })
    },
    enabled: isSearchable(query),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  })
}

/**
 * Where a hit opens for the role the reader is in. Only the student's pages exist so far: a
 * teacher's and an administrator's hits get their addresses with their own phases, and until
 * then are not offered rather than leading nowhere.
 */
export function searchHitPath(hit: SearchHit, role: Role): string | null {
  if (role === 'TEACHER' && hit.courseId !== undefined) {
    if (hit.type === 'COURSE') return `/teacher/courses/${hit.courseId}`
    if (hit.type === 'LESSON') return `/teacher/lessons/${hit.id}/present`
  }
  if (role !== 'STUDENT' || hit.courseId === undefined) return null
  if (hit.type === 'COURSE') return `/student/courses/${hit.courseId}`
  if (hit.type === 'LESSON') return `/student/courses/${hit.courseId}/lessons/${hit.id}`
  return null
}
