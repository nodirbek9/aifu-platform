import { randomUUID } from 'node:crypto'
import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { clearAuthoringLeftovers, clearDeadline, resetReviewedStudent } from './db'
import { demoPassword, isPhone, signIn, texts } from './support'

const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** DAS-S1-A8 "Koʻpaytirish jadvali": the task the reviewed student submits to. */
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'
const TASK_CODE = 'DAS-S1-A8'
/** A1, the first practical: every kind of block, the lesson plan among them. */
const LESSON = 'ec38dba3-8d16-4e24-9bb2-5eb8fd329ab4'
const REVIEWED = 'student4'
const REVIEWED_NAME = 'Dev Student Four'
const SOLUTION = `n = int(input())
for i in range(1, n + 1):
    print(" ".join(str(i * j) for j in range(1, n + 1)))
`

const label = (template: string, values: Record<string, string | number>) =>
  Object.entries(values).reduce((text, [key, value]) => text.replace(`{{${key}}}`, String(value)), template)

async function signInAsTeacher(page: Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem('aifu-active-role', 'TEACHER')
  })
  await signIn(page, 'n.ayitbayev')
}

async function apiToken(request: APIRequestContext, username: string): Promise<string> {
  const response = await request.post('/api/v1/auth/login', {
    data: { identifier: username, password: demoPassword() },
  })
  expect(response.ok()).toBe(true)
  const body = (await response.json()) as { accessToken: string }
  return body.accessToken
}

/** The student sends a correct solution and the sandbox checks it, as in the student's own flow. */
async function submitAsStudent(request: APIRequestContext): Promise<string> {
  const token = await apiToken(request, REVIEWED)
  const headers = { Authorization: `Bearer ${token}` }
  const created = await request.post('/api/v1/submissions', {
    headers: { ...headers, 'Idempotency-Key': randomUUID() },
    data: { codingTaskId: TASK, languageCode: 'python', sourceCode: SOLUTION },
  })
  expect(created.ok()).toBe(true)
  const { id } = (await created.json()) as { id: string }
  await expect
    .poll(
      async () => {
        const detail = await request.get(`/api/v1/submissions/${id}`, { headers })
        return ((await detail.json()) as { status: string }).status
      },
      { timeout: 60_000, intervals: [1000] },
    )
    .toBe('COMPLETED')
  return id
}

async function expectNoSidewaysScroll(page: Page): Promise<void> {
  // on an emulated phone the layout viewport grows with the content: compare with the screen
  const screenWidth = page.viewportSize()?.width ?? 390
  await expect
    .poll(() => page.evaluate(() => document.documentElement.scrollWidth))
    .toBeLessThanOrEqual(screenWidth + 1)
}

