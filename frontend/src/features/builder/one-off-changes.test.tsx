import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { resetSessionForTests } from '@/shared/api/session'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import type { components } from '@/shared/api/schema'
import { OneOffChanges } from './one-off-changes'

const { BrowserRequest } = vi.hoisted(() => {
  const NativeRequest = globalThis.Request
  class BrowserRequest extends NativeRequest {
    constructor(input: RequestInfo | URL, init?: RequestInit) {
      super(typeof input === 'string' ? new URL(input, 'http://localhost') : input, init)
    }
  }
  vi.stubGlobal('Request', BrowserRequest)
  return { BrowserRequest }
})

const slots = [
  { id: 'slot', pairNo: 1, shift: 'MORNING', shiftPairNo: 1, startsAt: '08:30', endsAt: '09:50' },
] satisfies components['schemas']['PairSlotView'][]
const fetchMock = vi.fn<typeof fetch>()
let client: QueryClient
let writes: unknown[]
let respond: () => Promise<Response>
function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  resetSessionForTests()
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  writes = []
  respond = () => Promise.resolve(json({ id: 'change', kind: 'ADD', onDate: '2026-10-10' }))
  vi.stubGlobal('Request', BrowserRequest)
  vi.stubGlobal('fetch', fetchMock)
  Object.defineProperty(Element.prototype, 'scrollIntoView', { configurable: true, value: () => undefined })
  fetchMock.mockImplementation(async (input) => {
    if (!(input instanceof Request)) throw new Error('Expected a typed client request')
    const path = new URL(input.url).pathname
    if (input.method === 'POST') {
      writes.push(await input.json())
      return respond()
    }
    if (path.endsWith('/pair-slots')) return json(slots)
    if (path.endsWith('/rooms')) return json([{ id: 'room', label: 'Test room' }])
    if (path.endsWith('/users')) return json({ content: [{ id: 'teacher', fullName: 'Test teacher' }] })
    if (path.endsWith('/exceptions')) return json([])
    throw new Error(`Unexpected request: ${path}`)
  })
})

afterEach(() => {
  cleanup()
  client.clear()
  fetchMock.mockReset()
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
})

function open(props: React.ComponentProps<typeof OneOffChanges> = {}) {
  return render(
    <QueryClientProvider client={client}>
      <OneOffChanges entryId="entry" initialKind="ADD" selectedDate="2026-10-10" {...props} />
    </QueryClientProvider>,
  )
}

async function choose(label: string, option: string | RegExp) {
  const user = userEvent.setup()
  screen.getByRole('combobox', { name: label }).focus()
  await user.keyboard('{Enter}')
  await user.click(await screen.findByRole('option', { name: option }))
}

async function complete() {
  await choose(uz.builder.pair, /08:30/)
  await userEvent.setup().type(screen.getByLabelText(uz.admin.reason), 'Additional teaching')
}

