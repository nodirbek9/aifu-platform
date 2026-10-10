import { createElement, type PropsWithChildren } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import { MemoryRouter, useSearchParams } from 'react-router'
import { afterEach, describe, expect, it, vi } from 'vitest'
import createClient from 'openapi-fetch'
import { api } from '@/shared/api/client'
import type { paths } from '@/shared/api/schema'
import { ApiFailure } from '@/shared/api/errors'
import {
  getProblemAction,
  narrowProblemPage,
  narrowProblemView,
  readProblemType,
  type AdminProblemView,
} from './problems-model'
import { readProblemsFilter, updateProblemsSearch, useAdminProblemsQuery } from './problems-queries'

vi.mock(import('@/shared/api/client'), async (importOriginal) => {
  const actual = await importOriginal()
  // Resolve relative API paths as the browser would, using the real typed client.
  return { ...actual, api: createClient<paths>({ baseUrl: 'http://localhost', fetch: actual.authFetch }) }
})

afterEach(() => {
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
})

function queryWrapper({ children }: PropsWithChildren) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return createElement(
    QueryClientProvider,
    { client },
    createElement(
      MemoryRouter,
      {
        initialEntries: ['/admin/problems?type=LOCKED_ACCOUNT&page=2&size=10&other=keep'],
      },
      children,
    ),
  )
}

function mockProblemsResponse(body: unknown) {
  const fetch = vi.fn<typeof globalThis.fetch>().mockResolvedValue(
    new Response(JSON.stringify(body), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    }),
  )
  vi.stubGlobal('fetch', fetch)
  return fetch
}

function lastProblemsUrl(fetch: ReturnType<typeof mockProblemsResponse>) {
  const request = fetch.mock.lastCall?.[0]
  if (!(request instanceof Request)) throw new Error('Expected an API request')
  return new URL(request.url)
}

describe('problems query', () => {
  const emptyPage = {
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  }

  it('loads the filtered page through the typed client and keeps server totals', async () => {
    const page = {
      ...emptyPage,
      content: [{ problemKey: 'LOCKED_ACCOUNT:user', type: 'LOCKED_ACCOUNT', userId: 'user' }],
      page: 2,
      totalElements: 42,
      totalPages: 5,
      hasNext: true,
      hasPrevious: true,
    }
    const fetch = mockProblemsResponse(page)
    const { result } = renderHook(() => useAdminProblemsQuery(), { wrapper: queryWrapper })
    expect(result.current.isPending).toBe(true)
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    const url = lastProblemsUrl(fetch)
    expect(url.pathname).toBe('/api/v1/admin/problems')
    expect(Object.fromEntries(url.searchParams)).toEqual({
      type: 'LOCKED_ACCOUNT',
      page: '2',
      size: '10',
    })
    expect(result.current.data).toEqual(page)
  })

  it('updates URL filters, resets the page and fetches the newly selected type', async () => {
    const fetch = mockProblemsResponse(emptyPage)
    const { result } = renderHook(
      () => {
        const query = useAdminProblemsQuery()
        const [search] = useSearchParams()
        return { query, search }
      },
      { wrapper: queryWrapper },
    )
    await waitFor(() => expect(result.current.query.isSuccess).toBe(true))
    act(() => result.current.query.updateParams({ type: 'COURSE_WITHOUT_TEACHER' }))
    await waitFor(() =>
      expect(Object.fromEntries(lastProblemsUrl(fetch).searchParams)).toEqual({
        type: 'COURSE_WITHOUT_TEACHER',
        page: '0',
        size: '10',
      }),
    )
    expect(result.current.search.toString()).toBe('type=COURSE_WITHOUT_TEACHER&size=10&other=keep')
    act(() => result.current.query.updateParams({ page: 1 }))
    await waitFor(() =>
      expect(Object.fromEntries(lastProblemsUrl(fetch).searchParams)).toEqual({
        type: 'COURSE_WITHOUT_TEACHER',
        page: '1',
        size: '10',
      }),
    )
    expect(result.current.search.get('page')).toBe('1')
    expect(result.current.search.get('other')).toBe('keep')
  })

  it('returns an honest empty page without inventing rows or totals', async () => {
    mockProblemsResponse(emptyPage)
    const { result } = renderHook(() => useAdminProblemsQuery(), { wrapper: queryWrapper })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(emptyPage)
  })

  it('treats an invalid response as an error and supports retry', async () => {
    const fetch = mockProblemsResponse(emptyPage)
    fetch.mockResolvedValueOnce(
      new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } }),
    )
    const { result } = renderHook(() => useAdminProblemsQuery(), { wrapper: queryWrapper })
    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.data).toBeUndefined()
    await act(async () => {
      await result.current.refetch()
    })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(emptyPage)
  })

  it('retains API errors and requestId for the error state', async () => {
    vi.spyOn(api, 'GET').mockResolvedValue({
      error: { code: 'AUTH_FORBIDDEN', requestId: 'request-123' },
      response: new Response(null, { status: 403 }),
    })
    const { result } = renderHook(() => useAdminProblemsQuery(), { wrapper: queryWrapper })
    await waitFor(() => expect(result.current.isError).toBe(true))
    expect(result.current.error).toBeInstanceOf(ApiFailure)
    expect(result.current.error).toMatchObject({
      problem: { code: 'AUTH_FORBIDDEN', requestId: 'request-123' },
    })
  })
})

