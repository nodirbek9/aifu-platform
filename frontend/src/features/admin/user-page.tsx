import { useState } from 'react'
import { KeyRound, LockOpen } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Checkbox } from '@/shared/ui/checkbox'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { useToast } from '@/shared/ui/toast'
import { STATUS_TONE, STATUSES } from './model'
import { StudentGroupCard } from './student-group-card'
import { StudentEnrollmentsCard } from './student-enrollments-card'
import { TeacherWorkCard } from './teacher-work-card'
import { RemoveStudentDialog } from './remove-student-dialog'
import {
  useResetPassword,
  useRolesQuery,
  useSetRoles,
  useSetStatus,
  useUnlockUser,
  useUpdateUser,
  useUserQuery,
  type UserDetail,
} from './users-queries'

function Problem({ error }: { error: unknown }) {
  const { t } = useTranslation()
  if (error === null || error === undefined) return null
  return (
    <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
      {t(`errors.${toApiProblem(error).code}`)}
    </p>
  )
}

/** A-05 / 7.1-F4 / 7.2-F3 — account controls and sections for each of the user's roles. */
export function UserPage() {
  const { t } = useTranslation()
  const { userId = '' } = useParams()
  const user = useUserQuery(userId)
  const [removing, setRemoving] = useState(false)
  if (user.isPending) return <Skeleton className="h-96" />
  if (user.isError) {
    return (
      <ErrorState
        problem={toApiProblem(user.error)}
        onRetry={() => {
          void user.refetch()
        }}
      />
    )
  }
  const data = user.data
  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[{ label: t('nav.adminUsers'), to: '/admin/users' }, { label: data.fullName ?? '' }]}
      />
      <header className="grid grid-cols-1 gap-1">
        <h1 className="font-display text-display-lg text-text font-semibold wrap-anywhere">
          {data.fullName}
        </h1>
        <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
          <span className="font-mono">{data.username}</span>
          <Badge tone={STATUS_TONE[data.status ?? 'ACTIVE']}>
            {t(`admin.userStatus.${data.status ?? 'ACTIVE'}`)}
          </Badge>
          {(data.roles ?? []).map((role) => (
            <Badge key={role}>{t(`role.${role}`, role)}</Badge>
          ))}
        </p>
      </header>
      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-2">
        <div className="grid grid-cols-1 gap-6">
          <DetailsCard key={`details-${data.id ?? ''}`} user={data} />
          {(data.roles ?? []).includes('STUDENT') && (
            <>
              <section aria-label={t('admin.studentCard.groupTitle')}>
                <StudentGroupCard key={`group-${data.id ?? ''}`} user={data} />
              </section>
              <section aria-label={t('admin.studentCard.coursesTitle')}>
                <StudentEnrollmentsCard key={`enrollments-${data.id ?? ''}`} user={data} />
              </section>
            </>
          )}
          {(data.roles ?? []).includes('TEACHER') && (
            <section aria-label={t('admin.teacherWork.tab')}>
              <TeacherWorkCard key={`teacher-work-${data.id ?? ''}`} user={data} />
            </section>
          )}
        </div>
        <div className="grid grid-cols-1 gap-6">
          <RolesCard key={`roles-${(data.roles ?? []).join()}`} user={data} />
          <StatusCard user={data} />
          {(data.roles ?? []).includes('STUDENT') && (
            <Button
              variant="danger"
              className="h-auto min-h-11 justify-self-start whitespace-normal"
              onClick={() => setRemoving(true)}
            >
              {t('admin.removeStudent.title')}
            </Button>
          )}
          <PasswordCard user={data} />
        </div>
      </div>
      {removing && <RemoveStudentDialog key={userId} user={data} onClose={() => setRemoving(false)} />}
    </div>
  )
}

function DetailsCard({ user }: { user: UserDetail }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()
  const update = useUpdateUser(user.id ?? '')
  const [fullName, setFullName] = useState(user.fullName ?? '')
  const [phone, setPhone] = useState(user.phone ?? '')
  const [preferred, setPreferred] = useState(user.preferredLocale ?? 'uz')
  const student = user.studentProfile
  const teacher = user.teacherProfile
  return (
    <Card>
      <CardHeader title={t('admin.details')} />
      <CardContent className="grid grid-cols-1 gap-4">
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            const language = preferred === 'ru' || preferred === 'en' ? preferred : 'uz'
            update.mutate(
              { fullName: fullName.trim(), phone: phone.trim(), preferredLocale: language },
              {
                onSuccess: () => {
                  toast({ tone: 'success', title: t('admin.saved') })
                },
              },
            )
          }}
        >
          <Input
            label={t('admin.fullName')}
            value={fullName}
            onChange={(event) => {
              setFullName(event.target.value)
            }}
          />
          <Input
            label={t('profile.phone')}
            type="tel"
            value={phone}
            onChange={(event) => {
              setPhone(event.target.value)
            }}
          />
          <Select
            label={t('admin.preferredLanguage')}
            value={preferred}
            onValueChange={setPreferred}
            options={['uz', 'ru', 'en'].map((value) => ({ value, label: value.toUpperCase() }))}
          />
          <Problem error={update.error} />
          <Button
            type="submit"
            className="justify-self-start"
            disabled={fullName.trim() === ''}
            loading={update.isPending}
          >
            {t('authoring.save')}
          </Button>
        </form>
        <dl className="text-small grid grid-cols-1 gap-2 sm:grid-cols-2">
          <Fact label={t('admin.email')} value={user.email} />
          <Fact
            label={t('admin.lastLogin')}
            value={user.lastLoginAt === undefined ? undefined : formatDateTime(user.lastLoginAt, locale)}
          />
          <Fact
            label={t('admin.createdAt')}
            value={user.createdAt === undefined ? undefined : formatDateTime(user.createdAt, locale)}
          />
          {student !== undefined && (
            <>
              <Fact label={t('admin.studentNumber')} value={student.studentNumber} />
              <Fact label={t('profile.group')} value={student.groupCode} />
            </>
          )}
          {teacher !== undefined && (
            <>
              <Fact label={t('admin.department')} value={teacher.departmentName} />
              <Fact
                label={t('admin.academicTitle')}
                value={[teacher.academicDegree, teacher.academicTitle].filter(Boolean).join(', ')}
              />
            </>
          )}
        </dl>
      </CardContent>
    </Card>
  )
}

