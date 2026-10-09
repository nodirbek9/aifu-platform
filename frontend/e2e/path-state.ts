import { existsSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { runSql } from './db'

/**
 * The flows were written for courses whose learning path is off (every lesson open). On a
 * development database where an administrator has switched paths on, a run first remembers which
 * courses had it, switches them off, and `global-teardown` puts them back. The state file survives
 * an aborted run, so the next run restores the original state instead of remembering "all off".
 * `learning-path.spec.ts` switches the one path it needs on by itself.
 */
const STATE_FILE = path.join(os.tmpdir(), 'aifu-e2e-enabled-paths.json')

export function suspendLearningPaths(): void {
  if (!existsSync(STATE_FILE)) {
    const ids = runSql('select course_id from course_learning_paths where enabled')
      .split('\n')
      .filter(Boolean)
    writeFileSync(STATE_FILE, JSON.stringify(ids))
  }
  runSql('update course_learning_paths set enabled = false where enabled')
}

export function restoreLearningPaths(): void {
  if (!existsSync(STATE_FILE)) return
  const ids = JSON.parse(readFileSync(STATE_FILE, 'utf8')) as string[]
  if (ids.length > 0) {
    const list = ids.map((id) => `'${id}'`).join(',')
    runSql(`update course_learning_paths set enabled = true where course_id in (${list})`)
  }
  rmSync(STATE_FILE)
}
