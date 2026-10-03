import { useState } from 'react'
import { Archive, ArchiveRestore, Pencil, Plus, Trash2, UserMinus, UserPlus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { ConfirmDialog } from '@/features/authoring/confirm-dialog'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDate } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { PageHeader } from '@/shared/ui/page-header'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { Sheet, SheetBody, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { Skeleton } from '@/shared/ui/skeleton'
import { Switch } from '@/shared/ui/switch'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import {
  useAcademicYearsQuery,
  useAddGroupStudents,
  useAddHoliday,
  useAllGroupsQuery,
  useArchiveGroup,
  useCreateAcademicYear,
  useCreateTerm,
  useDeleteGroup,
  useDeleteHoliday,
  useDeletePairSlot,
  useDeleteRoom,
  useGroupStudentsListQuery,
  useHolidaysQuery,
  useMakeYearCurrent,
  usePairSlotsQuery,
  useProgrammesQuery,
  useRemoveGroupStudent,
  useRoomsQuery,
  useSaveGroup,
  useSavePairSlot,
  useSaveRoom,
  useSetParity,
  useTermsQuery,
  useTimetablesQuery,
  type AcademicYear,
  type Group,
  type GroupInput,
  type PairSlot,
  type Room,
  type RoomInput,
} from './reference-queries'
import { useTeachersQuery, useUsersQuery } from './users-queries'

const TABS = ['groups', 'rooms', 'bells', 'holidays', 'terms'] as const
const ROOM_TYPES = ['LECTURE_HALL', 'CLASSROOM', 'COMPUTER_LAB', 'LABORATORY', 'OTHER'] as const
const STUDY_FORMS = ['FULL_TIME', 'PART_TIME', 'EVENING', 'DISTANCE'] as const

function Problem({ error }: { error: unknown }) {
  const { t } = useTranslation()
  if (error === null || error === undefined) return null
  return (
    <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
      {t(`errors.${toApiProblem(error).code}`)}
    </p>
  )
}

/** A-03 — the records everything else is built on: groups, rooms, the bells, holidays, terms. */
export function ReferencePage() {
  const { t } = useTranslation()
  const [search, setSearch] = useSearchParams()
  const requested = search.get('tab')
  const tab = TABS.find((item) => item === requested) ?? 'groups'

  return (
    <div className="grid grid-cols-1 gap-6">
      <PageHeader title={t('nav.adminReference')} context={t('admin.referenceContext')} />
      <Tabs
        value={tab}
        onValueChange={(value) => {
          setSearch(value === 'groups' ? {} : { tab: value }, { replace: true })
        }}
      >
        <TabsList className="overflow-x-auto">
          {TABS.map((item) => (
            <TabsTrigger key={item} value={item}>
              {t(`admin.reference.${item}`)}
            </TabsTrigger>
          ))}
        </TabsList>
        <TabsContent value="groups" className="pt-6">
          {tab === 'groups' && <GroupsTab />}
        </TabsContent>
        <TabsContent value="rooms" className="pt-6">
          {tab === 'rooms' && <RoomsTab />}
        </TabsContent>
        <TabsContent value="bells" className="pt-6">
          {tab === 'bells' && <BellsTab />}
        </TabsContent>
        <TabsContent value="holidays" className="pt-6">
          {tab === 'holidays' && <HolidaysTab />}
        </TabsContent>
        <TabsContent value="terms" className="pt-6">
          {tab === 'terms' && <TermsTab />}
        </TabsContent>
      </Tabs>
    </div>
  )
}

function Loading({
  query,
}: {
  query: { isPending: boolean; isError: boolean; error: unknown; refetch: () => unknown }
}) {
  if (query.isPending) return <Skeleton className="h-64" />
  if (query.isError) {
    return (
      <ErrorState
        problem={toApiProblem(query.error)}
        onRetry={() => {
          void query.refetch()
        }}
      />
    )
  }
  return null
}

// ------------------------------------------------------------------ groups

function GroupsTab() {
  const { t } = useTranslation()
  const [search, setSearch] = useSearchParams()
  const groups = useAllGroupsQuery()
  const [editing, setEditing] = useState<Group | 'new'>()
  const [openId, setOpenId] = useState<string>()
  const query = search.get('q') ?? ''
  const showArchived = search.get('archived') === '1'
  const setParam = (key: string, value: string) => {
    const next = new URLSearchParams(search)
    if (value === '') next.delete(key)
    else next.set(key, value)
    setSearch(next, { replace: true })
  }
  if (!groups.isSuccess) return <Loading query={groups} />
  const rows = groups.data
    .filter((group) => showArchived || group.status !== 'ARCHIVED')
    .filter((group) => (group.code ?? '').toLowerCase().includes(query.trim().toLowerCase()))
    .sort((a, b) => (a.code ?? '').localeCompare(b.code ?? ''))
  const open = groups.data.find((group) => group.id === openId)

  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="flex flex-wrap items-end gap-3">
          <Input
            className="w-60"
            label={t('common.search')}
            type="search"
            value={query}
            onChange={(event) => {
              setParam('q', event.target.value)
            }}
          />
          <Switch
            className="pb-2"
            label={t('admin.showArchived')}
            checked={showArchived}
            onCheckedChange={(checked) => {
              setParam('archived', checked ? '1' : '')
            }}
          />
        </div>
        <Button
          onClick={() => {
            setEditing('new')
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('admin.newGroup')}
        </Button>
      </div>
      {rows.length === 0 ? (
        <EmptyState title={t('admin.noGroups')} />
      ) : (
        <div className="border-border overflow-x-auto rounded-lg border">
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>{t('admin.groupCode')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('admin.courseYear')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('admin.students')}</TableHeaderCell>
                <TableHeaderCell>{t('admin.curator')}</TableHeaderCell>
                <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((group) => (
                <TableRow key={group.id}>
                  <TableCell>
                    <button
                      type="button"
                      onClick={() => {
                        setOpenId(group.id)
                      }}
                      className="text-text hover:text-primary-hover font-semibold underline-offset-4 hover:underline"
                    >
                      {group.code}
                    </button>
                  </TableCell>
                  <TableCell numeric>{group.courseYear ?? '—'}</TableCell>
                  <TableCell numeric>
                    {group.studentCount ?? 0}
                    {group.plannedSize !== undefined && ` / ${group.plannedSize}`}
                  </TableCell>
                  <TableCell>{group.curatorName ?? '—'}</TableCell>
                  <TableCell>
                    <Badge tone={group.status === 'ARCHIVED' ? 'neutral' : 'success'}>
                      {t(`admin.groupStatus.${group.status ?? 'ACTIVE'}`)}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      {editing !== undefined && (
        <GroupDialog
          group={editing === 'new' ? undefined : editing}
          onClose={() => {
            setEditing(undefined)
          }}
        />
      )}
      <Sheet
        open={open !== undefined}
        onOpenChange={(next) => {
          if (!next) setOpenId(undefined)
        }}
      >
        <SheetContent>
          {open !== undefined && (
            <GroupSheet
              group={open}
              onEdit={() => {
                setEditing(open)
              }}
              onDeleted={() => {
                setOpenId(undefined)
              }}
            />
          )}
        </SheetContent>
      </Sheet>
    </div>
  )
}

function GroupDialog({ group, onClose }: { group: Group | undefined; onClose: () => void }) {
  const { t } = useTranslation()
  const save = useSaveGroup()
  const programmes = useProgrammesQuery()
  const teachers = useTeachersQuery()
  const [form, setForm] = useState({
    code: group?.code ?? '',
    programmeId: group?.programmeId ?? '',
    courseYear: group?.courseYear === undefined ? '1' : String(group.courseYear),
    admissionYear: group?.admissionYear === undefined ? '' : String(group.admissionYear),
    studyForm: group?.studyForm ?? 'FULL_TIME',
    instructionLanguage: group?.instructionLanguage ?? 'uz',
    plannedSize: group?.plannedSize === undefined ? '' : String(group.plannedSize),
    curatorUserId: group?.curatorUserId ?? '',
  })
  const set = (key: keyof typeof form, value: string) => {
    setForm({ ...form, [key]: value })
  }
  const whole = (value: string) => (value === '' ? undefined : Number.parseInt(value, 10))
  const studyForm = STUDY_FORMS.find((item) => item === form.studyForm)

  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent className="max-w-xl">
        <DialogTitle>
          {group === undefined ? t('admin.newGroup') : t('admin.editGroup', { code: group.code })}
        </DialogTitle>
        <DialogDescription>{t('admin.groupFormBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            const input: GroupInput = {
              code: form.code.trim(),
              ...(form.programmeId !== '' && { programmeId: form.programmeId }),
              ...(whole(form.courseYear) !== undefined && { courseYear: whole(form.courseYear) }),
              ...(whole(form.admissionYear) !== undefined && { admissionYear: whole(form.admissionYear) }),
              ...(studyForm !== undefined && { studyForm }),
              instructionLanguage: form.instructionLanguage,
              ...(whole(form.plannedSize) !== undefined && { plannedSize: whole(form.plannedSize) }),
              ...(form.curatorUserId !== '' && { curatorUserId: form.curatorUserId }),
            }
            save.mutate({ ...(group?.id !== undefined && { id: group.id }), input }, { onSuccess: onClose })
          }}
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label={t('admin.groupCode')}
              value={form.code}
              placeholder="BK-DI26/1"
              required
              onChange={(event) => {
                set('code', event.target.value)
              }}
            />
            <Select
              label={t('admin.programme')}
              value={form.programmeId === '' ? undefined : form.programmeId}
              placeholder={t('authoring.choose')}
              onValueChange={(value) => {
                set('programmeId', value)
              }}
              options={(programmes.data ?? []).map((item) => ({
                value: item.id ?? '',
                label: `${item.code ?? ''} · ${item.name ?? ''}`,
              }))}
            />
            <Input
              label={t('admin.courseYear')}
              type="number"
              min={1}
              max={6}
              value={form.courseYear}
              onChange={(event) => {
                set('courseYear', event.target.value)
              }}
            />
            <Input
              label={t('admin.admissionYear')}
              type="number"
              min={2000}
              value={form.admissionYear}
              onChange={(event) => {
                set('admissionYear', event.target.value)
              }}
            />
            <Select
              label={t('admin.studyForm')}
              value={form.studyForm}
              onValueChange={(value) => {
                set('studyForm', value)
              }}
              options={STUDY_FORMS.map((item) => ({ value: item, label: t(`admin.studyForms.${item}`) }))}
            />
            <Select
              label={t('admin.instructionLanguage')}
              value={form.instructionLanguage}
              onValueChange={(value) => {
                set('instructionLanguage', value)
              }}
              options={['uz', 'ru', 'en'].map((item) => ({ value: item, label: item.toUpperCase() }))}
            />
            <Input
              label={t('admin.plannedSize')}
              type="number"
              min={1}
              value={form.plannedSize}
              onChange={(event) => {
                set('plannedSize', event.target.value)
              }}
            />
            <Select
              label={t('admin.curator')}
              value={form.curatorUserId === '' ? undefined : form.curatorUserId}
              placeholder={t('authoring.choose')}
              onValueChange={(value) => {
                set('curatorUserId', value)
              }}
              options={(teachers.data ?? []).map((item) => ({
                value: item.id ?? '',
                label: item.fullName ?? '',
              }))}
            />
          </div>
          <Problem error={save.error} />
          <DialogFooter>
            <Button type="submit" disabled={form.code.trim() === ''} loading={save.isPending}>
              {t('authoring.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function GroupSheet({
  group,
  onEdit,
  onDeleted,
}: {
  group: Group
  onEdit: () => void
  onDeleted: () => void
}) {
  const { t } = useTranslation()
  const groupId = group.id ?? ''
  const students = useGroupStudentsListQuery(groupId)
  const archive = useArchiveGroup()
  const remove = useDeleteGroup()
  const add = useAddGroupStudents(groupId)
  const removeStudent = useRemoveGroupStudent(groupId)
  const [find, setFind] = useState('')
  const [confirming, setConfirming] = useState(false)
  const found = useUsersQuery({ query: find, role: 'STUDENT', page: 0 }, find.trim().length >= 2)
  const archived = group.status === 'ARCHIVED'

  return (
    <>
      <SheetHeader>
        <SheetTitle>{group.code}</SheetTitle>
        <SheetDescription>{t('admin.groupSheetBody', { count: group.studentCount ?? 0 })}</SheetDescription>
      </SheetHeader>
      <SheetBody className="grid grid-cols-1 content-start gap-6">
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" size="sm" onClick={onEdit}>
            <Pencil aria-hidden className="size-4" />
            {t('authoring.edit')}
          </Button>
          <Button
            variant="outline"
            size="sm"
            loading={archive.isPending}
            onClick={() => {
              archive.mutate({ id: groupId, archived: !archived })
            }}
          >
            {archived ? (
              <ArchiveRestore aria-hidden className="size-4" />
            ) : (
              <Archive aria-hidden className="size-4" />
            )}
            {archived ? t('admin.restore') : t('admin.archive')}
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setConfirming(true)
            }}
          >
            <Trash2 aria-hidden className="size-4" />
            {t('authoring.delete')}
          </Button>
        </div>
        <Problem error={archive.error ?? add.error ?? removeStudent.error} />

        <section className="grid grid-cols-1 gap-2">
          <h3 className="font-display text-h3 text-text font-semibold">{t('admin.roster')}</h3>
          {students.isPending && <Skeleton className="h-24" />}
          {students.data?.length === 0 && (
            <p className="text-small text-text-muted">{t('admin.noStudents')}</p>
          )}
          <ul className="divide-border grid grid-cols-1 divide-y">
            {students.data?.map((student) => (
              <li key={student.userId} className="flex items-center gap-2 py-2">
                <span className="text-body text-text min-w-0 flex-1 truncate">{student.fullName}</span>
                <span className="text-caption text-text-muted tabular-nums">{student.studentNumber}</span>
                <IconButton
                  size="sm"
                  aria-label={t('admin.removeFromGroup', { name: student.fullName ?? '' })}
                  onClick={() => {
                    removeStudent.mutate(student.userId ?? '')
                  }}
                >
                  <UserMinus aria-hidden className="size-4" />
                </IconButton>
              </li>
            ))}
          </ul>
        </section>

        <section className="grid grid-cols-1 gap-2">
          <h3 className="font-display text-h3 text-text font-semibold">{t('admin.addStudents')}</h3>
          <Input
            label={t('admin.findStudent')}
            type="search"
            value={find}
            onChange={(event) => {
              setFind(event.target.value)
            }}
          />
          <ul className="grid grid-cols-1 gap-1">
            {(found.data?.content ?? [])
              .filter((user) => !(students.data ?? []).some((student) => student.userId === user.id))
              .slice(0, 8)
              .map((user) => (
                <li key={user.id} className="flex items-center gap-2">
                  <span className="text-small text-text min-w-0 flex-1 truncate">
                    {user.fullName} <span className="text-text-muted">· {user.username}</span>
                  </span>
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => {
                      add.mutate([user.id ?? ''])
                    }}
                  >
                    <UserPlus aria-hidden className="size-4" />
                    {t('authoring.add')}
                  </Button>
                </li>
              ))}
          </ul>
        </section>
      </SheetBody>
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title={t('admin.deleteGroupTitle', { code: group.code ?? '' })}
        body={t('admin.deleteGroupBody', { count: group.studentCount ?? 0 })}
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending}
        {...(remove.isError && { error: t(`errors.${toApiProblem(remove.error).code}`) })}
        onConfirm={() => {
          remove.mutate(groupId, {
            onSuccess: () => {
              setConfirming(false)
              onDeleted()
            },
          })
        }}
      />
    </>
  )
}

// ------------------------------------------------------------------ rooms

function RoomsTab() {
  const { t } = useTranslation()
  const rooms = useRoomsQuery()
  const remove = useDeleteRoom()
  const [editing, setEditing] = useState<Room | 'new'>()
  const [deleting, setDeleting] = useState<Room>()
  if (!rooms.isSuccess) return <Loading query={rooms} />
  const rows = rooms.data
    .slice()
    .sort((a, b) => (a.label ?? '').localeCompare(b.label ?? '', undefined, { numeric: true }))

  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex justify-end">
        <Button
          onClick={() => {
            setEditing('new')
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('admin.newRoom')}
        </Button>
      </div>
      <div className="border-border overflow-x-auto rounded-lg border">
        <Table>
          <TableHead>
            <TableRow>
              <TableHeaderCell>{t('admin.room')}</TableHeaderCell>
              <TableHeaderCell>{t('admin.roomType')}</TableHeaderCell>
              <TableHeaderCell numeric>{t('admin.capacity')}</TableHeaderCell>
              <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
              <TableHeaderCell>
                <span className="sr-only">{t('admin.actions')}</span>
              </TableHeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((room) => (
              <TableRow key={room.id}>
                <TableCell className="font-medium">
                  {room.label ?? room.number}
                  {room.name !== undefined && room.name !== '' && (
                    <span className="text-caption text-text-muted block">{room.name}</span>
                  )}
                </TableCell>
                <TableCell>{t(`admin.roomTypes.${room.roomType ?? 'OTHER'}`)}</TableCell>
                <TableCell numeric>{room.capacity ?? '—'}</TableCell>
                <TableCell>
                  <Badge tone={room.active === false ? 'neutral' : 'success'}>
                    {room.active === false ? t('admin.inactive') : t('admin.active')}
                  </Badge>
                </TableCell>
                <TableCell className="text-right whitespace-nowrap">
                  <IconButton
                    size="sm"
                    aria-label={t('admin.editRoom', { room: room.label ?? '' })}
                    onClick={() => {
                      setEditing(room)
                    }}
                  >
                    <Pencil aria-hidden className="size-4" />
                  </IconButton>
                  <IconButton
                    size="sm"
                    aria-label={t('admin.deleteRoom', { room: room.label ?? '' })}
                    onClick={() => {
                      setDeleting(room)
                    }}
                  >
                    <Trash2 aria-hidden className="size-4" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      {editing !== undefined && (
        <RoomDialog
          room={editing === 'new' ? undefined : editing}
          onClose={() => {
            setEditing(undefined)
          }}
        />
      )}
      <ConfirmDialog
        open={deleting !== undefined}
        onOpenChange={(open) => {
          if (!open) setDeleting(undefined)
        }}
        title={t('admin.deleteRoomTitle', { room: deleting?.label ?? '' })}
        body={t('admin.deleteRoomBody')}
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending}
        {...(remove.isError && { error: t(`errors.${toApiProblem(remove.error).code}`) })}
        onConfirm={() => {
          remove.mutate(deleting?.id ?? '', {
            onSuccess: () => {
              setDeleting(undefined)
            },
          })
        }}
      />
    </div>
  )
}

function RoomDialog({ room, onClose }: { room: Room | undefined; onClose: () => void }) {
  const { t } = useTranslation()
  const save = useSaveRoom()
  const [form, setForm] = useState({
    number: room?.number ?? '',
    building: room?.building ?? '',
    name: room?.name ?? '',
    capacity: room?.capacity === undefined ? '' : String(room.capacity),
    roomType: room?.roomType ?? 'CLASSROOM',
    active: room?.active !== false,
  })
  const roomType = ROOM_TYPES.find((item) => item === form.roomType) ?? 'CLASSROOM'
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent>
        <DialogTitle>
          {room === undefined ? t('admin.newRoom') : t('admin.editRoom', { room: room.label ?? '' })}
        </DialogTitle>
        <DialogDescription>{t('admin.roomFormBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            const input: RoomInput = {
              number: form.number.trim(),
              roomType,
              active: form.active,
              ...(form.building.trim() !== '' && { building: form.building.trim() }),
              ...(form.name.trim() !== '' && { name: form.name.trim() }),
              ...(form.capacity !== '' && { capacity: Number.parseInt(form.capacity, 10) }),
            }
            save.mutate({ ...(room?.id !== undefined && { id: room.id }), input }, { onSuccess: onClose })
          }}
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label={t('admin.building')}
              value={form.building}
              placeholder="B"
              onChange={(event) => {
                setForm({ ...form, building: event.target.value })
              }}
            />
            <Input
              label={t('admin.roomNumber')}
              value={form.number}
              required
              placeholder="301"
              onChange={(event) => {
                setForm({ ...form, number: event.target.value })
              }}
            />
            <Input
              label={t('admin.roomName')}
              value={form.name}
              onChange={(event) => {
                setForm({ ...form, name: event.target.value })
              }}
            />
            <Input
              label={t('admin.capacity')}
              type="number"
              min={1}
              value={form.capacity}
              onChange={(event) => {
                setForm({ ...form, capacity: event.target.value })
              }}
            />
            <Select
              label={t('admin.roomType')}
              value={roomType}
              onValueChange={(value) => {
                setForm({ ...form, roomType: ROOM_TYPES.find((item) => item === value) ?? 'CLASSROOM' })
              }}
              options={ROOM_TYPES.map((item) => ({ value: item, label: t(`admin.roomTypes.${item}`) }))}
            />
            <Switch
              className="self-end pb-2"
              label={t('admin.active')}
              checked={form.active}
              onCheckedChange={(checked) => {
                setForm({ ...form, active: checked })
              }}
            />
          </div>
          <Problem error={save.error} />
          <DialogFooter>
            <Button type="submit" disabled={form.number.trim() === ''} loading={save.isPending}>
              {t('authoring.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ------------------------------------------------------------------ the bells

function BellsTab() {
  const { t } = useTranslation()
  const slots = usePairSlotsQuery()
  const save = useSavePairSlot()
  if (!slots.isSuccess) return <Loading query={slots} />
  const rows = slots.data.slice().sort((a, b) => (a.pairNo ?? 0) - (b.pairNo ?? 0))
  const last = rows[rows.length - 1]
  return (
    <Card className="max-w-2xl">
      <CardHeader title={t('admin.reference.bells')} description={t('admin.bellsBody')} />
      <CardContent className="grid grid-cols-1 gap-3">
        {rows.map((slot) => (
          <SlotRow key={slot.id} slot={slot} />
        ))}
        <Button
          variant="outline"
          className="justify-self-start"
          loading={save.isPending}
          onClick={() => {
            // the next pair starts ten minutes after the last one ends and lasts 80 minutes
            const startsAt = addMinutes(last?.endsAt ?? '08:20', last === undefined ? 0 : 10)
            save.mutate({
              input: { pairNo: (last?.pairNo ?? 0) + 1, startsAt, endsAt: addMinutes(startsAt, 80) },
            })
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('admin.addPair')}
        </Button>
        <Problem error={save.error} />
      </CardContent>
    </Card>
  )
}

function addMinutes(time: string, minutes: number): string {
  const [h = 0, m = 0] = time.split(':').map(Number)
  const total = (h * 60 + m + minutes) % (24 * 60)
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`
}

function SlotRow({ slot }: { slot: PairSlot }) {
  const { t } = useTranslation()
  const save = useSavePairSlot()
  const remove = useDeletePairSlot()
  const [startsAt, setStartsAt] = useState((slot.startsAt ?? '').slice(0, 5))
  const [endsAt, setEndsAt] = useState((slot.endsAt ?? '').slice(0, 5))
  const dirty = startsAt !== (slot.startsAt ?? '').slice(0, 5) || endsAt !== (slot.endsAt ?? '').slice(0, 5)
  return (
    <div className="grid grid-cols-1 gap-1">
      <div className="flex flex-wrap items-end gap-3">
        <span className="text-body text-text w-20 pb-2 font-semibold">
          {t('admin.pairNo', { number: slot.pairNo ?? 0 })}
        </span>
        <Input
          className="w-32"
          label={t('admin.startsAt')}
          type="time"
          value={startsAt}
          onChange={(event) => {
            setStartsAt(event.target.value)
          }}
        />
        <Input
          className="w-32"
          label={t('admin.endsAt')}
          type="time"
          value={endsAt}
          onChange={(event) => {
            setEndsAt(event.target.value)
          }}
        />
        {dirty && (
          <Button
            size="sm"
            className="mb-1"
            loading={save.isPending}
            onClick={() => {
              save.mutate({ id: slot.id ?? '', input: { pairNo: slot.pairNo ?? 0, startsAt, endsAt } })
            }}
          >
            {t('authoring.save')}
          </Button>
        )}
        <IconButton
          className="mb-1"
          aria-label={t('admin.deletePair', { number: slot.pairNo ?? 0 })}
          onClick={() => {
            remove.mutate(slot.id ?? '')
          }}
        >
          <Trash2 aria-hidden className="size-4" />
        </IconButton>
      </div>
      <Problem error={save.error ?? remove.error} />
    </div>
  )
}

// ------------------------------------------------------------------ holidays

function HolidaysTab() {
  const { t } = useTranslation()
  const locale = useLocale()
  const holidays = useHolidaysQuery()
  const add = useAddHoliday()
  const remove = useDeleteHoliday()
  const [date, setDate] = useState('')
  const [name, setName] = useState('')
  if (!holidays.isSuccess) return <Loading query={holidays} />
  const rows = holidays.data.slice().sort((a, b) => (a.date ?? '').localeCompare(b.date ?? ''))
  return (
    <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
      <Card className="lg:col-span-5">
        <CardHeader title={t('admin.newHoliday')} description={t('admin.holidayBody')} />
        <CardContent>
          <form
            className="grid grid-cols-1 gap-3"
            onSubmit={(event) => {
              event.preventDefault()
              if (date === '' || name.trim() === '') return
              add.mutate(
                { date, name: name.trim() },
                {
                  onSuccess: () => {
                    setDate('')
                    setName('')
                  },
                },
              )
            }}
          >
            <Input
              label={t('admin.date')}
              type="date"
              value={date}
              onChange={(event) => {
                setDate(event.target.value)
              }}
            />
            <Input
              label={t('admin.holidayName')}
              value={name}
              onChange={(event) => {
                setName(event.target.value)
              }}
            />
            <Problem error={add.error} />
            <Button
              type="submit"
              className="justify-self-start"
              disabled={date === '' || name.trim() === ''}
              loading={add.isPending}
            >
              <Plus aria-hidden className="size-4" />
              {t('authoring.add')}
            </Button>
          </form>
        </CardContent>
      </Card>
      <Card className="lg:col-span-7">
        <CardHeader title={t('admin.reference.holidays')} />
        <CardContent>
          {rows.length === 0 && <p className="text-small text-text-muted">{t('admin.noHolidays')}</p>}
          <ul className="divide-border grid grid-cols-1 divide-y">
            {rows.map((holiday) => (
              <li key={holiday.id} className="flex items-center gap-3 py-2">
                <span className="text-small text-text-muted w-36 tabular-nums">
                  {holiday.date === undefined ? '' : formatDate(holiday.date, locale)}
                </span>
                <span className="text-body text-text min-w-0 flex-1">{holiday.name}</span>
                <IconButton
                  size="sm"
                  aria-label={t('admin.deleteHoliday', { name: holiday.name ?? '' })}
                  onClick={() => {
                    remove.mutate(holiday.id ?? '')
                  }}
                >
                  <Trash2 aria-hidden className="size-4" />
                </IconButton>
              </li>
            ))}
          </ul>
          <Problem error={remove.error} />
        </CardContent>
      </Card>
    </div>
  )
}

// ------------------------------------------------------------------ years, terms, parity

function TermsTab() {
  const { t } = useTranslation()
  const years = useAcademicYearsQuery()
  const create = useCreateAcademicYear()
  const [form, setForm] = useState({ code: '', startsOn: '', endsOn: '' })
  if (!years.isSuccess) return <Loading query={years} />
  const rows = years.data.slice().sort((a, b) => (b.startsOn ?? '').localeCompare(a.startsOn ?? ''))
  return (
    <div className="grid grid-cols-1 gap-6">
      {rows.map((year) => (
        <YearCard key={year.id} year={year} />
      ))}
      <Card className="max-w-2xl">
        <CardHeader title={t('admin.newYear')} />
        <CardContent>
          <form
            className="flex flex-wrap items-end gap-3"
            onSubmit={(event) => {
              event.preventDefault()
              create.mutate(form, {
                onSuccess: () => {
                  setForm({ code: '', startsOn: '', endsOn: '' })
                },
              })
            }}
          >
            <Input
              className="w-36"
              label={t('admin.yearCode')}
              placeholder="2027/2028"
              value={form.code}
              onChange={(event) => {
                setForm({ ...form, code: event.target.value })
              }}
            />
            <Input
              label={t('admin.startsOn')}
              type="date"
              value={form.startsOn}
              onChange={(event) => {
                setForm({ ...form, startsOn: event.target.value })
              }}
            />
            <Input
              label={t('admin.endsOn')}
              type="date"
              value={form.endsOn}
              onChange={(event) => {
                setForm({ ...form, endsOn: event.target.value })
              }}
            />
            <Button
              type="submit"
              disabled={form.code === '' || form.startsOn === '' || form.endsOn === ''}
              loading={create.isPending}
            >
              {t('authoring.create')}
            </Button>
          </form>
          <Problem error={create.error} />
        </CardContent>
      </Card>
    </div>
  )
}

function YearCard({ year }: { year: AcademicYear }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const terms = useTermsQuery(year.id)
  const timetables = useTimetablesQuery()
  const current = useMakeYearCurrent()
  const createTerm = useCreateTerm(year.id ?? '')
  const parity = useSetParity()
  const [form, setForm] = useState({ name: '', startsOn: '', endsOn: '' })
  const parityOf = (termId: string | undefined) =>
    timetables.data?.find((item) => item.termId === termId && item.firstWeekParity !== undefined)
      ?.firstWeekParity

  return (
    <Card>
      <CardHeader
        title={year.code}
        description={
          year.startsOn !== undefined && year.endsOn !== undefined
            ? `${formatDate(year.startsOn, locale)} — ${formatDate(year.endsOn, locale)}`
            : undefined
        }
        action={
          year.current === true ? (
            <Badge tone="success">{t('admin.currentYear')}</Badge>
          ) : (
            <Button
              size="sm"
              variant="outline"
              loading={current.isPending}
              onClick={() => {
                current.mutate(year.id ?? '')
              }}
            >
              {t('admin.makeCurrent')}
            </Button>
          )
        }
      />
      <CardContent className="grid grid-cols-1 gap-4">
        {terms.isPending && <Skeleton className="h-16" />}
        <ul className="divide-border grid grid-cols-1 divide-y">
          {terms.data?.map((term) => (
            <li key={term.id} className="flex flex-wrap items-center gap-3 py-2.5">
              <span className="text-body text-text min-w-40 flex-1 font-medium">{term.name}</span>
              <span className="text-small text-text-muted tabular-nums">
                {term.startsOn !== undefined && formatDate(term.startsOn, locale)} —{' '}
                {term.endsOn !== undefined && formatDate(term.endsOn, locale)}
              </span>
              <SegmentedControl
                size="sm"
                aria-label={t('admin.firstWeek', { term: term.name ?? '' })}
                value={parityOf(term.id) ?? ''}
                onValueChange={(value) => {
                  if (value === 'ODD' || value === 'EVEN')
                    parity.mutate({ termId: term.id ?? '', parity: value })
                }}
                options={[
                  { value: 'ODD', label: t('admin.firstWeekOdd') },
                  { value: 'EVEN', label: t('admin.firstWeekEven') },
                ]}
              />
            </li>
          ))}
        </ul>
        <form
          className="flex flex-wrap items-end gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            createTerm.mutate(
              { ...form, ordinal: (terms.data?.length ?? 0) + 1 },
              {
                onSuccess: () => {
                  setForm({ name: '', startsOn: '', endsOn: '' })
                },
              },
            )
          }}
        >
          <Input
            className="w-40"
            label={t('admin.termName')}
            placeholder="2-semestr"
            value={form.name}
            onChange={(event) => {
              setForm({ ...form, name: event.target.value })
            }}
          />
          <Input
            label={t('admin.startsOn')}
            type="date"
            value={form.startsOn}
            onChange={(event) => {
              setForm({ ...form, startsOn: event.target.value })
            }}
          />
          <Input
            label={t('admin.endsOn')}
            type="date"
            value={form.endsOn}
            onChange={(event) => {
              setForm({ ...form, endsOn: event.target.value })
            }}
          />
          <Button
            type="submit"
            variant="outline"
            disabled={form.name === '' || form.startsOn === '' || form.endsOn === ''}
            loading={createTerm.isPending}
          >
            <Plus aria-hidden className="size-4" />
            {t('admin.addTerm')}
          </Button>
        </form>
        <Problem error={createTerm.error ?? parity.error ?? current.error} />
      </CardContent>
    </Card>
  )
}
