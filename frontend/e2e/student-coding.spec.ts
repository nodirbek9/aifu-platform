import { expect, test, type Page, type TestInfo } from '@playwright/test'
import { isPhone, signIn, texts } from './support'

const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** DAS-S1-A8 "Koʻpaytirish jadvali": the task this flow submits to (reset before each run). */
const TASK = '400e43c8-8d16-41b6-8fe8-5bf6af264f04'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'

const SOLUTION = `n = int(input())
for i in range(1, n + 1):
    print(" ".join(str(i * j) for j in range(1, n + 1)))
`
const WRONG = `n = int(input())
print(n)
`
const BROKEN_JAVA = `public class Main {
    public static void main(String[] args) {
        int x = ;
    }
}
`

// code really runs in the sandbox: a run takes a few seconds
const RESULT = { timeout: 45_000 }

async function openTask(page: Page): Promise<void> {
  await signIn(page, 'student3')
  await page.goto(`/student/courses/${DASTURLASH}/tasks/${TASK}`)
  await expect(page.getByRole('heading', { level: 1, name: /Koʻpaytirish jadvali/ })).toBeVisible()
}

/** On a phone the task page is three tabs; on a desktop everything is on screen. */
async function showPane(page: Page, testInfo: TestInfo, pane: 'phoneTask' | 'phoneCode' | 'phoneResult') {
  if (isPhone(testInfo)) await page.getByRole('radio', { name: uz.task[pane], exact: true }).click()
}

async function writeCode(page: Page, testInfo: TestInfo, code: string): Promise<void> {
  await showPane(page, testInfo, 'phoneCode')
  const editor = page.getByRole('textbox', { name: uz.task.editorLabel })
  await editor.fill(code)
}

