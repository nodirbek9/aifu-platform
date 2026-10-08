// Tests for the assessment importer (docs/PLAN.md §4, C0). A fake api keeps the whole
// platform in memory; the tests cover a first import (create), a second run (idempotent:
// nothing new is created), a dry run (GETs only) and the path_step capability probe.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { test } from 'node:test'
import { importCourse } from './assess-import.mjs'

// ------------------------------------------------------------------ fixture content

const tr = (uz = 'Savol matni', ru = 'Текст вопроса', en = 'Question text') => ({ uz, ru, en })
const option = (text) => tr(text, 'Вариант', 'Option')

function question(overrides = {}) {
  return {
    type: 'SINGLE',
    difficulty: 'EASY',
    points: 1,
    prompt: tr(),
    options: [option('Birinchi'), option('Ikkinchi'), option('Uchinchi')],
    correct: [1],
    explanation: tr('Izoh', 'Пояснение', 'Explanation'),
    ...overrides,
  }
}

/** 9 questions: 3 per difficulty band, all five types — passes validateAssess. */
function testQuestions() {
  return [
    question({ difficulty: 'EASY' }),
    question({ difficulty: 'EASY', type: 'MULTI', correct: [0, 2] }),
    question({ difficulty: 'EASY', type: 'TF', options: [option('Toʻgʻri'), option('Notoʻgʻri')], correct: [0] }),
    question({ difficulty: 'MEDIUM', type: 'SHORT', answers: ['massiv'], options: undefined, correct: undefined }),
    question({ difficulty: 'MEDIUM', type: 'NUM', answers: [3.14], tolerance: 0.01, options: undefined, correct: undefined }),
    question({ difficulty: 'MEDIUM' }),
    question({ difficulty: 'HARD' }),
    question({ difficulty: 'HARD', type: 'MULTI', correct: [1] }),
    question({ difficulty: 'HARD', type: 'SHORT', answers: ['sikl', 'takrorlash'], options: undefined, correct: undefined }),
  ]
}

const CODE = {
  starter: { python: 'n = int(input())\n', java: 'public class Main {}\n', cpp: 'int main() {}\n' },
  solution: { python: 'print(1)\n', java: 'public class Main {}\n', cpp: 'int main() {}\n' },
  tests: [
    { input: '1\n', expected: '1\n', hidden: false },
    { input: '2\n', expected: '2\n', hidden: true },
    { input: '3\n', expected: '3\n', hidden: true },
  ],
}

/** A course folder on disk: M1 with a CODE practice, M2 with an ANSWER practice, a midterm. */
function writeCourseFolder() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-import-'))
  fs.writeFileSync(path.join(dir, 'course.json'), JSON.stringify({ subjectCode: 'TST1000', termName: '1-semestr' }))
  fs.mkdirSync(path.join(dir, 'M1'))
  fs.writeFileSync(
    path.join(dir, 'M1', 'assess.json'),
    JSON.stringify({
      test: { questions: testQuestions() },
      practice: { kind: 'CODE', title: tr('Mashq', 'Задание', 'Practice'), statement: tr('Shart', 'Условие', 'Statement'), code: CODE },
    }),
  )
  fs.mkdirSync(path.join(dir, 'M2'))
  fs.writeFileSync(
    path.join(dir, 'M2', 'assess.json'),
    JSON.stringify({
      test: { questions: testQuestions() },
      practice: {
        kind: 'ANSWER',
        title: tr('Savollar', 'Вопросы', 'Questions'),
        statement: tr('Shart', 'Условие', 'Statement'),
        answer: { questions: [question(), question({ type: 'SHORT', answers: ['javob'], options: undefined, correct: undefined })] },
      },
    }),
  )
  fs.writeFileSync(
    path.join(dir, 'midterm.json'),
    JSON.stringify({ afterLesson: 'M1', questions: Array.from({ length: 60 }, () => question({ lesson: 'M1' })) }),
  )
  return dir
}

// ------------------------------------------------------------------ the fake api

