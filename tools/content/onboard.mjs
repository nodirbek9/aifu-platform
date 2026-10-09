// One command that takes a course from its content folder to a working course in the platform:
//
//   node tools/content/onboard.mjs <COURSE> [--author] [--enable] [--replace] [--no-verify] [--dry-run]
//
//   1. scaffold   subject, course, scheme, modules and lessons from course.json   (scaffold.mjs)
//   2. lessons    the three-language lesson texts                                  (import.mjs)
//   3. author     test banks, practice and midterm written by an AI worker, only with --author
//   4. check      the offline rules for every assess.json and midterm.json         (assess.mjs)
//   5. verify     every CODE practice in the sandbox images (Docker)               (assess-verify.mjs)
//   6. import     quizzes, coding tasks, activities, the midterm                    (assess-import.mjs)
//   7. path       midterm + anchor saved; --enable switches the gate on            (assess-path.mjs)
//
// Every step is idempotent, so re-running after a fix continues where it stopped. Environment:
// AIFU_API, AIFU_USER, AIFU_PASSWORD (docs/06-CONTENT-GUIDE.md). Nothing here is specific to one
// course: a new subject is a new content/<COURSE>/ folder, never a code change.
import { spawnSync } from 'node:child_process'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { courseDir } from './assess.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))

/** The steps for the given flags, as {name, script, args}. Pure, so the plan is testable. */
export function planSteps(course, dir, { author = false, enable = false, replace = false, verify = true, dryRun = false } = {}) {
  const has = (script) => fs.existsSync(path.join(HERE, script))
  const dry = dryRun ? ['--dry-run'] : []
  const steps = []
  if (has('scaffold.mjs')) steps.push({ name: 'scaffold', script: 'scaffold.mjs', args: [dir, ...dry] })
  steps.push({ name: 'lessons', script: 'import.mjs', args: [dir, ...dry] })
  if (author && !dryRun) {
    steps.push({ name: 'author lessons', script: 'assess-author.mjs', args: [course, ...(verify ? [] : ['--no-verify'])] })
    steps.push({ name: 'author midterm', script: 'assess-author.mjs', args: [course, '--midterm'] })
  }
  steps.push({ name: 'check', script: 'assess.mjs', args: ['check', course] })
  if (verify && !dryRun) steps.push({ name: 'verify', script: 'assess-verify.mjs', args: [course] })
  steps.push({ name: 'import', script: 'assess-import.mjs', args: [course, ...dry, ...(replace ? ['--replace'] : [])] })
  steps.push({ name: 'path', script: 'assess-path.mjs', args: [course, ...dry, ...(enable ? ['--enable'] : [])] })
  return steps
}

function run(step) {
  console.log(`\n=== ${step.name} ===`)
  const result = spawnSync('node', [path.join(HERE, step.script), ...step.args], { stdio: 'inherit', env: process.env })
  return result.status === 0
}

function main() {
  const [course, ...rest] = process.argv.slice(2)
  if (course === undefined) {
    console.error('Usage: node tools/content/onboard.mjs <COURSE> [--author] [--enable] [--replace] [--no-verify] [--dry-run]')
    process.exit(2)
  }
  if (!process.env.AIFU_PASSWORD) {
    console.error('AIFU_PASSWORD is not set')
    process.exit(2)
  }
  const dir = courseDir(course)
  if (!fs.existsSync(path.join(dir, 'course.json'))) {
    console.error(`${dir}/course.json is missing: it names the subject and the term (docs/06-CONTENT-GUIDE.md §1a)`)
    process.exit(2)
  }
  const steps = planSteps(path.basename(dir), dir, {
    author: rest.includes('--author'),
    enable: rest.includes('--enable'),
    replace: rest.includes('--replace'),
    verify: !rest.includes('--no-verify'),
    dryRun: rest.includes('--dry-run'),
  })
  for (const step of steps) {
    if (!run(step)) {
      console.error(`\nStopped at "${step.name}". Fix it and run the same command again: finished steps are idempotent.`)
      process.exit(1)
    }
  }
  console.log(`\n${path.basename(dir)}: done${rest.includes('--dry-run') ? ' (dry run, nothing written)' : ''}.`)
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  main()
}
