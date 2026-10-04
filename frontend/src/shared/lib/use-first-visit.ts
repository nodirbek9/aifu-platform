import { useEffect, useState } from 'react'

/**
 * True on the first visit of a page in this browser session, false on every later one (and on
 * every refetch): what an entrance animation needs so it plays once, not each time.
 */
export function useFirstVisit(key: string): boolean {
  const storageKey = `aifu-visited:${key}`
  const [first] = useState(() => {
    try {
      return sessionStorage.getItem(storageKey) === null
    } catch {
      return false
    }
  })
  useEffect(() => {
    try {
      sessionStorage.setItem(storageKey, '1')
    } catch {
      /* storage unavailable: the entrance just plays again next time */
    }
  }, [storageKey])
  return first
}

/** The entrance of the n-th block of a page, in turn (written out whole for Tailwind to find). */
export const RISE = [
  'animate-rise-in',
  'animate-rise-in rise-step-1',
  'animate-rise-in rise-step-2',
  'animate-rise-in rise-step-3',
  'animate-rise-in rise-step-4',
] as const

/** The entrance class of block `step`, or nothing when the page is not new to this session. */
export function riseIn(first: boolean, step: number): string | undefined {
  return first ? RISE[Math.min(step, RISE.length - 1)] : undefined
}