class FakeApi {
  constructor({ pathStep = false, seedActivity = false } = {}) {
    this.calls = []
    this.pathStep = pathStep
    this.next = 1
    this.course = {
      id: 'course-1',
      subjectCode: 'TST1000',
      termName: '1-semestr',
      assessmentComponents: [{ id: 'comp-on', code: 'ON', kind: 'MIDTERM', maxPoints: 30 }],
    }
    this.modules = [{ id: 'mod-1', code: 'MOD1' }]
    this.lessons = [
      { id: 'les-1', code: 'M1', moduleId: 'mod-1', position: 1, sessionType: 'LECTURE', status: 'PUBLISHED', title: 'Birinchi dars' },
      { id: 'les-2', code: 'M2', moduleId: 'mod-1', position: 2, sessionType: 'PRACTICAL', status: 'PUBLISHED', title: 'Ikkinchi dars' },
    ]
    this.lessonTranslations = {
      'les-1': [
        { locale: 'uz', title: 'Birinchi dars' },
        { locale: 'ru', title: 'Первый урок' },
        { locale: 'en', title: 'First lesson' },
      ],
      'les-2': [
        { locale: 'uz', title: 'Ikkinchi dars' },
        { locale: 'ru', title: 'Второй урок' },
        { locale: 'en', title: 'Second lesson' },
      ],
    }
    this.quizzes = []
    this.tasks = []
    this.activities = []
    if (seedActivity) {
      this.activities.push({
        id: 'act-seed',
        courseId: 'course-1',
        moduleId: 'mod-1',
        lessonId: 'les-1',
        position: 1,
        activityType: 'MANUAL_TASK',
        referenceId: null,
        status: 'PUBLISHED',
      })
    }
  }

  id(prefix) {
    const value = `${prefix}-${this.next}`
    this.next += 1
    return value
  }

  serializeActivity(activity) {
    return this.pathStep ? { pathStep: activity.pathStep ?? null, ...activity } : { ...activity }
  }

  async call(method, rawUrl, body) {
    this.calls.push({ method, url: rawUrl, body })
    return this.route(method, rawUrl.split('?')[0], body ?? {})
  }

