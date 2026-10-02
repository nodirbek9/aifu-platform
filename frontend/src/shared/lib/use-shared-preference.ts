import { useCallback, useSyncExternalStore } from 'react'

const listeners = new Set<() => void>()

function subscribe(listener: () => void): () => void {
  listeners.add(listener)
  window.addEventListener('storage', listener)
  return () => {
    listeners.delete(listener)
    window.removeEventListener('storage', listener)
  }
}

function read(key: string): string | null {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

/**
 * A string preference of this browser that every component reading it shares: choosing the
 * code language in one lesson example changes all of them, in every open tab. Kept in
 * localStorage under the given key.
 */
export function useSharedPreference(key: string): [string | null, (value: string) => void] {
  const value = useSyncExternalStore(subscribe, () => read(key))
  const update = useCallback(
    (next: string) => {
      try {
        localStorage.setItem(key, next)
      } catch {
        /* storage unavailable — nothing to share, nothing to remember */
      }
      for (const listener of listeners) listener()
    },
    [key],
  )
  return [value, update]
}
