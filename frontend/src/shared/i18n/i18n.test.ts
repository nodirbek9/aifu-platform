// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { check, extractErrorCodes } from '../../../scripts/i18n-check.mjs'

describe('i18n catalogues', () => {
  it('have identical keys, no empty values, and a text for every ApiError code', () => {
    expect(check()).toEqual([])
  })

  it('extracts the error-code union from the generated contract', () => {
    const codes = extractErrorCodes()
    expect(codes.length).toBeGreaterThan(40)
    expect(codes).toContain('AUTH_INVALID_CREDENTIALS')
    expect(codes).toContain('TIMETABLE_CONFLICT')
  })
})
