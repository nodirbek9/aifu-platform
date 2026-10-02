import { execFileSync } from 'node:child_process'

/**
 * The flows change the demo student's data: a lesson gets completed, a solution is submitted.
 * The API — rightly — offers no way to undo either, and a task allows a limited number of
 * attempts. So before every run two things of the demo student are cleared in the development
 * database, through the Compose Postgres container:
 *   - the lesson progress;
 *   - the submissions to the one task the coding flow submits to (DAS-S1-A8).
 * Nothing else is touched. Set E2E_DB_CONTAINER to another container name, or to "none" to skip.
 */
const STUDENT_ENROLLMENTS = `
  select e.id from enrollments e join users u on u.id = e.student_user_id where u.username = 'student3'`

const RESET_SQL = `
  delete from lesson_progress where enrollment_id in (${STUDENT_ENROLLMENTS});
  delete from submissions
  where coding_task_id = (select id from coding_tasks where code = 'DAS-S1-A8')
    and enrollment_id in (${STUDENT_ENROLLMENTS});`

export default function globalSetup(): void {
  const container = process.env.E2E_DB_CONTAINER ?? 'aifu-platform-postgres-1'
  if (container === 'none') return
  try {
    execFileSync(
      'docker',
      [
        'exec',
        container,
        'sh',
        '-c',
        'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "$0"',
        RESET_SQL,
      ],
      { stdio: 'pipe' },
    )
  } catch (error) {
    console.warn(
      `e2e: could not reset the demo student's data in "${container}" — ` +
        'flows that complete a lesson or submit a solution may find them already done.',
      error instanceof Error ? error.message : error,
    )
  }
}