function Fact({ label, value }: { label: string; value: string | undefined }) {
  return (
    <div className="grid grid-cols-1 gap-0.5">
      <dt className="text-caption text-text-muted font-medium uppercase">{label}</dt>
      <dd className="text-text wrap-anywhere">{value === undefined || value === '' ? '—' : value}</dd>
    </div>
  )
}

function RolesCard({ user }: { user: UserDetail }) {
  const { t } = useTranslation()
  const roles = useRolesQuery()
  const save = useSetRoles(user.id ?? '')
  const [chosen, setChosen] = useState<string[]>(user.roles ?? [])
  const changed = [...chosen].sort().join() !== [...(user.roles ?? [])].sort().join()
  return (
    <Card>
      <CardHeader title={t('admin.roles')} description={t('admin.rolesBody')} />
      <CardContent className="grid grid-cols-1 gap-3">
        {(roles.data ?? []).map((role) => (
          <Checkbox
            key={role.code}
            label={
              <span>
                {t(`role.${role.code ?? ''}`, role.name ?? '')}
                {role.description !== undefined && (
                  <span className="text-caption text-text-muted block">{role.description}</span>
                )}
              </span>
            }
            checked={chosen.includes(role.code ?? '')}
            onCheckedChange={(checked) => {
              setChosen(checked ? [...chosen, role.code ?? ''] : chosen.filter((code) => code !== role.code))
            }}
          />
        ))}
        <Problem error={save.error} />
        <Button
          className="justify-self-start"
          disabled={!changed || chosen.length === 0}
          loading={save.isPending}
          onClick={() => {
            save.mutate(chosen)
          }}
        >
          {t('admin.saveRoles')}
        </Button>
      </CardContent>
    </Card>
  )
}

function StatusCard({ user }: { user: UserDetail }) {
  const { t } = useTranslation()
  const save = useSetStatus(user.id ?? '')
  const unlock = useUnlockUser(user.id ?? '')
  const [status, setStatus] = useState(user.status ?? 'ACTIVE')
  const [reason, setReason] = useState('')
  const [openedAt] = useState(() => Date.now())
  const locked = user.lockedUntil !== undefined && new Date(user.lockedUntil).getTime() > openedAt
  return (
    <Card>
      <CardHeader title={t('admin.status')} />
      <CardContent>
        <form
          className="grid grid-cols-1 gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            save.mutate({ status, reason: reason.trim() })
          }}
        >
          <Select
            label={t('admin.status')}
            value={status}
            onValueChange={(value) => {
              const next = STATUSES.find((item) => item === value)
              if (next !== undefined) setStatus(next)
            }}
            options={STATUSES.map((value) => ({ value, label: t(`admin.userStatus.${value}`) }))}
          />
          <Input
            label={t('admin.reason')}
            value={reason}
            onChange={(event) => {
              setReason(event.target.value)
            }}
          />
          <Problem error={save.error ?? unlock.error} />
          <div className="flex flex-wrap gap-2">
            <Button type="submit" disabled={status === user.status} loading={save.isPending}>
              {t('admin.changeStatus')}
            </Button>
            {locked && (
              <Button
                variant="outline"
                loading={unlock.isPending}
                onClick={() => {
                  unlock.mutate(undefined)
                }}
              >
                <LockOpen aria-hidden className="size-4" />
                {t('admin.unlock')}
              </Button>
            )}
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function PasswordCard({ user }: { user: UserDetail }) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const reset = useResetPassword(user.id ?? '')
  const [password, setPassword] = useState('')
  return (
    <Card>
      <CardHeader title={t('admin.resetPassword')} description={t('admin.resetPasswordBody')} />
      <CardContent>
        <form
          className="grid grid-cols-1 gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            reset.mutate(password, {
              onSuccess: () => {
                setPassword('')
                toast({ tone: 'success', title: t('admin.passwordReset') })
              },
            })
          }}
        >
          <Input
            label={t('admin.newPassword')}
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => {
              setPassword(event.target.value)
            }}
          />
          <Problem error={reset.error} />
          <Button
            type="submit"
            variant="outline"
            className="justify-self-start"
            disabled={password.length < 8}
            loading={reset.isPending}
          >
            <KeyRound aria-hidden className="size-4" />
            {t('admin.setPassword')}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
