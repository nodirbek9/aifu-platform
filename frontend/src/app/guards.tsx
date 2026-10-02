import { useEffect } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { readLoginLocationState, type LoginLocationState } from '@/features/auth/login-state'
import { roleHomePath, type Role } from '@/features/auth/model'
import { AppShell } from './shell/app-shell'
import { AppSplash, NoAccessPage } from './system-pages'

/**
 * Everything behind the login. Signed out → the login page, remembering where the user was
 * going (not after a deliberate sign-out: the next person starts at their own home).
 * A first login must change the password before anything else (spec G-08) — the server does
 * not enforce that, so this guard does.
 */
export function RequireAuth() {
  const { state } = useAuth()
  const location = useLocation()
  if (state.status === 'unknown') return <AppSplash />
  if (state.status === 'anonymous') {
    const loginState: LoginLocationState = {}
    if (state.reason !== 'signedOut') {
      loginState.from = { pathname: location.pathname, search: location.search }
    }
    if (state.reason === 'expired') loginState.reason = 'expired'
    return <Navigate to="/login" replace state={loginState} />
  }
  if (state.user.mustChangePassword && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />
  }
  return <Outlet />
}

/** The login page is for the signed-out; anyone signed in is sent on to where they were going. */
export function PublicOnly() {
  const { state } = useAuth()
  const location = useLocation()
  if (state.status === 'unknown') return <AppSplash />
  if (state.status === 'authenticated') {
    if (state.user.mustChangePassword) return <Navigate to="/change-password" replace />
    const { from } = readLoginLocationState(location.state)
    return <Navigate to={from !== undefined ? `${from.pathname}${from.search}` : '/'} replace />
  }
  return <Outlet />
}

/** The signed-in frame. A user without any role has nothing to open. */
export function ShellRoute() {
  const { state, activeRole } = useAuth()
  if (state.status !== 'authenticated') return null
  if (activeRole === null) return <NoAccessPage />
  return <AppShell user={state.user} activeRole={activeRole} />
}

/** "/" is the home of the role the user last worked in. */
export function RoleHomeRedirect() {
  const { activeRole } = useAuth()
  if (activeRole === null) return <NoAccessPage />
  return <Navigate to={roleHomePath(activeRole)} replace />
}

/**
 * A section of one role (spec G-09). Another role's address shows "no access", never a crash;
 * opening a section of another role the user does hold makes that the role they work in.
 */
export function RequireRole({ role }: { role: Role }) {
  const { state, activeRole, setActiveRole } = useAuth()
  const allowed = state.status === 'authenticated' && state.user.roles.includes(role)
  useEffect(() => {
    if (allowed && activeRole !== role) setActiveRole(role)
  }, [allowed, activeRole, role, setActiveRole])
  return allowed ? <Outlet /> : <NoAccessPage />
}
