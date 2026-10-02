import { databaseAvailable, resetDemoStudent } from './db'

/**
 * The flows change the demo student's data: a lesson gets completed, a solution is submitted.
 * The API — rightly — offers no way to undo either, and a task allows a limited number of
 * attempts. So before every run two things of the demo student are cleared in the development
 * database (see `db.ts`): the lesson progress, and the submissions to the one task the coding
 * flow submits to (DAS-S1-A8). The quiz flows clear their own attempts. Nothing else is touched.
 */
export default function globalSetup(): void {
  if (!databaseAvailable) return
  try {
    resetDemoStudent('student3')
  } catch (error) {
    console.warn(
      "e2e: could not reset the demo student's data — " +
        'flows that complete a lesson or submit a solution may find them already done.',
      error instanceof Error ? error.message : error,
    )
  }
}
