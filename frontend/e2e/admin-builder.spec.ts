import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { restoreDemoTimetable } from './db'
import { demoPassword, isPhone, signIn, texts } from './support'

const { uz } = texts

const label = (template: string, values: Record<string, string | number>) =>
  Object.entries(values).reduce((text, [key, value]) => text.replace(`{{${key}}}`, String(value)), template)

async function choose(page: Page, field: string, option: string | RegExp): Promise<void> {
  await page.getByRole('combobox', { name: field }).click()
  await page.getByRole('option', { name: option }).first().click()
}

async function adminRequest(request: APIRequestContext): Promise<Record<string, string>> {
  const login = await request.post('/api/v1/auth/login', {
    data: { identifier: 'admin', password: demoPassword() },
  })
  const { accessToken } = (await login.json()) as { accessToken: string }
  return { Authorization: `Bearer ${accessToken}` }
}

/** Spec §9, step 1 — on a draft, never on the demo timetable; everything is put back afterwards. */
test.describe('the administrator builds a week (spec §9, step 1)', () => {
  test.beforeAll(() => {
    restoreDemoTimetable()
  })
  test.afterAll(() => {
    restoreDemoTimetable()
  })

  test('two groups get their classes, a teacher placed twice is refused, the draft is published', async ({
    page,
    request,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the builder is built for the desktop')
    test.setTimeout(240_000)

    // the records the week needs: two groups and a computer room (their screens have their own flow)
    const headers = await adminRequest(request)
    for (const code of ['E2E-TA', 'E2E-TB']) {
      expect(
        (
          await request.post('/api/v1/admin/groups', {
            headers,
            data: { code, courseYear: 1, plannedSize: 20 },
          })
        ).ok(),
      ).toBe(true)
    }
    expect(
      (
        await request.post('/api/v1/admin/rooms', {
          headers,
          data: { number: 'E2E-T1', building: 'E', capacity: 40, roomType: 'COMPUTER_LAB', active: true },
        })
      ).ok(),
    ).toBe(true)

    await signIn(page, 'admin')
    await page.goto('/admin/reference?tab=bells')
    // each shift has its own first pair
    for (const shift of ['MORNING', 'AFTERNOON', 'EVENING'] as const) {
      await expect(page.getByRole('heading', { name: uz.timetable.shift[shift] })).toBeVisible()
    }
    await expect(page.getByText(label(uz.admin.pairNo, { number: 1 }))).toHaveCount(3)

    // a new, empty draft for the term
    await page.goto('/admin/timetable')
    await page.getByRole('button', { name: uz.builder.newDraft }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(uz.admin.title).fill('E2E jadval')
    await dialog.getByRole('button', { name: uz.authoring.create }).click()
    await expect(page.getByText(uz.builder.status.DRAFT, { exact: true }).first()).toBeVisible()

    // group A: Dasturlash, Monday, pair 1
    await choose(page, uz.builder.which.group, 'E2E-TA')
    const addClass = async (day: string, pair: number) => {
      await page.getByRole('button', { name: label(uz.builder.addHere, { day, pair }) }).click()
      const sheet = page.getByRole('dialog')
      await choose(page, uz.admin.course, /DAS1110/)
      await choose(page, uz.admin.teacher, /Ayitbayev/)
      await choose(page, uz.admin.room, /E2E-T1/)
      return sheet
    }
    let sheet = await addClass(uz.builder.days['1'], 1)
    await expect(sheet.getByText(uz.builder.fits)).toBeVisible()
    await sheet.getByRole('button', { name: uz.authoring.save }).click()
    await expect(page.getByRole('button', { name: /^DAS1110, Dasturlash/ })).toHaveCount(1)

    // group B at the same time with the same teacher: the clash is named and saving is blocked
    await choose(page, uz.builder.which.group, 'E2E-TB')
    sheet = await addClass(uz.builder.days['1'], 1)
    await expect(sheet.getByRole('alert').filter({ hasText: uz.builder.conflict.TEACHER })).toBeVisible()
    await expect(sheet.getByRole('button', { name: uz.authoring.save })).toBeDisabled()
    // the next pair is free
    await choose(
      page,
      uz.builder.pair,
      new RegExp(`^${uz.timetable.shiftShort.MORNING} · ${label(uz.admin.pairNo, { number: 2 })}`),
    )
    await expect(sheet.getByText(uz.builder.fits)).toBeVisible()
    await sheet.getByRole('button', { name: uz.authoring.save }).click()
    await expect(page.getByRole('button', { name: /^DAS1110, Dasturlash/ })).toHaveCount(1)

    // group A's class moved by keyboard to pair 2: the teacher is busy there, the drop is refused
    await choose(page, uz.builder.which.group, 'E2E-TA')
    const card = page.getByRole('button', { name: /^DAS1110, Dasturlash/ })
    await card.focus()
    await expect(card).toBeFocused()
    await page.keyboard.press('Space')
    await expect(card).toHaveAttribute('aria-pressed', 'true')
    await page.keyboard.press('ArrowDown')
    // a person lets go when the cell has answered: the check runs while the class is in the air
    await page.waitForTimeout(700)
    await page.keyboard.press('Space')
    await expect(page.getByText(uz.builder.refused, { exact: true }).first()).toBeVisible()

    // to Tuesday it goes, and the move can be undone
    await card.focus()
    await page.keyboard.press('Space')
    await expect(card).toHaveAttribute('aria-pressed', 'true')
    await page.keyboard.press('ArrowRight')
    await page.waitForTimeout(700)
    await page.keyboard.press('Space')
    await expect(page.getByRole('button', { name: uz.builder.undoMove })).toBeVisible()
    await page.getByRole('button', { name: uz.builder.undoMove }).click()
    await expect(page.getByRole('button', { name: uz.builder.undoMove })).toHaveCount(0)

    // the load panel counts what is scheduled; the draft is published
    await expect(page.getByRole('heading', { name: uz.builder.load })).toBeVisible()
    await page.getByRole('button', { name: uz.authoring.publish, exact: true }).click()
    await page.getByRole('dialog').getByRole('button', { name: uz.authoring.publish }).click()
    await expect(page.getByText(uz.builder.status.PUBLISHED, { exact: true }).first()).toBeVisible()
    await expect(page.getByText(uz.builder.publishedReadOnly)).toBeVisible()
  })

  test('on a phone the week reads as a list of days', async ({ page }, testInfo) => {
    test.skip(!isPhone(testInfo), 'the list is for the phone')
    await signIn(page, 'admin')
    await page.goto('/admin/timetable')
    await expect(page.getByRole('region', { name: uz.builder.days['1'] })).toBeVisible()
    const width = page.viewportSize()?.width ?? 390
    await expect
      .poll(() => page.evaluate(() => document.documentElement.scrollWidth))
      .toBeLessThanOrEqual(width + 1)
  })
})
