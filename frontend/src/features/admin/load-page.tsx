import { useState } from 'react'
import { FileUp, Plus, Trash2 } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import type { components } from '@/shared/api/schema'
import { api, unwrap } from '@/shared/api/client'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { readSpreadsheet, recordsOf } from '@/shared/lib/spreadsheet-read'
import { SESSION_TYPES, type SessionType } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { PageHeader } from '@/shared/ui/page-header'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import {
  useAcademicYearsQuery,
  useAllGroupsQuery,
  useRefreshingMutation,
  useTermsQuery,
} from './reference-queries'
import { useCoursesQuery } from './structure-queries'
import { useTeachersQuery } from './users-queries'

type Schemas = components['schemas']
type LoadRow = Schemas['LoadRow']
type LoadItem = Schemas['LoadItemRequest']

const loadKey = (termId: string) => ['admin', 'teaching-load', termId] as const

function useLoadQuery(termId: string | undefined, locale: string) {
  return useQuery({
    queryKey: [...loadKey(termId ?? ''), locale],
    queryFn: () =>
      unwrap(api.GET('/api/v1/admin/teaching-load', { params: { query: { termId: termId ?? '', locale } } })),
    enabled: termId !== undefined,
  })
}

/** The columns of the department's sheet; the hours of each kind of class are a column each. */
const SHEET = {
  subject: ['Fan', 'Fan kodi', 'Subject', 'Предмет', 'Код'],
  group: ['Guruh', 'Group', 'Группа'],
  teacher: ['Oʻqituvchi', "O'qituvchi", 'Teacher', 'Преподаватель'],
  LECTURE: ['Maʼruza', "Ma'ruza", 'Lecture', 'Лекция'],
  PRACTICAL: ['Amaliy', 'Amaliyot', 'Practical', 'Практика'],
  SEMINAR: ['Seminar', 'Семинар'],
  LABORATORY: ['Laboratoriya', 'Laboratory', 'Лабораторная'],
} as const

/**
 * A-04 — the teaching load of a term: one line per course, group and kind of class, entered by
 * hand or read from the department's Excel sheet, seen as lines or per teacher. It feeds the
 * timetable builder's "Load" panel.
 */
