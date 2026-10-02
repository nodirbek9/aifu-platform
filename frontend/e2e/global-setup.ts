import { execFileSync } from 'node:child_process'

/**
 * The flows change the demo student's data (a lesson gets completed), and the API — rightly —
 * does not let a student un-complete a lesson. So before every run the lesson progress of the
 * demo student is cleared in the development database, through the Compose Postgres container.
 * Nothing else is touched. Set E2E_DB_CONTAINER to another container name, or to "none" to skip.
 */
const RESET_SQL = `
  delete from lesson_progress
  where enrollment_id in (
    select e.id from enrollments e join users u on u.id = e.student_user_id where u.username = 'student3'
  )`

export default function globalSetup(): void {
  const container = process.env.E2E_DB_CONTAINER ?? 'aifu-platform-postgres-1'
  if (container === 'none') return
  try {
    execFileSync(
      'docker',
      ['exec', container, 'sh', '-c', 'psql -q -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "$0"', RESET_SQL],
      { stdio: 'pipe' },
    )
  } catch (error) {
    console.warn(
      `e2e: could not reset the demo student's lesson progress in "${container}" — ` +
        'the "marked as done" flow may find the lesson already completed.',
      error instanceof Error ? error.message : error,
    )
  }
}
