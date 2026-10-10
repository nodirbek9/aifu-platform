import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import { ToastProvider } from '@/shared/ui/toast'
import { TeacherWorkCard } from './teacher-work-card'
import { UserPage } from './user-page'
import { UsersPage } from './users-page'

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

type Schemas = components['schemas']
const fetchMock = vi.fn<typeof fetch>()
let client: QueryClient
let account: Schemas['UserDetail']
let assigned: boolean
let failYears: boolean
let emptyYears: boolean

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  resetSessionForTests()
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  account = { id: 'teacher-a', fullName: 'Teacher A', roles: ['TEACHER'], status: 'ACTIVE' }
  assigned = true
  failYears = false
  emptyYears = false
  vi.stubGlobal('Request', BrowserRequest)
  const respond = (input: RequestInfo | URL) => {
    if (!(input instanceof Request)) throw new Error('Expected an OpenAPI Request')
    const url = new URL(input.url)
    const path = url.pathname
    if (path === '/api/v1/admin/users/teacher-a') return json(account)
    if (path === '/api/v1/admin/users') return json({ content: [account], totalPages: 3 })
    if (path === '/api/v1/admin/roles') return json([{ code: 'TEACHER' }, { code: 'STUDENT' }])
    if (path === '/api/v1/groups') return json([])
    if (path === '/api/v1/admin/enrollments') return json({ content: [], totalPages: 0 })
    if (path === '/api/v1/admin/academic-years') {
      if (failYears) return json({ code: 'INTERNAL_ERROR', requestId: 'teacher-request' }, 500)
      return json(emptyYears ? [] : [{ id: 'year-a', code: '2026/2027', current: true }])
    }
    if (path.endsWith('/terms')) {
      return json([{ id: 'term-a', name: 'Term A', startsOn: '2026-09-01', endsOn: '2027-01-31' }])
    }
    if (path === '/api/v1/admin/courses') return json({ content: [{ id: 'course-a' }], totalPages: 1 })
    if (path === '/api/v1/admin/courses/course-a') {
      return json({
        id: 'course-a',
        teachers: assigned ? [{ userId: 'teacher-a' }] : [],
        translations: [{ locale: 'uz', title: 'Unscheduled assignment' }],
      })
    }
    if (path === '/api/v1/admin/timetables') {
      return json([{ id: 'tt-a', termId: 'term-a', name: 'Timetable A', status: 'PUBLISHED' }])
    }
    if (path.endsWith('/load')) return json([{ plannedHours: 36, scheduledHours: 24, remainingHours: 12 }])
    if (path.endsWith('/week')) {
      const date = url.searchParams.get('date') ?? ''
      const classes: Schemas['ClassView'][] = [
        { entryId: 'one', date, teacherUserId: 'teacher-a', status: 'SCHEDULED' },
        { entryId: 'one', date, teacherUserId: 'teacher-a', status: 'SCHEDULED' },
        { entryId: 'two', date, teacherUserId: 'teacher-a', status: 'REPLACED' },
        { entryId: 'three', date, teacherUserId: 'teacher-a', status: 'CANCELLED' },
        { entryId: 'four', date, teacherUserId: 'teacher-b', status: 'REPLACED' },
      ]
      return json({ days: [{ classes }] })
    }
    throw new Error(`Unexpected request: ${path}`)
  }
  fetchMock.mockImplementation((input) => Promise.resolve(respond(input)))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  cleanup()
  client.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

function Location() {
  return <output data-testid="location">{useLocation().search}</output>
}

function open(view: 'card' | 'user' | 'users' = 'card', search = '') {
  render(
    <QueryClientProvider client={client}>
      <ToastProvider>
        <MemoryRouter initialEntries={[`/admin/users/teacher-a${search}`]}>
          <Location />
          <Routes>
            <Route
              path="/admin/users/:userId"
              element={
                view === 'card' ? (
                  <TeacherWorkCard user={account} />
                ) : view === 'user' ? (
                  <UserPage />
                ) : (
                  <UsersPage />
                )
              }
            />
          </Routes>
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>,
  )
}

