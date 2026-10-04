import { useState } from 'react'
import { FileUp, Plus, Trash2, UserPlus, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { LocaleTabs } from '@/features/authoring/locale-tabs'
import { toApiProblem } from '@/shared/api/errors'
import { SUPPORTED_LOCALES, isLocale, type Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge, SESSION_TYPES } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Checkbox } from '@/shared/ui/checkbox'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Switch } from '@/shared/ui/switch'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import { PUBLICATION_TONE } from './model'
import { useAllGroupsQuery } from './reference-queries'
import {
  useAddComponent,
  useAdminCourseQuery,
  useAssignTeacher,
  useCourseEnrollmentsQuery,
  useCourseStatus,
  useEnrollGroup,
  useEnrollStudents,
  useImportEnrollments,
  useRemoveTeacher,
  useSaveCourseText,
  useSetAllocation,
  useSetRequiredLocales,
  type CourseDetail,
  type CsvImportRow,
  type EnrollResult,
} from './structure-queries'
import { useTeachersQuery, useUsersQuery } from './users-queries'

function Problem({ error }: { error: unknown }) {
  const { t } = useTranslation()
  if (error === null || error === undefined) return null
  return (
    <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
      {t(`errors.${toApiProblem(error).code}`)}
    </p>
  )
}

/**
 * A-06 — one course delivery as the study office runs it: its title per language, status,
 * teachers, the hour plan per kind of class, the assessment scheme, the languages content must
 * exist in, and who is enrolled — one student, several, or a whole group at once.
 */
export function CourseAdminPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { courseId = '' } = useParams()
  const course = useAdminCourseQuery(courseId)
  const status = useCourseStatus(courseId)
  if (course.isPending) return <Skeleton className="h-96" />
  if (course.isError) {
    return (
      <ErrorState
        problem={toApiProblem(course.error)}
        onRetry={() => {
          void course.refetch()
        }}
      />
    )
  }
  const data = course.data
  const title =
    data.translations?.find((row) => row.locale === locale)?.title ??
    data.translations?.[0]?.title ??
    data.subjectCode ??
    ''
  const current = data.status ?? 'DRAFT'
  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[{ label: t('nav.adminStructure'), to: '/admin/structure?tab=courses' }, { label: title }]}
      />
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid grid-cols-1 gap-1">
          <h1 className="font-display text-display-lg text-text font-semibold wrap-anywhere">{title}</h1>
          <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
            <span className="font-mono">{data.subjectCode}</span>
            <span>{[data.cohortCode, data.termName].filter(Boolean).join(' · ')}</span>
            <span>
              {t('admin.creditsHours', { credits: data.credits ?? 0, hours: data.totalHours ?? 0 })}
            </span>
            <Badge tone={PUBLICATION_TONE[current]}>{t(`admin.publication.${current}`)}</Badge>
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          {current === 'DRAFT' && (
            <Button
              loading={status.isPending}
              onClick={() => {
                status.mutate('PUBLISHED')
              }}
            >
              {t('authoring.publish')}
            </Button>
          )}
          {current === 'PUBLISHED' && (
            <Button
              variant="outline"
              loading={status.isPending}
              onClick={() => {
                status.mutate('CLOSED')
              }}
            >
              {t('admin.closeCourse')}
            </Button>
          )}
        </div>
      </header>
      {status.isError && (
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {t(`errors.${toApiProblem(status.error).code}`)}
          {toApiProblem(status.error).message !== '' && ` ${toApiProblem(status.error).message}`}
        </p>
      )}
      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-2">
        <div className="grid grid-cols-1 gap-6">
          <CourseTexts course={data} />
          <Teachers course={data} />
          <Allocations course={data} />
        </div>
        <div className="grid grid-cols-1 gap-6">
          <Components course={data} />
          <RequiredLocales key={(data.requiredLocales ?? []).join()} course={data} />
        </div>
      </div>
      <Enrollments courseId={courseId} />
    </div>
  )
}

function CourseTexts({ course }: { course: CourseDetail }) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const row = (l: Locale) => course.translations?.find((item) => item.locale === l)
  return (
    <Card>
      <CardHeader title={t('admin.texts')} />
      <CardContent className="grid grid-cols-1 gap-4">
        <LocaleTabs
          value={locale}
          onValueChange={setLocale}
          has={(l) => row(l) !== undefined}
          required={(course.requiredLocales ?? ['uz']).filter(isLocale)}
        />
        <CourseTextForm
          key={locale}
          courseId={course.id ?? ''}
          locale={locale}
          title={row(locale)?.title ?? ''}
          description={row(locale)?.description ?? ''}
        />
      </CardContent>
    </Card>
  )
}

