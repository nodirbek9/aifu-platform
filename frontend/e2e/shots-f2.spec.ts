import { expect, test } from '@playwright/test'
import { isPhone, shot, signIn, texts } from './support'

/** Review screenshots of the practice task and the work list (phase F2): `npm run shots`. */
const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const TASK_WITH_HINTS = '1240550f-5bcc-4155-a17d-1a9c34945a03'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'

test.describe('@shots F2', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`task page ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(120_000)
      const project = testInfo.project.name
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')
      await page.goto(`/student/courses/${DASTURLASH}/tasks/${TASK_WITH_HINTS}`)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await shot(page, 'f2', `task.${theme}.uz.${project}`)

      if (isPhone(testInfo)) {
        await page.getByRole('radio', { name: uz.task.phoneCode, exact: true }).click()
        await shot(page, 'f2', `task-code.${theme}.uz.${project}`)
      }
      // the starter code does not solve the task: the examples fail, with what differed
      await page.getByRole('button', { name: uz.task.runExamples }).click()
      await expect(page.getByText(/\d+ \/ \d+ test/)).toBeVisible({ timeout: 45_000 })
      await shot(page, 'f2', `task-examples.${theme}.uz.${project}`)
    })
  }

  test('lesson with highlighted code and the runner, work list', async ({ page }, testInfo) => {
    test.setTimeout(120_000)
    const project = testInfo.project.name
    await signIn(page, 'student3')
    await page.goto(`/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`)
    const tryIt = page.getByRole('button', { name: uz.lesson.tryIt }).first()
    await tryIt.scrollIntoViewIfNeeded()
    await page.screenshot({ path: `e2e/__screenshots__/f2/lesson-code.light.uz.${project}.png` })
    await tryIt.click()
    await page.getByLabel(uz.task.customInput).fill('1\n2\n3\n')
    await page.getByRole('button', { name: uz.task.run, exact: true }).click()
    await expect(page.getByText(uz.verdict.OK, { exact: true })).toBeVisible({ timeout: 45_000 })
    await page.screenshot({ path: `e2e/__screenshots__/f2/lesson-try-it.light.uz.${project}.png` })

    await page.goto('/student/assignments')
    await shot(page, 'f2', `assignments.light.uz.${project}`)
  })
})
