import { expect, test } from '@playwright/test'
import { isPhone, shot, signIn, texts } from './support'

/** Review screenshots of the rest of the student's side (phase F3): `npm run shots`. */
const { uz } = texts

/** Friday 2 October 2026, 09:10 in Tashkent: the first pair is going on. */
const FRIDAY_MORNING = new Date('2026-10-02T04:10:00Z')

test.describe('@shots F3', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`timetable ${theme}`, async ({ page }, testInfo) => {
      const project = testInfo.project.name
      const name = (screen: string) => `timetable-${screen}.${theme}.uz.${project}`
      await page.clock.setFixedTime(FRIDAY_MORNING)
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')

      // this week: a holiday on Thursday, the practical going on now
      await page.goto('/student/timetable')
      await expect(page.getByRole('heading', { level: 1, name: uz.nav.timetable })).toBeVisible()
      await shot(page, 'f3', name('this-week'))

      // a cancelled practical
      await page.goto('/student/timetable?date=2026-10-16')
      if (isPhone(testInfo)) await page.getByRole('button', { name: /16$/ }).click()
      await shot(page, 'f3', name('cancelled'))

      // a lecture moved from Monday to Tuesday, and its sheet
      await page.goto('/student/timetable?date=2026-10-19')
      await shot(page, 'f3', name('moved'))
      await page
        .getByRole('button', { name: /Dasturlash/ })
        .first()
        .click()
      await expect(page.getByRole('dialog')).toBeVisible()
      await page.waitForTimeout(500)
      await page.screenshot({ path: `e2e/__screenshots__/f3/${name('sheet')}.png` })
    })
  }

  test('timetable of a group with odd / even subgroups', async ({ page }, testInfo) => {
    const project = testInfo.project.name
    await signIn(page, 'xolmatov.u')
    await page.goto('/student/timetable?date=2026-10-05')
    await expect(page.getByText(uz.timetable.parity.EVEN)).toBeVisible()
    await shot(page, 'f3', `timetable-even.light.uz.${project}`)
    await page.goto('/student/timetable?date=2026-10-12')
    await expect(page.getByText(uz.timetable.parity.ODD)).toBeVisible()
    await shot(page, 'f3', `timetable-odd.light.uz.${project}`)
  })

  for (const theme of ['light', 'dark'] as const) {
    test(`grades, attendance and the rest ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(120_000)
      const project = testInfo.project.name
      const name = (screen: string) => `${screen}.${theme}.uz.${project}`
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')
      await shot(page, 'f3', name('dashboard'))

      await page.goto('/student/grades')
      await expect(page.getByRole('heading', { level: 1, name: uz.nav.grades })).toBeVisible()
      await page.getByText(uz.grades.historyTitle).first().click()
      await shot(page, 'f3', name('grades'))

      await page.goto('/student/attendance')
      await shot(page, 'f3', name('attendance'))

      await page.goto('/notifications')
      await shot(page, 'f3', name('notifications'))

      await page.goto('/announcements')
      await shot(page, 'f3', name('announcements'))

      await page.goto('/profile')
      await shot(page, 'f3', name('profile'))

      // the bell and the search of the top bar
      await page.goto('/student')
      await page.getByRole('button', { name: /^Bildirishnomalar/ }).click()
      await page.waitForTimeout(800)
      await page.screenshot({ path: `e2e/__screenshots__/f3/${name('bell')}.png` })
      await page.keyboard.press('Escape')

      if (isPhone(testInfo)) await page.getByRole('button', { name: uz.common.search, exact: true }).click()
      else await page.keyboard.press('Control+k')
      await page.getByRole('combobox').fill('massiv')
      await expect(page.getByText(uz.search.results)).toBeVisible()
      await page.waitForTimeout(400)
      await page.screenshot({ path: `e2e/__screenshots__/f3/${name('search')}.png` })
    })
  }
})
