import { expect, test } from '@playwright/test'
import { signIn, texts } from './support'

const { uz, ru } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'

test.describe('the student learns (S-01, S-03, S-04)', () => {
  test('the dashboard shows the day, where to continue, deadlines and grades', async ({ page }) => {
    await signIn(page, 'student3')

    await expect(page.getByRole('heading', { level: 1, name: 'Dev Student Three' })).toBeVisible()
    await expect(page.getByText('BK-DI26/1')).toBeVisible()
    for (const title of [
      uz.dashboard.todayTitle,
      uz.dashboard.continueTitle,
      uz.dashboard.deadlinesTitle,
      uz.dashboard.gradesTitle,
    ]) {
      await expect(page.getByRole('heading', { level: 3, name: title })).toBeVisible()
    }
    // real data arrived in every block: no skeleton is left
    await expect(page.locator('.animate-pulse')).toHaveCount(0)
    await expect(page.getByRole('link', { name: uz.dashboard.continueAction })).toBeVisible()
  })

  test('the courses list leads to a course with its plan, syllabus and work', async ({ page }) => {
    await signIn(page, 'student3')
    const navigation = page.getByRole('navigation', { name: uz.common.mainNavigation })
    await navigation.getByRole('link', { name: uz.nav.courses }).click()
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.courses })).toBeVisible()

    await page.getByRole('link', { name: 'Dasturlash (2026/2027, 1-semestr)' }).click()
    await expect(page).toHaveURL(new RegExp(`/student/courses/${DASTURLASH}$`))
    await expect(page.getByRole('heading', { level: 2, name: 'Dasturlash asoslari' })).toBeVisible()

    await page.getByRole('tab', { name: uz.courses.tabSyllabus }).click()
    await expect(page).toHaveURL(/tab=syllabus/)
    await expect(page.getByRole('heading', { level: 2, name: uz.courses.outcomes })).toBeVisible()
    await expect(page.getByRole('heading', { level: 2, name: uz.courses.assessment })).toBeVisible()

    await page.getByRole('tab', { name: uz.courses.tabWork }).click()
    await expect(page.getByText('Kabisa yili')).toBeVisible()

    // the tab is part of the address: a reload stays on it
    await page.reload()
    await expect(page.getByRole('tab', { name: uz.courses.tabWork })).toHaveAttribute('data-state', 'active')
  })

  test('a lesson is read, marked as done, and the plan shows it', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto(`/student/courses/${DASTURLASH}`)
    await page.getByRole('link', { name: uz.dashboard.continueAction }).first().click()
    await expect(page).toHaveURL(/\/lessons\//)
    const title = await page.getByRole('article').getByRole('heading', { level: 1 }).innerText()

    // the chapter is there: its text under headings
    await expect(page.getByRole('article').getByRole('heading', { level: 2 }).first()).toBeVisible()
    await page.getByRole('button', { name: uz.lesson.markDone }).click()
    await expect(page.getByText(uz.lesson.done)).toBeVisible()

    await page.getByRole('link', { name: 'Dasturlash (2026/2027, 1-semestr)' }).click()
    const row = page.getByRole('listitem').filter({ hasText: title })
    await expect(row.getByText(uz.lessonStatus.COMPLETED)).toBeVisible()
  })

  test('switching the language keeps the lesson on screen and marks untranslated text', async ({
    page,
  }, testInfo) => {
    await signIn(page, 'student3')
    await page.goto(`/student/courses/${DASTURLASH}`)
    await page.getByRole('link', { name: /Dasturlashning asosiy tushunchalari/ }).click()
    await expect(page).toHaveURL(/\/lessons\//)
    const heading = page.getByRole('article').getByRole('heading', { level: 2 }).first()
    await expect(heading).toBeVisible()
    await expect(page.getByText(uz.common.shownInUzbek)).toHaveCount(0)

    if (testInfo.project.name === 'phone') {
      await page.getByRole('button', { name: new RegExp(`^${uz.common.language}`) }).click()
      await page.getByRole('menuitem', { name: 'RU' }).click()
    } else {
      await page.getByRole('radio', { name: 'RU' }).click()
    }

    // the lesson exists in Uzbek only: the text stays, with a note that it is a fallback
    await expect(page.getByText(ru.common.shownInUzbek)).toBeVisible()
    await expect(heading).toBeVisible()
    await expect(page.getByRole('navigation', { name: ru.lesson.neighbours })).toBeVisible()
  })

  test('a lesson of a course the student is not in is not shown', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto(`/student/courses/${DASTURLASH}/lessons/00000000-0000-0000-0000-000000000000`)
    await expect(page.getByRole('alert')).toContainText(uz.errors.RESOURCE_NOT_FOUND)
  })
})
