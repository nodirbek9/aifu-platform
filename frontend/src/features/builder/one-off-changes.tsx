import { useState } from 'react'
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
import { useAddException, useDeleteException, useExceptionsQuery } from './queries'

type OneOffChangesProps = { entryId: string }

/** "Tushdan keyin · 1-juftlik · 13:30–14:50": a pair as the administrator picks it. */
function slotLabel(slot: PairSlot, t: TFunction): string {
  return [
    t(`timetable.shiftShort.${slot.shift ?? 'MORNING'}`),
    t('admin.pairNo', { number: slot.shiftPairNo ?? 0 }),
    `${(slot.startsAt ?? '').slice(0, 5)}–${(slot.endsAt ?? '').slice(0, 5)}`,
  ].join(' · ')
}

const KINDS = ['CANCELLED', 'MOVED', 'REPLACED'] as const

/** A-02.7 — a class cancelled, moved or given another teacher or room on one date, with a reason. */
export function OneOffChanges({ entryId }: OneOffChangesProps) {
  const { t } = useTranslation()
  const pairName = usePairName()
  const locale = useLocale()
  const changes = useExceptionsQuery(entryId)
  const add = useAddException(entryId, locale)
  const remove = useDeleteException(entryId)
  const slots = usePairSlotsQuery()
  const rooms = useRoomsQuery()
  const teachers = useTeachersQuery()
  const [kind, setKind] = useState<(typeof KINDS)[number]>('CANCELLED')
  const [onDate, setOnDate] = useState('')
  const [newDate, setNewDate] = useState('')
  const [newSlot, setNewSlot] = useState<string>()
  const [newRoom, setNewRoom] = useState<string>()
  const [newTeacher, setNewTeacher] = useState<string>()
  const [reason, setReason] = useState('')
  return (
    <section className="border-border grid grid-cols-1 gap-3 border-t pt-4">
      <h3 className="font-display text-h3 text-text font-semibold">{t('builder.oneOff')}</h3>
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
          label={t('builder.onDate')}
          type="date"
          value={onDate}
          onChange={(event) => {
            setOnDate(event.target.value)
          }}
        />
        <Select
          label={t('builder.changeKind')}
          value={kind}
          onValueChange={(value) => {
            setKind(KINDS.find((item) => item === value) ?? 'CANCELLED')
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
                setNewDate(event.target.value)
              }}
            />
            <Select
              label={t('builder.pair')}
              value={newSlot}
              placeholder={t('authoring.choose')}
              onValueChange={setNewSlot}
              options={(slots.data ?? []).map((slot) => ({
                value: slot.id ?? '',
                label: slotLabel(slot, t),
              }))}
            />
          </>
        )}
        {kind === 'REPLACED' && (
          <>
            <Select
              label={t('builder.replacementTeacher')}
              value={newTeacher}
              placeholder={t('builder.sameTeacher')}
              onValueChange={setNewTeacher}
              options={(teachers.data ?? []).map((teacher) => ({
                value: teacher.id ?? '',
                label: teacher.fullName ?? '',
              }))}
            />
            <Select
              label={t('admin.room')}
              value={newRoom}
              placeholder={t('builder.sameRoom')}
              onValueChange={setNewRoom}
              options={(rooms.data ?? []).map((room) => ({ value: room.id ?? '', label: room.label ?? '' }))}
            />
          </>
        )}
        <Input
          className="sm:col-span-2"
          label={t('admin.reason')}
          value={reason}
          onChange={(event) => {
            setReason(event.target.value)
          }}
        />
      </div>
      {add.isError && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(add.error).code}`)}
        </p>
      )}
      <Button
        variant="outline"
        className="justify-self-start"
        disabled={
          onDate === '' ||
          reason.trim() === '' ||
          (kind === 'MOVED' && (newDate === '' || newSlot === undefined)) ||
          (kind === 'REPLACED' && newTeacher === undefined && newRoom === undefined)
        }
        loading={add.isPending}
        onClick={() => {
          add.mutate(
            {
              kind,
              onDate,
              reason: reason.trim(),
              ...(kind === 'MOVED' && { newDate, ...(newSlot !== undefined && { newPairSlotId: newSlot }) }),
              ...(kind === 'REPLACED' && newRoom !== undefined && { newRoomId: newRoom }),
              ...(kind === 'REPLACED' && newTeacher !== undefined && { newTeacherUserId: newTeacher }),
            },
            {
              onSuccess: () => {
                setReason('')
                setOnDate('')
                setNewTeacher(undefined)
                setNewRoom(undefined)
              },
            },
          )
        }}
      >
        {t('builder.addChange')}
      </Button>
    </section>
  )
}
