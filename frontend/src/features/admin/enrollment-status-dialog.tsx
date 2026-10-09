import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { z } from 'zod'
import { toApiProblem } from '@/shared/api/errors'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { ErrorState } from '@/shared/ui/error-state'
import { Select } from '@/shared/ui/select'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import { enrollmentTransitions, type EnrollmentSummary } from './student-enrollment-model'
import { useChangeEnrollmentStatus } from './student-enrollment-queries'

export type EditableEnrollment = EnrollmentSummary &
  Required<Pick<EnrollmentSummary, 'id' | 'courseId' | 'studentUserId' | 'status'>>

type EnrollmentStatusDialogProps = {
  enrollment: EditableEnrollment
  onClose: () => void
}

/** 7.1-F3: keep the reason and target status after a failed transition. */
export function EnrollmentStatusDialog({ enrollment, onClose }: EnrollmentStatusDialogProps) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const change = useChangeEnrollmentStatus()
  const [opener] = useState(() => document.activeElement)
  const choices = enrollmentTransitions(enrollment.status)
  const schema = z.object({
    status: z.enum(choices),
    reason: z.string().trim().min(1),
  })
  const form = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    defaultValues: { status: choices[0], reason: '' },
  })
  const submit = form.handleSubmit(({ status, reason }) => {
    if (change.isPending || !choices.includes(status)) return
    change.mutate(
      {
        id: enrollment.id,
        courseId: enrollment.courseId,
        studentUserId: enrollment.studentUserId,
        status,
        reason,
      },
      {
        onSuccess: () => {
          toast({ tone: 'success', title: t('admin.studentCard.statusChanged') })
          onClose()
        },
      },
    )
  })

  if (choices.length === 0) return null
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open && !change.isPending) onClose()
      }}
    >
      <DialogContent
        className="max-h-dvh overflow-y-auto"
        onCloseAutoFocus={(event) => {
          if (opener instanceof HTMLElement && opener.isConnected) {
            event.preventDefault()
            opener.focus()
          }
        }}
      >
        <DialogTitle>{t('admin.studentCard.changeEnrollmentStatus')}</DialogTitle>
        <DialogDescription>
          {[enrollment.subjectCode, enrollment.cohortCode, enrollment.termName].filter(Boolean).join(' · ')}
          {' · '}
          {t(`admin.enrollmentStatus.${enrollment.status}`)}
        </DialogDescription>
        <form className="mt-4 grid grid-cols-1 gap-4" onSubmit={(event) => void submit(event)}>
          <Controller
            control={form.control}
            name="status"
            render={({ field }) => (
              <Select
                label={t('admin.status')}
                value={field.value}
                onValueChange={field.onChange}
                disabled={change.isPending}
                options={choices.map((value) => ({ value, label: t(`admin.enrollmentStatus.${value}`) }))}
              />
            )}
          />
          <Textarea
            label={t('admin.reason')}
            {...form.register('reason')}
            required
            disabled={change.isPending}
            error={
              form.formState.errors.reason === undefined ? undefined : t('admin.studentCard.reasonRequired')
            }
          />
          {change.isError && (
            <ErrorState problem={toApiProblem(change.error)} onRetry={() => void submit()} />
          )}
          <DialogFooter className="mt-2 flex-wrap">
            <Button variant="outline" disabled={change.isPending} onClick={onClose}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" disabled={change.isPending} loading={change.isPending}>
              {t('admin.studentCard.changeEnrollmentStatus')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
