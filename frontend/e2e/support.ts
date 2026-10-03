import { mkdirSync } from 'node:fs'
import path from 'node:path'
import { expect, type APIRequestContext, type Page, type TestInfo } from '@playwright/test'
import en from '../src/shared/i18n/en.json' with { type: 'json' }
import ru from '../src/shared/i18n/ru.json' with { type: 'json' }
import uz from '../src/shared/i18n/uz.json' with { type: 'json' }

export const texts = { uz, ru, en }
export type Locale = keyof typeof texts

/** The password of every demo account (docs/03-API-GUIDE.md §9); never written into the code. */
export function demoPassword(): string {
  const password = process.env.E2E_DEMO_PASSWORD
  if (password === undefined || password === '') {
    throw new Error('Set E2E_DEMO_PASSWORD to the demo password of docs/03-API-GUIDE.md §9')
  }
  return password
}

export function isPhone(testInfo: TestInfo): boolean {
  return testInfo.project.name === 'phone'
}

/**
 * Puts the account's saved interface language back to Uzbek through the API, so a test never
 * depends on what an earlier run left on the server.
 */
export async function resetLocale(request: APIRequestContext, username: string): Promise<void> {
  const login = await request.post('/api/v1/auth/login', {
    data: { identifier: username, password: demoPassword() },
  })
  expect(login.ok(), `API login of ${username}`).toBe(true)
  const tokens = (await login.json()) as { accessToken: string; refreshToken: string }
  const headers = { Authorization: `Bearer ${tokens.accessToken}` }
  await request.patch('/api/v1/auth/me', { headers, data: { preferredLocale: 'uz' } })
  await request.post('/api/v1/auth/logout', { headers, data: { refreshToken: tokens.refreshToken } })
}

/** Opens the login page (if not there yet) and fills the form; does not wait for what happens next. */
export async function submitLogin(page: Page, username: string, password: string): Promise<void> {
  if (!page.url().includes('/login')) await page.goto('/login')
  await page.getByLabel(uz.auth.identifier).fill(username)
  await page.getByLabel(uz.auth.password, { exact: true }).fill(password)
  await page.getByRole('button', { name: uz.auth.signIn, exact: true }).click()
}

/** Signs in through the login page as a demo account and waits for the shell. */
export async function signIn(page: Page, username: string): Promise<void> {
  await resetLocale(page.request, username)
  await submitLogin(page, username, demoPassword())
  await expect(page.getByRole('navigation', { name: uz.common.mainNavigation })).toBeVisible()
}

/** Opens the profile menu of the top bar. */
export async function openProfileMenu(page: Page): Promise<void> {
  await page.getByRole('button', { name: new RegExp(`^${uz.common.profile}`) }).click()
}

/**
 * Saves a full-page review screenshot to e2e/__screenshots__/<phase>/<name>.png once the page
 * has settled: no request in flight, no skeleton left, fonts loaded, entrance animations over.
 */
export async function shot(page: Page, phase: string, name: string): Promise<void> {
  const directory = path.join(import.meta.dirname, '__screenshots__', phase)
  mkdirSync(directory, { recursive: true })
  await page.waitForLoadState('networkidle')
  await expect(page.locator('.animate-pulse')).toHaveCount(0)
  await page.evaluate(() => document.fonts.ready)
  await page.waitForTimeout(700)
  await page.screenshot({ path: path.join(directory, `${name}.png`), fullPage: true })
}