function CourseTextForm(props: { courseId: string; locale: Locale; title: string; description: string }) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const save = useSaveCourseText(props.courseId)
  const [title, setTitle] = useState(props.title)
  const [description, setDescription] = useState(props.description)
  return (
    <form
      className="grid grid-cols-1 gap-3"
      onSubmit={(event) => {
        event.preventDefault()
        save.mutate(
          { locale: props.locale, title: title.trim(), description: description.trim() },
          {
            onSuccess: () => {
              toast({ tone: 'success', title: t('admin.saved') })
            },
          },
        )
      }}
    >
      <Input
        label={t('admin.title')}
        value={title}
        onChange={(event) => {
          setTitle(event.target.value)
        }}
      />
      <Textarea
        label={t('authoring.quizDescription')}
        rows={3}
        value={description}
        onChange={(event) => {
          setDescription(event.target.value)
        }}
      />
      <Problem error={save.error} />
      <Button
        type="submit"
        className="justify-self-start"
        disabled={title.trim() === ''}
        loading={save.isPending}
      >
        {t('authoring.save')}
      </Button>
    </form>
  )
}

const TEACHER_ROLES = ['LEAD', 'ASSISTANT', 'GRADER', 'GUEST'] as const

function Teachers({ course }: { course: CourseDetail }) {
  const { t } = useTranslation()
  const teachers = useTeachersQuery()
  const assign = useAssignTeacher(course.id ?? '')
  const remove = useRemoveTeacher(course.id ?? '')
  const [chosen, setChosen] = useState<string>()
  const [role, setRole] = useState<(typeof TEACHER_ROLES)[number]>('LEAD')
  return (
    <Card>
      <CardHeader title={t('admin.teachers')} />
      <CardContent className="grid grid-cols-1 gap-3">
        <ul className="grid grid-cols-1 gap-1">
          {(course.teachers ?? []).map((teacher) => (
            <li key={teacher.userId} className="flex items-center gap-2">
              <span className="text-body text-text min-w-0 flex-1 truncate">{teacher.fullName}</span>
              <Badge>{t(`admin.teacherRole.${teacher.teacherRole ?? 'LEAD'}`)}</Badge>
              <IconButton
                size="sm"
                aria-label={t('admin.removeTeacher', { name: teacher.fullName ?? '' })}
                onClick={() => {
                  remove.mutate(teacher.userId ?? '')
                }}
              >
                <Trash2 aria-hidden className="size-4" />
              </IconButton>
            </li>
          ))}
        </ul>
        <div className="flex flex-wrap items-end gap-2">
          <Select
            className="w-64"
            label={t('admin.teacher')}
            value={chosen}
            placeholder={t('authoring.choose')}
            onValueChange={setChosen}
            options={(teachers.data ?? [])
              .filter((teacher) => !(course.teachers ?? []).some((item) => item.userId === teacher.id))
              .map((teacher) => ({ value: teacher.id ?? '', label: teacher.fullName ?? '' }))}
          />
          <Select
            className="w-40"
            label={t('admin.teacherRoleLabel')}
            value={role}
            onValueChange={(value) => {
              setRole(TEACHER_ROLES.find((item) => item === value) ?? 'LEAD')
            }}
            options={TEACHER_ROLES.map((value) => ({ value, label: t(`admin.teacherRole.${value}`) }))}
          />
          <Button
            variant="outline"
            disabled={chosen === undefined}
            loading={assign.isPending}
            onClick={() => {
              if (chosen !== undefined) assign.mutate({ teacherUserId: chosen, teacherRole: role })
            }}
          >
            <UserPlus aria-hidden className="size-4" />
            {t('admin.assign')}
          </Button>
        </div>
        <Problem error={assign.error ?? remove.error} />
      </CardContent>
    </Card>
  )
}

