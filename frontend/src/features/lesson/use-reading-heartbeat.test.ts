import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useReadingHeartbeat } from './use-reading-heartbeat'
import * as queries from './queries'

describe('useReadingHeartbeat', () => {
  const mutateMock = vi.fn()

  beforeEach(() => {
    vi.useFakeTimers()
    vi.spyOn(queries, 'useRecordReadingBeat').mockReturnValue({
      mutate: mutateMock,
    } as unknown as ReturnType<typeof queries.useRecordReadingBeat>)
    mutateMock.mockClear()
  })

  afterEach(() => {
    vi.clearAllTimers()
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('does not send beats when pathEnabled is false', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: false,
        lectureDone: false,
      }),
    )

    vi.advanceTimersByTime(30_000)
    expect(mutateMock).not.toHaveBeenCalled()
  })

  it('does not send beats when lecture is already DONE', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: true,
        lectureDone: true,
      }),
    )

    vi.advanceTimersByTime(30_000)
    expect(mutateMock).not.toHaveBeenCalled()
  })

  it('sends heartbeat after 15 seconds when pathEnabled, not done, and active', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: true,
        lectureDone: false,
      }),
    )

    expect(mutateMock).not.toHaveBeenCalled()

    vi.advanceTimersByTime(15_000)
    expect(mutateMock).toHaveBeenCalledTimes(1)
    expect(mutateMock).toHaveBeenCalledWith({
      maxBlockPosition: 0,
      atEnd: false,
    })

    vi.advanceTimersByTime(15_000)
    expect(mutateMock).toHaveBeenCalledTimes(2)
  })

  it('does not send heartbeat if inactive for more than 120 seconds', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: true,
        lectureDone: false,
      }),
    )

    // Advance 130s without activity
    vi.advanceTimersByTime(130_000)
    mutateMock.mockClear()

    // Next 15s interval: user has been inactive for > 120s
    vi.advanceTimersByTime(15_000)
    expect(mutateMock).not.toHaveBeenCalled()
  })

  it('resets inactivity timer on user events', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: true,
        lectureDone: false,
      }),
    )

    // Wait 100 seconds
    vi.advanceTimersByTime(100_000)
    mutateMock.mockClear()

    // Trigger user activity
    window.dispatchEvent(new Event('pointerdown'))

    // Advance another 30 seconds (total 130s since start, but only 30s since interaction)
    vi.advanceTimersByTime(30_000)
    expect(mutateMock).toHaveBeenCalled()
  })

  it('flushes beat immediately on visibilitychange to hidden if active', () => {
    renderHook(() =>
      useReadingHeartbeat({
        courseId: 'course-1',
        lessonId: 'lesson-1',
        pathEnabled: true,
        lectureDone: false,
      }),
    )

    // Mock document.visibilityState
    Object.defineProperty(document, 'visibilityState', {
      value: 'hidden',
      writable: true,
      configurable: true,
    })

    document.dispatchEvent(new Event('visibilitychange'))
    expect(mutateMock).toHaveBeenCalledTimes(1)
  })
})
