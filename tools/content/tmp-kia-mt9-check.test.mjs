// Temporary scratch check: runs the official offline schema validator over the new
// KIA1406 MT9 assess.json. Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'
import { countViolations } from './fix-apostrophes.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const FILE = path.resolve(HERE, '..', '..', 'content', 'KIA1406', 'MT9', 'assess.json')

test('MT9: assess.json parses and validateAssess reports no problems', () => {
  const raw = fs.readFileSync(FILE, 'utf8')
  let data
  try {
    data = JSON.parse(raw)
  } catch (error) {
    assert.fail(`MT9: invalid JSON: ${error.message}`)
  }
  assert.deepEqual(validateAssess(data), [], 'MT9 problems')
  const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
  const types = {}
  for (const q of data.test.questions) {
    counts[q.difficulty] += 1
    types[q.type] = (types[q.type] || 0) + 1
  }
  const uzStrings = JSON.stringify(data, (key, value) => (key === 'uz' ? `[${countViolations(value)}]${value}` : value))
  assert.equal(uzStrings.includes('[0]'), true, 'sanity: the marker ran')
  assert.equal(uzStrings.match(/\[\d*[1-9]\d*\]/g), null, 'MT9: uz apostrophe violations')
  console.log(`>>> MT9: ${data.test.questions.length} questions`, JSON.stringify(counts), JSON.stringify(types), 'practice=' + data.practice.kind, `practice questions=${data.practice.answer.questions.length}`)
})