function Allocations({ course }: { course: CourseDetail }) {
  const { t } = useTranslation()
  const save = useSetAllocation(course.id ?? '')
  const planned = (course.sessionAllocations ?? []).reduce((sum, row) => sum + (row.allocatedHours ?? 0), 0)
  return (
    <Card>
      <CardHeader
        title={t('admin.hourPlan')}
        description={t('admin.hourPlanBody', { planned, total: course.totalHours ?? 0 })}
      />
      <CardContent className="grid grid-cols-1 gap-2">
        {SESSION_TYPES.map((type) => (
          <AllocationRow
            key={`${type}-${String(course.sessionAllocations?.find((row) => row.sessionType === type)?.allocatedHours ?? 0)}`}
            label={t(`sessionType.${type}`)}
            hours={course.sessionAllocations?.find((row) => row.sessionType === type)?.allocatedHours ?? 0}
            pending={save.isPending}
            onSave={(hours) => {
              save.mutate({ sessionType: type, allocatedHours: hours })
            }}
          />
        ))}
        {planned !== (course.totalHours ?? 0) && (
          <p className="text-small text-warning">{t('admin.hourPlanMismatch')}</p>
        )}
        <Problem error={save.error} />
      </CardContent>
    </Card>
  )
}

function AllocationRow(props: {
  label: string
  hours: number
  pending: boolean
  onSave: (hours: number) => void
}) {
  const { t } = useTranslation()
  const [value, setValue] = useState(String(props.hours))
  const parsed = Number.parseInt(value, 10)
  return (
    <div className="flex items-center gap-2">
      <span className="text-body text-text min-w-0 flex-1">{props.label}</span>
      <Input
        className="w-24"
        aria-label={t('admin.hoursOf', { kind: props.label })}
        type="number"
        min={0}
        value={value}
        onChange={(event) => {
          setValue(event.target.value)
        }}
      />
      <Button
        size="sm"
        variant="outline"
        disabled={!Number.isFinite(parsed) || parsed === props.hours}
        loading={props.pending}
        onClick={() => {
          props.onSave(parsed)
        }}
      >
        {t('authoring.save')}
      </Button>
    </div>
  )
}

const KINDS = [
  'CONTINUOUS',
  'MIDTERM',
  'FINAL',
  'ATTENDANCE',
  'INDEPENDENT_WORK',
  'PRACTICAL_ACTIVITY',
  'PROJECT',
  'COURSE_WORK',
] as const

