import type { TFunction } from 'i18next'
import type { ApiProblem } from '@/shared/api/errors'

/** "Missing: RU, EN" from a TRANSLATION_INCOMPLETE answer; the plain error otherwise. */
export function publishError(problem: ApiProblem, t: TFunction) {
  const missing = problem.details?.missingLocales
  if (problem.code === 'TRANSLATION_INCOMPLETE' && Array.isArray(missing)) {
    return t('authoring.publishMissing', { locales: missing.map(String).join(', ').toUpperCase() })
  }
  return problem.code === 'PUBLICATION_VALIDATION_FAILED' && problem.message !== ''
    ? `${t(`errors.${problem.code}`)} ${problem.message}`
    : t(`errors.${problem.code}`)
}
