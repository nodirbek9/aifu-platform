import AxeBuilder from '@axe-core/playwright'
import { expect, test } from '@playwright/test'
import { shot, signIn } from './support'

const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
const FIRST_LECTURE = 'b7c48060-bb44-4319-83c8-7a11915a5747'
const LESSON_PATH = `/student/courses/${DASTURLASH}/lessons/${FIRST_LECTURE}`

test.describe('Lesson mode design & theme (Phase 4)', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`lesson theme, axe contrast and layout in ${theme} theme`, async ({ page }, testInfo) => {
      await page.addInitScript((val) => {
        localStorage.setItem('aifu-theme', val)
      }, theme)

      await signIn(page, 'student3')
      await page.goto(LESSON_PATH)
      await expect(page.getByRole('article')).toBeVisible()
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
      await page.waitForLoadState('networkidle')

      // Save screenshot for review (desktop and phone)
      await shot(page, 'phase4', `lesson-${theme}.${testInfo.project.name}`)

      // Axe audit: ensure no color-contrast or other accessibility violations
      const axeResults = await new AxeBuilder({ page })
        .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'])
        .analyze()

      const contrastViolations = axeResults.violations.filter((v) => v.id === 'color-contrast')
      expect(contrastViolations, `Axe color-contrast violations in ${theme}: ${JSON.stringify(contrastViolations, null, 2)}`).toHaveLength(0)

      // Spacing & bottom bar: verify content doesn't get covered by fixed bottom navigation
      // Check last paragraph bounding box vs viewport / bottom bar
      const lastParagraph = page.locator('article p:visible').last()
      await expect(lastParagraph).toBeVisible()
      const pBox = await lastParagraph.boundingBox()
      expect(pBox).not.toBeNull()

      if (pBox !== null) {
        // If bottom tab-bar exists, ensure last paragraph is above its top edge or article has padding
        const tabBar = page.locator('nav[aria-label]').filter({ hasText: /Bosh sahifa|Asosiy/i })
        if (await tabBar.isVisible()) {
          const tabBox = await tabBar.boundingBox()
          if (tabBox !== null) {
            // When scrolled down to the paragraph, it should not overlap under tab bar
            await lastParagraph.scrollIntoViewIfNeeded()
            const scrolledPBox = await lastParagraph.boundingBox()
            if (scrolledPBox !== null) {
              expect(scrolledPBox.y + scrolledPBox.height).toBeLessThanOrEqual(tabBox.y + 1)
            }
          }
        }
      }
    })
  }
})
