import { databaseAvailable } from './db'
import { restoreLearningPaths } from './path-state'

/** Puts back the learning paths the run switched off (see `path-state.ts`). */
export default function globalTeardown(): void {
  if (!databaseAvailable) return
  try {
    restoreLearningPaths()
  } catch (error) {
    console.warn(
      'e2e: could not restore the learning paths —',
      error instanceof Error ? error.message : error,
    )
  }
}
