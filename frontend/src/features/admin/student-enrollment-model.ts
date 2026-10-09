import type { components, paths } from '@/shared/api/schema'

export type EnrollmentSummary = components['schemas']['EnrollmentSummary']
export type EnrollmentStatus = NonNullable<EnrollmentSummary['status']>
export type StudentEnrollmentFilter = Omit<
  NonNullable<paths['/api/v1/admin/enrollments']['get']['parameters']['query']>,
  'userId'
>
export type ChangeEnrollmentStatusInput =
  paths['/api/v1/admin/enrollments/{id}/status']['put']['parameters']['query'] &
    Required<Pick<EnrollmentSummary, 'id' | 'courseId' | 'studentUserId'>>

const transitions = {
  PENDING: ['ACTIVE', 'WITHDRAWN'],
  ACTIVE: ['SUSPENDED', 'COMPLETED', 'FAILED', 'WITHDRAWN'],
  SUSPENDED: ['ACTIVE', 'WITHDRAWN'],
  WITHDRAWN: ['ACTIVE'],
  FAILED: ['ACTIVE'],
  COMPLETED: [],
} satisfies Record<EnrollmentStatus, readonly EnrollmentStatus[]>

export function enrollmentTransitions(status: EnrollmentStatus): readonly EnrollmentStatus[] {
  return transitions[status]
}
