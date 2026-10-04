import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import { useRefreshingMutation } from './reference-queries'

type Schemas = components['schemas']
export type SubjectSummary = Schemas['SubjectSummary']
export type SubjectDetail = Schemas['SubjectDetail']
export type CourseSummary = Schemas['CourseSummary']
export type CourseDetail = Schemas['CourseDetail']
export type SessionType = Schemas['SetSessionAllocationRequest']['sessionType']
export type ComponentInput = Schemas['CreateAssessmentComponentRequest']
export type EnrollmentSummary = Schemas['EnrollmentSummary']
export type EnrollResult = Schemas['BulkEnrollResultView']
export type CsvImportRow = Schemas['CsvImportRowView']

export const structureKeys = {
  subjects: ['admin', 'subjects'] as const,
  subject: (id: string) => ['admin', 'subjects', id] as const,
  courses: ['admin', 'courses'] as const,
  course: (id: string) => ['admin', 'courses', id] as const,
  enrollments: (courseId: string) => ['admin', 'enrollments', courseId] as const,
}

export type ListFilter = { query: string; status?: string; page: number }

export function useSubjectsQuery(filter: ListFilter) {
  return useQuery({
    queryKey: [...structureKeys.subjects, filter],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/subjects', {
          params: {
            query: {
              ...(filter.query !== '' && { query: filter.query }),
              ...(filter.status === 'DRAFT' || filter.status === 'PUBLISHED' || filter.status === 'ARCHIVED'
                ? { status: filter.status }
                : {}),
              page: filter.page,
              size: 25,
              sort: ['code,asc'],
            },
          },
        }),
      ),
    placeholderData: keepPreviousData,
  })
}

/** Every subject (up to a hundred), for choosing one: a prerequisite, a new course. */
export function useAllSubjectsQuery() {
  return useQuery({
    queryKey: [...structureKeys.subjects, 'all'],
    queryFn: async () =>
      (
        await unwrap(
          api.GET('/api/v1/admin/subjects', { params: { query: { size: 100, sort: ['code,asc'] } } }),
        )
      ).content ?? [],
  })
}

export function useSubjectQuery(id: string) {
  return useQuery({
    queryKey: structureKeys.subject(id),
    queryFn: () => unwrap(api.GET('/api/v1/admin/subjects/{id}', { params: { path: { id } } })),
  })
}

export function useCreateSubject() {
  return useRefreshingMutation(
    [structureKeys.subjects],
    async (input: Schemas['CreateSubjectRequest'] & { title: string; locale: Locale }) => {
      const { title, locale, ...request } = input
      const subject = await unwrap(api.POST('/api/v1/admin/subjects', { body: request }))
      if (subject.id !== undefined) {
        await unwrap(
          api.PUT('/api/v1/admin/subjects/{id}/translations/{locale}', {
            params: { path: { id: subject.id, locale } },
            body: { title },
          }),
        )
      }
      return subject
    },
  )
}

export function useSaveSubjectText(id: string) {
  return useRefreshingMutation(
    [structureKeys.subject(id), structureKeys.subjects],
    ({ locale, text }: { locale: Locale; text: Schemas['UpsertSubjectTranslationRequest'] }) =>
      unwrap(
        api.PUT('/api/v1/admin/subjects/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: text,
        }),
      ),
  )
}

export function useSubjectStatus(id: string) {
  return useRefreshingMutation(
    [structureKeys.subject(id), structureKeys.subjects],
    (status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED') =>
      unwrap(api.PUT('/api/v1/admin/subjects/{id}/status', { params: { path: { id } }, body: { status } })),
  )
}

export function useAddPrerequisite(id: string) {
  return useRefreshingMutation([structureKeys.subject(id)], (input: Schemas['AddPrerequisiteRequest']) =>
    unwrap(api.POST('/api/v1/admin/subjects/{id}/prerequisites', { params: { path: { id } }, body: input })),
  )
}

export function useRemovePrerequisite(id: string) {
  return useRefreshingMutation([structureKeys.subject(id)], (prerequisiteId: string) =>
    unwrap(
      api.DELETE('/api/v1/admin/subjects/{id}/prerequisites/{prerequisiteId}', {
        params: { path: { id, prerequisiteId } },
      }),
    ),
  )
}

export function useAddOutcome(id: string) {
  return useRefreshingMutation(
    [structureKeys.subject(id)],
    async (input: Schemas['AddLearningOutcomeRequest'] & { statement: string; locale: Locale }) => {
      const { statement, locale, ...request } = input
      const subject = await unwrap(
        api.POST('/api/v1/admin/subjects/{id}/learning-outcomes', {
          params: { path: { id } },
          body: request,
        }),
      )
      const outcome = subject.learningOutcomes?.find((item) => item.code === request.code)
      if (outcome?.id !== undefined) {
        await unwrap(
          api.PUT('/api/v1/admin/subjects/learning-outcomes/{outcomeId}/translations/{locale}', {
            params: { path: { outcomeId: outcome.id, locale } },
            body: { statement },
          }),
        )
      }
      return subject
    },
  )
}

// ------------------------------------------------------------------ courses

export function useCoursesQuery(filter: ListFilter & { termId?: string; size?: number }) {
  return useQuery({
    queryKey: [...structureKeys.courses, filter],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/courses', {
          params: {
            query: {
              ...(filter.query !== '' && { query: filter.query }),
              ...(filter.status === 'DRAFT' ||
              filter.status === 'PUBLISHED' ||
              filter.status === 'ARCHIVED' ||
              filter.status === 'CLOSED'
                ? { status: filter.status }
                : {}),
              ...(filter.termId !== undefined && { termId: filter.termId }),
              page: filter.page,
              size: filter.size ?? 25,
            },
          },
        }),
      ),
    placeholderData: keepPreviousData,
  })
}

