import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { PageHeader } from '@/shared/ui/page-header'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { useAuditQuery, type AuditAction } from './audit-queries'
import { usePermissionsQuery, useRolesQuery } from './users-queries'

/** The actions an administrator filters by most; the full list is long and technical. */
const ACTIONS: readonly AuditAction[] = [
  'LOGIN_FAILURE',
  'ACCOUNT_LOCKED',
  'USER_CREATED',
  'USER_STATUS_CHANGED',
  'USER_PASSWORD_RESET',
  'ROLE_GRANTED',
  'ROLE_REVOKED',
  'COURSE_PUBLISHED',
  'ENROLLMENT_CREATED',
  'GRADE_RECORDED',
  'GRADE_VOIDED',
  'GROUP_CHANGED',
  'TIMETABLE_PUBLISHED',
  'TIMETABLE_ENTRY_CHANGED',
  'TIMETABLE_CONFLICT_OVERRIDDEN',
  'PERMISSION_DENIED',
]

const OUTCOME_TONE = { SUCCESS: 'success', FAILURE: 'danger', DENIED: 'warning' } as const

/** A-07 — who did what, when, with what outcome; and the roles with their permissions. */
export function AuditPage() {
  const { t } = useTranslation()
  const [search, setSearch] = useSearchParams()
  const tab = search.get('tab') === 'roles' ? 'roles' : 'log'
  return (
    <div className="grid grid-cols-1 gap-6">
      <PageHeader title={t('nav.adminAudit')} />
      <Tabs
        value={tab}
        onValueChange={(value) => {
          setSearch(value === 'log' ? {} : { tab: value }, { replace: true })
        }}
      >
        <TabsList>
          <TabsTrigger value="log">{t('admin.auditLog')}</TabsTrigger>
          <TabsTrigger value="roles">{t('admin.rolesCatalogue')}</TabsTrigger>
        </TabsList>
        <TabsContent value="log" className="pt-6">
          {tab === 'log' && <AuditLog />}
        </TabsContent>
        <TabsContent value="roles" className="pt-6">
          {tab === 'roles' && <RolesCatalogue />}
        </TabsContent>
      </Tabs>
    </div>
  )
}

function AuditLog() {
  const { t } = useTranslation()
  const locale = useLocale()
  const [search, setSearch] = useSearchParams()
  const action = ACTIONS.find((item) => item === search.get('action'))
  const filter = {
    ...(action !== undefined && { action }),
    entityType: search.get('entity') ?? '',
    from: search.get('from') ?? '',
    to: search.get('to') ?? '',
    page: Number(search.get('page') ?? '0') || 0,
  }
  const log = useAuditQuery(filter)
  const set = (key: string, value: string) => {
    const next = new URLSearchParams(search)
    if (value === '' || value === 'all') next.delete(key)
    else next.set(key, value)
    if (key !== 'page') next.delete('page')
    setSearch(next, { replace: true })
  }
  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex flex-wrap items-end gap-3">
        <Select
          className="w-64"
          label={t('admin.action')}
          value={action ?? 'all'}
          onValueChange={(value) => {
            set('action', value)
          }}
          options={[
            { value: 'all', label: t('admin.allActions') },
            ...ACTIONS.map((item) => ({ value: item, label: t(`admin.auditAction.${item}`) })),
          ]}
        />
        <Input
          className="w-44"
          label={t('admin.entityType')}
          placeholder="user, course…"
          value={filter.entityType}
          onChange={(event) => {
            set('entity', event.target.value.trim())
          }}
        />
        <Input
          label={t('admin.from')}
          type="date"
          value={filter.from}
          onChange={(event) => {
            set('from', event.target.value)
          }}
        />
        <Input
          label={t('admin.to')}
          type="date"
          value={filter.to}
          onChange={(event) => {
            set('to', event.target.value)
          }}
        />
      </div>
      {log.isPending && <Skeleton className="h-96" />}
      {log.isError && (
        <ErrorState
          problem={toApiProblem(log.error)}
          onRetry={() => {
            void log.refetch()
          }}
        />
      )}
      {log.isSuccess && (log.data.content ?? []).length === 0 && <EmptyState title={t('admin.noAudit')} />}
      {log.isSuccess && (log.data.content ?? []).length > 0 && (
        <>
          <div className="border-border overflow-x-auto rounded-lg border">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('admin.when')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.who')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.action')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.entityType')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.outcomeTitle')}</TableHeaderCell>
                  <TableHeaderCell>IP</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(log.data.content ?? []).map((row) => (
                  <TableRow key={row.id}>
                    <TableCell className="whitespace-nowrap tabular-nums">
                      {row.occurredAt === undefined ? '' : formatDateTime(row.occurredAt, locale)}
                    </TableCell>
                    <TableCell>{row.actorLabel ?? '—'}</TableCell>
                    <TableCell>{t(`admin.auditAction.${row.action ?? ''}`, row.action ?? '')}</TableCell>
                    <TableCell className="font-mono">{row.entityType}</TableCell>
                    <TableCell>
                      <Badge tone={OUTCOME_TONE[row.outcome ?? 'SUCCESS']}>
                        {t(`admin.auditOutcome.${row.outcome ?? 'SUCCESS'}`)}
                      </Badge>
                    </TableCell>
                    <TableCell className="font-mono">{row.ipAddress}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <Pagination
            page={filter.page}
            totalPages={log.data.totalPages ?? 1}
            onPageChange={(page) => {
              set('page', String(page))
            }}
          />
        </>
      )}
    </div>
  )
}

function RolesCatalogue() {
  const { t } = useTranslation()
  const roles = useRolesQuery()
  const permissions = usePermissionsQuery()
  if (roles.isPending || permissions.isPending) return <Skeleton className="h-96" />
  const describe = (code: string) => permissions.data?.find((item) => item.code === code)?.description ?? code
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      {(roles.data ?? []).map((role) => (
        <Card key={role.code}>
          <CardHeader
            title={t(`role.${role.code ?? ''}`, role.name ?? '')}
            description={t('admin.permissionCount', { count: role.permissions?.length ?? 0 })}
          />
          <CardContent>
            <ul className="text-small grid grid-cols-1 gap-1">
              {(role.permissions ?? []).map((code) => (
                <li key={code} className="grid grid-cols-1">
                  <span className="text-text font-mono">{code}</span>
                  <span className="text-caption text-text-muted">{describe(code)}</span>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