function Components({ course }: { course: CourseDetail }) {
  const { t } = useTranslation()
  const add = useAddComponent(course.id ?? '')
  const components = (course.assessmentComponents ?? [])
    .slice()
    .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
  const top = components.filter((item) => item.parentComponentId === undefined)
  // each part right under its component: the order numbers count within a parent, not overall
  const ordered = top.flatMap((parent) => [
    parent,
    ...components.filter((item) => item.parentComponentId === parent.id),
  ])
  const total = top.reduce((sum, item) => sum + (item.maxPoints ?? 0), 0)
  const [form, setForm] = useState({
    code: '',
    kind: 'CONTINUOUS',
    maxPoints: '',
    minPass: '',
    parentId: '',
    blocksFinal: false,
  })
  return (
    <Card>
      <CardHeader title={t('admin.scheme')} description={t('admin.schemeBody', { total })} />
      <CardContent className="grid grid-cols-1 gap-3">
        <ul className="grid grid-cols-1 gap-1">
          {ordered.map((item) => (
            <li
              key={item.id}
              className={
                item.parentComponentId === undefined
                  ? 'flex items-center gap-2'
                  : 'flex items-center gap-2 pl-6'
              }
            >
              <span className="text-body text-text min-w-0 flex-1 font-mono">{item.code}</span>
              <span className="text-small text-text-muted">
                {t(`admin.componentKind.${item.kind ?? 'CONTINUOUS'}`)}
              </span>
              <span className="text-small text-text font-semibold tabular-nums">{item.maxPoints}</span>
              {item.minPassPercent !== undefined && (
                <Badge tone={item.blocksFinal === true ? 'warning' : 'neutral'}>
                  ≥ {item.minPassPercent}%
                </Badge>
              )}
            </li>
          ))}
        </ul>
        {total !== 100 && top.length > 0 && (
          <p className="text-small text-warning">{t('admin.schemeNot100')}</p>
        )}
        <form
          className="grid grid-cols-1 gap-2 sm:grid-cols-3"
          onSubmit={(event) => {
            event.preventDefault()
            const kind = KINDS.find((item) => item === form.kind) ?? 'CONTINUOUS'
            add.mutate(
              {
                code: form.code.trim(),
                kind,
                maxPoints: Number(form.maxPoints),
                blocksFinal: form.blocksFinal,
                displayOrder: components.length + 1,
                ...(form.minPass !== '' && { minPassPercent: Number(form.minPass) }),
                ...(form.parentId !== '' && { parentComponentId: form.parentId }),
              },
              {
                onSuccess: () => {
                  setForm({
                    code: '',
                    kind: 'CONTINUOUS',
                    maxPoints: '',
                    minPass: '',
                    parentId: '',
                    blocksFinal: false,
                  })
                },
              },
            )
          }}
        >
          <Input
            label={t('admin.code')}
            placeholder="JN"
            value={form.code}
            onChange={(event) => {
              setForm({ ...form, code: event.target.value })
            }}
          />
          <Select
            label={t('admin.kind')}
            value={form.kind}
            onValueChange={(value) => {
              setForm({ ...form, kind: value })
            }}
            options={KINDS.map((value) => ({ value, label: t(`admin.componentKind.${value}`) }))}
          />
          <Input
            label={t('authoring.maxPoints')}
            type="number"
            min={1}
            value={form.maxPoints}
            onChange={(event) => {
              setForm({ ...form, maxPoints: event.target.value })
            }}
          />
          <Input
            label={t('authoring.passingPercent')}
            type="number"
            min={0}
            max={100}
            value={form.minPass}
            onChange={(event) => {
              setForm({ ...form, minPass: event.target.value })
            }}
          />
          <Select
            label={t('admin.partOf')}
            value={form.parentId === '' ? 'none' : form.parentId}
            onValueChange={(value) => {
              setForm({ ...form, parentId: value === 'none' ? '' : value })
            }}
            options={[
              { value: 'none', label: t('admin.topLevel') },
              ...top.map((item) => ({ value: item.id ?? '', label: item.code ?? '' })),
            ]}
          />
          <Switch
            className="self-end pb-2"
            label={t('admin.blocksFinal')}
            checked={form.blocksFinal}
            onCheckedChange={(checked) => {
              setForm({ ...form, blocksFinal: checked })
            }}
          />
          <Button
            type="submit"
            variant="outline"
            className="justify-self-start sm:col-span-3"
            disabled={form.code.trim() === '' || !(Number(form.maxPoints) > 0)}
            loading={add.isPending}
          >
            <Plus aria-hidden className="size-4" />
            {t('admin.addComponent')}
          </Button>
        </form>
        <Problem error={add.error} />
      </CardContent>
    </Card>
  )
}

function RequiredLocales({ course }: { course: CourseDetail }) {
  const { t } = useTranslation()
  const save = useSetRequiredLocales(course.id ?? '')
  const [chosen, setChosen] = useState<string[]>(course.requiredLocales ?? ['uz'])
  return (
    <Card>
      <CardHeader title={t('admin.requiredLocales')} description={t('admin.requiredLocalesBody')} />
      <CardContent className="grid grid-cols-1 gap-3">
        <div className="flex flex-wrap gap-4">
          {SUPPORTED_LOCALES.map((l) => (
            <Checkbox
              key={l}
              label={l.toUpperCase()}
              checked={chosen.includes(l)}
              onCheckedChange={(checked) => {
                setChosen(checked ? [...chosen, l] : chosen.filter((item) => item !== l))
              }}
            />
          ))}
        </div>
        <Problem error={save.error} />
        <Button
          variant="outline"
          className="justify-self-start"
          disabled={chosen.length === 0}
          loading={save.isPending}
          onClick={() => {
            save.mutate(chosen)
          }}
        >
          {t('authoring.save')}
        </Button>
      </CardContent>
    </Card>
  )
}

