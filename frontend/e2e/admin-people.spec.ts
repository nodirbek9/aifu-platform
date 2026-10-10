import { randomUUID } from 'node:crypto'
import { expect, test, type Locator, type Page } from '@playwright/test'
import createClient from 'openapi-fetch'
import type { components, paths } from '../src/shared/api/schema'
import { demoPassword, expectFitsScreen, shot, signIn, texts } from './support'
import { runSql } from './db'
import { narrowProblemPage, type AdminProblemType } from '../src/features/admin/problems-model'

const { uz } = texts
type Schemas = components['schemas']

test('problems dashboard and shell links, responsive themes and locales (7.4-F3)', async ({
  page,
}, testInfo) => {
  test.setTimeout(180_000)
  await signIn(page, 'admin')
  await page.goto('/admin')
  await page.getByRole('link', { name: uz.nav.adminProblems, exact: true }).last().focus()
  await page.keyboard.press('Enter')
  await expect(page).toHaveURL(/\/admin\/problems$/)
  const reviews = [
    { width: 390, height: 844, locales: ['uz', 'ru', 'en'] as const, themes: ['light', 'dark'] as const },
    { width: 768, height: 1024, locales: ['uz'] as const, themes: ['light'] as const },
    { width: 1440, height: 900, locales: ['uz', 'ru'] as const, themes: ['light', 'dark'] as const },
  ]
  await page.emulateMedia({ reducedMotion: 'reduce' })
  for (const review of reviews) {
    await page.setViewportSize({ width: review.width, height: review.height })
    for (const locale of review.locales)
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
          page.getByRole('heading', { name: texts[locale].admin.problems.title, exact: true }),
        ).toBeVisible()
        await expect(
          page.getByRole('region', { name: texts[locale].nav.adminProblems }).getByRole('status'),
        ).toBeVisible()
        await expectFitsScreen(page)
        await shot(page, 'problems', `${review.width}.${theme}.${locale}.${testInfo.project.name}`)
      }
  }
  await page.evaluate(() => localStorage.setItem('aifu-locale', 'uz'))
  await page.reload()
  await choose(page, uz.assignments.filterType, uz.admin.problems.type.LOCKED_ACCOUNT)
  await page.reload()
  await expect(page.getByRole('combobox', { name: uz.assignments.filterType })).toContainText(
    uz.admin.problems.type.LOCKED_ACCOUNT,
  )
  if (testInfo.project.name === 'phone') {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.getByRole('button', { name: uz.common.more, exact: true }).click()
    await page.getByRole('link', { name: uz.nav.adminProblems, exact: true }).click()
  } else {
    await page
      .getByRole('navigation', { name: uz.common.mainNavigation })
      .getByRole('link', { name: uz.nav.adminProblems, exact: true })
      .click()
  }
  await expect(page).toHaveURL(/\/admin\/problems$/)
})

