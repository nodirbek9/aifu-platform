import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router'
import { api, unwrap } from '@/shared/api/client'
import { fieldErrors, toApiProblem } from '@/shared/api/errors'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { useAuth } from './auth-context'
import { PublicLayout } from './public-layout'

// The server's rule (StrongPassword): 10 to 72 characters.
const MIN_PASSWORD_LENGTH = 10
const MAX_PASSWORD_LENGTH = 72

export function ChangePasswordPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { forgetSession } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)

  const schema = z
    .object({
      currentPassword: z.string().min(1, t('auth.fieldRequired')),
      newPassword: z
        .string()
        .min(MIN_PASSWORD_LENGTH, t('auth.passwordTooShort'))
        .max(MAX_PASSWORD_LENGTH, t('auth.passwordTooLong')),
      confirmPassword: z.string().min(1, t('auth.fieldRequired')),
    })
    .refine((values) => values.newPassword === values.confirmPassword, {
      path: ['confirmPassword'],
      message: t('auth.passwordsDoNotMatch'),
    })
  type ChangePasswordForm = z.infer<typeof schema>

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordForm>({ resolver: zodResolver(schema) })

  const onSubmit = handleSubmit(async (values) => {
    setFormError(null)
    try {
      await unwrap(
        api.PUT('/api/v1/auth/password', {
          body: { currentPassword: values.currentPassword, newPassword: values.newPassword },
        }),
      )
      // The server revoked every refresh token: sign in again with the new password.
      forgetSession()
      void navigate('/login', { replace: true, state: { passwordChanged: true } })
    } catch (error) {
      const problem = toApiProblem(error)
      const fields = fieldErrors(problem)
      let handled = false
      for (const [field, message] of Object.entries(fields)) {
        if (field === 'currentPassword' || field === 'newPassword') {
          setError(field, { type: 'server', message })
          handled = true
        }
      }
      if (!handled) setFormError(t(`errors.${problem.code}`))
    }
  })

  return (
    <PublicLayout>
      <div className="grid gap-6">
        <div className="grid gap-1.5">
          <h1 className="font-display text-display-lg text-text font-semibold">
            {t('auth.changePasswordTitle')}
          </h1>
          <p className="text-small text-text-muted">{t('auth.changePasswordBody')}</p>
        </div>

        <form
          className="grid gap-4"
          noValidate
          onSubmit={(event) => {
            void onSubmit(event)
          }}
        >
          <Input
            label={t('auth.currentPassword')}
            type="password"
            autoComplete="current-password"
            autoFocus
            error={errors.currentPassword?.message}
            {...register('currentPassword')}
          />
          <Input
            label={t('auth.newPassword')}
            type="password"
            autoComplete="new-password"
            error={errors.newPassword?.message}
            {...register('newPassword')}
          />
          <Input
            label={t('auth.confirmPassword')}
            type="password"
            autoComplete="new-password"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
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
            {isSubmitting ? t('auth.changingPassword') : t('auth.changePassword')}
          </Button>
        </form>
      </div>
    </PublicLayout>
  )
}
