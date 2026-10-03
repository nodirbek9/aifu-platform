import { useEffect, useRef, useState } from 'react'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'

const PAUSE_MS = 800

export type SaveState =
  { kind: 'saved' } | { kind: 'pending' } | { kind: 'saving' } | { kind: 'failed'; problem: ApiProblem }

/**
 * Saves what is typed after a pause: `change(value)` on every edit, `flush()` to save at once.
 * Edits are saved in order, the latest last; leaving the page saves what is still waiting.
 */
export function useAutosave<T>(save: (value: T) => Promise<unknown>) {
  const [state, setState] = useState<SaveState>({ kind: 'saved' })
  const timer = useRef<number | undefined>(undefined)
  const waiting = useRef<{ value: T } | null>(null)
  const chain = useRef<Promise<unknown>>(Promise.resolve())
  const saveRef = useRef(save)
  useEffect(() => {
    saveRef.current = save
  })

  const run = () => {
    window.clearTimeout(timer.current)
    timer.current = undefined
    const next = waiting.current
    if (next === null) return
    waiting.current = null
    setState({ kind: 'saving' })
    chain.current = chain.current.then(async () => {
      try {
        await saveRef.current(next.value)
        if (waiting.current === null && timer.current === undefined) setState({ kind: 'saved' })
      } catch (failure) {
        setState({ kind: 'failed', problem: toApiProblem(failure) })
      }
    })
  }

  const change = (value: T) => {
    waiting.current = { value }
    setState({ kind: 'pending' })
    window.clearTimeout(timer.current)
    timer.current = window.setTimeout(run, PAUSE_MS)
  }

  useEffect(
    () => () => {
      if (waiting.current !== null) {
        window.clearTimeout(timer.current)
        void saveRef.current(waiting.current.value)
      }
    },
    [],
  )

  return { state, change, flush: run }
}
