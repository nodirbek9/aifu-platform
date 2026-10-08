// Tests for the pure parts of the sandbox verifier (docs/PLAN.md §4, phase 6 C0):
// the output comparison, the run classification and the per-lesson verdict, each
// with the Docker runner injected as a fake — no Docker needed.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { test } from 'node:test'
import { classifyOutcome, collectCodePractices, outputsMatch, verifyCodePractice, verifyCourse } from './assess-verify.mjs'

const ran = (output) => ({ ran: true, timedOut: false, output })
const crashed = (detail = 'compile error') => ({ ran: false, timedOut: false, output: '', detail })
const timedOut = () => ({ ran: false, timedOut: true, output: '' })

const TESTS = [
  { input: '1\n', expected: '1\n', hidden: false },
  { input: '2\n', expected: '2\n', hidden: true },
  { input: '42\n', expected: '42\n', hidden: true },
]

const SOLUTION = { python: 'SOLVED py', java: 'SOLVED java', cpp: 'SOLVED cpp' }
const STARTER = { python: 'starter py', java: 'starter java', cpp: 'starter cpp' }
const code = () => ({ starter: { ...STARTER }, solution: { ...SOLUTION }, tests: TESTS.map((t) => ({ ...t })) })

const hasProblem = (problems, fragment) =>
  assert.ok(
    problems.some((problem) => problem.includes(fragment)),
    `expected a problem containing ${JSON.stringify(fragment)}, got:\n${problems.join('\n')}`,
  )

test('outputsMatch: trims both sides like the platform', () => {
  assert.ok(outputsMatch('42\n', '42'))
  assert.ok(outputsMatch('  42  \n', '42\n\n'))
  assert.ok(!outputsMatch('42\n', '43\n'))
  assert.ok(!outputsMatch('4 2\n', '42\n'))
})

test('outputsMatch: normalises CRLF before comparing', () => {
  assert.ok(outputsMatch('a\r\nb\r\n', 'a\nb\n'))
})

test('classifyOutcome: PASS only when the run was clean and the output matches', () => {
  assert.equal(classifyOutcome(ran('42\n'), '42'), 'PASS')
  assert.equal(classifyOutcome(ran('43\n'), '42'), 'FAIL')
  assert.equal(classifyOutcome(crashed(), '42'), 'CRASH')
  assert.equal(classifyOutcome(timedOut(), '42'), 'CRASH')
})

/** A runner whose solutions print the expected output and whose starters print garbage. */
function goodRunner(lang, source, input) {
  const test = TESTS.find((t) => t.input === input)
  return ran(source.startsWith('SOLVED') ? test.expected : 'not implemented\n')
}

test('verifyCodePractice: solutions pass every test, starters fail — no problems', () => {
  assert.deepEqual(verifyCodePractice('M1', code(), { run: goodRunner }), [])
})

test('verifyCodePractice: a wrong solution output is reported per language and test', () => {
  const run = (lang, source, input) => {
    if (lang === 'java' && source.startsWith('SOLVED') && input === '2\n') return ran('wrong\n')
    return goodRunner(lang, source, input)
  }
  const problems = verifyCodePractice('M1', code(), { run })
  assert.equal(problems.length, 1)
  hasProblem(problems, 'M1: solution java test 1 failed')
})

test('verifyCodePractice: a crashing solution is reported', () => {
  const run = (lang, source, input) => {
    if (lang === 'cpp' && source.startsWith('SOLVED')) return crashed('g++: error')
    return goodRunner(lang, source, input)
  }
  const problems = verifyCodePractice('M1', code(), { run })
  assert.equal(problems.length, TESTS.length)
  hasProblem(problems, 'M1: solution cpp test 0 crashed')
})

test('verifyCodePractice: a starter that passes every test is reported', () => {
  const problems = verifyCodePractice('M1', code(), {
    run: (lang, source, input) => ran(TESTS.find((t) => t.input === input).expected),
  })
  assert.equal(problems.length, 3)
  hasProblem(problems, 'M1: starter python fails no test — it must fail at least one')
  hasProblem(problems, 'M1: starter cpp fails no test — it must fail at least one')
})

test('verifyCodePractice: a crashing starter is reported (it must compile and run)', () => {
  const run = (lang, source, input) => {
    if (lang === 'python' && !source.startsWith('SOLVED') && input === '42\n') return crashed('SyntaxError')
    return goodRunner(lang, source, input)
  }
  const problems = verifyCodePractice('M1', code(), { run })
  hasProblem(problems, 'M1: starter python test 2 crashed — the starter must compile and run')
})

test('collectCodePractices: keeps CODE practices, skips ANSWER, missing and invalid files', () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-verify-'))
  try {
    fs.mkdirSync(path.join(dir, 'M1'))
    fs.mkdirSync(path.join(dir, 'M2'))
    fs.mkdirSync(path.join(dir, 'M3'))
    fs.mkdirSync(path.join(dir, 'M4'))
    fs.writeFileSync(path.join(dir, 'M1', 'assess.json'), JSON.stringify({ practice: { kind: 'CODE', code: code() } }))
    fs.writeFileSync(path.join(dir, 'M2', 'assess.json'), JSON.stringify({ practice: { kind: 'ANSWER' } }))
    fs.writeFileSync(path.join(dir, 'M4', 'assess.json'), '{ not json')

    const practices = collectCodePractices(dir)
    assert.deepEqual(practices.map((p) => p.name), ['M1'])

    assert.deepEqual(collectCodePractices(dir, { lesson: 'M2' }), [])
    assert.throws(() => collectCodePractices(dir, { lesson: 'M9' }), /lesson folder not found: M9/)
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('verifyCourse: per-lesson lines, problems aggregated, runner injected', async () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-verify-'))
  try {
    fs.mkdirSync(path.join(dir, 'M1'))
    fs.mkdirSync(path.join(dir, 'M2'))
    fs.writeFileSync(path.join(dir, 'M1', 'assess.json'), JSON.stringify({ practice: { kind: 'CODE', code: code() } }))
    fs.writeFileSync(path.join(dir, 'M2', 'assess.json'), JSON.stringify({ practice: { kind: 'CODE', code: code() } }))

    const lines = []
    const run = (lang, source, input) => {
      // M2's python starter accidentally solves the task.
      if (lang === 'python') return ran(TESTS.find((t) => t.input === input).expected)
      return goodRunner(lang, source, input)
    }
    // Only the M2 practice misbehaves: route by making M1 fully good first.
    const good = await verifyCourse(dir, { lesson: 'M1', log: (line) => lines.push(line), run: goodRunner })
    assert.deepEqual(good.problems, [])
    assert.ok(lines.some((line) => line.startsWith('M1: OK')))

    lines.length = 0
    const bad = await verifyCourse(dir, { lesson: 'M2', log: (line) => lines.push(line), run })
    assert.equal(bad.problems.length, 1)
    hasProblem(bad.problems, 'M2: starter python fails no test')
    assert.ok(lines.some((line) => line.startsWith('M2: 1 problem(s)')))
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})
