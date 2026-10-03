import { lazy, Suspense, useState } from 'react'
import { CheckCircle2, Eye, EyeOff, PlayCircle, Plus, Trash2, XCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { useQueryClient } from '@tanstack/react-query'
import { useTeacherCoursesQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import {
  EDITOR_LANGUAGES,
  isEditorLanguage,
  languageName,
  type EditorLanguage,
} from '@/shared/editor/languages'
import type { Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { Badge, VerdictBadge, VERDICTS, type Verdict } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Checkbox } from '@/shared/ui/checkbox'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { Textarea } from '@/shared/ui/textarea'
import { ConfirmDialog } from './confirm-dialog'
import { StatusBadge } from './curriculum-tree'
import { HtmlField } from './html-field'
import { LocaleTabs } from './locale-tabs'
import { publishError } from './publish-error'
import { SaveStatus } from './save-status'
import {
  taskKeys,
  useAddHint,
  useAddTestCase,
  useAttachLanguage,
  useDeleteHint,
  useDeleteTestCase,
  useDetachLanguage,
  useSaveHintText,
  useSaveLanguageCode,
  useSaveTaskSettings,
  useSaveTaskText,
  useSaveTestCase,
  useTaskQuery,
  useTaskStatus,
  useVerificationQuery,
  useVerify,
  type CodingTaskRaw,
  type TaskSettings,
  type TaskTexts,
  type TestCaseFields,
  type TestCaseRaw,
} from './task-queries'
import { useAuthoringCourseQuery } from './queries'
import { useAutosave } from './use-autosave'

const CodeEditor = lazy(async () => ({ default: (await import('@/shared/editor/code-editor')).CodeEditor }))

const DIFFICULTIES = ['INTRODUCTORY', 'EASY', 'MEDIUM', 'HARD', 'ADVANCED'] as const
const SCORING = ['PER_TEST_EQUAL', 'PER_TEST_WEIGHTED', 'ALL_OR_NOTHING'] as const
const COMPARISON = [
  'TRIMMED',
  'EXACT',
  'IGNORE_WHITESPACE',
  'CASE_INSENSITIVE',
  'NUMERIC_TOLERANCE',
  'REGEX',
] as const

function isVerdict(value: string | undefined): value is Verdict {
  return value !== undefined && (VERDICTS as readonly string[]).includes(value)
}

/**
 * T-05 — a coding task, edited: the statement in three languages; starter code and a reference
 * solution for Python, Java and C++; the tests; the limits; the hints; and "Verify solutions",
 * which runs every reference solution against every test before the task is published.
 */
export function CodingTaskEditorPage() {
  const { taskId = '' } = useParams()
  const task = useTaskQuery(taskId)
  const queryClient = useQueryClient()
  const [revision, setRevision] = useState(0)
  const reload = () => {
    void queryClient.invalidateQueries({ queryKey: taskKeys.task(taskId) }).then(() => {
      setRevision((value) => value + 1)
    })
  }
  if (task.isPending) return <Skeleton className="h-[32rem]" />
  if (task.isError) {
    return (
      <ErrorState
        problem={toApiProblem(task.error)}
        onRetry={() => {
          void task.refetch()
        }}
      />
    )
  }
  return <TaskEditor key={revision} task={task.data} onReload={reload} />
}

function TaskEditor({ task, onReload }: { task: CodingTaskRaw; onReload: () => void }) {
  const { t } = useTranslation()
  const courses = useTeacherCoursesQuery()
  const courseId = task.courseId ?? ''
  const course = useAuthoringCourseQuery(courseId)
  const status = useTaskStatus(task.id ?? '')
  const courseTitle = courses.data?.find((item) => item.id === courseId)?.title ?? ''
  const title = task.title ?? task.code ?? ''
  const required = course.data?.requiredLocales ?? ['uz']
  const published = task.status === 'PUBLISHED'

  return (
    <div className="grid gap-6">
      <Breadcrumb
        items={[
          { label: t('nav.teacherClasses'), to: '/teacher/courses' },
          { label: courseTitle, to: `/teacher/courses/${courseId}?tab=practice` },
          { label: title },
        ]}
      />
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid gap-1">
          <h1 className="font-display text-display-lg text-text font-semibold">{title}</h1>
          <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
            {task.code}
            {task.status !== undefined && <StatusBadge status={task.status} />}
          </p>
        </div>
        <Button
          variant={published ? 'outline' : 'primary'}
          loading={status.isPending}
          onClick={() => {
            status.mutate(published ? 'DRAFT' : 'PUBLISHED')
          }}
        >
          {published ? <EyeOff aria-hidden className="size-4" /> : <Eye aria-hidden className="size-4" />}
          {published ? t('authoring.unpublish') : t('authoring.publish')}
        </Button>
      </header>
      {status.isError && (
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {publishError(toApiProblem(status.error), t)}
        </p>
      )}

      <Tabs defaultValue="statement">
        <TabsList className="overflow-x-auto">
          <TabsTrigger value="statement">{t('authoring.taskStatement')}</TabsTrigger>
          <TabsTrigger value="code">{t('authoring.taskCode')}</TabsTrigger>
          <TabsTrigger value="tests">
            {t('authoring.taskTests', { count: task.testCases?.length ?? 0 })}
          </TabsTrigger>
          <TabsTrigger value="settings">{t('authoring.taskSettings')}</TabsTrigger>
          <TabsTrigger value="hints">
            {t('authoring.taskHints', { count: task.hints?.length ?? 0 })}
          </TabsTrigger>
          <TabsTrigger value="verify">{t('authoring.verify')}</TabsTrigger>
        </TabsList>
        <TabsContent value="statement" className="pt-6">
          <StatementEditor task={task} required={required} onReload={onReload} />
        </TabsContent>
        <TabsContent value="code" className="pt-6">
          <LanguagesEditor task={task} />
        </TabsContent>
        <TabsContent value="tests" className="pt-6">
          <TestsEditor task={task} onReload={onReload} />
        </TabsContent>
        <TabsContent value="settings" className="pt-6">
          <SettingsEditor task={task} onReload={onReload} />
        </TabsContent>
        <TabsContent value="hints" className="pt-6">
          <HintsEditor task={task} required={required} />
        </TabsContent>
        <TabsContent value="verify" className="pt-6">
          <VerificationPanel task={task} />
        </TabsContent>
      </Tabs>
    </div>
  )
}

// ------------------------------------------------------------------ statement

function textsOf(task: CodingTaskRaw, locale: Locale): TaskTexts | undefined {
  const row = task.translations?.find((item) => item.locale === locale)
  if (row === undefined) return undefined
  return {
    title: row.title ?? '',
    description: row.description ?? '',
    instructions: row.instructions ?? '',
    constraintsText: row.constraintsText ?? '',
    explanation: row.explanation ?? '',
  }
}

function StatementEditor({
  task,
  required,
  onReload,
}: {
  task: CodingTaskRaw
  required: readonly Locale[]
  onReload: () => void
}) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const save = useSaveTaskText(task.id ?? '')
  const autosave = useAutosave((value: { locale: Locale; texts: TaskTexts }) => save.mutateAsync(value))
  return (
    <Card>
      <CardHeader
        title={t('authoring.taskStatement')}
        action={<SaveStatus state={autosave.state} onReload={onReload} />}
      />
      <CardContent className="grid gap-4">
        <LocaleTabs
          value={locale}
          onValueChange={(next) => {
            autosave.flush()
            setLocale(next)
          }}
          has={(l) => textsOf(task, l) !== undefined}
          required={required}
        />
        <StatementFields
          key={locale}
          initial={
            textsOf(task, locale) ?? {
              title: '',
              description: '',
              instructions: '',
              constraintsText: '',
              explanation: '',
            }
          }
          onChange={(texts) => {
            // the server keeps a language only with both its title and its statement
            if (texts.title.trim() !== '' && texts.description.trim() !== '')
              autosave.change({ locale, texts })
          }}
        />
      </CardContent>
    </Card>
  )
}

function StatementFields({
  initial,
  onChange,
}: {
  initial: TaskTexts
  onChange: (texts: TaskTexts) => void
}) {
  const { t } = useTranslation()
  const [texts, setTexts] = useState(initial)
  const set = (key: keyof TaskTexts, value: string) => {
    const next = { ...texts, [key]: value }
    setTexts(next)
    onChange(next)
  }
  return (
    <div className="grid gap-4">
      <Input
        label={t('authoring.taskTitle')}
        value={texts.title}
        maxLength={300}
        onChange={(event) => {
          set('title', event.target.value)
        }}
      />
      <HtmlField
        label={t('authoring.taskDescription')}
        hint={t('authoring.descriptionRequired')}
        value={texts.description}
        rows={10}
        onChange={(value) => {
          set('description', value)
        }}
      />
      <Textarea
        label={t('task.inputOutput')}
        value={texts.instructions}
        rows={4}
        onChange={(event) => {
          set('instructions', event.target.value)
        }}
      />
      <Textarea
        label={t('task.constraints')}
        value={texts.constraintsText}
        rows={3}
        onChange={(event) => {
          set('constraintsText', event.target.value)
        }}
      />
      <Textarea
        label={t('authoring.taskExplanation')}
        value={texts.explanation}
        rows={4}
        onChange={(event) => {
          set('explanation', event.target.value)
        }}
      />
    </div>
  )
}

// ------------------------------------------------------------------ starter code and solutions

function LanguagesEditor({ task }: { task: CodingTaskRaw }) {
  const { t } = useTranslation()
  const [language, setLanguage] = useState<EditorLanguage>('python')
  const attach = useAttachLanguage(task.id ?? '')
  const attached = task.languages?.find((item) => item.code === language)

  return (
    <div className="grid gap-4">
      <div role="group" aria-label={t('authoring.programmingLanguage')} className="flex flex-wrap gap-2">
        {EDITOR_LANGUAGES.map((code) => {
          const has = task.languages?.some((item) => item.code === code) === true
          return (
            <button
              key={code}
              type="button"
              aria-pressed={language === code}
              onClick={() => {
                setLanguage(code)
              }}
              className={cn(
                'text-small focus-visible:outline-focus-ring inline-flex h-9 items-center gap-2 rounded-md border px-3 font-semibold focus-visible:outline-2 focus-visible:outline-offset-2',
                language === code
                  ? 'border-primary bg-primary text-primary-foreground'
                  : 'border-border bg-surface-raised text-text',
              )}
            >
              {languageName(code)}
              {has ? (
                <CheckCircle2 aria-label={t('authoring.languageAttached')} className="size-4" />
              ) : (
                <span className="sr-only">{t('authoring.languageMissing')}</span>
              )}
            </button>
          )
        })}
      </div>
      {attached === undefined ? (
        <EmptyState
          title={t('authoring.languageNotAttached', { language: languageName(language) })}
          body={t('authoring.languageNotAttachedBody')}
          action={
            <Button
              loading={attach.isPending}
              onClick={() => {
                attach.mutate({ code: language, starterCode: '', solutionCode: '' })
              }}
            >
              <Plus aria-hidden className="size-4" />
              {t('authoring.attachLanguage', { language: languageName(language) })}
            </Button>
          }
        />
      ) : (
        <LanguageCode
          key={attached.taskLanguageId}
          taskId={task.id ?? ''}
          taskLanguageId={attached.taskLanguageId ?? ''}
          language={language}
          starter={attached.starterCode ?? ''}
          solution={attached.solutionCode ?? ''}
        />
      )}
      {attach.isError && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(attach.error).code}`)}
        </p>
      )}
    </div>
  )
}

function LanguageCode({
  taskId,
  taskLanguageId,
  language,
  starter,
  solution,
}: {
  taskId: string
  taskLanguageId: string
  language: EditorLanguage
  starter: string
  solution: string
}) {
  const { t } = useTranslation()
  const save = useSaveLanguageCode()
  const detach = useDetachLanguage(taskId)
  const [code, setCode] = useState({ starterCode: starter, solutionCode: solution })
  const [confirming, setConfirming] = useState(false)
  const autosave = useAutosave((value: typeof code) => save.mutateAsync({ taskLanguageId, ...value }))
  const change = (next: typeof code) => {
    setCode(next)
    autosave.change(next)
  }
  return (
    <Card>
      <CardHeader
        title={languageName(language)}
        action={
          <div className="flex items-center gap-3">
            <SaveStatus state={autosave.state} />
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setConfirming(true)
              }}
            >
              <Trash2 aria-hidden className="size-4" />
              {t('authoring.detachLanguage')}
            </Button>
          </div>
        }
      />
      <CardContent className="grid gap-4 xl:grid-cols-2">
        {(['starterCode', 'solutionCode'] as const).map((key) => (
          <div key={key} className="grid gap-1.5">
            <span className="text-small text-text font-medium">{t(`authoring.${key}`)}</span>
            <p className="text-caption text-text-muted tracking-normal">{t(`authoring.${key}Hint`)}</p>
            <Suspense fallback={<Skeleton className="h-72" />}>
              <CodeEditor
                aria-label={`${t(`authoring.${key}`)} — ${languageName(language)}`}
                value={code[key]}
                language={language}
                onChange={(value) => {
                  change({ ...code, [key]: value })
                }}
                className="border-border h-72 rounded-md border"
              />
            </Suspense>
          </div>
        ))}
      </CardContent>
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title={t('authoring.detachLanguageTitle', { language: languageName(language) })}
        body={t('authoring.detachLanguageBody')}
        confirmLabel={t('authoring.detachLanguage')}
        pending={detach.isPending}
        onConfirm={() => {
          detach.mutate(taskLanguageId, {
            onSettled: () => {
              setConfirming(false)
            },
          })
        }}
      />
    </Card>
  )
}

// ------------------------------------------------------------------ tests

function TestsEditor({ task, onReload }: { task: CodingTaskRaw; onReload: () => void }) {
  const { t } = useTranslation()
  const add = useAddTestCase(task.id ?? '')
  const tests = (task.testCases ?? []).slice().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
  const weighted = task.scoringStrategy === 'PER_TEST_WEIGHTED'
  return (
    <div className="grid gap-4">
      <p className="text-small text-text-muted max-w-prose">{t('authoring.testsHint')}</p>
      {tests.length === 0 && <EmptyState title={t('authoring.noTests')} body={t('authoring.noTestsBody')} />}
      {tests.map((test, index) => (
        <TestCaseCard
          key={test.id}
          taskId={task.id ?? ''}
          test={test}
          index={index}
          weighted={weighted}
          onReload={onReload}
        />
      ))}
      <Button
        variant="outline"
        className="justify-self-start"
        loading={add.isPending}
        onClick={() => {
          add.mutate({ stdin: '', expectedStdout: '' })
        }}
      >
        <Plus aria-hidden className="size-4" />
        {t('authoring.addTest')}
      </Button>
    </div>
  )
}

function fieldsOf(test: TestCaseRaw): TestCaseFields {
  return {
    name: test.name ?? '',
    stdin: test.stdin ?? '',
    expectedStdout: test.expectedStdout ?? '',
    visibility: test.visibility ?? 'HIDDEN',
    isExample: test.isExample ?? false,
    weight: test.weight ?? 1,
    comparisonMode: test.comparisonMode ?? 'TRIMMED',
    ...(test.numericTolerance !== undefined && { numericTolerance: test.numericTolerance }),
  }
}

function TestCaseCard({
  taskId,
  test,
  index,
  weighted,
  onReload,
}: {
  taskId: string
  test: TestCaseRaw
  index: number
  weighted: boolean
  onReload: () => void
}) {
  const { t } = useTranslation()
  const save = useSaveTestCase()
  const remove = useDeleteTestCase(taskId)
  const [fields, setFields] = useState(() => fieldsOf(test))
  const [confirming, setConfirming] = useState(false)
  const autosave = useAutosave((value: TestCaseFields) =>
    save.mutateAsync({ id: test.id ?? '', fields: value }),
  )
  const change = (patch: Partial<TestCaseFields>) => {
    const next = { ...fields, ...patch }
    // an example is shown to the student, so it cannot be hidden
    if (next.isExample === true) next.visibility = 'VISIBLE'
    setFields(next)
    autosave.change(next)
  }
  const label =
    fields.name !== undefined && fields.name !== ''
      ? fields.name
      : t('authoring.testNumber', { number: index + 1 })

  return (
    <article aria-label={label} className="border-border bg-surface-raised grid gap-3 rounded-lg border p-4">
      <header className="flex flex-wrap items-center gap-2">
        <h3 className="text-body text-text font-semibold">{label}</h3>
        <Badge tone={fields.visibility === 'VISIBLE' ? 'info' : 'neutral'}>
          {t(`authoring.testVisibility.${fields.visibility ?? 'HIDDEN'}`)}
        </Badge>
        {fields.isExample === true && <Badge tone="accent">{t('authoring.example')}</Badge>}
        <span className="ml-auto flex items-center gap-2">
          <SaveStatus state={autosave.state} onReload={onReload} />
          <IconButton
            aria-label={t('authoring.deleteTest', { name: label })}
            onClick={() => {
              setConfirming(true)
            }}
          >
            <Trash2 aria-hidden className="size-4" />
          </IconButton>
        </span>
      </header>
      <div className="grid gap-3 md:grid-cols-2">
        <Textarea
          label={t('authoring.stdin')}
          value={fields.stdin ?? ''}
          rows={4}
          className="font-mono"
          onChange={(event) => {
            change({ stdin: event.target.value })
          }}
        />
        <Textarea
          label={t('authoring.expectedStdout')}
          value={fields.expectedStdout ?? ''}
          rows={4}
          className="font-mono"
          onChange={(event) => {
            change({ expectedStdout: event.target.value })
          }}
        />
      </div>
      <div className="grid items-end gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <Input
          label={t('authoring.testName')}
          value={fields.name ?? ''}
          onChange={(event) => {
            change({ name: event.target.value })
          }}
        />
        <Select
          label={t('authoring.comparison')}
          value={fields.comparisonMode ?? 'TRIMMED'}
          onValueChange={(value) => {
            const mode = COMPARISON.find((item) => item === value)
            if (mode !== undefined) change({ comparisonMode: mode })
          }}
          options={COMPARISON.map((value) => ({ value, label: t(`authoring.comparisonMode.${value}`) }))}
        />
        {weighted && (
          <Input
            label={t('authoring.weight')}
            type="number"
            min={0}
            step={0.5}
            value={String(fields.weight)}
            onChange={(event) => {
              change({ weight: Number(event.target.value) || 0 })
            }}
          />
        )}
        <div className="grid gap-2 pb-1">
          <Checkbox
            label={t('authoring.example')}
            checked={fields.isExample === true}
            onCheckedChange={(checked) => {
              change({ isExample: checked })
            }}
          />
          <Checkbox
            label={t('authoring.visibleToStudents')}
            checked={fields.visibility === 'VISIBLE'}
            disabled={fields.isExample === true}
            onCheckedChange={(checked) => {
              change({ visibility: checked ? 'VISIBLE' : 'HIDDEN' })
            }}
          />
        </div>
      </div>
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title={t('authoring.deleteTestTitle')}
        body={t('authoring.deleteTestBody', { name: label })}
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending}
        onConfirm={() => {
          remove.mutate(test.id ?? '', {
            onSettled: () => {
              setConfirming(false)
            },
          })
        }}
      />
    </article>
  )
}

// ------------------------------------------------------------------ settings

function SettingsEditor({ task, onReload }: { task: CodingTaskRaw; onReload: () => void }) {
  const { t } = useTranslation()
  const save = useSaveTaskSettings(task.id ?? '')
  const [settings, setSettings] = useState<TaskSettings>(() => ({
    maxPoints: task.maxPoints ?? 100,
    ...(task.difficulty !== undefined && { difficulty: task.difficulty }),
    ...(task.scoringStrategy !== undefined && { scoringStrategy: task.scoringStrategy }),
    ...(task.maxAttempts !== undefined && { maxAttempts: task.maxAttempts }),
    ...(task.cpuTimeLimitMs !== undefined && { cpuTimeLimitMs: task.cpuTimeLimitMs }),
    ...(task.wallTimeLimitMs !== undefined && { wallTimeLimitMs: task.wallTimeLimitMs }),
    ...(task.memoryLimitKb !== undefined && { memoryLimitKb: task.memoryLimitKb }),
  }))
  const autosave = useAutosave((value: TaskSettings) => save.mutateAsync(value))
  const change = (patch: Partial<TaskSettings>) => {
    const next = { ...settings, ...patch }
    setSettings(next)
    autosave.change(next)
  }
  const number = (value: string) => {
    const parsed = Number(value)
    return value === '' || !Number.isFinite(parsed) ? undefined : parsed
  }

  return (
    <Card>
      <CardHeader
        title={t('authoring.taskSettings')}
        action={<SaveStatus state={autosave.state} onReload={onReload} />}
      />
      <CardContent className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Select
          label={t('authoring.difficulty')}
          value={settings.difficulty ?? 'EASY'}
          onValueChange={(value) => {
            const difficulty = DIFFICULTIES.find((item) => item === value)
            if (difficulty !== undefined) change({ difficulty })
          }}
          options={DIFFICULTIES.map((value) => ({ value, label: t(`authoring.difficultyLevel.${value}`) }))}
        />
        <Select
          label={t('authoring.scoring')}
          value={settings.scoringStrategy ?? 'PER_TEST_EQUAL'}
          onValueChange={(value) => {
            const scoringStrategy = SCORING.find((item) => item === value)
            if (scoringStrategy !== undefined) change({ scoringStrategy })
          }}
          options={SCORING.map((value) => ({ value, label: t(`authoring.scoringMode.${value}`) }))}
        />
        <Input
          label={t('authoring.maxPoints')}
          type="number"
          min={0}
          value={String(settings.maxPoints)}
          onChange={(event) => {
            change({ maxPoints: number(event.target.value) ?? 0 })
          }}
        />
        <Input
          label={t('authoring.maxAttempts')}
          type="number"
          min={1}
          value={settings.maxAttempts === undefined ? '' : String(settings.maxAttempts)}
          onChange={(event) => {
            const value = number(event.target.value)
            change(value === undefined ? { maxAttempts: undefined } : { maxAttempts: value })
          }}
        />
        <Input
          label={t('authoring.cpuLimit')}
          type="number"
          min={100}
          step={100}
          value={settings.cpuTimeLimitMs === undefined ? '' : String(settings.cpuTimeLimitMs)}
          onChange={(event) => {
            const value = number(event.target.value)
            if (value !== undefined) change({ cpuTimeLimitMs: value })
          }}
        />
        <Input
          label={t('authoring.wallLimit')}
          type="number"
          min={100}
          step={100}
          value={settings.wallTimeLimitMs === undefined ? '' : String(settings.wallTimeLimitMs)}
          onChange={(event) => {
            const value = number(event.target.value)
            if (value !== undefined) change({ wallTimeLimitMs: value })
          }}
        />
        <Input
          label={t('authoring.memoryLimit')}
          type="number"
          min={16}
          value={
            settings.memoryLimitKb === undefined ? '' : String(Math.round(settings.memoryLimitKb / 1024))
          }
          onChange={(event) => {
            const value = number(event.target.value)
            if (value !== undefined) change({ memoryLimitKb: value * 1024 })
          }}
        />
      </CardContent>
    </Card>
  )
}

// ------------------------------------------------------------------ hints

function HintsEditor({ task, required }: { task: CodingTaskRaw; required: readonly Locale[] }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const add = useAddHint(task.id ?? '')
  const [body, setBody] = useState('')
  const [penalty, setPenalty] = useState('10')
  const hints = (task.hints ?? []).slice().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
  return (
    <div className="grid gap-4">
      <p className="text-small text-text-muted max-w-prose">{t('authoring.hintsHint')}</p>
      {hints.map((hint, index) => (
        <HintCard key={hint.id} taskId={task.id ?? ''} hint={hint} index={index} required={required} />
      ))}
      <Card>
        <CardHeader title={t('authoring.newHint')} />
        <CardContent>
          <form
            className="grid gap-3"
            onSubmit={(event) => {
              event.preventDefault()
              if (body.trim() === '') return
              add.mutate(
                { penaltyPercent: Number(penalty) || 0, body: body.trim(), locale },
                {
                  onSuccess: () => {
                    setBody('')
                  },
                },
              )
            }}
          >
            <Textarea
              label={t('authoring.hintText', { locale: locale.toUpperCase() })}
              value={body}
              rows={3}
              onChange={(event) => {
                setBody(event.target.value)
              }}
            />
            <div className="flex flex-wrap items-end gap-3">
              <Input
                className="w-32"
                label={t('authoring.penalty')}
                type="number"
                min={0}
                max={100}
                value={penalty}
                onChange={(event) => {
                  setPenalty(event.target.value)
                }}
              />
              <Button type="submit" disabled={body.trim() === ''} loading={add.isPending}>
                <Plus aria-hidden className="size-4" />
                {t('authoring.addHint')}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

type HintRaw = NonNullable<CodingTaskRaw['hints']>[number]

function HintCard({
  taskId,
  hint,
  index,
  required,
}: {
  taskId: string
  hint: HintRaw
  index: number
  required: readonly Locale[]
}) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const save = useSaveHintText()
  const remove = useDeleteHint(taskId)
  const autosave = useAutosave((value: { locale: Locale; body: string }) =>
    save.mutateAsync({ hintId: hint.id ?? '', ...value }),
  )
  const bodyIn = (l: Locale) => hint.translations?.find((item) => item.locale === l)?.body
  return (
    <article className="border-border bg-surface-raised grid gap-3 rounded-lg border p-4">
      <header className="flex flex-wrap items-center gap-2">
        <h3 className="text-body text-text font-semibold">
          {t('authoring.hintNumber', { number: index + 1 })}
        </h3>
        <Badge tone="warning">{t('authoring.penaltyValue', { percent: hint.penaltyPercent ?? 0 })}</Badge>
        <span className="ml-auto flex items-center gap-2">
          <SaveStatus state={autosave.state} />
          <IconButton
            aria-label={t('authoring.deleteHint', { number: index + 1 })}
            onClick={() => {
              remove.mutate(hint.id ?? '')
            }}
          >
            <Trash2 aria-hidden className="size-4" />
          </IconButton>
        </span>
      </header>
      <LocaleTabs
        value={locale}
        onValueChange={(next) => {
          autosave.flush()
          setLocale(next)
        }}
        has={(l) => bodyIn(l) !== undefined}
        required={required}
      />
      <HintBody
        key={locale}
        initial={bodyIn(locale) ?? ''}
        onChange={(body) => {
          if (body.trim() !== '') autosave.change({ locale, body })
        }}
      />
    </article>
  )
}

function HintBody({ initial, onChange }: { initial: string; onChange: (body: string) => void }) {
  const { t } = useTranslation()
  const [body, setBody] = useState(initial)
  return (
    <Textarea
      label={t('authoring.hintBody')}
      value={body}
      rows={3}
      onChange={(event) => {
        setBody(event.target.value)
        onChange(event.target.value)
      }}
    />
  )
}

// ------------------------------------------------------------------ verification

function VerificationPanel({ task }: { task: CodingTaskRaw }) {
  const { t } = useTranslation()
  const verification = useVerificationQuery(task.id ?? '')
  const verify = useVerify(task.id ?? '')
  const runs = verification.data?.runs ?? []
  const running = runs.some((run) => run.status === 'QUEUED' || run.status === 'RUNNING') || verify.isPending
  const missing = verification.data?.languagesWithoutSolution ?? []

  return (
    <div className="grid gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-small text-text-muted max-w-prose">{t('authoring.verifyHint')}</p>
        <Button
          loading={running}
          onClick={() => {
            verify.mutate()
          }}
        >
          <PlayCircle aria-hidden className="size-4" />
          {t('authoring.verifyRun')}
        </Button>
      </div>
      {verify.isError && (
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {t(`errors.${toApiProblem(verify.error).code}`)}
        </p>
      )}
      {verification.isPending && <Skeleton className="h-40" />}
      {verification.isSuccess && runs.length === 0 && (
        <EmptyState title={t('authoring.notVerified')} body={t('authoring.notVerifiedBody')} />
      )}
      {runs.length > 0 && !running && (
        <p
          role="status"
          className={cn(
            'text-body flex items-center gap-2 font-semibold',
            verification.data?.allPassed === true ? 'text-success' : 'text-danger',
          )}
        >
          {verification.data?.allPassed === true ? (
            <CheckCircle2 aria-hidden className="size-5" />
          ) : (
            <XCircle aria-hidden className="size-5" />
          )}
          {verification.data?.allPassed === true ? t('authoring.verifyPassed') : t('authoring.verifyFailed')}
        </p>
      )}
      {missing.length > 0 && (
        <p className="text-small text-warning">
          {t('authoring.withoutSolution', { languages: missing.map(languageName).join(', ') })}
        </p>
      )}
      <div className="grid gap-4 lg:grid-cols-3">
        {runs.map((run) => (
          <Card key={run.id}>
            <CardHeader
              title={
                isEditorLanguage(run.languageCode ?? '')
                  ? languageName(run.languageCode ?? '')
                  : (run.languageCode ?? '')
              }
              action={
                run.status === 'COMPLETED' && isVerdict(run.verdict) ? (
                  <VerdictBadge verdict={run.verdict} label={t(`verdict.${run.verdict}`)} />
                ) : (
                  <Badge tone="info">
                    {t(`task.status.${run.status === 'RUNNING' ? 'RUNNING' : 'QUEUED'}`)}
                  </Badge>
                )
              }
            />
            <CardContent className="grid gap-2">
              {run.compileOutput !== undefined && run.compileOutput !== '' && (
                <pre className="bg-surface-sunken text-caption text-danger max-h-40 overflow-auto rounded-sm p-2 font-mono">
                  {run.compileOutput}
                </pre>
              )}
              {run.testsTotal !== undefined && (
                <p className="text-small text-text-muted tabular-nums">
                  {t('authoring.testsPassed', { passed: run.testsPassed ?? 0, total: run.testsTotal })}
                </p>
              )}
              <ul className="grid gap-1">
                {(run.tests ?? []).map((test) => (
                  <li key={test.testCaseId ?? test.position} className="text-small flex items-center gap-2">
                    {test.passed === true ? (
                      <CheckCircle2 aria-label={t('authoring.passed')} className="text-success size-4" />
                    ) : (
                      <XCircle aria-label={t('authoring.failed')} className="text-danger size-4" />
                    )}
                    <span className="text-text">
                      {test.name !== undefined && test.name !== ''
                        ? test.name
                        : t('authoring.testNumber', { number: (test.position ?? 0) + 1 })}
                    </span>
                    {test.passed !== true && test.verdict !== undefined && (
                      <span className="text-caption text-text-muted">{test.verdict}</span>
                    )}
                    {test.timeMs !== undefined && (
                      <span className="text-caption text-text-muted ml-auto tabular-nums">
                        {test.timeMs} ms
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
