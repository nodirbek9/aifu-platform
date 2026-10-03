import { expect, test } from '@playwright/test'
import { shot, signIn } from './support'

/** Review screenshots of the teacher in the classroom (phase F4a): `npm run shots`. */
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** The Friday practical of Dasturlash, taught by n.ayitbayev. */
const FRIDAY_PRACTICAL = '73429519-f2db-4531-a5b3-78afca89cac4'
/** A1, the first practical: it has a teacher-only lesson plan. */
const FIRST_PRACTICAL_LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'

test.describe('@shots F4a', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`teacher ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(180_000)
      const project = testInfo.project.name
      const name = (screen: string) => `${screen}.${theme}.uz.${project}`
      await page.clock.setFixedTime(new Date('2026-10-02T04:10:00Z'))
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
        localStorage.setItem('aifu-active-role', 'TEACHER')
      }, theme)
      await signIn(page, 'n.ayitbayev')
      await page.goto('/teacher')
      await shot(page, 'f4a', name('dashboard'))

      await page.goto('/teacher/timetable?date=2026-09-28')
      await shot(page, 'f4a', name('timetable'))

      await page.goto(`/teacher/classes/${FRIDAY_PRACTICAL}/2026-09-25`)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f4a', name('class-session'))
      await page.goto(`/teacher/classes/${FRIDAY_PRACTICAL}/2026-09-04`)
      await expect(page.getByText(/Tashkiliy qism/)).toBeVisible()
      await shot(page, 'f4a', name('class-session-plan'))

      await page.goto(`/teacher/lessons/${FIRST_PRACTICAL_LESSON}/present`)
      await shot(page, 'f4a', name('present-title'))
      await page.keyboard.press('ArrowRight')
      await page.keyboard.press('ArrowRight')
      await shot(page, 'f4a', name('present-slide'))

      for (const tab of ['plan', 'journal', 'students', 'announcements']) {
        await page.goto(`/teacher/courses/${DASTURLASH}?tab=${tab}`)
        await shot(page, 'f4a', name(`course-${tab}`))
      }
      await page.goto('/teacher/groups')
      await shot(page, 'f4a', name('groups'))
    })
  }
})
