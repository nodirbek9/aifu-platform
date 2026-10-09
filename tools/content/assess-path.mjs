// Configures (and optionally enables) the learning path of one course from its folder:
// the midterm quiz imported by assess-import.mjs and the anchor lesson of midterm.json.
//
//   node assess-path.mjs <COURSE> [--enable | --disable] [--dry-run]
//
// Without --enable/--disable the current on/off state is kept (a new course starts off).
// Enabling locks lessons for students (docs/decisions/ADR-018): the import never does it alone.
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { Api, findCourse, lessonsByCode } from './api.mjs'
import { courseDir } from './assess.mjs'

const listAll = async (api, url) => {
  const all = []
  for (let page = 0; ; page++) {
    const result = await api.call('GET', `${url}${url.includes('?') ? '&' : '?'}size=100&page=${page}`)
    all.push(...(result.content ?? []))
    if (!result.hasNext && !(result.totalPages > page + 1)) return all
  }
}

/** Builds the request body for PUT /admin/courses/{id}/learning-path. Pure, so it is testable. */
export function buildPathRequest({ enabled, midtermActivityId, midtermAfterLessonId }) {
  return {
    enabled,
    ...(midtermActivityId && midtermAfterLessonId && { midtermActivityId, midtermAfterLessonId }),
  }
}

export async function configurePath(course, { enable = null, dryRun = false, api, log = console.log } = {}) {
  const dir = courseDir(course)
  const spec = JSON.parse(fs.readFileSync(path.join(dir, 'course.json'), 'utf8'))
  const courseId = await findCourse(api, spec)
  const current = await api.call('GET', `/admin/courses/${courseId}/learning-path`)
  const enabled = enable === null ? current?.enabled === true : enable

  let midtermActivityId
  let midtermAfterLessonId
  const midtermFile = path.join(dir, 'midterm.json')
  if (fs.existsSync(midtermFile)) {
    const midterm = JSON.parse(fs.readFileSync(midtermFile, 'utf8'))
    const lessons = await lessonsByCode(api, courseId)
    const anchor = lessons.get(midterm.afterLesson)
    if (!anchor) throw new Error(`${course}: midterm afterLesson ${midterm.afterLesson} is not a lesson of the course`)
    const activities = await listAll(api, `/teacher/courses/${courseId}/activities`)
    const activity = activities.find((a) => a.pathStep === 'MIDTERM' && a.status !== 'ARCHIVED')
    if (!activity) throw new Error(`${course}: no MIDTERM activity yet — run assess-import.mjs first`)
    midtermActivityId = activity.id
    midtermAfterLessonId = anchor.id
  } else {
    log(`${course}: no midterm.json — the path is configured without a midterm`)
  }

  const body = buildPathRequest({ enabled, midtermActivityId, midtermAfterLessonId })
  if (dryRun) {
    log(`${course}: would PUT learning-path ${JSON.stringify(body)}`)
    return body
  }
  await api.call('PUT', `/admin/courses/${courseId}/learning-path`, body)
  log(`${course}: learning path saved, ${enabled ? 'ENABLED' : 'disabled'}${midtermActivityId ? `, midterm after ${path.basename(midtermFile)} anchor` : ''}`)
  return body
}

async function main() {
  const [course, ...rest] = process.argv.slice(2)
  if (course === undefined || (rest.includes('--enable') && rest.includes('--disable'))) {
    console.error('Usage: node tools/content/assess-path.mjs <COURSE> [--enable | --disable] [--dry-run]')
    process.exit(2)
  }
  if (!process.env.AIFU_PASSWORD) {
    console.error('AIFU_PASSWORD is not set')
    process.exit(2)
  }
  const api = new Api(process.env.AIFU_API ?? 'http://localhost:8080', process.env.AIFU_USER ?? 'admin', process.env.AIFU_PASSWORD)
  const enable = rest.includes('--enable') ? true : rest.includes('--disable') ? false : null
  try {
    await configurePath(course, { enable, dryRun: rest.includes('--dry-run'), api })
  } catch (error) {
    console.error(error.message)
    process.exit(1)
  }
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  await main()
}
