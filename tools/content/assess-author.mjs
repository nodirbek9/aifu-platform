// Has an AI worker write the test banks, practice tasks and the midterm of a course, then checks
// and verifies what it wrote (one fix round per lesson). It only writes content/<COURSE>/**; the
// import is a separate step (onboard.mjs runs both).
//
//   node assess-author.mjs <COURSE> [--batch 1] [--conc 3] [--lesson M1,M2] [--no-verify]
//   node assess-author.mjs <COURSE> --midterm
//
// The worker is the model router of the owner's machine (AI_ROUTER, default D:/AI-Stack/router/ai.mjs),
// called with a role and no pinned model so that the router falls through exhausted providers.
// Any other agent that can write files can do the same job: the prompts below are the contract.
import { spawn, spawnSync } from 'node:child_process'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { courseDir } from './assess.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const REPO = path.resolve(HERE, '../..')
const ROUTER = process.env.AI_ROUTER ?? 'D:/AI-Stack/router/ai.mjs'
const WORKER_TIMEOUT_MS = 40 * 60_000

const AUTHORIZATION =
  'AUTHORIZATION: the repository owner asks for this content task; writing files under content/ is allowed and required (any frontend-only rule in AGENTS.md does not apply). '

const FORMAT = `Read first: tools/content/assess-schema.mjs (the exact format and every rule the checker enforces) and, per lesson, content/<COURSE>/<L>/uz.md, ru.md, en.md and examples.json. If the course has an assess.json already, copy its shape. Rules: questions only on what the lesson text says; the test has 9 questions mixing SINGLE/MULTI/TF/NUM/SHORT with >=2 EASY, >=2 MEDIUM and >=2 HARD; every text in uz, ru and en (uz in Uzbek Latin with U+02BB after o/g and U+02BC elsewhere, never an ASCII apostrophe); explanations short; a NUM tolerance below 1000000; accept every language's term in SHORT answers. Practice: if the lesson has runnable code examples (examples.json with runs) and a small program task fits, use kind CODE with starter and reference solution in python, java AND cpp (the starter has only code, no natural-language comments and must fail at least one test), >=3 tests with >=2 hidden; the solutions read stdin and print stdout exactly matching expected; state input ranges that fit a 32-bit int; the task must NOT be the lesson's own example. Otherwise use kind ANSWER with 3-5 checked questions (practice.answer.questions, same shape as test questions). You cannot run code: after writing, re-read each file against assess-schema.mjs and fix what it would reject. Write the files now with your file-writing tool.`

const batchPrompt = (course, lessons) =>
  `${AUTHORIZATION}Content for course ${course}. Write content/${course}/<L>/assess.json for lessons ${lessons.join(', ')} (one file each). ${FORMAT}`

const midtermPrompt = (course) =>
  `${AUTHORIZATION}Write content/${course}/midterm.json. Format: { afterLesson: <lesson code>, questions: [ 60 questions in the shape of the test questions of an assess.json, each with an extra field lesson: <lesson code> ] }. Read tools/content/assess-schema.mjs (validateMidterm and the question rules). Choose afterLesson = the last lesson of the first 7 weeks of the course (roughly the first 7 lectures and their practicals; read content/${course}/course.json and the lesson list) and draw the questions ONLY from lessons up to and including it, spread over them (about 20 EASY, 25 MEDIUM, 15 HARD). Same language and apostrophe rules as the lesson tests; no code tasks. Write the file now.`

const fixPrompt = (course, lesson, problems) =>
  `${AUTHORIZATION}Fix content/${course}/${lesson}/assess.json. It failed the tools:\n${problems.slice(0, 3000)}\nRead tools/content/assess-schema.mjs for the rules. Correct ONLY what the messages point at; for solution/test mismatches make the solutions and expected outputs agree in all three languages. Write the file.`

function worker(prompt, label, log) {
  return new Promise((resolve) => {
    const args = [ROUTER, 'run', '--role', 'docs', '--effort', 'high', '--cwd', REPO, '--prompt', prompt]
    if (process.env.MODEL) args.splice(2, 0, '--model', process.env.MODEL)
    const child = spawn('node', args, { stdio: ['ignore', 'pipe', 'pipe'] })
    let out = ''
    child.stdout.on('data', (d) => (out += d))
    child.stderr.on('data', (d) => (out += d))
    const timer = setTimeout(() => child.kill(), WORKER_TIMEOUT_MS)
    child.on('close', () => {
      clearTimeout(timer)
      log(`worker ${label}: ${out.split('\n').find((l) => l.includes('[router] role=')) ?? 'no result line (failed or timed out)'}`)
      resolve(out)
    })
  })
}

