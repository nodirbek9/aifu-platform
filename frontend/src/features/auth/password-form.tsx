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

// The server's rule (StrongPassword): 10 to 72 characters.
const MIN_PASSWORD_LENGTH = 10
const MAX_PASSWORD_LENGTH = 72

type PasswordFormProps = {
  /** Put the cursor in the first field (the forced change after a first sign-in). */
  autoFocus?: boolean
  /** A full-width accent button (the public page) or a regular one (the profile). */
  prominent?: boolean
}

/**
 * Current password, the new one twice. Saving ends every session of the account — the server
 * revokes them — so the form finishes on the login page, where the new password is used.
 */
export function PasswordForm({ autoFocus = false, prominent = false }: PasswordFormProps) {
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
  type Values = z.infer<typeof schema>

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ resolver: zodResolver(schema) })

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
        autoFocus={autoFocus}
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
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {formError}
        </p>
      )}

      {prominent ? (
        <Button type="submit" variant="accent" size="lg" full loading={isSubmitting}>
          {isSubmitting ? t('auth.changingPassword') : t('auth.changePassword')}
        </Button>
      ) : (
        <Button type="submit" className="justify-self-start" loading={isSubmitting}>
          {isSubmitting ? t('auth.changingPassword') : t('auth.changePassword')}
        </Button>
      )}
    </form>
  )
}
