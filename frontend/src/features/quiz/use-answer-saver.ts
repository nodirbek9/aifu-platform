import { useCallback, useEffect, useRef, useState } from 'react'
import { toApiProblem } from '@/shared/api/errors'
import type { Answer } from './model'
import { saveAnswer } from './queries'

export type SaveStatus = 'idle' | 'saving' | 'saved' | 'error'

const TYPING_PAUSE_MS = 600

/**
 * Saves answers as the student goes. One request at a time per question and the latest value
 * wins, so a slow network can never write an older answer over a newer one. An answer that
 * could not be saved stays queued: `retry()` and `flush()` send it again.
 */
export function useAnswerSaver(attemptId: string, onAttemptClosed: () => void) {
  /** The newest value not yet accepted by the server, per question. */
  const queued = useRef(new Map<string, Answer>())
  const sending = useRef(new Map<string, Promise<void>>())
  const timers = useRef(new Map<string, number>())
  const failed = useRef(new Set<string>())
  const everSaved = useRef(false)
  const closed = useRef(onAttemptClosed)
  const [status, setStatus] = useState<SaveStatus>('idle')

  useEffect(() => {
    closed.current = onAttemptClosed
  }, [onAttemptClosed])

  const publish = useCallback(() => {
    if (failed.current.size > 0) setStatus('error')
    else if (queued.current.size > 0 || sending.current.size > 0) setStatus('saving')
    else setStatus(everSaved.current ? 'saved' : 'idle')
  }, [])

  const send = useCallback(
    (questionId: string): Promise<void> => {
      const running = sending.current.get(questionId)
      if (running !== undefined) return running
      const work = (async () => {
        for (;;) {
          const answer = queued.current.get(questionId)
          if (answer === undefined) return
          queued.current.delete(questionId)
          try {
            await saveAnswer(attemptId, questionId, answer)
            failed.current.delete(questionId)
            everSaved.current = true
          } catch (error) {
            const { code } = toApiProblem(error)
            if (code === 'ATTEMPT_EXPIRED' || code === 'ATTEMPT_ALREADY_SUBMITTED') {
              // the attempt is over on the server: nothing more can be saved into it
              queued.current.clear()
              failed.current.clear()
              closed.current()
              return
            }
            // keep it for a retry, unless the student has answered again meanwhile
            if (!queued.current.has(questionId)) queued.current.set(questionId, answer)
            failed.current.add(questionId)
            return
          }
        }
      })().finally(() => {
        sending.current.delete(questionId)
        publish()
      })
      sending.current.set(questionId, work)
      publish()
      return work
    },
    [attemptId, publish],
  )

  /** Queues an answer. A choice goes at once; typing waits for a pause (`settled: false`). */
  const save = useCallback(
    (questionId: string, answer: Answer, settled: boolean) => {
      queued.current.set(questionId, answer)
      failed.current.delete(questionId)
      const timer = timers.current.get(questionId)
      if (timer !== undefined) window.clearTimeout(timer)
      timers.current.delete(questionId)
      if (settled) {
        void send(questionId)
      } else {
        timers.current.set(
          questionId,
          window.setTimeout(() => {
            timers.current.delete(questionId)
            void send(questionId)
          }, TYPING_PAUSE_MS),
        )
        publish()
      }
    },
    [send, publish],
  )

  /** Sends everything still waiting and tells whether all of it is now saved. */
  const flush = useCallback(async (): Promise<boolean> => {
    for (const timer of timers.current.values()) window.clearTimeout(timer)
    timers.current.clear()
    failed.current.clear()
    await Promise.all([...queued.current.keys()].map((questionId) => send(questionId)))
    await Promise.all([...sending.current.values()])
    publish()
    return failed.current.size === 0 && queued.current.size === 0
  }, [send, publish])

  // leaving the page mid-typing: what waits for a pause is sent now
  useEffect(() => {
    const pending = timers.current
    return () => {
      for (const [questionId, timer] of pending) {
        window.clearTimeout(timer)
        void send(questionId)
      }
      pending.clear()
    }
  }, [send])

  return { status, save, flush, retry: flush }
}
