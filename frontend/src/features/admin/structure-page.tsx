import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { PageHeader } from '@/shared/ui/page-header'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { PUBLICATION_TONE } from './model'
import { useAcademicYearsQuery, useDepartmentsQuery, useTermsQuery } from './reference-queries'
import {
  useAllSubjectsQuery,
  useCoursesQuery,
  useCreateCourse,
  useCreateSubject,
  useSubjectsQuery,
} from './structure-queries'

/** The list's filters live in the address: a link to a filtered page is shareable. */
function useListParams() {
  const [search, setSearch] = useSearchParams()
  const set = (key: string, value: string) => {
    const next = new URLSearchParams(search)
    if (value === '' || value === 'all') next.delete(key)
    else next.set(key, value)
    if (key !== 'page' && key !== 'tab') next.delete('page')
    setSearch(next, { replace: true })
  }
  return {
    search,
    set,
    query: search.get('q') ?? '',
    status: search.get('status') ?? undefined,
    page: Number(search.get('page') ?? '0') || 0,
  }
}

/** A-06 — the academic structure: subjects and the courses that deliver them in a term. */
export function StructurePage() {
  const { t } = useTranslation()
  const params = useListParams()
  const tab = params.search.get('tab') === 'courses' ? 'courses' : 'subjects'
  return (
    <div className="grid grid-cols-1 gap-6">
      <PageHeader title={t('nav.adminStructure')} context={t('admin.structureContext')} />
      <Tabs
        value={tab}
        onValueChange={(value) => {
          params.set('tab', value === 'subjects' ? '' : value)
        }}
      >
        <TabsList>
          <TabsTrigger value="subjects">{t('admin.subjects')}</TabsTrigger>
          <TabsTrigger value="courses">{t('admin.courses')}</TabsTrigger>
        </TabsList>
        <TabsContent value="subjects" className="pt-6">
          {tab === 'subjects' && <SubjectsTab />}
        </TabsContent>
        <TabsContent value="courses" className="pt-6">
          {tab === 'courses' && <CoursesTab />}
        </TabsContent>
      </Tabs>
    </div>
  )
}

function Filters({ statuses }: { statuses: readonly string[] }) {
  const { t } = useTranslation()
  const params = useListParams()
  return (
    <div className="flex flex-wrap items-end gap-3">
      <Input
        className="w-72"
        label={t('common.search')}
        type="search"
        value={params.query}
        onChange={(event) => {
          params.set('q', event.target.value)
        }}
      />
      <Select
        className="w-44"
        label={t('admin.status')}
        value={params.status ?? 'all'}
        onValueChange={(value) => {
          params.set('status', value)
        }}
        options={[
          { value: 'all', label: t('admin.allStatuses') },
          ...statuses.map((status) => ({ value: status, label: t(`admin.publication.${status}`) })),
        ]}
      />
    </div>
  )
}

function SubjectsTab() {
  const { t } = useTranslation()
  const params = useListParams()
  const subjects = useSubjectsQuery({
    query: params.query,
    ...(params.status !== undefined && { status: params.status }),
    page: params.page,
  })
  const [creating, setCreating] = useState(false)
  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <Filters statuses={['DRAFT', 'PUBLISHED', 'ARCHIVED']} />
        <Button
          onClick={() => {
            setCreating(true)
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('admin.newSubject')}
        </Button>
      </div>
      {subjects.isPending && <Skeleton className="h-64" />}
      {subjects.isError && (
        <ErrorState
          problem={toApiProblem(subjects.error)}
          onRetry={() => {
            void subjects.refetch()
          }}
        />
      )}
      {subjects.isSuccess && (subjects.data.content ?? []).length === 0 && (
        <EmptyState title={t('admin.noSubjects')} />
      )}
      {subjects.isSuccess && (subjects.data.content ?? []).length > 0 && (
        <>
          <div className="border-border overflow-x-auto rounded-lg border">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('admin.code')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.title')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.department')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.kind')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(subjects.data.content ?? []).map((subject) => (
                  <TableRow key={subject.id}>
                    <TableCell className="font-mono">{subject.code}</TableCell>
                    <TableCell>
                      <Link
                        to={`/admin/structure/subjects/${subject.id ?? ''}`}
                        className="text-text hover:text-primary-hover font-medium underline-offset-4 hover:underline"
                      >
                        {subject.title}
                      </Link>
                    </TableCell>
                    <TableCell>{subject.departmentName}</TableCell>
                    <TableCell>{t(`admin.subjectKind.${subject.subjectKind ?? 'MANDATORY'}`)}</TableCell>
                    <TableCell>
                      <Badge tone={PUBLICATION_TONE[subject.status ?? 'DRAFT']}>
                        {t(`admin.publication.${subject.status ?? 'DRAFT'}`)}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <Pagination
            page={params.page}
            totalPages={subjects.data.totalPages ?? 1}
            onPageChange={(page) => {
              params.set('page', String(page))
            }}
          />
        </>
      )}
      {creating && (
        <NewSubjectDialog
          onClose={() => {
            setCreating(false)
          }}
        />
      )}
    </div>
  )
}

