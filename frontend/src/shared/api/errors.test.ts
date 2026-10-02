import { describe, expect, it } from 'vitest'
import { fieldErrors, isApiErrorBody, toApiProblem, withStatus } from './errors'

describe('toApiProblem', () => {
  it('passes through a server error envelope', () => {
    const problem = toApiProblem({
      code: 'AUTH_INVALID_CREDENTIALS',
      message: 'Bad credentials',
      requestId: 'abc123',
      details: { fields: { password: 'Required' } },
    })
    expect(problem.code).toBe('AUTH_INVALID_CREDENTIALS')
    expect(problem.message).toBe('Bad credentials')
    expect(problem.requestId).toBe('abc123')
    expect(problem.details).toEqual({ fields: { password: 'Required' } })
  })

  it('maps a fetch failure to NETWORK_ERROR', () => {
    const problem = toApiProblem(new TypeError('Failed to fetch'))
    expect(problem.code).toBe('NETWORK_ERROR')
    expect(problem.status).toBe(0)
  })

  it('maps other errors and odd values to UNKNOWN_ERROR', () => {
    expect(toApiProblem(new Error('boom')).code).toBe('UNKNOWN_ERROR')
    expect(toApiProblem('boom').code).toBe('UNKNOWN_ERROR')
    expect(toApiProblem(null).code).toBe('UNKNOWN_ERROR')
    expect(toApiProblem({ message: 'no code' }).code).toBe('UNKNOWN_ERROR')
  })

  it('withStatus attaches the HTTP status', () => {
    const problem = withStatus(toApiProblem({ code: 'INTERNAL_ERROR', message: 'x' }), 500)
    expect(problem.status).toBe(500)
  })
})

describe('isApiErrorBody', () => {
  it('accepts only objects with a string code', () => {
    expect(isApiErrorBody({ code: 'RESOURCE_NOT_FOUND' })).toBe(true)
    expect(isApiErrorBody({ code: 42 })).toBe(false)
    expect(isApiErrorBody(null)).toBe(false)
    expect(isApiErrorBody('RESOURCE_NOT_FOUND')).toBe(false)
  })
})

describe('fieldErrors', () => {
  it('reads details.fields into a string map', () => {
    const problem = toApiProblem({
      code: 'VALIDATION_FAILED',
      message: 'x',
      details: { fields: { identifier: 'Required', password: 'Too short' } },
    })
    expect(fieldErrors(problem)).toEqual({ identifier: 'Required', password: 'Too short' })
  })

  it('returns an empty map when there are no fields', () => {
    expect(fieldErrors(toApiProblem({ code: 'INTERNAL_ERROR', message: 'x' }))).toEqual({})
    expect(
      fieldErrors(toApiProblem({ code: 'INTERNAL_ERROR', message: 'x', details: { fields: 'no' } })),
    ).toEqual({})
  })
})
