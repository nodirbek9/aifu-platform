import { expect, test } from '@playwright/test'
import { shot, signIn } from './support'

// Phase 8: the latest grades name the work, the timetable shows the whole course name, and no
// uz title has an ASCII apostrophe between letters.
test.describe('small defects (phase 8)', () => {
  test('the latest grades name the graded work, not only the course code', async ({ page }, testInfo) => {
    await signIn(page, 'student3')
    await page.goto('/student')
    await page.waitForLoadState('networkidle')
    await shot(page, 'phase8', `home.${testInfo.project.name}`)
    const block = page
      .getByRole('region')
      .filter({ hasText: /Soʻnggi baholar/ })
      .first()
    await expect(block).toBeVisible()
    const rows = block.getByRole('listitem')
    expect(await rows.count()).toBeGreaterThan(0)
    // the main text of every row is more than a bare subject code such as DAS1110
    for (const text of await rows.allInnerTexts()) {
      const firstLine = text.split('\n')[0]?.trim() ?? ''
      expect(firstLine, text).not.toMatch(/^[A-Z]{2,6}\d{3,5}$/)
    }
  })

  test('a class card on the timetable shows the course name without cutting the term', async ({
    page,
  }, testInfo) => {
    await signIn(page, 'student3')
    await page.goto('/student/timetable')
    await page.waitForLoadState('networkidle')
    await shot(page, 'phase8', `timetable.${testInfo.project.name}`)
    await expect(page.locator('main')).not.toContainText(/\(\d{4}\/\d{4}, \d+-…/)
  })

  test('uz lesson titles use the Uzbek apostrophes', async ({ page }) => {
    await signIn(page, 'student3')
    await page.goto('/student/courses/2eec7704-6e25-4adb-966e-b54de91a9b69')
    await page.waitForLoadState('networkidle')
    await expect(page.getByRole('main')).toContainText('Dasturlashning asosiy tushunchalari')
    // a person's name ("Quvondiq o'g'li") is data the registrar typed; lesson titles are ours
    const text = (await page.getByRole('main').innerText()).replaceAll("o'g'li", '')
    expect(text).not.toMatch(/[A-Za-z]'[A-Za-z]/)
  })
})
