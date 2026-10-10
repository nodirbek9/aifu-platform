import { useMutation, useQueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import { ApiFailure, toApiProblem, type ApiProblem } from '@/shared/api/errors'
import type { components } from '@/shared/api/schema'
import { studentEnrollmentKeys } from './student-enrollment-queries'
import { structureKeys } from './structure-queries'
import { userKeys } from './users-queries'

type Enrollment = components['schemas']['EnrollmentDetail']
export type RemovalEnrollment = Required<Pick<Enrollment, 'id' | 'courseId'>>
export type RemoveStudentInput = {
  confirmed: boolean
  userId: string
  reason: string
  enrollments: readonly RemovalEnrollment[]
}

export type RemovalOutcome =
  | { status: 'succeeded' | 'already-satisfied' }
  | { status: 'blocked' }
  | { status: 'failed'; problem: ApiProblem; verificationProblem?: ApiProblem }

export type RemoveStudentResult =
  | { status: 'cancelled' }
  | {
      status: 'complete' | 'partial'
      account: RemovalOutcome
      enrollments: (RemovalEnrollment & { outcome: RemovalOutcome })[]
    }

function invalidState(): ApiFailure {
  return new ApiFailure({ status: 0, code: 'INVALID_STATE_TRANSITION', message: '' })
}

async function readUser(userId: string) {
  const user = await unwrap(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: userId } } }))
  if (user.id !== userId || user.status === undefined) throw invalidState()
  return user
}

async function readEnrollment(userId: string, target: RemovalEnrollment) {
  const enrollment = await unwrap(
    api.GET('/api/v1/admin/enrollments/{id}', { params: { path: { id: target.id } } }),
  )
  if (
    enrollment.id !== target.id ||
    enrollment.studentUserId !== userId ||
    enrollment.courseId !== target.courseId ||
    enrollment.status === undefined
  ) {
    throw invalidState()
  }
  return enrollment
}

/** An ambiguous write is never repeated here: verify it, or leave it for a fresh-state retry. */
async function confirmWrite(
  write: () => Promise<unknown>,
  satisfied: () => Promise<boolean>,
): Promise<RemovalOutcome> {
  try {
    await write()
    return { status: 'succeeded' }
  } catch (error) {
    const problem = toApiProblem(error)
    try {
      if (await satisfied()) return { status: 'succeeded' }
      return { status: 'failed', problem }
    } catch (verificationError) {
      return { status: 'failed', problem, verificationProblem: toApiProblem(verificationError) }
    }
  }
}

function satisfied(outcome: RemovalOutcome) {
  return outcome.status === 'succeeded' || outcome.status === 'already-satisfied'
}

/** Non-atomic by design. A retry uses server state, never compensation or cached successes. */
export function useRemoveStudent() {
  const queryClient = useQueryClient()
  return useMutation({
    retry: false,
    mutationFn: async (input: RemoveStudentInput): Promise<RemoveStudentResult> => {
      if (!input.confirmed) return { status: 'cancelled' }
      const { userId } = input
      const reason = input.reason.trim()
      if (userId === '' || reason === '') throw invalidState()
      const targets = [...new Map(input.enrollments.map((target) => [target.id, target])).values()]
      const results = targets.map((target) => ({
        ...target,
        outcome: { status: 'blocked' } satisfies RemovalOutcome,
      }))
      let user: Awaited<ReturnType<typeof readUser>>
      try {
        user = await readUser(userId)
      } catch (error) {
        return {
          status: 'partial',
          account: { status: 'failed', problem: toApiProblem(error) },
          enrollments: results,
        }
      }

      // Read every explicitly selected target before any write. Failed reads cannot become writes.
      const snapshots: { target: RemovalEnrollment; enrollment?: Enrollment; problem?: ApiProblem }[] = []
      for (const target of targets) {
        try {
          snapshots.push({ target, enrollment: await readEnrollment(userId, target) })
        } catch (error) {
          snapshots.push({ target, problem: toApiProblem(error) })
        }
      }

      const account: RemovalOutcome =
        user.status === 'DISABLED'
          ? { status: 'already-satisfied' }
          : await confirmWrite(
              () =>
                unwrap(
                  api.PUT('/api/v1/admin/users/{id}/status', {
                    params: { path: { id: userId } },
                    body: { status: 'DISABLED', reason },
                  }),
                ),
              async () => (await readUser(userId)).status === 'DISABLED',
            )
      if (!satisfied(account)) return { status: 'partial', account, enrollments: results }

      const enrollments: (RemovalEnrollment & { outcome: RemovalOutcome })[] = []
      for (const { target, enrollment, problem } of snapshots) {
        let outcome: RemovalOutcome
        if (problem) outcome = { status: 'failed', problem }
        else if (enrollment?.status === 'WITHDRAWN') outcome = { status: 'already-satisfied' }
        else if (
          enrollment?.status === 'PENDING' ||
          enrollment?.status === 'ACTIVE' ||
          enrollment?.status === 'SUSPENDED'
        ) {
          outcome = await confirmWrite(
            () =>
              unwrap(
                api.PUT('/api/v1/admin/enrollments/{id}/status', {
                  params: { path: { id: target.id }, query: { status: 'WITHDRAWN', reason } },
                }),
              ),
            async () => (await readEnrollment(userId, target)).status === 'WITHDRAWN',
          )
        } else outcome = { status: 'failed', problem: invalidState().problem }
        enrollments.push({ ...target, outcome })
      }
      return {
        status: enrollments.every(({ outcome }) => satisfied(outcome)) ? 'complete' : 'partial',
        account,
        enrollments,
      }
    },
    onSettled: async (_data, _error, input) => {
      if (!input.confirmed) return
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: userKeys.user(input.userId) }),
        queryClient.invalidateQueries({ queryKey: userKeys.list }),
        queryClient.invalidateQueries({ queryKey: studentEnrollmentKeys.user(input.userId) }),
        ...input.enrollments.flatMap(({ courseId }) => [
          queryClient.invalidateQueries({ queryKey: structureKeys.enrollments(courseId) }),
          queryClient.invalidateQueries({ queryKey: structureKeys.course(courseId) }),
        ]),
        queryClient.invalidateQueries({ queryKey: ['admin', 'problems'] }),
      ])
    },
  })
}
