// Validation rules for the assessment files of phase 6 (docs/PLAN.md §4, C0):
// content/<COURSE>/<LESSON>/assess.json and content/<COURSE>/midterm.json.
// Pure functions, no fs and no network: each takes parsed JSON and returns a list of
// human-readable problems (empty list = valid).
import { countViolations } from './fix-apostrophes.mjs'

export const QUESTION_TYPES = ['SINGLE', 'MULTI', 'TF', 'SHORT', 'NUM']
export const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD']
export const CODE_LANGUAGES = ['python', 'java', 'cpp']
const LOCALES = ['uz', 'ru', 'en']

export const MIN_TEST_QUESTIONS = 8
export const MAX_TEST_QUESTIONS = 10
export const MIN_PER_DIFFICULTY = 2
export const MIN_CODE_TESTS = 3
export const MIN_HIDDEN_TESTS = 2
export const MIN_MIDTERM_QUESTIONS = 60

const isNonEmptyString = (value) => typeof value === 'string' && value.trim() !== ''
const isPlainObject = (value) => value !== null && typeof value === 'object' && !Array.isArray(value)

/** A {uz, ru, en} text: every locale non-empty, the uz one in the official orthography. */
function checkLocalized(value, at, problems) {
  if (!isPlainObject(value)) {
    problems.push(`${at}: must be an object with uz, ru and en`)
    return
  }
  for (const locale of LOCALES) {
    const text = value[locale]
    if (!isNonEmptyString(text)) {
      problems.push(`${at}.${locale}: empty or missing`)
      continue
    }
    if (locale === 'uz' && countViolations(text) > 0) {
      problems.push(`${at}.uz: ASCII apostrophe — use ʻ (U+02BB) after o/g and ʼ (U+02BC) between other letters`)
    }
  }
}

export function validateQuestion(question, at, problems, { requireLesson = false } = {}) {
  if (!isPlainObject(question)) {
    problems.push(`${at}: must be an object`)
    return
  }
  const type = question.type
  if (!QUESTION_TYPES.includes(type)) {
    problems.push(`${at}.type: must be one of ${QUESTION_TYPES.join('|')}`)
  }
  if (!DIFFICULTIES.includes(question.difficulty)) {
    problems.push(`${at}.difficulty: must be one of ${DIFFICULTIES.join('|')}`)
  }
  if (typeof question.points !== 'number' || !Number.isFinite(question.points) || question.points <= 0) {
    problems.push(`${at}.points: must be a positive number`)
  }
  checkLocalized(question.prompt, `${at}.prompt`, problems)
  checkLocalized(question.explanation, `${at}.explanation`, problems)
  if (requireLesson && !isNonEmptyString(question.lesson)) {
    problems.push(`${at}.lesson: missing (the lesson code the question belongs to)`)
  }

  if (type === 'SINGLE' || type === 'MULTI' || type === 'TF') {
    const options = question.options
    const optionCount = Array.isArray(options) ? options.length : 0
    if (!Array.isArray(options) || optionCount < 2) {
      problems.push(`${at}.options: ${type} needs at least 2 options`)
    } else {
      if (type === 'TF' && optionCount !== 2) {
        problems.push(`${at}.options: TF needs exactly 2 options, has ${optionCount}`)
      }
      options.forEach((option, i) => checkLocalized(option, `${at}.options[${i}]`, problems))
    }
    const correct = question.correct
    if (!Array.isArray(correct) || correct.length === 0) {
      problems.push(`${at}.correct: must be a non-empty array of option indices`)
    } else {
      if ((type === 'SINGLE' || type === 'TF') && correct.length !== 1) {
        problems.push(`${at}.correct: ${type} needs exactly 1 index, has ${correct.length}`)
      }
      for (const index of correct) {
        if (!Number.isInteger(index) || index < 0 || index >= optionCount) {
          problems.push(`${at}.correct: index ${JSON.stringify(index)} out of range (0..${optionCount - 1})`)
        }
      }
      if (new Set(correct).size !== correct.length) {
        problems.push(`${at}.correct: duplicate indices`)
      }
    }
  }

  if (type === 'SHORT') {
    const answers = question.answers
    if (!Array.isArray(answers) || answers.length === 0 || !answers.every(isNonEmptyString)) {
      problems.push(`${at}.answers: SHORT needs at least one non-empty accepted answer`)
    }
  }

  if (type === 'NUM') {
    const answers = question.answers
    if (!Array.isArray(answers) || answers.length === 0 || typeof answers[0] !== 'number' || !Number.isFinite(answers[0])) {
      problems.push(`${at}.answers: NUM needs a numeric first answer`)
    }
    if (typeof question.tolerance !== 'number' || !Number.isFinite(question.tolerance) || question.tolerance < 0) {
      problems.push(`${at}.tolerance: NUM needs a numeric tolerance >= 0`)
    }
  }
}

