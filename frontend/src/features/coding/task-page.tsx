import { lazy, Suspense, useEffect, useRef, useState } from 'react'
import {
  ArrowLeft,
  Maximize2,
  Minimize2,
  Minus,
  Play,
  Plus,
  RotateCcw,
  Send,
  TestTubeDiagonal,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { useActivitiesQuery, useCourseQuery } from '@/features/courses/queries'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'
import { CODE_LANGUAGE_KEY, isEditorLanguage, type EditorLanguage } from '@/shared/editor/languages'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { useMediaQuery } from '@/shared/lib/use-media-query'
import { useSharedPreference } from '@/shared/lib/use-shared-preference'
import { Button } from '@/shared/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from '@/shared/ui/dialog'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { LocaleControl } from '@/shared/ui/locale-control'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { SplitPane } from '@/shared/ui/split-pane'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { Textarea } from '@/shared/ui/textarea'
import { clearDraft, isFinished, readDraft, writeDraft, type SubmissionSummary, type Task } from './model'
import {
  useHistoryQuery,
  useRefreshAfterGrading,
  useRunQuery,
  useStartRun,
  useSubmissionQuery,
  useSubmit,
  useTaskQuery,
} from './queries'
import { CustomRunResult, ExamplesRunResult, HistoryList, SubmissionResult } from './task-results'
import { TaskStatement } from './task-statement'

const CodeEditor = lazy(async () => ({ default: (await import('@/shared/editor/code-editor')).CodeEditor }))

const FONT_SIZE_KEY = 'aifu-editor-font-size'
const FONT_SIZES = [12, 13, 14, 16, 18, 20] as const
const DEFAULT_FONT_SIZE = 14

type ResultTab = 'output' | 'examples' | 'submission' | 'history'
type Job = { id: string; startedAt: number }

/**
 * S-05 — the practice task: an IDE, not a form. Statement on the left, editor and results on
 * the right, the divider draggable; on a phone three tabs with Run and Submit pinned below.
 */
export function TaskPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { changeLocale } = useAuth()
  const { courseId = '', taskId = '' } = useParams()
  const task = useTaskQuery(courseId, taskId, locale)
  const course = useCourseQuery(courseId, locale)

  return (
    <div className="bg-surface flex h-dvh flex-col">
      <header className="border-border flex h-14 shrink-0 items-center gap-1 border-b px-2 md:gap-2 md:px-6">
        <Link
          to={`/student/courses/${courseId}?tab=work`}
          className={cn(
            'text-small text-text-muted flex h-11 min-w-0 items-center gap-2 rounded-sm px-2 font-medium',
            'duration-fast ease-standard hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          )}
        >
          <ArrowLeft aria-hidden className="size-5 shrink-0" />
          <span className="truncate">{course.data?.title ?? t('lesson.back')}</span>
        </Link>
        <div className="flex-1" />
        <LocaleControl value={locale} onChange={changeLocale} />
      </header>

      {task.isPending && (
        <div className="grid gap-6 p-6 lg:grid-cols-2">
          <SkeletonText lines={8} />
          <Skeleton className="h-96" />
        </div>
      )}
      {task.isError && (
        <div className="mx-auto w-full max-w-xl p-6">
          <ErrorState
            problem={toApiProblem(task.error)}
            onRetry={() => {
              void task.refetch()
            }}
          />
        </div>
      )}
      {task.isSuccess && <Workspace key={task.data.id} task={task.data} />}
    </div>
  )
}

function initialLanguage(task: Task, preferred: string | null): EditorLanguage | undefined {
  const wanted = task.languages.find((language) => language.code === preferred)
  return (wanted ?? task.languages[0])?.code
}

function Workspace({ task }: { task: Task }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const isDesktop = useMediaQuery('(min-width: 64rem)')
  const [preferred, setPreferred] = useSharedPreference(CODE_LANGUAGE_KEY)
  const [storedFontSize, setStoredFontSize] = useSharedPreference(FONT_SIZE_KEY)
  const fontSize = FONT_SIZES.find((size) => String(size) === storedFontSize) ?? DEFAULT_FONT_SIZE

  const [language, setLanguage] = useState(() => initialLanguage(task, preferred))
  const starter = task.languages.find((item) => item.code === language)?.starterCode ?? ''
  const [code, setCode] = useState(() =>
    language === undefined ? '' : (readDraft(task.id, language) ?? starter),
  )
  const [stdin, setStdin] = useState(task.examples[0]?.stdin ?? '')
  const [tab, setTab] = useState<ResultTab>('output')
  const [phoneTab, setPhoneTab] = useState<'task' | 'code' | 'result'>('task')
  const [fullScreen, setFullScreen] = useState(false)
  const [resetOpen, setResetOpen] = useState(false)
  const [problem, setProblem] = useState<ApiProblem | null>(null)

  const [customJob, setCustomJob] = useState<Job>()
  const [examplesJob, setExamplesJob] = useState<Job>()
  const [submissionJob, setSubmissionJob] = useState<Job>()
  const [viewed, setViewed] = useState<SubmissionSummary>()

  const startRun = useStartRun()
  const submit = useSubmit()
  const customRun = useRunQuery(customJob?.id, customJob?.startedAt ?? 0)
  const examplesRun = useRunQuery(examplesJob?.id, examplesJob?.startedAt ?? 0)
  const submission = useSubmissionQuery(submissionJob?.id, submissionJob?.startedAt ?? 0)
  const viewedSubmission = useSubmissionQuery(viewed?.id, 0)
  const history = useHistoryQuery(task.id)
  const activities = useActivitiesQuery(locale, task.courseId)
  const activity = activities.data?.find((item) => item.referenceId === task.id)
  const refreshAfterGrading = useRefreshAfterGrading(task.courseId, task.id)

  const runBusy =
    startRun.isPending ||
    (customRun.data !== undefined && !isFinished(customRun.data.status)) ||
    (examplesRun.data !== undefined && !isFinished(examplesRun.data.status))
  const submitBusy =
    submit.isPending || (submission.data !== undefined && !isFinished(submission.data.status))
  const attemptsUsed = history.data?.total
  const attemptsLeft =
    task.maxAttempts !== undefined && attemptsUsed !== undefined ? task.maxAttempts - attemptsUsed : undefined
  const canSubmit = language !== undefined && !submitBusy && (attemptsLeft === undefined || attemptsLeft > 0)

  // once a graded submission is finished, the work list and the plan have changed
  const refreshedFor = useRef<string | null>(null)
  const finishedSubmissionId =
    submission.data !== undefined && isFinished(submission.data.status) ? submission.data.id : undefined
  useEffect(() => {
    if (finishedSubmissionId !== undefined && refreshedFor.current !== finishedSubmissionId) {
      refreshedFor.current = finishedSubmissionId
      void refreshAfterGrading()
    }
  }, [finishedSubmissionId, refreshAfterGrading])

  const edit = (next: string) => {
    setCode(next)
    if (language !== undefined) writeDraft(task.id, language, next)
  }

  const switchLanguage = (next: string) => {
    if (!isEditorLanguage(next)) return
    const nextStarter = task.languages.find((item) => item.code === next)?.starterCode ?? ''
    setLanguage(next)
    setPreferred(next)
    setCode(readDraft(task.id, next) ?? nextStarter)
  }

  const run = (mode: 'CUSTOM' | 'EXAMPLES') => {
    if (language === undefined || runBusy) return
    setProblem(null)
    setTab(mode === 'CUSTOM' ? 'output' : 'examples')
    setPhoneTab('result')
    startRun.mutate(
      { taskId: task.id, language, source: code, mode, ...(mode === 'CUSTOM' && { stdin }) },
      {
        onSuccess: ({ run: started, startedAt }) => {
          const job = { id: started.id, startedAt }
          if (mode === 'CUSTOM') setCustomJob(job)
          else setExamplesJob(job)
        },
        onError: (error) => {
          setProblem(toApiProblem(error))
        },
      },
    )
  }

  const send = () => {
    if (language === undefined || !canSubmit) return
    setProblem(null)
    setViewed(undefined)
    setTab('submission')
    setPhoneTab('result')
    submit.mutate(
      { taskId: task.id, language, source: code, idempotencyKey: crypto.randomUUID() },
      {
        onSuccess: ({ submission: created, startedAt }) => {
          setSubmissionJob({ id: created.id, startedAt })
        },
        onError: (error) => {
          setProblem(toApiProblem(error))
        },
      },
    )
  }

  const restore = (source: string, sourceLanguage: string) => {
    if (!isEditorLanguage(sourceLanguage)) return
    setLanguage(sourceLanguage)
    setPreferred(sourceLanguage)
    setCode(source)
    writeDraft(task.id, sourceLanguage, source)
    setPhoneTab('code')
  }

  const fontStep = (direction: -1 | 1) => {
    const index = FONT_SIZES.indexOf(fontSize)
    const next = FONT_SIZES[index + direction]
    if (next !== undefined) setStoredFontSize(String(next))
  }

  const statement = (
    <div className="h-full overflow-y-auto px-4 py-6 md:px-6">
      <TaskStatement task={task} activity={activity} attemptsUsed={attemptsUsed} />
    </div>
  )

  const actions = (
    <div className="flex flex-wrap items-center gap-2">
      <Button
        variant="outline"
        size="sm"
        disabled={language === undefined || runBusy}
        onClick={() => {
          run('CUSTOM')
        }}
      >
        <Play aria-hidden className="size-4" />
        {t('task.run')}
        <kbd className="text-caption text-text-muted hidden font-sans tracking-normal lg:inline">
          Ctrl+Enter
        </kbd>
      </Button>
      <Button
        variant="outline"
        size="sm"
        disabled={language === undefined || runBusy || task.examples.length === 0}
        onClick={() => {
          run('EXAMPLES')
        }}
      >
        <TestTubeDiagonal aria-hidden className="size-4" />
        {t('task.runExamples')}
      </Button>
      <Button variant="accent" size="sm" disabled={!canSubmit} loading={submit.isPending} onClick={send}>
        <Send aria-hidden className="size-4" />
        {t('task.submit')}
        <kbd className="text-caption hidden font-sans tracking-normal opacity-80 lg:inline">
          Ctrl+Shift+Enter
        </kbd>
      </Button>
    </div>
  )

  const editor = (
    <div
      className={cn('bg-surface-raised flex min-h-0 flex-col', fullScreen ? 'fixed inset-0 z-30' : 'h-full')}
    >
      <div className="border-border flex shrink-0 flex-wrap items-center gap-2 border-b px-3 py-2">
        {language !== undefined && (
          <SegmentedControl
            aria-label={t('lesson.codeLanguage')}
            size="sm"
            value={language}
            onValueChange={switchLanguage}
            options={task.languages.map((item) => ({ value: item.code, label: item.name }))}
          />
        )}
        <div className="flex-1" />
        <IconButton
          size="sm"
          aria-label={t('task.fontSmaller')}
          disabled={fontSize === FONT_SIZES[0]}
          onClick={() => {
            fontStep(-1)
          }}
        >
          <Minus aria-hidden className="size-4" />
        </IconButton>
        <span
          aria-hidden
          className="text-caption text-text-muted w-5 text-center tracking-normal tabular-nums"
        >
          {fontSize}
        </span>
        <IconButton
          size="sm"
          aria-label={t('task.fontLarger')}
          disabled={fontSize === FONT_SIZES[FONT_SIZES.length - 1]}
          onClick={() => {
            fontStep(1)
          }}
        >
          <Plus aria-hidden className="size-4" />
        </IconButton>
        <IconButton
          size="sm"
          aria-label={t('task.reset')}
          onClick={() => {
            setResetOpen(true)
          }}
        >
          <RotateCcw aria-hidden className="size-4" />
        </IconButton>
        <IconButton
          size="sm"
          className="hidden lg:inline-flex"
          aria-label={fullScreen ? t('task.exitFullScreen') : t('task.fullScreen')}
          onClick={() => {
            setFullScreen(!fullScreen)
          }}
        >
          {fullScreen ? (
            <Minimize2 aria-hidden className="size-4" />
          ) : (
            <Maximize2 aria-hidden className="size-4" />
          )}
        </IconButton>
      </div>
      <div className="min-h-0 flex-1">
        {language !== undefined && (
          <Suspense fallback={<Skeleton className="m-4 h-40" />}>
            <CodeEditor
              aria-label={t('task.editorLabel')}
              className="h-full"
              value={code}
              language={language}
              fontSize={fontSize}
              onChange={edit}
              onRun={() => {
                run('CUSTOM')
              }}
              onSubmit={send}
            />
          </Suspense>
        )}
      </div>
      {fullScreen && <div className="border-border shrink-0 border-t px-3 py-2">{actions}</div>}
    </div>
  )

  const shownSubmission = viewed !== undefined ? viewedSubmission.data : submission.data

  const results = (
    <Tabs
      value={tab}
      onValueChange={(value) => {
        if (value === 'output' || value === 'examples' || value === 'submission' || value === 'history') {
          setTab(value)
        }
      }}
      className="flex h-full min-h-0 flex-col"
    >
      <TabsList className="shrink-0 px-3">
        <TabsTrigger value="output">{t('task.tabOutput')}</TabsTrigger>
        <TabsTrigger value="examples">{t('task.tabExamples')}</TabsTrigger>
        <TabsTrigger value="submission">{t('task.tabSubmission')}</TabsTrigger>
        <TabsTrigger value="history">
          {t('task.tabHistory')}
          {attemptsUsed !== undefined && attemptsUsed > 0 && (
            <span className="text-text-muted ml-1.5 tabular-nums">{attemptsUsed}</span>
          )}
        </TabsTrigger>
      </TabsList>
      <div className="min-h-0 flex-1 overflow-y-auto p-4">
        {problem !== null && (
          <p
            role="alert"
            className="bg-danger-soft text-small text-danger mb-4 rounded-sm px-3 py-2 font-medium"
          >
            {t(`errors.${problem.code}`)}
          </p>
        )}
        <TabsContent value="output" className="grid gap-4">
          <Textarea
            label={t('task.customInput')}
            rows={3}
            value={stdin}
            className="font-mono"
            onChange={(event) => {
              setStdin(event.target.value)
            }}
          />
          {customRun.data !== undefined ? (
            <CustomRunResult
              run={customRun.data}
              onRetry={() => {
                run('CUSTOM')
              }}
            />
          ) : (
            <p className="text-small text-text-muted">{t('task.outputEmpty')}</p>
          )}
        </TabsContent>
        <TabsContent value="examples">
          {examplesRun.data !== undefined ? (
            <ExamplesRunResult
              run={examplesRun.data}
              onRetry={() => {
                run('EXAMPLES')
              }}
            />
          ) : (
            <p className="text-small text-text-muted">{t('task.examplesEmpty')}</p>
          )}
        </TabsContent>
        <TabsContent value="submission" className="grid gap-4">
          {viewed !== undefined && (
            <div className="border-border flex flex-wrap items-center justify-between gap-3 border-b pb-3">
              <p className="text-small text-text-muted">
                {t('task.viewingAttempt', { number: viewed.attemptNumber })}
              </p>
              {viewedSubmission.data !== undefined && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    restore(viewedSubmission.data.sourceCode, viewedSubmission.data.language)
                  }}
                >
                  <RotateCcw aria-hidden className="size-4" />
                  {t('task.restore')}
                </Button>
              )}
            </div>
          )}
          {viewed !== undefined && viewedSubmission.isPending && <SkeletonText lines={3} />}
          {shownSubmission !== undefined ? (
            <>
              <SubmissionResult submission={shownSubmission} reveal={viewed === undefined} onRetry={send} />
              {viewed !== undefined && isEditorLanguage(shownSubmission.language) && (
                <div className="border-border h-64 overflow-hidden rounded-md border">
                  <Suspense fallback={<Skeleton className="h-full" />}>
                    <CodeEditor
                      aria-label={t('task.submittedCode')}
                      className="h-full"
                      value={shownSubmission.sourceCode}
                      language={shownSubmission.language}
                      fontSize={13}
                      readOnly
                    />
                  </Suspense>
                </div>
              )}
            </>
          ) : (
            viewed === undefined && <p className="text-small text-text-muted">{t('task.submissionEmpty')}</p>
          )}
        </TabsContent>
        <TabsContent value="history">
          <HistoryList
            history={history}
            selectedId={viewed?.id}
            onSelect={(item) => {
              setViewed(item)
              setTab('submission')
            }}
          />
        </TabsContent>
      </div>
    </Tabs>
  )

  return (
    <>
      {isDesktop ? (
        <SplitPane
          className="min-h-0 flex-1"
          storageKey="aifu-task-split"
          dividerLabel={t('task.divider')}
          left={statement}
          right={
            <div className="flex h-full min-h-0 flex-col">
              <div className="min-h-0 flex-3">{editor}</div>
              <div className="border-border flex shrink-0 items-center justify-between gap-3 border-y px-3 py-2">
                {actions}
                {attemptsLeft !== undefined && (
                  <span className="text-small text-text-muted tabular-nums">
                    {t('task.attemptsLeft', { count: attemptsLeft })}
                  </span>
                )}
              </div>
              <div className="min-h-0 flex-2">{results}</div>
            </div>
          }
        />
      ) : (
        <div className="flex min-h-0 flex-1 flex-col">
          <SegmentedControl
            aria-label={t('task.phoneTabs')}
            className="mx-4 mt-3 grid shrink-0 grid-cols-3"
            value={phoneTab}
            onValueChange={(value) => {
              if (value === 'task' || value === 'code' || value === 'result') setPhoneTab(value)
            }}
            options={[
              { value: 'task', label: t('task.phoneTask') },
              { value: 'code', label: t('task.phoneCode') },
              { value: 'result', label: t('task.phoneResult') },
            ]}
          />
          <div className="min-h-0 flex-1">
            {phoneTab === 'task' && statement}
            {phoneTab === 'code' && editor}
            {phoneTab === 'result' && results}
          </div>
          <div className="border-border bg-surface-raised pb-safe shrink-0 border-t px-3 py-2">{actions}</div>
        </div>
      )}

      <Dialog open={resetOpen} onOpenChange={setResetOpen}>
        <DialogContent>
          <DialogTitle>{t('task.resetTitle')}</DialogTitle>
          <DialogDescription>{t('task.resetBody')}</DialogDescription>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">{t('task.resetCancel')}</Button>
            </DialogClose>
            <Button
              variant="danger"
              onClick={() => {
                if (language !== undefined) clearDraft(task.id, language)
                setCode(starter)
                setResetOpen(false)
              }}
            >
              {t('task.reset')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
