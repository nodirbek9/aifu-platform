import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { authFetch } from './client'
import { resetSessionForTests, setSession } from './session'

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

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('authFetch', () => {
  it('attaches the access token when there is one', async () => {
    setSession({ accessToken: 'a1', refreshToken: 'r1' })
    fetchMock.mockResolvedValue(json({ ok: true }, 200))
    await authFetch('http://localhost/api/v1/auth/me')
    const request = fetchMock.mock.calls[0]?.[0]
    expect(request).toBeInstanceOf(Request)
    if (request instanceof Request) {
      expect(request.headers.get('Authorization')).toBe('Bearer a1')
    }
  })

  it('sends no Authorization header when signed out', async () => {
    fetchMock.mockResolvedValue(json({ ok: true }, 200))
    await authFetch('http://localhost/api/v1/auth/me')
    const request = fetchMock.mock.calls[0]?.[0]
    if (request instanceof Request) {
      expect(request.headers.get('Authorization')).toBeNull()
    }
  })

  it('on AUTH_TOKEN_EXPIRED refreshes once and retries the original request with the new token', async () => {
    setSession({ accessToken: 'expired', refreshToken: 'r1' })
    fetchMock
      .mockResolvedValueOnce(json({ code: 'AUTH_TOKEN_EXPIRED', message: 'expired' }, 401))
      .mockResolvedValueOnce(json({ accessToken: 'fresh', refreshToken: 'r2' }, 200))
      .mockResolvedValueOnce(json({ ok: true }, 200))
    const response = await authFetch('http://localhost/api/v1/auth/me', {
      method: 'POST',
      body: JSON.stringify({ x: 1 }),
    })
    expect(response.status).toBe(200)
    expect(fetchMock).toHaveBeenCalledTimes(3)
    const retried = fetchMock.mock.calls[2]?.[0]
    if (retried instanceof Request) {
      expect(retried.headers.get('Authorization')).toBe('Bearer fresh')
      expect(await retried.text()).toBe(JSON.stringify({ x: 1 }))
    }
  })

  it('passes a non-expiry 401 through untouched', async () => {
    setSession({ accessToken: 'a1', refreshToken: 'r1' })
    fetchMock.mockResolvedValue(json({ code: 'AUTH_INVALID_CREDENTIALS', message: 'no' }, 401))
    const response = await authFetch('http://localhost/api/v1/auth/login', { method: 'POST', body: '{}' })
    expect(response.status).toBe(401)
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
})
