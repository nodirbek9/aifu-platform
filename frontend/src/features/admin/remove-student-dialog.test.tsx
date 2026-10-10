import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import { RemoveStudentDialog } from './remove-student-dialog'

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
let client: QueryClient
let account: Schemas['UserDetail']
let rows: Schemas['EnrollmentDetail'][]
let writes: Request[]
let intercept: ((request: Request) => Promise<Response> | undefined) | undefined
const close = vi.fn()

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function server(request: Request): Promise<Response> {
  const url = new URL(request.url)
  if (url.pathname === '/api/v1/admin/enrollments') {
    const page = Number(url.searchParams.get('page'))
    return Promise.resolve(
      json({
        content: rows.slice(page * 100, (page + 1) * 100),
        hasNext: rows.length > (page + 1) * 100,
      }),
    )
  }
  if (url.pathname.includes('/users/')) {
    if (request.method === 'PUT') account = { ...account, status: 'DISABLED' }
    return Promise.resolve(json(account))
  }
  const row = rows.find((item) => url.pathname.split('/')[5] === item.id)
  if (!row) throw new Error(`Unexpected request: ${request.method} ${url.pathname}`)
  if (request.method === 'PUT') row.status = 'WITHDRAWN'
  return Promise.resolve(json(row))
}

beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  resetSessionForTests()
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  account = { id: 'student-a', fullName: 'Test student', username: 'test.student', status: 'ACTIVE' }
  rows = (['ACTIVE', 'SUSPENDED', 'COMPLETED'] as const).map(
    (status, index) =>
      ({
        id: `enrollment-${index}`,
        courseId: `course-${index}`,
        studentUserId: account.id,
        subjectCode: `SUBJECT-${index}`,
        status,
      }) satisfies Schemas['EnrollmentDetail'],
  )
  writes = []
  intercept = undefined
  vi.stubGlobal('Request', BrowserRequest)
  // jsdom has no layout observer; keep the real Radix checkbox and its input behavior.
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {
        /* No layout measurements in jsdom. */
      }
      unobserve() {
        /* No layout measurements in jsdom. */
      }
      disconnect() {
        /* No layout measurements in jsdom. */
      }
    },
  )
  vi.stubGlobal(
    'fetch',
    vi.fn<typeof fetch>(async (input) => {
      if (!(input instanceof NativeRequest)) throw new Error('Expected OpenAPI Request')
      if (input.method === 'PUT') writes.push(input)
      return intercept?.(input) ?? server(input)
    }),
  )
})

afterEach(() => {
  cleanup()
  client.clear()
  close.mockReset()
  vi.unstubAllGlobals()
})

function open() {
  render(
    <QueryClientProvider client={client}>
      <RemoveStudentDialog user={account} onClose={close} />
    </QueryClientProvider>,
  )
}

async function confirm() {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText(uz.admin.reason), 'Registrar decision')
  await user.click(screen.getByRole('button', { name: uz.admin.removeStudent.confirm }))
}

