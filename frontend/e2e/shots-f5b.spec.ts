import { expect, test } from '@playwright/test'
import { shot, signIn } from './support'

/** Review screenshots of the timetable builder (phase F5b): `npm run shots`. */
test.describe('@shots F5b', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`builder ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(240_000)
      const name = (screen: string) => `${screen}.${theme}.uz.${testInfo.project.name}`
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'admin')
      await page.goto('/admin/timetable')
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f5b', name('by-group'))
      await page.goto('/admin/timetable?view=teacher')
      await shot(page, 'f5b', name('by-teacher'))
      await page.goto('/admin/timetable?view=room')
      await shot(page, 'f5b', name('by-room'))
      await page.goto('/admin/timetable')
      // a class card: its name lists course, kind, teacher, groups and room
      await page
        .getByRole('main')
        .getByRole('button', { name: /, .*, / })
        .first()
        .click()
      await expect(page.getByRole('dialog')).toBeVisible()
      await shot(page, 'f5b', name('class-panel'))
    })
  }
})
