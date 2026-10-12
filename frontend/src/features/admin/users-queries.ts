import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import { useRefreshingMutation } from './reference-queries'

type Schemas = components['schemas']
export type UserSummary = Schemas['UserSummary']
export type UserDetail = Schemas['UserDetail']
export type UserStatus = NonNullable<UserSummary['status']>
export type CreateUserInput = Schemas['CreateUserRequest']
export type ImportRow = Schemas['ImportStudentRow']
export type ImportResult = Schemas['ImportStudentResult']

export const userKeys = {
  list: ['admin', 'users'] as const,
  user: (id: string) => ['admin', 'users', id] as const,
  roles: ['admin', 'roles'] as const,
  permissions: ['admin', 'permissions'] as const,
}

export type UserFilter = { query?: string; status?: UserStatus; role?: string; page: number }

export const PAGE_SIZE = 25

export function useUsersQuery(filter: UserFilter, enabled = true) {
  return useQuery({
    queryKey: [...userKeys.list, filter],
    queryFn: () =>
      unwrap(
        api.GET('/api/v1/admin/users', {
          params: {
            query: {
              ...(filter.query !== undefined && filter.query !== '' && { query: filter.query }),
              ...(filter.status !== undefined && { status: filter.status }),
              ...(filter.role !== undefined && { role: filter.role }),
              page: filter.page,
              size: PAGE_SIZE,
              sort: ['fullName,asc'],
            },
          },
        }),
      ),
    placeholderData: keepPreviousData,
    enabled,
  })
}

/** Every teacher (up to a hundred): curators, course teachers, load lines. */
export function useTeachersQuery() {
  return useQuery({
    queryKey: [...userKeys.list, 'teachers'],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/admin/users', {
          params: { query: { role: 'TEACHER', size: 100, sort: ['fullName,asc'] } },
        }),
      )
      return page.content ?? []
    },
    staleTime: 60_000,
  })
}

export function useUserQuery(id: string) {
  return useQuery({
    queryKey: userKeys.user(id),
    queryFn: () => unwrap(api.GET('/api/v1/admin/users/{id}', { params: { path: { id } } })),
    enabled: id !== '',
  })
}

export function useCreateUser() {
  return useRefreshingMutation([userKeys.list], (input: CreateUserInput) =>
    unwrap(api.POST('/api/v1/admin/users', { body: input })),
  )
}

export function useUpdateUser(id: string) {
  return useRefreshingMutation([userKeys.list], (input: Schemas['UpdateUserRequest']) =>
    unwrap(api.PATCH('/api/v1/admin/users/{id}', { params: { path: { id } }, body: input })),
  )
}

export function useSetRoles(id: string) {
  return useRefreshingMutation([userKeys.list], (roles: string[]) =>
    unwrap(api.PUT('/api/v1/admin/users/{id}/roles', { params: { path: { id } }, body: { roles } })),
  )
}

export function useSetStatus(id: string) {
  return useRefreshingMutation(
    [userKeys.list],
    ({ status, reason }: { status: UserStatus; reason: string }) =>
      unwrap(
        api.PUT('/api/v1/admin/users/{id}/status', {
          params: { path: { id } },
          body: { status, ...(reason !== '' && { reason }) },
        }),
      ),
  )
}

export function useResetPassword(id: string) {
  return useRefreshingMutation([userKeys.user(id)], (newPassword: string) =>
    unwrap(
      api.POST('/api/v1/admin/users/{id}/reset-password', {
        params: { path: { id } },
        body: { newPassword },
      }),
    ),
  )
}

export function useUnlockUser(id: string) {
  return useRefreshingMutation([userKeys.user(id)], () =>
    unwrap(api.POST('/api/v1/admin/users/{id}/unlock', { params: { path: { id } } })),
  )
}

export function useImportStudents() {
  return useRefreshingMutation(
    [userKeys.list, ['admin', 'groups'], ['groups']],
    (input: { rows: ImportRow[]; dryRun: boolean; initialPassword?: string }) =>
      unwrap(
        api.POST('/api/v1/admin/users/import', {
          body: {
            rows: input.rows,
            dryRun: input.dryRun,
            ...(input.initialPassword !== undefined &&
              input.initialPassword !== '' && {
                initialPassword: input.initialPassword,
              }),
          },
        }),
      ),
  )
}

export function useRolesQuery() {
  return useQuery({
    queryKey: userKeys.roles,
    queryFn: () => unwrap(api.GET('/api/v1/admin/roles')),
    staleTime: Infinity,
  })
}

export function usePermissionsQuery() {
  return useQuery({
    queryKey: userKeys.permissions,
    queryFn: () => unwrap(api.GET('/api/v1/admin/permissions')),
    staleTime: Infinity,
  })
}
