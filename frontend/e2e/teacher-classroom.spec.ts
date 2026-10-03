import { expect, test, type Page, type TestInfo } from '@playwright/test'
import { clearClassSession } from './db'
import { isPhone, signIn, texts } from './support'

const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** The Friday practical of Dasturlash, taught by n.ayitbayev; its class of 2 October is used here. */
const FRIDAY_PRACTICAL = '73429519-f2db-4531-a5b3-78afca89cac4'
const CLASS_DATE = '2026-10-02'

async function signInAsTeacher(page: Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem('aifu-active-role', 'TEACHER')
  })
  await signIn(page, 'n.ayitbayev')
}

async function showDay(page: Page, testInfo: TestInfo, dayOfMonth: number): Promise<void> {
  if (!isPhone(testInfo)) return
  await page
    .getByRole('group', { name: uz.timetable.days })
    .getByRole('button', { name: new RegExp(`${dayOfMonth}$`) })
    .click()
}

test.describe('the teacher in the classroom (spec §9, step 2)', () => {
  test.beforeEach(() => {
    clearClassSession(FRIDAY_PRACTICAL, CLASS_DATE)
  })

  test('the week, the class, attendance, the topic, and the lesson on the projector', async ({
    page,
  }, testInfo) => {
    test.setTimeout(120_000)
    await signInAsTeacher(page)

    // the week: hours and odd / even, the holiday of 1 October
    await page.goto(`/teacher/timetable?date=${CLASS_DATE}`)
    await expect(
      page.getByText(uz.teaching.weekLoad.replace('{{pairs}}', '3').replace('{{hours}}', '6')),
    ).toBeVisible()
    await expect(page.getByText(uz.timetable.parity.ODD)).toBeVisible()

    // the class opens from its card
    await showDay(page, testInfo, 2)
    await page
      .getByRole('button', { name: /Dasturlash/ })
      .first()
      .click()
    await expect(page).toHaveURL(new RegExp(`/teacher/classes/${FRIDAY_PRACTICAL}/${CLASS_DATE}`))
    await expect(page.getByRole('heading', { level: 1, name: /Dasturlash/ })).toBeVisible()
    await expect(page.getByText(uz.teaching.attendanceNotTaken)).toBeVisible()

    // everyone present in one tap, then one student absent
    await page.getByRole('button', { name: uz.teaching.allPresent }).click()
    await expect(page.getByText(/Keldi: 13, kechikdi: 0, kelmadi: 0/)).toBeVisible()
    await page.getByRole('button', { name: `${uz.attendance.status.ABSENT}: Yusupov Bekzod` }).click()
    await expect(page.getByText(/Keldi: 12, kechikdi: 0, kelmadi: 1/)).toBeVisible()
    await expect(page.getByText(uz.quiz.saving)).toHaveCount(0)

    // the suggested lesson is linked; a note is kept
    await page.getByRole('button', { name: uz.teaching.linkLesson }).click()
    await expect(page.getByRole('link', { name: uz.teaching.present })).toBeVisible()
    await page.getByLabel(uz.teaching.notes).fill('Massivlar mavzusiga oʻtishdan oldin takrorlash.')
    await expect(page.getByText(uz.quiz.saved, { exact: true })).toBeVisible({ timeout: 10_000 })

    // all of it is on the server
    await page.reload()
    await expect(page.getByText(/Keldi: 12, kechikdi: 0, kelmadi: 1/)).toBeVisible()
    await expect(
      page.getByRole('button', { name: `${uz.attendance.status.ABSENT}: Yusupov Bekzod` }),
    ).toHaveAttribute('aria-pressed', 'true')
    await expect(page.getByLabel(uz.teaching.notes)).toHaveValue(
      'Massivlar mavzusiga oʻtishdan oldin takrorlash.',
    )

    // the lesson on the projector: one section at a time, the arrows move, Escape leaves
    await page.getByRole('link', { name: uz.teaching.present }).click()
    await expect(page).toHaveURL(/\/present/)
    await expect(page.getByText(/^1 \/ \d+$/)).toBeVisible()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByText(/^2 \/ \d+$/)).toBeVisible()
    await page.getByRole('button', { name: uz.common.next }).click()
    await expect(page.getByText(/^3 \/ \d+$/)).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page).toHaveURL(new RegExp(`/teacher/classes/${FRIDAY_PRACTICAL}/${CLASS_DATE}`))

    // the journal now has the class, the thematic plan its topic
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=journal`)
    await expect(
      page
        .getByRole('row', { name: /Yusupov Bekzod/ })
        .getByText('Y', { exact: true })
        .last(),
    ).toBeVisible()
  })

  test('the journal downloads as an Excel file', async ({ page }) => {
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=journal`)
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: uz.teaching.exportExcel }).click()
    const file = await download
    expect(file.suggestedFilename()).toMatch(/\.xlsx$/)
    // kept for a look with a spreadsheet reader: test-results/journal.xlsx
    await file.saveAs('test-results/journal.xlsx')
  })

  test('an announcement is posted to the course and taken back', async ({ page }) => {
    await signInAsTeacher(page)
    await page.goto(`/teacher/courses/${DASTURLASH}?tab=announcements`)
    const title = `Sinov eʼloni ${Date.now()}`
    await page.getByLabel(uz.teaching.announcementTitle).fill(title)
    await page.getByLabel(uz.teaching.announcementBody).fill('Ertangi dars B-301 da.')
    await page.getByRole('button', { name: uz.teaching.post }).click()
    const article = page.getByRole('article').filter({ hasText: title })
    await expect(article).toBeVisible()
    await article.getByRole('button', { name: uz.teaching.delete }).click()
    await page.getByRole('dialog').getByRole('button', { name: uz.teaching.delete }).click()
    await expect(article).toHaveCount(0)
  })
})