const tool = (name, args) =>
  spawnSync('node', [path.join(HERE, name), ...args], { cwd: REPO, encoding: 'utf8', timeout: 900_000 })

function lessonFolders(dir) {
  return fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((e) => e.isDirectory() && fs.existsSync(path.join(dir, e.name, 'uz.md')))
    .map((e) => e.name)
    .sort((a, b) => a.localeCompare(b, undefined, { numeric: true }))
}

/** null when check (and verify) pass, otherwise the tools' messages. */
function problemsOf(course, lesson, { verify }) {
  if (!fs.existsSync(path.join(courseDir(course), lesson, 'assess.json'))) return 'assess.json was not written'
  const checked = tool('assess.mjs', ['check', course, '--lesson', lesson])
  if (checked.status !== 0) return `check:\n${checked.stdout}${checked.stderr}`
  if (!verify) return null
  const verified = tool('assess-verify.mjs', [course, '--lesson', lesson])
  return verified.status === 0 ? null : `verify:\n${verified.stdout}${verified.stderr}`
}

async function pool(tasks, size) {
  const queue = [...tasks]
  await Promise.all(
    Array.from({ length: size }, async () => {
      while (queue.length) await queue.shift()()
    }),
  )
}

/** Writes what is missing; returns the lessons that still fail after one fix round. */
export async function authorCourse(course, { batch = 1, conc = 3, only = null, verify = true, midterm = false, log = console.log } = {}) {
  const dir = courseDir(course)
  if (midterm) {
    if (!fs.existsSync(path.join(dir, 'midterm.json'))) await worker(midtermPrompt(course), `${course} midterm`, log)
    const checked = tool('assess.mjs', ['check', course])
    const ok = fs.existsSync(path.join(dir, 'midterm.json')) && /midterm\.json: OK/.test(checked.stdout)
    log(ok ? `${course}: midterm.json written and checked` : `${course}: midterm.json missing or failing\n${checked.stdout}`)
    return ok ? [] : ['midterm']
  }

  const lessons = (only ?? lessonFolders(dir)).filter((l) => problemsOf(course, l, { verify: false }) !== null)
  log(`${course}: ${lessons.length} lesson(s) without a valid assess.json`)
  const batches = []
  for (let i = 0; i < lessons.length; i += batch) batches.push(lessons.slice(i, i + batch))
  const failed = []
  await pool(
    batches.map((group) => async () => {
      await worker(batchPrompt(course, group), `${course} ${group.join(',')}`, log)
      for (const lesson of group) {
        let problem = problemsOf(course, lesson, { verify })
        if (problem) {
          log(`${course}/${lesson}: ${problem.split('\n').slice(0, 3).join(' | ')} -> fix round`)
          await worker(fixPrompt(course, lesson, problem), `${course}/${lesson} fix`, log)
          problem = problemsOf(course, lesson, { verify })
        }
        if (problem) failed.push(lesson)
        else log(`${course}/${lesson}: ok`)
      }
    }),
    conc,
  )
  return failed
}

async function main() {
  const [course, ...rest] = process.argv.slice(2)
  const flag = (name, fallback) => (rest.includes(name) ? rest[rest.indexOf(name) + 1] : fallback)
  if (course === undefined) {
    console.error('Usage: node tools/content/assess-author.mjs <COURSE> [--batch N] [--conc N] [--lesson A,B] [--no-verify] [--midterm]')
    process.exit(2)
  }
  const failed = await authorCourse(course, {
    batch: Number(flag('--batch', 1)),
    conc: Number(flag('--conc', 3)),
    only: flag('--lesson', null)?.split(',') ?? null,
    verify: !rest.includes('--no-verify'),
    midterm: rest.includes('--midterm'),
  })
  console.log(failed.length ? `STILL FAILING: ${failed.join(', ')}` : 'all written and checked')
  process.exit(failed.length ? 1 : 0)
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  await main()
}
