import { describe, expect, it } from 'vitest'
import {
  campusNow,
  formatDate,
  formatDateTime,
  formatLongDay,
  formatRelativeTime,
  formatTimeOfDay,
  formatDateRange,
  formatWeekdayShort,
  shiftDate,
  weekdayOf,
} from './dates'

describe('campus time', () => {
  it('reads an instant on the campus clock (UTC+5), whatever the machine is set to', () => {
    expect(campusNow(new Date('2026-10-02T03:30:00Z'))).toEqual({ date: '2026-10-02', time: '08:30:00' })
  })

  it('crosses midnight with the campus, not with UTC', () => {
    expect(campusNow(new Date('2026-10-02T19:30:00Z'))).toEqual({ date: '2026-10-03', time: '00:30:00' })
  })

  it('compares as text with the times the API sends', () => {
    const { time } = campusNow(new Date('2026-10-02T04:00:00Z'))
    expect('08:30:00' <= time && time < '09:50:00').toBe(true)
  })
})

describe('Uzbek dates are written by hand (browsers lack the data)', () => {
  it('names the day and the month', () => {
    expect(formatLongDay('2026-10-02', 'uz')).toBe('juma, 2-oktabr')
    expect(formatLongDay('2026-01-04', 'uz')).toBe('yakshanba, 4-yanvar')
  })

  it('writes a date and a deadline on the campus clock', () => {
    expect(formatDate('2026-10-04T18:00:00Z', 'uz')).toBe('4-okt 2026')
    expect(formatDateTime('2026-10-04T18:00:00Z', 'uz')).toBe('4-okt, 23:00')
    // 20:00 UTC is already the next day in Tashkent
    expect(formatDateTime('2026-10-04T20:00:00Z', 'uz')).toBe('5-okt, 01:00')
  })

  it('speaks of the recent past and the near future', () => {
    const now = new Date('2026-10-02T12:00:00Z')
    expect(formatRelativeTime('2026-10-02T11:59:40Z', 'uz', now)).toBe('hozirgina')
    expect(formatRelativeTime('2026-10-02T11:55:00Z', 'uz', now)).toBe('5 daqiqa oldin')
    expect(formatRelativeTime('2026-10-02T09:00:00Z', 'uz', now)).toBe('3 soat oldin')
    expect(formatRelativeTime('2026-10-01T11:00:00Z', 'uz', now)).toBe('kecha')
    expect(formatRelativeTime('2026-10-05T12:00:00Z', 'uz', now)).toBe('3 kun keyin')
    expect(formatRelativeTime('2026-09-01T11:00:00Z', 'uz', now)).toBe('1-sen 2026')
  })
})

describe('campus dates as the calendar has them', () => {
  it('moves by whole days across months and years', () => {
    expect(shiftDate('2026-10-19', 7)).toBe('2026-10-26')
    expect(shiftDate('2026-10-01', -7)).toBe('2026-09-24')
    expect(shiftDate('2026-12-28', 7)).toBe('2027-01-04')
  })

  it('knows the weekday: Monday is 1, Sunday is 7', () => {
    expect(weekdayOf('2026-10-19')).toBe(1)
    expect(weekdayOf('2026-10-25')).toBe(7)
  })

  it('names the columns of a week', () => {
    expect(formatWeekdayShort('2026-10-19', 'uz')).toBe('Dush')
    expect(formatWeekdayShort('2026-10-24', 'uz')).toBe('Shan')
    expect(formatWeekdayShort('2026-10-19', 'en')).toBe('Mon')
    expect(formatWeekdayShort('2026-10-19', 'ru')).toBe('Пн')
  })

  it('writes the dates of a week, within a month and across two', () => {
    expect(formatDateRange('2026-10-19', '2026-10-25', 'uz')).toBe('19–25 oktabr')
    expect(formatDateRange('2026-09-28', '2026-10-04', 'uz')).toBe('28 sentabr – 4 oktabr')
    expect(formatDateRange('2026-10-19', '2026-10-25', 'ru')).toBe('19–25 октября')
    expect(formatDateRange('2026-10-19', '2026-10-25', 'en')).toBe('October 19–25')
    expect(formatDateRange('2026-09-28', '2026-10-04', 'en')).toBe('September 28 – October 4')
  })
})

describe('Russian and English come from Intl', () => {
  it('shortens a time of day', () => {
    expect(formatTimeOfDay('08:30:00')).toBe('08:30')
  })

  it('names a campus day', () => {
    expect(formatLongDay('2026-10-02', 'en')).toMatch(/Friday/)
    expect(formatLongDay('2026-10-02', 'en')).toMatch(/October/)
    expect(formatLongDay('2026-10-02', 'ru')).toContain('2 октября')
  })

  it('speaks relatively', () => {
    const now = new Date('2026-10-02T12:00:00Z')
    expect(formatRelativeTime('2026-10-02T11:55:00Z', 'en', now)).toBe('5 minutes ago')
    expect(formatRelativeTime('2026-10-01T11:00:00Z', 'en', now)).toBe('yesterday')
  })
})
