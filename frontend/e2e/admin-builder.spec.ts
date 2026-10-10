import { expect, test, type APIRequestContext, type Page } from '@playwright/test'
import { restoreDemoTimetable } from './db'
import { demoPassword, isPhone, signIn, texts } from './support'
import createClient from 'openapi-fetch'
import type { components, paths } from '../src/shared/api/schema'
import { expectFitsScreen, shot } from './support'

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
  expect(login.ok(), 'The running backend must accept the demo administrator login').toBe(true)
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

// Independent of the desktop-only drag-and-drop flow: ADD works on both projects.
test('week preview adds one dated class and refreshes after removal (7.5-F3)', async ({
  page,
  request,
  baseURL,
}, testInfo) => {
  test.setTimeout(180_000)
  const headers = await adminRequest(request)
  const api = createClient<paths>({ baseUrl: baseURL, headers })
  type Schemas = components['schemas']
  async function result<T>(call: Promise<{ data?: T; response: Response }>): Promise<T> {
    const response = await call
    if (!response.response.ok || response.data === undefined) {
      throw new Error(
        `Fixture API failed: ${response.response.status} ${new URL(response.response.url).pathname}`,
      )
    }
    return response.data
  }
  const timetables = await result(api.GET('/api/v1/admin/timetables'))
  const baseline = timetables.find((item) => item.status === 'PUBLISHED')
  if (!baseline?.id || !baseline.termId || !baseline.termStartsOn)
    throw new Error('A published demo timetable is required')
  const templates = await result(
    api.GET('/api/v1/admin/timetables/{id}/entries', {
      params: { path: { id: baseline.id }, query: { locale: 'uz' } },
    }),
  )
  const template = templates.find((item) => item.courseId && item.teacherUserId)
  if (!template?.courseId || !template.teacherUserId) throw new Error('A real source class is required')
  const slots = await result(api.GET('/api/v1/timetable/pair-slots'))
  const pairs = slots
    .filter((slot) => slot.shift === 'MORNING')
    .sort((a, b) => (a.pairNo ?? 0) - (b.pairNo ?? 0))
  const first = pairs[0]
  const second = pairs[1]
  if (!first?.id || !second?.id) throw new Error('Two morning pairs are required')
  const marker = `E2E-TF3-${testInfo.project.name}-${Date.now()}`
  let timetableId: string | undefined
  let groupId: string | undefined
  let roomId: string | undefined
  const shiftDate = (date: string, days: number) => {
    const value = new Date(`${date}T12:00:00Z`)
    value.setUTCDate(value.getUTCDate() + days)
    return value.toISOString().slice(0, 10)
  }
  try {
    const group = await result(
      api.POST('/api/v1/admin/groups', {
        body: {
          code: marker,
          courseYear: 1,
          plannedSize: 10,
          shift: 'MORNING',
        },
      }),
    )
    groupId = group.id
    const room = await result(
      api.POST('/api/v1/admin/rooms', {
        body: {
          number: marker,
          capacity: 30,
          roomType: 'COMPUTER_LAB',
          active: true,
        },
      }),
    )
    roomId = room.id
    const timetable = await result(
      api.POST('/api/v1/admin/timetables', {
        body: {
          name: marker,
          termId: baseline.termId,
        },
      }),
    )
    timetableId = timetable.id
    if (!timetableId || !groupId || !roomId) throw new Error('Missing fixture identifiers')
    const body: Schemas['EntryRequest'] = {
      courseId: template.courseId,
      teacherUserId: template.teacherUserId,
      groupIds: [groupId],
      roomId,
      sessionType: template.sessionType ?? 'LECTURE',
      dayOfWeek: 1,
      weekParity: 'EVERY',
      pairSlotId: first.id,
    }
    const source = await result(
      api.POST('/api/v1/admin/timetables/{id}/entries', {
        params: { path: { id: timetableId }, query: { locale: 'uz' } },
        body,
      }),
    )
    const other = await result(
      api.POST('/api/v1/admin/timetables/{id}/entries', {
        params: { path: { id: timetableId }, query: { locale: 'uz' } },
        body: { ...body, pairSlotId: second.id },
      }),
    )
    if (!source.id || !other.id) throw new Error('Missing source identifiers')
    const fixtureTimetableId = timetableId
    const weekAt = (date: string) =>
      result(
        api.GET('/api/v1/admin/timetables/{id}/week', {
          params: { path: { id: fixtureTimetableId }, query: { groupId, date, locale: 'uz' } },
        }),
      )
    const load = () =>
      result(
        api.GET('/api/v1/admin/timetables/{id}/load', {
          params: { path: { id: fixtureTimetableId }, query: { groupId, locale: 'uz' } },
        }),
      )
    // Pick actual term dates using the server's holiday calendar, rather than assuming a free date.
    let week = await weekAt(baseline.termStartsOn)
    let monday: string | undefined
    let addedDate: string | undefined
    for (let attempt = 0; attempt < 8; attempt++) {
      monday = week.days?.find((day) => day.classes?.some((item) => item.entryId === source.id))?.date
      addedDate = week.days?.find(
        (day) => day.date && !day.holiday && new Date(`${day.date}T12:00:00Z`).getUTCDay() === 2,
      )?.date
      if (monday && addedDate) break
      week = await weekAt(shiftDate(week.weekStart ?? baseline.termStartsOn, 7))
    }
    if (!monday || !addedDate) throw new Error('No suitable non-holiday week in the term')
    const originalLoad = await load()
    const hours = (rows: Schemas['LoadRow'][]) =>
      rows.reduce((total, row) => total + (row.scheduledHours ?? 0), 0)
    await signIn(page, 'admin')
    await page.emulateMedia({ reducedMotion: 'reduce' })
    const address = `/admin/timetable?tt=${timetableId}&view=group&id=${groupId}`
    await page.goto(address)
    const openPreview = async () => {
      await page.getByRole('button', { name: uz.builder.weekPreview, exact: true }).click()
      const dialog = page.getByRole('dialog')
      await dialog.getByLabel(uz.builder.weekOf).fill(addedDate ?? '')
      const action = dialog.getByRole('button', { name: uz.builder.addSingleClass })
      await action.focus()
      await page.keyboard.press('Enter')
      return dialog
    }
    let dialog = await openPreview()
    await choose(page, uz.builder.sourceClass, new RegExp((source.startsAt ?? '').slice(0, 5)))
    await choose(page, uz.builder.pair, new RegExp((first.startsAt ?? '').slice(0, 5)))
    const reason = `${marker} additional class`
    await dialog.getByLabel(uz.admin.reason).fill(reason)
    await expect(dialog.getByLabel(uz.builder.singleClassDate)).toHaveValue(addedDate)
    await dialog.getByRole('button', { name: uz.builder.addSingleClass }).click()
    await expect(dialog.getByText(uz.builder.singleClassAdded)).toBeVisible()
    const checkAdded = async () => {
      const result = await weekAt(addedDate ?? '')
      expect(
        result.days
          ?.find((day) => day.date === addedDate)
          ?.classes?.filter((item) => item.entryId === source.id),
      ).toHaveLength(1)
      expect(
        result.days?.find((day) => day.date === monday)?.classes?.some((item) => item.entryId === source.id),
      ).toBe(true)
    }
    await checkAdded()
    expect(hours(await load())).toBeGreaterThan(hours(originalLoad))
    await expectFitsScreen(page)
    await page.reload()
    dialog = await openPreview()
    await choose(page, uz.builder.sourceClass, new RegExp((source.startsAt ?? '').slice(0, 5)))
    await expect(dialog.getByText(reason, { exact: false })).toBeVisible()
    await checkAdded()
    const nextDate = shiftDate(addedDate, 7)
    await dialog.getByLabel(uz.builder.weekOf).fill(nextDate)
    const next = await weekAt(nextDate)
    expect(
      next.days?.find((day) => day.date === nextDate)?.classes?.some((item) => item.entryId === source.id),
    ).toBe(false)
    expect(next.days?.flatMap((day) => day.classes ?? []).some((item) => item.entryId === source.id)).toBe(
      true,
    )
    await dialog.getByLabel(uz.builder.weekOf).fill(addedDate)
    await choose(page, uz.builder.sourceClass, new RegExp((other.startsAt ?? '').slice(0, 5)))
    await choose(page, uz.builder.pair, new RegExp((first.startsAt ?? '').slice(0, 5)))
    await dialog.getByLabel(uz.admin.reason).fill(`${marker} conflict`)
    const refused = page.waitForResponse(
      (response) =>
        response.request().method() === 'POST' && response.url().includes(`/${other.id}/exceptions`),
    )
    await dialog.getByRole('button', { name: uz.builder.addSingleClass }).click()
    expect((await refused).status()).toBe(409)
    for (const kind of ['TEACHER', 'GROUP', 'ROOM'] as const) {
      await expect(dialog.getByRole('alert').filter({ hasText: uz.builder.conflict[kind] })).toBeVisible()
    }
    await expect(dialog.getByText(uz.common.requestId, { exact: false })).toBeVisible()
    await expectFitsScreen(page)
    await choose(page, uz.builder.sourceClass, new RegExp((source.startsAt ?? '').slice(0, 5)))
    await shot(page, '7.5-F3', `preview.light.uz.${testInfo.project.name}`)
    const weekRefresh = page.waitForResponse(
      (response) =>
        response.url().includes(`/timetables/${timetableId}/week`) && response.request().method() === 'GET',
    )
    const loadRefresh = page.waitForResponse(
      (response) =>
        response.url().includes(`/timetables/${timetableId}/load`) && response.request().method() === 'GET',
    )
    await dialog.getByRole('button', { name: uz.builder.deleteChange }).focus()
    await page.keyboard.press('Enter')
    expect((await weekRefresh).ok()).toBe(true)
    expect((await loadRefresh).ok()).toBe(true)
    await expect(dialog.getByText(reason, { exact: false })).toHaveCount(0)
    expect(hours(await load())).toBe(hours(originalLoad))
    const removed = await weekAt(addedDate)
    expect(
      removed.days
        ?.find((day) => day.date === addedDate)
        ?.classes?.some((item) => item.entryId === source.id),
    ).toBe(false)
    expect(
      removed.days?.find((day) => day.date === monday)?.classes?.some((item) => item.entryId === source.id),
    ).toBe(true)
    await page.keyboard.press('Escape')
    await expect(page.getByRole('dialog')).toHaveCount(0)
    await expectFitsScreen(page)
  } finally {
    // Only the isolated draft and its own reference records are removed; the published timetable is untouched.
    for (const [path, id] of [
      ['timetables', timetableId],
      ['groups', groupId],
      ['rooms', roomId],
    ] as const) {
      if (id) expect((await request.delete(`/api/v1/admin/${path}/${id}`, { headers })).ok()).toBe(true)
    }
  }
})
