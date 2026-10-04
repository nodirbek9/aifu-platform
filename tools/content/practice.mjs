// The texts of a course's practice in three languages: module titles, coding tasks and their
// hints, quizzes with their questions and options, and course activities (docs/06-CONTENT-GUIDE.md).
//
//   node practice.mjs export <course folder>     writes practice.uz.json from the platform
//   node practice.mjs import <course folder> [--dry-run]
//
// The files live next to course.json: practice.uz.json, practice.ru.json, practice.en.json. They
// are keyed by stable codes — module, task and quiz codes, question and option positions — so they
// import into any database that holds the course. The structure of practice.ru.json and
// practice.en.json must match practice.uz.json (the same keys, the same number of questions,
// options and hints): a translation can never silently drop a question.
import fs from 'node:fs'
import path from 'node:path'
import { apiFromEnvironment, findCourse, lessonsByCode } from './api.mjs'

const LOCALES = ['uz', 'ru', 'en']
/** Items the end-to-end tests create in a development database: not course content. */
const isTestArtifact = (code) => /(^|-)E2E-/.test(code ?? '')

/** The fields of an object that have a value: what a translation file keeps. */
function present(object, fields) {
  const out = {}
  for (const field of fields) {
    if (object?.[field] !== null && object?.[field] !== undefined && object[field] !== '') out[field] = object[field]
  }
  return out
}

function inLocale(translations, locale) {
  return (translations ?? []).find((t) => t.locale === locale)
}

async function readCourse(api, courseId) {
  const all = async (url) => (await api.call('GET', `${url}${url.includes('?') ? '&' : '?'}size=500`)).content ?? []
  const modules = await all(`/teacher/courses/${courseId}/modules`)
  const tasks = (await all(`/teacher/courses/${courseId}/coding-tasks`)).filter((task) => !isTestArtifact(task.code))
  const quizzes = (await all(`/teacher/courses/${courseId}/quizzes`)).filter((quiz) => !isTestArtifact(quiz.code))
  for (const quiz of quizzes) {
    quiz.questions = (await all(`/teacher/quizzes/${quiz.id}/questions`)).sort((a, b) => a.position - b.position)
  }
  const activities = await all(`/teacher/courses/${courseId}/activities`)
  const lessons = await lessonsByCode(api, courseId)
  const lessonCode = new Map([...lessons.values()].map((lesson) => [lesson.id, lesson.code]))
  const referenceCode = new Map([
    ...tasks.map((task) => [task.id, `CODING_TASK:${task.code}`]),
    ...quizzes.map((quiz) => [quiz.id, `QUIZ:${quiz.code}`]),
  ])
  for (const activity of activities) {
    activity.key = referenceCode.get(activity.referenceId) ?? `${lessonCode.get(activity.lessonId) ?? '-'}#${activity.position}`
  }
  // an activity of a test artifact points at a task or quiz filtered out above
  const known = activities.filter((activity) => !activity.referenceId || referenceCode.has(activity.referenceId))
  return { modules, tasks, quizzes, activities: known }
}

function exportLocale(course, locale) {
  const file = { modules: {}, codingTasks: {}, quizzes: {}, activities: {} }
  for (const module of course.modules) {
    const t = inLocale(module.translations, locale)
    if (t) file.modules[module.code] = present(t, ['title', 'description'])
  }
  for (const task of course.tasks) {
    const t = inLocale(task.translations, locale)
    if (!t) continue
    file.codingTasks[task.code] = {
      ...present(t, ['title', 'description', 'instructions', 'constraintsText', 'explanation']),
      hints: (task.hints ?? [])
        .slice()
        .sort((a, b) => a.position - b.position)
        .map((hint) => inLocale(hint.translations, locale)?.body ?? ''),
    }
  }
  for (const quiz of course.quizzes) {
    const t = inLocale(quiz.translations, locale)
    if (!t) continue
    file.quizzes[quiz.code] = {
      ...present(t, ['title', 'description', 'instructions']),
      questions: quiz.questions.map((question) => ({
        ...present(inLocale(question.translations, locale), ['prompt', 'hint', 'explanation']),
        options: (question.options ?? [])
          .slice()
          .sort((a, b) => a.position - b.position)
          .map((option) => present(inLocale(option.translations, locale), ['text', 'feedback'])),
      })),
    }
  }
  for (const activity of course.activities) {
    const t = inLocale(activity.translations, locale)
    if (t) file.activities[activity.key] = present(t, ['title', 'instructions'])
  }
  return file
}

/** The shape a translation must keep: keys, and the counts of questions, options and hints. */
function shape(file) {
  return JSON.stringify({
    modules: Object.keys(file.modules ?? {}).sort(),
    codingTasks: Object.entries(file.codingTasks ?? {})
      .map(([code, task]) => `${code}:${task.hints?.length ?? 0}`)
      .sort(),
    quizzes: Object.entries(file.quizzes ?? {})
      .map(([code, quiz]) => `${code}:${quiz.questions.map((q) => q.options.length).join(',')}`)
      .sort(),
    activities: Object.keys(file.activities ?? {}).sort(),
  })
}