test('problems navigation, all six filters and real fixes remove the exact rows (7.4-F3)', async ({
  page,
  baseURL,
}, testInfo) => {
  test.setTimeout(240_000)
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
  const students: string[] = []
  const enrollments: string[] = []
  let courseId: string | undefined
  let groupId: string | undefined
  const status = (userId: string, value: 'ACTIVE' | 'DISABLED') =>
    result(
      api.PUT('/api/v1/admin/users/{id}/status', {
        params: { path: { id: userId } },
        body: { status: value, reason: 'Problems fixture' },
      }),
    )
  const problems = async (type: AdminProblemType, pageNumber = 0) =>
    narrowProblemPage(
      await result(
        api.GET('/api/v1/admin/problems', { params: { query: { type, page: pageNumber, size: 20 } } }),
      ),
    )
  async function findPage(type: AdminProblemType, key: string) {
    let current = await problems(type)
    for (;;) {
      if (current.content.some((row) => row.problemKey === key)) return current.page
      if (!current.hasNext) throw new Error(`Missing real problem ${key}`)
      current = await problems(type, current.page + 1)
    }
  }
  try {
    groupId = id(await result(api.POST('/api/v1/admin/groups', { body: { code: `E2E-P-${suffix}` } })))
    const catalogue = await result(api.GET('/api/v1/admin/courses', { params: { query: { size: 100 } } }))
    const template = catalogue.content?.find((course) => course.status === 'PUBLISHED')
    if (!template) throw new Error('A published course is required')
    const source = await result(
      api.GET('/api/v1/admin/courses/{id}', { params: { path: { id: id(template) } } }),
    )
    if (!source.subjectId || !source.termId) throw new Error('Course context missing')
    courseId = id(
      await result(
        api.POST('/api/v1/admin/courses', {
          body: {
            subjectId: source.subjectId,
            termId: source.termId,
            credits: source.credits ?? 6,
            totalHours: source.totalHours ?? 150,
            cohortCode: `E2E-P-${suffix}`,
          },
        }),
      ),
    )
    // Isolated dev fixtures for lock/expired-attempt states that administrative APIs cannot create.
    // No seeded course is changed and append-only attempt/grant history is retained.
    runSql(`update courses set status = 'PUBLISHED' where id = '${courseId}'`)
    for (let index = 0; index < 3; index++) {
      const student = id(
        await result(
          api.POST('/api/v1/admin/users', {
            body: {
              username: `problem.${suffix}.${index}`,
              email: `problem.${suffix}.${index}@example.test`,
              fullName: `Problem ${suffix} ${index}`,
              roles: ['STUDENT'],
              studentProfile: { studentNumber: `P-${suffix}-${index}` },
            },
          }),
        ),
      )
      students.push(student)
      await status(student, 'ACTIVE')
    }
    const [ungrouped, inactive, locked] = students
    if (!ungrouped || !inactive || !locked) throw new Error('Student fixture missing')
    for (const student of [inactive, locked]) {
      await result(
        api.POST('/api/v1/admin/groups/{id}/students', {
          params: { path: { id: groupId } },
          body: { userIds: [student] },
        }),
      )
      enrollments.push(
        id(
          await result(
            api.POST('/api/v1/admin/enrollments', {
              body: { studentUserId: student, courseId, source: 'ADMIN' },
            }),
          ),
        ),
      )
    }
    await status(inactive, 'DISABLED')
    const midtermEnrollment = enrollments[1]
    if (!midtermEnrollment) throw new Error('Midterm enrollment missing')
    const moduleId = randomUUID(),
      lessonId = randomUUID(),
      quizId = randomUUID(),
      activityId = randomUUID()
    runSql(`
      update users set locked_until = now() + interval '1 hour' where id = '${locked}';
      insert into course_modules (id, course_id, code, position, status) values ('${moduleId}', '${courseId}', 'P-${suffix}', 0, 'PUBLISHED');
      insert into lessons (id, module_id, position, session_type, status, is_required, estimated_minutes) values ('${lessonId}', '${moduleId}', 0, 'LECTURE', 'PUBLISHED', true, 1);
      insert into quizzes (id, course_id, code, status, max_attempts, passing_percent, max_points, shuffle_questions) values ('${quizId}', '${courseId}', 'P-${suffix}', 'PUBLISHED', 1, 60, 100, false);
      insert into quiz_translations (quiz_id, locale, title) values ('${quizId}', 'uz', 'E2E midterm ${suffix}');
      insert into activities (id, course_id, position, activity_type, path_step, reference_id, is_graded, status, variant_strategy, collaboration_mode)
        values ('${activityId}', '${courseId}', 0, 'QUIZ', 'MIDTERM', '${quizId}', false, 'PUBLISHED', 'NONE', 'INDIVIDUAL');
      insert into course_learning_paths (course_id, enabled, midterm_activity_id, midterm_after_lesson_id) values ('${courseId}', true, '${activityId}', '${lessonId}');
      insert into quiz_attempts (quiz_id, enrollment_id, activity_id, attempt_number, status, expires_at) values ('${quizId}', '${midtermEnrollment}', '${activityId}', 1, 'EXPIRED', now() - interval '1 hour');
    `)
    await signIn(page, 'admin')
    await page.goto('/admin')
    await page.getByRole('link', { name: uz.nav.adminProblems, exact: true }).last().click()
    await expect(page).toHaveURL(/\/admin\/problems$/)
    const cases: { type: AdminProblemType; key: string; path: string; fix: () => Promise<unknown> }[] = [
      {
        type: 'STUDENT_WITHOUT_GROUP',
        key: ungrouped,
        path: `/admin/users/${ungrouped}#group`,
        fix: async () => {
          await choose(page, uz.admin.studentCard.changeGroup, `E2E-P-${suffix}`)
          await page.getByRole('button', { name: uz.admin.studentCard.changeGroup, exact: true }).click()
          await expect(page.getByLabel(uz.profile.group, { exact: true })).toHaveValue(`E2E-P-${suffix}`)
        },
      },
      {
        type: 'COURSE_WITHOUT_TEACHER',
        key: courseId,
        path: `/admin/structure/courses/${courseId}`,
        fix: async () => {
          const teachers = await result(
            api.GET('/api/v1/admin/users', {
              params: { query: { role: 'TEACHER', size: 100, sort: ['fullName,asc'] } },
            }),
          )
          const teacher = teachers.content?.[0]
          if (!teacher?.fullName) throw new Error('Teacher fixture missing')
          await choose(page, uz.admin.teacher, teacher.fullName)
          await page.getByRole('button', { name: uz.admin.assign, exact: true }).click()
          await expect(
            page.getByRole('button', { name: uz.admin.removeTeacher.replace('{{name}}', teacher.fullName) }),
          ).toBeVisible()
        },
      },
      {
        type: 'ACTIVE_ENROLLMENT_INACTIVE_USER',
        key: enrollments[0] ?? '',
        path: `/admin/users/${inactive}#enrollments`,
        fix: async () => {
          await choose(page, uz.admin.status, uz.admin.userStatus.ACTIVE)
          await page.getByLabel(uz.admin.reason, { exact: true }).fill('Restore active student')
          await page.getByRole('button', { name: uz.admin.changeStatus, exact: true }).click()
          await expect
            .poll(
              async () =>
                (await result(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: inactive } } })))
                  .status,
            )
            .toBe('ACTIVE')
        },
      },
      {
        type: 'LOCKED_ACCOUNT',
        key: locked,
        path: `/admin/users/${locked}#status`,
        fix: async () => {
          await page.getByRole('button', { name: uz.admin.unlock, exact: true }).focus()
          await page.keyboard.press('Enter')
          await expect(page.getByRole('button', { name: uz.admin.unlock, exact: true })).toHaveCount(0)
        },
      },
      {
        type: 'MIDTERM_ATTEMPTS_EXHAUSTED',
        key: midtermEnrollment,
        path: `/admin/learning-path?course=${courseId}&enrollment=${midtermEnrollment}&quiz=${quizId}`,
        fix: async () => {
          await expect(
            page.getByRole('heading', { name: uz.admin.learningPath.title, exact: true }),
          ).toBeVisible()
          // Exact deep-link selection belongs to 7.4-F4; here exercise the existing grant API.
          await result(
            api.POST('/api/v1/admin/quizzes/{quizId}/enrollments/{enrollmentId}/attempt-grants', {
              params: { path: { quizId, enrollmentId: midtermEnrollment } },
              body: { reason: 'Problems coverage grant' },
            }),
          )
        },
      },
      {
        type: 'GROUP_WITHOUT_PUBLISHED_ENTRIES',
        key: groupId,
        path: `/admin/timetable?view=group&id=${groupId}`,
        fix: async () => {
          await expect(page.getByRole('heading', { name: uz.nav.adminTimetable, exact: true })).toBeVisible()
          // Retire an unused group through its real administrative endpoint.
          await api
            .PUT('/api/v1/admin/groups/{id}/archived', {
              params: { path: { id: groupId ?? '' }, query: { value: true } },
            })
            .then((response) => expect(response.response.ok).toBe(true))
        },
      },
    ]
    await page.emulateMedia({ reducedMotion: 'reduce' })
    for (const item of cases) {
      await page.goto('/admin/problems?page=3&other=keep')
      await choose(page, uz.assignments.filterType, uz.admin.problems.type[item.type])
      expect(new URL(page.url()).searchParams.has('page')).toBe(false)
      expect(new URL(page.url()).searchParams.get('other')).toBe('keep')
      const key = `${item.type}:${item.key}`
      const pageNumber = await findPage(item.type, key)
      await page.goto(`/admin/problems?type=${item.type}&page=${pageNumber}`)
      const action = page
        .locator(`section[aria-label="${uz.nav.adminProblems}"] a`)
        .filter({ hasText: uz.admin.problems.fix })
      const exactAction = action.and(page.locator(`a[href="${item.path}"]`))
      await expect(exactAction).toBeVisible()
      await expectFitsScreen(page)
      await shot(page, 'problems', `${item.type}.${testInfo.project.name}`)
      await exactAction.click()
      await expect(page).toHaveURL(new RegExp(item.path.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '$'))
      await item.fix()
      await page.goto(`/admin/problems?type=${item.type}&page=${pageNumber}`)
      await expect(page.getByRole('heading', { name: uz.admin.problems.title, exact: true })).toBeVisible()
      await expect(
        page.locator(`section[aria-label="${uz.nav.adminProblems}"] a[href="${item.path}"]`),
      ).toHaveCount(0)
      const remaining = await problems(item.type, pageNumber)
      expect(remaining.content.some((row) => row.problemKey === key)).toBe(false)
    }
  } finally {
    for (const enrollmentId of enrollments)
      await api.PUT('/api/v1/admin/enrollments/{id}/status', {
        params: {
          path: { id: enrollmentId },
          query: { status: 'WITHDRAWN', reason: 'Problems fixture cleanup' },
        },
      })
    for (const student of students) {
      runSql(`update users set locked_until = null where id = '${student}'`)
      await status(student, 'DISABLED')
    }
    if (courseId)
      await api.PUT('/api/v1/admin/courses/{id}/status', {
        params: { path: { id: courseId }, query: { status: 'ARCHIVED' } },
      })
    if (groupId)
      await api.PUT('/api/v1/admin/groups/{id}/archived', {
        params: { path: { id: groupId }, query: { value: true } },
      })
    await api.POST('/api/v1/auth/logout', { body: { refreshToken: login.refreshToken } })
  }
})