describe('remove student confirmation (7.3-F3)', () => {
  it('shows identity and consequences; cancellation sends no writes', async () => {
    open()
    await screen.findByRole('checkbox', { name: /SUBJECT-0/ })
    expect(screen.getByText('Test student · test.student')).toBeVisible()
    expect(screen.getByText(uz.admin.removeStudent.description)).toBeVisible()
    await userEvent.click(screen.getByRole('button', { name: uz.common.cancel }))
    expect(close).toHaveBeenCalledOnce()
    expect(writes).toHaveLength(0)
  })

  it('requires a nonblank reason and withdraws only explicit selections', async () => {
    open()
    await userEvent.click(await screen.findByRole('checkbox', { name: /SUBJECT-0/ }))
    expect(screen.queryByRole('checkbox', { name: /SUBJECT-2/ })).not.toBeInTheDocument()
    await userEvent.type(screen.getByLabelText(uz.admin.reason), '   ')
    await userEvent.click(screen.getByRole('button', { name: uz.admin.removeStudent.confirm }))
    expect(await screen.findByText(uz.admin.studentCard.reasonRequired)).toBeVisible()
    expect(writes).toHaveLength(0)
    await confirm()
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
    expect(writes).toHaveLength(2)
    expect(rows.map((row) => row.status)).toEqual(['WITHDRAWN', 'SUSPENDED', 'COMPLETED'])
    expect(account.status).toBe('DISABLED')
    expect(new URL(writes[1]?.url ?? '').searchParams.get('reason')).toBe('Registrar decision')
  })

  it('offers deactivation only when there are no eligible enrollments', async () => {
    rows = rows.filter((row) => row.status === 'COMPLETED')
    open()
    expect(await screen.findByText(uz.admin.removeStudent.deactivateOnly)).toBeVisible()
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
    await confirm()
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
    expect(writes).toHaveLength(1)
  })

  it('waits for all pages before offering all eligible courses', async () => {
    rows = Array.from(
      { length: 101 },
      (_, index) =>
        ({
          id: `enrollment-${index}`,
          courseId: `course-${index}`,
          studentUserId: account.id,
          subjectCode: `SUBJECT-${index}`,
          status: 'PENDING',
        }) satisfies Schemas['EnrollmentDetail'],
    )
    let release: (() => void) | undefined
    intercept = (request) =>
      new URL(request.url).searchParams.get('page') === '1'
        ? new Promise<Response>((resolve) => {
            release = () => void server(request).then(resolve)
          })
        : undefined
    open()
    await waitFor(() => expect(release).toBeDefined())
    expect(screen.queryByRole('checkbox')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: uz.admin.removeStudent.confirm })).toBeDisabled()
    release?.()
    intercept = undefined
    await userEvent.click(await screen.findByRole('checkbox', { name: uz.admin.removeStudent.allEligible }))
    expect(screen.getAllByRole('checkbox', { checked: true })).toHaveLength(102)
    await confirm()
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
    expect(writes).toHaveLength(102)
  })

  it('shows a read error with request ID and retries without writing', async () => {
    intercept = () => Promise.resolve(json({ code: 'SERVICE_UNAVAILABLE', requestId: 'request-load' }, 503))
    open()
    expect(await screen.findByText(/request-load/)).toBeVisible()
    expect(screen.getByRole('button', { name: uz.admin.removeStudent.confirm })).toBeDisabled()
    intercept = undefined
    await userEvent.click(screen.getByRole('button', { name: uz.common.retry }))
    await screen.findByRole('checkbox', { name: /SUBJECT-0/ })
    expect(writes).toHaveLength(0)
  })

  it('keeps partial success, reason and request ID visible; retry skips confirmed work', async () => {
    intercept = (request) =>
      request.method === 'PUT' && request.url.includes('enrollment-1')
        ? Promise.resolve(json({ code: 'ENROLLMENT_INVALID_TRANSITION', requestId: 'request-withdraw' }, 422))
        : undefined
    open()
    await userEvent.click(await screen.findByRole('checkbox', { name: uz.admin.removeStudent.allEligible }))
    await confirm()
    expect(await screen.findByText(uz.admin.removeStudent.partial)).toBeVisible()
    expect(screen.getByText(/request-withdraw/)).toBeVisible()
    expect(screen.getByLabelText(uz.admin.reason)).toHaveValue('Registrar decision')
    expect(screen.getByLabelText(uz.admin.reason)).toBeDisabled()
    expect(rows[0]?.status).toBe('WITHDRAWN')
    writes = []
    intercept = undefined
    await userEvent.click(screen.getByRole('button', { name: uz.admin.removeStudent.retryRemaining }))
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
    expect(writes).toHaveLength(1)
    expect(writes[0]?.url).toContain('enrollment-1/status')
  })

  it('blocks repeated confirmation and closing while a write is pending', async () => {
    let release: (() => void) | undefined
    intercept = (request) =>
      request.method === 'PUT' && request.url.includes('/users/')
        ? new Promise<Response>((resolve) => {
            release = () => void server(request).then(resolve)
          })
        : undefined
    open()
    await screen.findByRole('checkbox', { name: /SUBJECT-0/ })
    await confirm()
    await waitFor(() => expect(release).toBeDefined())
    expect(screen.getAllByRole('button', { name: uz.common.close })[0]).toBeDisabled()
    expect(screen.getByRole('button', { name: uz.admin.removeStudent.retryRemaining })).toBeDisabled()
    await userEvent.keyboard('{Escape}')
    expect(close).not.toHaveBeenCalled()
    expect(writes).toHaveLength(1)
    release?.()
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
  })

  it('shows an account failure and blocks withdrawals until retry succeeds', async () => {
    intercept = (request) =>
      request.method === 'PUT' && request.url.includes('/users/')
        ? Promise.resolve(json({ code: 'AUTH_FORBIDDEN', requestId: 'request-disable' }, 403))
        : undefined
    open()
    await userEvent.click(await screen.findByRole('checkbox', { name: uz.admin.removeStudent.allEligible }))
    await confirm()
    expect(await screen.findByText(uz.admin.removeStudent.partial)).toBeVisible()
    expect(screen.getByText(/request-disable/)).toBeVisible()
    expect(writes).toHaveLength(1)
    expect(rows.map((row) => row.status)).toEqual(['ACTIVE', 'SUSPENDED', 'COMPLETED'])
    intercept = undefined
    await userEvent.click(screen.getByRole('button', { name: uz.admin.removeStudent.retryRemaining }))
    expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
    expect(account.status).toBe('DISABLED')
  })

  it.each(['account', 'enrollment'] as const)(
    'verifies a lost %s response and displays completion without duplicate writes',
    async (operation) => {
      const match = operation === 'account' ? '/users/' : '/enrollments/enrollment-0/status'
      intercept = (request) =>
        request.method === 'PUT' && request.url.includes(match)
          ? server(request).then(() => {
              throw new TypeError('Response lost')
            })
          : undefined
      open()
      await userEvent.click(await screen.findByRole('checkbox', { name: /SUBJECT-0/ }))
      await confirm()
      expect(await screen.findByText(uz.admin.removeStudent.complete)).toBeVisible()
      expect(screen.queryByRole('alert')).not.toBeInTheDocument()
      expect(writes).toHaveLength(2)
      expect(writes.filter((request) => request.url.includes(match))).toHaveLength(1)
      expect(rows[0]?.status).toBe('WITHDRAWN')
    },
  )
})
