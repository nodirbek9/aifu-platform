import { useRef, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Trash2 } from 'lucide-react'
import type { TFunction } from 'i18next'
import { useTranslation } from 'react-i18next'
import { usePairSlotsQuery, useRoomsQuery, type PairSlot } from '@/features/admin/reference-queries'
import { useTeachersQuery } from '@/features/admin/users-queries'
import { usePairName } from '@/features/timetable/use-pair-name'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDate } from '@/shared/lib/dates'
import { Button } from '@/shared/ui/button'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { ErrorState } from '@/shared/ui/error-state'
import { SkeletonText } from '@/shared/ui/skeleton'
import { clashOf } from './model'
import { useAddException, useDeleteException, useExceptionsQuery } from './queries'

type OneOffChangesProps = {
  entryId?: string
  selectedDate?: string
  initialKind?: (typeof KINDS)[number]
  sourceClasses?: readonly { value: string; label: string }[]
}

/** "Tushdan keyin · 1-juftlik · 13:30–14:50": a pair as the administrator picks it. */
function slotLabel(slot: PairSlot, t: TFunction): string {
  return [
    t(`timetable.shiftShort.${slot.shift ?? 'MORNING'}`),
    t('admin.pairNo', { number: slot.shiftPairNo ?? 0 }),
    `${(slot.startsAt ?? '').slice(0, 5)}–${(slot.endsAt ?? '').slice(0, 5)}`,
  ].join(' · ')
}

const KINDS = ['CANCELLED', 'MOVED', 'REPLACED', 'ADD'] as const
const formSchema = z
  .object({
    kind: z.enum(KINDS),
    onDate: z.iso.date(),
    newDate: z.string(),
    newSlot: z.string(),
    newRoom: z.string(),
    newTeacher: z.string(),
    reason: z.string().trim().min(1),
  })
  .refine((value) => value.kind !== 'ADD' || value.newSlot !== '')
  .refine(
    (value) =>
      value.kind !== 'MOVED' || (z.iso.date().safeParse(value.newDate).success && value.newSlot !== ''),
  )
  .refine((value) => value.kind !== 'REPLACED' || value.newRoom !== '' || value.newTeacher !== '')