  route(method, url, body) {
    let match
    const found = (list, id, what) => {
      const item = list.find((entry) => (entry.id ?? entry.taskLanguageId) === id)
      if (!item) throw new Error(`fake api: no ${what} ${id}`)
      return item
    }

    if (method === 'GET' && url === '/admin/courses') {
      return { content: [{ id: 'course-1', subjectCode: 'TST1000', termName: '1-semestr' }] }
    }
    if (method === 'GET' && url === '/admin/courses/course-1') return this.course
    if (method === 'GET' && url === '/teacher/courses/course-1/modules') return { content: this.modules }
    if (method === 'GET' && (match = url.match(/^\/teacher\/modules\/([^/]+)\/lessons$/))) {
      return { content: this.lessons.filter((lesson) => lesson.moduleId === match[1]) }
    }
    if (method === 'GET' && (match = url.match(/^\/teacher\/lessons\/([^/]+)$/))) {
      const lesson = found(this.lessons, match[1], 'lesson')
      return { ...lesson, blocks: [], translations: this.lessonTranslations[lesson.id] ?? [] }
    }

    // quizzes
    if (method === 'GET' && url === '/teacher/courses/course-1/quizzes') return { content: this.quizzes }
    if (method === 'POST' && url === '/teacher/courses/course-1/quizzes') {
      const quiz = { id: this.id('quiz'), code: body.code, status: 'DRAFT', questions: [], translations: [] }
      this.quizzes.push(quiz)
      return quiz
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)$/))) {
      return Object.assign(found(this.quizzes, match[1], 'quiz'), body)
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)\/status$/))) {
      const quiz = found(this.quizzes, match[1], 'quiz')
      quiz.status = body.status
      return quiz
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)\/translations\/([^/]+)$/))) {
      const quiz = found(this.quizzes, match[1], 'quiz')
      quiz.translations.push({ locale: match[2], ...body })
      return quiz
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)\/questions\/reorder$/))) {
      const quiz = found(this.quizzes, match[1], 'quiz')
      for (const [index, id] of body.orderedQuestionIds.entries()) {
        found(quiz.questions, id, 'question').position = index + 1
      }
      return null
    }
    if (method === 'GET' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)\/questions$/))) {
      return { content: found(this.quizzes, match[1], 'quiz').questions }
    }
    if (method === 'POST' && (match = url.match(/^\/teacher\/quizzes\/([^/]+)\/questions$/))) {
      const quiz = found(this.quizzes, match[1], 'quiz')
      const question = {
        id: this.id('q'),
        quizId: quiz.id,
        position: Math.max(0, ...quiz.questions.map((q) => q.position)) + 1,
        questionType: body.questionType,
        points: body.points,
        options: [],
        answerKeys: [],
        translations: [],
      }
      quiz.questions.push(question)
      return question
    }

    // questions, options, answer keys
    const everyQuestion = () => this.quizzes.flatMap((quiz) => quiz.questions)
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/questions\/([^/]+)$/))) {
      return Object.assign(found(everyQuestion(), match[1], 'question'), body)
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/questions\/([^/]+)\/translations\/([^/]+)$/))) {
      const question = found(everyQuestion(), match[1], 'question')
      question.translations.push({ locale: match[2], ...body })
      return question
    }
    if (method === 'POST' && (match = url.match(/^\/teacher\/questions\/([^/]+)\/options$/))) {
      const question = found(everyQuestion(), match[1], 'question')
      const option = {
        id: this.id('opt'),
        position: Math.max(0, ...question.options.map((o) => o.position)) + 1,
        isCorrect: body.isCorrect,
        translations: [],
      }
      question.options.push(option)
      return option
    }
    if (method === 'POST' && (match = url.match(/^\/teacher\/questions\/([^/]+)\/answer-keys$/))) {
      const question = found(everyQuestion(), match[1], 'question')
      const key = { id: this.id('key'), ...body }
      question.answerKeys.push(key)
      return key
    }
    if (method === 'DELETE' && (match = url.match(/^\/teacher\/questions\/([^/]+)$/))) {
      const quiz = this.quizzes.find((q) => q.questions.some((question) => question.id === match[1]))
      quiz.questions = quiz.questions.filter((question) => question.id !== match[1])
      return null
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/options\/([^/]+)$/))) {
      return Object.assign(found(everyQuestion().flatMap((q) => q.options), match[1], 'option'), body)
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/options\/([^/]+)\/translations\/([^/]+)$/))) {
      const option = found(everyQuestion().flatMap((q) => q.options), match[1], 'option')
      option.translations.push({ locale: match[2], ...body })
      return option
    }
    if (method === 'DELETE' && (match = url.match(/^\/teacher\/options\/([^/]+)$/))) {
      for (const question of everyQuestion()) question.options = question.options.filter((o) => o.id !== match[1])
      return null
    }
    if (method === 'DELETE' && (match = url.match(/^\/teacher\/answer-keys\/([^/]+)$/))) {
      for (const question of everyQuestion()) question.answerKeys = question.answerKeys.filter((k) => k.id !== match[1])
      return null
    }

    // coding tasks
    if (method === 'GET' && url === '/teacher/courses/course-1/coding-tasks') return { content: this.tasks }
    if (method === 'POST' && url === '/teacher/courses/course-1/coding-tasks') {
      const task = { id: this.id('task'), code: body.code, status: 'DRAFT', languages: [], testCases: [], translations: [] }
      this.tasks.push(task)
      return task
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)$/))) {
      return Object.assign(found(this.tasks, match[1], 'coding task'), body)
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/status$/))) {
      const task = found(this.tasks, match[1], 'coding task')
      task.status = body.status
      return task
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/translations\/([^/]+)$/))) {
      const task = found(this.tasks, match[1], 'coding task')
      task.translations.push({ locale: match[2], ...body })
      return task
    }
    if (method === 'GET' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/languages$/))) {
      return found(this.tasks, match[1], 'coding task').languages
    }
    if (method === 'POST' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/languages\/([^/]+)$/))) {
      const task = found(this.tasks, match[1], 'coding task')
      const language = { taskLanguageId: this.id('lang'), code: match[2], ...body }
      task.languages.push(language)
      return language
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/coding-task-languages\/([^/]+)$/))) {
      const language = found(this.tasks.flatMap((t) => t.languages), match[1], 'language')
      return Object.assign(language, body)
    }
    if (method === 'GET' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/test-cases$/))) {
      return found(this.tasks, match[1], 'coding task').testCases
    }
    if (method === 'POST' && (match = url.match(/^\/teacher\/coding-tasks\/([^/]+)\/test-cases$/))) {
      const task = found(this.tasks, match[1], 'coding task')
      const testCase = {
        id: this.id('tc'),
        position: Math.max(0, ...task.testCases.map((t) => t.position)) + 1,
        ...body,
      }
      task.testCases.push(testCase)
      return testCase
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/coding-task-test-cases\/([^/]+)$/))) {
      return Object.assign(found(this.tasks.flatMap((t) => t.testCases), match[1], 'test case'), body)
    }
    if (method === 'DELETE' && (match = url.match(/^\/teacher\/coding-task-test-cases\/([^/]+)$/))) {
      for (const task of this.tasks) task.testCases = task.testCases.filter((t) => t.id !== match[1])
      return null
    }

    // activities
    if (method === 'GET' && url === '/teacher/courses/course-1/activities') {
      return { content: this.activities.map((activity) => this.serializeActivity(activity)) }
    }
    if (method === 'POST' && url === '/teacher/courses/course-1/activities') {
      const activity = {
        id: this.id('act'),
        courseId: 'course-1',
        position: this.activities.length + 1,
        referenceId: null,
        status: 'DRAFT',
        translations: [],
        ...body,
      }
      this.activities.push(activity)
      return this.serializeActivity(activity)
    }
    if (method === 'PATCH' && (match = url.match(/^\/teacher\/activities\/([^/]+)$/))) {
      return this.serializeActivity(Object.assign(found(this.activities, match[1], 'activity'), body))
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/activities\/([^/]+)\/translations\/([^/]+)$/))) {
      const activity = found(this.activities, match[1], 'activity')
      activity.translations.push({ locale: match[2], ...body })
      return this.serializeActivity(activity)
    }
    if (method === 'PUT' && (match = url.match(/^\/teacher\/activities\/([^/]+)\/status$/))) {
      const activity = found(this.activities, match[1], 'activity')
      activity.status = body.status
      return this.serializeActivity(activity)
    }

    throw new Error(`fake api: unexpected ${method} ${url}`)
  }
}