describe('single class form (7.5-F2)', () => {
  it('prefills the selected date and requires a pair and reason, without an original occurrence', async () => {
    open()
    expect(screen.getByLabelText(uz.builder.singleClassDate)).toHaveValue('2026-10-10')
    const button = screen.getByRole('button', { name: uz.builder.addSingleClass })
    expect(button).toBeDisabled()
    await complete()
    await userEvent.setup().click(button)
    await waitFor(() =>
      expect(writes).toEqual([
        { kind: 'ADD', onDate: '2026-10-10', newPairSlotId: 'slot', reason: 'Additional teaching' },
      ]),
    )
    expect(await screen.findByText(uz.builder.singleClassAdded)).toBeVisible()
  })

  it('sends optional overrides and allows reverting them to the original teacher and room', async () => {
    open()
    await complete()
    await choose(uz.builder.replacementTeacher, 'Test teacher')
    await choose(uz.admin.room, 'Test room')
    await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addSingleClass }))
    await waitFor(() => expect(writes[0]).toMatchObject({ newTeacherUserId: 'teacher', newRoomId: 'room' }))
    await userEvent.setup().type(screen.getByLabelText(uz.builder.singleClassDate), '2026-10-11')
    await userEvent.setup().type(screen.getByLabelText(uz.admin.reason), 'Another class')
    await choose(uz.builder.replacementTeacher, 'Test teacher')
    await choose(uz.builder.replacementTeacher, uz.builder.sameTeacher)
    await choose(uz.admin.room, 'Test room')
    await choose(uz.admin.room, uz.builder.sameRoom)
    await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addSingleClass }))
    await waitFor(() => expect(writes).toHaveLength(2))
    expect(writes[1]).not.toHaveProperty('newTeacherUserId')
    expect(writes[1]).not.toHaveProperty('newRoomId')
  })

  it('preserves inputs after a conflict, shows its details and request ID, and permits a corrected retry', async () => {
    respond = () =>
      Promise.resolve(
        json(
          {
            code: 'TIMETABLE_CONFLICT',
            requestId: 'request-f2',
            details: {
              conflicts: [
                {
                  type: 'TEACHER',
                  courseTitle: 'Conflicting course',
                  teacherName: 'Busy teacher',
                  pairNo: 1,
                },
              ],
            },
          },
          409,
        ),
      )
    open()
    await complete()
    await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addSingleClass }))
    expect(await screen.findByText('request-f2')).toBeVisible()
    expect(screen.getByText(/Conflicting course/)).toHaveTextContent('Busy teacher')
    expect(screen.getByLabelText(uz.admin.reason)).toHaveValue('Additional teaching')
    expect(screen.getByLabelText(uz.builder.singleClassDate)).toHaveValue('2026-10-10')
    respond = () => Promise.resolve(json({ id: 'change', kind: 'ADD' }))
    await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addSingleClass }))
    expect(await screen.findByText(uz.builder.singleClassAdded)).toBeVisible()
    expect(writes).toHaveLength(2)
  })

  it('blocks repeated submissions while the request is pending', async () => {
    let finish: ((response: Response) => void) | undefined
    respond = () =>
      new Promise((resolve) => {
        finish = resolve
      })
    open()
    await complete()
    const button = screen.getByRole('button', { name: uz.builder.addSingleClass })
    await userEvent.setup().dblClick(button)
    await waitFor(() => expect(writes).toHaveLength(1))
    expect(button).toBeDisabled()
    await act(async () => {
      finish?.(json({ id: 'change' }))
      await Promise.resolve()
    })
  })

  it('offers a source selector and an actionable empty state without posting an empty entry ID', async () => {
    open({ entryId: undefined, sourceClasses: [] })
    expect(screen.getByText(uz.builder.noSourceClass)).toBeVisible()
    expect(screen.getByRole('combobox', { name: uz.builder.sourceClass })).toHaveTextContent(
      uz.builder.chooseSourceClass,
    )
    await complete()
    expect(screen.getByRole('button', { name: uz.builder.addSingleClass })).toBeDisabled()
    expect(writes).toHaveLength(0)
  })

  it('uses the selected source entry when provided by the preview', async () => {
    open({ entryId: undefined, sourceClasses: [{ value: 'source', label: 'Test source class' }] })
    await choose(uz.builder.sourceClass, 'Test source class')
    await complete()
    await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addSingleClass }))
    await waitFor(() => expect(writes).toHaveLength(1))
    expect(
      fetchMock.mock.calls.some(
        ([input]) =>
          input instanceof Request && input.method === 'POST' && input.url.includes('/source/exceptions'),
      ),
    ).toBe(true)
  })

  for (const kind of ['CANCELLED', 'MOVED', 'REPLACED'] as const) {
    it(`preserves the ${kind} payload and omits ADD-only fields`, async () => {
      open({ initialKind: kind })
      if (kind === 'MOVED') {
        await userEvent.setup().type(screen.getByLabelText(uz.builder.newDate), '2026-10-12')
        await choose(uz.builder.pair, /08:30/)
      }
      if (kind === 'REPLACED') await choose(uz.admin.room, 'Test room')
      await userEvent.setup().type(screen.getByLabelText(uz.admin.reason), 'Existing behavior')
      await userEvent.setup().click(screen.getByRole('button', { name: uz.builder.addChange }))
      await waitFor(() =>
        expect(writes).toEqual([
          {
            kind,
            onDate: '2026-10-10',
            reason: 'Existing behavior',
            ...(kind === 'MOVED' && { newDate: '2026-10-12', newPairSlotId: 'slot' }),
            ...(kind === 'REPLACED' && { newRoomId: 'room' }),
          },
        ]),
      )
    })
  }
})
