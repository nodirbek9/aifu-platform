import { createElement, type PropsWithChildren } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, renderHook } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { resetSessionForTests } from '@/shared/api/session'
import { builderKeys, useAddException, useDeleteException, type ExceptionRequest } from './queries'

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

const request = {
  kind: 'ADD',
  onDate: '2026-10-10',
  newPairSlotId: 'pair-slot',
  newRoomId: 'room',
  newTeacherUserId: 'teacher',
  reason: 'An additional class',
} satisfies ExceptionRequest

const affectedKeys = [
  builderKeys.exceptions('entry'),
  ['admin', 'week-preview', 'draft', { kind: 'group', id: 'group' }, '2026-10-10', 'uz'],
  ['admin', 'week-preview', 'published', { kind: 'teacher', id: 'teacher' }, '2026-10-12', 'ru'],
  ['admin', 'week', 'published', '2026-10-10', 'en'],
  [...builderKeys.load('draft'), { kind: 'group', id: 'group' }, 'uz'],
  [...builderKeys.load('published'), { kind: 'teacher', id: 'teacher' }, 'ru'],
  ['admin', 'teaching-load', 'term', 'en'],
  ['admin', 'courses', 'teacher-work', 'teacher', 'term', 'uz'],
  ['teacher', 'class-plan', 'entry', '2026-10-10', 'uz'],
  ['teacher', 'journal', 'course', 'ru'],
  ['timetable', 'week', 'student', '2026-10-10', 'uz'],
  ['timetable', 'today', 'teacher'],
  ['timetable', 'next', 'student', 'en'],
] as const
const unaffectedKeys = [builderKeys.exceptions('another-entry'), ['admin', 'users'], ['courses']] as const

function wrapper({ children }: PropsWithChildren) {
  return createElement(QueryClientProvider, { client }, children)
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

beforeEach(() => {
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  resetSessionForTests()
  vi.stubGlobal('Request', BrowserRequest)
  vi.stubGlobal('fetch', fetchMock)
  for (const key of [...affectedKeys, ...unaffectedKeys]) client.setQueryData(key, [])
})

afterEach(() => {
  cleanup()
  client.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

describe('one-off timetable changes', () => {
  it('sends the typed ADD payload unchanged, without a newDate, with entry and locale parameters', async () => {
    fetchMock.mockImplementation(async (input) => {
      if (!(input instanceof NativeRequest)) throw new Error('Expected an HTTP Request')
      expect(input.method).toBe('POST')
      const url = new URL(input.url)
      expect(url.pathname).toBe('/api/v1/admin/timetable-entries/entry/exceptions')
      expect(url.searchParams.get('locale')).toBe('ru')
      expect(await input.json()).toEqual(request)
      return json({ id: 'exception', ...request })
    })
    const { result } = renderHook(() => useAddException('entry', 'ru'), { wrapper })
    await act(async () => {
      await expect(result.current.mutateAsync(request)).resolves.toMatchObject({ kind: 'ADD' })
    })
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  for (const operation of ['add', 'delete'] as const) {
    for (const outcome of ['success', 'failure'] as const) {
      it(`${operation} invalidates all occurrence caches on ${outcome} settlement only`, async () => {
        let finish: ((response: Response) => void) | undefined
        fetchMock.mockImplementation((input) => {
          if (!(input instanceof NativeRequest)) throw new Error('Expected an HTTP Request')
          if (operation === 'delete') {
            expect(input.method).toBe('DELETE')
            expect(new URL(input.url).pathname).toBe('/api/v1/admin/timetable-exceptions/exception')
            expect(input.body).toBeNull()
          }
          return new Promise<Response>((resolve) => {
            finish = resolve
          })
        })
        const { result } = renderHook(
          () => ({ add: useAddException('entry', 'uz'), remove: useDeleteException('entry') }),
          { wrapper },
        )
        await act(async () => {
          const pending =
            operation === 'add'
              ? result.current.add.mutateAsync(request)
              : result.current.remove.mutateAsync('exception')
          // Observe the rejection immediately while the HTTP request is still pending.
          const settled = pending.then(
            () => 'success',
            () => 'failure',
          )
          await vi.waitFor(() => expect(finish).toBeDefined())
          for (const key of affectedKeys) expect(client.getQueryState(key)?.isInvalidated).toBe(false)
          if (finish === undefined) throw new Error('The mutation did not send a request')
          finish(
            outcome === 'success'
              ? operation === 'delete'
                ? new Response(null, { status: 204 })
                : json({ id: 'exception', ...request })
              : json({ code: 'TIMETABLE_CONFLICT', requestId: 'conflict-request' }, 409),
          )
          expect(await settled).toBe(outcome)
        })
        for (const key of affectedKeys)
          expect(client.getQueryState(key)?.isInvalidated, JSON.stringify(key)).toBe(true)
        for (const key of unaffectedKeys)
          expect(client.getQueryState(key)?.isInvalidated, JSON.stringify(key)).toBe(false)
        expect(fetchMock).toHaveBeenCalledTimes(1)
      })
    }
  }
})
