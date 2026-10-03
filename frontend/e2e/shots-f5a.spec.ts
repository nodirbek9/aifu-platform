import { expect, test } from '@playwright/test'
import { shot, signIn } from './support'

/** Review screenshots of the administrator's records (phase F5a): `npm run shots`. */
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'

test.describe('@shots F5a', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`admin ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(480_000)
      const project = testInfo.project.name
      const name = (screen: string) => `${screen}.${theme}.uz.${project}`
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'admin')
      const screens: [string, string][] = [
        ['dashboard', '/admin'],
        ['users', '/admin/users'],
        ['import', '/admin/users/import'],
        ['structure-subjects', '/admin/structure'],
        ['structure-courses', '/admin/structure?tab=courses'],
        ['course', `/admin/structure/courses/${DASTURLASH}`],
        ['reference-groups', '/admin/reference'],
        ['reference-rooms', '/admin/reference?tab=rooms'],
        ['reference-bells', '/admin/reference?tab=bells'],
        ['reference-terms', '/admin/reference?tab=terms'],
        ['load', '/admin/load'],
        ['audit', '/admin/audit'],
      ]
      for (const [screen, path] of screens) {
        await page.goto(path)
        await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
        await shot(page, 'f5a', name(screen))
      }
    })
  }
})
