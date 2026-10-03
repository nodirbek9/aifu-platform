import AxeBuilder from '@axe-core/playwright'
import { expect, test, type Page } from '@playwright/test'
import { quizIdByCode } from './db'
import { signIn } from './support'

const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'
const FIRST_PRACTICAL_LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'
const FRIDAY_PRACTICAL = '73429519-f2db-4531-a5b3-78afca89cac4'

/** Every violation of WCAG 2.1 A / AA axe can find, listed with the elements, or none. */
async function audit(page: Page, path: string): Promise<string[]> {
  await page.goto(path)
  await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
  await page.waitForLoadState('networkidle')
  const result = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
    .analyze()
  return result.violations.map(
    (violation) =>
      `${path} — ${violation.id}: ${violation.help} (${violation.nodes
        .slice(0, 3)
        .map((node) => node.target.join(' '))
        .join(' | ')})`,
  )
}

async function sweep(page: Page, paths: readonly string[]): Promise<void> {
  const found: string[] = []
  for (const path of paths) found.push(...(await audit(page, path)))
  expect(found, found.join('\n')).toEqual([])
}

/** Spec §3: every route passes axe in both themes. */
test.describe('accessibility (axe, WCAG 2.1 AA)', () => {
  test.skip(({ isMobile }) => isMobile, 'the routes are the same on the phone; axe runs once')
  // entrances finished: contrast is measured on the page at rest, not half faded in
  test.use({ contextOptions: { reducedMotion: 'reduce' } })

  for (const theme of ['light', 'dark'] as const) {
    test(`the sign-in page, ${theme}`, async ({ page }) => {
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await sweep(page, ['/login'])
    })

    test(`the student, ${theme}`, async ({ page }) => {
      test.setTimeout(240_000)
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')
      await sweep(page, [
        '/student',
        '/student/timetable',
        '/student/courses',
        `/student/courses/${DASTURLASH}`,
        `/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`,
        `/student/courses/${DASTURLASH}/tasks/${TASK}`,
        `/student/courses/${DASTURLASH}/quizzes/${quizIdByCode('DAS-Q-M2')}`,
        '/student/assignments',
        '/student/grades',
        '/student/attendance',
        '/notifications',
        '/announcements',
        '/profile',
      ])
    })

    test(`the teacher, ${theme}`, async ({ page }) => {
      test.setTimeout(240_000)
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
        localStorage.setItem('aifu-active-role', 'TEACHER')
      }, theme)
      await signIn(page, 'n.ayitbayev')
      await sweep(page, [
        '/teacher',
        '/teacher/timetable',
        '/teacher/courses',
        ...[
          'plan',
          'content',
          'practice',
          'review',
          'grades',
          'analytics',
          'journal',
          'students',
          'announcements',
        ].map((tab) => `/teacher/courses/${DASTURLASH}?tab=${tab}`),
        `/teacher/classes/${FRIDAY_PRACTICAL}/2026-10-02`,
        '/teacher/journal',
        '/teacher/groups',
        `/teacher/lessons/${FIRST_PRACTICAL_LESSON}/edit`,
        `/teacher/coding-tasks/${TASK}/edit`,
        `/teacher/quizzes/${quizIdByCode('DAS-Q-M2')}/edit`,
      ])
    })

    test(`the administrator, ${theme}`, async ({ page }) => {
      test.setTimeout(240_000)
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'admin')
      await sweep(page, [
        '/admin',
        '/admin/timetable',
        '/admin/users',
        '/admin/users/import',
        '/admin/structure',
        '/admin/structure?tab=courses',
        `/admin/structure/courses/${DASTURLASH}`,
        '/admin/reference',
        '/admin/reference?tab=rooms',
        '/admin/reference?tab=bells',
        '/admin/reference?tab=terms',
        '/admin/load',
        '/admin/audit',
      ])
    })
  }
})