describe('teacher work integration (7.2-F3)', () => {
  it('preserves query/status filters and resets pagination when selecting the teacher preset', async () => {
    open('users', '?q=Teacher&status=ACTIVE&role=STUDENT&page=2')
    const user = userEvent.setup()
    await user.click(screen.getByRole('tab', { name: uz.admin.teacherWork.tab }))
    expect(screen.getByTestId('location')).toHaveTextContent('?q=Teacher&status=ACTIVE&role=TEACHER')
    expect(screen.getByTestId('location')).not.toHaveTextContent('page=')
    expect(screen.getByRole('tab', { name: uz.admin.teacherWork.tab })).toHaveAttribute(
      'aria-selected',
      'true',
    )
    await screen.findByRole('link', { name: 'Teacher A' })
    await user.click(screen.getByRole('tab', { name: uz.nav.adminUsers }))
    expect(screen.getByTestId('location')).toHaveTextContent('?q=Teacher&status=ACTIVE')
    expect(screen.getByTestId('location')).not.toHaveTextContent('role=')
  })

  it('restores the teacher preset from the URL', () => {
    open('users', '?role=TEACHER')
    expect(screen.getByRole('tab', { name: uz.admin.teacherWork.tab })).toHaveAttribute(
      'aria-selected',
      'true',
    )
  })

  it('preserves an existing non-teacher role filter when opening the users preset', async () => {
    open('users', '?q=Teacher&status=ACTIVE&role=STUDENT&page=2')
    expect(screen.getByTestId('location')).toHaveTextContent('?q=Teacher&status=ACTIVE&role=STUDENT')
    expect(screen.getByTestId('location')).toHaveTextContent('page=2')
    expect(screen.getByRole('tab', { name: uz.nav.adminUsers })).toHaveAttribute('aria-selected', 'true')
    await vi.waitFor(() => {
      expect(screen.getByRole('combobox', { name: uz.admin.role })).toHaveTextContent(uz.role.STUDENT)
    })
    await screen.findByRole('link', { name: 'Teacher A' })
  })

  it('selects the teacher preset by keyboard and sends the preserved filters to the API', async () => {
    open('users', '?q=Teacher&status=ACTIVE&page=2')
    const user = userEvent.setup()
    screen.getByRole('tab', { name: uz.nav.adminUsers }).focus()
    await user.keyboard('{ArrowRight}')
    expect(screen.getByRole('tab', { name: uz.admin.teacherWork.tab })).toHaveFocus()
    await vi.waitFor(() => {
      expect(
        fetchMock.mock.calls.some(([request]) => {
          if (!(request instanceof Request)) return false
          const url = new URL(request.url)
          return (
            url.pathname === '/api/v1/admin/users' &&
            url.searchParams.get('role') === 'TEACHER' &&
            url.searchParams.get('query') === 'Teacher' &&
            url.searchParams.get('status') === 'ACTIVE' &&
            url.searchParams.get('page') === '0'
          )
        }),
      ).toBe(true)
    })
  })

  it('renders both sections for a multi-role user and preserves account controls', async () => {
    account.roles = ['STUDENT', 'TEACHER']
    open('user')
    expect(await screen.findByRole('region', { name: uz.admin.teacherWork.tab })).toBeVisible()
    expect(await screen.findByRole('heading', { name: uz.admin.studentCard.groupTitle })).toBeVisible()
    expect(await screen.findByRole('heading', { name: uz.admin.studentCard.coursesTitle })).toBeVisible()
    expect(screen.getByRole('heading', { name: uz.admin.resetPassword })).toBeVisible()
  })

  it('does not request teacher work for a student-only account', async () => {
    account.roles = ['STUDENT']
    open('user')
    await screen.findByRole('heading', { name: uz.admin.studentCard.coursesTitle })
    expect(screen.queryByRole('region', { name: uz.admin.teacherWork.tab })).not.toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([r]) => r instanceof Request && r.url.includes('academic-years'))).toBe(
      false,
    )
  })

  it('shows unscheduled assignments and distinguishes weekly occurrences from term totals', async () => {
    open()
    expect(await screen.findByRole('link', { name: 'Unscheduled assignment' })).toHaveAttribute(
      'href',
      '/admin/structure/courses/course-a',
    )
    const weekly = await screen.findByRole('region', { name: uz.admin.teacherWork.weeklyHours })
    expect(await within(weekly).findByText('4', { exact: true })).toBeVisible()
    const term = screen.getByRole('region', { name: uz.admin.teacherWork.termLoad })
    for (const total of ['36', '24', '12']) expect(await within(term).findByText(total)).toBeVisible()
  })

  it('offers an action when there are no assignments', async () => {
    assigned = false
    open()
    expect(await screen.findByText(uz.admin.teacherWork.noCourses)).toBeVisible()
    expect(screen.getByRole('link', { name: uz.admin.studentCard.openCourses })).toHaveAttribute(
      'href',
      '/admin/structure',
    )
  })

  it('renders a loading skeleton while reference data is pending', () => {
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>(() => {
          // Keep this read pending to inspect the loading state.
        }),
    )
    const { container } = render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <TeacherWorkCard user={account} />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(container.querySelector('[aria-hidden="true"]')).toBeInTheDocument()
  })

  it('shows an actionable empty reference state', async () => {
    emptyYears = true
    open()
    expect(await screen.findByRole('link', { name: uz.nav.adminReference })).toHaveAttribute(
      'href',
      '/admin/reference',
    )
  })

  it('shows a request ID and retries a failed reference read', async () => {
    failYears = true
    open()
    expect(await screen.findByText('teacher-request', { exact: false })).toBeVisible()
    failYears = false
    await userEvent.setup().click(screen.getByRole('button', { name: uz.common.retry }))
    expect(await screen.findByRole('link', { name: 'Unscheduled assignment' })).toBeVisible()
  })
})
