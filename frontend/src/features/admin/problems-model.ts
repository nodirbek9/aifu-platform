import { z } from 'zod'
import type { components } from '@/shared/api/schema'

type ProblemDto = components['schemas']['AdminProblemView']
export type AdminProblemType = NonNullable<ProblemDto['type']>
export type AdminProblemView = Omit<ProblemDto, 'problemKey' | 'type'> &
  Required<Pick<ProblemDto, 'problemKey' | 'type'>>

export const ADMIN_PROBLEM_TYPES = [
  'STUDENT_WITHOUT_GROUP',
  'GROUP_WITHOUT_PUBLISHED_ENTRIES',
  'COURSE_WITHOUT_TEACHER',
  'ACTIVE_ENROLLMENT_INACTIVE_USER',
  'LOCKED_ACCOUNT',
  'MIDTERM_ATTEMPTS_EXHAUSTED',
] as const satisfies readonly AdminProblemType[]

export function readProblemType(value: unknown): AdminProblemType | undefined {
  return ADMIN_PROBLEM_TYPES.find((type) => type === value)
}

export function narrowProblemView(raw: ProblemDto): AdminProblemView {
  const type = readProblemType(raw.type)
  if (!raw.problemKey?.trim() || type === undefined)
    throw new Error('Invalid problem view: missing problemKey or unknown type')
  return { ...raw, problemKey: raw.problemKey, type }
}

// The generated operation's 200 application/json response is unknown. Validate it
// against the generated DTO rather than casting the HTTP response.
const problemSchema = z.object({
  problemKey: z
    .string()
    .min(1)
    .refine((value) => value.trim() !== ''),
  type: z.enum(ADMIN_PROBLEM_TYPES),
  userId: z.string().optional(),
  fullName: z.string().optional(),
  groupId: z.string().optional(),
  groupCode: z.string().optional(),
  courseId: z.string().optional(),
  subjectCode: z.string().optional(),
  cohortCode: z.string().optional(),
  termName: z.string().optional(),
  enrollmentId: z.string().optional(),
  quizId: z.string().optional(),
  attemptsUsed: z.number().int().nonnegative().optional(),
  attemptsAllowed: z.number().int().nonnegative().optional(),
  grantAvailable: z.boolean().optional(),
  lockedUntil: z.string().optional(),
}) satisfies z.ZodType<AdminProblemView>

const problemPageSchema = z.object({
  content: z.array(problemSchema),
  page: z.number().int().nonnegative(),
  size: z.number().int().positive().max(100),
  totalElements: z.number().int().nonnegative(),
  totalPages: z.number().int().nonnegative(),
  hasNext: z.boolean(),
  hasPrevious: z.boolean(),
}) satisfies z.ZodType<components['schemas']['PageResponseAdminProblemView']>

export function narrowProblemPage(raw: unknown) {
  const page = problemPageSchema.parse(raw)
  return { ...page, content: page.content.map(narrowProblemView) }
}

export type ProblemAction =
  | {
      type: 'navigate'
      path: string
      label: 'admin.problems.fix' | 'admin.problems.reviewUsers' | 'admin.problems.reviewAttempt'
    }
  | { type: 'unavailable'; label: 'admin.problems.actionUnavailable' }

export function getProblemAction(problem: AdminProblemView): ProblemAction {
  const unavailable: ProblemAction = { type: 'unavailable', label: 'admin.problems.actionUnavailable' }
  const userId = problem.userId?.trim()
  const groupId = problem.groupId?.trim()
  const courseId = problem.courseId?.trim()
  const enrollmentId = problem.enrollmentId?.trim()
  const quizId = problem.quizId?.trim()
  switch (problem.type) {
    case 'STUDENT_WITHOUT_GROUP':
      return userId
        ? {
            type: 'navigate',
            path: `/admin/users/${encodeURIComponent(userId)}#group`,
            label: 'admin.problems.fix',
          }
        : unavailable
    case 'GROUP_WITHOUT_PUBLISHED_ENTRIES':
      return groupId
        ? {
            type: 'navigate',
            path: `/admin/timetable?${new URLSearchParams({ view: 'group', id: groupId })}`,
            label: 'admin.problems.fix',
          }
        : unavailable
    case 'COURSE_WITHOUT_TEACHER':
      return courseId
        ? {
            type: 'navigate',
            path: `/admin/structure/courses/${encodeURIComponent(courseId)}`,
            label: 'admin.problems.fix',
          }
        : unavailable
    case 'ACTIVE_ENROLLMENT_INACTIVE_USER':
      return userId
        ? {
            type: 'navigate',
            path: `/admin/users/${encodeURIComponent(userId)}#enrollments`,
            label: 'admin.problems.reviewUsers',
          }
        : unavailable
    case 'LOCKED_ACCOUNT':
      return userId
        ? {
            type: 'navigate',
            path: `/admin/users/${encodeURIComponent(userId)}#status`,
            label: 'admin.problems.fix',
          }
        : unavailable
    case 'MIDTERM_ATTEMPTS_EXHAUSTED':
      return courseId && enrollmentId && quizId
        ? {
            type: 'navigate',
            path: `/admin/learning-path?${new URLSearchParams({ course: courseId, enrollment: enrollmentId, quiz: quizId })}`,
            label: problem.grantAvailable === true ? 'admin.problems.fix' : 'admin.problems.reviewAttempt',
          }
        : unavailable
    default: {
      const exhaustive: never = problem.type
      return exhaustive
    }
  }
}
