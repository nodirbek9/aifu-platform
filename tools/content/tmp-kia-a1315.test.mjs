// Scratch check for the KIA1406 A13/A14/A15 assess.json files against the official
// schema validator. Delete after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const COURSE = path.resolve(HERE, '..', '..', 'content', 'KIA1406')

for (const lesson of ['A13', 'A14', 'A15']) {
  test(`${lesson}: assess.json parses and validateAssess reports no problems`, () => {
    const raw = fs.readFileSync(path.join(COURSE, lesson, 'assess.json'), 'utf8')
    let data
    try {
      data = JSON.parse(raw)
    } catch (error) {
      assert.fail(`${lesson}: invalid JSON: ${error.message}`)
    }
    assert.deepEqual(validateAssess(data), [], `${lesson} problems`)
    const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
    const types = {}
    for (const q of data.test.questions) {
      counts[q.difficulty] += 1
      types[q.type] = (types[q.type] || 0) + 1
    }
    console.log(`>>> ${lesson}: ${data.test.questions.length} questions`, JSON.stringify(counts), JSON.stringify(types), 'practice=' + data.practice.kind)
  })
}
