import { useRef, useState } from 'react'
import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragOverEvent,
  type DragStartEvent,
} from '@dnd-kit/core'
import { CalendarSearch, Download, Printer, Undo2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { useAllGroupsQuery, usePairSlotsQuery, useRoomsQuery } from '@/features/admin/reference-queries'
import { useTeachersQuery } from '@/features/admin/users-queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { useMediaQuery } from '@/shared/lib/use-media-query'
import { downloadXlsx } from '@/shared/lib/xlsx'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { ScheduleGrid } from '@/shared/ui/schedule-grid'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { Sheet, SheetBody, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { Skeleton } from '@/shared/ui/skeleton'
import { useToast } from '@/shared/ui/toast'
import { DropCell, EntryCard, EntryFace, type CellMark } from './builder-grid'
import { EntryPanel, type Undo } from './entry-panel'
import { LoadPanel } from './load-panel'
import {
  DAYS,
  cellByCell,
  cellId,
  clashOf,
  parseCellId,
  requestOf,
  visibleIn,
  type Parity,
  type ViewKind,
} from './model'
import {
  checkEntry,
  useEntriesQuery,
  useSaveEntry,
  useTimetableListQuery,
  type Entry,
  type Scope,
} from './queries'
import { TimetableBar } from './timetable-bar'
import { WeekPreview } from './week-preview'

type Editing = {
  entry?: Entry
  start?: { day: number; pairSlotId: string; groupId?: string; teacherId?: string; roomId?: string }
}

/**
 * A-02 — the timetable builder: one grid seen by group, teacher or room, odd / even / every week;
 * classes added and edited in a side panel and moved by drag (or by keyboard: space, the arrows,
 * space) with the conflict check of the server answering while the class is in the air.
 */
export function BuilderPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()
  const [search, setSearch] = useSearchParams()
  const wide = useMediaQuery('(min-width: 64rem)')
  const timetables = useTimetableListQuery()
  const groups = useAllGroupsQuery()
  const teachers = useTeachersQuery()
  const rooms = useRoomsQuery()
  const slots = usePairSlotsQuery()

  const list = (timetables.data ?? []).filter((item) => item.status !== 'ARCHIVED')
  const selected =
    (timetables.data ?? []).find((item) => item.id === search.get('tt')) ??
    list.find((item) => item.status === 'DRAFT') ??
    list[0]
  const view: ViewKind =
    search.get('view') === 'teacher' ? 'teacher' : search.get('view') === 'room' ? 'room' : 'group'
  const choices =
    view === 'group'
      ? (groups.data ?? [])
          .filter((group) => group.status !== 'ARCHIVED')
          .map((group) => ({ value: group.id ?? '', label: group.code ?? '' }))
      : view === 'teacher'
        ? (teachers.data ?? []).map((teacher) => ({ value: teacher.id ?? '', label: teacher.fullName ?? '' }))
        : (rooms.data ?? []).map((room) => ({ value: room.id ?? '', label: room.label ?? room.number ?? '' }))
  const scopeId = search.get('id') ?? choices[0]?.value
  const scope: Scope | undefined =
    scopeId === undefined || scopeId === '' ? undefined : { kind: view, id: scopeId }
  const parityParam = search.get('parity')
  const parity: Parity = parityParam === 'ODD' || parityParam === 'EVEN' ? parityParam : 'EVERY'
  const editable = selected?.status === 'DRAFT'

  const entries = useEntriesQuery(selected?.id, scope, locale)
  const save = useSaveEntry(selected?.id ?? '', locale)
  const [editing, setEditing] = useState<Editing>()
  const [undo, setUndo] = useState<Undo>()
  const [dragged, setDragged] = useState<Entry>()
  const [marks, setMarks] = useState<Record<string, CellMark>>({})
  const [shakingId, setShakingId] = useState<string>()
  const [preview, setPreview] = useState(false)
  const [announcement, setAnnouncement] = useState('')
  const checkTimer = useRef<number | undefined>(undefined)
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: cellByCell }),
  )

  const setParam = (patch: Record<string, string | undefined>) => {
    const next = new URLSearchParams(search)
    for (const [key, value] of Object.entries(patch)) {
      if (value === undefined || value === '') next.delete(key)
      else next.set(key, value)
    }
    setSearch(next, { replace: true })
  }

  const visible = (entries.data ?? []).filter((entry) => visibleIn(entry, parity))
  const daySlots = (slots.data ?? [])
    .slice()
    .sort((a, b) => (a.startsAt ?? '').localeCompare(b.startsAt ?? ''))
  // a group sees its own shift, and any other shift it already has classes in; a teacher or a
  // room is free all day
  const groupShift =
    scope?.kind === 'group' ? groups.data?.find((group) => group.id === scope.id)?.shift : undefined
  const usedShifts = new Set(
    visible.map((entry) => daySlots.find((slot) => slot.id === entry.pairSlotId)?.shift),
  )
  const orderedSlots =
    groupShift === undefined
      ? daySlots
      : daySlots.filter((slot) => slot.shift === groupShift || usedShifts.has(slot.shift))
  const shownShifts = new Set(orderedSlots.map((slot) => slot.shift)).size
  const inCell = (day: number, pairSlotId: string) =>
    visible.filter((entry) => entry.dayOfWeek === day && entry.pairSlotId === pairSlotId)
  const startFor = (day: number, pairSlotId: string): Editing['start'] => ({
    day,
    pairSlotId,
    ...(scope?.kind === 'group' && { groupId: scope.id }),
    ...(scope?.kind === 'teacher' && { teacherId: scope.id }),
    ...(scope?.kind === 'room' && { roomId: scope.id }),
  })

  // ---------------------------------------------------------------- drag and drop

  const onDragStart = (event: DragStartEvent) => {
    setDragged(visible.find((entry) => entry.id === event.active.id))
    setMarks({})
  }

  const onDragOver = (event: DragOverEvent) => {
    const target = event.over === null ? null : parseCellId(String(event.over.id))
    const entry = dragged
    if (target === null || entry === undefined || selected?.id === undefined) return
    const key = cellId(target.day, target.pairSlotId)
    if (marks[key] !== undefined && marks[key] !== 'checking') return
    setMarks((current) => ({ ...current, [key]: 'checking' }))
    window.clearTimeout(checkTimer.current)
    checkTimer.current = window.setTimeout(() => {
      checkEntry(selected.id ?? '', requestOf(entry, target), entry.id, locale).then(
        (result) => {
          const clash = clashOf(result)
          const mark: CellMark =
            clash.conflicts.length > 0 ? 'conflict' : clash.warnings.length > 0 ? 'warning' : 'free'
          setMarks((current) => ({ ...current, [key]: mark }))
        },
        () => {
          setMarks((current) => {
            const next = { ...current }
            next[key] = 'free'
            return next
          })
        },
      )
    }, 150)
  }

  const refuse = (entry: Entry, message: string) => {
    setShakingId(entry.id)
    window.setTimeout(() => {
      setShakingId(undefined)
    }, 420)
    setAnnouncement(message)
    toast({ tone: 'danger', title: t('builder.refused'), body: message })
  }

  const onDragEnd = (event: DragEndEvent) => {
    const entry = dragged
    setDragged(undefined)
    window.clearTimeout(checkTimer.current)
    const target = event.over === null ? null : parseCellId(String(event.over.id))
    if (entry === undefined || target === null) return
    if (target.day === entry.dayOfWeek && target.pairSlotId === entry.pairSlotId) return
    const previous = requestOf(entry)
    save.mutate(
      { id: entry.id, request: requestOf(entry, target) },
      {
        onSuccess: () => {
          setUndo({
            label: t('builder.undoMove'),
            run: () =>
              save.mutateAsync({
                id: entry.id,
                request: { ...previous, force: true, forceReason: t('builder.undoReason') },
              }),
          })
          setAnnouncement(t('builder.moved', { title: entry.courseTitle ?? '' }))
        },
        onError: (failure) => {
          const problem = toApiProblem(failure)
          if (problem.code !== 'TIMETABLE_CONFLICT') {
            refuse(entry, t(`errors.${problem.code}`))
            return
          }
          const clash = clashOf(problem.details)
          const first = clash.conflicts[0]
          if (first !== undefined) {
            refuse(
              entry,
              t('builder.refusedBecause', {
                what: t(`builder.conflict.${first.type ?? 'GROUP'}`),
                other: [first.subjectCode, first.courseTitle, first.teacherName, first.groupCodes?.join(', ')]
                  .filter(Boolean)
                  .join(' · '),
              }),
            )
          } else {
            // only warnings: the panel opens on the moved class to say why and take a reason
            setEditing({ entry: { ...entry, dayOfWeek: target.day, pairSlotId: target.pairSlotId } })
          }
        },
      },
    )
  }

  // ---------------------------------------------------------------- export

  const exportExcel = () => {
    const header = ['', ...DAYS.map((day) => t(`builder.days.${day}`))]
    const rows = orderedSlots.map((slot) => [
      `${t('admin.pairNo', { number: slot.shiftPairNo ?? 0 })} ${(slot.startsAt ?? '').slice(0, 5)}–${(slot.endsAt ?? '').slice(0, 5)}`,
      ...DAYS.map((day) =>
        inCell(day, slot.id ?? '')
          .map((entry) =>
            [
              entry.courseTitle,
              t(`sessionType.${entry.sessionType ?? 'LECTURE'}`),
              entry.teacherName,
              (entry.groups ?? []).map((group) => group.code).join(', '),
              entry.roomLabel,
              entry.weekParity === 'EVERY' || entry.weekParity === undefined
                ? undefined
                : t(`builder.parity.${entry.weekParity}`),
            ]
              .filter(Boolean)
              .join(', '),
          )
          .join(' / '),
      ),
    ])
    const who = choices.find((choice) => choice.value === scope?.id)?.label ?? ''
    downloadXlsx(`${selected?.name ?? ''} — ${who}`, [{ name: who, rows: [header, ...rows] }])
  }

  if (timetables.isPending || slots.isPending) return <Skeleton className="h-[40rem]" />
  if (timetables.isError) {
    return (
      <ErrorState
        problem={toApiProblem(timetables.error)}
        onRetry={() => {
          void timetables.refetch()
        }}
      />
    )
  }

  const scopeLabel = choices.find((choice) => choice.value === scope?.id)?.label ?? ''
  const panel =
    editing === undefined || selected === undefined ? null : (
      <EntryPanel
        key={`${editing.entry?.id ?? 'new'}-${editing.entry?.dayOfWeek ?? editing.start?.day ?? 0}-${editing.entry?.pairSlotId ?? editing.start?.pairSlotId ?? ''}`}
        timetable={selected}
        {...(editing.entry !== undefined && { entry: editing.entry })}
        {...(editing.start !== undefined && { start: editing.start })}
        editable={editable}
        onDone={(done) => {
          setEditing(undefined)
          if (done !== undefined) setUndo(done)
        }}
      />
    )

  return (
    <div className="grid grid-cols-1 gap-6 print:gap-2">
      <PageHeader title={t('nav.adminTimetable')} context={t('builder.context')} className="print:hidden" />
      <TimetableBar
        timetables={list}
        selected={selected}
        onSelect={(id) => {
          setParam({ tt: id })
          setUndo(undefined)
        }}
      />
      {selected === undefined ? (
        <EmptyState title={t('builder.noTimetable')} body={t('builder.noTimetableBody')} />
      ) : (
        <>
          <div className="flex flex-wrap items-end gap-3" data-print-hidden>
            <div className="grid grid-cols-1 gap-1.5">
              <span className="text-small text-text font-medium">{t('builder.view')}</span>
              <SegmentedControl
                aria-label={t('builder.view')}
                value={view}
                onValueChange={(value) => {
                  setParam({ view: value === 'group' ? undefined : value, id: undefined })
                }}
                options={[
                  { value: 'group', label: t('builder.byGroup') },
                  { value: 'teacher', label: t('builder.byTeacher') },
                  { value: 'room', label: t('builder.byRoom') },
                ]}
              />
            </div>
            <Select
              className="w-60"
              label={t(`builder.which.${view}`)}
              value={scope?.id}
              placeholder={t('authoring.choose')}
              onValueChange={(value) => {
                setParam({ id: value })
              }}
              options={choices}
            />
            <div className="grid grid-cols-1 gap-1.5">
              <span className="text-small text-text font-medium">{t('builder.weeks')}</span>
              <SegmentedControl
                aria-label={t('builder.weeks')}
                value={parity}
                onValueChange={(value) => {
                  setParam({ parity: value === 'EVERY' ? undefined : value })
                }}
                options={[
                  { value: 'EVERY', label: t('builder.parity.EVERY') },
                  { value: 'ODD', label: t('builder.parity.ODD') },
                  { value: 'EVEN', label: t('builder.parity.EVEN') },
                ]}
              />
            </div>
            <div className="flex flex-wrap gap-2">
              {undo !== undefined && editable && (
                <Button
                  variant="outline"
                  onClick={() => {
                    const last = undo
                    setUndo(undefined)
                    last.run().then(
                      () => {
                        setAnnouncement(t('builder.undone'))
                      },
                      (failure: unknown) => {
                        toast({ tone: 'danger', title: t(`errors.${toApiProblem(failure).code}`) })
                      },
                    )
                  }}
                >
                  <Undo2 aria-hidden className="size-4" />
                  {undo.label}
                </Button>
              )}
              <Button
                variant="ghost"
                onClick={() => {
                  setPreview(true)
                }}
              >
                <CalendarSearch aria-hidden className="size-4" />
                {t('builder.weekPreview')}
              </Button>
              <Button variant="ghost" onClick={exportExcel}>
                <Download aria-hidden className="size-4" />
                {t('teaching.exportExcel')}
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  window.print()
                }}
              >
                <Printer aria-hidden className="size-4" />
                {t('teaching.print')}
              </Button>
            </div>
          </div>
          <p aria-live="polite" className="sr-only">
            {announcement}
          </p>
          <h2 className="font-display text-h2 text-text hidden font-semibold print:block">
            {selected.name} — {scopeLabel}
          </h2>

          <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-12">
            <div className="grid min-w-0 grid-cols-1 gap-4 xl:col-span-9">
              {entries.isPending && scope !== undefined && <Skeleton className="h-[32rem]" />}
              {entries.isError && (
                <ErrorState
                  problem={toApiProblem(entries.error)}
                  onRetry={() => {
                    void entries.refetch()
                  }}
                />
              )}
              {entries.isSuccess && wide && (
                <DndContext
                  sensors={sensors}
                  onDragStart={onDragStart}
                  onDragOver={onDragOver}
                  onDragEnd={onDragEnd}
                  onDragCancel={() => {
                    setDragged(undefined)
                  }}
                >
                  <ScheduleGrid
                    label={`${selected.name ?? ''} — ${scopeLabel}`}
                    corner={t('builder.pair')}
                    columns={DAYS.map((day) => ({
                      key: String(day),
                      header: (
                        <span className="text-small text-text font-semibold">{t(`builder.days.${day}`)}</span>
                      ),
                    }))}
                    rows={orderedSlots.map((slot) => ({
                      key: slot.id ?? '',
                      header: (
                        <span className="text-caption text-text-muted grid grid-cols-1 tabular-nums">
                          {shownShifts > 1 && slot.shiftPairNo === 1 && (
                            <span className="text-accent-text font-medium uppercase">
                              {t(`timetable.shiftShort.${slot.shift ?? 'MORNING'}`)}
                            </span>
                          )}
                          <span className="text-text font-semibold">{slot.shiftPairNo}</span>
                          {(slot.startsAt ?? '').slice(0, 5)}
                          <br />
                          {(slot.endsAt ?? '').slice(0, 5)}
                        </span>
                      ),
                    }))}
                    renderCell={(row, column) => {
                      const day = Number(column.key)
                      return (
                        <DropCell
                          day={day}
                          pairSlotId={row.key}
                          entries={inCell(day, row.key)}
                          mark={marks[cellId(day, row.key)]}
                          editable={editable}
                          shakingId={shakingId}
                          addLabel={t('builder.addHere', {
                            day: t(`builder.days.${day}`),
                            pair: orderedSlots.find((slot) => slot.id === row.key)?.shiftPairNo ?? '',
                          })}
                          onOpen={(entry) => {
                            setEditing({ entry })
                          }}
                          onAdd={() => {
                            setEditing({ start: startFor(day, row.key) })
                          }}
                        />
                      )
                    }}
                  />
                  <DragOverlay dropAnimation={null}>
                    {dragged === undefined ? null : <EntryFace entry={dragged} lifted />}
                  </DragOverlay>
                </DndContext>
              )}
              {entries.isSuccess && !wide && (
                <div className="grid grid-cols-1 gap-4">
                  {DAYS.map((day) => {
                    const own = visible
                      .filter((entry) => entry.dayOfWeek === day)
                      .sort((a, b) => (a.pairNo ?? 0) - (b.pairNo ?? 0))
                    return (
                      <section
                        key={day}
                        aria-label={t(`builder.days.${day}`)}
                        className="grid grid-cols-1 gap-2"
                      >
                        <h3 className="text-small text-text font-semibold">{t(`builder.days.${day}`)}</h3>
                        {own.length === 0 && (
                          <p className="text-small text-text-muted">{t('builder.freeDay')}</p>
                        )}
                        {own.map((entry) => (
                          <div key={entry.id} className="flex items-center gap-2">
                            <span className="text-caption text-text-muted w-10 shrink-0 tabular-nums">
                              {(entry.startsAt ?? '').slice(0, 5)}
                            </span>
                            <div className="min-w-0 flex-1">
                              <EntryCard
                                entry={entry}
                                draggable={false}
                                shaking={false}
                                onOpen={(item) => {
                                  setEditing({ entry: item })
                                }}
                              />
                            </div>
                          </div>
                        ))}
                      </section>
                    )
                  })}
                  {editable && (
                    <Button
                      variant="outline"
                      className="justify-self-start"
                      onClick={() => {
                        setEditing({ start: startFor(1, orderedSlots[0]?.id ?? '') })
                      }}
                    >
                      {t('builder.addClass')}
                    </Button>
                  )}
                </div>
              )}
            </div>

            <aside className="grid min-w-0 grid-cols-1 gap-4 xl:col-span-3" data-print-hidden>
              <Card>
                <CardHeader title={t('builder.load')} description={t('builder.loadBody')} />
                <CardContent>
                  <LoadPanel timetableId={selected.id} scope={scope} />
                </CardContent>
              </Card>
            </aside>
          </div>
        </>
      )}

      <Sheet
        open={editing !== undefined}
        onOpenChange={(open) => {
          if (!open) setEditing(undefined)
        }}
      >
        <SheetContent className="sm:max-w-xl">
          <SheetHeader>
            <SheetTitle>
              {editing?.entry === undefined ? t('builder.addClass') : t('builder.editClass')}
            </SheetTitle>
            <SheetDescription>
              {editable ? t('builder.panelBody') : t('builder.publishedReadOnly')}
            </SheetDescription>
          </SheetHeader>
          <SheetBody>{panel}</SheetBody>
        </SheetContent>
      </Sheet>

      {preview && selected !== undefined && (
        <WeekPreview
          timetableId={selected.id ?? ''}
          scope={scope}
          onClose={() => {
            setPreview(false)
          }}
        />
      )}
    </div>
  )
}
