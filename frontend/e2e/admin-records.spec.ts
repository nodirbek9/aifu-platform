import { expect, test, type Page } from '@playwright/test'
import { clearAdminLeftovers } from './db'
import { expectFitsScreen, isPhone, signIn, texts } from './support'

const { uz } = texts

const label = (template: string, values: Record<string, string | number>) =>
  Object.entries(values).reduce((text, [key, value]) => text.replace(`{{${key}}}`, String(value)), template)

/** Picks an option of one of the app's selects by its visible label. */
async function choose(page: Page, field: string, option: string | RegExp): Promise<void> {
  await page.getByRole('combobox', { name: field }).click()
  await page.getByRole('option', { name: option }).first().click()
}

test.describe('the administrator keeps the records (spec A-03, A-05)', () => {
  test.beforeAll(() => {
    clearAdminLeftovers()
  })
  // the imported students join a real group: other flows count its members
  test.afterAll(() => {
    clearAdminLeftovers()
  })

  test('a group, a room and a holiday are created', async ({ page }, testInfo) => {
    test.skip(isPhone(testInfo), 'the records are entered on the desktop')
    await signIn(page, 'admin')

    // a group
    await page.goto('/admin/reference')
    await page.getByRole('button', { name: uz.admin.newGroup }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(uz.admin.groupCode).fill('E2E-DI26/9')
    await dialog.getByLabel(uz.admin.plannedSize).fill('25')
    await dialog.getByRole('button', { name: uz.authoring.save }).click()
    await expect(dialog).toHaveCount(0)
    await page.getByRole('searchbox', { name: uz.common.search }).fill('E2E')
    await expect(page.getByRole('button', { name: 'E2E-DI26/9' })).toBeVisible()
    await expect(page).toHaveURL(/q=E2E/)

    // a room
    await page.getByRole('tab', { name: uz.admin.reference.rooms }).click()
    await page.getByRole('button', { name: uz.admin.newRoom }).click()
    await dialog.getByLabel(uz.admin.building).fill('E2E')
    await dialog.getByLabel(uz.admin.roomNumber).fill('E2E-101')
    await dialog.getByLabel(uz.admin.capacity).fill('30')
    await choose(page, uz.admin.roomType, uz.admin.roomTypes.COMPUTER_LAB)
    await dialog.getByRole('button', { name: uz.authoring.save }).click()
    await expect(page.getByRole('row', { name: /E2E-101/ })).toContainText(uz.admin.roomTypes.COMPUTER_LAB)

    // a holiday
    await page.getByRole('tab', { name: uz.admin.reference.holidays }).click()
    await page.getByLabel(uz.admin.date).fill('2027-01-14')
    await page.getByLabel(uz.admin.holidayName).fill('E2E Vatan himoyachilari kuni')
    await page.getByRole('button', { name: uz.authoring.add }).click()
    await expect(page.getByText('E2E Vatan himoyachilari kuni')).toBeVisible()
  })

  test('a registrar CSV enrols by student number, and says which lines it could not', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the import is done on the desktop')
    await signIn(page, 'admin')
    await page.goto('/admin/structure/courses/2eec7704-6e25-4adb-966e-b54de91a9b69')
    // both demo students are already enrolled and the third number belongs to nobody: nothing changes
    const csv = ['student_number', 'DEV-S-003', 'DEV-S-004', 'NOBODY-0000'].join('\n')
    await page.locator('input[type=file][accept*=csv]').setInputFiles({
      name: 'registrator.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csv, 'utf8'),
    })
    await expect(
      page.getByRole('status').filter({ hasText: label(uz.admin.enrollOutcome, { done: 0, failed: 3 }) }),
    ).toBeVisible()
    const failures = page.getByRole('list', { name: uz.admin.csvFailures })
    await expect(failures.getByRole('listitem')).toHaveCount(3)
    await expect(failures).toContainText('NOBODY-0000')
  })

  test('three students are imported from a file, checked first without saving', async ({
    page,
  }, testInfo) => {
    test.skip(isPhone(testInfo), 'the import is done on the desktop')
    await signIn(page, 'admin')
    await page.goto('/admin/users/import')
    const csv = [
      'F.I.Sh;Talaba raqami;Guruh;Login;Email',
      'Eshonqulov Bobur;E2E-0001;BK-DI26/1;e2e.bobur;e2e.bobur@example.uz',
      'Yoʻldosheva Nilufar;E2E-0002;BK-DI26/1;e2e.nilufar;e2e.nilufar@example.uz',
      'Qurbonov Sherzod;E2E-0003;BK-DI26/1;e2e.sherzod;e2e.sherzod@example.uz',
    ].join('\r\n')
    await page.locator('input[type=file]').setInputFiles({
      name: 'talabalar.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csv, 'utf8'),
    })
    await expect(page.getByText(label(uz.admin.importRowsRead, { count: 3 }))).toBeVisible()

    // the dry run says what would happen, row by row, and saves nothing
    await page.getByLabel(uz.admin.importPassword).fill('Start!2026e2e')
    await page.getByRole('button', { name: uz.admin.dryRun }).click()
    await expect(page.getByRole('heading', { name: uz.admin.dryRunResult })).toBeVisible()
    await expect(page.getByRole('row').filter({ hasText: 'E2E-0002' })).toContainText(
      uz.admin.outcome.CREATED,
    )
    await page.goto('/admin/users?q=e2e.')
    await expect(page.getByText(uz.admin.noUsers)).toBeVisible()

    // the real import
    await page.goto('/admin/users/import')
    await page.locator('input[type=file]').setInputFiles({
      name: 'talabalar.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csv, 'utf8'),
    })
    await page.getByLabel(uz.admin.importPassword).fill('Start!2026e2e')
    await page.getByRole('button', { name: uz.admin.dryRun }).click()
    await expect(page.getByRole('heading', { name: uz.admin.dryRunResult })).toBeVisible()
    await page.getByRole('button', { name: uz.admin.importNow }).click()
    await expect(page.getByRole('heading', { name: uz.admin.importResult })).toBeVisible()
    await page.goto('/admin/users?q=e2e.')
    await expect(page.getByText(label(uz.admin.userCount, { count: 3 }))).toBeVisible()
    await page.getByRole('link', { name: 'Yoʻldosheva Nilufar' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Yoʻldosheva Nilufar' })).toBeVisible()
    await expect(page.getByText('BK-DI26/1')).toBeVisible()
  })

  test('every admin screen fits a phone', async ({ page }, testInfo) => {
    test.skip(!isPhone(testInfo), 'the sweep is for the phone')
    test.setTimeout(180_000)
    await signIn(page, 'admin')
    for (const path of [
      '/admin',
      '/admin/users',
      '/admin/users/import',
      '/admin/structure',
      '/admin/structure?tab=courses',
      '/admin/reference',
      '/admin/reference?tab=rooms',
      '/admin/reference?tab=bells',
      '/admin/reference?tab=holidays',
      '/admin/reference?tab=terms',
      '/admin/load',
      '/admin/audit',
      '/admin/audit?tab=roles',
    ]) {
      await page.goto(path)
      await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible()
      await page.waitForLoadState('networkidle')
      await expectFitsScreen(page)
    }
  })
})
