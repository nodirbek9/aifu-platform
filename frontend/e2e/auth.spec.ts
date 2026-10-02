import { expect, test } from '@playwright/test'
import { isPhone, openProfileMenu, signIn, submitLogin, texts } from './support'

const { uz, ru, en } = texts

test.describe('signing in (G-08, G-09)', () => {
  test('a student lands on the student home with the student sections', async ({ page }) => {
    await signIn(page, 'student3')

    await expect(page).toHaveURL(/\/student$/)
    await expect(page.getByRole('heading', { level: 1, name: 'Dev Student Three' })).toBeVisible()
    const navigation = page.getByRole('navigation', { name: uz.common.mainNavigation })
    await expect(navigation.getByRole('link', { name: uz.nav.courses })).toBeVisible()
    await expect(navigation.getByRole('link', { name: uz.nav.adminUsers })).toHaveCount(0)
  })

  test('an administrator lands on the admin home', async ({ page }) => {
    await signIn(page, 'admin')

    await expect(page).toHaveURL(/\/admin$/)
    const navigation = page.getByRole('navigation', { name: uz.common.mainNavigation })
    await expect(navigation.getByRole('link', { name: uz.nav.adminUsers })).toBeVisible()
  })

  test('a teacher who is also an administrator can switch role', async ({ page }, testInfo) => {
    await signIn(page, 'n.ayitbayev')
    await expect(page).toHaveURL(/\/teacher$/)

    if (isPhone(testInfo)) {
      await openProfileMenu(page)
    } else {
      await page.getByRole('button', { name: new RegExp(`^${uz.role.switchRole}`) }).click()
    }
    await page.getByRole('menuitem', { name: uz.role.ADMIN }).click()

    await expect(page).toHaveURL(/\/admin$/)
    const navigation = page.getByRole('navigation', { name: uz.common.mainNavigation })
    await expect(navigation.getByRole('link', { name: uz.nav.adminUsers })).toBeVisible()

    // the choice is remembered: "/" now means the admin home
    await page.goto('/')
    await expect(page).toHaveURL(/\/admin$/)
  })

  test('wrong credentials are explained and nothing is let in', async ({ page }) => {
    // an account that does not exist: the same answer as a wrong password, and nobody gets locked
    await submitLogin(page, 'no.such.account', 'not-the-password')

    await expect(page.getByRole('alert')).toHaveText(uz.errors.AUTH_INVALID_CREDENTIALS)
    await expect(page).toHaveURL(/\/login$/)
  })

  test('empty fields are marked without calling the server', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('button', { name: uz.auth.signIn, exact: true }).click()

    await expect(page.getByText(uz.auth.fieldRequired)).toHaveCount(2)
  })
})

test.describe('session', () => {
  test('a reload keeps the user signed in, on the same page', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto('/student/grades')
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.grades })).toBeVisible()

    await page.reload()

    await expect(page).toHaveURL(/\/student\/grades$/)
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.grades })).toBeVisible()
  })

  test('a deep link opened signed out returns there after login', async ({ page }) => {
    await page.goto('/student/attendance')
    await expect(page).toHaveURL(/\/login$/)

    await signIn(page, 'student3')

    await expect(page).toHaveURL(/\/student\/attendance$/)
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.attendance })).toBeVisible()
  })

  test('signing out returns to the login page and closes the session', async ({ page }) => {
    await signIn(page, 'student3')

    await openProfileMenu(page)
    await page.getByRole('menuitem', { name: uz.auth.logout, exact: true }).click()

    await expect(page).toHaveURL(/\/login$/)
    await page.goto('/student')
    await expect(page).toHaveURL(/\/login$/)
  })
})

test.describe('roles and unknown addresses (G-09)', () => {
  test("a student who opens an administrator's address sees no access, not a crash", async ({ page }) => {
    await signIn(page, 'student3')

    await page.goto('/admin/users')

    await expect(page.getByRole('heading', { level: 1, name: uz.system.noAccessTitle })).toBeVisible()
    // still inside the student's own shell, with a way back
    const navigation = page.getByRole('navigation', { name: uz.common.mainNavigation })
    await expect(navigation.getByRole('link', { name: uz.nav.courses })).toBeVisible()
    await page.getByRole('link', { name: uz.system.noAccessAction }).click()
    await expect(page).toHaveURL(/\/student$/)
  })

  test('an address that does not exist says so', async ({ page }) => {
    await signIn(page, 'student3')

    await page.goto('/student/no-such-page')

    await expect(page.getByRole('heading', { level: 1, name: uz.system.notFoundTitle })).toBeVisible()
  })
})

test.describe('language and theme (G-01, G-05)', () => {
  test('the login page speaks all three languages', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('heading', { level: 1, name: uz.auth.signInTitle })).toBeVisible()

    await page.getByRole('radio', { name: 'RU' }).click()
    await expect(page.getByRole('heading', { level: 1, name: ru.auth.signInTitle })).toBeVisible()
    await expect(page.locator('html')).toHaveAttribute('lang', 'ru')

    await page.getByRole('radio', { name: 'EN' }).click()
    await expect(page.getByRole('heading', { level: 1, name: en.auth.signInTitle })).toBeVisible()

    // remembered across a reload
    await page.reload()
    await expect(page.getByRole('heading', { level: 1, name: en.auth.signInTitle })).toBeVisible()
  })

  test('the interface language is saved to the account', async ({ page }, testInfo) => {
    await signIn(page, 'student3')

    if (isPhone(testInfo)) {
      await page.getByRole('button', { name: new RegExp(`^${uz.common.language}`) }).click()
      await page.getByRole('menuitem', { name: 'RU' }).click()
    } else {
      await page.getByRole('radio', { name: 'RU' }).click()
    }
    await expect(page.getByRole('heading', { level: 3, name: ru.dashboard.todayTitle })).toBeVisible()

    // a fresh browser has no local choice: the language comes from the account
    await expect
      .poll(async () => {
        const context = await page.context().browser()?.newContext()
        if (context === undefined) return null
        const fresh = await context.newPage()
        const login = await fresh.request.post('/api/v1/auth/login', {
          data: { identifier: 'student3', password: process.env.E2E_DEMO_PASSWORD ?? '' },
        })
        const tokens = (await login.json()) as { accessToken: string }
        const me = await fresh.request.get('/api/v1/auth/me', {
          headers: { Authorization: `Bearer ${tokens.accessToken}` },
        })
        const body = (await me.json()) as { preferredLocale?: string }
        await context.close()
        return body.preferredLocale ?? null
      })
      .toBe('ru')
  })

  test('the theme can be changed and is remembered', async ({ page }, testInfo) => {
    await signIn(page, 'student3')
    await expect(page.locator('html')).not.toHaveClass(/dark/)

    if (isPhone(testInfo)) {
      await openProfileMenu(page)
    } else {
      await page.getByRole('button', { name: uz.common.theme, exact: true }).click()
    }
    await page.getByRole('menuitem', { name: uz.common.themeDark }).click()
    await expect(page.locator('html')).toHaveClass(/dark/)

    await page.reload()
    await expect(page.locator('html')).toHaveClass(/dark/)
  })
})
