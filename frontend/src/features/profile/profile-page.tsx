import { useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useTheme } from '@/app/theme'
import { useAuth } from '@/features/auth/auth-context'
import type { CurrentUser } from '@/features/auth/model'
import { PasswordForm } from '@/features/auth/password-form'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Avatar } from '@/shared/ui/avatar'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Input } from '@/shared/ui/input'
import { LanguageSwitch } from '@/shared/ui/language-switch'
import { PageHeader } from '@/shared/ui/page-header'
import { ThemeSwitch } from '@/shared/ui/theme-switch'
import { useToast } from '@/shared/ui/toast'

/** S-09 — my profile: who I am here, the language and the theme, my phone, my password. */
export function ProfilePage() {
  const { t } = useTranslation()
  const { state, changeLocale } = useAuth()
  const { theme, setTheme } = useTheme()
  const locale = useLocale()
  if (state.status !== 'authenticated') return null
  const { user } = state

  return (
    <div className="max-w-lesson grid gap-6">
      <PageHeader title={t('common.profile')} />

      <Card>
        <CardContent className="grid gap-5 pt-5">
          <div className="flex items-center gap-4">
            <Avatar name={user.fullName} size="lg" />
            <div className="grid min-w-0 gap-0.5">
              <h2 className="font-display text-title text-text font-semibold">{user.fullName}</h2>
              <p className="text-small text-text-muted">
                {user.roles.map((role) => t(`role.${role}`)).join(' · ')}
              </p>
            </div>
          </div>
          <dl className="border-border grid gap-x-6 gap-y-4 border-t pt-5 sm:grid-cols-2">
            <Fact label={t('auth.identifier')}>{user.username}</Fact>
            {user.groupCode !== undefined && <Fact label={t('profile.group')}>{user.groupCode}</Fact>}
            {user.studentNumber !== undefined && (
              <Fact label={t('profile.studentNumber')}>{user.studentNumber}</Fact>
            )}
            {user.email !== undefined && <Fact label={t('profile.email')}>{user.email}</Fact>}
          </dl>
          <PhoneForm user={user} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader title={t('profile.appearance')} />
        <CardContent className="grid gap-5">
          <Setting label={t('common.language')} hint={t('profile.languageHint')}>
            <LanguageSwitch value={locale} onChange={changeLocale} />
          </Setting>
          <Setting label={t('common.theme')} hint={t('profile.themeHint')}>
            <ThemeSwitch value={theme} onChange={setTheme} />
          </Setting>
        </CardContent>
      </Card>

      <Card>
        <CardHeader title={t('auth.changePasswordTitle')} description={t('profile.passwordHint')} />
        <CardContent className="max-w-sm">
          <PasswordForm />
        </CardContent>
      </Card>
    </div>
  )
}

function Fact({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="grid gap-0.5">
      <dt className="text-caption tracking-caption text-text-muted font-medium uppercase">{label}</dt>
      <dd className="text-body text-text break-words">{children}</dd>
    </div>
  )
}

function Setting({ label, hint, children }: { label: string; hint: string; children: ReactNode }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-2">
      <div className="grid gap-0.5">
        <p className="text-body text-text font-medium">{label}</p>
        <p className="text-small text-text-muted">{hint}</p>
      </div>
      {children}
    </div>
  )
}

/** The one thing of the account its owner may edit; the rest is kept by the academic office. */
function PhoneForm({ user }: { user: CurrentUser }) {
  const { t } = useTranslation()
  const { updatePhone } = useAuth()
  const { toast } = useToast()
  const [phone, setPhone] = useState(user.phone ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string>()
  const changed = phone.trim() !== (user.phone ?? '')

  const save = async () => {
    setSaving(true)
    setError(undefined)
    try {
      await updatePhone(phone.trim())
      toast({ tone: 'success', title: t('profile.phoneSaved') })
    } catch (failure) {
      setError(t(`errors.${toApiProblem(failure).code}`))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form
      className="border-border flex flex-wrap items-end gap-3 border-t pt-5"
      onSubmit={(event) => {
        event.preventDefault()
        void save()
      }}
    >
      <div className="min-w-0 flex-1 sm:max-w-xs">
        <Input
          label={t('profile.phone')}
          type="tel"
          inputMode="tel"
          autoComplete="tel"
          placeholder="+998 90 123 45 67"
          maxLength={32}
          value={phone}
          error={error}
          onChange={(event) => {
            setPhone(event.target.value)
          }}
        />
      </div>
      <Button type="submit" variant="outline" disabled={!changed} loading={saving}>
        {t('profile.save')}
      </Button>
    </form>
  )
}