// ------------------------------------------------------------------ helpers

function run(dir, api, options = {}) {
  const lines = []
  return importCourse(dir, { api, log: (line) => lines.push(line), ...options }).then(() => lines)
}

const byCode = (list, code) => list.find((item) => item.code === code)

// ------------------------------------------------------------------ tests

test('create: quizzes, questions, tasks and activities are created with the ADR-016 settings', async () => {
  const dir = writeCourseFolder()
  try {
    const api = new FakeApi()
    const lines = await run(dir, api)
    const courseCode = path.basename(dir)

    // the TEST quiz of M1: settings of ADR-016, 9 questions, all types mapped
    const test = byCode(api.quizzes, `${courseCode}-M1-T`)
    assert.ok(test, 'TEST quiz created')
    assert.equal(test.maxAttempts, 2)
    assert.equal(test.passingPercent, 60)
    assert.equal(test.revealPolicy, 'AFTER_LAST_ATTEMPT')
    assert.equal(test.questionPoolSize, 5)
    assert.equal(test.shuffleOptions, true)
    assert.equal(test.status, 'PUBLISHED')
    assert.deepEqual(
      test.translations.map((t) => [t.locale, t.title]),
      [
        ['uz', 'Birinchi dars — Test'],
        ['ru', 'Первый урок — Тест'],
        ['en', 'First lesson — Test'],
      ],
    )
    assert.equal(test.questions.length, 9)
    assert.deepEqual(
      test.questions.map((q) => q.questionType),
      ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER', 'NUMERIC', 'SINGLE_CHOICE', 'SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'SHORT_ANSWER'],
    )
    const single = test.questions[0]
    assert.equal(single.difficulty, 'EASY')
    assert.equal(single.options.length, 3)
    assert.deepEqual(single.options.map((o) => o.isCorrect), [false, true, false])
    assert.equal(single.options[0].translations.length, 3)
    assert.equal(single.translations.length, 3)
    assert.equal(single.translations[0].prompt, 'Savol matni')
    const short = test.questions[3]
    assert.deepEqual(short.answerKeys.map((k) => [k.expectedValue, k.matchMode]), [['massiv', 'TRIMMED']])
    const numeric = test.questions[4]
    assert.deepEqual(
      numeric.answerKeys.map((k) => [k.expectedValue, k.matchMode, k.tolerance]),
      [['3.14', 'NUMERIC_TOLERANCE', 0.01]],
    )

    // the TEST activity: QUIZ on the lesson, ungraded, bound to the quiz, published
    const testActivity = api.activities.find((a) => a.referenceId === test.id)
    assert.ok(testActivity, 'TEST activity created')
    assert.equal(testActivity.activityType, 'QUIZ')
    assert.equal(testActivity.lessonId, 'les-1')
    assert.equal(testActivity.isGraded, false)
    assert.equal(testActivity.status, 'PUBLISHED')
    assert.equal(testActivity.pathStep, undefined, 'this API has no path_step — nothing is sent')

    // the PRACTICE of M1 is a coding task: 3 languages, 3 tests, 2 hidden
    const task = byCode(api.tasks, `${courseCode}-M1-P`)
    assert.ok(task, 'coding task created')
    assert.equal(task.status, 'PUBLISHED')
    assert.equal(task.maxPoints, 100)
    assert.equal(task.scoringStrategy, 'ALL_OR_NOTHING')
    assert.deepEqual(task.languages.map((l) => l.code), ['python', 'java', 'cpp'])
    assert.equal(task.languages[0].starterCode, CODE.starter.python)
    assert.equal(task.languages[0].solutionCode, CODE.solution.python)
    assert.equal(task.testCases.length, 3)
    assert.deepEqual(task.testCases.map((t) => t.visibility), ['VISIBLE', 'HIDDEN', 'HIDDEN'])
    const taskActivity = api.activities.find((a) => a.referenceId === task.id)
    assert.equal(taskActivity.activityType, 'CODING_TASK')
    assert.equal(taskActivity.lessonId, 'les-1')

    // the PRACTICE of M2 is a checked-answer quiz: no limit, pass 100, reveal NEVER
    const practice = byCode(api.quizzes, `${courseCode}-M2-P`)
    assert.ok(practice, 'ANSWER practice quiz created')
    assert.equal(practice.maxAttempts, null)
    assert.equal(practice.passingPercent, 100)
    assert.equal(practice.revealPolicy, 'NEVER')
    assert.equal(practice.questions.length, 2)
    const practiceActivity = api.activities.find((a) => a.referenceId === practice.id)
    assert.equal(practiceActivity.lessonId, 'les-2')

    // the midterm: pool 30, 2400 s, graded module-level activity on the ON component
    const midterm = byCode(api.quizzes, `${courseCode}-ON`)
    assert.ok(midterm, 'midterm quiz created')
    assert.equal(midterm.questionPoolSize, 30)
    assert.equal(midterm.timeLimitSeconds, 2400)
    assert.equal(midterm.maxAttempts, 2)
    assert.equal(midterm.questions.length, 60)
    const midtermActivity = api.activities.find((a) => a.referenceId === midterm.id)
    assert.equal(midtermActivity.activityType, 'QUIZ')
    assert.equal(midtermActivity.lessonId ?? null, null)
    assert.equal(midtermActivity.moduleId, 'mod-1')
    assert.equal(midtermActivity.isGraded, true)
    assert.equal(midtermActivity.assessmentComponentId, 'comp-on')

    // the learning path is never touched; the path_step note is logged
    assert.ok(!api.calls.some((call) => call.url.includes('learning-path')), 'no learning-path call')
    assert.ok(lines.some((line) => line.includes('does not expose activities.path_step')))
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('idempotent: a second run creates and deletes nothing', async () => {
  const dir = writeCourseFolder()
  try {
    const api = new FakeApi()
    await run(dir, api)
    const counts = {
      quizzes: api.quizzes.length,
      questions: api.quizzes.flatMap((q) => q.questions).length,
      tasks: api.tasks.length,
      activities: api.activities.length,
    }
    api.calls = []
    await run(dir, api)
    const creates = api.calls.filter((call) => call.method === 'POST' || call.method === 'DELETE')
    assert.deepEqual(creates, [], `second run must not create or delete:\n${creates.map((c) => `${c.method} ${c.url}`).join('\n')}`)
    assert.equal(api.quizzes.length, counts.quizzes)
    assert.equal(api.quizzes.flatMap((q) => q.questions).length, counts.questions)
    assert.equal(api.tasks.length, counts.tasks)
    assert.equal(api.activities.length, counts.activities)
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('dry-run: only GETs, nothing written, the plan is printed', async () => {
  const dir = writeCourseFolder()
  try {
    const api = new FakeApi()
    const lines = await run(dir, api, { dryRun: true })
    const writes = api.calls.filter((call) => call.method !== 'GET')
    assert.deepEqual(writes, [], `dry run must not write:\n${writes.map((c) => `${c.method} ${c.url}`).join('\n')}`)
    assert.equal(api.quizzes.length, 0)
    assert.equal(api.tasks.length, 0)
    assert.equal(api.activities.length, 0)
    assert.ok(api.calls.length > 0, 'dry run still reads the platform')
    assert.ok(lines.some((line) => line.includes('would POST /teacher/courses/course-1/quizzes')))
    assert.ok(lines.some((line) => line.includes('dry run')))
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('path_step is sent only when the API advertises it', async () => {
  const dir = writeCourseFolder()
  try {
    const api = new FakeApi({ pathStep: true, seedActivity: true })
    await run(dir, api, { lesson: 'M1' })
    const creates = api.calls.filter((call) => call.method === 'POST' && call.url === '/teacher/courses/course-1/activities')
    assert.equal(creates.length, 2, 'TEST and PRACTICE activities of M1')
    assert.deepEqual(creates.map((call) => call.body.pathStep), ['TEST', 'PRACTICE'])

    // without a visible pathStep field nothing is sent (see the create test) and --lesson skips the midterm
    assert.ok(!byCode(api.quizzes, `${path.basename(dir)}-ON`), 'no midterm with --lesson')
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})
