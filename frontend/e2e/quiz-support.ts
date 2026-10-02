import { expect, type Page } from '@playwright/test'
import { texts } from './support'

const { uz } = texts

export const DASTURLASH = '2eec7704-6e25-4adb-966e-b54de91a9b69'
/** "Dasturlash asoslari: takrorlash testi": seven questions, one of every type, ten minutes. */
export const QUIZ_CODE = 'DAS-Q-M2'
export const QUIZ_TITLE = 'Dasturlash asoslari: takrorlash testi'

export const ANSWERS = {
  algorithm: 'Masalani yechish uchun aniq qadamlar ketma-ketligi',
  integers: ['42', '−7'],
  falseStatement: 'Notoʻgʻri',
  remainder: '2',
  printFunction: 'Print',
  stages: ['Masalani tahlil qilish', 'Algoritm tuzish', 'Kodni yozish', 'Testlash va xatolarni tuzatish'],
  pairs: [
    ['42', 'butun son (int)'],
    ['3.14', 'haqiqiy son (float)'],
    ['"salom"', 'satr (str)'],
    ['True', 'mantiqiy qiymat (bool)'],
  ],
} as const

export function quizPath(quizId: string): string {
  return `/student/courses/${DASTURLASH}/quizzes/${quizId}`
}

/** From the start screen into a new attempt: the clock starts on the confirmation. */
export async function startQuiz(page: Page): Promise<void> {
  await page.getByRole('button', { name: uz.quiz.start, exact: true }).click()
  await page.getByRole('dialog').getByRole('button', { name: uz.quiz.start, exact: true }).click()
  await expect(page).toHaveURL(/attempt=/)
  await expect(page.getByRole('timer')).toBeVisible()
}

export async function expectQuestion(page: Page, number: number): Promise<void> {
  await expect(
    page.getByText(uz.quiz.questionOf.replace('{{number}}', String(number)).replace('{{total}}', '7')),
  ).toBeVisible()
}

export async function next(page: Page): Promise<void> {
  await page.getByRole('button', { name: uz.common.next, exact: true }).click()
}

export async function expectSaved(page: Page): Promise<void> {
  await expect(page.getByRole('banner').getByText(uz.quiz.saved, { exact: true })).toBeVisible()
}

/** A choice option is the label of a real radio or checkbox: clicking its text chooses it. */
export async function choose(page: Page, optionText: string): Promise<void> {
  await page
    .locator('label')
    .filter({ hasText: new RegExp(`^${escapeRegExp(optionText)}$`) })
    .click()
}

/** Brings the ordering question to `wanted`, whatever order the server mixed the options into. */
export async function arrange(page: Page, wanted: readonly string[]): Promise<void> {
  for (const [place, text] of wanted.entries()) {
    for (;;) {
      const current = await page.getByRole('main').getByRole('listitem').allInnerTexts()
      const at = current.findIndex((item) => item.includes(text))
      if (at <= place) break
      await page.getByRole('button', { name: uz.quiz.moveUp.replace('{{text}}', text), exact: true }).click()
    }
  }
}

export async function pair(page: Page, first: string, second: string): Promise<void> {
  await page.getByRole('main').getByRole('button', { name: first, exact: true }).click()
  await page.getByRole('main').getByRole('button', { name: second, exact: true }).click()
}

/** Opens the confirmation from the last question and hands the attempt in. */
export async function finishQuiz(page: Page): Promise<void> {
  await page.getByRole('button', { name: uz.quiz.finish, exact: true }).last().click()
  await page.getByRole('dialog').getByRole('button', { name: uz.quiz.finish, exact: true }).click()
}

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}
