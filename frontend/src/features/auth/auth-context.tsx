import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, unwrap } from '@/shared/api/client'
import {
  clearSession,
  getRefreshToken,
  onSessionExpired,
  refreshOnce,
  setSession,
} from '@/shared/api/session'
import { queryClient } from '@/shared/api/query-client'
import { applyLocale, type Locale } from '@/shared/i18n'
import { defaultRole, isRole, narrowUser, type CurrentUser, type Role } from './model'

export type AuthState =
  | { status: 'unknown' }
  | { status: 'anonymous'; reason?: 'expired' | 'signedOut' | 'passwordChanged' }
  | { status: 'authenticated'; user: CurrentUser }

type AuthContextValue = {
  state: AuthState
  /** Signs in and returns the user; throws ApiProblem on failure. */
  signIn: (identifier: string, password: string) => Promise<CurrentUser>
  signOut: () => Promise<void>
  signOutAll: () => Promise<void>
  /** Drops the session on this device only — after the server revoked it (password change). */
  forgetSession: (reason?: 'signedOut' | 'passwordChanged') => void
  activeRole: Role | null
  setActiveRole: (role: Role) => void
  changeLocale: (locale: Locale) => void
  /** Saves my phone number; the user of the session is replaced by the server's answer. */
  updatePhone: (phone: string) => Promise<void>
  clearExpiredNotice: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

const ACTIVE_ROLE_KEY = 'aifu-active-role'

function readStoredRole(): Role | null {
  try {
    const stored = localStorage.getItem(ACTIVE_ROLE_KEY)
    return isRole(stored) ? stored : null
  } catch {
    return null
  }
}

async function fetchMe(): Promise<CurrentUser> {
  return narrowUser(await unwrap(api.GET('/api/v1/auth/me')))
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'unknown' })
  const [activeRole, setActiveRoleState] = useState<Role | null>(null)

  const pickRole = useCallback((user: CurrentUser): Role | null => {
    const stored = readStoredRole()
    const role = stored !== null && user.roles.includes(stored) ? stored : defaultRole(user.roles)
    setActiveRoleState(role)
    return role
  }, [])

  useEffect(() => {
    let cancelled = false
    const boot = async () => {
      if (getRefreshToken() === null) {
        if (!cancelled) setState({ status: 'anonymous' })
        return
      }
      const refreshed = await refreshOnce()
      if (cancelled) return
      if (!refreshed) {
        setState({ status: 'anonymous' })
        return
      }
      try {
        const user = await fetchMe()
        if (cancelled) return
        if (user.preferredLocale !== null) applyLocale(user.preferredLocale)
        pickRole(user)
        setState({ status: 'authenticated', user })
      } catch {
        if (!cancelled) setState({ status: 'anonymous' })
      }
    }
    void boot()
    return () => {
      cancelled = true
    }
  }, [pickRole])

  useEffect(() => {
    return onSessionExpired(() => {
      queryClient.clear()
      setActiveRoleState(null)
      setState({ status: 'anonymous', reason: 'expired' })
    })
  }, [])

  const signIn = useCallback(
    async (identifier: string, password: string): Promise<CurrentUser> => {
      const tokens = await unwrap(api.POST('/api/v1/auth/login', { body: { identifier, password } }))
      if (typeof tokens.accessToken !== 'string' || typeof tokens.refreshToken !== 'string') {
        throw new Error('Login response is missing tokens')
      }
      setSession({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken })
      const user = await fetchMe()
      if (user.preferredLocale !== null) applyLocale(user.preferredLocale)
      pickRole(user)
      setState({ status: 'authenticated', user })
      return user
    },
    [pickRole],
  )

  const localSignOut = useCallback((reason: 'signedOut' | 'passwordChanged' = 'signedOut') => {
    clearSession()
    queryClient.clear()
    setActiveRoleState(null)
    try {
      localStorage.removeItem(ACTIVE_ROLE_KEY)
    } catch {
      /* ignore */
    }
    setState({ status: 'anonymous', reason })
  }, [])

  const signOut = useCallback(async () => {
    const refreshToken = getRefreshToken()
    if (refreshToken !== null) {
      try {
        await api.POST('/api/v1/auth/logout', { body: { refreshToken } })
      } catch {
        /* best effort — local cleanup happens regardless */
      }
    }
    localSignOut()
  }, [localSignOut])

  const signOutAll = useCallback(async () => {
    try {
      await api.POST('/api/v1/auth/logout-all')
    } catch {
      /* best effort */
    }
    localSignOut()
  }, [localSignOut])

  const setActiveRole = useCallback((role: Role) => {
    setActiveRoleState(role)
    try {
      localStorage.setItem(ACTIVE_ROLE_KEY, role)
    } catch {
      /* ignore */
    }
  }, [])

  const changeLocale = useCallback(
    (locale: Locale) => {
      applyLocale(locale)
      if (state.status === 'authenticated') {
        void api.PATCH('/api/v1/auth/me', { body: { preferredLocale: locale } }).catch(() => undefined)
      }
    },
    [state],
  )

  const updatePhone = useCallback(async (phone: string) => {
    const user = narrowUser(await unwrap(api.PATCH('/api/v1/auth/me', { body: { phone } })))
    setState({ status: 'authenticated', user })
  }, [])

  const clearExpiredNotice = useCallback(() => {
    setState((current) => (current.status === 'anonymous' ? { status: 'anonymous' } : current))
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      state,
      signIn,
      signOut,
      signOutAll,
      forgetSession: localSignOut,
      activeRole,
      setActiveRole,
      changeLocale,
      updatePhone,
      clearExpiredNotice,
    }),
    [
      state,
      signIn,
      signOut,
      signOutAll,
      localSignOut,
      activeRole,
      setActiveRole,
      changeLocale,
      updatePhone,
      clearExpiredNotice,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (value === null) throw new Error('useAuth must be used inside AuthProvider')
  return value
}
