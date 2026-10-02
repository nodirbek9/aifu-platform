import { useEffect, useState } from 'react'

/** The value as it was `delayMs` ago, unless it changed since: what a typed query settles into. */
export function useDebouncedValue<T>(value: T, delayMs: number): T {
  const [settled, setSettled] = useState(value)
  useEffect(() => {
    const timer = window.setTimeout(() => {
      setSettled(value)
    }, delayMs)
    return () => {
      window.clearTimeout(timer)
    }
  }, [value, delayMs])
  return settled
}