test('cancel then deactivate with selected withdrawals preserves history and terminal rows (7.3-F3)', async ({
  page,
  baseURL,
}, testInfo) => {
  test.setTimeout(240_000)
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
  let studentId: string | undefined
  const enrollmentIds: string[] = []
  const detail = (enrollmentId: string) =>
    result(api.GET('/api/v1/admin/enrollments/{id}', { params: { path: { id: enrollmentId } } }))
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
  try {
    studentId = id(
      await result(
        api.POST('/api/v1/admin/users', {
          body: {
            username: `remove.${suffix}`,
            email: `remove.${suffix}@example.test`,
            fullName: `Removal ${suffix}`,
            roles: ['STUDENT'],
            studentProfile: { studentNumber: `REMOVE-${suffix}` },
          },
        }),
      ),
    )
    await result(
      api.PUT('/api/v1/admin/users/{id}/status', {
        params: { path: { id: studentId } },
        body: { status: 'ACTIVE', reason: 'Removal fixture' },
      }),
    )
    const catalogue = await result(
      api.GET('/api/v1/admin/courses', { params: { query: { size: 100, locale: 'uz' } } }),
    )
    const courses = (catalogue.content ?? []).filter((course) => course.status === 'PUBLISHED').slice(0, 3)
    if (courses.length !== 3) throw new Error('Three published courses are required for removal verification')
    for (const course of courses) {
      enrollmentIds.push(
        id(
          await result(
            api.POST('/api/v1/admin/enrollments', {
              body: { studentUserId: studentId, courseId: id(course), source: 'ADMIN' },
            }),
          ),
        ),
      )
    }
    const [selected, unselected, completed] = enrollmentIds
    const [selectedCourse, , completedCourse] = courses
    if (!selected || !unselected || !completed || !selectedCourse || !completedCourse)
      throw new Error('Incomplete removal fixture')
    await change(unselected, 'SUSPENDED', 'Unselected enrollment fixture')
    await signIn(page, 'admin')
    await page.goto(`/admin/users/${studentId}`)

    // Exercise the actual terminal-state guard, without intercepting or fabricating an HTTP response.
    await change(completed, 'COMPLETED', 'Completed by competing administrator')
    const rejected = await api.PUT('/api/v1/admin/enrollments/{id}/status', {
      params: {
        path: { id: completed },
        query: { status: 'WITHDRAWN', reason: 'Terminal withdrawal must fail' },
      },
    })
    expect(rejected.response.status).toBe(422)
    expect(rejected.error?.code).toBe('ENROLLMENT_INVALID_TRANSITION')
    expect(rejected.error?.requestId).toBeTruthy()
    await page.reload()
    const card = page.getByRole('region', { name: uz.admin.studentCard.coursesTitle, exact: true })
    const terminalRow = card
      .getByRole('listitem')
      .filter({ has: page.locator(`a[href="/admin/structure/courses/${id(completedCourse)}"]`) })
    const before = await Promise.all(enrollmentIds.map(detail))
    const currentEnrollments = await result(
      api.GET('/api/v1/admin/enrollments', {
        params: { query: { userId: studentId, size: 100 } },
      }),
    )
    const selectedSnapshot = currentEnrollments.content?.find((enrollment) => enrollment.id === selected)
    if (!selectedSnapshot) throw new Error('Selected enrollment snapshot missing')
    const selectedLabel =
      [selectedSnapshot.subjectCode, selectedSnapshot.cohortCode, selectedSnapshot.termName]
        .filter(Boolean)
        .join(' · ') +
      ' · ' +
      uz.admin.enrollmentStatus.ACTIVE

    await page.getByRole('button', { name: uz.admin.removeStudent.title, exact: true }).click()
    let dialog = page.getByRole('dialog')
    await expect(dialog.getByRole('checkbox')).toHaveCount(3)
    await dialog.getByRole('checkbox', { name: selectedLabel, exact: true }).check()
    await dialog.getByLabel(uz.admin.reason).fill('Cancelled removal')
    await dialog.getByRole('button', { name: uz.common.cancel, exact: true }).click()
    await expect(dialog).toHaveCount(0)
    expect(
      (await result(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: studentId } } }))).status,
    ).toBe('ACTIVE')
    expect(await Promise.all(enrollmentIds.map(detail))).toEqual(before)

    await page.getByRole('button', { name: uz.admin.removeStudent.title, exact: true }).click()
    dialog = page.getByRole('dialog')
    await dialog.getByRole('checkbox', { name: selectedLabel, exact: true }).check()
    const reason = `Removal decision ${suffix}`
    await dialog.getByLabel(uz.admin.reason).fill(reason)
    await page.emulateMedia({ reducedMotion: 'reduce' })
    for (const width of [390, 768, 1440]) {
      await page.setViewportSize({ width, height: 900 })
      for (const theme of ['light', 'dark'] as const) {
        await page.evaluate(
          (theme) => document.documentElement.classList.toggle('dark', theme === 'dark'),
          theme,
        )
        await expectFitsScreen(page)
        await shot(page, 'remove-student', `${width}.${theme}.${testInfo.project.name}`)
      }
    }
    await dialog.getByRole('button', { name: uz.admin.removeStudent.confirm }).focus()
    await page.keyboard.press('Enter')
    await expect(dialog.getByText(uz.admin.removeStudent.complete, { exact: true })).toBeVisible()
    await dialog.getByRole('button', { name: uz.common.close, exact: true }).first().click()
    await page.reload()
    expect(
      (await result(api.GET('/api/v1/admin/users/{id}', { params: { path: { id: studentId } } }))).status,
    ).toBe('DISABLED')
    await expect(page.getByRole('combobox', { name: uz.admin.status, exact: true })).toContainText(
      uz.admin.userStatus.DISABLED,
    )
    const withdrawn = await detail(selected)
    expect(withdrawn.status).toBe('WITHDRAWN')
    expect(withdrawn.events?.slice(0, before[0]?.events?.length)).toEqual(before[0]?.events)
    expect(withdrawn.events?.at(-1)).toMatchObject({ eventType: 'WITHDRAWN', toStatus: 'WITHDRAWN', reason })
    expect(await detail(unselected)).toEqual(before[1])
    expect(await detail(completed)).toEqual(before[2])
    await expect(terminalRow).toContainText(uz.admin.enrollmentStatus.COMPLETED)
    await expect(
      terminalRow.getByRole('button', { name: uz.admin.studentCard.changeEnrollmentStatus }),
    ).toHaveCount(0)
  } finally {
    // Preserve append-only enrollment history; retire only this test's non-terminal records.
    for (const enrollmentId of enrollmentIds) {
      const enrollment = await detail(enrollmentId)
      if (
        enrollment.status === 'ACTIVE' ||
        enrollment.status === 'PENDING' ||
        enrollment.status === 'SUSPENDED'
      )
        await change(enrollmentId, 'WITHDRAWN', 'Removal fixture cleanup')
    }
    if (studentId)
      await result(
        api.PUT('/api/v1/admin/users/{id}/status', {
          params: { path: { id: studentId } },
          body: { status: 'DISABLED', reason: 'Removal fixture cleanup' },
        }),
      )
    await api.POST('/api/v1/auth/logout', { body: { refreshToken: login.refreshToken } })
  }
})

