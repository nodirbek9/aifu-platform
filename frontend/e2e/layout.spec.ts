import { expect, test, type Page } from '@playwright/test'
import { quizIdByCode } from './db'
import { expectFitsScreen, signIn } from './support'

const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'
const FIRST_PRACTICAL_LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'
const FRIDAY_PRACTICAL = '73429519-f2db-4531-a5b3-78afca89cac4'

async function sweep(page: Page, paths: readonly string[]): Promise<void> {
  for (const path of paths) {
    await page.goto(path)
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
    await page.waitForLoadState('networkidle')
    await expectFitsScreen(page)
  }
}

/** Every screen fits a 390 px phone: no sideways scrolling anywhere (spec G-02). */
test.describe('every screen fits a phone', () => {
  test.skip(({ isMobile }) => !isMobile, 'the sweep is for the phone')

  test('the student', async ({ page }) => {
    test.setTimeout(240_000)
    await signIn(page, 'student3')
    await sweep(page, [
      '/student',
      '/student/timetable',
      '/student/courses',
      `/student/courses/${DASTURLASH}`,
      `/student/courses/${DASTURLASH}?tab=syllabus`,
      `/student/courses/${DASTURLASH}?tab=work`,
      `/student/courses/${DASTURLASH}?tab=grades`,
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

  test('the teacher', async ({ page }) => {
    test.setTimeout(240_000)
    await page.addInitScript(() => {
      localStorage.setItem('aifu-active-role', 'TEACHER')
    })
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
})
