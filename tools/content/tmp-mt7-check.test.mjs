// Temporary scratch check: runs the official offline schema validator over the new
// KIA1406 MT7 assess.json. Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const FILE = path.resolve(HERE, '..', '..', 'content', 'KIA1406', 'MT7', 'assess.json')

test('MT7: assess.json parses and validateAssess reports no problems', () => {
  const raw = fs.readFileSync(FILE, 'utf8')
  let data
  try {
    data = JSON.parse(raw)
  } catch (error) {
    assert.fail(`MT7: invalid JSON: ${error.message}`)
  }
  assert.deepEqual(validateAssess(data), [], 'MT7 problems')
  const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
  for (const q of data.test.questions) counts[q.difficulty] += 1
  console.log(`>>> MT7: ${data.test.questions.length} questions`, JSON.stringify(counts), 'practice=' + data.practice.kind)
})
