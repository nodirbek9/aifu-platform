import assert from 'node:assert/strict'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { checkCourse } from '../../tools/content/assess.mjs'

const dir = path.resolve(fileURLToPath(import.meta.url), '..')

test('scratch: A7, A8, A9 pass the offline checker', () => {
  const { results } = checkCourse(dir)
  const report = new Map(results.map((r) => [r.name, r.problems]))
  for (const name of ['A7', 'A8', 'A9']) {
    console.log(`${name}: ${JSON.stringify(report.get(name), null, 2)}`)
  }
  assert.deepEqual(report.get('A7'), [])
  assert.deepEqual(report.get('A8'), [])
  assert.deepEqual(report.get('A9'), [])
})
