// Temporary scratch check for the KIA1406 A7/A8/A9 assess.json files (phase 6 content).
// Runs the official offline schema, then simulates the algorithm of each CODE practice in
// JavaScript to confirm that every solution's expected output is right and that every
// starter fails at least one test. Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const COURSE = path.resolve(HERE, '..', '..', 'content', 'KIA1406')

const CJK = /[\p{Script=Han}\p{Script=Hiragana}\p{Script=Katakana}\p{Script=Hangul}]/u

function walkStrings(node, at, out) {
  if (typeof node === 'string') out.push([at, node])
  else if (Array.isArray(node)) node.forEach((item, i) => walkStrings(item, `${at}[${i}]`, out))
  else if (node !== null && typeof node === 'object') {
    for (const [key, child] of Object.entries(node)) walkStrings(child, `${at}.${key}`, out)
  }
  return out
}

const fermat = (input) => {
  const n = Number(input.trim())
  let a = 0
  while (a * a < n) a += 1
  let b = 0
  while (b * b < a * a - n) b += 1
  return `${a - b} ${a + b}`
}
const fermatStarter = (input) => {
  const n = Number(input.trim())
  let a = 0
  while (a * a < n) a += 1
  let b = 0
  while (b * b < a * a - n) b += 1
  return `${a + b} ${a - b}`
}

const salts = (input, filter) => {
  const parts = input.trim().split(/\s+/)
  const n = Number(parts[0])
  const order = []
  const counts = new Map()
  for (let i = 0; i < n; i += 1) {
    const tuz = parts[1 + i * 2 + 1]
    if (!counts.has(tuz)) {
      counts.set(tuz, 0)
      order.push(tuz)
    }
    counts.set(tuz, counts.get(tuz) + 1)
  }
  const rows = order.filter((t) => (filter === 'starter' ? counts.get(t) >= 1 : counts.get(t) > 1)).map((t) => `${t} ${counts.get(t)}`)
  return rows.length ? rows.join('\n') : 'ok'
}

const domain = (input, mode) => {
  const parts = input.trim().split(/\s+/)
  const t = parts[0].toLowerCase()
  const out = []
  for (const nom of parts.slice(1)) {
    const k = nom.toLowerCase()
    const genuine = mode === 'starter' ? k === t || k.startsWith(t) : k === t || k.endsWith('.' + t)
    if (genuine) out.push('ok')
    else {
      const qism = nom.split('.')
      out.push(qism.slice(-2).join('.'))
    }
  }
  return out.join('\n')
}

const SIM = { A7: [fermat, fermatStarter], A8: [(i) => salts(i, 'solution'), (i) => salts(i, 'starter')], A9: [(i) => domain(i, 'solution'), (i) => domain(i, 'starter')] }

for (const lesson of ['A7', 'A8', 'A9']) {
  test(`${lesson}: assess.json passes the official schema`, () => {
    const file = path.join(COURSE, lesson, 'assess.json')
    const raw = fs.readFileSync(file, 'utf8')
    assert.equal(raw.includes('\t'), false, `${lesson}: tab character in file`)
    let data
    try {
      data = JSON.parse(raw)
    } catch (error) {
      assert.fail(`${lesson}: invalid JSON: ${error.message}`)
    }
    assert.deepEqual(validateAssess(data), [], `${lesson}: schema rejected the file`)

    for (const [at, value] of walkStrings(data, '', [])) {
      assert.equal(CJK.test(value), false, `${lesson}: CJK character leaked into ${at}`)
      assert.match(value, /^[^\u0000-\u0008\u000e-\u001f]*$/, `${lesson}: control character in ${at}`)
    }

    const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
    const types = new Set()
    for (const question of data.test.questions) {
      counts[question.difficulty] += 1
      types.add(question.type)
    }
    assert.equal(data.test.questions.length, 9, `${lesson}: expected 9 questions`)
    assert.equal(types.size, 5, `${lesson}: expected all five question types, got ${[...types]}`)
    assert.deepEqual(counts, { EASY: 3, MEDIUM: 3, HARD: 3 }, `${lesson}: difficulty split`)

    const localeKeys = ['uz', 'ru', 'en']
    for (const question of data.test.questions) {
      for (const locale of localeKeys) {
        assert.ok(question.prompt[locale].trim().length > 0, `${lesson}: empty prompt.${locale}`)
        assert.ok(question.explanation[locale].trim().length > 0, `${lesson}: empty explanation.${locale}`)
        assert.ok(question.explanation[locale].split(/\s+/).length <= 40, `${lesson}: long explanation (${locale}): ${question.explanation[locale]}`)
      }
    }

    assert.equal(data.practice.kind, 'CODE', `${lesson}: practice kind`)
    for (const locale of localeKeys) {
      assert.ok(data.practice.statement[locale].trim().length > 0, `${lesson}: empty statement.${locale}`)
    }
    const code = data.practice.code
    for (const language of ['python', 'java', 'cpp']) {
      assert.ok(code.starter[language].trim().length > 0, `${lesson}: empty starter ${language}`)
      assert.ok(code.solution[language].trim().length > 0, `${lesson}: empty solution ${language}`)
    }
    const hidden = code.tests.filter((t) => t.hidden).length
    assert.ok(hidden >= 2, `${lesson}: only ${hidden} hidden tests`)
    console.log(`${lesson}: ${code.tests.length} tests (${hidden} hidden), difficulty`, counts)
  })

  test(`${lesson}: solutions match every expected output, starters fail at least one`, () => {
    const data = JSON.parse(fs.readFileSync(path.join(COURSE, lesson, 'assess.json'), 'utf8'))
    const [solve, start] = SIM[lesson]
    let starterFails = 0
    for (const t of data.practice.code.tests) {
      const got = solve(t.input).replace(/\r\n/g, '\n').trim()
      assert.equal(got, t.expected.replace(/\r\n/g, '\n').trim(), `${lesson}: solution mismatch for input ${JSON.stringify(t.input)}`)
      const st = start(t.input).replace(/\r\n/g, '\n').trim()
      if (st !== t.expected.replace(/\r\n/g, '\n').trim()) starterFails += 1
      assert.equal(st.length > 0, true, `${lesson}: starter produced no output for ${JSON.stringify(t.input)}`)
    }
    assert.ok(starterFails >= 1, `${lesson}: the starter fails no test`)
    console.log(`${lesson}: starter fails ${starterFails}/${data.practice.code.tests.length} tests`)
  })
}
