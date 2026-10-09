// Temporary scratch check: validates content/IAKTT1105/M13/assess.json with the
// official schema validator and recomputes every CODE test's expected output from
// its input (IEEE double + integer truncation — the same arithmetic all three
// sandbox languages use). Deleted right after the run.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const FILE = path.resolve(HERE, '..', '..', 'content', 'IAKTT1105', 'M13', 'assess.json')

test('IAKTT1105 M13: assess.json parses and validateAssess reports no problems', () => {
  const data = JSON.parse(fs.readFileSync(FILE, 'utf8'))
  assert.deepEqual(validateAssess(data), [], 'schema problems')
})

test('IAKTT1105 M13: every expected output equals the recomputed one', () => {
  const data = JSON.parse(fs.readFileSync(FILE, 'utf8'))
  const { tests } = data.practice.code
  for (const t of tests) {
    let bestKanal = ''
    let bestFoyda = null
    const lines = []
    for (const satr of t.input.split('\n')) {
      if (!satr.trim()) continue
      const [kanal, v, o, rev, ad] = satr.split(/\s+/)
      const [tashrif, buyurtma, tushum, reklama] = [v, o, rev, ad].map(Number)
      const foyda = Math.trunc((tushum * 30) / 100) - reklama
      if (bestFoyda === null || foyda > bestFoyda) {
        bestFoyda = foyda
        bestKanal = kanal
      }
      lines.push(
        `${kanal}: conversion ${((100 * buyurtma) / tashrif).toFixed(2)}%, average order ${Math.trunc(tushum / buyurtma)}, CAC ${Math.trunc(reklama / buyurtma)}, ROAS ${(tushum / reklama).toFixed(2)}`,
      )
    }
    lines.push(`best: ${bestKanal}`)
    assert.equal(lines.join('\n'), t.expected.trim(), `test ${JSON.stringify(t.input.slice(0, 30))}`)
  }
})

test('IAKTT1105 M13: starters are comment-free and TODO-free', () => {
  const data = JSON.parse(fs.readFileSync(FILE, 'utf8'))
  for (const [lang, code] of Object.entries(data.practice.code.starter)) {
    if (lang === 'python') assert.ok(!code.includes('#'), 'python starter has #')
    else assert.ok(!code.includes('//') && !code.includes('/*'), `${lang} starter has //`)
    assert.ok(!/\bTODO\b/i.test(code), `${lang} starter has TODO`)
  }
})
