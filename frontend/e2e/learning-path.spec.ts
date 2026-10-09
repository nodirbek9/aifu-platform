import { expect, test, type APIRequestContext } from '@playwright/test'
import { resetDemoStudent, runSql } from './db'
import { demoPassword, signIn, texts } from './support'

const { uz } = texts

/** DAS1110 (semester 1): the demo course with TEST and PRACTICE steps (dev seed V914). */
const COURSE = '2eec7704-6e25-4adb-966e-b54de91a9b69'

async function adminHeaders(request: APIRequestContext): Promise<{ Authorization: string }> {
  const login = await request.post('/api/v1/auth/login', {
    data: { identifier: 'admin', password: demoPassword() },
  })
  expect(login.ok(), 'API login of admin').toBe(true)
  const tokens = (await login.json()) as { accessToken: string }
  return { Authorization: `Bearer ${tokens.accessToken}` }
}

async function setPathEnabled(request: APIRequestContext, enabled: boolean): Promise<void> {
  const response = await request.put(`/api/v1/admin/courses/${COURSE}/learning-path`, {
    headers: await adminHeaders(request),
    data: { enabled },
  })
  expect(response.ok(), `learning path enabled=${String(enabled)}`).toBe(true)
}

/** The server refuses the lesson to student3 itself, whatever the browser does. */
async function expectLockedByApi(request: APIRequestContext, lesson: string): Promise<void> {
  const login = await request.post('/api/v1/auth/login', {
    data: { identifier: 'student3', password: demoPassword() },
  })
  const { accessToken, refreshToken } = (await login.json()) as { accessToken: string; refreshToken: string }
  const headers = { Authorization: `Bearer ${accessToken}` }
  const response = await request.get(`/api/v1/lessons/${lesson}`, { headers })
  expect(response.status()).toBe(422)
  expect(((await response.json()) as { code: string }).code).toBe('LESSON_PREREQUISITE_NOT_MET')
  await request.post('/api/v1/auth/logout', { headers, data: { refreshToken } })
}

/** A lesson of the course by its code. */
function lessonId(code: string): string {
  return runSql(`
    select l.id from lessons l join course_modules m on m.id = l.module_id
    where m.course_id = '${COURSE}' and l.code = '${code}'`)
}

const QUIZ_CODE = 'DAS-Q-M2'
const GRANT_REASON = 'Kasallik sababli qoldirilgan'

/** Uses up the student's attempts at the quiz: only then may an administrator give one more. */
function exhaustAttempts(): void {
  runSql(`
    insert into quiz_attempts (quiz_id, enrollment_id, attempt_number, status, submitted_at)
    select q.id, e.id, n, 'SUBMITTED', now()
    from quizzes q, generate_series(1, q.max_attempts) n,
         enrollments e join users u on u.id = e.student_user_id
    where q.code = '${QUIZ_CODE}' and u.username = 'student3' and e.course_id = '${COURSE}'
      and not exists (select 1 from quiz_attempts a
                      where a.quiz_id = q.id and a.enrollment_id = e.id and a.attempt_number = n)`)
}

/** Removes the student's attempts at the quiz and the grants for it (attempts are append-only for the API). */
function clearAttemptsAndGrants(): void {
  const enrollment = `select e.id from enrollments e join users u on u.id = e.student_user_id
    where u.username = 'student3' and e.course_id = '${COURSE}'`
  // grants are append-only (like grades): only a test reset may remove them, guard trigger off for this session
  runSql(`
    set session_replication_role = replica;
    delete from quiz_attempt_grants where enrollment_id in (${enrollment});
    delete from quiz_attempts where enrollment_id in (${enrollment})
      and quiz_id = (select id from quizzes where code = '${QUIZ_CODE}');
    set session_replication_role = origin;`)
}

test.describe('the learning path (phase 5)', () => {
  test.beforeEach(async ({ request }) => {
    resetDemoStudent('student3')
    clearAttemptsAndGrants()
    exhaustAttempts()
    await setPathEnabled(request, true)
  })

  test.afterAll(async ({ request }) => {
    clearAttemptsAndGrants()
    await setPathEnabled(request, false)
  })

  test('a lesson behind an unfinished one is locked by URL and by API, and shows no text', async ({
    page,
  }) => {
    // M1 opens the course; M3 (next module) waits for the test and practice of M2
    const first = lessonId('M1')
    const second = lessonId('M3')

    await signIn(page, 'student3')
    await expectLockedByApi(page.request, second)
    await page.goto(`/student/courses/${COURSE}/lessons/${second}`)

    await expect(page.getByText(uz.lesson.lockedPreviousTitle)).toBeVisible()
    await expect(page.locator('[data-block-position]')).toHaveCount(0)

    // the first lesson is open
    await page.goto(`/student/courses/${COURSE}/lessons/${first}`)
    await expect(page.getByText(uz.lesson.lockedPreviousTitle)).toHaveCount(0)
    await expect(page.locator('[data-block-position]').first()).toBeVisible()
  })

  test('the administrator gives one more attempt with a reason and sees it listed', async ({ page }) => {
    await signIn(page, 'admin')
    await page.goto('/admin/learning-path')
    await expect(page.getByRole('heading', { level: 1, name: uz.admin.learningPath.title })).toBeVisible()

    await page.getByRole('button', { name: uz.admin.learningPath.giveAttemptAction }).click()
    const dialog = page.getByRole('dialog')
    // the course has a quiz for every lesson now: pick the one whose attempts the test used up
    await dialog.getByRole('combobox', { name: uz.admin.learningPath.selectQuiz }).click()
    await page
      .getByRole('option', { name: /DAS-Q-M2/ })
      .first()
      .click()
    await dialog.getByRole('combobox', { name: uz.admin.learningPath.selectStudent }).click()
    await page.getByRole('option', { name: 'Dev Student Three' }).first().click()
    await expect(dialog.getByText(uz.admin.learningPath.giveAttemptTitle)).toBeVisible()

    const submit = dialog.getByRole('button', { name: uz.admin.learningPath.submitGrant })
    await dialog.getByLabel(uz.admin.learningPath.grantReasonLabel).fill('abc')
    await expect(submit).toBeDisabled()
    await dialog.getByLabel(uz.admin.learningPath.grantReasonLabel).fill(GRANT_REASON)
    await submit.click()

    await expect(page.getByText(uz.admin.learningPath.grantSuccess).first()).toBeVisible()
    await expect(page.getByText(GRANT_REASON)).toBeVisible()
  })
})
