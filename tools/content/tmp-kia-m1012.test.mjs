// Temporary scratch check: runs the official offline schema validator over the new
// KIA1406 M10/M11/M12 assess.json files. Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const COURSE = path.resolve(HERE, '..', '..', 'content', 'KIA1406')

for (const lesson of ['M10', 'M11', 'M12']) {
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
    const tests = data.practice.code.tests
    const visible = tests.filter((t) => !t.hidden).length
    console.log(`>>> ${lesson}: ${data.test.questions.length} questions`, JSON.stringify(counts), JSON.stringify(types), 'practice=' + data.practice.kind, `tests=${tests.length} visible=${visible}`)
  })
}