export function useAdminCourseQuery(id: string) {
  return useQuery({
    queryKey: structureKeys.course(id),
    queryFn: () => unwrap(api.GET('/api/v1/admin/courses/{id}', { params: { path: { id } } })),
  })
}

export function useCreateCourse() {
  return useRefreshingMutation(
    [structureKeys.courses],
    async (input: Schemas['CreateCourseRequest'] & { title: string; locale: Locale }) => {
      const { title, locale, ...request } = input
      const course = await unwrap(api.POST('/api/v1/admin/courses', { body: request }))
      if (course.id !== undefined) {
        await unwrap(
          api.PUT('/api/v1/admin/courses/{id}/translations/{locale}', {
            params: { path: { id: course.id, locale } },
            body: { title },
          }),
        )
      }
      return course
    },
  )
}

function useCourseMutation<V>(id: string, mutationFn: (variables: V) => Promise<unknown>) {
  return useRefreshingMutation([structureKeys.course(id), structureKeys.courses], mutationFn)
}

export function useSaveCourseText(id: string) {
  return useCourseMutation(
    id,
    ({ locale, title, description }: { locale: Locale; title: string; description: string }) =>
      unwrap(
        api.PUT('/api/v1/admin/courses/{id}/translations/{locale}', {
          params: { path: { id, locale } },
          body: { title, ...(description !== '' && { description }) },
        }),
      ),
  )
}

export function useCourseStatus(id: string) {
  return useCourseMutation(id, (status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'CLOSED') =>
    unwrap(api.PUT('/api/v1/admin/courses/{id}/status', { params: { path: { id }, query: { status } } })),
  )
}

export function useAssignTeacher(id: string) {
  return useCourseMutation(id, (input: Schemas['AssignTeacherRequest']) =>
    unwrap(api.POST('/api/v1/admin/courses/{id}/teachers', { params: { path: { id } }, body: input })),
  )
}

export function useRemoveTeacher(id: string) {
  return useCourseMutation(id, (teacherUserId: string) =>
    unwrap(
      api.DELETE('/api/v1/admin/courses/{id}/teachers/{teacherUserId}', {
        params: { path: { id, teacherUserId } },
      }),
    ),
  )
}

export function useSetAllocation(id: string) {
  return useCourseMutation(id, (input: Schemas['SetSessionAllocationRequest']) =>
    unwrap(
      api.PUT('/api/v1/admin/courses/{id}/session-allocations', { params: { path: { id } }, body: input }),
    ),
  )
}

export function useAddComponent(id: string) {
  return useCourseMutation(id, (input: ComponentInput) =>
    unwrap(
      api.POST('/api/v1/admin/courses/{id}/assessment-components', { params: { path: { id } }, body: input }),
    ),
  )
}

export function useSetRequiredLocales(id: string) {
  return useCourseMutation(id, (locales: string[]) =>
    unwrap(
      api.PUT('/api/v1/admin/courses/{id}/required-locales', { params: { path: { id } }, body: { locales } }),
    ),
  )
}

// ------------------------------------------------------------------ enrollments

export function useCourseEnrollmentsQuery(courseId: string) {
  return useQuery({
    queryKey: structureKeys.enrollments(courseId),
    queryFn: async () =>
      (
        await unwrap(
          api.GET('/api/v1/admin/enrollments', {
            params: { query: { courseId, size: 100, sort: ['enrolledAt,desc'] } },
          }),
        )
      ).content ?? [],
  })
}

export function useEnrollStudents(courseId: string) {
  return useRefreshingMutation([structureKeys.enrollments(courseId)], (studentUserIds: string[]) =>
    unwrap(
      api.POST('/api/v1/admin/enrollments/bulk', { body: { courseId, studentUserIds, source: 'ADMIN' } }),
    ),
  )
}

/** Enrols the students of a registrar CSV export: one student number per line. */
export function useImportEnrollments(courseId: string) {
  return useRefreshingMutation([structureKeys.enrollments(courseId)], (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return unwrap(
      api.POST('/api/v1/admin/enrollments/import', {
        params: { query: { courseId, source: 'ADMIN' } },
        body: { file: '' },
        bodySerializer: () => form,
      }),
    )
  })
}

export function useEnrollGroup(courseId: string) {
  return useRefreshingMutation([structureKeys.enrollments(courseId)], (groupCode: string) =>
    unwrap(
      api.POST('/api/v1/admin/enrollments/by-group', { body: { courseId, groupCode, source: 'ADMIN' } }),
    ),
  )
}
