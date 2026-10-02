import { describe, expect, it } from 'vitest'
import { classMoment, currentOrNext, isHeld, type ClassItem } from './model'

function lesson(startsAt: string, endsAt: string, status: ClassItem['status'] = 'SCHEDULED'): ClassItem {
  return {
    entryId: `${startsAt}-${status}`,
    date: '2026-10-02',
    startsAt,
    endsAt,
    courseTitle: 'Dasturlash',
    sessionType: 'PRACTICAL',
    groups: ['BK-DI26/1'],
    status,
  }
}

describe('where a class stands against the campus clock', () => {
  const first = lesson('08:30:00', '09:50:00')

  it('is going on from its first minute to its last', () => {
    expect(classMoment(first, { date: '2026-10-02', time: '08:30:00' })).toBe('now')
    expect(classMoment(first, { date: '2026-10-02', time: '09:49:59' })).toBe('now')
  })

  it('is over at the bell and ahead before it', () => {
    expect(classMoment(first, { date: '2026-10-02', time: '09:50:00' })).toBe('past')
    expect(classMoment(first, { date: '2026-10-02', time: '08:29:59' })).toBe('upcoming')
  })

  it('follows the date before the time', () => {
    expect(classMoment(first, { date: '2026-10-01', time: '23:00:00' })).toBe('upcoming')
    expect(classMoment(first, { date: '2026-10-03', time: '00:00:01' })).toBe('past')
  })
})

describe('the class to point at', () => {
  const classes = [
    lesson('08:30:00', '09:50:00'),
    lesson('10:00:00', '11:20:00'),
    lesson('11:30:00', '12:50:00'),
  ]

  it('is the one going on', () => {
    expect(currentOrNext(classes, { date: '2026-10-02', time: '10:15:00' })?.startsAt).toBe('10:00:00')
  })

  it('is the next one during a break', () => {
    expect(currentOrNext(classes, { date: '2026-10-02', time: '09:55:00' })?.startsAt).toBe('10:00:00')
  })

  it('is nothing once the day is over', () => {
    expect(currentOrNext(classes, { date: '2026-10-02', time: '13:00:00' })).toBeUndefined()
  })

  it('never points at a class that is not held', () => {
    const day = [lesson('08:30:00', '09:50:00', 'CANCELLED'), lesson('10:00:00', '11:20:00', 'MOVED_IN')]
    expect(currentOrNext(day, { date: '2026-10-02', time: '08:40:00' })?.startsAt).toBe('10:00:00')
    expect(isHeld('HOLIDAY')).toBe(false)
    expect(isHeld('MOVED_OUT')).toBe(false)
    expect(isHeld('REPLACED')).toBe(true)
  })
})
