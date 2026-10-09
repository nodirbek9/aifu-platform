import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { z } from 'zod'
import { toApiProblem } from '@/shared/api/errors'
import { Button, button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { useToast } from '@/shared/ui/toast'
import { useAddGroupStudents, useAllGroupsQuery } from './reference-queries'
import type { UserDetail } from './users-queries'

const groupSchema = z.object({ groupId: z.string().min(1) })

/** A-05 / 7.1-F2: move membership through the existing atomic group endpoint. */
export function StudentGroupCard({ user }: { user: UserDetail }) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const groups = useAllGroupsQuery()
  const form = useForm<z.infer<typeof groupSchema>>({
    resolver: zodResolver(groupSchema),
    defaultValues: { groupId: '' },
  })
  const groupId = useWatch({ control: form.control, name: 'groupId' })
  const move = useAddGroupStudents(groupId)
  const code = user.studentProfile?.groupCode
  const current = groups.data?.find((group) => code !== undefined && group.code === code)
  const targets = (groups.data ?? []).flatMap((group) =>
    group.status !== 'ARCHIVED' && group.id && group.code && group.id !== current?.id
      ? [{ value: group.id, label: group.code }]
      : [],
  )
  const canMove = Boolean(user.id) && targets.some((target) => target.value === groupId)
  const submit = form.handleSubmit(() => {
    if (!canMove || user.id === undefined || move.isPending) return
    move.mutate([user.id], {
      onSuccess: () => {
        form.reset()
        toast({ tone: 'success', title: t('admin.studentCard.groupChanged') })
      },
    })
  })

  return (
    <Card>
      <CardHeader title={t('admin.studentCard.groupTitle')} />
      <CardContent className="grid grid-cols-1 gap-4">
        {groups.isPending ? (
          <Skeleton className="h-32" />
        ) : groups.isError ? (
          <ErrorState problem={toApiProblem(groups.error)} onRetry={() => void groups.refetch()} />
        ) : (
          <>
            {current === undefined ? (
              <p className="text-body text-text font-medium wrap-anywhere">
                {code === undefined || code === ''
                  ? t('admin.studentCard.noGroup')
                  : t('admin.studentCard.unknownGroup', { code })}
              </p>
            ) : (
              <Input label={t('profile.group')} value={current.code ?? ''} readOnly />
            )}
            {targets.length === 0 ? (
              <EmptyState
                title={t('admin.noGroups')}
                action={
                  <Link to="/admin/reference" className={button({ variant: 'outline', size: 'sm' })}>
                    {t('admin.newGroup')}
                  </Link>
                }
              />
            ) : (
              <form className="grid grid-cols-1 gap-3" onSubmit={(event) => void submit(event)}>
                <Controller
                  control={form.control}
                  name="groupId"
                  render={({ field }) => (
                    <Select
                      label={t('admin.studentCard.changeGroup')}
                      placeholder={t('profile.group')}
                      value={field.value}
                      onValueChange={field.onChange}
                      options={targets}
                      disabled={move.isPending}
                    />
                  )}
                />
                <Button
                  type="submit"
                  className="justify-self-start"
                  disabled={!canMove || move.isPending}
                  loading={move.isPending}
                >
                  {t('admin.studentCard.changeGroup')}
                </Button>
                {move.isError && (
                  <ErrorState problem={toApiProblem(move.error)} onRetry={() => void submit()} />
                )}
              </form>
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}
