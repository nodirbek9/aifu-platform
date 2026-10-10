import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, useLocation } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import createClient from 'openapi-fetch'
import type { paths } from '@/shared/api/schema'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import { navFor } from '@/app/shell/nav'
import { ProblemsPage } from './problems-page'
import { ADMIN_PROBLEM_TYPES, type AdminProblemView } from './problems-model'

vi.mock(import('@/shared/api/client'), async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, api: createClient<paths>({ baseUrl: 'http://localhost', fetch: actual.authFetch }) }
})

let client: QueryClient
const fetchMock = vi.fn<typeof fetch>()
const emptyPage = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
}
function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}
function Location() {
  const location = useLocation()
  return <output aria-label="location">{location.pathname + location.search}</output>
}
function openPage(search = '') {
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/admin/problems' + search]}>
        <ProblemsPage />
        <Location />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}
beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  fetchMock.mockResolvedValue(json(emptyPage))
  vi.stubGlobal('fetch', fetchMock)
})
afterEach(() => {
  cleanup()
  client.clear()
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

describe('problems page (7.4-F3)', () => {
  it('offers the problems destination only in admin navigation', () => {
    expect(navFor('ADMIN')).toContainEqual(
      expect.objectContaining({ key: 'adminProblems', to: '/admin/problems' }),
    )
    for (const role of ['STUDENT', 'TEACHER'] as const)
      expect(navFor(role).some((item) => item.to === '/admin/problems')).toBe(false)
  })

  it('shows skeletons while the real query is pending', () => {
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>(() => {
          /* Remain pending until unmount. */
        }),
    )
    openPage()
    expect(screen.getByLabelText(uz.common.loading)).toBeVisible()
    expect(screen.queryByRole('link', { name: uz.admin.problems.fix })).not.toBeInTheDocument()
  })

  it('shows the requestId and retries an API error', async () => {
    fetchMock.mockResolvedValueOnce(json({ code: 'AUTH_FORBIDDEN', requestId: 'problems-request' }, 403))
    openPage()
    expect(await screen.findByRole('alert')).toHaveTextContent('problems-request')
    await userEvent.click(screen.getByRole('button', { name: uz.common.retry }))
    expect(await screen.findByText(uz.admin.problems.empty)).toBeVisible()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('shows an honest empty state with a next action', async () => {
    openPage()
    expect(await screen.findByText(uz.admin.problems.empty)).toBeVisible()
    expect(screen.getByText(uz.admin.problems.emptyBody)).toBeVisible()
    expect(screen.getByRole('link', { name: uz.admin.problems.reviewUsers })).toHaveAttribute(
      'href',
      '/admin/users',
    )
  })

  it('renders every action mapping and the server total rather than page length', async () => {
    const content: AdminProblemView[] = ADMIN_PROBLEM_TYPES.map((type) => ({
      problemKey: type,
      type,
      userId: 'user',
      groupId: 'group',
      courseId: 'course',
      enrollmentId: 'enrollment',
      quizId: 'quiz',
      grantAvailable: true,
      fullName: 'Real API name',
    }))
    fetchMock.mockResolvedValue(
      json({ ...emptyPage, content, totalElements: 106, totalPages: 6, hasNext: true }),
    )
    openPage()
    const rows = await screen.findAllByRole('listitem')
    const paths = [
      '/admin/users/user#group',
      '/admin/timetable?view=group&id=group',
      '/admin/structure/courses/course',
      '/admin/users/user#enrollments',
      '/admin/users/user#status',
      '/admin/learning-path?course=course&enrollment=enrollment&quiz=quiz',
    ]
    rows.forEach((row, index) => {
      expect(within(row).getByRole('link')).toHaveAttribute('href', paths[index])
      expect(row).toHaveTextContent('Real API name')
    })
    expect(
      within(screen.getByRole('region', { name: uz.nav.adminProblems })).getByRole('status'),
    ).toHaveTextContent('106')
  })

  it('keeps rows with missing identifiers visible without malformed links', async () => {
    const content = ADMIN_PROBLEM_TYPES.map((type) => ({ problemKey: type, type }))
    fetchMock.mockResolvedValue(json({ ...emptyPage, content, totalElements: 6, totalPages: 1 }))
    openPage()
    const rows = await screen.findAllByRole('listitem')
    for (const row of rows) {
      expect(within(row).queryByRole('link')).not.toBeInTheDocument()
      expect(row).toHaveTextContent(uz.admin.problems.actionUnavailable)
    }
  })

  it('offers review when a midterm grant has already been used', async () => {
    fetchMock.mockResolvedValue(
      json({
        ...emptyPage,
        content: [
          {
            problemKey: 'midterm',
            type: 'MIDTERM_ATTEMPTS_EXHAUSTED',
            courseId: 'course',
            enrollmentId: 'enrollment',
            quizId: 'quiz',
            grantAvailable: false,
          },
        ],
        totalElements: 1,
        totalPages: 1,
      }),
    )
    openPage()
    expect(await screen.findByRole('link', { name: uz.admin.problems.reviewAttempt })).toHaveAttribute(
      'href',
      '/admin/learning-path?course=course&enrollment=enrollment&quiz=quiz',
    )
  })

  it('ignores an invalid URL type and fetches all types', async () => {
    openPage('?type=INVALID')
    expect(await screen.findByText(uz.admin.problems.empty)).toBeVisible()
    expect(screen.getByRole('combobox')).toHaveTextContent(uz.admin.problems.allTypes)
    const request = fetchMock.mock.calls[0]?.[0]
    if (!(request instanceof Request)) throw new Error('Expected a typed API request')
    expect(new URL(request.url).searchParams.has('type')).toBe(false)
  })

  it('resets the page when changing type and preserves unrelated URL filters', async () => {
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', { configurable: true, value: vi.fn() })
    openPage('?page=3&size=10&other=keep')
    await screen.findByText(uz.admin.problems.empty)
    fireEvent.keyDown(screen.getByRole('combobox'), { key: 'ArrowDown' })
    fireEvent.keyDown(screen.getByRole('option', { name: uz.admin.problems.type.LOCKED_ACCOUNT }), {
      key: 'Enter',
    })
    await waitFor(() =>
      expect(screen.getByLabelText('location')).toHaveTextContent(
        '/admin/problems?size=10&other=keep&type=LOCKED_ACCOUNT',
      ),
    )
    const request = fetchMock.mock.lastCall?.[0]
    if (!(request instanceof Request)) throw new Error('Expected a typed API request')
    expect(new URL(request.url).searchParams.get('page')).toBe('0')
  })
})