const destinations: { problem: AdminProblemView; path: string }[] = [
  {
    problem: { problemKey: 'group:user', type: 'STUDENT_WITHOUT_GROUP', userId: 'user' },
    path: '/admin/users/user#group',
  },
  {
    problem: { problemKey: 'timetable:group', type: 'GROUP_WITHOUT_PUBLISHED_ENTRIES', groupId: 'group' },
    path: '/admin/timetable?view=group&id=group',
  },
  {
    problem: { problemKey: 'teacher:course', type: 'COURSE_WITHOUT_TEACHER', courseId: 'course' },
    path: '/admin/structure/courses/course',
  },
  {
    problem: { problemKey: 'enrollment:user', type: 'ACTIVE_ENROLLMENT_INACTIVE_USER', userId: 'user' },
    path: '/admin/users/user#enrollments',
  },
  {
    problem: { problemKey: 'locked:user', type: 'LOCKED_ACCOUNT', userId: 'user' },
    path: '/admin/users/user#status',
  },
  {
    problem: {
      problemKey: 'midterm:enrollment',
      type: 'MIDTERM_ATTEMPTS_EXHAUSTED',
      courseId: 'course',
      enrollmentId: 'enrollment',
      quizId: 'quiz',
      grantAvailable: true,
    },
    path: '/admin/learning-path?course=course&enrollment=enrollment&quiz=quiz',
  },
]

describe('problem actions', () => {
  it.each(destinations)('maps $problem.type to an internal destination', ({ problem, path }) => {
    expect(getProblemAction(problem)).toMatchObject({ type: 'navigate', path })
  })
  it.each(destinations)('disables $problem.type without its identifiers', ({ problem }) => {
    for (const id of ['userId', 'groupId', 'courseId', 'enrollmentId', 'quizId'] as const) {
      if (problem[id] === undefined) continue
      for (const missing of [undefined, '', '   ']) {
        expect(getProblemAction({ ...problem, [id]: missing })).toEqual({
          type: 'unavailable',
          label: 'admin.problems.actionUnavailable',
        })
      }
    }
  })
  it('offers review when grant availability is false or absent', () => {
    const problem = destinations[5]?.problem
    if (!problem) throw new Error('Missing midterm case')
    for (const grantAvailable of [false, undefined]) {
      expect(getProblemAction({ ...problem, grantAvailable })).toMatchObject({
        type: 'navigate',
        label: 'admin.problems.reviewAttempt',
      })
    }
    expect(getProblemAction(problem)).toMatchObject({ label: 'admin.problems.fix' })
  })
  it('encodes identifiers without altering the internal destination', () => {
    expect(
      getProblemAction({ problemKey: 'key', type: 'LOCKED_ACCOUNT', userId: 'u/other?x#y' }),
    ).toMatchObject({ path: '/admin/users/u%2Fother%3Fx%23y#status' })
    expect(
      getProblemAction({
        problemKey: 'key',
        type: 'MIDTERM_ATTEMPTS_EXHAUSTED',
        courseId: 'c&x',
        enrollmentId: 'e#x',
        quizId: 'q?x',
      }),
    ).toMatchObject({ path: '/admin/learning-path?course=c%26x&enrollment=e%23x&quiz=q%3Fx' })
  })
})

