import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useTranslation } from 'react-i18next'
import { useLocation } from 'react-router'
import { Eye, EyeOff } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { toApiProblem } from '@/shared/api/errors'
import { useAuth } from './auth-context'
import { readLoginLocationState } from './login-state'
import { PublicLayout } from './public-layout'

export function LoginPage() {
  const { t } = useTranslation()
  const location = useLocation()
  const { signIn, clearExpiredNotice } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)
  const [showPassword, setShowPassword] = useState(false)

  const state = readLoginLocationState(location.state)
  const notice =
    state.reason === 'expired' ? 'expired' : state.passwordChanged === true ? 'passwordChanged' : null

  const schema = z.object({
    identifier: z.string().min(1, t('auth.fieldRequired')),
    password: z.string().min(1, t('auth.fieldRequired')),
  })
  type LoginForm = z.infer<typeof schema>

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(schema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      // Where to go next is the route guard's decision (PublicOnly), not this form's.
      await signIn(values.identifier, values.password)
    } catch (error) {
      const problem = toApiProblem(error)
      setFormError(t(`errors.${problem.code}`))
    }
  })

  return (
    <PublicLayout>
      <div className="grid gap-6">
        <div className="grid gap-1.5">
          <h1 className="font-display text-display-lg text-text font-semibold">{t('auth.signInTitle')}</h1>
          <p className="text-small text-text-muted">{t('auth.signInSubtitle')}</p>
        </div>

        {notice === 'expired' && (
          <Callout variant="INFO" title={t('auth.sessionExpiredTitle')}>
            {t('auth.sessionExpiredBody')}
          </Callout>
        )}
        {notice === 'passwordChanged' && (
          <Callout variant="TIP" title={t('auth.passwordChangedTitle')}>
            {t('auth.passwordChangedBody')}
          </Callout>
        )}

        <form
          className="grid gap-4"
          noValidate
          onSubmit={(event) => {
            clearExpiredNotice()
            void onSubmit(event)
          }}
        >
          <Input
            label={t('auth.identifier')}
            autoComplete="username"
            autoFocus
            error={errors.identifier?.message}
            {...register('identifier')}
          />
          <Input
            label={t('auth.password')}
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            error={errors.password?.message}
            trailing={
              <IconButton
                aria-label={showPassword ? t('auth.hidePassword') : t('auth.showPassword')}
                size="sm"
                onClick={() => {
                  setShowPassword((current) => !current)
                }}
              >
                {showPassword ? (
                  <EyeOff aria-hidden className="size-4" />
                ) : (
                  <Eye aria-hidden className="size-4" />
                )}
              </IconButton>
            }
            {...register('password')}
          />

          {formError !== null && (
            <p
              role="alert"
              className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
            >
              {formError}
            </p>
          )}

          <Button type="submit" variant="accent" size="lg" full loading={isSubmitting}>
            {isSubmitting ? t('auth.signingIn') : t('auth.signIn')}
          </Button>
        </form>
      </div>
    </PublicLayout>
  )
}