test.describe('the teacher writes the course (spec T-04 … T-06)', () => {
  test.beforeAll(() => {
    clearAuthoringLeftovers()
  })

  test('a module and a lesson are made, written in two languages, previewed, reordered and removed', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'editing is checked on the desktop; the phone gets the layout check below')
    test.setTimeout(150_000)
    const stamp = Date.now().toString(36)
    const moduleTitle = `E2E modul ${stamp}`
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=content`)

    // a module, then a lesson in it
    await page.getByRole('button', { name: uz.authoring.newModule }).click()
    await page
      .getByRole('dialog')
      .getByLabel(label(uz.authoring.titleIn, { locale: 'UZ' }))
      .fill(moduleTitle)
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.create }).click()
    const module = page.getByRole('region', { name: moduleTitle })
    await expect(module).toBeVisible()
    await module.getByRole('button', { name: uz.authoring.newLesson }).click()
    await page
      .getByRole('dialog')
      .getByLabel(label(uz.authoring.titleIn, { locale: 'UZ' }))
      .fill('E2E dars')
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.create }).click()
    await module.getByRole('link', { name: 'E2E dars' }).click()
    await expect(page).toHaveURL(/\/teacher\/lessons\/[\w-]+\/edit$/)
    await expect(page.getByRole('heading', { level: 1, name: 'E2E dars' })).toBeVisible()

    // a text block in Uzbek, seen in the preview as the student will see it
    await page.getByRole('button', { name: uz.authoring.addBlock }).click()
    await page
      .getByRole('dialog')
      .getByRole('button', { name: uz.authoring.blockType.TEXT, exact: true })
      .click()
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.add, exact: true }).click()
    const text = page.getByRole('article', {
      name: label(uz.authoring.blockLabel, { position: 1, type: uz.authoring.blockType.TEXT }),
    })
    // typed in the visual editor: a heading from the toolbar, then a paragraph
    await text.getByRole('textbox', { name: uz.authoring.content }).click()
    await text.getByRole('button', { name: uz.authoring.format.h2, exact: true }).click()
    await page.keyboard.type('Kirish')
    await page.keyboard.press('Enter')
    await page.keyboard.type('Salom, dunyo')
    await expect(text.getByText(uz.quiz.saved, { exact: true })).toBeVisible({ timeout: 10_000 })
    const preview = page.getByRole('complementary', { name: uz.authoring.preview })
    await expect(preview.getByText('Salom, dunyo')).toBeVisible()

    // the Russian text of the same block: missing until written, then shown in the Russian preview
    await expect(text.getByRole('button', { name: /^ru/ })).toContainText(uz.authoring.translationMissing)
    await text.getByRole('button', { name: /^ru/ }).click()
    await text.getByRole('radio', { name: 'HTML' }).click()
    await text.getByLabel(uz.authoring.content).fill('<p>Привет, мир</p>')
    await expect(text.getByText(uz.quiz.saved, { exact: true })).toBeVisible({ timeout: 10_000 })
    await preview.getByRole('radio', { name: 'RU' }).click()
    await expect(preview.getByText('Привет, мир')).toBeVisible()

    // a callout, moved above the text with the keyboard
    await page.getByRole('button', { name: uz.authoring.addBlock }).click()
    await page
      .getByRole('dialog')
      .getByRole('button', { name: uz.authoring.blockType.CALLOUT, exact: true })
      .click()
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.add, exact: true }).click()
    await page
      .getByRole('button', { name: label(uz.authoring.dragBlock, { position: 2 }) })
      .press('Alt+ArrowUp')
    await expect(
      page.getByRole('article', {
        name: label(uz.authoring.blockLabel, { position: 1, type: uz.authoring.blockType.CALLOUT }),
      }),
    ).toBeVisible()

    // everything is on the server
    await page.reload()
    const reloaded = page.getByRole('article', {
      name: label(uz.authoring.blockLabel, { position: 2, type: uz.authoring.blockType.TEXT }),
    })
    await reloaded.getByRole('radio', { name: 'HTML' }).click()
    await expect(reloaded.getByLabel(uz.authoring.content)).toHaveValue('<h2>Kirish</h2><p>Salom, dunyo</p>')

    // published and taken back
    await page.getByRole('button', { name: uz.authoring.publish, exact: true }).click()
    await expect(page.getByRole('button', { name: uz.authoring.unpublish, exact: true })).toBeVisible()
    await page.getByRole('button', { name: uz.authoring.unpublish, exact: true }).click()
    await expect(page.getByRole('button', { name: uz.authoring.publish, exact: true })).toBeVisible()

    // the lesson and the module are removed from the tree
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=content`)
    await module
      .getByRole('button', { name: label(uz.authoring.lessonActions, { title: 'E2E dars' }) })
      .click()
    await page.getByRole('menuitem', { name: uz.authoring.delete }).click()
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.delete }).click()
    await expect(module.getByRole('link', { name: 'E2E dars' })).toHaveCount(0)
    await module
      .getByRole('button', { name: label(uz.authoring.moduleActions, { title: moduleTitle }) })
      .click()
    await page.getByRole('menuitem', { name: uz.authoring.delete }).click()
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.delete }).click()
    await expect(module).toHaveCount(0)
  })

  test('a coding task gets its statement, a Python solution, a test, and passes the verification', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'editing is checked on the desktop')
    test.setTimeout(180_000)
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=practice`)
    await page.getByRole('button', { name: uz.authoring.newTask }).click()
    await page
      .getByRole('dialog')
      .getByLabel(label(uz.authoring.titleIn, { locale: 'UZ' }))
      .fill('E2E yigʻindi')
    await page.getByRole('dialog').getByLabel(uz.authoring.taskDescription).fill('Ikki sonning yigʻindisi.')
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.create }).click()
    await expect(page).toHaveURL(/\/teacher\/coding-tasks\/[\w-]+\/edit$/)

    await page.getByRole('radio', { name: 'HTML' }).first().click()
    await page
      .getByLabel(uz.authoring.taskDescription)
      .fill('<p>Ikki butun sonning yigʻindisini chiqaring.</p>')
    await expect(page.getByText(uz.quiz.saved, { exact: true }).first()).toBeVisible({ timeout: 10_000 })

    await page.getByRole('tab', { name: uz.authoring.taskCode }).click()
    await page
      .getByRole('button', { name: label(uz.authoring.attachLanguage, { language: 'Python' }) })
      .click()
    await page
      .getByRole('textbox', { name: `${uz.authoring.solutionCode} — Python` })
      .fill('a, b = map(int, input().split())\nprint(a + b)\n')
    await expect(page.getByText(uz.quiz.saved, { exact: true }).first()).toBeVisible({ timeout: 10_000 })

    await page.getByRole('tab', { name: /^Testlar/ }).click()
    await page.getByRole('button', { name: uz.authoring.addTest }).click()
    const test1 = page.getByRole('article', { name: label(uz.authoring.testNumber, { number: 1 }) })
    await test1.getByLabel(uz.authoring.stdin).fill('2 3')
    await test1.getByLabel(uz.authoring.expectedStdout).fill('5')
    await expect(test1.getByText(uz.quiz.saved, { exact: true })).toBeVisible({ timeout: 10_000 })

    await page.getByRole('tab', { name: uz.authoring.verify }).click()
    await page.getByRole('button', { name: uz.authoring.verifyRun }).click()
    await expect(page.getByText(uz.authoring.verifyPassed)).toBeVisible({ timeout: 90_000 })
  })

  test('a lesson gets a lesson to be done first and a learning outcome, then both are removed', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the editor is used on the desktop')
    await signInAsTeacher(page)
    await page.goto(`/teacher/lessons/${LESSON}/edit`)
    await expect(page.getByText(uz.authoring.noPrerequisites)).toBeVisible()

    await page.getByRole('combobox', { name: uz.authoring.addPrerequisite }).click()
    await page.getByRole('option', { name: /^M1 · / }).click()
    const remove = page.getByRole('button', { name: /^Olib tashlash: M1 · / })
    await expect(remove).toBeVisible()

    await page.getByRole('combobox', { name: uz.authoring.addOutcome }).click()
    const outcome = page.getByRole('option').first()
    const outcomeLabel = (await outcome.textContent()) ?? ''
    await outcome.click()
    const removeOutcome = page.getByRole('button', { name: `Olib tashlash: ${outcomeLabel}` })
    await expect(removeOutcome).toBeVisible()

    // the links are the server's: a reload keeps them
    await page.reload()
    await expect(remove).toBeVisible()
    await expect(removeOutcome).toBeVisible()

    // A1 must stay open for the other flows: both links go again
    await remove.click()
    await expect(page.getByText(uz.authoring.noPrerequisites)).toBeVisible()
    await removeOutcome.click()
    await expect(page.getByText(uz.authoring.noOutcomes)).toBeVisible()
  })

  test('a task given as course work gets a deadline the students will see', async ({ page }, testInfo) => {
    test.skip(isPhone(testInfo), 'the course work is managed on the desktop')
    try {
      await signInAsTeacher(page)
      await page.goto(`/teacher/courses/${DASTURLASH}?tab=practice`)
      const work = page.getByRole('list', { name: uz.authoring.courseWork })
      const row = work.getByRole('listitem').filter({ hasText: 'Yigʻindisi k ga teng juftliklar' })
      await expect(row).toContainText(uz.authoring.noDeadline)
      await row.getByLabel(uz.authoring.deadline).fill('2026-12-20T18:00')
      await row.getByRole('button', { name: uz.authoring.saveDeadline }).click()
      await expect(row).toContainText('Muddat:')
      await page.reload()
      await expect(row).not.toContainText(uz.authoring.noDeadline)
    } finally {
      clearDeadline('DAS-S1-MT6')
    }
  })

  test('a quiz gets a question with options, the right one marked, and is published', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'editing is checked on the desktop')
    test.setTimeout(120_000)
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=practice`)
    await page.getByRole('button', { name: uz.authoring.newQuiz }).click()
    await page
      .getByRole('dialog')
      .getByLabel(label(uz.authoring.titleIn, { locale: 'UZ' }))
      .fill('E2E test')
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.create }).click()
    await expect(page).toHaveURL(/\/teacher\/quizzes\/[\w-]+\/edit$/)

    // publishing an empty quiz is refused, and the reason is shown
    await page.getByRole('button', { name: uz.authoring.publish, exact: true }).click()
    await expect(
      page.getByRole('alert').filter({ hasText: uz.errors.PUBLICATION_VALIDATION_FAILED }),
    ).toBeVisible()

    await page.getByRole('button', { name: uz.authoring.addQuestion }).click()
    await page.getByLabel(uz.authoring.prompt).last().fill('2 + 2 nechaga teng?')
    await page.getByRole('button', { name: uz.authoring.add, exact: true }).click()
    const question = page.getByRole('article', { name: label(uz.authoring.questionNumber, { number: 1 }) })
    await expect(question).toBeVisible()
    await question.getByRole('button', { name: uz.authoring.addOption }).click()
    await question.getByRole('textbox', { name: label(uz.authoring.optionNumber, { number: 1 }) }).fill('3')
    await question.getByRole('button', { name: uz.authoring.addOption }).click()
    await question.getByRole('textbox', { name: label(uz.authoring.optionNumber, { number: 2 }) }).fill('4')
    await question.getByRole('checkbox', { name: uz.authoring.correct }).nth(1).click()
    await expect(question.getByRole('checkbox', { name: uz.authoring.correct }).nth(1)).toBeChecked()

    await page.getByRole('button', { name: uz.authoring.publish, exact: true }).click()
    await expect(page.getByRole('button', { name: uz.authoring.unpublish, exact: true })).toBeVisible()
    await page.getByRole('button', { name: uz.authoring.unpublish, exact: true }).click()
    await expect(page.getByRole('button', { name: uz.authoring.publish, exact: true })).toBeVisible()
  })
})