/** A-02.7 — a class cancelled, moved or given another teacher or room on one date, with a reason. */
export function OneOffChanges({
  entryId,
  selectedDate = '',
  initialKind = 'CANCELLED',
  sourceClasses,
}: OneOffChangesProps) {
  const { t } = useTranslation()
  const pairName = usePairName()
  const locale = useLocale()
  const [sourceId, setSourceId] = useState(entryId ?? '')
  const effectiveEntryId = entryId ?? sourceClasses?.find((source) => source.value === sourceId)?.value
  const changes = useExceptionsQuery(effectiveEntryId)
  const add = useAddException(effectiveEntryId ?? '', locale)
  const remove = useDeleteException(effectiveEntryId ?? '')
  const slots = usePairSlotsQuery()
  const rooms = useRoomsQuery()
  const teachers = useTeachersQuery()
  const form = useForm({
    resolver: zodResolver(formSchema),
    defaultValues: {
      kind: initialKind,
      onDate: selectedDate,
      newDate: '',
      newSlot: '',
      newRoom: '',
      newTeacher: '',
      reason: '',
    },
  })
  const {
    kind = initialKind,
    onDate = '',
    newDate = '',
    newSlot = '',
    newRoom = '',
    newTeacher = '',
    reason = '',
  } = useWatch({ control: form.control })
  const submitting = useRef(false)
  const [added, setAdded] = useState(false)
  const submit = () =>
    form.handleSubmit((value) => {
      if (submitting.current || add.isPending || effectiveEntryId === undefined) return
      submitting.current = true
      setAdded(false)
      add.mutate(
        {
          kind: value.kind,
          onDate: value.onDate,
          reason: value.reason,
          ...(value.kind === 'MOVED' && { newDate: value.newDate, newPairSlotId: value.newSlot }),
          ...(value.kind === 'ADD' && { newPairSlotId: value.newSlot }),
          ...((value.kind === 'ADD' || value.kind === 'REPLACED') &&
            value.newRoom !== '' && { newRoomId: value.newRoom }),
          ...((value.kind === 'ADD' || value.kind === 'REPLACED') &&
            value.newTeacher !== '' && { newTeacherUserId: value.newTeacher }),
        },
        {
          onSuccess: () => {
            setAdded(value.kind === 'ADD')
            form.reset({ ...value, reason: '', onDate: '', newTeacher: '', newRoom: '' })
          },
          onSettled: () => {
            submitting.current = false
          },
        },
      )
    })()
  const problem = add.isError ? toApiProblem(add.error) : undefined
  const conflicts = problem?.code === 'TIMETABLE_CONFLICT' ? clashOf(problem.details).conflicts : []
  return (
    <section className="border-border grid grid-cols-1 gap-3 border-t pt-4">
      <h3 className="font-display text-h3 text-text font-semibold">{t('builder.oneOff')}</h3>
      {sourceClasses !== undefined && (
        <>
          {sourceClasses.length === 0 && (
            <p className="text-small text-text-muted">{t('builder.noSourceClass')}</p>
          )}
          <Select
            label={t('builder.sourceClass')}
            placeholder={t('builder.chooseSourceClass')}
            options={sourceClasses}
            value={effectiveEntryId}
            disabled={add.isPending}
            onValueChange={(value) => {
              setSourceId(value)
              add.reset()
              setAdded(false)
            }}
          />
        </>
      )}
      {changes.isLoading && <SkeletonText lines={2} />}
      {changes.isError && (
        <ErrorState
          problem={toApiProblem(changes.error)}
          onRetry={() => {
            void changes.refetch()
          }}
        />
      )}
      <ul className="grid grid-cols-1 gap-1">
        {(changes.data ?? []).map((change) => (
          <li key={change.id} className="text-small flex items-center gap-2">
            <span className="text-text min-w-0 flex-1">
              {change.onDate === undefined ? '' : formatDate(change.onDate, locale)} ·{' '}
              {t(`builder.kind.${change.kind ?? 'CANCELLED'}`)}
              {change.newDate !== undefined && ` → ${formatDate(change.newDate, locale)}`}
              {change.newPairNo !== undefined && `, ${pairName(change.newPairNo)}`}
              {change.newRoomLabel !== undefined && `, ${change.newRoomLabel}`}
              {change.newTeacherName !== undefined && `, ${change.newTeacherName}`}
              {change.reason !== undefined && change.reason !== '' && (
                <span className="text-text-muted"> — {change.reason}</span>
              )}
            </span>
            <IconButton
              size="sm"
              aria-label={t('builder.deleteChange')}
              onClick={() => {
                remove.mutate(change.id ?? '')
              }}
            >
              <Trash2 aria-hidden className="size-4" />
            </IconButton>
          </li>
        ))}
      </ul>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <Input
          label={kind === 'ADD' ? t('builder.singleClassDate') : t('builder.onDate')}
          type="date"
          value={onDate}
          onChange={(event) => {
            form.setValue('onDate', event.target.value)
          }}
        />
        <Select
          label={t('builder.changeKind')}
          value={kind}
          disabled={add.isPending}
          onValueChange={(value) => {
            form.setValue('kind', KINDS.find((item) => item === value) ?? 'CANCELLED')
            add.reset()
            setAdded(false)
          }}
          options={KINDS.map((value) => ({ value, label: t(`builder.kind.${value}`) }))}
        />
        {kind === 'MOVED' && (
          <>
            <Input
              label={t('builder.newDate')}
              type="date"
              value={newDate}
              onChange={(event) => {
                form.setValue('newDate', event.target.value)
              }}
            />
            <Select
              label={t('builder.pair')}
              value={newSlot || undefined}
              placeholder={t('authoring.choose')}
              onValueChange={(value) => {
                form.setValue('newSlot', value)
              }}
              options={(slots.data ?? []).map((slot) => ({
                value: slot.id ?? '',
                label: slotLabel(slot, t),
              }))}
            />
          </>
        )}
        {kind === 'ADD' && (
          <Select
            label={t('builder.pair')}
            value={newSlot || undefined}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              form.setValue('newSlot', value)
            }}
            options={(slots.data ?? []).map((slot) => ({ value: slot.id ?? '', label: slotLabel(slot, t) }))}
          />
        )}
        {(kind === 'REPLACED' || kind === 'ADD') && (
          <>
            <Select
              label={t('builder.replacementTeacher')}
              value={newTeacher || 'same'}
              placeholder={t('builder.sameTeacher')}
              onValueChange={(value) => {
                form.setValue('newTeacher', value === 'same' ? '' : value)
              }}
              options={[
                { value: 'same', label: t('builder.sameTeacher') },
                ...(teachers.data ?? []).map((teacher) => ({
                  value: teacher.id ?? '',
                  label: teacher.fullName ?? '',
                })),
              ]}
            />
            <Select
              label={t('admin.room')}
              value={newRoom || 'same'}
              placeholder={t('builder.sameRoom')}
              onValueChange={(value) => {
                form.setValue('newRoom', value === 'same' ? '' : value)
              }}
              options={[
                { value: 'same', label: t('builder.sameRoom') },
                ...(rooms.data ?? []).map((room) => ({ value: room.id ?? '', label: room.label ?? '' })),
              ]}
            />
          </>
        )}
        <Input
          className="sm:col-span-2"
          label={t('admin.reason')}
          value={reason}
          onChange={(event) => {
            form.setValue('reason', event.target.value)
          }}
        />
      </div>
      {problem !== undefined && <ErrorState problem={problem} />}
      {conflicts.map((conflict, index) => (
        <p key={index} role="alert" className="text-small text-danger">
          {t(`builder.conflict.${conflict.type ?? 'GROUP'}`)} ·{' '}
          {[
            conflict.subjectCode,
            conflict.courseTitle,
            conflict.teacherName,
            conflict.roomLabel,
            conflict.groupCodes?.join(', '),
            conflict.pairNo === undefined ? undefined : pairName(conflict.pairNo),
          ]
            .filter(Boolean)
            .join(' · ')}
        </p>
      ))}
      {added && (
        <p role="status" className="text-small text-success">
          {t('builder.singleClassAdded')}
        </p>
      )}
      <Button
        variant="outline"
        className="justify-self-start"
        disabled={
          add.isPending ||
          effectiveEntryId === undefined ||
          onDate === '' ||
          reason.trim() === '' ||
          (kind === 'MOVED' && (newDate === '' || newSlot === '')) ||
          (kind === 'ADD' && newSlot === '') ||
          (kind === 'REPLACED' && newTeacher === '' && newRoom === '')
        }
        loading={add.isPending}
        onClick={() => {
          void submit()
        }}
      >
        {kind === 'ADD' ? t('builder.addSingleClass') : t('builder.addChange')}
      </Button>
    </section>
  )
}