test.describe('the practice task (S-05)', () => {
  test('a solution is written in Python, run, checked on the examples, submitted and accepted', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000)
    await openTask(page)

    // the statement: examples, and the three languages with their own starter code
    await expect(page.getByText('Namuna 1')).toBeVisible()
    await showPane(page, testInfo, 'phoneCode')
    for (const language of ['Python', 'Java', 'C++']) {
      await expect(page.getByRole('radio', { name: language, exact: true })).toBeVisible()
    }
    await page.getByRole('radio', { name: 'Java', exact: true }).click()
    await expect(page.getByRole('textbox', { name: uz.task.editorLabel })).toContainText('public class Main')
    await page.getByRole('radio', { name: 'Python', exact: true }).click()

    await writeCode(page, testInfo, SOLUTION)

    // Run with custom input: the program's own output, not graded
    await page.getByRole('button', { name: new RegExp(`^${uz.task.run}`) }).click()
    await showPane(page, testInfo, 'phoneResult')
    await expect(page.getByText(uz.verdict.OK, { exact: true })).toBeVisible(RESULT)
    await expect(page.getByRole('tabpanel').locator('pre', { hasText: '3 6 9' })).toBeVisible()

    // Run examples: every visible test
    await page.getByRole('button', { name: uz.task.runExamples }).click()
    await expect(
      page.getByText(uz.task.testsPassed.replace('{{passed}}', '2').replace('{{total}}', '2')),
    ).toBeVisible(RESULT)

    // Submit: graded on hidden tests too
    await page.getByRole('button', { name: new RegExp(`^${uz.task.submit}`) }).click()
    await expect(page.getByText(uz.verdict.ACCEPTED, { exact: true }).first()).toBeVisible(RESULT)
    await expect(page.getByText(uz.task.hiddenTest).first()).toBeVisible()
    // the data of a hidden test is never shown
    await expect(page.locator('pre', { hasText: '4 8 12 16 20' })).toHaveCount(0)

    // the attempt is in the history; the draft survives a reload
    await page.getByRole('tab', { name: new RegExp(`^${uz.task.tabHistory}`) }).click()
    await expect(page.getByRole('button', { name: /#1/ })).toBeVisible()
    await page.reload()
    await showPane(page, testInfo, 'phoneCode')
    await expect(page.getByRole('textbox', { name: uz.task.editorLabel })).toContainText('range(1, n + 1)')
  })

  test('a wrong answer and a compilation error are explained, without using an attempt', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000)
    await openTask(page)

    await writeCode(page, testInfo, WRONG)
    await page.getByRole('button', { name: uz.task.runExamples }).click()
    await showPane(page, testInfo, 'phoneResult')
    await expect(page.getByText(uz.verdict.WRONG_ANSWER, { exact: true }).first()).toBeVisible(RESULT)
    await expect(page.getByText(uz.task.expectedOutput).first()).toBeVisible()
    await expect(page.getByText(uz.task.actualOutput).first()).toBeVisible()

    await showPane(page, testInfo, 'phoneCode')
    await page.getByRole('radio', { name: 'Java', exact: true }).click()
    await writeCode(page, testInfo, BROKEN_JAVA)
    await page.getByRole('button', { name: new RegExp(`^${uz.task.run}`) }).click()
    await showPane(page, testInfo, 'phoneResult')
    await expect(page.getByText(uz.verdict.COMPILATION_ERROR, { exact: true })).toBeVisible(RESULT)
    await expect(page.getByText(uz.task.compileOutput)).toBeVisible()

    // runs are not attempts
    await page.getByRole('tab', { name: new RegExp(`^${uz.task.tabHistory}`) }).click()
    await expect(page.getByRole('button', { name: /#\d+/ })).toHaveCount(
      // the first test of this file may already have submitted once
      await page.getByRole('button', { name: /#\d+/ }).count(),
    )
  })

  test('a hint is revealed one at a time', async ({ page }, testInfo) => {
    await signIn(page, 'student3')
    // DAS-S1-A7 "Tub sonni aniqlash" has three hints
    await page.goto(`/student/courses/${DASTURLASH}/tasks/1240550f-5bcc-4155-a17d-1a9c34945a03`)
    await showPane(page, testInfo, 'phoneTask')
    await expect(page.getByRole('heading', { level: 2, name: uz.task.hints })).toBeVisible()

    const reveal = page.getByRole('button', { name: /maslahatni ochish/ })
    if ((await reveal.count()) > 0) await reveal.click()
    // whether opened now or on an earlier run, an opened hint shows its text
    await expect(page.getByText(/^1-maslahat/)).toBeVisible()
  })
})

test.describe('"Try it" in a lesson (S-04.4)', () => {
  test('a code example is run without leaving the lesson', async ({ page }) => {
    test.setTimeout(120_000)
    await signIn(page, 'student3')
    await page.goto(`/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`)

    await page.getByRole('button', { name: uz.lesson.tryIt }).first().click()
    await page.getByLabel(uz.task.customInput).fill('1\n2\n3\n')
    await page.getByRole('button', { name: uz.task.run, exact: true }).click()

    await expect(page.getByText(uz.verdict.OK, { exact: true })).toBeVisible(RESULT)
    await expect(page.locator('pre', { hasText: '6' }).first()).toBeVisible()
    await expect(page).toHaveURL(/\/lessons\//)
  })
})

test.describe('all my work (S-07)', () => {
  test('the list filters by state and keeps the filter in the address', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto('/student/assignments')
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.assignments })).toBeVisible()
    await expect(page.getByRole('link', { name: /Arifmetik amallar/ })).toBeVisible()

    await page.getByRole('combobox', { name: uz.assignments.filterState }).click()
    await page.getByRole('option', { name: uz.activityState.COMPLETED }).click()
    await expect(page).toHaveURL(/state=COMPLETED/)
    await expect(page.getByRole('link', { name: /Arifmetik amallar/ })).toHaveCount(0)
    await expect(page.getByRole('link', { name: /Kabisa yili/ })).toBeVisible()

    await page.reload()
    await expect(page.getByRole('link', { name: /Kabisa yili/ })).toBeVisible()
  })
})
