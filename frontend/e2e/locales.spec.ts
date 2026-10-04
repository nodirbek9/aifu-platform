import { expect, test, type Page, type TestInfo } from '@playwright/test'
import { quizIdByCode } from './db'
import { isPhone, signIn } from './support'

const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'
const FIRST_PRACTICAL_LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'

/** A catalogue key shown instead of its text: i18next prints the key when a text is missing. */
const RAW_KEY =
  /\b(?:common|nav|admin|authoring|builder|review|gradebook|analytics|teaching|task|quiz|lesson|grades|courses|dashboard|timetable|profile|errors|sessionType|role|attendance|announcements|notification|search|verdict|chart|auth|system)\.[a-zA-Z]+/

const LANGUAGE = { ru: 'RU', en: 'EN' } as const

async function switchTo(page: Page, locale: keyof typeof LANGUAGE, testInfo: TestInfo): Promise<void> {
  if (isPhone(testInfo)) {
    // on a phone the language switch is in the profile page
    await page.goto('/profile')
  }
  await page.getByRole('radio', { name: LANGUAGE[locale], exact: true }).first().click()
  await expect(page.locator('html')).toHaveAttribute('lang', locale)
}

async function sweep(page: Page, paths: readonly string[], testInfo: TestInfo): Promise<void> {
  const problems: string[] = []
  for (const path of paths) {
    await page.goto(path)
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
    await page.waitForLoadState('networkidle')
    const text = await page.locator('body').innerText()
    const raw = RAW_KEY.exec(text)
    if (raw !== null) problems.push(`${path}: shows the key ${raw[0]}`)
    if (isPhone(testInfo)) {
      const width = await page.evaluate(() => document.documentElement.scrollWidth)
      if (width > (page.viewportSize()?.width ?? 390) + 1) problems.push(`${path}: ${width}px wide`)
    }
  }
  expect(problems, problems.join('\n')).toEqual([])
}

/** Spec §9 "in all three languages": every screen in Russian and English, on desktop and phone. */
for (const locale of ['ru', 'en'] as const) {
  test.describe(`every screen in ${locale}`, () => {
    test(`the student (${locale})`, async ({ page }, testInfo) => {
      test.setTimeout(240_000)
      await signIn(page, 'student3')
      await switchTo(page, locale, testInfo)
      await sweep(
        page,
        [
          '/student',
          '/student/timetable',
          '/student/courses',
          `/student/courses/${DASTURLASH}`,
          `/student/courses/${DASTURLASH}?tab=syllabus`,
          `/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`,
          `/student/courses/${DASTURLASH}/tasks/${TASK}`,
          `/student/courses/${DASTURLASH}/quizzes/${quizIdByCode('DAS-Q-M2')}`,
          '/student/assignments',
          '/student/grades',
          '/student/attendance',
          '/notifications',
          '/profile',
        ],
        testInfo,
      )
    })

    test(`the teacher (${locale})`, async ({ page }, testInfo) => {
      test.setTimeout(300_000)
      await page.addInitScript(() => {
        localStorage.setItem('aifu-active-role', 'TEACHER')
      })
      await signIn(page, 'n.ayitbayev')
      await switchTo(page, locale, testInfo)
      await sweep(
        page,
        [
          '/teacher',
          '/teacher/timetable',
          ...['plan', 'content', 'practice', 'review', 'grades', 'analytics', 'journal'].map(
            (tab) => `/teacher/courses/${DASTURLASH}?tab=${tab}`,
          ),
          `/teacher/lessons/${FIRST_PRACTICAL_LESSON}/edit`,
          `/teacher/coding-tasks/${TASK}/edit`,
          `/teacher/quizzes/${quizIdByCode('DAS-Q-M2')}/edit`,
          '/teacher/groups',
        ],
        testInfo,
      )
    })

    test(`the administrator (${locale})`, async ({ page }, testInfo) => {
      test.setTimeout(240_000)
      await signIn(page, 'admin')
      await switchTo(page, locale, testInfo)
      await sweep(
        page,
        [
          '/admin',
          '/admin/timetable',
          '/admin/users',
          '/admin/users/import',
          '/admin/structure',
          `/admin/structure/courses/${DASTURLASH}`,
          '/admin/reference',
          '/admin/reference?tab=terms',
          '/admin/load',
          '/admin/audit',
        ],
        testInfo,
      )
    })
  })
}
