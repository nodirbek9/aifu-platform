import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import { teachingKeys } from '@/features/teaching/queries'

type Schemas = components['schemas']
export type ActivityRaw = Schemas['ActivityDetail']
export type CourseWorkType = 'CODING_TASK' | 'QUIZ'
type ActivityStatus = NonNullable<ActivityRaw['status']>

const activityKeys = {
  list: (courseId: string) => ['authoring', 'activities', courseId] as const,
}

/** Every activity of the course, any status, with its texts: what the course work manager lists. */
export function useCourseActivitiesQuery(courseId: string) {
  return useQuery({
    queryKey: activityKeys.list(courseId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/activities', {
          params: { path: { courseId }, query: { page: 0, size: 500 } },
        }),
      )
      return page.content ?? []
    },
  })
}

function useActivityMutation<V>(courseId: string, mutationFn: (variables: V) => Promise<unknown>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSettled: async () => {
      await queryClient.invalidateQueries({ queryKey: activityKeys.list(courseId) })
      await queryClient.invalidateQueries({ queryKey: teachingKeys.activities(courseId) })
    },
  })
}

export type NewCourseWork = {
  type: CourseWorkType
  referenceId: string
  moduleId: string
  dueAt?: string
  /** The referenced task's or quiz's title in each language it has */
  titles: { locale: string; title: string }[]
  componentId?: string
  maxPoints?: number
}

/**
 * A coding task or a quiz becomes course work: an activity placed in a module, pointing at it,
 * with the deadline students see, titled as the task or quiz is, and published.
 */
export function useCreateCourseWork(courseId: string) {
  return useActivityMutation(courseId, async (work: NewCourseWork) => {
    const graded = work.componentId !== undefined
    const created = await unwrap(
      api.POST('/api/v1/teacher/courses/{courseId}/activities', {
        params: { path: { courseId } },
        body: {
          activityType: work.type,
          moduleId: work.moduleId,
          isGraded: graded,
          ...(graded && { assessmentComponentId: work.componentId, maxPoints: work.maxPoints ?? 100 }),
        },
      }),
    )
    const id = created.id ?? ''
    await unwrap(
      api.PATCH('/api/v1/teacher/activities/{id}', {
        params: { path: { id } },
        body: { referenceId: work.referenceId, ...(work.dueAt !== undefined && { dueAt: work.dueAt }) },
      }),
    )
    for (const { locale, title } of work.titles) {
      await unwrap(
        api.PUT('/api/v1/teacher/activities/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: { title },
        }),
      )
    }
    await unwrap(
      api.PUT('/api/v1/teacher/activities/{id}/status', {
        params: { path: { id } },
        body: { status: 'PUBLISHED' },
      }),
    )
  })
}

/** A new deadline (or none); every other detail of the activity is sent back as it is. */
export function useSetActivityDeadline(courseId: string) {
  return useActivityMutation(courseId, ({ activity, dueAt }: { activity: ActivityRaw; dueAt?: string }) =>
    unwrap(
      api.PATCH('/api/v1/teacher/activities/{id}', {
        params: { path: { id: activity.id ?? '' } },
        body: {
          ...(activity.referenceId !== undefined && { referenceId: activity.referenceId }),
          ...(activity.weight !== undefined && { weight: activity.weight }),
          ...(activity.attemptLimit !== undefined && { attemptLimit: activity.attemptLimit }),
          ...(activity.collaborationMode !== undefined && { collaborationMode: activity.collaborationMode }),
          ...(activity.variantStrategy !== undefined && { variantStrategy: activity.variantStrategy }),
          ...(activity.availableFrom !== undefined && { availableFrom: activity.availableFrom }),
          ...(dueAt !== undefined && { dueAt }),
        },
      }),
    ),
  )
}

export function useActivityStatus(courseId: string) {
  return useActivityMutation(courseId, ({ id, status }: { id: string; status: ActivityStatus }) =>
    unwrap(api.PUT('/api/v1/teacher/activities/{id}/status', { params: { path: { id } }, body: { status } })),
  )
}

/** "2026-10-12T18:00" as the datetime-local input shows a moment in the browser's time zone. */
export function toLocalInput(iso: string | undefined): string {
  if (iso === undefined) return ''
  const date = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${String(date.getFullYear())}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function fromLocalInput(value: string): string | undefined {
  if (value === '') return undefined
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString()
}