function Enrollments({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const enrollments = useCourseEnrollmentsQuery(courseId)
  const groups = useAllGroupsQuery()
  const byGroup = useEnrollGroup(courseId)
  const students = useEnrollStudents(courseId)
  const [group, setGroup] = useState<string>()
  const [find, setFind] = useState('')
  const found = useUsersQuery({ query: find, role: 'STUDENT', page: 0 }, find.trim().length >= 2)
  const importCsv = useImportEnrollments(courseId)
  const [outcome, setOutcome] = useState<(EnrollResult | CsvImportRow)[]>()
  const [csvFailures, setCsvFailures] = useState<CsvImportRow[]>([])
  const summary = (rows: (EnrollResult | CsvImportRow)[]) => {
    const done = rows.filter((row) => row.success === true).length
    return t('admin.enrollOutcome', { done, failed: rows.length - done })
  }
  return (
    <Card>
      <CardHeader title={t('admin.enrolled', { count: enrollments.data?.length ?? 0 })} />
      <CardContent className="grid grid-cols-1 gap-4">
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="flex flex-wrap items-end gap-2">
            <Select
              className="w-56"
              label={t('admin.enrollGroup')}
              value={group}
              placeholder={t('authoring.choose')}
              onValueChange={setGroup}
              options={(groups.data ?? [])
                .filter((item) => item.status !== 'ARCHIVED')
                .map((item) => ({ value: item.code ?? '', label: item.code ?? '' }))}
            />
            <Button
              variant="outline"
              disabled={group === undefined}
              loading={byGroup.isPending}
              onClick={() => {
                if (group !== undefined)
                  byGroup.mutate(group, {
                    onSuccess: (rows) => {
                      setOutcome(rows)
                      setCsvFailures([])
                    },
                  })
              }}
            >
              <Users aria-hidden className="size-4" />
              {t('admin.enrollWholeGroup')}
            </Button>
          </div>
          <div className="grid grid-cols-1 gap-2">
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
                .filter((user) => !(enrollments.data ?? []).some((row) => row.studentUserId === user.id))
                .slice(0, 6)
                .map((user) => (
                  <li key={user.id} className="flex items-center gap-2">
                    <span className="text-small text-text min-w-0 flex-1 truncate">{user.fullName}</span>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => {
                        students.mutate([user.id ?? ''], {
                          onSuccess: (rows) => {
                            setOutcome(rows)
                            setCsvFailures([])
                          },
                        })
                      }}
                    >
                      <UserPlus aria-hidden className="size-4" />
                      {t('admin.enroll')}
                    </Button>
                  </li>
                ))}
            </ul>
          </div>
        </div>
        <div className="grid grid-cols-1 gap-1">
          <label className="inline-flex w-fit">
            <input
              type="file"
              accept=".csv,text/csv,text/plain"
              className="peer sr-only"
              onChange={(event) => {
                const file = event.target.files?.[0]
                event.target.value = ''
                if (file === undefined) return
                importCsv.mutate(file, {
                  onSuccess: (rows) => {
                    setOutcome(rows)
                    setCsvFailures(rows.filter((row) => row.success !== true))
                  },
                })
              }}
            />
            <span className="border-border text-small text-text hover:bg-surface-sunken peer-focus-visible:outline-focus-ring inline-flex cursor-pointer items-center gap-2 rounded-md border px-3 py-2 font-medium peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2">
              <FileUp aria-hidden className="size-4" />
              {importCsv.isPending ? t('admin.csvImporting') : t('admin.csvImport')}
            </span>
          </label>
          <p className="text-caption text-text-muted tracking-normal">{t('admin.csvImportHint')}</p>
        </div>
        {outcome !== undefined && (
          <p role="status" className="text-small text-text">
            {summary(outcome)}
          </p>
        )}
        {csvFailures.length > 0 && (
          <ul className="text-small grid grid-cols-1 gap-1" aria-label={t('admin.csvFailures')}>
            {csvFailures.map((row, index) => (
              <li key={`${row.identifier ?? ''}-${String(index)}`} className="text-danger">
                <span className="font-medium">{row.identifier}</span>
                {' — '}
                {row.errorCode !== undefined
                  ? t(`errors.${row.errorCode}`, { defaultValue: row.errorMessage ?? '' })
                  : row.errorMessage}
              </li>
            ))}
          </ul>
        )}
        <Problem error={byGroup.error ?? students.error ?? importCsv.error} />
        <div className="overflow-x-auto">
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>{t('gradebook.student')}</TableHeaderCell>
                <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
                <TableHeaderCell>{t('admin.enrolledAt')}</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(enrollments.data ?? []).map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.studentFullName}</TableCell>
                  <TableCell>
                    <Badge tone={row.status === 'ACTIVE' ? 'success' : 'neutral'}>
                      {t(`admin.enrollmentStatus.${row.status ?? 'ACTIVE'}`)}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {row.enrolledAt === undefined ? '' : formatRelativeTime(row.enrolledAt, locale)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  )
}