function NewSubjectDialog({ onClose }: { onClose: () => void }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const navigate = useNavigate()
  const departments = useDepartmentsQuery()
  const create = useCreateSubject()
  const [form, setForm] = useState({ code: '', title: '', departmentId: '', kind: 'MANDATORY' })
  const kind = form.kind === 'ELECTIVE' || form.kind === 'OPTIONAL' ? form.kind : 'MANDATORY'
  const ready = form.code.trim() !== '' && form.title.trim() !== '' && form.departmentId !== ''
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent>
        <DialogTitle>{t('admin.newSubject')}</DialogTitle>
        <DialogDescription>{t('admin.newSubjectBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (!ready) return
            create.mutate(
              {
                code: form.code.trim(),
                departmentId: form.departmentId,
                subjectKind: kind,
                defaultInstructionLanguage: locale,
                title: form.title.trim(),
                locale,
              },
              {
                onSuccess: (subject) => {
                  onClose()
                  if (subject.id !== undefined) void navigate(`/admin/structure/subjects/${subject.id}`)
                },
              },
            )
          }}
        >
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              label={t('admin.code')}
              placeholder="DAS1110"
              value={form.code}
              onChange={(event) => {
                setForm({ ...form, code: event.target.value })
              }}
            />
            <Select
              label={t('admin.kind')}
              value={kind}
              onValueChange={(value) => {
                setForm({ ...form, kind: value })
              }}
              options={['MANDATORY', 'ELECTIVE', 'OPTIONAL'].map((value) => ({
                value,
                label: t(`admin.subjectKind.${value}`),
              }))}
            />
          </div>
          <Input
            label={t('authoring.titleIn', { locale: locale.toUpperCase() })}
            value={form.title}
            onChange={(event) => {
              setForm({ ...form, title: event.target.value })
            }}
          />
          <Select
            label={t('admin.department')}
            value={form.departmentId === '' ? undefined : form.departmentId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              setForm({ ...form, departmentId: value })
            }}
            options={(departments.data ?? []).map((item) => ({
              value: item.id ?? '',
              label: item.name ?? '',
            }))}
          />
          {create.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(create.error).code}`)}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={!ready} loading={create.isPending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function CoursesTab() {
  const { t } = useTranslation()
  const params = useListParams()
  const courses = useCoursesQuery({
    query: params.query,
    ...(params.status !== undefined && { status: params.status }),
    page: params.page,
  })
  const [creating, setCreating] = useState(false)
  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <Filters statuses={['DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED']} />
        <Button
          onClick={() => {
            setCreating(true)
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('admin.newCourse')}
        </Button>
      </div>
      {courses.isPending && <Skeleton className="h-64" />}
      {courses.isError && (
        <ErrorState
          problem={toApiProblem(courses.error)}
          onRetry={() => {
            void courses.refetch()
          }}
        />
      )}
      {courses.isSuccess && (courses.data.content ?? []).length === 0 && (
        <EmptyState title={t('admin.noCourses')} />
      )}
      {courses.isSuccess && (courses.data.content ?? []).length > 0 && (
        <>
          <div className="border-border overflow-x-auto rounded-lg border">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('admin.title')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.code')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.term')}</TableHeaderCell>
                  <TableHeaderCell numeric>{t('admin.credits')}</TableHeaderCell>
                  <TableHeaderCell>{t('admin.status')}</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(courses.data.content ?? []).map((course) => (
                  <TableRow key={course.id}>
                    <TableCell>
                      <Link
                        to={`/admin/structure/courses/${course.id ?? ''}`}
                        className="text-text hover:text-primary-hover font-medium underline-offset-4 hover:underline"
                      >
                        {course.title}
                      </Link>
                      {course.cohortCode !== undefined && (
                        <span className="text-caption text-text-muted block">{course.cohortCode}</span>
                      )}
                    </TableCell>
                    <TableCell className="font-mono">{course.subjectCode}</TableCell>
                    <TableCell>{course.termName}</TableCell>
                    <TableCell numeric>{course.credits}</TableCell>
                    <TableCell>
                      <Badge tone={PUBLICATION_TONE[course.status ?? 'DRAFT']}>
                        {t(`admin.publication.${course.status ?? 'DRAFT'}`)}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <Pagination
            page={params.page}
            totalPages={courses.data.totalPages ?? 1}
            onPageChange={(page) => {
              params.set('page', String(page))
            }}
          />
        </>
      )}
      {creating && (
        <NewCourseDialog
          onClose={() => {
            setCreating(false)
          }}
        />
      )}
    </div>
  )
}

function NewCourseDialog({ onClose }: { onClose: () => void }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const navigate = useNavigate()
  const subjects = useAllSubjectsQuery()
  const years = useAcademicYearsQuery()
  const [yearId, setYearId] = useState<string>()
  const chosenYear = yearId ?? years.data?.find((year) => year.current === true)?.id
  const terms = useTermsQuery(chosenYear)
  const create = useCreateCourse()
  const [form, setForm] = useState({
    subjectId: '',
    termId: '',
    title: '',
    credits: '6',
    totalHours: '180',
    cohortCode: '',
  })
  const ready =
    form.subjectId !== '' && form.termId !== '' && form.title.trim() !== '' && Number(form.credits) > 0
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent className="max-w-xl">
        <DialogTitle>{t('admin.newCourse')}</DialogTitle>
        <DialogDescription>{t('admin.newCourseBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (!ready) return
            create.mutate(
              {
                subjectId: form.subjectId,
                termId: form.termId,
                credits: Number(form.credits),
                ...(form.totalHours !== '' && { totalHours: Number.parseInt(form.totalHours, 10) }),
                ...(form.cohortCode.trim() !== '' && { cohortCode: form.cohortCode.trim() }),
                instructionLanguage: locale,
                title: form.title.trim(),
                locale,
              },
              {
                onSuccess: (course) => {
                  onClose()
                  if (course.id !== undefined) void navigate(`/admin/structure/courses/${course.id}`)
                },
              },
            )
          }}
        >
          <Select
            label={t('admin.subject')}
            value={form.subjectId === '' ? undefined : form.subjectId}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              const subject = subjects.data?.find((item) => item.id === value)
              setForm({
                ...form,
                subjectId: value,
                title: form.title === '' ? (subject?.title ?? '') : form.title,
              })
            }}
            options={(subjects.data ?? []).map((item) => ({
              value: item.id ?? '',
              label: `${item.code ?? ''} · ${item.title ?? ''}`,
            }))}
          />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Select
              label={t('admin.academicYear')}
              value={chosenYear}
              placeholder={t('authoring.choose')}
              onValueChange={setYearId}
              options={(years.data ?? []).map((year) => ({ value: year.id ?? '', label: year.code ?? '' }))}
            />
            <Select
              label={t('admin.term')}
              value={form.termId === '' ? undefined : form.termId}
              placeholder={t('authoring.choose')}
              onValueChange={(value) => {
                setForm({ ...form, termId: value })
              }}
              options={(terms.data ?? []).map((term) => ({ value: term.id ?? '', label: term.name ?? '' }))}
            />
          </div>
          <Input
            label={t('authoring.titleIn', { locale: locale.toUpperCase() })}
            value={form.title}
            onChange={(event) => {
              setForm({ ...form, title: event.target.value })
            }}
          />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Input
              label={t('admin.credits')}
              type="number"
              min={1}
              step={0.5}
              value={form.credits}
              onChange={(event) => {
                setForm({ ...form, credits: event.target.value })
              }}
            />
            <Input
              label={t('admin.totalHours')}
              type="number"
              min={1}
              value={form.totalHours}
              onChange={(event) => {
                setForm({ ...form, totalHours: event.target.value })
              }}
            />
            <Input
              label={t('admin.cohort')}
              placeholder="DI-26-01-S1"
              value={form.cohortCode}
              onChange={(event) => {
                setForm({ ...form, cohortCode: event.target.value })
              }}
            />
          </div>
          {create.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(create.error).code}`)}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={!ready} loading={create.isPending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
