import { createElement, type PropsWithChildren } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, renderHook, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { ApiFailure } from '@/shared/api/errors'
import { useAddGroupStudents, referenceKeys } from './reference-queries'
import { structureKeys } from './structure-queries'
import { enrollmentTransitions, type EnrollmentStatus } from './student-enrollment-model'
import {
  studentEnrollmentKeys,
  useChangeEnrollmentStatus,
  useStudentEnrollmentsQuery,
} from './student-enrollment-queries'

const fetchMock = vi.fn<typeof fetch>()
const { NativeRequest, BrowserRequest } = vi.hoisted(() => {
  const NativeRequest = globalThis.Request
  // Install before the real OpenAPI client captures Request at module initialization.
  class BrowserRequest extends NativeRequest {
    constructor(input: RequestInfo | URL, init?: RequestInit) {
      super(typeof input === 'string' ? new URL(input, 'http://localhost') : input, init)
    }
  }
  vi.stubGlobal('Request', BrowserRequest)
  return { NativeRequest, BrowserRequest }
})
let queryClient: QueryClient

function wrapper({ children }: PropsWithChildren) {
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

function requestAt(index = 0): Request {
  const request = fetchMock.mock.calls[index]?.[0]
  if (!(request instanceof NativeRequest)) throw new Error('Expected an HTTP Request')
  return request
}

const emptyPage = {
  content: [],
  page: 0,
  size: 25,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
} satisfies components['schemas']['PageResponseEnrollmentSummary']

beforeEach(() => {
  queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  resetSessionForTests()
  fetchMock.mockImplementation(() => Promise.resolve(json(emptyPage)))
  vi.stubGlobal('fetch', fetchMock)
  // Node's Request requires an absolute URL; the browser resolves the API's relative paths.
  vi.stubGlobal('Request', BrowserRequest)
})

afterEach(() => {
  cleanup()
  queryClient.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

describe('enrollment transitions', () => {
  it.each<{ status: EnrollmentStatus; choices: EnrollmentStatus[] }>([
    { status: 'PENDING', choices: ['ACTIVE', 'WITHDRAWN'] },
    { status: 'ACTIVE', choices: ['SUSPENDED', 'COMPLETED', 'FAILED', 'WITHDRAWN'] },
    { status: 'SUSPENDED', choices: ['ACTIVE', 'WITHDRAWN'] },
    { status: 'WITHDRAWN', choices: ['ACTIVE'] },
    { status: 'FAILED', choices: ['ACTIVE'] },
    { status: 'COMPLETED', choices: [] },
  ])('$status exposes only its valid destinations', ({ status, choices }) => {
    expect(enrollmentTransitions(status)).toEqual(choices)
  })
})

describe('student enrollment reads', () => {
  it('serializes the user, intersection, status, pagination and repeated sorting parameters', async () => {
    const filter = {
      courseId: 'course-a',
      status: 'SUSPENDED',
      page: 2,
      size: 10,
      sort: ['enrolledAt,desc', 'id,asc'],
    } satisfies Parameters<typeof useStudentEnrollmentsQuery>[1]
    const { result } = renderHook(() => useStudentEnrollmentsQuery('student-a', filter), { wrapper })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(emptyPage)
    const request = requestAt()
    const url = new URL(request.url)
    expect(request.method).toBe('GET')
    expect(url.pathname).toBe('/api/v1/admin/enrollments')
    expect(Object.fromEntries(url.searchParams)).toMatchObject({
      userId: 'student-a',
      courseId: 'course-a',
      status: 'SUSPENDED',
      page: '2',
      size: '10',
    })
    expect(url.searchParams.getAll('sort')).toEqual(filter.sort)
  })

  it('uses a user-only filter with default pagination and leaves default ordering to the server', async () => {
    const { result } = renderHook(() => useStudentEnrollmentsQuery('student-a'), { wrapper })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(Object.fromEntries(new URL(requestAt().url).searchParams)).toEqual({
      userId: 'student-a',
      page: '0',
      size: '25',
    })
  })

  it('does not request enrollments without a student', () => {
    const { result } = renderHook(() => useStudentEnrollmentsQuery(''), { wrapper })
    expect(result.current.fetchStatus).toBe('idle')
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('isolates cache entries by user, course, status, page, size and sorting', async () => {
    const filters = [
      {},
      { courseId: 'course-a' },
      { status: 'ACTIVE' },
      { page: 1 },
      { size: 10 },
      { sort: ['id,asc'] },
    ] satisfies Parameters<typeof useStudentEnrollmentsQuery>[1][]
    const views = filters.map((filter) =>
      renderHook(() => useStudentEnrollmentsQuery('student-a', filter), { wrapper }),
    )
    views.push(renderHook(() => useStudentEnrollmentsQuery('student-b'), { wrapper }))
    await waitFor(() => expect(views.every((view) => view.result.current.isSuccess)).toBe(true))
    expect(fetchMock).toHaveBeenCalledTimes(7)
    expect(queryClient.getQueryCache().getAll()).toHaveLength(7)
  })

  it('never shows the previous student data while another student is loading', async () => {
    const page = {
      ...emptyPage,
      content: [{ id: 'enrollment-a', studentUserId: 'student-a', status: 'ACTIVE' }],
    } satisfies components['schemas']['PageResponseEnrollmentSummary']
    fetchMock.mockResolvedValueOnce(json(page))
    const { result, rerender } = renderHook(({ userId }) => useStudentEnrollmentsQuery(userId), {
      initialProps: { userId: 'student-a' },
      wrapper,
    })
    await waitFor(() => expect(result.current.data).toEqual(page))
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>(() => {
          // Keep the second student's request pending to inspect the loading state.
        }),
    )
    rerender({ userId: 'student-b' })
    expect(result.current.data).toBeUndefined()
    expect(result.current.isPlaceholderData).toBe(false)
  })
})

describe('enrollment status writes', () => {
  it.each([undefined, '', 'Registrar correction: oʻqish & review?'])(
    'sends status and optional reason (%s) as query parameters without a JSON body',
    async (reason) => {
      fetchMock.mockResolvedValue(json({ id: 'enrollment-a', status: 'WITHDRAWN' }))
      const { result } = renderHook(() => useChangeEnrollmentStatus(), { wrapper })
      await act(async () => {
        await result.current.mutateAsync({
          id: 'enrollment-a',
          courseId: 'course-a',
          studentUserId: 'student-a',
          status: 'WITHDRAWN',
          reason,
        })
      })
      const request = requestAt()
      const url = new URL(request.url)
      expect(request.method).toBe('PUT')
      expect(url.pathname).toBe('/api/v1/admin/enrollments/enrollment-a/status')
      expect(url.searchParams.get('status')).toBe('WITHDRAWN')
      expect(url.searchParams.get('reason')).toBe(reason || null)
      expect([...url.searchParams.keys()]).toEqual(reason ? ['status', 'reason'] : ['status'])
      expect(request.body).toBeNull()
      expect(request.headers.has('Content-Type')).toBe(false)
    },
  )

  it.each([200, 409])(
    'refreshes affected caches after a %i response, preserving unrelated caches',
    async (status) => {
      const affected = [
        studentEnrollmentKeys.page('student-a', { page: 0 }),
        studentEnrollmentKeys.page('student-a', { page: 1, status: 'ACTIVE' }),
        structureKeys.enrollments('course-a'),
        [...structureKeys.enrollments('course-a'), { page: 1 }],
        ['admin', 'problems', { type: 'INACTIVE_USER', page: 0 }],
      ]
      const unaffected = [
        studentEnrollmentKeys.page('student-b', { page: 0 }),
        structureKeys.enrollments('course-b'),
        ['admin', 'users', 'student-b'],
      ]
      for (const key of [...affected, ...unaffected]) queryClient.setQueryData(key, emptyPage)
      fetchMock.mockResolvedValue(
        status === 200
          ? json({ id: 'enrollment-a', status: 'SUSPENDED' })
          : json({ code: 'ENROLLMENT_INVALID_TRANSITION', requestId: 'request-a' }, status),
      )
      const { result } = renderHook(() => useChangeEnrollmentStatus(), { wrapper })
      await act(async () => {
        const mutation = result.current.mutateAsync({
          id: 'enrollment-a',
          courseId: 'course-a',
          studentUserId: 'student-a',
          status: 'SUSPENDED',
        })
        if (status === 200) await mutation
        else await expect(mutation).rejects.toBeInstanceOf(ApiFailure)
      })
      for (const key of affected) expect(queryClient.getQueryState(key)?.isInvalidated).toBe(true)
      for (const key of unaffected) expect(queryClient.getQueryState(key)?.isInvalidated).toBe(false)
    },
  )
})

describe('group moves', () => {
  it('preserves the existing request and refreshes user details plus old/new admin and teacher rosters', async () => {
    const affected = [
      referenceKeys.groups,
      referenceKeys.groupStudents('group-old'),
      referenceKeys.groupStudents('group-new'),
      ['groups'],
      ['groups', 'group-old', 'students'],
      ['groups', 'group-new', 'students'],
      ['admin', 'users', 'student-a'],
      ['admin', 'users', 'student-b'],
    ]
    const unrelatedUser = ['admin', 'users', 'student-c']
    for (const key of [...affected, unrelatedUser]) queryClient.setQueryData(key, {})
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))
    const { result } = renderHook(() => useAddGroupStudents('group-new'), { wrapper })
    await act(async () => {
      await result.current.mutateAsync(['student-a', 'student-b'])
    })
    const request = requestAt()
    expect(request.method).toBe('POST')
    expect(new URL(request.url).pathname).toBe('/api/v1/admin/groups/group-new/students')
    expect(await request.json()).toEqual({ userIds: ['student-a', 'student-b'] })
    for (const key of affected) expect(queryClient.getQueryState(key)?.isInvalidated).toBe(true)
    expect(queryClient.getQueryState(unrelatedUser)?.isInvalidated).toBe(false)
  })
})
