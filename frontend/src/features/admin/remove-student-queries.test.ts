import { createElement, type PropsWithChildren } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, renderHook } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { studentEnrollmentKeys } from './student-enrollment-queries'
import { structureKeys } from './structure-queries'
import { userKeys } from './users-queries'
import { useRemoveStudent, type RemoveStudentInput, type RemoveStudentResult } from './remove-student-queries'

const { NativeRequest, BrowserRequest } = vi.hoisted(() => {
  const NativeRequest = globalThis.Request
  class BrowserRequest extends NativeRequest {
    constructor(input: RequestInfo | URL, init?: RequestInit) {
      super(typeof input === 'string' ? new URL(input, 'http://localhost') : input, init)
    }
  }
  vi.stubGlobal('Request', BrowserRequest)
  return { NativeRequest, BrowserRequest }
})

type Schemas = components['schemas']
const fetchMock = vi.fn<typeof fetch>()
let queryClient: QueryClient
let user: Schemas['UserDetail']
let enrollments: Map<string, Schemas['EnrollmentDetail']>
let writes: Request[]
let requests: Request[]
let intercept: ((request: Request) => Promise<Response> | undefined) | undefined

const input = {
  confirmed: true,
  userId: 'student-a',
  reason: '  Registrar decision: oʻqish & review?  ',
  enrollments: [
    { id: 'enrollment-a', courseId: 'course-a' },
    { id: 'enrollment-b', courseId: 'course-b' },
  ],
} satisfies RemoveStudentInput

