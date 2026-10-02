import { useTranslation } from 'react-i18next'
import { PasswordForm } from './password-form'
import { PublicLayout } from './public-layout'

/** The forced change after a first sign-in: nothing else is reachable until it is done. */
export function ChangePasswordPage() {
  const { t } = useTranslation()
  return (
    <PublicLayout>
      <div className="grid gap-6">
        <div className="grid gap-1.5">
          <h1 className="font-display text-display-lg text-text font-semibold">
            {t('auth.changePasswordTitle')}
          </h1>
          <p className="text-small text-text-muted">{t('auth.changePasswordBody')}</p>
        </div>
        <PasswordForm autoFocus prominent />
      </div>
    </PublicLayout>
  )
}
