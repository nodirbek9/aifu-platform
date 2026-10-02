import type { components } from '@/shared/api/schema'
import { isLocale, type Locale } from '@/shared/i18n'

export type Role = 'STUDENT' | 'TEACHER' | 'ADMIN'
export const ROLES: readonly Role[] = ['STUDENT', 'TEACHER', 'ADMIN']

export function isRole(value: unknown): value is Role {
  return typeof value === 'string' && (ROLES as readonly string[]).includes(value)
}

type CurrentUserResponse = components['schemas']['CurrentUserResponse']

/** The server user, narrowed once at the edge: required fields guaranteed, roles and locale parsed. */
export type CurrentUser = {
  id: string
  username: string
  fullName: string
  email?: string
  phone?: string
  roles: readonly Role[]
  permissions: readonly string[]
  preferredLocale: Locale | null
  groupCode?: string
  studentNumber?: string
  mustChangePassword: boolean
}

export function narrowUser(raw: CurrentUserResponse): CurrentUser {
  if (typeof raw.id !== 'string' || typeof raw.username !== 'string' || typeof raw.fullName !== 'string') {
    throw new Error('CurrentUserResponse is missing required identity fields')
  }
  const user: CurrentUser = {
    id: raw.id,
    username: raw.username,
    fullName: raw.fullName,
    roles: (raw.roles ?? []).filter(isRole),
    permissions: raw.permissions ?? [],
    preferredLocale: isLocale(raw.preferredLocale) ? raw.preferredLocale : null,
    mustChangePassword: raw.mustChangePassword === true,
  }
  if (typeof raw.email === 'string') user.email = raw.email
  if (typeof raw.phone === 'string' && raw.phone !== '') user.phone = raw.phone
  if (typeof raw.groupCode === 'string') user.groupCode = raw.groupCode
  if (typeof raw.studentNumber === 'string') user.studentNumber = raw.studentNumber
  return user
}

const ROLE_HOME: Record<Role, string> = {
  STUDENT: '/student',
  TEACHER: '/teacher',
  ADMIN: '/admin',
}

export function roleHomePath(role: Role): string {
  return ROLE_HOME[role]
}

/** Default role for a fresh session: students study, teachers teach, admins administrate. */
export function defaultRole(roles: readonly Role[]): Role | null {
  for (const role of ROLES) {
    if (roles.includes(role)) return role
  }
  return null
}
