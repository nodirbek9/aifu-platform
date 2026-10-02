import { useEffect, useState } from 'react'
import { remainingSeconds, type Attempt } from './model'

export type Clocked = Pick<Attempt, 'expiresAt' | 'clockOffsetMs'>

/** Seconds left on the server's clock, re-read four times a second; `undefined` without a limit. */
export function useRemainingSeconds(attempt: Clocked): number | undefined {
  const [now, setNow] = useState(() => Date.now())
  const timed = attempt.expiresAt !== undefined
  useEffect(() => {
    if (!timed) return
    const timer = window.setInterval(() => {
      setNow(Date.now())
    }, 250)
    return () => {
      window.clearInterval(timer)
    }
  }, [timed])
  return remainingSeconds(attempt, now)
}
