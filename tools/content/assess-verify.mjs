#!/usr/bin/env node
// tools/content/assess-verify.mjs — runs the solutions and starters of every CODE
// practice of a course in the Docker sandbox images (docs/PLAN.md §4, C0).
//
//   node tools/content/assess-verify.mjs <COURSE> [--lesson X] [--dry]
//
// <COURSE> is a folder name under content/ (DAS1110-S1) or a path to the folder.
// For every lesson whose assess.json practice has kind CODE: each solution
// (python, java, cpp) must pass every test (outputs compared trimmed, like the
// platform), and each starter must compile and run without crashing yet fail at
// least one test. Lessons without assess.json and ANSWER practices are skipped.
// Exit code 1 when any problem is found. --dry validates the arguments and lists
// what would run without touching Docker.
import { execFileSync } from 'node:child_process'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import { courseDir } from './assess.mjs'
import { CODE_LANGUAGES } from './assess-schema.mjs'

// The same images and commands the sandbox (and run-examples.mjs) uses.
const IMAGES = {
  python: { image: 'python:3.11-slim', file: 'main.py', cmd: 'python3 main.py' },
  java: { image: 'eclipse-temurin:21-jdk', file: 'Main.java', cmd: 'javac Main.java && java Main' },
  cpp: { image: 'gcc:13', file: 'main.cpp', cmd: 'g++ -O2 -std=c++17 -o main main.cpp && ./main' },
}
const RUN_TIMEOUT_MS = 60_000

/**
 * Runs one program against one test input in the sandbox image of its language.
 * Never throws: returns { ran, timedOut, output, detail? } where ran is false
 * when the compile/run step failed (the harness itself stayed intact).
 */
function dockerRun(lang, code, input) {
  const { image, file, cmd } = IMAGES[lang]
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-verify-'))
  fs.writeFileSync(path.join(dir, file), code)
  const mount = `${dir.replace(/\\/g, '/')}:/w`
  try {
    const output = execFileSync(
      'docker',
      ['run', '--rm', '-i', '--network', 'none', '-v', mount, '-w', '/w', image, 'sh', '-c', cmd],
      {
        input,
        encoding: 'utf8',
        stdio: ['pipe', 'pipe', 'pipe'],
        timeout: RUN_TIMEOUT_MS,
        env: { ...process.env, MSYS_NO_PATHCONV: '1' },
      },
    )
    return { ran: true, timedOut: false, output }
  } catch (error) {
    const timedOut = error.killed === true || error.signal === 'SIGTERM'
    return {
      ran: false,
      timedOut,
      output: typeof error.stdout === 'string' ? error.stdout : '',
      detail: (typeof error.stderr === 'string' && error.stderr.trim()) || error.message,
    }
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
}

/** The platform comparison: CRLF normalised, both sides trimmed. */
export function outputsMatch(actual, expected) {
  return actual.replace(/\r\n/g, '\n').trim() === expected.replace(/\r\n/g, '\n').trim()
}

/**
 * One run against one test: PASS (ran cleanly, output matches), FAIL (ran
 * cleanly, wrong output) or CRASH (compile error, non-zero exit, timeout).
 */
export function classifyOutcome(result, expected) {
  if (!result.ran || result.timedOut) return 'CRASH'
  return outputsMatch(result.output ?? '', expected) ? 'PASS' : 'FAIL'
}

/**
 * Verifies one lesson's CODE practice. `run(lang, code, input)` is the runner.
 * Returns the list of problems (empty = verified).
 */
export function verifyCodePractice(name, code, { run }) {
  const problems = []
  const tests = code.tests
  for (const lang of CODE_LANGUAGES) {
    for (let i = 0; i < tests.length; i += 1) {
      const result = run(lang, code.solution[lang], tests[i].input)
      const outcome = classifyOutcome(result, tests[i].expected)
      if (outcome === 'PASS') continue
      const got = outcome === 'FAIL' ? `, got ${JSON.stringify((result.output ?? '').trim()).slice(0, 120)}` : ''
      const detail = result.detail ? ` (${result.detail.split('\n').pop().slice(0, 120)})` : ''
      problems.push(`${name}: solution ${lang} test ${i} ${outcome === 'CRASH' ? 'crashed' : 'failed'}${got}${detail}`)
    }
    let starterFailures = 0
    for (let i = 0; i < tests.length; i += 1) {
      const result = run(lang, code.starter[lang], tests[i].input)
      const outcome = classifyOutcome(result, tests[i].expected)
      if (outcome === 'CRASH') {
        const detail = result.detail ? ` (${result.detail.split('\n').pop().slice(0, 120)})` : ''
        problems.push(`${name}: starter ${lang} test ${i} crashed — the starter must compile and run${detail}`)
      } else if (outcome === 'FAIL') {
        starterFailures += 1
      }
    }
    if (starterFailures === 0) {
      problems.push(`${name}: starter ${lang} fails no test — it must fail at least one`)
    }
  }
  return problems
}

/**
 * The CODE practices of a course folder: one entry per lesson whose assess.json
 * parses and whose practice.kind is CODE, as { name, code }. Everything else is
 * skipped (assess.mjs check reports missing/invalid files and wrong shapes).
 */
export function collectCodePractices(dir, { lesson = null } = {}) {
  const lessonNames = fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && !entry.name.startsWith('.'))
    .map((entry) => entry.name)
    .sort()

  const targets = lesson === null ? lessonNames : [lesson]
  const practices = []
  for (const name of targets) {
    const lessonPath = path.join(dir, name)
    if (lesson !== null && (!fs.existsSync(lessonPath) || !fs.statSync(lessonPath).isDirectory())) {
      throw new Error(`lesson folder not found: ${name}`)
    }
    const file = path.join(lessonPath, 'assess.json')
    if (!fs.existsSync(file)) continue
    let data
    try {
      data = JSON.parse(fs.readFileSync(file, 'utf8'))
    } catch {
      continue
    }
    if (data?.practice?.kind !== 'CODE') continue
    practices.push({ name, code: data.practice.code })
  }
  return practices
}

