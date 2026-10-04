import { expect, test } from '@playwright/test'
import { resetDemoStudent, resetNotificationsAndPhone, runSql } from './db'
import { isPhone, signIn, texts } from './support'

const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'

const expired = {
  status: 401,
  contentType: 'application/json',
  body: JSON.stringify({ code: 'AUTH_TOKEN_EXPIRED', message: 'Token expired' }),
}

/** F6.5 — the network and the session misbehave; the app says so, recovers, and loses nothing. */
test.describe('resilience', () => {
  test.skip(({ isMobile }) => isMobile, 'the behaviour does not depend on the screen')

  test('the API is down: an error with a retry, and the retry brings the page back', async ({ page }) => {
    await signIn(page, 'student3')
    await page.route('**/api/v1/enrollments/me**', (route) => route.abort('connectionrefused'))
    await page.goto('/student/courses')
    await expect(page.getByText(uz.common.errorStateTitle)).toBeVisible({ timeout: 20_000 })
    await page.unroute('**/api/v1/enrollments/me**')
    await page.getByRole('button', { name: uz.common.retry }).first().click()
    await expect(page.getByRole('link', { name: /Dasturlash/ }).first()).toBeVisible()
  })

  test('a slow network shows the page taking shape, then the page', async ({ page }) => {
    await signIn(page, 'student3')
    await page.route('**/api/v1/enrollments/me**', async (route) => {
      await new Promise((resolve) => setTimeout(resolve, 2000))
      await route.continue()
    })
    await page.goto('/student/courses')
    await expect(page.locator('.animate-pulse').first()).toBeVisible()
    await expect(page.getByRole('link', { name: /Dasturlash/ }).first()).toBeVisible({ timeout: 15_000 })
  })

  test('the session expires in the middle of a form: the token is renewed and the save goes through', async ({
    page,
  }) => {
    resetNotificationsAndPhone('student3')
    await signIn(page, 'student3')
    await page.goto('/profile')
    let refused = false
    await page.route('**/api/v1/auth/me', async (route) => {
      if (route.request().method() === 'PATCH' && !refused) {
        refused = true
        await route.fulfill(expired)
        return
      }
      await route.continue()
    })
    await page.getByLabel(uz.profile.phone).fill('+998 90 111 22 33')
    await page.getByRole('button', { name: uz.profile.save, exact: true }).click()
    await expect(page.getByText(uz.profile.phoneSaved)).toBeVisible()
    expect(refused).toBe(true)
    await expect(page).toHaveURL(/\/profile$/)
    resetNotificationsAndPhone('student3')
  })

  test('two tabs renew the session at the same moment and both stay signed in', async ({ page, context }) => {
    await signIn(page, 'student3')
    const second = await context.newPage()
    await second.goto('/student/courses')
    await expect(second.getByRole('navigation', { name: uz.common.mainNavigation })).toBeVisible()
    // the next call of each tab meets an expired token: both refresh, with one refresh token
    for (const tab of [page, second]) {
      let once = false
      await tab.route('**/api/v1/enrollments/me**', async (route) => {
        if (!once) {
          once = true
          await route.fulfill(expired)
          return
        }
        await route.continue()
      })
    }
    await Promise.all([page.goto('/student/courses'), second.goto('/student/courses')])
    for (const tab of [page, second]) {
      await expect(tab.getByRole('link', { name: /Dasturlash/ }).first()).toBeVisible()
      await expect(tab).toHaveURL(/\/student\/courses$/)
    }
  })

  test('a reload while a solution is in flight neither loses nor doubles it', async ({ page }, testInfo) => {
    test.skip(isPhone(testInfo), 'desktop')
    test.setTimeout(120_000)
    resetDemoStudent('student3')
    await signIn(page, 'student3')
    // the submission is sent and the page is reloaded before the answer can arrive
    await page.goto(`/student/courses/${DASTURLASH}/tasks/${TASK}`)
    await page.getByRole('textbox', { name: uz.task.editorLabel }).fill('print(1)\n')
    await page.getByRole('button', { name: new RegExp(`^${uz.task.submit}`) }).click()
    await page.reload()
    await page.getByRole('tab', { name: uz.task.tabHistory }).click()
    await expect(page.getByRole('listitem').filter({ has: page.getByRole('button') })).toHaveCount(1, {
      timeout: 20_000,
    })
    const count = runSql(`
      select count(*) from submissions s join enrollments e on e.id = s.enrollment_id
      join users u on u.id = e.student_user_id
      where u.username = 'student3' and s.coding_task_id = '${TASK}'`)
    expect(count).toBe('1')
    resetDemoStudent('student3')
  })
})