export function LoadPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const [search, setSearch] = useSearchParams()
  const years = useAcademicYearsQuery()
  const currentYear = years.data?.find((year) => year.current === true)?.id
  const terms = useTermsQuery(currentYear)
  const termId = search.get('term') ?? terms.data?.[0]?.id
  const view = search.get('view') === 'teacher' ? 'teacher' : 'lines'
  const load = useLoadQuery(termId, locale)
  const remove = useRefreshingMutation([loadKey(termId ?? '')], (itemId: string) =>
    unwrap(api.DELETE('/api/v1/admin/teaching-load/{itemId}', { params: { path: { itemId } } })),
  )
  const setParam = (key: string, value: string) => {
    const next = new URLSearchParams(search)
    next.set(key, value)
    setSearch(next, { replace: true })
  }
  const rows = (load.data ?? [])
    .slice()
    .sort((a, b) =>
      `${a.courseTitle ?? ''}${a.groupCode ?? ''}`.localeCompare(
        `${b.courseTitle ?? ''}${b.groupCode ?? ''}`,
      ),
    )
  const teachers = [...new Set(rows.map((row) => row.teacherName ?? t('admin.noTeacher')))].sort()

  return (
    <div className="grid grid-cols-1 gap-6">
      <PageHeader title={t('nav.adminLoad')} context={t('admin.loadContext')} />
      <div className="flex flex-wrap items-end gap-3">
        <Select
          className="w-56"
          label={t('admin.term')}
          value={termId}
          placeholder={t('authoring.choose')}
          onValueChange={(value) => {
            setParam('term', value)
          }}
          options={(terms.data ?? []).map((term) => ({
            value: term.id ?? '',
            label: `${term.academicYearCode ?? ''} · ${term.name ?? ''}`,
          }))}
        />
        <SegmentedControl
          aria-label={t('admin.loadView')}
          value={view}
          onValueChange={(value) => {
            setParam('view', value)
          }}
          options={[
            { value: 'lines', label: t('admin.loadLines') },
            { value: 'teacher', label: t('admin.loadByTeacher') },
          ]}
        />
      </div>

      {termId !== undefined && (
        <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-2">
          <NewLoadLine termId={termId} />
          <LoadImport termId={termId} />
        </div>
      )}

      {load.isPending && termId !== undefined && <Skeleton className="h-64" />}
      {load.isError && (
        <ErrorState
          problem={toApiProblem(load.error)}
          onRetry={() => {
            void load.refetch()
          }}
        />
      )}
      {load.isSuccess && rows.length === 0 && (
        <EmptyState title={t('admin.noLoad')} body={t('admin.noLoadBody')} />
      )}
      {load.isSuccess && rows.length > 0 && view === 'lines' && (
        <div className="border-border overflow-x-auto rounded-lg border">
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>{t('admin.course')}</TableHeaderCell>
                <TableHeaderCell>{t('profile.group')}</TableHeaderCell>
                <TableHeaderCell>{t('authoring.sessionType')}</TableHeaderCell>
                <TableHeaderCell>{t('admin.teacher')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('admin.planned')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('admin.scheduled')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('admin.remaining')}</TableHeaderCell>
                <TableHeaderCell>
                  <span className="sr-only">{t('admin.actions')}</span>
                </TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <LoadLine
                  key={row.itemId ?? `${row.courseId ?? ''}${row.groupId ?? ''}${row.sessionType ?? ''}`}
                  row={row}
                  onDelete={() => {
                    if (row.itemId !== undefined) remove.mutate(row.itemId)
                  }}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      {load.isSuccess && rows.length > 0 && view === 'teacher' && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {teachers.map((name) => {
            const own = rows.filter((row) => (row.teacherName ?? t('admin.noTeacher')) === name)
            const planned = own.reduce((sum, row) => sum + (row.plannedHours ?? 0), 0)
            const scheduled = own.reduce((sum, row) => sum + (row.scheduledHours ?? 0), 0)
            return (
              <Card key={name}>
                <CardHeader title={name} description={t('admin.teacherLoad', { planned, scheduled })} />
                <CardContent>
                  <ul className="grid grid-cols-1 gap-1">
                    {own.map((row) => (
                      <li key={row.itemId} className="text-small flex items-center gap-2">
                        <span className="text-text min-w-0 flex-1 truncate">
                          {row.courseTitle} · {row.groupCode}
                        </span>
                        <span className="text-text-muted">
                          {t(`sessionType.${row.sessionType ?? 'LECTURE'}`)}
                        </span>
                        <span className="text-text tabular-nums">{row.plannedHours}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}

function LoadLine({ row, onDelete }: { row: LoadRow; onDelete: () => void }) {
  const { t } = useTranslation()
  const remaining = row.remainingHours ?? 0
  return (
    <TableRow>
      <TableCell>
        {row.courseTitle}
        <span className="text-caption text-text-muted block font-mono">{row.subjectCode}</span>
      </TableCell>
      <TableCell>{row.groupCode}</TableCell>
      <TableCell>{t(`sessionType.${row.sessionType ?? 'LECTURE'}`)}</TableCell>
      <TableCell>{row.teacherName ?? '—'}</TableCell>
      <TableCell numeric>{row.plannedHours}</TableCell>
      <TableCell numeric>{row.scheduledHours}</TableCell>
      <TableCell numeric className={remaining > 0 ? 'text-warning font-semibold' : undefined}>
        {remaining}
      </TableCell>
      <TableCell className="text-right">
        <IconButton size="sm" aria-label={t('admin.deleteLoadLine')} onClick={onDelete}>
          <Trash2 aria-hidden className="size-4" />
        </IconButton>
      </TableCell>
    </TableRow>
  )
}

function useUpsertLoad(termId: string) {
  return useRefreshingMutation([loadKey(termId)], (items: LoadItem[]) =>
    unwrap(api.PUT('/api/v1/admin/teaching-load', { params: { query: { termId } }, body: { items } })),
  )
}

function NewLoadLine({ termId }: { termId: string }) {
  const { t } = useTranslation()
  const courses = useCoursesQuery({ query: '', termId, page: 0, size: 100 })
  const groups = useAllGroupsQuery()
  const teachers = useTeachersQuery()
  const save = useUpsertLoad(termId)
  const [form, setForm] = useState({
    courseId: '',
    groupId: '',
    sessionType: 'LECTURE',
    hours: '',
    teacherId: '',
  })
  const sessionType = SESSION_TYPES.find((item) => item === form.sessionType) ?? 'LECTURE'
  const ready = form.courseId !== '' && form.groupId !== '' && Number(form.hours) > 0
  return (
    <Card>
      <CardHeader title={t('admin.newLoadLine')} />
      <CardContent>
        <form
          className="grid grid-cols-1 gap-3 sm:grid-cols-2"
          onSubmit={(event) => {
            event.preventDefault()
            if (!ready) return
            save.mutate(
              [
                {
                  courseId: form.courseId,
                  groupId: form.groupId,
                  sessionType,
                  plannedHours: Number.parseInt(form.hours, 10),
                  ...(form.teacherId !== '' && { teacherUserId: form.teacherId }),
                },
              ],
              {
                onSuccess: () => {
                  setForm({ ...form, hours: '' })
                },
              },
            )
          }}
        >
          <Select
            label={t('admin.course')}
            value={form.courseId === '' ? undefined : form.courseId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              setForm({ ...form, courseId: value })
            }}
            options={(courses.data?.content ?? []).map((course) => ({
              value: course.id ?? '',
              label: `${course.subjectCode ?? ''} · ${course.title ?? ''}`,
            }))}
          />
          <Select
            label={t('profile.group')}
            value={form.groupId === '' ? undefined : form.groupId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              setForm({ ...form, groupId: value })
            }}
            options={(groups.data ?? [])
              .filter((group) => group.status !== 'ARCHIVED')
              .map((group) => ({ value: group.id ?? '', label: group.code ?? '' }))}
          />
          <Select
            label={t('authoring.sessionType')}
            value={sessionType}
            onValueChange={(value) => {
              setForm({ ...form, sessionType: value })
            }}
            options={SESSION_TYPES.map((type) => ({ value: type, label: t(`sessionType.${type}`) }))}
          />
          <Input
            label={t('authoring.hoursLabel')}
            type="number"
            min={1}
            value={form.hours}
            onChange={(event) => {
              setForm({ ...form, hours: event.target.value })
            }}
          />
          <Select
            className="sm:col-span-2"
            label={t('admin.teacher')}
            value={form.teacherId === '' ? undefined : form.teacherId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              setForm({ ...form, teacherId: value })
            }}
            options={(teachers.data ?? []).map((teacher) => ({
              value: teacher.id ?? '',
              label: teacher.fullName ?? '',
            }))}
          />
          {save.isError && (
            <p role="alert" className="text-small text-danger sm:col-span-2">
              {t(`errors.${toApiProblem(save.error).code}`)}
            </p>
          )}
          <Button type="submit" className="justify-self-start" disabled={!ready} loading={save.isPending}>
            <Plus aria-hidden className="size-4" />
            {t('authoring.add')}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

type Problem = { row: number; reason: string }

function LoadImport({ termId }: { termId: string }) {
  const { t } = useTranslation()
  const courses = useCoursesQuery({ query: '', termId, page: 0, size: 100 })
  const groups = useAllGroupsQuery()
  const teachers = useTeachersQuery()
  const save = useUpsertLoad(termId)
  const [items, setItems] = useState<LoadItem[]>([])
  const [problems, setProblems] = useState<Problem[]>([])
  const [fileName, setFileName] = useState<string>()

  const read = (file: File) => {
    setFileName(file.name)
    readSpreadsheet(file).then(
      (sheet) => {
        const found: LoadItem[] = []
        const missing: Problem[] = []
        recordsOf(sheet, SHEET).forEach((record, index) => {
          const row = index + 2
          const course = (courses.data?.content ?? []).find(
            (item) => (item.subjectCode ?? '').toLowerCase() === record.subject.toLowerCase(),
          )
          const group = (groups.data ?? []).find(
            (item) => (item.code ?? '').toLowerCase() === record.group.toLowerCase(),
          )
          const teacher = (teachers.data ?? []).find(
            (item) => (item.fullName ?? '').toLowerCase() === record.teacher.toLowerCase(),
          )
          if (course === undefined)
            missing.push({ row, reason: t('admin.loadNoCourse', { code: record.subject }) })
          else if (group === undefined)
            missing.push({ row, reason: t('admin.loadNoGroup', { code: record.group }) })
          else {
            for (const type of [
              'LECTURE',
              'PRACTICAL',
              'SEMINAR',
              'LABORATORY',
            ] as const satisfies readonly SessionType[]) {
              const hours = Number.parseInt(record[type], 10)
              if (!Number.isFinite(hours) || hours <= 0) continue
              found.push({
                courseId: course.id ?? '',
                groupId: group.id ?? '',
                sessionType: type,
                plannedHours: hours,
                ...(teacher?.id !== undefined && { teacherUserId: teacher.id }),
              })
            }
          }
        })
        setItems(found)
        setProblems(missing)
      },
      () => {
        setItems([])
        setProblems([{ row: 0, reason: t('admin.importUnreadable') }])
      },
    )
  }

  return (
    <Card>
      <CardHeader title={t('admin.loadImport')} description={t('admin.loadImportBody')} />
      <CardContent className="grid grid-cols-1 gap-3">
        <label className="border-border-strong bg-surface-raised text-small text-text hover:bg-surface-sunken focus-within:outline-focus-ring inline-flex h-10 cursor-pointer items-center gap-2 justify-self-start rounded-md border px-3 font-medium focus-within:outline-2">
          <FileUp aria-hidden className="size-4" />
          {fileName ?? t('admin.chooseFile')}
          <input
            type="file"
            accept=".xlsx,.csv"
            className="sr-only"
            onChange={(event) => {
              const file = event.target.files?.[0]
              if (file !== undefined) read(file)
            }}
          />
        </label>
        {fileName !== undefined && (
          <p role="status" className="text-small text-text">
            {t('admin.loadRead', { lines: items.length, problems: problems.length })}
          </p>
        )}
        {problems.length > 0 && (
          <ul className="text-small text-danger grid grid-cols-1 gap-0.5">
            {problems.slice(0, 10).map((problem) => (
              <li key={`${problem.row}-${problem.reason}`}>
                {problem.row > 0 && `${t('admin.row')} ${problem.row}: `}
                {problem.reason}
              </li>
            ))}
          </ul>
        )}
        {save.isError && (
          <p role="alert" className="text-small text-danger">
            {t(`errors.${toApiProblem(save.error).code}`)}
          </p>
        )}
        <Button
          className="justify-self-start"
          disabled={items.length === 0}
          loading={save.isPending}
          onClick={() => {
            save.mutate(items, {
              onSuccess: () => {
                setItems([])
                setFileName(undefined)
              },
            })
          }}
        >
          {t('admin.loadSave', { count: items.length })}
        </Button>
      </CardContent>
    </Card>
  )
}
