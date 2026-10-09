// Temporary scratch check: runs the official offline schema validator over the new
// IAKTT1105 M9 assess.json and the course checker. Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'
import { checkCourse } from './assess.mjs'
import { countViolations } from './fix-apostrophes.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const COURSE = path.resolve(HERE, '..', '..', 'content', 'IAKTT1105')

test('M9: assess.json parses and validateAssess reports no problems', () => {
  const raw = fs.readFileSync(path.join(COURSE, 'M9', 'assess.json'), 'utf8')
  const data = JSON.parse(raw)
  assert.deepEqual(validateAssess(data), [], 'M9 problems')

  const counts = { EASY: 0, MEDIUM: 0, HARD: 0 }
  const types = {}
  for (const q of data.test.questions) {
    counts[q.difficulty] += 1
    types[q.type] = (types[q.type] || 0) + 1
  }
  console.log('>>> M9 test:', data.test.questions.length, JSON.stringify(counts), JSON.stringify(types), 'practice=' + data.practice.kind)
})

test('M9: no ASCII apostrophe in any uz string of the file', () => {
  const data = JSON.parse(fs.readFileSync(path.join(COURSE, 'M9', 'assess.json'), 'utf8'))
  const offenders = []
  const collect = (value, trail) => {
    if (typeof value === 'string') {
      const inUz = /\.uz$/.test(trail)
      if (inUz && countViolations(value) > 0) offenders.push(`${trail} -> ${value}`)
      return
    }
    if (Array.isArray(value)) return value.forEach((item, i) => collect(item, `${trail}[${i}]`))
    if (value !== null && typeof value === 'object') {
      for (const [key, child] of Object.entries(value)) collect(child, `${trail}.${key}`)
    }
  }
  collect(data, 'root')
  void walk
  assert.deepEqual(offenders, [], 'uz apostrophe violations')
})

test('M9: every localized object has all three non-empty locales', () => {
  const data = JSON.parse(fs.readFileSync(path.join(COURSE, 'M9', 'assess.json'), 'utf8'))
  const missing = []
  const check = (value, trail) => {
    if (Array.isArray(value)) return value.forEach((item, i) => check(item, `${trail}[${i}]`))
    if (value === null || typeof value !== 'object') return
    const keys = Object.keys(value)
    if (keys.some((k) => ['uz', 'ru', 'en'].includes(k))) {
      for (const locale of ['uz', 'ru', 'en']) {
        const text = value[locale]
        if (typeof text !== 'string' || text.trim() === '') missing.push(`${trail}.${locale}`)
      }
      return
    }
    for (const [key, child] of Object.entries(value)) check(child, `${trail}.${key}`)
  }
  check(data, 'root')
  assert.deepEqual(missing, [], 'missing locale text')
})

test('course check: M9 reported as OK by assess.mjs checkCourse', () => {
  const { results, missing } = checkCourse(COURSE, { lesson: 'M9' })
  console.log('>>> checkCourse M9:', JSON.stringify(results), 'missing=', missing)
  assert.deepEqual(results[0].problems, [], 'checkCourse problems')
})
