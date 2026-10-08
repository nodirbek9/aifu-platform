#!/usr/bin/env node
// tools/content/assess-import.mjs — imports the assessment files of phase 6
// (docs/PLAN.md §4, C0) into the platform through its API:
//
//   node tools/content/assess-import.mjs <COURSE> [--lesson X] [--dry-run]
//
// <COURSE> is a folder name under content/ (DAS1110-S1) or a path to the folder.
// For every <LESSON>/assess.json it upserts, by stable code:
//   <COURSE>-<LESSON>-T  the lesson's TEST quiz (settings per ADR-016: 2 attempts,
//                        pass 60, reveal AFTER_LAST_ATTEMPT, pool of 5), bound to the
//                        lesson as a QUIZ activity (path_step TEST, ungraded);
//   <COURSE>-<LESSON>-P  the PRACTICE: kind CODE becomes a coding task with starter +
//                        reference solution in python/java/cpp and the tests (hidden
//                        included); kind ANSWER becomes a checked-answer quiz (no
//                        attempt limit, pass 100, reveal NEVER) — activity path_step
//                        PRACTICE, ungraded;
// and midterm.json becomes <COURSE>-ON, a quiz (2 attempts, pool 30, 2400 s, pass 60)
// bound module-level as the graded MIDTERM activity of the course's ON component.
// The learning path itself is never enabled here (that is PUT …/learning-path, B10).
//
// Re-running creates nothing new: quizzes, tasks and activities are found by code or
// reference and updated in place. What the platform refuses to change after answers
// exist (e.g. deleting a question) is logged, not fatal. --dry-run performs only GETs
// and prints the plan.
//
// Note: the activity API does not expose path_step yet (CreateActivityRequest has no
// such field and the server rejects unknown ones); the importer sends it only when the
// API advertises it, and otherwise logs that the flag must come from the database.
import fs from 'node:fs'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import { Api, findCourse, lessonsByCode } from './api.mjs'
import { courseDir } from './assess.mjs'
import { CODE_LANGUAGES, validateAssess, validateMidterm } from './assess-schema.mjs'

const LOCALES = ['uz', 'ru', 'en']

/** assess.json type → the platform's questionType (docs/api/openapi.json CreateQuestionRequest). */
const QUESTION_TYPES = {
  SINGLE: 'SINGLE_CHOICE',
  MULTI: 'MULTIPLE_CHOICE',
  TF: 'TRUE_FALSE',
  SHORT: 'SHORT_ANSWER',
  NUM: 'NUMERIC',
}

const TEST_SETTINGS = {
  maxAttempts: 2,
  passingPercent: 60,
  revealPolicy: 'AFTER_LAST_ATTEMPT',
  shuffleOptions: true,
}
const PRACTICE_QUIZ_SETTINGS = { maxAttempts: null, passingPercent: 100, revealPolicy: 'NEVER' }
const MIDTERM_SETTINGS = {
  maxAttempts: 2,
  passingPercent: 60,
  revealPolicy: 'AFTER_LAST_ATTEMPT',
  shuffleOptions: true,
  timeLimitSeconds: 2400,
  questionPoolSize: 30,
}
const TASK_SETTINGS = { maxPoints: 100, scoringStrategy: 'ALL_OR_NOTHING' }

const TEST_SUFFIX = { uz: 'Test', ru: 'Тест', en: 'Test' }
const MIDTERM_TITLE = { uz: 'Oraliq nazorat', ru: 'Промежуточный контроль', en: 'Midterm assessment' }

// ------------------------------------------------------------------ context

/** Shared state of one run: the api, the dry-run flag, the log, and what the API supports. */
function makeContext(api, { dryRun, log }) {
  const ctx = {
    api,
    dryRun,
    log,
    pathStepSupported: false,
    noteActivity(activity) {
      if (activity !== null && typeof activity === 'object' && 'pathStep' in activity) {
        ctx.pathStepSupported = true
      }
    },
    /** A write call; under --dry-run it is only printed. Returns null when dry. */
    async write(method, url, body) {
      if (ctx.dryRun) {
        log(`    would ${method} ${url}`)
        return null
      }
      return api.call(method, url, body)
    },
  }
  return ctx
}

