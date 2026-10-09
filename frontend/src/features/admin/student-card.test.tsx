import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { components } from '@/shared/api/schema'
import { resetSessionForTests } from '@/shared/api/session'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import { ToastProvider } from '@/shared/ui/toast'
import { UserPage } from './user-page'

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
let student: Schemas['UserDetail']
let enrollments: Schemas['EnrollmentSummary'][]
let groups: Schemas['GroupView'][]
let failMutation: boolean

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  resetSessionForTests()
  client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  student = { id: 'student-a', fullName: 'Test student', roles: ['STUDENT'], status: 'ACTIVE' }
  enrollments = []
  groups = [{ id: 'group-a', code: 'GROUP-A', status: 'ACTIVE' }]
  failMutation = false
  vi.stubGlobal('Request', BrowserRequest)
  const respond = (input: RequestInfo | URL) => {
    if (!(input instanceof Request)) throw new Error('Expected an OpenAPI Request')
    const path = new URL(input.url).pathname
    if (input.method === 'PUT' && path.endsWith('/status')) {
      if (failMutation) return json({ code: 'ENROLLMENT_INVALID_TRANSITION', requestId: 'request-f4' }, 409)
      return json({})
    }
    if (path === '/api/v1/admin/users/student-a') return json(student)
    if (path === '/api/v1/admin/roles') return json([])
    if (path === '/api/v1/groups') return json(groups)
    if (path === '/api/v1/admin/enrollments') {
      return json({ content: enrollments, page: 0, size: 25, totalPages: enrollments.length ? 1 : 0 })
    }
    if (path === '/api/v1/admin/courses/course-a') {
      return json({ id: 'course-a', translations: [{ locale: 'uz', title: 'Test course' }] })
    }
    throw new Error(`Unexpected request: ${input.method} ${path}`)
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

function openCard() {
  return render(
    <QueryClientProvider client={client}>
      <ToastProvider>
        <MemoryRouter initialEntries={['/admin/users/student-a']}>
          <Routes>
            <Route path="/admin/users/:userId" element={<UserPage />} />
          </Routes>
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>,
  )
}

describe('student card composition (7.1-F4)', () => {
  it('renders both student sections even when the student profile is absent', async () => {
    openCard()
    expect(await screen.findByRole('heading', { name: uz.admin.studentCard.groupTitle })).toBeVisible()
    expect(await screen.findByRole('heading', { name: uz.admin.studentCard.coursesTitle })).toBeVisible()
    expect(await screen.findByText(uz.admin.studentCard.noGroup)).toBeVisible()
  })

  it('offers a group move for an unassigned profile', async () => {
    student.studentProfile = { studentNumber: 'TEST-A' }
    openCard()
    expect(await screen.findByText(uz.admin.studentCard.noGroup)).toBeVisible()
    expect(screen.getByRole('combobox', { name: uz.admin.studentCard.changeGroup })).toBeEnabled()
  })

  it('keeps an unresolved group code visible and offers a next action', async () => {
    student.studentProfile = { groupCode: 'UNKNOWN' }
    groups = []
    openCard()
    expect(
      await screen.findByText(uz.admin.studentCard.unknownGroup.replace('{{code}}', 'UNKNOWN')),
    ).toBeVisible()
    expect(screen.getByRole('link', { name: uz.admin.newGroup })).toHaveAttribute('href', '/admin/reference')
  })

  it('shows an actionable empty enrollment page', async () => {
    openCard()
    expect(await screen.findByText(uz.admin.studentCard.noEnrollments)).toBeVisible()
    expect(screen.getByRole('link', { name: uz.admin.studentCard.openCourses })).toHaveAttribute(
      'href',
      '/admin/structure?tab=courses',
    )
  })

  it('retains completed enrollments and their course link without transition actions', async () => {
    enrollments = [
      { id: 'enrollment-a', studentUserId: 'student-a', courseId: 'course-a', status: 'COMPLETED' },
    ]
    openCard()
    expect(await screen.findByText(uz.admin.enrollmentStatus.COMPLETED)).toBeVisible()
    expect(await screen.findByRole('link', { name: 'Test course' })).toHaveAttribute(
      'href',
      '/admin/structure/courses/course-a',
    )
    expect(
      screen.queryByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }),
    ).not.toBeInTheDocument()
  })

  it('preserves the reason, request ID and selected status after a failed mutation, then retries', async () => {
    enrollments = [{ id: 'enrollment-a', studentUserId: 'student-a', courseId: 'course-a', status: 'ACTIVE' }]
    failMutation = true
    const user = userEvent.setup()
    openCard()
    await user.click(await screen.findByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }))
    const dialog = within(screen.getByRole('dialog'))
    await user.type(dialog.getByLabelText(uz.admin.reason), 'Registrar correction')
    await user.click(dialog.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }))
    expect(await dialog.findByText('request-f4', { exact: false })).toBeVisible()
    expect(dialog.getByLabelText(uz.admin.reason)).toHaveValue('Registrar correction')
    expect(dialog.getByRole('combobox', { name: uz.admin.status })).toHaveTextContent(
      uz.admin.enrollmentStatus.SUSPENDED,
    )
    failMutation = false
    await user.click(dialog.getByRole('button', { name: uz.common.retry }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    const writes = fetchMock.mock.calls.flatMap(([request]) =>
      request instanceof Request && request.method === 'PUT' ? [request] : [],
    )
    expect(writes).toHaveLength(2)
    expect(new URL(writes[1]?.url ?? '').searchParams.get('reason')).toBe('Registrar correction')
  })

  it('preserves account controls for a user without the student role', async () => {
    student.roles = ['TEACHER']
    openCard()
    expect(await screen.findByRole('heading', { level: 1, name: 'Test student' })).toBeVisible()
    expect(screen.queryByRole('heading', { name: uz.admin.studentCard.groupTitle })).not.toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: uz.admin.studentCard.coursesTitle })).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: uz.admin.resetPassword })).toBeVisible()
  })
})
