import type { UserStatus } from './users-queries'

export const STATUSES: readonly UserStatus[] = ['ACTIVE', 'PENDING', 'SUSPENDED', 'DISABLED']

/** How each account status reads at a glance. */
export const STATUS_TONE = {
  ACTIVE: 'success',
  PENDING: 'info',
  SUSPENDED: 'warning',
  DISABLED: 'neutral',
} as const satisfies Record<UserStatus, string>

/** Draft, published, closed, archived: how a subject or a course stands. */
export const PUBLICATION_TONE = {
  DRAFT: 'neutral',
  PUBLISHED: 'success',
  ARCHIVED: 'warning',
  CLOSED: 'info',
} as const