const listAll = async (api, url) =>
  (await api.call('GET', `${url}${url.includes('?') ? '&' : '?'}size=500`)).content ?? []

async function readState(ctx, courseId) {
  const quizzes = await listAll(ctx.api, `/teacher/courses/${courseId}/quizzes`)
  const tasks = await listAll(ctx.api, `/teacher/courses/${courseId}/coding-tasks`)
  const activities = await listAll(ctx.api, `/teacher/courses/${courseId}/activities`)
  for (const activity of activities) ctx.noteActivity(activity)
  return {
    quizzes: new Map(quizzes.map((quiz) => [quiz.code, quiz])),
    tasks: new Map(tasks.map((task) => [task.code, task])),
    activities,
  }
}

// ------------------------------------------------------------------ quizzes

/** Finds or creates the quiz, applies the ADR-016 settings and the three titles. */
async function ensureQuiz(ctx, state, courseId, { code, titles, settings, questionCount }) {
  const existing = state.quizzes.get(code)
  ctx.log(
    existing
      ? `  quiz ${code}: update in place (${questionCount} questions in the file)`
      : `  quiz ${code}: create (${questionCount} questions in the file)`,
  )
  let quiz = existing
  if (!quiz) {
    quiz = await ctx.write('POST', `/teacher/courses/${courseId}/quizzes`, { code })
    if (quiz === null) return null
    state.quizzes.set(code, quiz)
  }
  await ctx.write('PATCH', `/teacher/quizzes/${quiz.id}`, settings)
  for (const locale of LOCALES) {
    await ctx.write('PUT', `/teacher/quizzes/${quiz.id}/translations/${locale}`, { title: titles[locale] })
  }
  return quiz
}

async function publishQuiz(ctx, quiz) {
  if (quiz !== null && quiz.status !== 'PUBLISHED') {
    await ctx.write('PUT', `/teacher/quizzes/${quiz.id}/status`, { status: 'PUBLISHED' })
  }
}

/** A delete the platform may refuse once answers exist: logged, never fatal. */
async function removeOwned(ctx, kind, id, why) {
  try {
    await ctx.write('DELETE', `/teacher/${kind}/${id}`)
    ctx.log(`    ${kind} ${id}: removed (${why})`)
  } catch (error) {
    ctx.log(`    ${kind} ${id} cannot be removed (${why}); the platform refuses it after publish: ${error.message}`)
  }
}

function desiredAnswerKeys(question) {
  if (question.type === 'SHORT') {
    return question.answers.map((answer) => ({ expectedValue: answer, matchMode: 'TRIMMED' }))
  }
  if (question.type === 'NUM') {
    return [
      { expectedValue: String(question.answers[0]), matchMode: 'NUMERIC_TOLERANCE', tolerance: question.tolerance },
    ]
  }
  return null
}

async function syncAnswerKeys(ctx, target, question) {
  const desired = desiredAnswerKeys(question)
  if (desired === null) return
  const existing = target.answerKeys ?? []
  const keyOf = (key) => `${key.matchMode}|${key.expectedValue}|${key.tolerance ?? ''}`
  const same =
    existing.length === desired.length &&
    existing.map(keyOf).sort().join('\n') === desired.map(keyOf).sort().join('\n')
  if (same) return
  for (const old of existing) {
    try {
      await ctx.write('DELETE', `/teacher/answer-keys/${old.id}`)
    } catch (error) {
      ctx.log(`    answer key ${old.id} cannot be replaced; the platform refuses it after publish: ${error.message}`)
      return
    }
  }
  for (const key of desired) await ctx.write('POST', `/teacher/questions/${target.id}/answer-keys`, key)
}

