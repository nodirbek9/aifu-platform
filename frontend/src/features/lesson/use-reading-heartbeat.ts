import { useEffect, useRef } from 'react'
import { useRecordReadingBeat } from './queries'

export type ReadingHeartbeatOptions = {
  courseId: string
  lessonId: string
  pathEnabled: boolean
  lectureDone: boolean
  sentinelRef?: React.RefObject<HTMLElement | null>
}

const HEARTBEAT_INTERVAL_MS = 15_000
const ACTIVE_THRESHOLD_MS = 120_000

/**
 * Tracks reading engagement in a lesson and sends periodic reading heartbeats to the API.
 * Follows ADR-015 anti-cheat rules:
 * - Active tab (visibilityState === 'visible')
 * - Active user within 120 s (pointer, keyboard, scroll)
 * - Tracks maxBlockPosition via [data-block-position]
 * - Tracks atEnd via sentinel intersection
 * - 15 s cadence, stops when lecture is DONE
 * - Flushes on visibilitychange to 'hidden'
 */
export function useReadingHeartbeat({
  courseId,
  lessonId,
  pathEnabled,
  lectureDone,
  sentinelRef,
}: ReadingHeartbeatOptions) {
  const recordBeat = useRecordReadingBeat(courseId, lessonId)
  const maxBlockPositionRef = useRef<number>(0)
  const atEndRef = useRef<boolean>(false)
  const lastActiveAtRef = useRef<number>(0)

  // Mark user active on interaction
  useEffect(() => {
    if (!pathEnabled || lectureDone) return
    lastActiveAtRef.current = Date.now()

    function onActivity() {
      lastActiveAtRef.current = Date.now()
    }

    const events = ['pointerdown', 'pointermove', 'keydown', 'scroll'] as const
    for (const event of events) {
      window.addEventListener(event, onActivity, { passive: true })
    }

    return () => {
      for (const event of events) {
        window.removeEventListener(event, onActivity)
      }
    }
  }, [pathEnabled, lectureDone])

  // Track content block positions and sentinel
  useEffect(() => {
    if (!pathEnabled || lectureDone) return
    if (typeof IntersectionObserver === 'undefined') return

    const blockObserver = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting || entry.boundingClientRect.top < window.innerHeight) {
          const rawPos = entry.target.getAttribute('data-block-position')
          if (rawPos !== null) {
            const pos = parseInt(rawPos, 10)
            if (!Number.isNaN(pos) && pos > maxBlockPositionRef.current) {
              maxBlockPositionRef.current = pos
            }
          }
        }
      }
    })

    const blocks = document.querySelectorAll('[data-block-position]')
    for (const block of blocks) {
      blockObserver.observe(block)
    }

    let sentinelObserver: IntersectionObserver | null = null
    const sentinelEl = sentinelRef?.current
    if (sentinelEl) {
      sentinelObserver = new IntersectionObserver((entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            atEndRef.current = true
          }
        }
      })
      sentinelObserver.observe(sentinelEl)
    }

    return () => {
      blockObserver.disconnect()
      sentinelObserver?.disconnect()
    }
  }, [pathEnabled, lectureDone, sentinelRef])

  // 15 s timer and visibilitychange flush
  useEffect(() => {
    if (!pathEnabled || lectureDone) return

    function isUserActive(): boolean {
      return Date.now() - lastActiveAtRef.current < ACTIVE_THRESHOLD_MS
    }

    function isVisible(): boolean {
      return typeof document !== 'undefined' && document.visibilityState === 'visible'
    }

    function sendBeat() {
      if (!isUserActive()) return
      recordBeat.mutate({
        maxBlockPosition: maxBlockPositionRef.current,
        atEnd: atEndRef.current,
      })
    }

    const intervalId = window.setInterval(() => {
      if (isVisible()) {
        sendBeat()
      }
    }, HEARTBEAT_INTERVAL_MS)

    function onVisibilityChange() {
      if (document.visibilityState === 'hidden') {
        sendBeat()
      }
    }

    document.addEventListener('visibilitychange', onVisibilityChange)

    return () => {
      window.clearInterval(intervalId)
      document.removeEventListener('visibilitychange', onVisibilityChange)
    }
  }, [pathEnabled, lectureDone, recordBeat])

  return {
    getMaxBlockPosition: () => maxBlockPositionRef.current,
    isAtEnd: () => atEndRef.current,
  }
}
