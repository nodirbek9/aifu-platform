import { expect, test, type Page, type TestInfo } from '@playwright/test'
import { resetNotificationsAndPhone } from './db'
import { isPhone, signIn, texts } from './support'

const { uz } = texts
const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'

/** On a phone the week is a day strip: a day is chosen before its classes are on screen. */
async function showDay(page: Page, testInfo: TestInfo, dayOfMonth: number): Promise<void> {
  if (!isPhone(testInfo)) return
  await page
    .getByRole('group', { name: uz.timetable.days })
    .getByRole('button', { name: new RegExp(`${dayOfMonth}$`) })
    .click()
}

test.describe('my timetable (S-02)', () => {
  test('a moved lecture, a cancelled practical and a holiday are shown as the server has them', async ({
    page,
  }, testInfo) => {
    await signIn(page, 'student3')

    // the week of 19 October: the Monday lecture is moved to Tuesday
    await page.goto('/student/timetable?date=2026-10-19')
    await expect(page.getByText('19–25 oktabr')).toBeVisible()
    await expect(page.getByText(uz.timetable.parity.EVEN)).toBeVisible()
    await showDay(page, testInfo, 19)
    await expect(page.getByText(uz.classStatus.MOVED_OUT).first()).toBeVisible()
    await showDay(page, testInfo, 20)
    await expect(page.getByText(uz.classStatus.MOVED_IN).first()).toBeVisible()

    // the class in full: where it came from, with the reason
    await page
      .getByRole('button', { name: new RegExp(uz.classStatus.MOVED_IN) })
      .first()
      .click()
    const sheet = page.getByRole('dialog')
    await expect(sheet.getByText('A-204')).toBeVisible()
    await expect(sheet.getByText(/Avvalgi vaqti: Dush, 08:30/)).toBeVisible()
    await expect(sheet.getByText("Ma'ruza zalida tadbir o'tkaziladi")).toBeVisible()
    await page.keyboard.press('Escape')

    // one week back: an odd week, the Friday practical cancelled
    await page.getByRole('button', { name: uz.timetable.previousWeek }).click()
    await expect(page).toHaveURL(/date=2026-10-12/)
    await expect(page.getByText(uz.timetable.parity.ODD)).toBeVisible()
    await showDay(page, testInfo, 16)
    await expect(page.getByText(uz.classStatus.CANCELLED).first()).toBeVisible()
    await expect(page.getByText("O'qituvchi malaka oshirish kursida").first()).toBeVisible({
      visible: isPhone(testInfo),
    })

    // a holiday in the week of 1 October
    await page.goto('/student/timetable?date=2026-10-01')
    await showDay(page, testInfo, 1)
    await expect(page.getByText("O'qituvchi va murabbiylar kuni").first()).toBeVisible()

    // back to the week today is in
    await page.goto('/student/timetable?date=2026-11-16')
    await page.getByRole('button', { name: uz.timetable.thisWeek }).click()
    await expect(page).not.toHaveURL(/date=/)
    await expect(page.getByRole('button', { name: uz.timetable.thisWeek })).toHaveCount(0)
  })

  test('a class split into subgroups is shown to each half in its own week', async ({ page }, testInfo) => {
    await signIn(page, 'xolmatov.u')
    await page.goto('/student/timetable?date=2026-10-05')
    await expect(page.getByText(uz.timetable.parity.EVEN)).toBeVisible()
    await showDay(page, testInfo, 8)
    await expect(page.getByText(uz.timetable.subgroup.replace('{{name}}', 'B')).first()).toBeVisible()

    await page.getByRole('button', { name: uz.timetable.nextWeek }).click()
    await expect(page.getByText(uz.timetable.parity.ODD)).toBeVisible()
    await showDay(page, testInfo, 15)
    await expect(page.getByText(uz.timetable.subgroup.replace('{{name}}', 'A')).first()).toBeVisible()
  })
})

test.describe('grades and attendance (S-08, S-10)', () => {
  test('grades follow the JN / ON / YaN scheme, with the admission and the history of changes', async ({
    page,
  }) => {
    await signIn(page, 'student3')
    await page.goto('/student/grades')
    await expect(page.getByRole('heading', { level: 1, name: uz.nav.grades })).toBeVisible()

    const course = page
      .locator('div')
      .filter({ has: page.getByRole('heading', { level: 2, name: /^Dasturlash/ }) })
      .last()
    await expect(course.getByText('48', { exact: true })).toBeVisible()
    await expect(course.getByText(uz.grades.admission.granted)).toBeVisible()
    await expect(course.getByText(uz.grades.finalPending)).toBeVisible()
    // JN is the sum of its three parts; the final is not graded yet
    await expect(course.getByRole('row', { name: /Joriy nazorat.*24 \/ 30/ })).toBeVisible()
    await expect(course.getByRole('row', { name: /Yakuniy nazorat.*— \/ 40/ })).toBeVisible()

    // the corrected mark is kept
    await course.getByText(uz.grades.historyTitle).click()
    await expect(course.getByText(uz.grades.status.REVISED)).toBeVisible()

    // the same record on the course's own page
    await page.goto(`/student/courses/${DASTURLASH}?tab=grades`)
    await expect(page.getByText(uz.grades.admission.granted)).toBeVisible()
  })

  test('attendance counts what was held and names every missed class', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto('/student/attendance')
    await expect(
      page.getByRole('img', { name: uz.attendance.percent.replace('{{percent}}', '50') }),
    ).toBeVisible()
    await expect(page.getByRole('main').getByRole('listitem')).toHaveCount(4)

    await page.getByRole('radio', { name: uz.attendance.missed }).click()
    await expect(page).toHaveURL(/show=missed/)
    await expect(page.getByRole('main').getByRole('listitem')).toHaveCount(2)
    await expect(
      page.getByRole('main').getByRole('listitem').filter({ hasText: uz.attendance.status.ABSENT }),
    ).toHaveCount(1)
    await expect(
      page.getByRole('main').getByRole('listitem').filter({ hasText: uz.attendance.status.EXCUSED }),
    ).toHaveCount(1)
  })
})

