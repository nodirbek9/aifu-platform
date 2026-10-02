import { test, type Page } from '@playwright/test'
import { resetLocale, shot as save, signIn, texts, type Locale } from './support'

/**
 * Review screenshots (`npm run shots`), not assertions: every F0 screen at this project's
 * width, in both themes and three languages, saved under e2e/__screenshots__/f0/.
 * They are looked at by a person (or an agent) against docs/04-DESIGN-BRIEF.md.
 */
const shot = (page: Page, project: string, name: string) => save(page, 'f0', `${name}.${project}`)
const THEMES = ['light', 'dark'] as const
const LOCALES: readonly Locale[] = ['uz', 'ru', 'en']

async function prefer(page: Page, theme: (typeof THEMES)[number], locale: Locale): Promise<void> {
  await page.addInitScript(
    ([themeValue, localeValue]) => {
      localStorage.setItem('aifu-theme', themeValue)
      localStorage.setItem('aifu-locale', localeValue)
    },
    [theme, locale] as const,
  )
}

test.describe('@shots F0', () => {
  for (const theme of THEMES) {
    for (const locale of LOCALES) {
      test(`login ${theme} ${locale}`, async ({ page }, testInfo) => {
        await prefer(page, theme, locale)
        await page.goto('/login')
        await shot(page, testInfo.project.name, `login.${theme}.${locale}`)
      })
    }
  }

  test('login with an error', async ({ page }, testInfo) => {
    await prefer(page, 'light', 'uz')
    await page.goto('/login')
    await page.getByLabel(texts.uz.auth.identifier).fill('no.such.account')
    await page.getByLabel(texts.uz.auth.password, { exact: true }).fill('not-the-password')
    await page.getByRole('button', { name: texts.uz.auth.signIn, exact: true }).click()
    await page.getByRole('alert').waitFor()
    await shot(page, testInfo.project.name, 'login-error.light.uz')
  })

  for (const theme of THEMES) {
    for (const [account, role] of [
      ['student3', 'student'],
      ['n.ayitbayev', 'teacher'],
      ['admin', 'admin'],
    ] as const) {
      test(`shell ${role} ${theme}`, async ({ page }, testInfo) => {
        await page.addInitScript((themeValue) => {
          localStorage.setItem('aifu-theme', themeValue)
        }, theme)
        await signIn(page, account)
        await shot(page, testInfo.project.name, `shell-${role}.${theme}.uz`)
      })
    }
  }

  test('shell in Russian, system pages', async ({ page }, testInfo) => {
    const project = testInfo.project.name
    await signIn(page, 'student3')
    await page.evaluate(() => {
      localStorage.setItem('aifu-locale', 'ru')
    })
    await page.goto('/student/courses')
    await shot(page, project, 'shell-student.light.ru')
    await page.goto('/admin/users')
    await shot(page, project, 'no-access.light.ru')
    await page.goto('/student/nothing-here')
    await shot(page, project, 'not-found.light.ru')
    await resetLocale(page.request, 'student3')
  })

  test('design system catalogue', async ({ page }, testInfo) => {
    await prefer(page, 'light', 'uz')
    await page.goto('/dev/ui')
    await shot(page, testInfo.project.name, 'dev-ui.light.uz')
  })
})