describe('problem response boundary', () => {
  const page = {
    content: [destinations[0]?.problem],
    page: 1,
    size: 20,
    totalElements: 42,
    totalPages: 3,
    hasNext: true,
    hasPrevious: true,
  }
  it('preserves server totals and optional fields', () => {
    const problem: AdminProblemView = {
      problemKey: 'key',
      type: 'MIDTERM_ATTEMPTS_EXHAUSTED',
      fullName: 'Student',
      attemptsUsed: 3,
      attemptsAllowed: 3,
      grantAvailable: false,
      subjectCode: 'CS',
      lockedUntil: '2026-10-10T12:00:00Z',
    }
    expect(narrowProblemView(problem)).toEqual(problem)
    expect(narrowProblemPage({ ...page, content: [problem] })).toEqual({ ...page, content: [problem] })
  })
  it('keeps incomplete rows for the unavailable action state', () => {
    expect(getProblemAction(narrowProblemView({ problemKey: 'key', type: 'LOCKED_ACCOUNT' })).type).toBe(
      'unavailable',
    )
    expect(narrowProblemPage({ ...page, content: [] }).content).toEqual([])
  })
  it('rejects missing identity and unknown types', () => {
    expect(() => narrowProblemView({})).toThrow('Invalid problem view')
    expect(() => narrowProblemView({ problemKey: ' ', type: 'LOCKED_ACCOUNT' })).toThrow(
      'Invalid problem view',
    )
    for (const content of [[{ problemKey: 'key', type: 'UNKNOWN' }], [{}]])
      expect(() => narrowProblemPage({ ...page, content })).toThrow()
    expect(readProblemType('UNKNOWN')).toBeUndefined()
  })
  it('rejects invalid envelopes instead of fabricating totals', () => {
    for (const raw of [
      null,
      {},
      { ...page, totalElements: undefined },
      { ...page, page: -1 },
      { ...page, content: 'invalid' },
      { ...page, size: 101 },
    ])
      expect(() => narrowProblemPage(raw)).toThrow()
  })
})

describe('URL filters', () => {
  it('restores filters after reload', () => {
    expect(readProblemsFilter(new URLSearchParams('type=LOCKED_ACCOUNT&page=2&size=50'))).toEqual({
      type: 'LOCKED_ACCOUNT',
      page: 2,
      size: 50,
    })
  })
  it('validates type and pagination and caps size', () => {
    for (const page of ['-1', 'bad', '1.5', 'Infinity', '9007199254740992'])
      expect(readProblemsFilter(new URLSearchParams({ type: 'UNKNOWN', page, size: '0' }))).toEqual({
        type: undefined,
        page: 0,
        size: 20,
      })
    expect(readProblemsFilter(new URLSearchParams('size=200')).size).toBe(100)
    expect(
      readProblemsFilter(new URLSearchParams('page=2'), { type: 'LOCKED_ACCOUNT', page: 3, size: 10 }),
    ).toEqual({ type: 'LOCKED_ACCOUNT', page: 3, size: 10 })
  })
  it('resets page on filter changes and preserves unrelated parameters', () => {
    const search = new URLSearchParams('type=LOCKED_ACCOUNT&page=3&size=50&other=keep')
    expect(updateProblemsSearch(search, { type: 'COURSE_WITHOUT_TEACHER' }).toString()).toBe(
      'type=COURSE_WITHOUT_TEACHER&size=50&other=keep',
    )
    expect(updateProblemsSearch(search, { type: null, size: 20 }).toString()).toBe('other=keep')
    expect(updateProblemsSearch(search, { size: 10, page: 9 }).get('page')).toBeNull()
    expect(search.get('page')).toBe('3')
  })
  it('retains filters when paging and removes default values', () => {
    const search = new URLSearchParams('type=LOCKED_ACCOUNT&size=10&page=3')
    expect(updateProblemsSearch(search, { page: 2 }).toString()).toBe('type=LOCKED_ACCOUNT&size=10&page=2')
    expect(updateProblemsSearch(search, { page: 0 }).toString()).toBe('type=LOCKED_ACCOUNT&size=10')
  })
})
