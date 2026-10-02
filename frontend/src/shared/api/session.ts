import type { components } from './schema'

type TokenResponse = components['schemas']['TokenResponse']

const REFRESH_TOKEN_KEY = 'aifu-refresh-token'

let accessToken: string | null = null
let refreshing: Promise<boolean> | null = null

type SessionExpiredListener = () => void
const sessionExpiredListeners = new Set<SessionExpiredListener>()

export function getAccessToken(): string | null {
  return accessToken
}

export function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  } catch {
    return null
  }
}

export function setSession(tokens: { accessToken: string; refreshToken: string }): void {
  accessToken = tokens.accessToken
  try {
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
  } catch {
    /* storage unavailable — session lives until the tab closes */
  }
}

export function clearSession(): void {
  accessToken = null
  try {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  } catch {
    /* ignore */
  }
}

/** Subscribe to "the session is unrecoverable" (refresh rejected). Returns an unsubscribe. */
export function onSessionExpired(listener: SessionExpiredListener): () => void {
  sessionExpiredListeners.add(listener)
  return () => {
    sessionExpiredListeners.delete(listener)
  }
}

function emitSessionExpired(): void {
  for (const listener of sessionExpiredListeners) listener()
}

function isTokenResponse(
  value: unknown,
): value is Required<Pick<TokenResponse, 'accessToken' | 'refreshToken'>> {
  if (typeof value !== 'object' || value === null) return false
  const candidate = value as Record<string, unknown>
  return typeof candidate.accessToken === 'string' && typeof candidate.refreshToken === 'string'
}

/**
 * Exchanges the stored refresh token for a new pair. The refresh token is single use,
 * so every concurrent caller shares this one flight; across tabs the Web Locks API
 * serialises the exchange and the stored token is re-read inside the lock.
 */
export function refreshOnce(): Promise<boolean> {
  refreshing ??= runRefresh().finally(() => {
    refreshing = null
  })
  return refreshing
}

async function runRefresh(): Promise<boolean> {
  if (typeof navigator !== 'undefined' && 'locks' in navigator) {
    return navigator.locks.request('aifu-token-refresh', doRefresh)
  }
  return doRefresh()
}

async function doRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (refreshToken === null) return false
  let response: Response
  try {
    response = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
  } catch {
    // Network failure: keep the session, the next request will try again.
    return false
  }
  if (!response.ok) {
    clearSession()
    emitSessionExpired()
    return false
  }
  const body: unknown = await response.json()
  if (!isTokenResponse(body)) {
    clearSession()
    emitSessionExpired()
    return false
  }
  setSession({ accessToken: body.accessToken, refreshToken: body.refreshToken })
  return true
}

/** Test-only: reset the in-memory state between tests. */
export function resetSessionForTests(): void {
  accessToken = null
  refreshing = null
  sessionExpiredListeners.clear()
}