test('teacher preset persists filters and separates weekly exceptions from term load (7.2-F3)', async ({
  page,
  baseURL,
}, testInfo) => {
  test.setTimeout(240_000)
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
  const teachers: string[] = []
  const courses: string[] = []
  let timetableId: string | undefined
  let groupId: string | undefined
  try {
    const year = await result(
      api.POST('/api/v1/admin/academic-years', {
        body: {
          code: `F3-${suffix}`,
          startsOn: '2026-10-05',
          endsOn: '2026-10-30',
        },
      }),
    )
    const yearId = id(year)
    const term = await result(
      api.POST('/api/v1/admin/academic-years/{id}/terms', {
        params: { path: { id: yearId } },
        body: {
          name: `F3 term ${suffix}`,
          startsOn: '2026-10-05',
          endsOn: '2026-10-30',
          ordinal: 1,
        },
      }),
    )
    const termId = id(term)
    for (const name of ['Original', 'Replacement']) {
      teachers.push(
        id(
          await result(
            api.POST('/api/v1/admin/users', {
              body: {
                username: `f3.${suffix}.${name.toLowerCase()}`,
                email: `f3.${suffix}.${name}@example.test`,
                fullName: `F3 ${suffix} ${name}`,
                roles: ['TEACHER'],
              },
            }),
          ),
        ),
      )
    }
    const teacherId = teachers[0]
    const replacementId = teachers[1]
    if (!teacherId || !replacementId) throw new Error('Missing fixture teachers')
    for (const teacher of teachers)
      await result(
        api.PUT('/api/v1/admin/users/{id}/status', {
          params: { path: { id: teacher } },
          body: { status: 'ACTIVE', reason: 'F3 teacher fixture' },
        }),
      )
    const subjects = await result(api.GET('/api/v1/admin/subjects', { params: { query: { size: 100 } } }))
    const subject = subjects.content?.[0]
    if (!subject) throw new Error('A subject is required for the teacher fixture')
    for (const title of ['Scheduled', 'Unscheduled']) {
      const courseId = id(
        await result(
          api.POST('/api/v1/admin/courses', {
            body: {
              subjectId: id(subject),
              termId,
              cohortCode: `F3-${suffix}-${title}`,
              credits: 1,
              totalHours: 36,
            },
          }),
        ),
      )
      courses.push(courseId)
      await result(
        api.PUT('/api/v1/admin/courses/{id}/translations/{locale}', {
          params: { path: { id: courseId, locale: 'uz' } },
          body: { title: `F3 ${suffix} ${title}` },
        }),
      )
      for (const teacher of teachers)
        await result(
          api.POST('/api/v1/admin/courses/{id}/teachers', {
            params: { path: { id: courseId } },
            body: { teacherUserId: teacher, teacherRole: 'LEAD' },
          }),
        )
    }
    const scheduled = courses[0]
    const unscheduled = courses[1]
    if (!scheduled || !unscheduled) throw new Error('Missing fixture courses')
    await result(
      api.PUT('/api/v1/admin/courses/{id}/session-allocations', {
        params: { path: { id: scheduled } },
        body: { sessionType: 'LECTURE', allocatedHours: 36 },
      }),
    )
    groupId = id(await result(api.POST('/api/v1/admin/groups', { body: { code: `F3-${suffix}` } })))
    timetableId = id(
      await result(
        api.POST('/api/v1/admin/timetables', { body: { name: `F3 timetable ${suffix}`, termId } }),
      ),
    )
    const slots = await result(api.GET('/api/v1/admin/pair-slots'))
    if (slots.length < 3) throw new Error('Three pair slots are required')
    const entries: string[] = []
    for (const slot of slots.slice(0, 3)) {
      entries.push(
        id(
          await result(
            api.POST('/api/v1/admin/timetables/{id}/entries', {
              params: { path: { id: timetableId } },
              body: {
                courseId: scheduled,
                teacherUserId: teacherId,
                groupIds: [groupId],
                pairSlotId: id(slot),
                dayOfWeek: 1,
                sessionType: 'LECTURE',
                weekParity: 'EVERY',
              },
            }),
          ),
        ),
      )
    }
    const cancelled = entries[1]
    const replaced = entries[2]
    if (!cancelled || !replaced) throw new Error('Missing fixture entries')
    await result(
      api.POST('/api/v1/admin/timetable-entries/{entryId}/exceptions', {
        params: { path: { entryId: cancelled } },
        body: { kind: 'CANCELLED', onDate: '2026-10-05', reason: 'F3 cancellation' },
      }),
    )
    await result(
      api.POST('/api/v1/admin/timetable-entries/{entryId}/exceptions', {
        params: { path: { entryId: replaced } },
        body: {
          kind: 'REPLACED',
          onDate: '2026-10-05',
          newTeacherUserId: replacementId,
          reason: 'F3 replacement',
        },
      }),
    )

    await signIn(page, 'admin')
    await page.goto(`/admin/users?q=F3+${suffix}&status=ACTIVE&page=2`)
    await page.getByRole('tab', { name: uz.admin.teacherWork.tab }).focus()
    await page.keyboard.press('Enter')
    await expect(page).toHaveURL(/role=TEACHER/)
    expect(new URL(page.url()).searchParams.get('page')).toBeNull()
    expect(new URL(page.url()).searchParams.get('q')).toBe(`F3 ${suffix}`)
    expect(new URL(page.url()).searchParams.get('status')).toBe('ACTIVE')
    await page.reload()
    await expect(page.getByRole('tab', { name: uz.admin.teacherWork.tab })).toHaveAttribute(
      'aria-selected',
      'true',
    )
    await page.getByRole('link', { name: `F3 ${suffix} Original`, exact: true }).click()
    const card = page.getByRole('region', { name: uz.admin.teacherWork.tab, exact: true })
    await choose(page, uz.admin.academicYear, year.code ?? '', card)
    await expect(card.getByRole('link', { name: `F3 ${suffix} Unscheduled`, exact: true })).toHaveAttribute(
      'href',
      `/admin/structure/courses/${unscheduled}`,
    )
    await card.getByLabel(uz.admin.date, { exact: true }).fill('2026-10-05')
    const weekly = card.getByRole('region', { name: uz.admin.teacherWork.weeklyHours })
    await expect(weekly.getByText('2', { exact: true })).toBeVisible()
    const week = await result(
      api.GET('/api/v1/admin/timetables/{id}/week', {
        params: { path: { id: timetableId }, query: { teacherId, date: '2026-10-05', locale: 'uz' } },
      }),
    )
    const classes = week.days?.flatMap((day) => day.classes ?? []) ?? []
    expect(classes.some((item) => item.status === 'CANCELLED')).toBe(true)
    expect(classes.some((item) => item.status === 'REPLACED' && item.teacherUserId === replacementId)).toBe(
      true,
    )
    const load = await result(
      api.GET('/api/v1/admin/timetables/{id}/load', {
        params: { path: { id: timetableId }, query: { teacherId, locale: 'uz' } },
      }),
    )
    const termLoad = card.getByRole('region', { name: uz.admin.teacherWork.termLoad })
    for (const field of ['plannedHours', 'scheduledHours', 'remainingHours'] as const) {
      const total = load.reduce((sum, row) => sum + (row[field] ?? 0), 0)
      await expect(
        termLoad
          .locator('dd')
          .filter({ hasText: new RegExp(`^${total}$`) })
          .first(),
      ).toBeVisible()
    }
    expect(load.reduce((sum, row) => sum + (row.scheduledHours ?? 0), 0)).toBeGreaterThan(2)
    await card.getByLabel(uz.admin.date, { exact: true }).fill('2026-10-12')
    await expect(weekly.getByText('6', { exact: true })).toBeVisible()
    await card.getByLabel(uz.admin.date, { exact: true }).fill('2026-10-05')
    await expect(weekly.getByText('2', { exact: true })).toBeVisible()
    await page.emulateMedia({ reducedMotion: 'reduce' })
    for (const width of [390, 768, 1440]) {
      await page.setViewportSize({ width, height: 900 })
      for (const locale of ['uz', 'ru', 'en'] as const) {
        for (const theme of ['light', 'dark'] as const) {
          await page.evaluate(
            ({ locale, theme }) => {
              localStorage.setItem('aifu-locale', locale)
              localStorage.setItem('aifu-theme', theme)
            },
            { locale, theme },
          )
          await page.reload()
          const localizedCard = page.getByRole('region', {
            name: texts[locale].admin.teacherWork.tab,
            exact: true,
          })
          await choose(page, texts[locale].admin.academicYear, year.code ?? '', localizedCard)
          await expect(
            localizedCard.getByRole('region', { name: texts[locale].admin.teacherWork.termLoad }),
          ).toBeVisible()
          await expectFitsScreen(page)
          await shot(page, 'teacher-work', `${width}.${theme}.${locale}.${testInfo.project.name}`)
        }
      }
    }
  } finally {
    if (timetableId) {
      const deleted = await api.DELETE('/api/v1/admin/timetables/{id}', {
        params: { path: { id: timetableId } },
      })
      expect(deleted.response.ok, 'fixture timetable cleanup').toBe(true)
    }
    for (const courseId of courses) {
      const deleted = await api.DELETE('/api/v1/admin/courses/{id}', { params: { path: { id: courseId } } })
      expect(deleted.response.ok, 'fixture course cleanup').toBe(true)
    }
    if (groupId)
      await result(
        api.PUT('/api/v1/admin/groups/{id}/archived', {
          params: { path: { id: groupId }, query: { value: true } },
        }),
      )
    for (const teacherId of teachers)
      await result(
        api.PUT('/api/v1/admin/users/{id}/status', {
          params: { path: { id: teacherId } },
          body: { status: 'DISABLED', reason: 'F3 fixture cleanup' },
        }),
      )
    await api.POST('/api/v1/auth/logout', { body: { refreshToken: login.refreshToken } })
  }
})

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
