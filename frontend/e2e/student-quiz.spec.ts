import { expect, test } from '@playwright/test'
import { clearQuizAttempts, endOpenAttemptIn, quizIdByCode } from './db'
import {
  ANSWERS,
  DASTURLASH,
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
import { isPhone, signIn, texts } from './support'

const { uz } = texts

test.describe('the quiz (S-06)', () => {
  test.beforeEach(() => {
    // an attempt cannot be undone through the API, and the quiz allows three
    clearQuizAttempts('student3', QUIZ_CODE)
  })

  test('every question type is answered, the attempt survives a reload, the result is reviewed', async ({
    page,
  }) => {
    test.setTimeout(120_000)
    await signIn(page, 'student3')

    // the way in: the course's work list
    await page.goto(`/student/courses/${DASTURLASH}?tab=work`)
    await page.getByRole('link', { name: new RegExp(QUIZ_TITLE) }).click()
    await expect(page.getByRole('heading', { level: 1, name: QUIZ_TITLE })).toBeVisible()
    await expect(page.getByText(uz.quiz.minutes.replace('{{count}}', '10'))).toBeVisible()
    await expect(page.getByText('0 / 3')).toBeVisible()

    await startQuiz(page)

    // 1. single choice
    await expectQuestion(page, 1)
    await choose(page, ANSWERS.algorithm)
    await expectSaved(page)
    await next(page)

    // 2. multiple choice
    for (const value of ANSWERS.integers) await choose(page, value)
    await next(page)

    // 3. true / false
    await choose(page, ANSWERS.falseStatement)
    await next(page)

    // 4. numeric: what is not a number is said to be so, and is not sent
    await page.getByRole('textbox').fill('ikki')
    await expect(page.getByText(uz.quiz.notANumber)).toBeVisible()
    await page.getByRole('textbox').fill(ANSWERS.remainder)
    await expect(page.getByText(uz.quiz.notANumber)).toHaveCount(0)
    await next(page)

    // 5. short answer — then the page is reloaded mid-attempt
    await expectQuestion(page, 5)
    await page.getByRole('textbox').fill(ANSWERS.printFunction)
    await expectSaved(page)
    await page.reload()
    await expectQuestion(page, 5)
    await expect(page.getByRole('textbox')).toHaveValue(ANSWERS.printFunction)
    await expect(page.getByRole('timer')).toBeVisible()
    await next(page)

    // 6. ordering
    await expectQuestion(page, 6)
    await arrange(page, ANSWERS.stages)
    // mixed into the right order already: then the order is confirmed as it stands
    const confirm = page.getByRole('button', { name: uz.quiz.confirmOrder })
    if (await confirm.isVisible()) await confirm.click()
    await next(page)

    // 7. matching: two pairs right, two swapped — half of its points
    await expectQuestion(page, 7)
    await pair(page, ...ANSWERS.pairs[0])
    await pair(page, ...ANSWERS.pairs[1])
    await pair(page, ANSWERS.pairs[2][0], ANSWERS.pairs[3][1])
    await pair(page, ANSWERS.pairs[3][0], ANSWERS.pairs[2][1])
    await expectSaved(page)

    // the navigator knows every question is answered
    await expect(
      page.getByText(uz.quiz.answeredCount.replace('{{answered}}', '7').replace('{{total}}', '7')).first(),
    ).toBeVisible()

    await finishQuiz(page)

    // the result: 9 of 10, passed; the review is open for this quiz
    await expect(page.getByRole('heading', { level: 2, name: uz.quiz.reviewTitle })).toBeVisible()
    const result = page.getByRole('region', { name: uz.quiz.resultTitle })
    await expect(result).toContainText('/ 10')
    await expect(result).toContainText('90%')
    await expect(result.getByText(uz.quiz.passed, { exact: true })).toBeVisible()
    await expect(result.getByText('9', { exact: true })).toBeVisible()
    await expect(page.getByText(uz.quiz.verdictPartial, { exact: true })).toBeVisible()
    await expect(page.getByText(uz.quiz.rightPairs)).toBeVisible()
    await expect(page.getByText(/17 = 5 · 3 \+ 2/)).toBeVisible()

    // back on the quiz page the attempt is listed, and one attempt is used
    await page.getByRole('link', { name: uz.quiz.toQuizPage }).click()
    await expect(page.getByRole('link', { name: /1-urinish.*9 \/ 10/ })).toBeVisible()
    await expect(page.getByText('1 / 3')).toBeVisible()
    await expect(page.getByRole('button', { name: uz.quiz.startAgain })).toBeVisible()
  })

  test('leaving and coming back resumes the attempt; unanswered questions are named before finishing', async ({
    page,
  }) => {
    await signIn(page, 'student3')
    const path = quizPath(quizIdByCode(QUIZ_CODE))
    await page.goto(path)
    await startQuiz(page)
    await choose(page, ANSWERS.algorithm)
    await expectSaved(page)

    // leave through the header, with the warning that the clock keeps running
    await page.getByRole('button', { name: uz.quiz.leave, exact: true }).click()
    await expect(page.getByRole('dialog')).toContainText(uz.quiz.leaveBodyTimed)
    await page.getByRole('dialog').getByRole('button', { name: uz.quiz.leave, exact: true }).click()
    await expect(page).toHaveURL(new RegExp(`/student/courses/${DASTURLASH}\\?tab=work`))

    // the quiz's own address goes straight back into the open attempt
    await page.goto(path)
    await expect(page).toHaveURL(/attempt=/)
    await expectQuestion(page, 1)
    await expect(page.getByRole('radio', { name: ANSWERS.algorithm })).toBeChecked()

    // a question is marked to come back to; the mark survives a reload
    await next(page)
    await page.getByRole('button', { name: uz.quiz.flag, exact: true }).click()
    await page.reload()
    await expect(page.getByRole('button', { name: uz.quiz.unflag, exact: true })).toBeVisible()

    // finishing with gaps: the dialog names them and leads to one
    for (let step = 2; step < 7; step += 1) await next(page)
    await page.getByRole('button', { name: uz.quiz.finish, exact: true }).last().click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toContainText(
      uz.quiz.answeredCount.replace('{{answered}}', '1').replace('{{total}}', '7'),
    )
    await expect(dialog).toContainText(uz.quiz.unansweredTitle)
    await dialog
      .getByRole('button', { name: uz.quiz.goToQuestion.replace('{{number}}', '3'), exact: true })
      .click()
    await expectQuestion(page, 3)

    await page.getByRole('navigation', { name: uz.quiz.navigator }).getByRole('button').nth(6).click()
    await expectQuestion(page, 7)
    await finishQuiz(page)

    const result = page.getByRole('region', { name: uz.quiz.resultTitle })
    await expect(result).toContainText('10%')
    await expect(result.getByText(uz.quiz.failed, { exact: true })).toBeVisible()
    await expect(page.getByText(uz.quiz.notAnswered, { exact: true }).first()).toBeVisible()
  })

  test('when the time runs out the quiz hands itself in and the saved answers are graded', async ({
    page,
  }) => {
    await signIn(page, 'student3')
    await page.goto(quizPath(quizIdByCode(QUIZ_CODE)))
    await startQuiz(page)
    await choose(page, ANSWERS.algorithm)
    await expectSaved(page)
    await next(page)
    for (const value of ANSWERS.integers) await choose(page, value)
    await expectSaved(page)

    // ten minutes become eight seconds; the page learns it when it reads the attempt again
    endOpenAttemptIn('student3', QUIZ_CODE, 8)
    await page.reload()
    await expect(page.getByRole('timer')).toHaveText(/0:0\d/)

    // nobody presses anything
    await expect(page.getByText(uz.quiz.closedByTime)).toBeVisible({ timeout: 30_000 })
    const result = page.getByRole('region', { name: uz.quiz.resultTitle })
    await expect(result.getByText('3', { exact: true })).toBeVisible()
    await expect(result).toContainText('30%')
    await expect(result.getByText(uz.quiz.failed, { exact: true })).toBeVisible()
  })

  test('the questions follow the interface language', async ({ page }, testInfo) => {
    await signIn(page, 'student3')
    await page.goto(quizPath(quizIdByCode(QUIZ_CODE)))
    await startQuiz(page)
    await expect(page.getByRole('heading', { level: 1, name: 'Algoritm nima?' })).toBeVisible()
    await choose(page, ANSWERS.algorithm)
    await expectSaved(page)

    if (isPhone(testInfo)) {
      await page.getByRole('button', { name: new RegExp(`^${uz.common.language}`) }).click()
      await page.getByRole('menuitem', { name: 'RU' }).click()
    } else {
      await page.getByRole('radio', { name: 'RU', exact: true }).click()
    }
    await expect(page.getByRole('heading', { level: 1, name: 'Что такое алгоритм?' })).toBeVisible()
    // the answer given in Uzbek is still the chosen one
    await expect(
      page.getByRole('radio', { name: 'Точная последовательность шагов для решения задачи' }),
    ).toBeChecked()
  })
})
