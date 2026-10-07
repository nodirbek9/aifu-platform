import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { AlertTriangle, CheckCircle2, OctagonX, Trash2 } from 'lucide-react'
import type { TFunction } from 'i18next'
import { useTranslation } from 'react-i18next'
import {
  useAllGroupsQuery,
  usePairSlotsQuery,
  useRoomsQuery,
  type PairSlot,
} from '@/features/admin/reference-queries'
import { useAdminCourseQuery, useCoursesQuery } from '@/features/admin/structure-queries'
import { useTeachersQuery } from '@/features/admin/users-queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDate } from '@/shared/lib/dates'
import { useDebouncedValue } from '@/shared/lib/use-debounced-value'
import { SESSION_TYPES } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Checkbox } from '@/shared/ui/checkbox'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { DAYS, clashOf, requestOf, type Clash, type Parity } from './model'
import {
  checkEntry,
  useAddException,
  useDeleteEntry,
  useDeleteException,
  useExceptionsQuery,
  useSaveEntry,
  type Entry,
  type EntryRequest,
  type Timetable,
} from './queries'
import { usePairName } from '@/features/timetable/use-pair-name'

/** The request without one optional field: an emptied input means "not set". */
/** "Tushdan keyin · 1-juftlik · 13:30–14:50": a pair as the administrator picks it. */
function slotLabel(slot: PairSlot, t: TFunction): string {
  return [
    t(`timetable.shiftShort.${slot.shift ?? 'MORNING'}`),
    t('admin.pairNo', { number: slot.shiftPairNo ?? 0 }),
    `${(slot.startsAt ?? '').slice(0, 5)}–${(slot.endsAt ?? '').slice(0, 5)}`,
  ].join(' · ')
}

function without(request: EntryRequest, key: 'roomId' | 'validFrom' | 'validTo'): EntryRequest {
  const copy = { ...request }
  copy[key] = undefined
  return copy
}

export type Undo = { label: string; run: () => Promise<unknown> }

type EntryPanelProps = {
  timetable: Timetable
  /** The class being edited; absent for a new one. */
  entry?: Entry
  /** Where a new class goes and who it is for, from the cell that was clicked. */
  start?: { day: number; pairSlotId: string; groupId?: string; teacherId?: string; roomId?: string }
  editable: boolean
  onDone: (undo?: Undo) => void
}

/** "Teacher busy: DAS1110 Dasturlash — Akbarov B., BK-DI26/1, B-301, Monday, pair 2". */
export function ClashList({ clash }: { clash: Clash }) {
  const { t } = useTranslation()
  const pairName = usePairName()
  if (clash.conflicts.length === 0 && clash.warnings.length === 0) return null
  return (
    <div className="grid grid-cols-1 gap-2">
      {clash.conflicts.map((conflict, index) => (
        <p
          key={`${conflict.type ?? ''}-${String(index)}`}
          role="alert"
          className="bg-danger-soft text-small text-danger flex gap-2 rounded-sm px-3 py-2"
        >
          <OctagonX aria-hidden className="mt-0.5 size-4 shrink-0" />
          <span>
            <strong>{t(`builder.conflict.${conflict.type ?? 'GROUP'}`)}</strong>{' '}
            {[
              [conflict.subjectCode, conflict.courseTitle].filter(Boolean).join(' '),
              conflict.teacherName,
              conflict.groupCodes?.join(', '),
              conflict.roomLabel,
              conflict.dayOfWeek === undefined ? undefined : t(`builder.days.${conflict.dayOfWeek}`),
              conflict.pairNo === undefined ? undefined : pairName(conflict.pairNo),
            ]
              .filter((part) => part !== undefined && part !== '')
              .join(' · ')}
          </span>
        </p>
      ))}
      {clash.warnings.map((warning, index) => (
        <p
          key={`${warning.code ?? ''}-${String(index)}`}
          className="bg-warning-soft text-small text-warning flex gap-2 rounded-sm px-3 py-2"
        >
          <AlertTriangle aria-hidden className="mt-0.5 size-4 shrink-0" />
          <span>
            {warning.code === undefined
              ? warning.message
              : t(`builder.warning.${warning.code}`, warning.message ?? '')}
          </span>
        </p>
      ))}
    </div>
  )
}

