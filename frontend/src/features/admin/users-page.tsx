import { useState } from 'react'
import { FileUp, Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Button, button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { PageHeader } from '@/shared/ui/page-header'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Switch } from '@/shared/ui/switch'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useAllGroupsQuery } from './reference-queries'
import { STATUS_TONE, STATUSES } from './model'
import { useCreateUser, useRolesQuery, useUsersQuery, type CreateUserInput } from './users-queries'

/** A-05 — the users: search, filter by role and status (all kept in the address), open one, add one. */
export function UsersPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const [search, setSearch] = useSearchParams()
  const roles = useRolesQuery()
  const [creating, setCreating] = useState(false)
  const status = STATUSES.find((item) => item === search.get('status'))
  const filter = {
    query: search.get('q') ?? '',
    ...(status !== undefined && { status }),
    ...(search.get('role') !== null && { role: search.get('role') ?? '' }),
    page: Number(search.get('page') ?? '0') || 0,
  }
  const users = useUsersQuery(filter)
  const setParam = (key: string, value: string) => {
    const next = new URLSearchParams(search)
    if (value === '' || value === 'all') next.delete(key)
    else next.set(key, value)
    if (key !== 'page') next.delete('page')
    setSearch(next, { replace: true })
  }

  return (
    <div className="grid grid-cols-1 gap-6">
      <PageHeader
        title={t('nav.adminUsers')}
        action={
          <div className="flex flex-wrap gap-2">
            <Link to="/admin/users/import" className={button({ variant: 'outline' })}>
              <FileUp aria-hidden className="size-4" />
              {t('admin.importStudents')}
            </Link>
            <Button
              onClick={() => {
                setCreating(true)
              }}
            >
              <Plus aria-hidden className="size-4" />
              {t('admin.newUser')}
            </Button>
          </div>
        }
      />
      <div className="flex flex-wrap items-end gap-3">
        <Input
          className="w-72"
          label={t('common.search')}
          type="search"
          placeholder={t('admin.userSearchHint')}
          value={filter.query}
          onChange={(event) => {
            setParam('q', event.target.value)
          }}
        />
        <Select
          className="w-48"
          label={t('admin.role')}
          value={filter.role ?? 'all'}
          onValueChange={(value) => {
            setParam('role', value)
          }}
          options={[
            { value: 'all', label: t('admin.allRoles') },
            ...(roles.data ?? []).map((role) => ({
              value: role.code ?? '',
              label: t(`role.${role.code ?? ''}`, role.name ?? ''),
            })),
          ]}
        />
        <Select
          className="w-44"
          label={t('admin.status')}
          value={filter.status ?? 'all'}
          onValueChange={(value) => {
            setParam('status', value)
          }}
          options={[
            { value: 'all', label: t('admin.allStatuses') },
            ...STATUSES.map((item) => ({ value: item, label: t(`admin.userStatus.${item}`) })),
          ]}
        />
      </div>

      {users.isPending && <Skeleton className="h-96" />}
      {users.isError && (
        <ErrorState
          problem={toApiProblem(users.error)}
          onRetry={() => {
            void users.refetch()
          }}
        />
      )}
      {users.isSuccess && (users.data.content ?? []).length === 0 && (
        <EmptyState title={t('admin.noUsers')} />
      )}
      {users.isSuccess && (users.data.content ?? []).length > 0 && (
        <>
          <p role="status" className="text-small text-text-muted">
            {t('admin.userCount', { count: users.data.totalElements ?? 0 })}
          </p>
          <div className="border-border overflow-x-auto rounded-lg border">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('admin.fullName')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.username')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.roles')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.lastLogin')}</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(users.data.content ?? []).map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <Link
                        to={`/admin/users/${user.id ?? ''}`}
                        className="text-text hover:text-primary-hover font-medium underline-offset-4 hover:underline"
                      >
                        {user.fullName}
                      </Link>
                      {user.email !== undefined && (
                        <span className="text-caption text-text-muted block">{user.email}</span>
                      )}
                    </TableCell>
                    <TableCell className="font-mono">{user.username}</TableCell>
                    <TableCell>
                      <span className="flex flex-wrap gap-1">
                        {(user.roles ?? []).map((role) => (
                          <Badge key={role}>{t(`role.${role}`, role)}</Badge>
                        ))}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge tone={STATUS_TONE[user.status ?? 'ACTIVE']}>
                        {t(`admin.userStatus.${user.status ?? 'ACTIVE'}`)}
                      </Badge>
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {user.lastLoginAt === undefined ? '—' : formatRelativeTime(user.lastLoginAt, locale)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <Pagination
            page={filter.page}
            totalPages={users.data.totalPages ?? 1}
            onPageChange={(page) => {
              setParam('page', String(page))
            }}
          />
        </>
      )}
      {creating && (
        <NewUserDialog
          onClose={() => {
            setCreating(false)
          }}
        />
      )}
    </div>
  )
}