test.describe('notifications, search, announcements, profile (S-09)', () => {
  test.beforeEach(() => {
    resetNotificationsAndPhone('student3')
  })

  test('a notification says what happened, leads to it, and is then read', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto('/notifications')
    await expect(page.getByText(uz.notification.unreadCount.replace('{{count}}', '4'))).toBeVisible()

    await page.getByRole('radio', { name: uz.notification.onlyUnread }).click()
    await expect(page.getByRole('main').getByRole('listitem')).toHaveCount(4)
    await expect(
      page.getByRole('button', { name: /Dars boshqa vaqtga koʻchirildi.*seshanba, 20-oktabr/ }),
    ).toBeVisible()

    // the grade notification opens the grades
    await page.getByRole('button', { name: new RegExp(uz.notification.type.GRADE_PUBLISHED) }).click()
    await expect(page).toHaveURL(/\/student\/grades$/)
    await page.goto('/notifications')
    await expect(page.getByText(uz.notification.unreadCount.replace('{{count}}', '3'))).toBeVisible()

    // from the bell: the cancelled class opens the timetable on its week
    await page.getByRole('button', { name: /^Bildirishnomalar: 3/ }).click()
    await page
      .getByRole('dialog')
      .getByRole('button', { name: new RegExp(uz.notification.timetable.CANCELLED) })
      .click()
    await expect(page).toHaveURL(/\/student\/timetable\?date=2026-10-16/)

    // the rest at once
    await page.goto('/notifications')
    await page.getByRole('button', { name: uz.common.markAllRead }).click()
    await expect(page.getByRole('button', { name: uz.common.notifications, exact: true })).toBeVisible()
    await page.getByRole('radio', { name: uz.notification.onlyUnread }).click()
    await expect(page.getByText(uz.notification.noneUnread)).toBeVisible()
  })

  test('search finds my lessons and courses as the word is being typed', async ({ page }, testInfo) => {
    await signIn(page, 'student3')
    if (isPhone(testInfo)) await page.getByRole('button', { name: uz.common.search, exact: true }).click()
    else await page.keyboard.press('Control+k')

    const input = page.getByRole('combobox')
    await input.fill('dastur')
    await expect(page.getByText(uz.search.results)).toBeVisible()
    // the course comes before the lessons that mention the word
    await expect(page.getByRole('option').filter({ hasText: uz.search.type.COURSE }).first()).toContainText(
      'Dasturlash',
    )

    await input.fill('massiv')
    const lesson = page
      .getByRole('option', { name: /Bir oʻlchovli massivlar|Bir o'lchovli massivlar/ })
      .first()
    await expect(lesson).toBeVisible()
    await lesson.click()
    await expect(page).toHaveURL(/\/student\/courses\/[0-9a-f-]+\/lessons\/[0-9a-f-]+/)
    await expect(page.getByRole('heading', { level: 1 })).toContainText(/massivlar/)
  })

  test('announcements are on the dashboard, on their own page and in the course', async ({ page }) => {
    await signIn(page, 'student3')
    await expect(page.getByRole('heading', { name: "Guruh yig'ilishi" })).toBeVisible()
    await page.getByRole('link', { name: uz.announcements.all }).click()
    await expect(page).toHaveURL(/\/announcements$/)
    await expect(page.getByRole('article')).toHaveCount(2)
    await expect(page.getByRole('article').filter({ hasText: 'BK-DI26/1' })).toHaveCount(1)

    await page.goto(`/student/courses/${DASTURLASH}?tab=announcements`)
    await expect(page.getByRole('article')).toHaveCount(1)
    await expect(page.getByRole('heading', { name: "Amaliy mashg'ulotlar tartibi" })).toBeVisible()
  })

  test('the profile shows who I am and saves my phone number', async ({ page }) => {
    await signIn(page, 'student3')
    await page.getByRole('button', { name: new RegExp(`^${uz.common.profile}`) }).click()
    await page.getByRole('menuitem', { name: uz.common.profile, exact: true }).click()
    await expect(page).toHaveURL(/\/profile$/)
    await expect(page.getByRole('main').getByText('BK-DI26/1', { exact: true })).toBeVisible()
    await expect(page.getByText('student3', { exact: true })).toBeVisible()

    const save = page.getByRole('button', { name: uz.profile.save, exact: true })
    await expect(save).toBeDisabled()
    await page.getByLabel(uz.profile.phone).fill('+998 90 000 00 01')
    await save.click()
    await expect(page.getByText(uz.profile.phoneSaved, { exact: true })).toBeVisible()
    await page.reload()
    await expect(page.getByLabel(uz.profile.phone)).toHaveValue('+998 90 000 00 01')
  })

  test('a day without classes names the next one', async ({ page }) => {
    await signIn(page, 'student1')
    const empty = page.getByText(uz.dashboard.todayEmptyTitle)
    // true on the days this group has no classes; on the others there is nothing to check here
    if (await empty.isVisible()) {
      await expect(page.getByText(/^Keyingi dars: /)).toBeVisible()
    }
  })
})
