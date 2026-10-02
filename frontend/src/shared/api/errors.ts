import type { components } from './schema'

export type ApiErrorBody = components['schemas']['ApiError']
export type ApiErrorCode = NonNullable<ApiErrorBody['code']>

/** A failure the UI can reason about. `status` 0 means the request never reached the server. */
export type ApiProblem = {
  status: number
  code: ApiErrorCode | 'NETWORK_ERROR' | 'UNKNOWN_ERROR'
  message: string
  details?: Record<string, unknown>
  requestId?: string
}

/** What every failed API call throws. Carries the normalised problem, HTTP status included. */
export class ApiFailure extends Error {
  readonly problem: ApiProblem

  constructor(problem: ApiProblem) {
    super(problem.message === '' ? problem.code : problem.message)
    this.name = 'ApiFailure'
    this.problem = problem
  }
}

export function isApiErrorBody(value: unknown): value is ApiErrorBody {
  return typeof value === 'object' && value !== null && 'code' in value && typeof value.code === 'string'
}

/**
 * Normalises anything a query or mutation can throw — an ApiFailure from `unwrap`,
 * a parsed ApiError body, a fetch TypeError, or something unexpected — into one shape.
 * Branch on `code`, never on `message`.
 */
export function toApiProblem(error: unknown): ApiProblem {
  if (error instanceof ApiFailure) return error.problem
  if (isApiErrorBody(error)) {
    const problem: ApiProblem = {
      status: 0,
      code: error.code ?? 'UNKNOWN_ERROR',
      message: error.message ?? '',
    }
    if (error.details !== undefined) problem.details = error.details
    if (error.requestId !== undefined) problem.requestId = error.requestId
    return problem
  }
  if (error instanceof TypeError) {
    return { status: 0, code: 'NETWORK_ERROR', message: error.message }
  }
  if (error instanceof Error) {
    return { status: 0, code: 'UNKNOWN_ERROR', message: error.message }
  }
  return { status: 0, code: 'UNKNOWN_ERROR', message: String(error) }
}

/** Attach the HTTP status to a problem when the response object is known. */
export function withStatus(problem: ApiProblem, status: number): ApiProblem {
  return { ...problem, status }
}

export function fieldErrors(problem: ApiProblem): Record<string, string> {
  const fields = problem.details?.fields
  if (typeof fields !== 'object' || fields === null) return {}
  const result: Record<string, string> = {}
  for (const [key, value] of Object.entries(fields)) {
    if (typeof value === 'string') result[key] = value
  }
  return result
}
