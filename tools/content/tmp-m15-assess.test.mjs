// Temporary scratch check: runs the official offline schema validator over the new
// IAKTT1105 M15 assess.json. Delete right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'
import { countViolations } from './fix-apostrophes.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const FILE = path.resolve(HERE, '..', '..', 'content', 'IAKTT1105', 'M15', 'assess.json')

test('M15: assess.json parses and validateAssess reports no problems', () => {
  const raw = fs.readFileSync(FILE, 'utf8')
  let data
  try {
    data = JSON.parse(raw)
  } catch (error) {
    assert.fail(`M15: invalid JSON: ${error.message}`)
  }
  assert.deepEqual(validateAssess(data), [], 'M15 problems')

  const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
  const types = {}
  let points = 0
  for (const q of data.test.questions) {
    counts[q.difficulty] += 1
    types[q.type] = (types[q.type] || 0) + 1
    points += q.points
  }
  console.log(
    `>>> M15: test ${data.test.questions.length} questions`,
    JSON.stringify(counts),
    JSON.stringify(types),
    `test points=${points}`,
    `practice=${data.practice.kind} (${data.practice.answer.questions.length} questions)`,
  )

  const uzStrings = []
  const walk = (node) => {
    if (typeof node === 'string') uzStrings.push(node)
    else if (Array.isArray(node)) node.forEach(walk)
    else if (node && typeof node === 'object') for (const [key, child] of Object.entries(node)) if (key !== 'ru' && key !== 'en') walk(child)
  }
  walk(data)
  const bad = uzStrings.filter((text) => countViolations(text) > 0)
  assert.deepEqual(bad, [], 'uz text with ASCII apostrophes')
})
