import { test } from '@playwright/test'
import { resetLocale, shot, signIn } from './support'

/**
 * Review screenshots of the student's core (phase F1): `npm run shots`. Saved under
 * e2e/__screenshots__/f1/ and looked at against docs/04-DESIGN-BRIEF.md.
 */
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'
const PRACTICAL_WITH_TASK = '074bb8b9-c9e5-444e-8657-305ca1509c85'

test.describe('@shots F1', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`student screens ${theme}`, async ({ page }, testInfo) => {
      const project = testInfo.project.name
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')
      await shot(page, 'f1', `dashboard.${theme}.uz.${project}`)

      await page.goto('/student/courses')
      await shot(page, 'f1', `courses.${theme}.uz.${project}`)

      await page.goto(`/student/courses/${DASTURLASH}`)
      await shot(page, 'f1', `course-plan.${theme}.uz.${project}`)
      await page.goto(`/student/courses/${DASTURLASH}?tab=syllabus`)
      await shot(page, 'f1', `course-syllabus.${theme}.uz.${project}`)
      await page.goto(`/student/courses/${DASTURLASH}?tab=work`)
      await shot(page, 'f1', `course-work.${theme}.uz.${project}`)

      await page.goto(`/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`)
      await shot(page, 'f1', `lesson-lecture.${theme}.uz.${project}`)
      await page.goto(`/student/courses/${DASTURLASH}/lessons/${PRACTICAL_WITH_TASK}`)
      await shot(page, 'f1', `lesson-practical.${theme}.uz.${project}`)
    })
  }

  test('student screens in Russian', async ({ page }, testInfo) => {
    const project = testInfo.project.name
    await signIn(page, 'student3')
    await page.evaluate(() => {
      localStorage.setItem('aifu-locale', 'ru')
    })
    await page.goto('/student')
    await shot(page, 'f1', `dashboard.light.ru.${project}`)
    await page.goto(`/student/courses/${DASTURLASH}`)
    await shot(page, 'f1', `course-plan.light.ru.${project}`)
    await page.goto(`/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`)
    await shot(page, 'f1', `lesson-lecture.light.ru.${project}`)
    await resetLocale(page.request, 'student3')
  })
})
