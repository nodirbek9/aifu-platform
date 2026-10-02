import { describe, expect, it } from 'vitest'
import {
  classKey,
  classMoment,
  currentOrNext,
  isHeld,
  narrowWeek,
  nowMark,
  shownDays,
  slotOf,
  type ClassItem,
} from './model'

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

describe('a week as the grid shows it', () => {
  const slots = [
    { pairNo: 1, startsAt: '08:30:00', endsAt: '09:50:00' },
    { pairNo: 2, startsAt: '10:00:00', endsAt: '11:20:00' },
  ]
  const week = narrowWeek({
    weekStart: '2026-09-28',
    weekEnd: '2026-10-04',
    weekNumber: 5,
    parity: 'ODD',
    slots: [...slots].reverse(),
    days: [
      { date: '2026-10-04', classes: [] },
      {
        date: '2026-10-02',
        classes: [
          {
            entryId: 'a',
            date: '2026-10-02',
            startsAt: '08:30:00',
            endsAt: '09:50:00',
            sessionType: 'PRACTICAL',
            pairNo: 1,
          },
        ],
      },
      { date: '2026-10-01', holiday: 'Bayram', classes: [] },
    ],
  })

  it('orders days and pairs, and takes the parity from the server', () => {
    expect(week.days.map((day) => day.date)).toEqual(['2026-10-01', '2026-10-02', '2026-10-04'])
    expect(week.slots.map((slot) => slot.pairNo)).toEqual([1, 2])
    expect(week.parity).toBe('ODD')
    expect(narrowWeek({ parity: 'EVERY' }).parity).toBeUndefined()
  })

  it('leaves Sunday out unless a class falls on it', () => {
    expect(shownDays(week).map((day) => day.date)).toEqual(['2026-10-01', '2026-10-02'])
    const withSunday = narrowWeek({
      days: [
        {
          date: '2026-10-04',
          classes: [
            {
              entryId: 'b',
              date: '2026-10-04',
              startsAt: '08:30:00',
              endsAt: '09:50:00',
              sessionType: 'LECTURE',
            },
          ],
        },
      ],
    })
    expect(shownDays(withSunday)).toHaveLength(1)
  })

  it('finds the pair of a class by its number, else by its time', () => {
    expect(slotOf({ pairNo: 2, startsAt: '10:00:00' }, slots)?.pairNo).toBe(2)
    expect(slotOf({ startsAt: '10:00:00' }, slots)?.pairNo).toBe(2)
    expect(slotOf({ startsAt: '09:00:00' }, slots)?.pairNo).toBe(1)
  })

  it('draws the "now" line in the pair going on, at the height the clock is at', () => {
    expect(nowMark(week, { date: '2026-10-02', time: '09:10:00' })).toEqual({
      date: '2026-10-02',
      pairNo: 1,
      progress: 0.5,
    })
  })

  it('puts the line at the top of the next pair during a break', () => {
    expect(nowMark(week, { date: '2026-10-02', time: '09:55:00' })).toEqual({
      date: '2026-10-02',
      pairNo: 2,
      progress: 0,
    })
  })

  it('draws no line before the first bell, after the last, or in another week', () => {
    expect(nowMark(week, { date: '2026-10-02', time: '07:00:00' })).toBeUndefined()
    expect(nowMark(week, { date: '2026-10-02', time: '18:00:00' })).toBeUndefined()
    expect(nowMark(week, { date: '2026-10-09', time: '09:10:00' })).toBeUndefined()
  })

  it('tells the old slot of a moved class from its new one', () => {
    const moved = lesson('08:30:00', '09:50:00', 'MOVED_OUT')
    expect(classKey(moved)).not.toBe(classKey({ ...moved, status: 'MOVED_IN', date: '2026-10-03' }))
  })
})