async function syncOptions(ctx, target, question) {
  if (!Array.isArray(question.options)) return
  const correct = new Set(question.correct)
  const existing = (target.options ?? []).slice().sort((a, b) => a.position - b.position)
  for (const [index, text] of question.options.entries()) {
    const isCorrect = correct.has(index)
    let option = existing[index]
    if (!option) {
      option = await ctx.write('POST', `/teacher/questions/${target.id}/options`, { isCorrect })
      if (option === null) continue
    } else if (option.isCorrect !== isCorrect) {
      await ctx.write('PATCH', `/teacher/options/${option.id}`, { isCorrect })
    }
    for (const locale of LOCALES) {
      await ctx.write('PUT', `/teacher/options/${option.id}/translations/${locale}`, { text: text[locale] })
    }
  }
  for (const extra of existing.slice(question.options.length)) {
    await removeOwned(ctx, 'options', extra.id, 'not in the file')
  }
}

/**
 * Makes the quiz's questions equal the file's, position by position. A question whose
 * type changed is replaced (questionType is not updatable); removals the platform
 * refuses are logged and kept. Returns the question ids in file order, for reordering.
 */
async function syncQuestions(ctx, quiz, questions, label) {
  if (quiz === null) {
    ctx.log(`    ${questions.length} question(s) would be created with the quiz`)
    return
  }
  const existing = (await listAll(ctx.api, `/teacher/quizzes/${quiz.id}/questions`))
    .slice()
    .sort((a, b) => a.position - b.position)
  const orderedIds = []
  for (const [index, question] of questions.entries()) {
    const type = QUESTION_TYPES[question.type]
    const current = existing[index]
    let target = current?.questionType === type ? current : null
    if (current && target === null) {
      await removeOwned(ctx, 'questions', current.id, `${label}[${index + 1}]: type ${current.questionType} → ${type}`)
    }
    if (target === null) {
      target = await ctx.write('POST', `/teacher/quizzes/${quiz.id}/questions`, {
        questionType: type,
        points: question.points,
      })
      if (target === null) continue
    }
    await ctx.write('PATCH', `/teacher/questions/${target.id}`, {
      points: question.points,
      difficulty: question.difficulty,
    })
    for (const locale of LOCALES) {
      await ctx.write('PUT', `/teacher/questions/${target.id}/translations/${locale}`, {
        prompt: question.prompt[locale],
        explanation: question.explanation[locale],
      })
    }
    await syncOptions(ctx, target, question)
    await syncAnswerKeys(ctx, target, question)
    orderedIds.push(target.id)
  }
  for (const extra of existing.slice(questions.length)) {
    await removeOwned(ctx, 'questions', extra.id, `${label}: not in the file`)
  }
  const kept = existing.filter((q) => orderedIds.includes(q.id)).map((q) => q.id)
  if (!ctx.dryRun && (kept.length !== orderedIds.length || kept.some((id, i) => orderedIds[i] !== id))) {
    await ctx.write('PUT', `/teacher/quizzes/${quiz.id}/questions/reorder`, { orderedQuestionIds: orderedIds })
  }
}

// ------------------------------------------------------------------ coding tasks

async function ensureCodingTask(ctx, state, courseId, practice, code) {
  const existing = state.tasks.get(code)
  ctx.log(existing ? `  coding task ${code}: update in place` : `  coding task ${code}: create`)
  let task = existing
  if (!task) {
    task = await ctx.write('POST', `/teacher/courses/${courseId}/coding-tasks`, { code })
    if (task === null) return null
    state.tasks.set(code, task)
  }
  await ctx.write('PATCH', `/teacher/coding-tasks/${task.id}`, TASK_SETTINGS)
  for (const locale of LOCALES) {
    await ctx.write('PUT', `/teacher/coding-tasks/${task.id}/translations/${locale}`, {
      title: practice.title[locale],
      description: practice.statement[locale],
    })
  }
  return task
}

