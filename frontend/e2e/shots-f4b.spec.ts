import { expect, test } from '@playwright/test'
import { quizIdByCode } from './db'
import { shot, signIn } from './support'

/** Review screenshots of the teacher's tools (phase F4b): `npm run shots`. */
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** A1, the first practical: text, code, and a teacher-only lesson plan. */
const FIRST_PRACTICAL_LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'
/** DAS-S1-A8 "Koʻpaytirish jadvali". */
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'

test.describe('@shots F4b', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`teacher tools ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(240_000)
      const project = testInfo.project.name
      const name = (screen: string) => `${screen}.${theme}.uz.${project}`
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
        localStorage.setItem('aifu-active-role', 'TEACHER')
      }, theme)
      await signIn(page, 'n.ayitbayev')

      for (const tab of ['content', 'practice', 'review', 'grades', 'analytics']) {
        await page.goto(`/teacher/courses/${DASTURLASH}?tab=${tab}`)
        await expect(page.getByRole('tabpanel')).toBeVisible()
        await shot(page, 'f4b', name(`course-${tab}`))
      }

      await page.goto(`/teacher/lessons/${FIRST_PRACTICAL_LESSON}/edit`)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f4b', name('lesson-editor'))

      await page.goto(`/teacher/coding-tasks/${TASK}/edit`)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f4b', name('task-statement'))
      for (const tab of [/^Kod va yechimlar/, /^Testlar/, /^Yechimlarni tekshirish/]) {
        await page.getByRole('tab', { name: tab }).click()
        await shot(
          page,
          'f4b',
          name(
            `task-${String(tab)
              .replace(/[^a-z]/gi, '')
              .slice(0, 12)}`,
          ),
        )
      }

      await page.goto(`/teacher/quizzes/${quizIdByCode('DAS-Q-M2')}/edit`)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f4b', name('quiz-editor'))
    })
  }
})
