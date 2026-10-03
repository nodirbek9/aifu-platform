import { useState } from 'react'
import { Code2, ListChecks, Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { languageName } from '@/shared/editor/languages'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { StatusBadge } from './curriculum-tree'
import { useCourseQuizzesQuery, useCreateQuiz } from './quiz-queries'
import { useCourseTasksQuery, useCreateTask } from './task-queries'

type Titled = { title?: string; code?: string; translations?: readonly { locale?: string; title?: string }[] }

function titleIn(item: Titled, locale: string): string {
  return item.translations?.find((row) => row.locale === locale)?.title ?? item.title ?? item.code ?? '—'
}

/** A short code from a title, made unique enough for one course. */
function codeFor(prefix: string, title: string): string {
  const base = title
    .normalize('NFKD')
    .replace(/[^A-Za-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
    .toUpperCase()
    .slice(0, 30)
  return `${prefix}-${base === '' ? 'NEW' : base}-${Date.now().toString(36).slice(-4).toUpperCase()}`
}

/** T-05 / T-06 entry — the course's coding tasks and quizzes, each opening its editor; new ones made here. */
export function PracticeList({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const tasks = useCourseTasksQuery(courseId)
  const quizzes = useCourseQuizzesQuery(courseId)
  const [creating, setCreating] = useState<'task' | 'quiz'>()

  return (
    <div className="grid items-start gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader
          title={t('authoring.codingTasks')}
          action={
            <Button
              size="sm"
              onClick={() => {
                setCreating('task')
              }}
            >
              <Plus aria-hidden className="size-4" />
              {t('authoring.newTask')}
            </Button>
          }
        />
        <CardContent>
          {tasks.isPending && <Skeleton className="h-40" />}
          {tasks.isError && (
            <ErrorState
              problem={toApiProblem(tasks.error)}
              onRetry={() => {
                void tasks.refetch()
              }}
            />
          )}
          {tasks.isSuccess && tasks.data.length === 0 && (
            <EmptyState icon={<Code2 aria-hidden className="size-6" />} title={t('authoring.noTasks')} />
          )}
          <ul className="divide-border grid divide-y">
            {tasks.data?.map((task) => (
              <li key={task.id} className="flex flex-wrap items-center gap-2 py-2.5">
                <Link
                  to={`/teacher/coding-tasks/${task.id ?? ''}/edit`}
                  className="text-body text-text hover:text-primary-hover min-w-0 flex-1 truncate font-medium underline-offset-4 hover:underline"
                >
                  {titleIn(task, locale)}
                </Link>
                <span className="text-caption text-text-muted tracking-normal">
                  {(task.languages ?? []).map((language) => languageName(language.code ?? '')).join(' · ')}
                  {' · '}
                  {t('authoring.testCount', { count: task.testCases?.length ?? 0 })}
                </span>
                {task.status !== undefined && <StatusBadge status={task.status} />}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader
          title={t('authoring.quizzes')}
          action={
            <Button
              size="sm"
              onClick={() => {
                setCreating('quiz')
              }}
            >
              <Plus aria-hidden className="size-4" />
              {t('authoring.newQuiz')}
            </Button>
          }
        />
        <CardContent>
          {quizzes.isPending && <Skeleton className="h-40" />}
          {quizzes.isError && (
            <ErrorState
              problem={toApiProblem(quizzes.error)}
              onRetry={() => {
                void quizzes.refetch()
              }}
            />
          )}
          {quizzes.isSuccess && quizzes.data.length === 0 && (
            <EmptyState
              icon={<ListChecks aria-hidden className="size-6" />}
              title={t('authoring.noQuizzes')}
            />
          )}
          <ul className="divide-border grid divide-y">
            {quizzes.data?.map((quiz) => (
              <li key={quiz.id} className="flex flex-wrap items-center gap-2 py-2.5">
                <Link
                  to={`/teacher/quizzes/${quiz.id ?? ''}/edit`}
                  className="text-body text-text hover:text-primary-hover min-w-0 flex-1 truncate font-medium underline-offset-4 hover:underline"
                >
                  {titleIn(quiz, locale)}
                </Link>
                {quiz.timeLimitSeconds !== undefined && (
                  <span className="text-caption text-text-muted tracking-normal">
                    {t('quiz.minutes', { count: Math.round(quiz.timeLimitSeconds / 60) })}
                  </span>
                )}
                {quiz.status !== undefined && <StatusBadge status={quiz.status} />}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <NewPracticeDialog
        courseId={courseId}
        kind={creating}
        onClose={() => {
          setCreating(undefined)
        }}
      />
    </div>
  )
}

function NewPracticeDialog({
  courseId,
  kind,
  onClose,
}: {
  courseId: string
  kind: 'task' | 'quiz' | undefined
  onClose: () => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const navigate = useNavigate()
  const createTask = useCreateTask(courseId)
  const createQuiz = useCreateQuiz(courseId)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const needsDescription = kind === 'task'
  const ready = title.trim() !== '' && (!needsDescription || description.trim() !== '')
  const pending = createTask.isPending || createQuiz.isPending
  const failure = createTask.error ?? createQuiz.error

  return (
    <Dialog
      open={kind !== undefined}
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent>
        <DialogTitle>{kind === 'quiz' ? t('authoring.newQuiz') : t('authoring.newTask')}</DialogTitle>
        <DialogDescription>{t('authoring.newPracticeBody')}</DialogDescription>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (!ready) return
            const done = {
              onSuccess: (id: string) => {
                setTitle('')
                setDescription('')
                onClose()
                void navigate(
                  kind === 'quiz' ? `/teacher/quizzes/${id}/edit` : `/teacher/coding-tasks/${id}/edit`,
                )
              },
            }
            if (kind === 'quiz')
              createQuiz.mutate({ code: codeFor('Q', title), title: title.trim(), locale }, done)
            else {
              createTask.mutate(
                {
                  code: codeFor('T', title),
                  title: title.trim(),
                  description: `<p>${description.trim().replace(/&/g, '&amp;').replace(/</g, '&lt;')}</p>`,
                  locale,
                },
                done,
              )
            }
          }}
        >
          <Input
            label={t('authoring.titleIn', { locale: locale.toUpperCase() })}
            value={title}
            maxLength={200}
            onChange={(event) => {
              setTitle(event.target.value)
            }}
          />
          {needsDescription && (
            <Textarea
              label={t('authoring.taskDescription')}
              value={description}
              rows={4}
              onChange={(event) => {
                setDescription(event.target.value)
              }}
            />
          )}
          {failure !== null && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(failure).code}`)}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={!ready} loading={pending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
