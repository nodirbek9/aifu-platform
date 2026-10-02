/** What a redirect to the login page carries: where to return, and why the user is here. */
export type LoginLocationState = {
  from?: { pathname: string; search: string }
  reason?: 'expired'
  passwordChanged?: boolean
}

export function readLoginLocationState(value: unknown): LoginLocationState {
  if (typeof value !== 'object' || value === null) return {}
  const state: LoginLocationState = {}
  if ('reason' in value && value.reason === 'expired') state.reason = 'expired'
  if ('passwordChanged' in value && value.passwordChanged === true) state.passwordChanged = true
  if ('from' in value && typeof value.from === 'object' && value.from !== null) {
    const from = value.from
    if ('pathname' in from && typeof from.pathname === 'string') {
      state.from = {
        pathname: from.pathname,
        search: 'search' in from && typeof from.search === 'string' ? from.search : '',
      }
    }
  }
  return state
}
