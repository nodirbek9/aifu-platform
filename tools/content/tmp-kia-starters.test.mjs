// Temporary scratch check: prints the code-starter problems of KIA1406 M10/M11/M12 assess.json
// as single-line JSON so escapes are visible. Deleted right after the run.
import fs from 'node:fs'
import path from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { validateAssess } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const COURSE = path.resolve(HERE, '..', '..', 'content', 'KIA1406')

for (const lesson of ['M10', 'M11', 'M12']) {
  test(`${lesson}: starter problem list`, () => {
    const data = JSON.parse(fs.readFileSync(path.join(COURSE, lesson, 'assess.json'), 'utf8'))
    const problems = validateAssess(data).filter((p) => p.includes('starter'))
    console.log('PROBLEMS ' + JSON.stringify(problems))
  })
}
