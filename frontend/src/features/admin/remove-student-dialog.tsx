import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useRef, useState, type SyntheticEvent } from 'react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { z } from 'zod'
import { api, unwrap } from '@/shared/api/client'
import { ApiFailure, toApiProblem } from '@/shared/api/errors'
import { Button } from '@/shared/ui/button'
import { Checkbox } from '@/shared/ui/checkbox'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { ErrorState } from '@/shared/ui/error-state'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import {
  useRemoveStudent,
  type RemovalOutcome,
  type RemoveStudentInput,
  type RemoveStudentResult,
} from './remove-student-queries'
import type { EnrollmentSummary } from './student-enrollment-model'
import { studentEnrollmentKeys } from './student-enrollment-queries'
import type { UserDetail } from './users-queries'

type EligibleEnrollment = EnrollmentSummary & Required<Pick<EnrollmentSummary, 'id' | 'courseId' | 'status'>>

function isEligible(row: EnrollmentSummary): row is EligibleEnrollment {
  return (
    row.id !== undefined &&
    row.courseId !== undefined &&
    (row.status === 'ACTIVE' || row.status === 'PENDING' || row.status === 'SUSPENDED')
  )
}

/** Keep every page in Query, and publish no selection until the complete read succeeds. */
function useRemovalEnrollments(userId: string) {
  return useQuery({
    queryKey: [...studentEnrollmentKeys.user(userId), 'removal'],
    enabled: userId !== '',
    queryFn: async ({ signal }) => {
      const rows: EnrollmentSummary[] = []
      for (let page = 0; ; page += 1) {
        const data = await unwrap(
          api.GET('/api/v1/admin/enrollments', {
            params: { query: { userId, page, size: 100, sort: ['enrolledAt,desc', 'id,asc'] } },
            signal,
          }),
        )
        if (data.content === undefined || data.hasNext === undefined) {
          throw new ApiFailure({ status: 0, code: 'INVALID_STATE_TRANSITION', message: '' })
        }
        rows.push(...data.content)
        if (!data.hasNext) return rows
      }
    },
  })
}

type RemoveStudentDialogProps = { user: UserDetail; onClose: () => void }