function wrapper({ children }: PropsWithChildren) {
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function refused() {
  return Promise.resolve(json({ code: 'ENROLLMENT_INVALID_TRANSITION', requestId: 'request-refused' }, 422))
}

async function server(request: Request): Promise<Response> {
  const url = new URL(request.url)
  if (url.pathname.includes('/users/')) {
    if (request.method === 'PUT') {
      const body: unknown = await request.json()
      expect(body).toEqual({ status: 'DISABLED', reason: input.reason.trim() })
      user = { ...user, status: 'DISABLED' }
    }
    return json(user)
  }
  const id = url.pathname.split('/')[5] ?? ''
  const enrollment = enrollments.get(id)
  if (!enrollment) throw new Error('Unexpected enrollment request')
  if (request.method === 'PUT') {
    expect(url.searchParams.get('status')).toBe('WITHDRAWN')
    expect(url.searchParams.get('reason')).toBe(input.reason.trim())
    expect(request.body).toBeNull()
    expect(request.headers.has('Content-Type')).toBe(false)
    const withdrawn = { ...enrollment, status: 'WITHDRAWN' } satisfies Schemas['EnrollmentDetail']
    enrollments.set(id, withdrawn)
    return json(withdrawn)
  }
  return json(enrollment)
}

beforeEach(() => {
  queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  resetSessionForTests()
  user = { id: input.userId, status: 'ACTIVE' }
  enrollments = new Map(
    input.enrollments.map(({ id, courseId }) => [
      id,
      { id, courseId, studentUserId: input.userId, status: 'ACTIVE' },
    ]),
  )
  writes = []
  requests = []
  intercept = undefined
  fetchMock.mockImplementation(async (value) => {
    if (!(value instanceof NativeRequest)) throw new Error('Expected a Request')
    requests.push(value)
    if (value.method === 'PUT') writes.push(value)
    return intercept?.(value) ?? server(value)
  })
  vi.stubGlobal('fetch', fetchMock)
  vi.stubGlobal('Request', BrowserRequest)
})

afterEach(() => {
  cleanup()
  queryClient.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

async function run(variables: RemoveStudentInput = input) {
  const { result } = renderHook(() => useRemoveStudent(), { wrapper })
  let output: RemoveStudentResult | undefined
  await act(async () => {
    output = await result.current.mutateAsync(variables)
  })
  if (!output || output.status === 'cancelled') throw new Error('Expected operation results')
  return output
}

describe('recoverable student removal (7.3-F1)', () => {
  it('cancellation sends nothing and does not invalidate cached state', async () => {
    queryClient.setQueryData(userKeys.user(input.userId), user)
    const { result } = renderHook(() => useRemoveStudent(), { wrapper })
    await act(async () => {
      expect(await result.current.mutateAsync({ ...input, confirmed: false })).toEqual({
        status: 'cancelled',
      })
    })
    expect(fetchMock).not.toHaveBeenCalled()
    expect(queryClient.getQueryState(userKeys.user(input.userId))?.isInvalidated).toBe(false)
  })

  it('refetches all targets before writes, disables first and withdraws sequentially', async () => {
    // Cached values must not determine what to write.
    queryClient.setQueryData(userKeys.user(input.userId), { ...user, status: 'DISABLED' })
    let release: (() => void) | undefined
    const firstWithdrawal = new Promise<void>((resolve) => {
      release = resolve
    })
    intercept = (request) =>
      request.method === 'PUT' && request.url.includes('enrollment-a')
        ? firstWithdrawal.then(() => server(request))
        : undefined
    const { result } = renderHook(() => useRemoveStudent(), { wrapper })
    let operation: Promise<RemoveStudentResult> | undefined
    await act(async () => {
      operation = result.current.mutateAsync(input)
      // Allow the preflight and disable requests to finish without resolving the first withdrawal.
      await vi.waitFor(() => expect(writes).toHaveLength(2))
    })
    expect(requests.slice(0, 3).every((request) => request.method === 'GET')).toBe(true)
    expect(writes.map((request) => new URL(request.url).pathname)).toEqual([
      '/api/v1/admin/users/student-a/status',
      '/api/v1/admin/enrollments/enrollment-a/status',
    ])
    await act(async () => {
      release?.()
      expect(await operation).toMatchObject({
        status: 'complete',
        account: { status: 'succeeded' },
        enrollments: [
          { id: 'enrollment-a', outcome: { status: 'succeeded' } },
          { id: 'enrollment-b', outcome: { status: 'succeeded' } },
        ],
      })
    })
    expect(writes).toHaveLength(3)
  })

  it('disable failure prevents every withdrawal and preserves its requestId', async () => {
    intercept = (request) => (request.method === 'PUT' ? refused() : undefined)
    expect(await run()).toMatchObject({
      status: 'partial',
      account: { status: 'failed', problem: { requestId: 'request-refused' } },
      enrollments: [{ outcome: { status: 'blocked' } }, { outcome: { status: 'blocked' } }],
    })
    expect(writes).toHaveLength(1)
    expect(user.status).toBe('ACTIVE')
  })

  it('keeps confirmed success after one withdrawal fails and retry skips completed work', async () => {
    intercept = (request) =>
      request.method === 'PUT' && request.url.includes('enrollment-b') ? refused() : undefined
    expect(await run()).toMatchObject({
      status: 'partial',
      account: { status: 'succeeded' },
      enrollments: [
        { outcome: { status: 'succeeded' } },
        { outcome: { status: 'failed', problem: { requestId: 'request-refused' } } },
      ],
    })
    expect(enrollments.get('enrollment-a')?.status).toBe('WITHDRAWN')
    expect(user.status).toBe('DISABLED')
    writes = []
    intercept = undefined
    expect(await run()).toMatchObject({
      status: 'complete',
      account: { status: 'already-satisfied' },
      enrollments: [{ outcome: { status: 'already-satisfied' } }, { outcome: { status: 'succeeded' } }],
    })
    expect(writes).toHaveLength(1)
    expect(writes[0]?.url).toContain('enrollment-b/status')
  })

  it.each(['PENDING', 'ACTIVE', 'SUSPENDED'] as const)(
    'withdraws %s with an already-disabled account',
    async (status) => {
      user.status = 'DISABLED'
      for (const enrollment of enrollments.values()) enrollment.status = status
      expect(await run()).toMatchObject({ status: 'complete', account: { status: 'already-satisfied' } })
      expect(writes).toHaveLength(2)
      expect(writes.every((request) => request.url.includes('/enrollments/'))).toBe(true)
    },
  )

  it.each(['COMPLETED', 'FAILED'] as const)(
    'does not withdraw a target that became terminal (%s)',
    async (status) => {
      const enrollment = enrollments.get('enrollment-a')
      if (enrollment) enrollment.status = status
      expect(await run()).toMatchObject({
        status: 'partial',
        enrollments: [
          { outcome: { status: 'failed', problem: { code: 'INVALID_STATE_TRANSITION' } } },
          { outcome: { status: 'succeeded' } },
        ],
      })
      expect(writes.some((request) => request.url.includes('enrollment-a'))).toBe(false)
    },
  )

  it('preserves a terminal-state race after preflight and does not compensate the account', async () => {
    intercept = (request) => {
      if (request.method !== 'PUT' || !request.url.includes('enrollment-a')) return undefined
      const enrollment = enrollments.get('enrollment-a')
      if (enrollment) enrollment.status = 'COMPLETED'
      return refused()
    }
    expect(await run()).toMatchObject({
      status: 'partial',
      enrollments: [
        { outcome: { status: 'failed', problem: { code: 'ENROLLMENT_INVALID_TRANSITION' } } },
        { outcome: { status: 'succeeded' } },
      ],
    })
    expect(enrollments.get('enrollment-a')?.status).toBe('COMPLETED')
    expect(user.status).toBe('DISABLED')
    expect(writes.filter((request) => request.url.includes('/users/'))).toHaveLength(1)
  })

  it.each(['account', 'enrollment'] as const)(
    'verifies a lost %s response without resending',
    async (operation) => {
      const match = operation === 'account' ? '/users/' : 'enrollment-a'
      intercept = (request) =>
        request.method === 'PUT' && request.url.includes(match)
          ? server(request).then(() => {
              throw new TypeError('Response lost')
            })
          : undefined
      expect(await run()).toMatchObject({ status: 'complete' })
      expect(writes.filter((request) => request.url.includes(match))).toHaveLength(1)
      const writeIndex = requests.findIndex(
        (request) => request.method === 'PUT' && request.url.includes(match),
      )
      expect(requests[writeIndex + 1]?.method).toBe('GET')
      expect(requests[writeIndex + 1]?.url).toContain(match)
    },
  )

  it('reports an ambiguous write plus verification failure; retry reads before sending anything', async () => {
    let lost = false
    intercept = (request) => {
      if (!request.url.includes('/users/')) return undefined
      if (request.method === 'PUT') {
        lost = true
        return server(request).then(() => {
          throw new TypeError('Response lost')
        })
      }
      if (lost) return Promise.reject(new TypeError('Verification unavailable'))
      return undefined
    }
    expect(await run()).toMatchObject({
      status: 'partial',
      account: {
        status: 'failed',
        problem: { code: 'NETWORK_ERROR' },
        verificationProblem: { code: 'NETWORK_ERROR' },
      },
    })
    expect(writes).toHaveLength(1)
    intercept = undefined
    writes = []
    expect(await run()).toMatchObject({ status: 'complete', account: { status: 'already-satisfied' } })
    expect(writes.every((request) => request.url.includes('/enrollments/'))).toBe(true)
  })

  it('does not write unreadable, missing-state or unrelated enrollments', async () => {
    const enrollment = enrollments.get('enrollment-a')
    if (enrollment) enrollment.studentUserId = 'student-b'
    intercept = (request) =>
      request.method === 'GET' && request.url.includes('enrollment-b') ? refused() : undefined
    expect(await run()).toMatchObject({
      status: 'partial',
      enrollments: [
        { outcome: { status: 'failed' } },
        { outcome: { status: 'failed', problem: { requestId: 'request-refused' } } },
      ],
    })
    expect(writes).toHaveLength(1)
  })

  it('a failed user refetch blocks all writes', async () => {
    intercept = () => refused()
    expect(await run()).toMatchObject({ status: 'partial', account: { status: 'failed' } })
    expect(writes).toHaveLength(0)
    expect(requests).toHaveLength(1)
  })

  it('supports deactivation only and deduplicates explicitly selected enrollments', async () => {
    expect(await run({ ...input, enrollments: [] })).toMatchObject({ status: 'complete', enrollments: [] })
    writes = []
    expect(await run({ ...input, enrollments: [...input.enrollments, ...input.enrollments] })).toMatchObject({
      status: 'complete',
    })
    expect(writes).toHaveLength(2)
  })

  it.each([false, true])(
    'invalidates user, enrollment, course and problems caches after settlement (partial=%s)',
    async (partial) => {
      const affected = [
        userKeys.user(input.userId),
        [...userKeys.list, { page: 0 }],
        studentEnrollmentKeys.page(input.userId, { page: 0 }),
        studentEnrollmentKeys.page(input.userId, { page: 1 }),
        structureKeys.enrollments('course-a'),
        structureKeys.enrollments('course-b'),
        [...structureKeys.course('course-a'), 'uz'],
        [...structureKeys.course('course-b'), 'en'],
        ['admin', 'problems', { page: 2 }],
      ]
      const unaffected = [
        studentEnrollmentKeys.user('student-b'),
        structureKeys.enrollments('course-c'),
        structureKeys.course('course-c'),
      ]
      for (const key of [...affected, ...unaffected]) queryClient.setQueryData(key, {})
      if (partial)
        intercept = (request) =>
          request.method === 'PUT' && request.url.includes('enrollment-b') ? refused() : undefined
      expect((await run()).status).toBe(partial ? 'partial' : 'complete')
      for (const key of affected) expect(queryClient.getQueryState(key)?.isInvalidated).toBe(true)
      for (const key of unaffected) expect(queryClient.getQueryState(key)?.isInvalidated).toBe(false)
    },
  )
})
