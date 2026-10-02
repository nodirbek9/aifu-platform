import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  onSessionExpired,
  refreshOnce,
  resetSessionForTests,
  setSession,
} from './session'

const fetchMock = vi.fn<typeof fetch>()

beforeEach(() => {
  resetSessionForTests()
  localStorage.clear()
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  fetchMock.mockReset()
  vi.unstubAllGlobals()
})

function tokenResponse(access: string, refresh: string): Response {
  return new Response(JSON.stringify({ accessToken: access, refreshToken: refresh }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('session storage', () => {
  it('keeps the access token in memory and the refresh token in localStorage', () => {
    setSession({ accessToken: 'a1', refreshToken: 'r1' })
    expect(getAccessToken()).toBe('a1')
    expect(getRefreshToken()).toBe('r1')
    clearSession()
    expect(getAccessToken()).toBeNull()
    expect(getRefreshToken()).toBeNull()
  })
})

describe('refreshOnce', () => {
  it('returns false without a stored refresh token and never calls fetch', async () => {
    expect(await refreshOnce()).toBe(false)
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('shares one flight between concurrent callers (single-use token safety)', async () => {
    setSession({ accessToken: 'old', refreshToken: 'r1' })
    let resolveFetch: ((response: Response) => void) | undefined
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>((resolve) => {
          resolveFetch = resolve
        }),
    )
    const first = refreshOnce()
    const second = refreshOnce()
    expect(fetchMock).toHaveBeenCalledTimes(1)
    resolveFetch?.(tokenResponse('a2', 'r2'))
    expect(await first).toBe(true)
    expect(await second).toBe(true)
    expect(getAccessToken()).toBe('a2')
    expect(getRefreshToken()).toBe('r2')
  })

  it('sends the stored refresh token to /auth/refresh', async () => {
    setSession({ accessToken: 'old', refreshToken: 'r1' })
    fetchMock.mockResolvedValue(tokenResponse('a2', 'r2'))
    await refreshOnce()
    const [url, init] = fetchMock.mock.calls[0] ?? []
    expect(url).toBe('/api/v1/auth/refresh')
    expect(init?.method).toBe('POST')
    expect(init?.body).toBe(JSON.stringify({ refreshToken: 'r1' }))
  })

  it('clears the session and notifies listeners when the refresh is rejected', async () => {
    setSession({ accessToken: 'old', refreshToken: 'stolen' })
    fetchMock.mockResolvedValue(
      new Response(JSON.stringify({ code: 'AUTH_REFRESH_TOKEN_REUSED', message: 'reused' }), {
        status: 401,
      }),
    )
    const listener = vi.fn()
    onSessionExpired(listener)
    expect(await refreshOnce()).toBe(false)
    expect(getAccessToken()).toBeNull()
    expect(getRefreshToken()).toBeNull()
    expect(listener).toHaveBeenCalledTimes(1)
  })

  it('keeps the session on a network failure so the next request can retry', async () => {
    setSession({ accessToken: 'old', refreshToken: 'r1' })
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'))
    expect(await refreshOnce()).toBe(false)
    expect(getRefreshToken()).toBe('r1')
  })
})