/**
 * Verifies every CODE practice of a course. Prints one line per lesson through
 * `log`. Returns { problems } — empty when every solution passes every test and
 * every starter fails at least one without crashing.
 */
export async function verifyCourse(course, { lesson = null, log = console.log, run = dockerRun } = {}) {
  const dir = courseDir(course)
  const practices = collectCodePractices(dir, { lesson })
  const problems = []
  for (const { name, code } of practices) {
    const lessonProblems = verifyCodePractice(name, code, { run })
    problems.push(...lessonProblems)
    if (lessonProblems.length === 0) {
      log(`${name}: OK — solutions pass ${code.tests.length} test(s) in ${CODE_LANGUAGES.length} languages, starters fail as expected`)
    } else {
      log(`${name}: ${lessonProblems.length} problem(s)`)
      for (const problem of lessonProblems) log(`  - ${problem}`)
    }
  }
  return { problems }
}

function main() {
  const args = process.argv.slice(2)
  const dry = args.includes('--dry')
  const lessonFlag = args.indexOf('--lesson')
  const lesson = lessonFlag === -1 ? null : args[lessonFlag + 1]
  const knownFlags = new Set(['--dry', '--lesson'])
  const unknown = args.filter((arg) => arg.startsWith('--') && !knownFlags.has(arg))
  const positional = args.filter((arg, i) => !arg.startsWith('--') && (lessonFlag === -1 || i !== lessonFlag + 1))
  const spec = positional[0]
  if (spec === undefined || positional.length > 1 || unknown.length > 0 || (lessonFlag !== -1 && lesson === undefined)) {
    console.error('Usage: node tools/content/assess-verify.mjs <COURSE> [--lesson X] [--dry]')
    process.exit(2)
  }

  let dir
  try {
    dir = courseDir(spec)
  } catch (error) {
    console.error(error.message)
    process.exit(2)
  }

  let practices
  try {
    practices = collectCodePractices(dir, { lesson })
  } catch (error) {
    console.error(error.message)
    process.exit(2)
  }

  if (dry) {
    for (const { name, code } of practices) {
      console.log(
        `${name}: would run ${CODE_LANGUAGES.length} solutions + ${CODE_LANGUAGES.length} starters × ${code.tests.length} test(s)`,
      )
    }
    console.log(`${practices.length} CODE practice(s) would be verified (dry run, nothing executed).`)
    return
  }

  verifyCourse(dir, { lesson }).then(({ problems }) => {
    console.log(`Verified ${practices.length} CODE practice(s), ${problems.length} problem(s).`)
    if (problems.length > 0) process.exit(1)
    console.log('All solutions pass and all starters fail as expected.')
  })
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  main()
}
