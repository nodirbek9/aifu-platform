import type { Locale } from '@/shared/i18n'

/** The campus lives in one time zone; times of day are always shown in it (API guide §2). */
export const CAMPUS_TIME_ZONE = 'Asia/Tashkent'

const MINUTE = 60
const HOUR = 60 * MINUTE
const DAY = 24 * HOUR

/** "5 minutes ago", "yesterday" — in the interface language. Older than a week: the date. */
export function formatRelativeTime(iso: string, locale: Locale, now: Date = new Date()): string {
  const then = new Date(iso)
  const seconds = Math.round((then.getTime() - now.getTime()) / 1000)
  const distance = Math.abs(seconds)
  const relative = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' })
  if (distance < MINUTE) return relative.format(0, 'minute')
  if (distance < HOUR) return relative.format(Math.trunc(seconds / MINUTE), 'minute')
  if (distance < DAY) return relative.format(Math.trunc(seconds / HOUR), 'hour')
  if (distance < 7 * DAY) return relative.format(Math.trunc(seconds / DAY), 'day')
  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: CAMPUS_TIME_ZONE,
  }).format(then)
}