async function syncLanguages(ctx, task, code) {
  const existing = (await ctx.api.call('GET', `/teacher/coding-tasks/${task.id}/languages`)) ?? []
  const byCode = new Map(existing.map((language) => [language.code, language]))
  for (const language of CODE_LANGUAGES) {
    const body = { starterCode: code.starter[language], solutionCode: code.solution[language] }
    const current = byCode.get(language)
    if (current) {
      await ctx.write('PATCH', `/teacher/coding-task-languages/${current.taskLanguageId}`, body)
    } else {
      await ctx.write('POST', `/teacher/coding-tasks/${task.id}/languages/${language}`, body)
    }
  }
}

async function syncTestCases(ctx, task, tests) {
  const existing = ((await ctx.api.call('GET', `/teacher/coding-tasks/${task.id}/test-cases`)) ?? [])
    .slice()
    .sort((a, b) => a.position - b.position)
  for (const [index, test] of tests.entries()) {
    const body = {
      stdin: test.input,
      expectedStdout: test.expected,
      visibility: test.hidden ? 'HIDDEN' : 'VISIBLE',
      isExample: !test.hidden,
      weight: 1,
    }
    const current = existing[index]
    if (current) {
      await ctx.write('PATCH', `/teacher/coding-task-test-cases/${current.id}`, body)
    } else {
      const created = await ctx.write('POST', `/teacher/coding-tasks/${task.id}/test-cases`, {
        stdin: test.input,
        expectedStdout: test.expected,
      })
      if (created !== null) await ctx.write('PATCH', `/teacher/coding-task-test-cases/${created.id}`, body)
    }
  }
  for (const extra of existing.slice(tests.length)) {
    await removeOwned(ctx, 'coding-task-test-cases', extra.id, 'not in the file')
  }
}

// ------------------------------------------------------------------ activities

/**
 * Finds the activity bound to the reference (or an unbound one of the same type at the
 * same place, left by an interrupted run) or creates it, then binds the reference,
 * writes the titles and publishes. path_step is sent only when the API exposes it.
 */
async function ensureActivity(ctx, state, courseId, { type, lesson, moduleId, referenceId, pathStep, graded, componentId, titles }) {
  const lessonId = lesson?.id ?? null
  const place = lessonId ?? moduleId
  let activity =
    (referenceId && state.activities.find((a) => a.referenceId === referenceId)) ||
    state.activities.find(
      (a) =>
        a.activityType === type &&
        (a.lessonId ?? null) === lessonId &&
        (lessonId ? true : (a.moduleId ?? null) === place) &&
        !a.referenceId,
    )
  const label = `activity ${pathStep} (${type})`
  if (!activity) {
    const body = {
      activityType: type,
      ...(lessonId ? { lessonId } : { moduleId }),
      isGraded: graded,
      ...(componentId && { assessmentComponentId: componentId }),
    }
    if (ctx.pathStepSupported) body.pathStep = pathStep
    ctx.log(`  ${label}: create and bind to ${referenceId ?? '(new)'}`)
    activity = await ctx.write('POST', `/teacher/courses/${courseId}/activities`, body)
    if (activity === null) return
    ctx.noteActivity(activity)
    state.activities.push(activity)
  } else {
    ctx.log(`  ${label}: update in place`)
  }
  if (referenceId && activity.referenceId !== referenceId) {
    const updated = await ctx.write('PATCH', `/teacher/activities/${activity.id}`, { referenceId })
    if (updated !== null) {
      const index = state.activities.findIndex((entry) => entry.id === activity.id)
      if (index >= 0) state.activities[index] = updated
      activity = updated
    }
  }
  for (const locale of LOCALES) {
    await ctx.write('PUT', `/teacher/activities/${activity.id}/translations/${locale}`, { title: titles[locale] })
  }
  if (activity.status !== 'PUBLISHED') {
    await ctx.write('PUT', `/teacher/activities/${activity.id}/status`, { status: 'PUBLISHED' })
  }
}

// ------------------------------------------------------------------ lessons + midterm

async function lessonTitles(ctx, lesson, fallback) {
  const detail = await ctx.api.call('GET', `/teacher/lessons/${lesson.id}`)
  const titles = {}
  for (const locale of LOCALES) {
    titles[locale] =
      (detail.translations ?? []).find((t) => t.locale === locale)?.title ?? lesson.title ?? fallback
  }
  return titles
}