/** 7.3-F2: explicit selection, one confirmation, and persistent non-atomic results. */
export function RemoveStudentDialog({ user, onClose }: RemoveStudentDialogProps) {
  const { t } = useTranslation()
  const enrollments = useRemovalEnrollments(user.id ?? '')
  const removal = useRemoveStudent()
  const [selected, setSelected] = useState<string[]>([])
  const [operation, setOperation] = useState<RemoveStudentInput>()
  const [result, setResult] = useState<RemoveStudentResult>()
  const [opener] = useState(() => document.activeElement)
  const submitting = useRef(false)
  const schema = z.object({ reason: z.string().trim().min(1) })
  const form = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    defaultValues: { reason: '' },
  })
  const eligible = (enrollments.data ?? []).filter(isEligible)
  const ready = enrollments.isSuccess && !enrollments.isFetching
  const frozen = operation !== undefined || removal.isPending

  const run = (input: RemoveStudentInput) => {
    if (submitting.current || removal.isPending) return
    submitting.current = true
    setOperation(input)
    removal.mutate(input, {
      onSuccess: setResult,
      onSettled: () => {
        submitting.current = false
      },
    })
  }
  const submit = (event: SyntheticEvent<HTMLFormElement>) => {
    void form.handleSubmit(({ reason }) => {
      if (!ready || operation !== undefined || !user.id) return
      run({
        confirmed: true,
        userId: user.id,
        reason,
        enrollments: eligible
          .filter((row) => selected.includes(row.id))
          .map(({ id, courseId }) => ({ id, courseId })),
      })
    })(event)
  }
  const retry = () => {
    if (operation) run(operation)
  }

  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open && !removal.isPending) onClose()
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
        onEscapeKeyDown={(event) => {
          if (removal.isPending) event.preventDefault()
        }}
        onPointerDownOutside={(event) => {
          if (removal.isPending) event.preventDefault()
        }}
      >
        <DialogTitle>{t('admin.removeStudent.title')}</DialogTitle>
        <DialogDescription>{t('admin.removeStudent.description')}</DialogDescription>
        <p className="text-body text-text mt-4 font-medium wrap-anywhere">
          {user.fullName} · {user.username}
        </p>
        <form className="mt-4 grid grid-cols-1 gap-4" onSubmit={submit}>
          {operation === undefined && (enrollments.isPending || enrollments.isFetching) && (
            <Skeleton className="h-32" />
          )}
          {operation === undefined && enrollments.isError && (
            <ErrorState
              problem={toApiProblem(enrollments.error)}
              onRetry={() => void enrollments.refetch()}
            />
          )}
          {operation === undefined && ready && (
            <fieldset disabled={frozen} className="grid min-w-0 grid-cols-1 gap-3">
              <legend className="text-small text-text mb-3 font-medium">
                {t('admin.removeStudent.selectCourses')}
              </legend>
              {eligible.length > 0 && (
                <Checkbox
                  label={t('admin.removeStudent.allEligible')}
                  checked={eligible.every((row) => selected.includes(row.id))}
                  onCheckedChange={(checked) => setSelected(checked ? eligible.map((row) => row.id) : [])}
                />
              )}
              {eligible.map((row) => (
                <Checkbox
                  key={row.id}
                  checked={selected.includes(row.id)}
                  label={
                    <span className="text-small wrap-anywhere">
                      {[row.subjectCode, row.cohortCode, row.termName].filter(Boolean).join(' · ') ||
                        row.courseId}
                      {' · '}
                      {t(`admin.enrollmentStatus.${row.status}`)}
                    </span>
                  }
                  onCheckedChange={(checked) =>
                    setSelected((previous) =>
                      checked ? [...previous, row.id] : previous.filter((id) => id !== row.id),
                    )
                  }
                />
              ))}
              {(eligible.length === 0 || selected.length === 0) && (
                <p className="text-small text-text-muted">{t('admin.removeStudent.deactivateOnly')}</p>
              )}
            </fieldset>
          )}
          <Textarea
            label={t('admin.reason')}
            {...form.register('reason')}
            required
            disabled={frozen}
            error={
              form.formState.errors.reason === undefined ? undefined : t('admin.studentCard.reasonRequired')
            }
          />
          {result !== undefined && result.status !== 'cancelled' && (
            <section className="grid grid-cols-1 gap-3" aria-live="polite">
              <p className="text-body text-text font-medium">
                {result.status === 'complete'
                  ? t('admin.removeStudent.complete')
                  : t('admin.removeStudent.partial')}
              </p>
              <Outcome label={t('admin.status')} outcome={result.account} />
              {result.enrollments.map((row) => {
                const source = enrollments.data?.find((item) => item.id === row.id)
                return (
                  <Outcome
                    key={row.id}
                    label={
                      [source?.subjectCode, source?.cohortCode, source?.termName]
                        .filter(Boolean)
                        .join(' · ') || row.courseId
                    }
                    outcome={row.outcome}
                  />
                )
              })}
            </section>
          )}
          {removal.isError && <ErrorState problem={toApiProblem(removal.error)} onRetry={retry} />}
          <DialogFooter className="mt-2 flex-col sm:flex-row">
            <Button variant="outline" disabled={removal.isPending} onClick={onClose}>
              {t(operation ? 'common.close' : 'common.cancel')}
            </Button>
            {operation === undefined ? (
              <Button
                type="submit"
                variant="danger"
                disabled={!ready || removal.isPending}
                loading={removal.isPending}
                className="h-auto min-h-11 whitespace-normal"
              >
                {t('admin.removeStudent.confirm')}
              </Button>
            ) : (
              result?.status !== 'complete' && (
                <Button
                  disabled={removal.isPending}
                  loading={removal.isPending}
                  onClick={retry}
                  className="h-auto min-h-11 whitespace-normal"
                >
                  {t('admin.removeStudent.retryRemaining')}
                </Button>
              )
            )}
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function Outcome({ label, outcome }: { label: string; outcome: RemovalOutcome }) {
  const { t } = useTranslation()
  return (
    <div className="grid grid-cols-1 gap-2">
      <p className="text-small text-text wrap-anywhere">
        {label} · {t(`admin.removeStudent.outcome.${outcome.status}`)}
      </p>
      {outcome.status === 'failed' && (
        <>
          <ErrorState problem={outcome.problem} />
          {outcome.verificationProblem && <ErrorState problem={outcome.verificationProblem} />}
        </>
      )}
    </div>
  )
}
