import { queryOptions, useQueries, useQuery } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import {
  narrowActivity,
  narrowCourse,
  narrowEnrollment,
  narrowGrade,
  narrowPlan,
  narrowSubject,
  type Enrollment,
} from './model'

/** Query keys of everything about the student's courses; mutations elsewhere invalidate by them. */
export const courseKeys = {
  enrollments: ['enrollments', 'me'] as const,
  course: (courseId: string, locale: Locale) => ['course', courseId, locale] as const,
  plan: (courseId: string, locale: Locale) => ['course-plan', courseId, locale] as const,
  planOfCourse: (courseId: string) => ['course-plan', courseId] as const,
  progress: (courseId: string) => ['course-progress', courseId] as const,
  activities: (courseId?: string) =>
    courseId === undefined ? (['activities', 'me'] as const) : (['activities', 'me', courseId] as const),
  grades: (enrollmentId: string, locale?: Locale) =>
    locale === undefined ? (['grades', enrollmentId] as const) : (['grades', enrollmentId, locale] as const),
  subject: (subjectId: string, locale: Locale) => ['subject', subjectId, locale] as const,
}

export function useEnrollmentsQuery() {
  return useQuery({
    queryKey: courseKeys.enrollments,
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/enrollments/me', { params: { query: { page: 0, size: 100 } } }),
      )
      return (page.content ?? []).map(narrowEnrollment).filter((enrollment) => enrollment !== null)
    },
  })
}

export function courseQuery(courseId: string, locale: Locale) {
  return queryOptions({
    queryKey: courseKeys.course(courseId, locale),
    queryFn: async () =>
      narrowCourse(
        await unwrap(api.GET('/api/v1/courses/{id}', { params: { path: { id: courseId } } })),
        locale,
      ),
  })
}

export function coursePlanQuery(courseId: string, locale: Locale) {
  return queryOptions({
    queryKey: courseKeys.plan(courseId, locale),
    queryFn: async () =>
      narrowPlan(
        await unwrap(
          api.GET('/api/v1/courses/{courseId}/plan/me', {
            params: { path: { courseId }, query: { locale } },
          }),
        ),
      ),
  })
}

/** When the student last opened anything in the course — to know which course to continue. */
function courseProgressQuery(courseId: string) {
  return queryOptions({
    queryKey: courseKeys.progress(courseId),
    queryFn: async () => {
      const view = await unwrap(
        api.GET('/api/v1/courses/{courseId}/progress/me', { params: { path: { courseId } } }),
      )
      return { courseId, lastAccessedAt: view.lastAccessedAt ?? '' }
    },
  })
}

export function useCourseQuery(courseId: string, locale: Locale) {
  return useQuery(courseQuery(courseId, locale))
}

export function useCoursePlanQuery(courseId: string, locale: Locale) {
  return useQuery(coursePlanQuery(courseId, locale))
}

/** The course the student worked in most recently, among their active enrollments. */
export function useLastCourseId(enrollments: readonly Enrollment[] | undefined) {
  return useQueries({
    queries: (enrollments ?? []).map((enrollment) => courseProgressQuery(enrollment.courseId)),
    combine: (results) => {
      const pending = enrollments === undefined || results.some((result) => result.isPending)
      const error = results.find((result) => result.isError)?.error
      const latest = results
        .flatMap((result) => (result.data !== undefined ? [result.data] : []))
        .sort((a, b) => b.lastAccessedAt.localeCompare(a.lastAccessedAt))[0]
      return { pending, error, courseId: latest?.courseId ?? enrollments?.[0]?.courseId }
    },
  })
}

export function useActivitiesQuery(locale: Locale, courseId?: string) {
  return useQuery({
    queryKey: [...courseKeys.activities(courseId), locale],
    queryFn: async () => {
      const query = courseId === undefined ? { locale } : { locale, courseId }
      const list = await unwrap(api.GET('/api/v1/activities/me', { params: { query } }))
      return list.map(narrowActivity).filter((activity) => activity !== null)
    },
  })
}

function gradesQuery(enrollment: Enrollment, locale?: Locale) {
  return queryOptions({
    queryKey: courseKeys.grades(enrollment.id, locale),
    queryFn: async () => {
      const view = await unwrap(
        api.GET('/api/v1/enrollments/{id}/grades', {
          params: { path: { id: enrollment.id }, query: locale !== undefined ? { locale } : undefined },
        }),
      )
      return (view.entries ?? [])
        .map(narrowGrade)
        .filter((grade) => grade !== null)
        .map((grade) => ({ ...grade, courseId: enrollment.courseId, subjectCode: enrollment.subjectCode }))
    },
  })
}

export function useGradesQuery(enrollment: Enrollment | undefined, locale?: Locale) {
  return useQuery({
    ...gradesQuery(enrollment ?? { id: '', courseId: '', subjectCode: '' }, locale),
    enabled: enrollment !== undefined,
  })
}

/** The newest published grades across all of the student's courses. */
export function useRecentGrades(
  enrollments: readonly Enrollment[] | undefined,
  limit: number,
  locale?: Locale,
) {
  return useQueries({
    queries: (enrollments ?? []).map((enrollment) => gradesQuery(enrollment, locale)),
    combine: (results) => ({
      pending: enrollments === undefined || results.some((result) => result.isPending),
      error: results.find((result) => result.isError)?.error,
      refetch: () => {
        for (const result of results) void result.refetch()
      },
      grades: results
        .flatMap((result) => result.data ?? [])
        .sort((a, b) => b.publishedAt.localeCompare(a.publishedAt))
        .slice(0, limit),
    }),
  })
}

export function useSubjectQuery(subjectId: string | undefined, locale: Locale) {
  return useQuery({
    queryKey: courseKeys.subject(subjectId ?? '', locale),
    queryFn: async () =>
      narrowSubject(
        await unwrap(api.GET('/api/v1/subjects/{id}', { params: { path: { id: subjectId ?? '' } } })),
        locale,
      ),
    enabled: subjectId !== undefined,
  })
}