async function importLesson(ctx, state, courseId, courseCode, lessonCode, data, lessons) {
  const lesson = lessons.get(lessonCode)
  if (!lesson) throw new Error(`${courseCode}: no lesson with the code ${lessonCode}`)
  const base = `${courseCode}-${lessonCode}`
  const titles = await lessonTitles(ctx, lesson, `${courseCode} ${lessonCode}`)

  const testTitles = {}
  for (const locale of LOCALES) testTitles[locale] = `${titles[locale]} — ${TEST_SUFFIX[locale]}`
  const testQuiz = await ensureQuiz(ctx, state, courseId, {
    code: `${base}-T`,
    titles: testTitles,
    settings: { ...TEST_SETTINGS, ...(data.test.questions.length >= 5 && { questionPoolSize: 5 }) },
    questionCount: data.test.questions.length,
  })
  await syncQuestions(ctx, testQuiz, data.test.questions, `${base}-T`)
  await publishQuiz(ctx, testQuiz)
  await ensureActivity(ctx, state, courseId, {
    type: 'QUIZ',
    lesson,
    referenceId: testQuiz?.id ?? null,
    pathStep: 'TEST',
    graded: false,
    titles: testTitles,
  })

  const practice = data.practice
  if (practice.kind === 'CODE') {
    const task = await ensureCodingTask(ctx, state, courseId, practice, `${base}-P`)
    if (task === null) {
      ctx.log(
        `    ${CODE_LANGUAGES.length} languages and ${practice.code.tests.length} test(s) would be created with the task`,
      )
    } else {
      await syncLanguages(ctx, task, practice.code)
      await syncTestCases(ctx, task, practice.code.tests)
      if (task.status !== 'PUBLISHED') {
        await ctx.write('PUT', `/teacher/coding-tasks/${task.id}/status`, { status: 'PUBLISHED' })
      }
    }
    await ensureActivity(ctx, state, courseId, {
      type: 'CODING_TASK',
      lesson,
      referenceId: task?.id ?? null,
      pathStep: 'PRACTICE',
      graded: false,
      titles: practice.title,
    })
  } else {
    const practiceQuiz = await ensureQuiz(ctx, state, courseId, {
      code: `${base}-P`,
      titles: practice.title,
      settings: PRACTICE_QUIZ_SETTINGS,
      questionCount: practice.answer.questions.length,
    })
    await syncQuestions(ctx, practiceQuiz, practice.answer.questions, `${base}-P`)
    await publishQuiz(ctx, practiceQuiz)
    await ensureActivity(ctx, state, courseId, {
      type: 'QUIZ',
      lesson,
      referenceId: practiceQuiz?.id ?? null,
      pathStep: 'PRACTICE',
      graded: false,
      titles: practice.title,
    })
  }
}

async function importMidterm(ctx, state, courseId, courseCode, data, lessons) {
  const anchor = lessons.get(data.afterLesson)
  if (!anchor) throw new Error(`${courseCode}: midterm afterLesson ${data.afterLesson}: no lesson with that code`)
  const course = await ctx.api.call('GET', `/admin/courses/${courseId}`)
  const component = (course.assessmentComponents ?? []).find((c) => c.kind === 'MIDTERM')
  if (!component) throw new Error(`${courseCode}: the course has no MIDTERM (ON) assessment component`)
  const code = `${courseCode}-ON`
  const quiz = await ensureQuiz(ctx, state, courseId, {
    code,
    titles: MIDTERM_TITLE,
    settings: MIDTERM_SETTINGS,
    questionCount: data.questions.length,
  })
  await syncQuestions(ctx, quiz, data.questions, code)
  await publishQuiz(ctx, quiz)
  await ensureActivity(ctx, state, courseId, {
    type: 'QUIZ',
    moduleId: anchor.moduleId,
    referenceId: quiz?.id ?? null,
    pathStep: 'MIDTERM',
    graded: true,
    componentId: component.id,
    titles: MIDTERM_TITLE,
  })
  ctx.log(`  midterm ${code}: bound after lesson ${data.afterLesson}; the learning path is NOT enabled by this import`)
}

