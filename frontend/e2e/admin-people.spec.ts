import { randomUUID } from 'node:crypto'
import { expect, test, type Locator, type Page } from '@playwright/test'
import createClient from 'openapi-fetch'
import type { components, paths } from '../src/shared/api/schema'
import { demoPassword, expectFitsScreen, shot, signIn, texts } from './support'

const { uz } = texts
type Schemas = components['schemas']

/** Fail with HTTP metadata only: authentication responses must never enter test logs. */
async function result<T>(request: Promise<{ data?: T; response: Response }>): Promise<T> {
  const response = await request
  if (!response.response.ok || response.data === undefined) {
    throw new Error(
      `Fixture API failed: ${response.response.status} ${new URL(response.response.url).pathname}`,
    )
  }
  return response.data
}

function id(value: { id?: string }): string {
  if (!value.id) throw new Error('Fixture API omitted its resource ID')
  return value.id
}

async function choose(page: Page, field: string, option: string, scope: Page | Locator = page) {
  await scope.getByRole('combobox', { name: field, exact: true }).click()
  await page.getByRole('option', { name: option, exact: true }).click()
}

test('student card persists group moves, paginated enrollments and reasoned transitions (7.1-F4)', async ({
  page,
  baseURL,
}, testInfo) => {
  test.setTimeout(240_000)
  page.setDefaultTimeout(15_000)
  const api = createClient<paths>({ baseUrl: baseURL })
  const login = await result(
    api.POST('/api/v1/auth/login', { body: { identifier: 'admin', password: demoPassword() } }),
  )
  if (!login.accessToken || !login.refreshToken) throw new Error('Authentication omitted tokens')
  api.use({
    onRequest({ request }) {
      request.headers.set('Authorization', `Bearer ${login.accessToken}`)
    },
  })
  const suffix = randomUUID().slice(0, 8)
  const groupIds: string[] = []
  const studentIds: string[] = []
  const enrollmentIds: string[] = []
  const oldCode = `F4-${suffix}-OLD`
  const newCode = `F4-${suffix}-NEW`
  const change = (
    enrollmentId: string,
    status: NonNullable<Schemas['EnrollmentDetail']['status']>,
    reason: string,
  ) =>
    result(
      api.PUT('/api/v1/admin/enrollments/{id}/status', {
        params: { path: { id: enrollmentId }, query: { status, reason } },
      }),
    )
  const detail = (enrollmentId: string) =>
    result(api.GET('/api/v1/admin/enrollments/{id}', { params: { path: { id: enrollmentId } } }))
  const roster = (groupId: string) =>
    result(api.GET('/api/v1/groups/{id}/students', { params: { path: { id: groupId } } }))

  try {
    const prerequisite = await api.GET('/api/v1/admin/enrollments', {
      params: { query: { userId: randomUUID(), page: 0, size: 25 } },
    })
    if (!prerequisite.response.ok) {
      throw new Error(
        `7.1-B1 runtime prerequisite failed: userId-only enrollment lookup returned HTTP ${prerequisite.response.status}. Start the backend containing 7.1-B1 before running this flow.`,
      )
    }
    for (const code of [oldCode, newCode]) {
      groupIds.push(id(await result(api.POST('/api/v1/admin/groups', { body: { code } }))))
    }
    const oldGroup = groupIds[0]
    const newGroup = groupIds[1]
    if (!oldGroup || !newGroup) throw new Error('Missing fixture groups')
    for (const name of ['a', 'b']) {
      studentIds.push(
        id(
          await result(
            api.POST('/api/v1/admin/users', {
              body: {
                username: `f4.${suffix}.${name}`,
                email: `f4.${suffix}.${name}@example.test`,
                fullName: `F4 ${suffix} Student ${name}`,
                roles: ['STUDENT'],
                studentProfile: { studentNumber: `F4-${suffix}-${name}`, groupCode: oldCode },
              },
            }),
          ),
        ),
      )
    }
    const student = studentIds[0]
    const other = studentIds[1]
    if (!student || !other) throw new Error('Missing fixture students')
    const courses = await result(
      api.GET('/api/v1/admin/courses', { params: { query: { size: 100, locale: 'uz' } } }),
    )
    const published = (courses.content ?? []).filter((course) => course.status === 'PUBLISHED')
    const firstCourse = published[0]
    const secondCourse = published[1]
    if (!firstCourse || !secondCourse) throw new Error('Two published courses are required')
    const firstCourseId = id(firstCourse)
    const secondCourseId = id(secondCourse)
    const enroll = async (userId: string, courseId: string) => {
      const enrollment = await result(
        api.POST('/api/v1/admin/enrollments', { body: { studentUserId: userId, courseId, source: 'ADMIN' } }),
      )
      enrollmentIds.push(id(enrollment))
      return id(enrollment)
    }

    // Real, terminal history supplies the second page without adding courses or altering seed students.
    for (let index = 0; index < 24; index += 1) {
      await change(await enroll(student, secondCourseId), 'WITHDRAWN', `F4 history ${index}`)
    }
    const activeSecond = await enroll(student, secondCourseId)
    const target = await enroll(student, firstCourseId)
    const otherEnrollment = await enroll(other, firstCourseId)
    const untouched = await detail(otherEnrollment)
    const otherUser = await result(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: other } } }))

    await signIn(page, 'admin')
    await page.goto(`/admin/users/${student}`)
    await expect(page.getByRole('heading', { level: 1, name: `F4 ${suffix} Student a` })).toBeVisible()
    await expect(page.getByLabel(uz.profile.group, { exact: true })).toHaveValue(oldCode)
    await choose(page, uz.admin.studentCard.changeGroup, newCode)
    await page.getByRole('button', { name: uz.admin.studentCard.changeGroup, exact: true }).click()
    await expect(page.getByLabel(uz.profile.group, { exact: true })).toHaveValue(newCode)
    await page.reload()
    await expect(page.getByLabel(uz.profile.group, { exact: true })).toHaveValue(newCode)
    expect((await roster(oldGroup)).map((row) => row.userId)).toEqual([other])
    expect((await roster(newGroup)).map((row) => row.userId)).toEqual([student])

    const card = page.getByRole('region', { name: uz.admin.studentCard.coursesTitle, exact: true })
    const rows = card.getByRole('listitem')
    await expect(rows).toHaveCount(25)
    await expect(card.getByRole('link', { name: firstCourse.title, exact: true }).first()).toHaveAttribute(
      'href',
      `/admin/structure/courses/${firstCourseId}`,
    )
    await expect(card.getByRole('link', { name: secondCourse.title, exact: true }).first()).toHaveAttribute(
      'href',
      `/admin/structure/courses/${secondCourseId}`,
    )
    await card.getByRole('button', { name: uz.common.next, exact: true }).click()
    await expect(page).toHaveURL(/enrollmentPage=1/)
    await expect(rows).toHaveCount(1)
    await page.reload()
    await expect(rows).toHaveCount(1)
    await card.getByRole('button', { name: uz.common.previous, exact: true }).click()
    await expect(rows).toHaveCount(25)

    const targetRow = rows.filter({
      has: page.locator(`a[href="/admin/structure/courses/${firstCourseId}"]`),
    })
    for (const status of ['SUSPENDED', 'ACTIVE', 'WITHDRAWN'] as const) {
      await targetRow.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }).click()
      const dialog = page.getByRole('dialog')
      await expect(dialog).toBeVisible()
      await choose(page, uz.admin.status, uz.admin.enrollmentStatus[status], dialog)
      const reason = `F4 ${suffix} ${status}`
      await dialog.getByLabel(uz.admin.reason).fill(reason)
      await dialog.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }).click()
      await expect(dialog).toHaveCount(0)
      await expect(targetRow).toContainText(uz.admin.enrollmentStatus[status])
      const persisted = await detail(target)
      expect(persisted.status).toBe(status)
      expect(persisted.events?.some((event) => event.toStatus === status && event.reason === reason)).toBe(
        true,
      )
    }
    await page.reload()
    await expect(targetRow).toContainText(uz.admin.enrollmentStatus.WITHDRAWN)
    expect((await detail(target)).events?.map((event) => event.eventType)).toEqual([
      'CREATED',
      'SUSPENDED',
      'RESUMED',
      'WITHDRAWN',
    ])
    expect(await detail(otherEnrollment)).toEqual(untouched)
    expect(await result(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: other } } }))).toEqual(
      otherUser,
    )
    expect((await detail(activeSecond)).status).toBe('ACTIVE')

    // Review the composed page at every required width/theme/locale with real server data.
    const reviews = [
      { width: 390, height: 844, locales: ['uz', 'ru', 'en'] as const, themes: ['light', 'dark'] as const },
      { width: 768, height: 1024, locales: ['uz'] as const, themes: ['light'] as const },
      { width: 1440, height: 900, locales: ['uz', 'ru'] as const, themes: ['light', 'dark'] as const },
    ]
    await page.emulateMedia({ reducedMotion: 'reduce' })
    for (const review of reviews) {
      await page.setViewportSize({ width: review.width, height: review.height })
      for (const locale of review.locales) {
        for (const theme of review.themes) {
          await page.evaluate(
            ({ locale, theme }) => {
              localStorage.setItem('aifu-locale', locale)
              localStorage.setItem('aifu-theme', theme)
            },
            { locale, theme },
          )
          await page.reload()
          await expect(
            page.getByRole('heading', { name: texts[locale].admin.studentCard.coursesTitle, exact: true }),
          ).toBeVisible()
          await expectFitsScreen(page)
          await shot(page, 'student-card', `${review.width}.${theme}.${locale}.${testInfo.project.name}`)
        }
      }
    }
    await page.evaluate(() => localStorage.setItem('aifu-locale', 'uz'))
    await page.reload()
    await targetRow.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }).focus()
    await page.keyboard.press('Enter')
    await expect(page.getByRole('dialog')).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.getByRole('dialog')).toHaveCount(0)
    await expect(
      targetRow.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }),
    ).toBeFocused()
  } finally {
    // Preserve append-only history; clean up only this run's active resources through existing APIs.
    for (const enrollmentId of enrollmentIds) {
      const enrollment = await detail(enrollmentId)
      if (
        enrollment.status !== 'WITHDRAWN' &&
        enrollment.status !== 'COMPLETED' &&
        enrollment.status !== 'FAILED'
      ) {
        await change(enrollmentId, 'WITHDRAWN', 'F4 fixture cleanup')
      }
    }
    for (const studentId of studentIds) {
      await result(
        api.PUT('/api/v1/admin/users/{id}/status', {
          params: { path: { id: studentId } },
          body: { status: 'DISABLED', reason: 'F4 fixture cleanup' },
        }),
      )
      for (const groupId of groupIds) {
        await api.DELETE('/api/v1/admin/groups/{id}/students/{userId}', {
          params: { path: { id: groupId, userId: studentId } },
        })
      }
    }
    for (const groupId of groupIds) {
      const response = await api.PUT('/api/v1/admin/groups/{id}/archived', {
        params: { path: { id: groupId }, query: { value: true } },
      })
      expect(response.response.ok, 'fixture group archive').toBe(true)
    }
    await api.POST('/api/v1/auth/logout', { body: { refreshToken: login.refreshToken } })
  }
})
