import assert from 'node:assert/strict'
import test from 'node:test'
import { planSteps } from './onboard.mjs'
import { buildPathRequest } from './assess-path.mjs'

const names = (steps) => steps.map((s) => s.name)

test('plan: the default chain imports, checks and configures; it never enables or authors', () => {
  const steps = planSteps('NEW1', '/c/NEW1')
  assert.ok(names(steps).includes('lessons') && names(steps).includes('check') && names(steps).includes('verify'))
  assert.ok(!names(steps).some((n) => n.startsWith('author')))
  const path = steps.find((s) => s.name === 'path')
  assert.ok(!path.args.includes('--enable'), 'a new course starts with the path off')
})

test('plan: --author adds the writer steps before the check; --enable and --replace reach their steps', () => {
  const steps = planSteps('NEW1', '/c/NEW1', { author: true, enable: true, replace: true })
  const order = names(steps)
  assert.ok(order.indexOf('author lessons') < order.indexOf('check'))
  assert.ok(order.indexOf('author midterm') < order.indexOf('check'))
  assert.ok(steps.find((s) => s.name === 'path').args.includes('--enable'))
  assert.ok(steps.find((s) => s.name === 'import').args.includes('--replace'))
})

test('plan: a dry run writes nothing — no authoring, no sandbox run, no import (the course may not exist yet)', () => {
  const steps = planSteps('NEW1', '/c/NEW1', { author: true, enable: true, dryRun: true })
  assert.ok(!names(steps).some((n) => n.startsWith('author') || n === 'verify' || n === 'import' || n === 'path'))
  for (const step of steps.filter((s) => ['scaffold', 'lessons'].includes(s.name))) {
    assert.ok(step.args.includes('--dry-run'), step.name)
  }
})

test('path request: without a midterm only the switch is sent; with one, activity and anchor too', () => {
  assert.deepEqual(buildPathRequest({ enabled: false }), { enabled: false })
  assert.deepEqual(buildPathRequest({ enabled: true, midtermActivityId: 'a', midtermAfterLessonId: 'l' }), {
    enabled: true,
    midtermActivityId: 'a',
    midtermAfterLessonId: 'l',
  })
  assert.deepEqual(buildPathRequest({ enabled: true, midtermActivityId: 'a' }), { enabled: true }, 'a half pair is never sent')
})
