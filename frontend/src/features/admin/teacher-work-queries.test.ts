import { createElement, type PropsWithChildren } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, renderHook, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { structureKeys } from './structure-queries'
import {
  readTeacherCourses,
  teacherWeeklyHours,
  teacherWorkKeys,
  useTeacherTermLoadQuery,
  useTeacherUsersQuery,
  useTeacherWeekQuery,
} from './teacher-work-queries'

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
const fetchMock = vi.fn<typeof fetch>()
let client: QueryClient
type Schemas = components['schemas']
const input = { teacherId: 'teacher-a', termId: 'term-a', locale: 'uz' } satisfies Parameters<
  typeof readTeacherCourses
>[1]

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}
function url(request: RequestInfo | URL) {
  if (!(request instanceof NativeRequest)) throw new Error('Expected HTTP Request')
  return new URL(request.url)
}
function wrapper({ children }: PropsWithChildren) {
  return createElement(QueryClientProvider, { client }, children)
}
function read(signal = new AbortController().signal) {
  return readTeacherCourses(client, input, signal)
}

beforeEach(() => {
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  resetSessionForTests()
  vi.stubGlobal('Request', BrowserRequest)
  vi.stubGlobal('fetch', fetchMock)
})
afterEach(() => {
  cleanup()
  client.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

describe('teacher assignments', () => {
  it('finds an unscheduled assignment beyond the first 100 courses, matching IDs despite duplicate names', async () => {
    fetchMock.mockImplementation(async (request) => {
      const requestUrl = url(request)
      if (requestUrl.pathname === '/api/v1/admin/courses') {
        expect(requestUrl.searchParams.get('termId')).toBe('term-a')
        expect(requestUrl.searchParams.get('locale')).toBe('uz')
        expect(requestUrl.searchParams.get('size')).toBe('100')
        const first = requestUrl.searchParams.get('page') === '0'
        return await Promise.resolve(
          json({
            content: first
              ? Array.from({ length: 100 }, (_, i) => ({ id: `course-${i}` }))
              : [{ id: 'unscheduled' }],
            hasNext: first,
          } satisfies Schemas['PageResponseCourseSummary']),
        )
      }
      const id = requestUrl.pathname.split('/').at(-1)
      return json({
        id,
        teachers: [{ userId: id === 'unscheduled' ? 'teacher-a' : 'teacher-b', fullName: 'Same name' }],
      } satisfies Schemas['CourseDetail'])
    })
    expect((await read()).map((course) => course.id)).toEqual(['unscheduled'])
    expect(fetchMock).toHaveBeenCalledTimes(103)
  })

  it('reuses cached details and bounds concurrent detail requests to four', async () => {
    client.setQueryData(structureKeys.course('cached'), {
      id: 'cached',
      teachers: [{ userId: 'teacher-a' }],
    } satisfies Schemas['CourseDetail'])
    let active = 0
    let peak = 0
    fetchMock.mockImplementation(async (request) => {
      const requestUrl = url(request)
      if (requestUrl.pathname === '/api/v1/admin/courses') {
        return json({
          content: [{ id: 'cached' }, ...Array.from({ length: 12 }, (_, i) => ({ id: `c-${i}` }))],
          hasNext: false,
        })
      }
      active++
      peak = Math.max(peak, active)
      await new Promise((resolve) => setTimeout(resolve, 5))
      active--
      return json({ teachers: [] })
    })
    expect((await read()).map((course) => course.id)).toEqual(['cached'])
    expect(peak).toBe(4)
    expect(fetchMock).toHaveBeenCalledTimes(13)
  })

  it('fails the whole result when a later catalogue page fails', async () => {
    fetchMock.mockResolvedValueOnce(json({ content: [], hasNext: true }))
    fetchMock.mockResolvedValueOnce(json({ code: 'INTERNAL_ERROR', requestId: 'page-failure' }, 500))
    await expect(read()).rejects.toMatchObject({ problem: { requestId: 'page-failure' } })
  })

  it('propagates cancellation to detail requests and starts no more requests', async () => {
    const controller = new AbortController()
    fetchMock.mockImplementation(async (request) => {
      if (!(request instanceof NativeRequest)) throw new Error('Expected HTTP Request')
      if (url(request).pathname === '/api/v1/admin/courses')
        return await Promise.resolve(json({ content: [{ id: 'c1' }, { id: 'c2' }], hasNext: true }))
      controller.abort()
      expect(request.signal.aborted).toBe(true)
      request.signal.throwIfAborted()
      return json({})
    })
    await expect(read(controller.signal)).rejects.toBeDefined()
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })

  it('isolates assignment keys by teacher, term and locale', () => {
    const key = teacherWorkKeys.courses(input)
    expect(key).not.toEqual(teacherWorkKeys.courses({ ...input, teacherId: 'teacher-b' }))
    expect(key).not.toEqual(teacherWorkKeys.courses({ ...input, termId: 'term-b' }))
    expect(key).not.toEqual(teacherWorkKeys.courses({ ...input, locale: 'ru' }))
  })
})

describe('concrete weekly hours', () => {
  it('counts held occurrences once and excludes the replaced original teacher and cancelled classes', () => {
    const lesson = {
      entryId: 'entry',
      date: '2026-10-09',
      teacherUserId: 'teacher-a',
      status: 'SCHEDULED',
    } satisfies Schemas['ClassView']
    const week = {
      days: [
        {
          classes: [
            lesson,
            lesson,
            { ...lesson, date: '2026-10-10', status: 'MOVED_IN' },
            { ...lesson, entryId: 'replacement', status: 'REPLACED', teacherUserId: 'teacher-b' },
            { ...lesson, entryId: 'cancelled', status: 'CANCELLED' },
            { ...lesson, entryId: 'holiday', status: 'HOLIDAY' },
            { ...lesson, entryId: 'original', status: 'MOVED_OUT' },
            { teacherUserId: 'teacher-a', status: 'SCHEDULED' },
          ],
        },
      ],
    } satisfies Schemas['WeekView']
    expect(teacherWeeklyHours(week, 'teacher-a')).toBe(4)
    expect(teacherWeeklyHours(week, 'teacher-b')).toBe(2)
    expect(teacherWeeklyHours({}, 'teacher-a')).toBe(0)
  })

  it('serializes teacher, date and locale separately from term load', async () => {
    fetchMock.mockImplementation((request) =>
      Promise.resolve(json(url(request).pathname.endsWith('/week') ? { days: [] } : [])),
    )
    const week = renderHook(() => useTeacherWeekQuery('tt', 'teacher-a', '2026-10-09', 'ru'), { wrapper })
    const load = renderHook(() => useTeacherTermLoadQuery('tt', 'teacher-a', 'ru'), { wrapper })
    await waitFor(() => expect(week.result.current.isSuccess && load.result.current.isSuccess).toBe(true))
    const urls = fetchMock.mock.calls.map(([request]) => url(request))
    expect(urls.find((u) => u.pathname.endsWith('/week'))?.searchParams.toString()).toBe(
      'teacherId=teacher-a&date=2026-10-09&locale=ru',
    )
    expect(urls.find((u) => u.pathname.endsWith('/load'))?.searchParams.toString()).toBe(
      'teacherId=teacher-a&locale=ru',
    )
  })

  it('presets the teacher role while preserving user filters and pagination', async () => {
    fetchMock.mockResolvedValue(json({ content: [] }))
    const { result } = renderHook(() => useTeacherUsersQuery({ query: 'name', status: 'ACTIVE', page: 2 }), {
      wrapper,
    })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    const request = fetchMock.mock.calls[0]?.[0]
    if (request === undefined) throw new Error('Expected request')
    expect(Object.fromEntries(url(request).searchParams)).toMatchObject({
      role: 'TEACHER',
      query: 'name',
      status: 'ACTIVE',
      page: '2',
      size: '25',
    })
  })
})
