import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import { CAMPUS_TIME_ZONE } from '@/shared/lib/dates'
import { useRefreshingMutation } from './reference-queries'
import { structureKeys } from './structure-queries'
import { problemKeys } from './problems-queries'

type Schemas = components['schemas']
export type LearningPathConfigView = Schemas['LearningPathConfigView']
export type LearningPathConfigRequest = Schemas['LearningPathConfigRequest']
export type AttemptGrantView = Schemas['AttemptGrantView']
export type AttemptGrantRequest = Schemas['AttemptGrantRequest']
export type PageResponseAttemptGrantView = Schemas['PageResponseAttemptGrantView']

export const learningPathKeys = {
  config: (courseId: string) => ['admin', 'courses', courseId, 'learning-path'] as const,
  grants: (courseId?: string, page = 0, size = 20) =>
    ['admin', 'attempt-grants', { courseId, page, size }] as const,
}

export function useLearningPathConfigQuery(courseId: string) {
  return useQuery({
    queryKey: learningPathKeys.config(courseId),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/courses/{courseId}/learning-path', {
          params: { path: { courseId } },
        }),
      ),
    enabled: courseId !== '',
  })
}

export function useSaveLearningPathConfig(courseId: string) {
  const queryClient = useQueryClient()
  return useRefreshingMutation(
    [learningPathKeys.config(courseId), structureKeys.course(courseId)],
    async (request: LearningPathConfigRequest) => {
      const res = await unwrap(
        api.PUT('/api/v1/admin/courses/{courseId}/learning-path', {
          params: { path: { courseId } },
          body: request,
        }),
      )
      void queryClient.invalidateQueries({ queryKey: ['course-plan', courseId] })
      return res
    },
  )
}

export function useAttemptGrantsQuery(filter: { courseId?: string; page?: number; size?: number }) {
  const page = filter.page ?? 0
  const size = filter.size ?? 20
  return useQuery({
    queryKey: learningPathKeys.grants(filter.courseId, page, size),
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/attempt-grants', {
          params: {
            query: {
              ...(filter.courseId !== undefined && filter.courseId !== ''
                ? { courseId: filter.courseId }
                : {}),
              page,
              size,
              sort: ['createdAt,desc'],
            },
          },
        }),
      ),
  })
}

export function useGrantAttempt() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      quizId,
      enrollmentId,
      reason,
    }: {
      quizId: string
      enrollmentId: string
      reason: string
    }) =>
      unwrap(
        api.POST('/api/v1/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants', {
          params: { path: { quizId, enrollmentId } },
          body: { reason },
        }),
      ),
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: ['admin', 'attempt-grants'] })
      void queryClient.invalidateQueries({ queryKey: ['course-plan'] })
      void queryClient.invalidateQueries({ queryKey: ['quiz'] })
      void queryClient.invalidateQueries({ queryKey: problemKeys.list })
    },
  })
}

export function useQuizDetailQuery(quizId?: string) {
  return useQuery({
    queryKey: ['authoring', 'quiz', quizId ?? ''],
    queryFn: () => unwrap(api.GET('/api/v1/teacher/quizzes/{id}', { params: { path: { id: quizId ?? '' } } })),
    enabled: Boolean(quizId && quizId.trim() !== ''),
  })
}

/** Form data for editing learning path config */
export type LearningPathFormState = {
  enabled: boolean
  midtermActivityId?: string
  midtermAfterLessonId?: string
  midtermOpensAtLocal?: string
}

/** Converts an ISO instant string to campus local datetime-local format 'YYYY-MM-DDTHH:mm' */
export function isoToCampusDatetimeLocal(iso: string | undefined): string {
  if (!iso) return ''
  const instant = new Date(iso)
  if (Number.isNaN(instant.getTime())) return ''
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: CAMPUS_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(instant)
  const get = (type: Intl.DateTimeFormatPartTypes): string =>
    parts.find((p) => p.type === type)?.value ?? '00'
  return `${get('year')}-${get('month')}-${get('day')}T${get('hour')}:${get('minute')}`
}

/** Converts campus local datetime-local format 'YYYY-MM-DDTHH:mm' to ISO string with +05:00 offset */
export function campusDatetimeLocalToIso(local: string | undefined): string | undefined {
  if (!local || local.trim() === '') return undefined
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(local)
  if (!match) return undefined
  const [, year, month, day, hour, minute] = match
  return `${year}-${month}-${day}T${hour}:${minute}:00+05:00`
}

/** Validates reason: at least 5 non-whitespace chars */
export function validateGrantReason(reason: string): boolean {
  return reason.trim().length >= 5
}

/** Pure helper to build PUT LearningPathConfigRequest from form state */
export function buildLearningPathConfigRequest(form: LearningPathFormState): LearningPathConfigRequest {
  const req: LearningPathConfigRequest = {
    enabled: form.enabled,
  }
  if (form.midtermActivityId && form.midtermActivityId !== '') {
    req.midtermActivityId = form.midtermActivityId
  }
  if (form.midtermAfterLessonId && form.midtermAfterLessonId !== '') {
    req.midtermAfterLessonId = form.midtermAfterLessonId
  }
  const iso = campusDatetimeLocalToIso(form.midtermOpensAtLocal)
  if (iso) {
    req.midtermOpensAt = iso
  }
  return req
}

export type GrantTargetError =
  | 'ENROLLMENT_NOT_FOUND'
  | 'ENROLLMENT_COURSE_MISMATCH'
  | 'QUIZ_NOT_FOUND'
  | 'QUIZ_COURSE_MISMATCH'

export type ResolveGrantTargetResult = {
  valid: boolean
  error?: GrantTargetError
  enrollmentId?: string
  quizId?: string
}

export type ResolveGrantTargetInput = {
  courseId: string
  targetEnrollmentId?: string
  targetQuizId?: string
  enrollment?: { id?: string; courseId?: string } | null
  quizzes?: { id?: string; courseId?: string }[] | null
  quiz?: { id?: string; courseId?: string } | null
}

/** Pure helper to validate targeted enrollment and quiz belong to the selected course */
export function resolveGrantTarget(input: ResolveGrantTargetInput): ResolveGrantTargetResult {
  const { courseId, targetEnrollmentId, targetQuizId, enrollment, quizzes, quiz } = input

  if (targetEnrollmentId && targetEnrollmentId.trim() !== '') {
    if (enrollment?.id !== targetEnrollmentId) {
      return { valid: false, error: 'ENROLLMENT_NOT_FOUND' }
    }
    if (enrollment.courseId !== courseId) {
      return { valid: false, error: 'ENROLLMENT_COURSE_MISMATCH' }
    }
  }

  if (targetQuizId && targetQuizId.trim() !== '') {
    const foundInList = quizzes?.find((q) => q.id === targetQuizId)
    if (foundInList) {
      if (foundInList.courseId && foundInList.courseId !== courseId) {
        return { valid: false, error: 'QUIZ_COURSE_MISMATCH' }
      }
    } else if (quiz?.id === targetQuizId) {
      if (quiz.courseId && quiz.courseId !== courseId) {
        return { valid: false, error: 'QUIZ_COURSE_MISMATCH' }
      }
    } else {
      return { valid: false, error: 'QUIZ_NOT_FOUND' }
    }
  }

  return {
    valid: true,
    enrollmentId: targetEnrollmentId && targetEnrollmentId.trim() !== '' ? targetEnrollmentId : undefined,
    quizId: targetQuizId && targetQuizId.trim() !== '' ? targetQuizId : undefined,
  }
}