/** A-02.3 / A-02.4 — a class added or edited, checked as it is typed, saved only when it fits. */
export function EntryPanel({ timetable, entry, start, editable, onDone }: EntryPanelProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const timetableId = timetable.id ?? ''
  const courses = useCoursesQuery({
    query: '',
    ...(timetable.termId !== undefined && { termId: timetable.termId }),
    page: 0,
    size: 100,
  })
  const groups = useAllGroupsQuery()
  const rooms = useRoomsQuery()
  const slots = usePairSlotsQuery()
  const save = useSaveEntry(timetableId, locale)
  const remove = useDeleteEntry()
  const [form, setForm] = useState<EntryRequest>(() =>
    entry !== undefined
      ? requestOf(entry)
      : {
          courseId: '',
          sessionType: 'LECTURE',
          teacherUserId: start?.teacherId ?? '',
          groupIds: start?.groupId === undefined ? [] : [start.groupId],
          pairSlotId: start?.pairSlotId ?? '',
          dayOfWeek: start?.day ?? 1,
          weekParity: 'EVERY',
          ...(start?.roomId !== undefined && { roomId: start.roomId }),
        },
  )
  const [reason, setReason] = useState('')
  const [refused, setRefused] = useState<Clash>()
  const course = useAdminCourseQuery(form.courseId)
  const complete =
    form.courseId !== '' && form.teacherUserId !== '' && form.groupIds.length > 0 && form.pairSlotId !== ''
  const settled = useDebouncedValue(form, 400)
  const check = useQuery({
    queryKey: ['admin', 'entry-check', timetableId, entry?.id, settled],
    queryFn: () => checkEntry(timetableId, settled, entry?.id, locale),
    enabled: editable && complete && settled === form,
  })
  const set = (patch: Partial<EntryRequest>) => {
    setRefused(undefined)
    setForm({ ...form, ...patch })
  }
  const live = check.data === undefined ? undefined : clashOf(check.data)
  const shown = refused ?? live
  const hard = (shown?.conflicts.length ?? 0) > 0
  const soft = !hard && (shown?.warnings.length ?? 0) > 0

  const submit = (force: boolean) => {
    const request: EntryRequest = { ...form, ...(force && { force: true, forceReason: reason.trim() }) }
    save.mutate(
      { ...(entry?.id !== undefined && { id: entry.id }), request },
      {
        onSuccess: (saved) => {
          const previous = entry === undefined ? undefined : requestOf(entry)
          onDone(
            entry === undefined || previous === undefined
              ? { label: t('builder.undoAdd'), run: () => remove.mutateAsync(saved.id ?? '') }
              : {
                  label: t('builder.undoEdit'),
                  run: () =>
                    save.mutateAsync({
                      id: entry.id,
                      request: { ...previous, force: true, forceReason: t('builder.undoReason') },
                    }),
                },
          )
        },
        onError: (failure) => {
          const problem = toApiProblem(failure)
          if (problem.code === 'TIMETABLE_CONFLICT') setRefused(clashOf(problem.details))
        },
      },
    )
  }

  const teachers = course.data?.teachers ?? []
  const sessionTypes = (course.data?.sessionAllocations ?? [])
    .filter((row) => (row.allocatedHours ?? 0) > 0)
    .map((row) => row.sessionType)

  return (
    <form
      className="grid grid-cols-1 gap-4"
      onSubmit={(event) => {
        event.preventDefault()
        if (complete && !hard) submit(soft)
      }}
    >
      <fieldset disabled={!editable} className="grid grid-cols-1 gap-4">
        <Select
          label={t('admin.course')}
          value={form.courseId === '' ? undefined : form.courseId}
          placeholder={t('authoring.choose')}
          onValueChange={(value) => {
            set({ courseId: value, teacherUserId: start?.teacherId ?? '' })
          }}
          options={(courses.data?.content ?? []).map((item) => ({
            value: item.id ?? '',
            label: `${item.subjectCode ?? ''} · ${item.title ?? ''}`,
          }))}
        />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select
            label={t('authoring.sessionType')}
            value={form.sessionType}
            onValueChange={(value) => {
              const type = SESSION_TYPES.find((item) => item === value)
              if (type !== undefined) set({ sessionType: type })
            }}
            options={SESSION_TYPES.filter(
              (type) => sessionTypes.length === 0 || sessionTypes.includes(type) || type === form.sessionType,
            ).map((type) => ({
              value: type,
              label: t(`sessionType.${type}`),
            }))}
          />
          <Select
            label={t('admin.teacher')}
            value={form.teacherUserId === '' ? undefined : form.teacherUserId}
            placeholder={form.courseId === '' ? t('builder.courseFirst') : t('authoring.choose')}
            onValueChange={(value) => {
              set({ teacherUserId: value })
            }}
            options={teachers.map((teacher) => ({
              value: teacher.userId ?? '',
              label: teacher.fullName ?? '',
            }))}
          />
        </div>
        <fieldset className="grid grid-cols-1 gap-2">
          <legend className="text-small text-text mb-1 font-medium">{t('builder.groups')}</legend>
          <div className="flex max-h-36 flex-wrap gap-x-4 gap-y-2 overflow-y-auto">
            {(groups.data ?? [])
              .filter((group) => group.status !== 'ARCHIVED')
              .map((group) => (
                <Checkbox
                  key={group.id}
                  label={group.code}
                  checked={form.groupIds.includes(group.id ?? '')}
                  onCheckedChange={(checked) => {
                    set({
                      groupIds: checked
                        ? [...form.groupIds, group.id ?? '']
                        : form.groupIds.filter((id) => id !== group.id),
                    })
                  }}
                />
              ))}
          </div>
        </fieldset>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Select
            label={t('builder.day')}
            value={String(form.dayOfWeek ?? 1)}
            onValueChange={(value) => {
              set({ dayOfWeek: Number(value) })
            }}
            options={DAYS.map((day) => ({ value: String(day), label: t(`builder.days.${day}`) }))}
          />
          <Select
            label={t('builder.pair')}
            value={form.pairSlotId === '' ? undefined : form.pairSlotId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              set({ pairSlotId: value })
            }}
            options={(slots.data ?? [])
              .slice()
              .sort((a, b) => (a.startsAt ?? '').localeCompare(b.startsAt ?? ''))
              .map((slot) => ({ value: slot.id ?? '', label: slotLabel(slot, t) }))}
          />
          <Select
            label={t('admin.room')}
            value={form.roomId ?? 'none'}
            onValueChange={(value) => {
              setRefused(undefined)
              setForm(value === 'none' ? without(form, 'roomId') : { ...form, roomId: value })
            }}
            options={[
              { value: 'none', label: t('builder.noRoom') },
              ...(rooms.data ?? [])
                .filter((room) => room.active !== false)
                .map((room) => ({
                  value: room.id ?? '',
                  label: `${room.label ?? room.number ?? ''} · ${t(`admin.roomTypes.${room.roomType ?? 'OTHER'}`)} · ${room.capacity ?? '?'}`,
                })),
            ]}
          />
          <div className="grid grid-cols-1 gap-1.5">
            <span className="text-small text-text font-medium">{t('builder.weeks')}</span>
            <SegmentedControl
              size="sm"
              aria-label={t('builder.weeks')}
              value={form.weekParity ?? 'EVERY'}
              onValueChange={(value) => {
                if (value === 'EVERY' || value === 'ODD' || value === 'EVEN')
                  set({ weekParity: value satisfies Parity })
              }}
              options={[
                { value: 'EVERY', label: t('builder.parity.EVERY') },
                { value: 'ODD', label: t('builder.parity.ODD') },
                { value: 'EVEN', label: t('builder.parity.EVEN') },
              ]}
            />
          </div>
          <Input
            label={t('builder.validFrom')}
            type="date"
            value={form.validFrom ?? ''}
            onChange={(event) => {
              setForm(
                event.target.value === ''
                  ? without(form, 'validFrom')
                  : { ...form, validFrom: event.target.value },
              )
            }}
          />
          <Input
            label={t('builder.validTo')}
            type="date"
            value={form.validTo ?? ''}
            onChange={(event) => {
              setForm(
                event.target.value === ''
                  ? without(form, 'validTo')
                  : { ...form, validTo: event.target.value },
              )
            }}
          />
          <Input
            label={t('builder.subgroup')}
            value={form.subgroup ?? ''}
            placeholder="1"
            onChange={(event) => {
              set({ subgroup: event.target.value })
            }}
          />
          <Input
            label={t('builder.note')}
            value={form.note ?? ''}
            onChange={(event) => {
              set({ note: event.target.value })
            }}
          />
        </div>
      </fieldset>

      {editable && complete && (
        <div aria-live="polite" className="grid grid-cols-1 gap-2">
          {check.isFetching && <p className="text-small text-text-muted">{t('builder.checking')}</p>}
          {!check.isFetching && shown !== undefined && !hard && !soft && (
            <p className="text-small text-success flex items-center gap-2">
              <CheckCircle2 aria-hidden className="size-4" />
              {t('builder.fits')}
            </p>
          )}
          {shown !== undefined && <ClashList clash={shown} />}
        </div>
      )}
      {soft && editable && (
        <Input
          label={t('builder.overrideReason')}
          value={reason}
          onChange={(event) => {
            setReason(event.target.value)
          }}
        />
      )}
      {save.isError && toApiProblem(save.error).code !== 'TIMETABLE_CONFLICT' && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(save.error).code}`)}
        </p>
      )}
      {editable && (
        <div className="flex flex-wrap gap-2">
          <Button
            type="submit"
            variant={soft ? 'danger' : 'primary'}
            disabled={!complete || hard || (soft && reason.trim().length < 3)}
            loading={save.isPending}
          >
            {soft ? t('builder.saveAnyway') : t('authoring.save')}
          </Button>
          {entry?.id !== undefined && (
            <Button
              variant="ghost"
              loading={remove.isPending}
              onClick={() => {
                const previous = requestOf(entry)
                remove.mutate(entry.id ?? '', {
                  onSuccess: () => {
                    onDone({
                      label: t('builder.undoDelete'),
                      run: () => save.mutateAsync({ request: previous }),
                    })
                  },
                })
              }}
            >
              <Trash2 aria-hidden className="size-4" />
              {t('builder.deleteClass')}
            </Button>
          )}
        </div>
      )}
      {entry?.id !== undefined && <OneOffChanges entryId={entry.id} />}
    </form>
  )
}

const KINDS = ['CANCELLED', 'MOVED', 'REPLACED'] as const

/** A-02.7 — a class cancelled, moved or given another teacher or room on one date, with a reason. */
function OneOffChanges({ entryId }: { entryId: string }) {
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
