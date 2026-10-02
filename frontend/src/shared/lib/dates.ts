import { useEffect, useState } from 'react'
import type { Locale } from '@/shared/i18n'

/** The campus lives in one time zone; times of day are always shown in it (API guide §2). */
export const CAMPUS_TIME_ZONE = 'Asia/Tashkent'

const MINUTE = 60
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR

/*
 * Browsers ship little or no Uzbek calendar data (Chromium prints "M10" for October), so Uzbek
 * dates are written here; Russian and English come from Intl.
 */
const UZ_MONTHS = [
  'yanvar',
  'fevral',
  'mart',
  'aprel',
  'may',
  'iyun',
  'iyul',
  'avgust',
  'sentabr',
  'oktabr',
  'noyabr',
  'dekabr',
] as const
const UZ_WEEKDAYS = [
  'yakshanba',
  'dushanba',
  'seshanba',
  'chorshanba',
  'payshanba',
  'juma',
  'shanba',
] as const

type CampusParts = { year: number; month: number; day: number; weekday: number; time: string }

/** The calendar fields of an instant on the campus clock. */
function campusParts(instant: Date): CampusParts {
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: CAMPUS_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(instant)
  const get = (type: Intl.DateTimeFormatPartTypes): string =>
    parts.find((part) => part.type === type)?.value ?? '00'
  const year = Number(get('year'))
  const month = Number(get('month'))
  const day = Number(get('day'))
  return {
    year,
    month,
    day,
    weekday: new Date(Date.UTC(year, month - 1, day)).getUTCDay(),
    time: `${get('hour')}:${get('minute')}:${get('second')}`,
  }
}

function uzMonth(month: number, short: boolean): string {
  const name = UZ_MONTHS[month - 1] ?? ''
  return short ? name.slice(0, 3) : name
}

/** "5 minutes ago", "yesterday" — in the interface language. Older than a week: the date. */
export function formatRelativeTime(iso: string, locale: Locale, now: Date = new Date()): string {
  const then = new Date(iso)
  const seconds = Math.round((then.getTime() - now.getTime()) / 1000)
  const distance = Math.abs(seconds)
  if (distance >= 7 * DAY) return formatDate(iso, locale)
  if (locale === 'uz') {
    const past = seconds <= 0
    const phrase = (count: number, unit: string) => `${count} ${unit} ${past ? 'oldin' : 'keyin'}`
    if (distance < MINUTE) return 'hozirgina'
    if (distance < HOUR) return phrase(Math.trunc(distance / MINUTE), 'daqiqa')
    if (distance < DAY) return phrase(Math.trunc(distance / HOUR), 'soat')
    const days = Math.trunc(distance / DAY)
    if (days === 1) return past ? 'kecha' : 'ertaga'
    return phrase(days, 'kun')
  }
  const relative = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' })
  if (distance < MINUTE) return relative.format(0, 'minute')
  if (distance < HOUR) return relative.format(Math.trunc(seconds / MINUTE), 'minute')
  if (distance < DAY) return relative.format(Math.trunc(seconds / HOUR), 'hour')
  return relative.format(Math.trunc(seconds / DAY), 'day')
}

/** "2 Oct 2026" in the interface language, on the campus calendar. */
export function formatDate(iso: string, locale: Locale): string {
  const instant = new Date(iso)
  if (locale === 'uz') {
    const { day, month, year } = campusParts(instant)
    return `${day}-${uzMonth(month, true)} ${year}`
  }
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: CAMPUS_TIME_ZONE,
  }).format(instant)
}

/** "2 Oct, 23:00" — a deadline: day and time on the campus clock. */
export function formatDateTime(iso: string, locale: Locale): string {
  const instant = new Date(iso)
  if (locale === 'uz') {
    const { day, month, time } = campusParts(instant)
    return `${day}-${uzMonth(month, true)}, ${time.slice(0, 5)}`
  }
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
    timeZone: CAMPUS_TIME_ZONE,
  }).format(instant)
}

/** "Friday, 2 October" for a campus date given as YYYY-MM-DD. */
export function formatLongDay(date: string, locale: Locale): string {
  // Noon UTC is the same calendar day in Tashkent (UTC+5), whatever the browser's zone.
  const instant = new Date(`${date}T12:00:00Z`)
  if (locale === 'uz') {
    const { day, month, weekday } = campusParts(instant)
    return `${UZ_WEEKDAYS[weekday] ?? ''}, ${day}-${uzMonth(month, false)}`
  }
  return new Intl.DateTimeFormat(locale, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    timeZone: CAMPUS_TIME_ZONE,
  }).format(instant)
}

/** "08:30:00" → "08:30". */
export function formatTimeOfDay(time: string): string {
  return time.slice(0, 5)
}

export type CampusNow = {
  /** YYYY-MM-DD on the campus calendar. */
  date: string
  /** HH:mm:ss on the campus clock. */
  time: string
}

/** The campus date and time of an instant, as strings that compare like the API's own. */
export function campusNow(now: Date = new Date()): CampusNow {
  const { year, month, day, time } = campusParts(now)
  const pad = (value: number) => String(value).padStart(2, '0')
  return { date: `${year}-${pad(month)}-${pad(day)}`, time }
}

/** The campus clock, re-read every half minute — enough to move the "now" marker of a class. */
export function useCampusNow(): CampusNow {
  const [now, setNow] = useState(() => campusNow())
  useEffect(() => {
    const timer = window.setInterval(() => {
      setNow(campusNow())
    }, 30_000)
    return () => {
      window.clearInterval(timer)
    }
  }, [])
  return now
}