function NewUserDialog({ onClose }: { onClose: () => void }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const create = useCreateUser()
  const roles = useRolesQuery()
  const groups = useAllGroupsQuery()
  const [form, setForm] = useState({
    fullName: '',
    username: '',
    email: '',
    role: 'STUDENT',
    password: '',
    mustChange: true,
    studentNumber: '',
    groupCode: '',
  })
  const set = (patch: Partial<typeof form>) => {
    setForm({ ...form, ...patch })
  }
  const student = form.role === 'STUDENT'
  const ready =
    form.fullName.trim() !== '' &&
    form.username.trim() !== '' &&
    form.email.includes('@') &&
    (!student || form.studentNumber.trim() !== '')

  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent className="max-w-xl">
        <DialogTitle>{t('admin.newUser')}</DialogTitle>
        <DialogDescription>{t('admin.newUserBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (!ready) return
            const input: CreateUserInput = {
              fullName: form.fullName.trim(),
              username: form.username.trim(),
              email: form.email.trim(),
              roles: [form.role],
              mustChangePassword: form.mustChange,
              ...(form.password !== '' && { initialPassword: form.password }),
              ...(student && {
                studentProfile: {
                  studentNumber: form.studentNumber.trim(),
                  ...(form.groupCode !== '' && { groupCode: form.groupCode }),
                },
              }),
            }
            create.mutate(input, {
              onSuccess: (user) => {
                onClose()
                if (user.id !== undefined) void navigate(`/admin/users/${user.id}`)
              },
            })
          }}
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label={t('admin.fullName')}
              value={form.fullName}
              onChange={(event) => {
                set({ fullName: event.target.value })
              }}
            />
            <Input
              label={t('admin.username')}
              value={form.username}
              autoComplete="off"
              onChange={(event) => {
                set({ username: event.target.value })
              }}
            />
            <Input
              label={t('admin.email')}
              type="email"
              value={form.email}
              onChange={(event) => {
                set({ email: event.target.value })
              }}
            />
            <Select
              label={t('admin.role')}
              value={form.role}
              onValueChange={(value) => {
                set({ role: value })
              }}
              options={(roles.data ?? []).map((role) => ({
                value: role.code ?? '',
                label: t(`role.${role.code ?? ''}`, role.name ?? ''),
              }))}
            />
            {student && (
              <>
                <Input
                  label={t('admin.studentNumber')}
                  value={form.studentNumber}
                  onChange={(event) => {
                    set({ studentNumber: event.target.value })
                  }}
                />
                <Select
                  label={t('profile.group')}
                  value={form.groupCode === '' ? undefined : form.groupCode}
                  placeholder={t('authoring.choose')}
                  onValueChange={(value) => {
                    set({ groupCode: value })
                  }}
                  options={(groups.data ?? []).map((group) => ({
                    value: group.code ?? '',
                    label: group.code ?? '',
                  }))}
                />
              </>
            )}
            <Input
              label={t('admin.initialPassword')}
              type="password"
              autoComplete="new-password"
              value={form.password}
              placeholder={t('admin.passwordGenerated')}
              onChange={(event) => {
                set({ password: event.target.value })
              }}
            />
            <Switch
              className="self-end pb-2"
              label={t('admin.mustChangePassword')}
              checked={form.mustChange}
              onCheckedChange={(checked) => {
                set({ mustChange: checked })
              }}
            />
          </div>
          {create.isError && (
            <p
              role="alert"
              className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
            >
              {t(`errors.${toApiProblem(create.error).code}`)}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={!ready} loading={create.isPending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
