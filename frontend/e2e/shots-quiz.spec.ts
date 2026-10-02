import { expect, test } from '@playwright/test'
import { clearQuizAttempts, quizIdByCode } from './db'
import {
  ANSWERS,
  QUIZ_CODE,
  QUIZ_TITLE,
  arrange,
  choose,
  expectQuestion,
  expectSaved,
  finishQuiz,
  next,
  pair,
  quizPath,
  startQuiz,
} from './quiz-support'
import { shot, signIn, texts } from './support'

/** Review screenshots of the quiz (phase F2, S-06): `npm run shots`. */
const { uz } = texts

test.describe('@shots F2 quiz', () => {
  for (const theme of ['light', 'dark'] as const) {
    test(`quiz ${theme}`, async ({ page }, testInfo) => {
      test.setTimeout(180_000)
      const project = testInfo.project.name
      const name = (screen: string) => `quiz-${screen}.${theme}.uz.${project}`
      clearQuizAttempts('student3', QUIZ_CODE)
      await page.addInitScript((value) => {
        localStorage.setItem('aifu-theme', value)
      }, theme)
      await signIn(page, 'student3')
      await page.goto(quizPath(quizIdByCode(QUIZ_CODE)))
      await expect(page.getByRole('heading', { level: 1, name: QUIZ_TITLE })).toBeVisible()
      await shot(page, 'f2', name('start'))

      await startQuiz(page)
      await choose(page, ANSWERS.algorithm)
      await expectSaved(page)
      await shot(page, 'f2', name('q1-single'))

      await next(page)
      await choose(page, '42')
      await choose(page, '3.14')
      await shot(page, 'f2', name('q2-multiple'))

      await next(page)
      await next(page)
      await page.getByRole('textbox').fill('abc')
      await shot(page, 'f2', name('q4-numeric-invalid'))
      await page.getByRole('textbox').fill(ANSWERS.remainder)

      await next(page)
      await page.getByRole('textbox').fill('input')

      await next(page)
      await expectQuestion(page, 6)
      await arrange(page, ANSWERS.stages.slice(0, 2))
      await shot(page, 'f2', name('q6-ordering'))

      await next(page)
      await pair(page, ...ANSWERS.pairs[0])
      await pair(page, '3.14', 'satr (str)')
      await page.getByRole('main').getByRole('button', { name: 'True', exact: true }).click()
      await page.getByRole('button', { name: uz.quiz.flag, exact: true }).click()
      await shot(page, 'f2', name('q7-matching'))

      await page.getByRole('button', { name: uz.quiz.finish, exact: true }).last().click()
      await expect(page.getByRole('dialog')).toBeVisible()
      await page.waitForTimeout(400)
      await page.screenshot({ path: `e2e/__screenshots__/f2/${name('finish-dialog')}.png` })
      await page.getByRole('dialog').getByRole('button', { name: uz.quiz.keepWorking }).click()

      await finishQuiz(page)
      await expect(page.getByRole('heading', { level: 2, name: uz.quiz.reviewTitle })).toBeVisible()
      await page.waitForTimeout(1500)
      await shot(page, 'f2', name('result'))

      await page.getByRole('link', { name: uz.quiz.toQuizPage }).click()
      await expect(page.getByRole('heading', { level: 2, name: uz.quiz.attemptsTitle })).toBeVisible()
      await shot(page, 'f2', name('start-after'))
    })
  }
})