async function importLocale(api, course, locale, file, dryRun) {
  let calls = 0
  const put = async (url, body) => {
    calls += 1
    if (!dryRun) await api.call('PUT', url, body)
  }
  const byCode = (items) => new Map(items.map((item) => [item.code, item]))
  const modules = byCode(course.modules)
  const tasks = byCode(course.tasks)
  const quizzes = byCode(course.quizzes)
  const activities = new Map(course.activities.map((activity) => [activity.key, activity]))
  const need = (map, key, what) => {
    const found = map.get(key)
    if (!found) throw new Error(`practice.${locale}.json: the course has no ${what} ${key}`)
    return found
  }

  for (const [code, text] of Object.entries(file.modules ?? {})) {
    await put(`/teacher/modules/${need(modules, code, 'module').id}/translations/${locale}`, text)
  }
  for (const [code, text] of Object.entries(file.codingTasks ?? {})) {
    const task = need(tasks, code, 'coding task')
    const { hints = [], ...fields } = text
    await put(`/teacher/coding-tasks/${task.id}/translations/${locale}`, fields)
    const ordered = (task.hints ?? []).slice().sort((a, b) => a.position - b.position)
    if (hints.length !== ordered.length) throw new Error(`${code}: ${hints.length} hints, the task has ${ordered.length}`)
    for (const [index, body] of hints.entries()) {
      if (body) await put(`/teacher/coding-task-hints/${ordered[index].id}/translations/${locale}`, { body })
    }
  }
  for (const [code, text] of Object.entries(file.quizzes ?? {})) {
    const quiz = need(quizzes, code, 'quiz')
    const { questions, ...fields } = text
    await put(`/teacher/quizzes/${quiz.id}/translations/${locale}`, fields)
    if (questions.length !== quiz.questions.length) {
      throw new Error(`${code}: ${questions.length} questions, the quiz has ${quiz.questions.length}`)
    }
    for (const [qi, question] of questions.entries()) {
      const target = quiz.questions[qi]
      const { options, ...questionFields } = question
      if (questionFields.prompt) await put(`/teacher/questions/${target.id}/translations/${locale}`, questionFields)
      const ordered = (target.options ?? []).slice().sort((a, b) => a.position - b.position)
      if (options.length !== ordered.length) {
        throw new Error(`${code} question ${qi + 1}: ${options.length} options, the question has ${ordered.length}`)
      }
      for (const [oi, option] of options.entries()) {
        if (option.text) await put(`/teacher/options/${ordered[oi].id}/translations/${locale}`, option)
      }
    }
  }
  for (const [key, text] of Object.entries(file.activities ?? {})) {
    await put(`/teacher/activities/${need(activities, key, 'activity').id}/translations/${locale}`, text)
  }
  return calls
}

async function main() {
  const [command, folder] = process.argv.slice(2)
  const dryRun = process.argv.includes('--dry-run')
  if (!['export', 'import'].includes(command) || !folder) {
    console.error('usage: node practice.mjs export|import <course folder> [--dry-run]')
    process.exit(2)
  }
  const spec = JSON.parse(fs.readFileSync(path.join(folder, 'course.json'), 'utf8'))
  const api = apiFromEnvironment()
  const course = await readCourse(api, await findCourse(api, spec))
  const fileOf = (locale) => path.join(folder, `practice.${locale}.json`)

  if (command === 'export') {
    const file = exportLocale(course, 'uz')
    fs.writeFileSync(fileOf('uz'), JSON.stringify(file, null, 2) + '\n')
    console.log(
      `${path.basename(folder)}: ${Object.keys(file.modules).length} modules, ${Object.keys(file.codingTasks).length} tasks, ` +
        `${Object.keys(file.quizzes).length} quizzes, ${Object.keys(file.activities).length} activities → practice.uz.json`,
    )
    return
  }

  const files = {}
  for (const locale of LOCALES) {
    if (fs.existsSync(fileOf(locale))) files[locale] = JSON.parse(fs.readFileSync(fileOf(locale), 'utf8'))
  }
  if (!files.uz) throw new Error(`${fileOf('uz')} is missing`)
  for (const locale of ['ru', 'en']) {
    if (files[locale] && shape(files[locale]) !== shape(files.uz)) {
      throw new Error(`practice.${locale}.json does not have the shape of practice.uz.json`)
    }
  }
  for (const [locale, file] of Object.entries(files)) {
    const calls = await importLocale(api, course, locale, file, dryRun)
    console.log(`${path.basename(folder)} ${locale}: ${calls} translations ${dryRun ? 'checked' : 'written'}`)
  }
}

main().catch((error) => {
  console.error(error.message)
  process.exit(1)
})