test.describe('the teacher reviews and grades (spec §9, step 6)', () => {
  test('a submission is reviewed, commented and graded; the student sees both', async ({
    page,
    browser,
    request,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the review is written on the desktop')
    test.setTimeout(180_000)
    resetReviewedStudent(REVIEWED, TASK_CODE)
    await submitAsStudent(request)

    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=review`)
    await page.getByRole('link', { name: REVIEWED_NAME }).click()
    await expect(page.getByRole('heading', { level: 1, name: REVIEWED_NAME })).toBeVisible()
    await expect(page.getByRole('textbox', { name: uz.review.code })).toContainText('range(1, n + 1)')

    const comment = 'Toʻgʻri yechim. Ichki siklni alohida funksiyaga ajrating.'
    await page.getByLabel(uz.review.writeFeedback).fill(comment)
    await page.getByRole('button', { name: uz.review.send }).click()
    await expect(page.getByRole('article').filter({ hasText: comment })).toBeVisible()

    await page.getByRole('combobox', { name: uz.review.component }).click()
    await page.getByRole('option', { name: /^JN_PRACTICAL/ }).click()
    await page.getByLabel(uz.gradebook.score).fill('9')
    await page.getByRole('button', { name: uz.gradebook.save }).click()
    await expect(page.getByText(uz.review.gradeSaved, { exact: true })).toBeVisible()

    // the queue no longer lists it as waiting
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=review`)
    await expect(page.getByRole('link', { name: REVIEWED_NAME })).toHaveCount(0)

    // the student sees the comment on the submission and the grade among the grades
    const context = await browser.newContext({ locale: 'uz-UZ', viewport: { width: 1440, height: 900 } })
    const student = await context.newPage()
    await signIn(student, REVIEWED)
    await student.goto(`/student/courses/${DASTURLASH}/tasks/${TASK}`)
    await student.getByRole('tab', { name: uz.task.tabHistory }).click()
    await student.getByRole('listitem').getByRole('button').first().click()
    await expect(student.getByText(comment)).toBeVisible()
    await student.goto('/student/grades')
    await expect(
      student
        .getByText('JN_PRACTICAL')
        .or(student.getByText(/Amaliy/))
        .first(),
    ).toBeVisible()
    await expect(student.getByText('9', { exact: true }).first()).toBeVisible()
    await context.close()
  })

  test('the gradebook: keyboard between cells, a grade voided with a reason, the history kept', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the gradebook is edited on the desktop')
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=grades`)
    const first = page.locator('[data-cell="0:0"]')
    await first.focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.locator('[data-cell="0:1"]')).toBeFocused()

    const cell = page.getByRole('button', { name: new RegExp(`^${REVIEWED_NAME}, JN_PRACTICAL`) })
    await expect(cell).toHaveText('9')
    await cell.focus()
    await page.keyboard.press('Enter')
    const sheet = page.getByRole('dialog')
    await sheet.getByRole('button', { name: uz.gradebook.void }).click()
    await sheet.getByLabel(uz.gradebook.reason).fill('Notoʻgʻri talabaga qoʻyilgan')
    await sheet.getByRole('button', { name: uz.gradebook.confirmVoid }).click()
    await expect(sheet.getByText(uz.grades.status.VOIDED)).toBeVisible()
    await expect(sheet.getByText(/Notoʻgʻri talabaga qoʻyilgan/)).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(cell).toHaveText('—')

    // the grid goes to Excel
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: uz.teaching.exportExcel }).click()
    expect((await download).suggestedFilename()).toMatch(/\.xlsx$/)
  })

  test('the analytics speak in sentences and have their tables', async ({ page }) => {
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=analytics`)
    await expect(page.getByRole('heading', { name: uz.analytics.funnel })).toBeVisible()
    await expect(page.getByRole('heading', { name: uz.analytics.atRisk })).toBeVisible()
    const funnel = page.getByRole('figure', { name: uz.analytics.funnel })
    await funnel.getByRole('button', { name: uz.chart.showTable }).click()
    await expect(funnel.getByRole('columnheader', { name: uz.analytics.stage })).toBeVisible()
    await expectNoSidewaysScroll(page)
  })
})

test.describe('the teacher tools on a phone', () => {
  test('every tool fits the screen', async ({ page }, testInfo) => {
    test.skip(!isPhone(testInfo), 'the layout check is for the phone')
    test.setTimeout(120_000)
    await signInAsTeacher(page)
    for (const tab of ['content', 'practice', 'review', 'grades', 'analytics']) {
      await page.goto(`/teacher/courses/${DASTURLASH}?tab=${tab}`)
      await expect(page.getByRole('tabpanel')).toBeVisible()
      await page.waitForLoadState('networkidle')
      await expectNoSidewaysScroll(page)
    }
    for (const editor of [`/teacher/coding-tasks/${TASK}/edit`, `/teacher/lessons/${LESSON}/edit`]) {
      await page.goto(editor)
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await page.waitForLoadState('networkidle')
      await expectNoSidewaysScroll(page)
    }
  })
})