export function validateTest(test, at, problems) {
  if (!isPlainObject(test)) {
    problems.push(`${at}: must be an object with questions`)
    return
  }
  const questions = test.questions
  if (!Array.isArray(questions)) {
    problems.push(`${at}.questions: must be an array`)
    return
  }
  if (questions.length < MIN_TEST_QUESTIONS || questions.length > MAX_TEST_QUESTIONS) {
    problems.push(`${at}.questions: ${questions.length} question(s), need ${MIN_TEST_QUESTIONS}–${MAX_TEST_QUESTIONS}`)
  }
  for (const band of DIFFICULTIES) {
    const count = questions.filter((question) => isPlainObject(question) && question.difficulty === band).length
    if (count < MIN_PER_DIFFICULTY) {
      problems.push(`${at}.questions: only ${count} ${band} question(s), need at least ${MIN_PER_DIFFICULTY}`)
    }
  }
  questions.forEach((question, i) => validateQuestion(question, `${at}.questions[${i}]`, problems))
}

/** Code without string literals, so a '#' or '//' inside a string is not read as a comment. */
function stripStringLiterals(code) {
  return code
    .replace(/"(?:[^"\\\n]|\\.)*"/g, '""')
    .replace(/'(?:[^'\\\n]|\\.)*'/g, "''")
    .replace(/`(?:[^`\\]|\\.)*`/g, '``')
}

function checkStarter(code, language, at, problems) {
  if (!isNonEmptyString(code)) {
    problems.push(`${at}: empty or missing`)
    return
  }
  const bare = stripStringLiterals(code)
  // Python: '#' starts a comment ('//' is floor division, allowed). Java/C++: '//' and '/*'
  // ('#' in C++ is the preprocessor, allowed — it is code, not prose).
  const comment = language === 'python' ? /#/ : /\/\/|\/\*/
  if (comment.test(bare)) {
    problems.push(`${at}: starter code must not contain natural-language comments`)
  }
  if (/\bTODO\b/i.test(code)) {
    problems.push(`${at}: starter code must be TODO-free`)
  }
}

export function validatePractice(practice, at, problems) {
  if (!isPlainObject(practice)) {
    problems.push(`${at}: must be an object`)
    return
  }
  const kind = practice.kind
  if (kind !== 'CODE' && kind !== 'ANSWER') {
    problems.push(`${at}.kind: must be CODE or ANSWER`)
  }
  checkLocalized(practice.title, `${at}.title`, problems)
  checkLocalized(practice.statement, `${at}.statement`, problems)

  if (kind === 'CODE') {
    const code = practice.code
    if (!isPlainObject(code)) {
      problems.push(`${at}.code: required for kind CODE`)
      return
    }
    for (const language of CODE_LANGUAGES) {
      checkStarter(isPlainObject(code.starter) ? code.starter[language] : undefined, language, `${at}.code.starter.${language}`, problems)
      if (!isNonEmptyString(isPlainObject(code.solution) ? code.solution[language] : undefined)) {
        problems.push(`${at}.code.solution.${language}: empty or missing`)
      }
    }
    const tests = code.tests
    if (!Array.isArray(tests) || tests.length < MIN_CODE_TESTS) {
      problems.push(`${at}.code.tests: ${Array.isArray(tests) ? tests.length : 0} test(s), need at least ${MIN_CODE_TESTS}`)
      return
    }
    const hidden = tests.filter((test) => isPlainObject(test) && test.hidden === true).length
    if (hidden < MIN_HIDDEN_TESTS) {
      problems.push(`${at}.code.tests: only ${hidden} hidden, need at least ${MIN_HIDDEN_TESTS}`)
    }
    tests.forEach((test, i) => {
      if (!isPlainObject(test)) {
        problems.push(`${at}.code.tests[${i}]: must be an object`)
        return
      }
      if (typeof test.input !== 'string') problems.push(`${at}.code.tests[${i}].input: must be a string`)
      if (typeof test.expected !== 'string') problems.push(`${at}.code.tests[${i}].expected: must be a string`)
      if (typeof test.hidden !== 'boolean') problems.push(`${at}.code.tests[${i}].hidden: must be a boolean`)
    })
  }

  if (kind === 'ANSWER') {
    const questions = isPlainObject(practice.answer) ? practice.answer.questions : undefined
    if (!Array.isArray(questions) || questions.length === 0) {
      problems.push(`${at}.answer.questions: required for kind ANSWER (a non-empty array)`)
      return
    }
    questions.forEach((question, i) => validateQuestion(question, `${at}.answer.questions[${i}]`, problems))
  }
}

/** One lesson's assess.json. Returns the list of problems (empty = valid). */
export function validateAssess(data) {
  if (!isPlainObject(data)) return ['root: must be an object']
  const problems = []
  validateTest(data.test, 'test', problems)
  validatePractice(data.practice, 'practice', problems)
  return problems
}

/** A course's midterm.json. Returns the list of problems (empty = valid). */
export function validateMidterm(data) {
  if (!isPlainObject(data)) return ['root: must be an object']
  const problems = []
  if (!isNonEmptyString(data.afterLesson)) {
    problems.push('afterLesson: missing (the lesson code the midterm follows)')
  }
  const questions = data.questions
  if (!Array.isArray(questions)) {
    problems.push('questions: must be an array')
    return problems
  }
  if (questions.length < MIN_MIDTERM_QUESTIONS) {
    problems.push(`questions: ${questions.length} question(s), need at least ${MIN_MIDTERM_QUESTIONS}`)
  }
  questions.forEach((question, i) => validateQuestion(question, `questions[${i}]`, problems, { requireLesson: true }))
  return problems
}