// ------------------------------------------------------------------ the course

function readJson(file, validate) {
  const data = JSON.parse(fs.readFileSync(file, 'utf8'))
  const problems = validate(data)
  if (problems.length > 0) throw new Error(`${file}:\n  - ${problems.join('\n  - ')}`)
  return data
}

/**
 * Imports one course folder. `course` is a folder name under content/ or a path;
 * `api` is anything with call(method, url, body) (api.mjs Api, or a fake in tests);
 * `log` defaults to console.log. With dryRun only GETs are made and the plan is printed.
 */
export async function importCourse(course, { lesson = null, dryRun = false, api, log = console.log } = {}) {
  if (!api) throw new Error('importCourse needs an api (see tools/content/api.mjs)')
  const dir = courseDir(course)
  const courseCode = path.basename(dir)
  const spec = JSON.parse(fs.readFileSync(path.join(dir, 'course.json'), 'utf8'))

  const lessonNames = fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && !entry.name.startsWith('.'))
    .map((entry) => entry.name)
    .sort((a, b) => a.localeCompare(b, 'en', { numeric: true }))
  const targets = lesson === null ? lessonNames : [lesson]
  const assessments = new Map()
  let skipped = 0
  for (const name of targets) {
    const file = path.join(dir, name, 'assess.json')
    if (!fs.existsSync(file)) {
      if (lesson !== null) throw new Error(`${file} not found`)
      skipped += 1
      continue
    }
    assessments.set(name, readJson(file, validateAssess))
  }
  const midtermFile = path.join(dir, 'midterm.json')
  const midterm = lesson === null && fs.existsSync(midtermFile) ? readJson(midtermFile, validateMidterm) : null
  log(
    `${courseCode}: ${assessments.size} assess.json file(s)` +
      (skipped > 0 ? `, ${skipped} lesson(s) without one (skipped)` : '') +
      (midterm ? ', midterm.json' : ''),
  )

  const ctx = makeContext(api, { dryRun, log })
  const courseId = await findCourse(api, spec)
  const lessons = await lessonsByCode(api, courseId)
  const state = await readState(ctx, courseId)

  for (const [lessonCode, data] of assessments) {
    log(`${lessonCode}:`)
    await importLesson(ctx, state, courseId, courseCode, lessonCode, data, lessons)
  }
  if (midterm) {
    log('midterm:')
    await importMidterm(ctx, state, courseId, courseCode, midterm, lessons)
  }
  if (!ctx.pathStepSupported) {
    log(
      'note: this API does not expose activities.path_step; the TEST/PRACTICE/MIDTERM flags ' +
        'come from the database (V032/V914 or a later migration)',
    )
  }
  log(dryRun ? `${courseCode}: dry run — nothing was written` : `${courseCode}: import finished`)
}

// ------------------------------------------------------------------ main

async function main() {
  const [spec, ...rest] = process.argv.slice(2)
  const lessonFlag = rest.indexOf('--lesson')
  const lesson = lessonFlag === -1 ? null : rest[lessonFlag + 1]
  const dryRun = rest.includes('--dry-run')
  if (spec === undefined || (lessonFlag !== -1 && lesson === undefined)) {
    console.error('Usage: node tools/content/assess-import.mjs <COURSE> [--lesson X] [--dry-run]')
    process.exit(2)
  }
  // dry-run still reads the platform (GETs only), so it needs credentials too
  if (!process.env.AIFU_PASSWORD) {
    console.error('AIFU_PASSWORD is not set')
    process.exit(2)
  }
  const api = new Api(process.env.AIFU_API ?? 'http://localhost:8080', process.env.AIFU_USER ?? 'admin', process.env.AIFU_PASSWORD)
  try {
    await importCourse(spec, { lesson, dryRun, api })
  } catch (error) {
    console.error(error.message)
    process.exit(1)
  }
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  main()
}
