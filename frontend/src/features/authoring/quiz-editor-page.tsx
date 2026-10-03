import { useState } from 'react'
import { ArrowDown, ArrowUp, Eye, EyeOff, Plus, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { useQueryClient } from '@tanstack/react-query'
import { useTeacherCoursesQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import type { Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { Badge } from '@/shared/ui/badge'
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
import { Switch } from '@/shared/ui/switch'
import { Textarea } from '@/shared/ui/textarea'
import { ConfirmDialog } from './confirm-dialog'
import { StatusBadge } from './curriculum-tree'
import { LocaleTabs } from './locale-tabs'
import { moved } from './model'
import { publishError } from './publish-error'
import { useAuthoringCourseQuery } from './queries'
import {
  useAddAnswerKey,
  useAddOption,
  useAddQuestion,
  useDeleteAnswerKey,
  useDeleteOption,
  useDeleteQuestion,
  useQuestionsQuery,
  useQuizQuery,
  useQuizStatus,
  useReorderOptions,
  useReorderQuestions,
  useSaveOptionFields,
  useSaveOptionText,
  useSaveQuestionFields,
  useSaveQuestionText,
  useSaveQuizSettings,
  useSaveQuizText,
  type MatchMode,
  type QuestionRaw,
  type QuestionTexts,
  type QuestionType,
  type QuizRaw,
  type QuizSettings,
  type QuizTexts,
} from './quiz-queries'
import { SaveStatus } from './save-status'
import { useAutosave } from './use-autosave'

const QUESTION_TYPES: readonly QuestionType[] = [
  'SINGLE_CHOICE',
  'MULTIPLE_CHOICE',
  'TRUE_FALSE',
  'SHORT_ANSWER',
  'NUMERIC',
  'ORDERING',
  'MATCHING',
]
const REVEAL = ['AFTER_ATTEMPT', 'IMMEDIATELY', 'AFTER_LAST_ATTEMPT', 'AFTER_DUE_DATE', 'NEVER'] as const
const MATCH_MODES = ['CASE_INSENSITIVE', 'EXACT', 'TRIMMED', 'REGEX'] as const

/**
 * T-06 — a quiz, edited: its settings (time, attempts, shuffling, when answers are revealed), its
 * texts in three languages, and its questions of every type with their options and answer keys.
 */
export function QuizEditorPage() {
  const { quizId = '' } = useParams()
  const quiz = useQuizQuery(quizId)
  const queryClient = useQueryClient()
  const [revision, setRevision] = useState(0)
  const reload = () => {
    void queryClient.invalidateQueries({ queryKey: ['authoring', 'quiz', quizId] }).then(() => {
      setRevision((value) => value + 1)
    })
  }
  if (quiz.isPending) return <Skeleton className="h-[32rem]" />
  if (quiz.isError) {
    return (
      <ErrorState
        problem={toApiProblem(quiz.error)}
        onRetry={() => {
          void quiz.refetch()
        }}
      />
    )
  }
  return <QuizEditor key={revision} quiz={quiz.data} onReload={reload} />
}

function QuizEditor({ quiz, onReload }: { quiz: QuizRaw; onReload: () => void }) {
  const { t } = useTranslation()
  const quizId = quiz.id ?? ''
  const courseId = quiz.courseId ?? ''
  const courses = useTeacherCoursesQuery()
  const course = useAuthoringCourseQuery(courseId)
  const status = useQuizStatus(quizId)
  const questions = useQuestionsQuery(quizId)
  const reorder = useReorderQuestions(quizId)
  const [adding, setAdding] = useState(false)
  const required = course.data?.requiredLocales ?? ['uz']
  const published = quiz.status === 'PUBLISHED'
  const title = quiz.title ?? quiz.code ?? ''
  const courseTitle = courses.data?.find((item) => item.id === courseId)?.title ?? ''
  const list = questions.data ?? []
  const points = list.reduce((sum, question) => sum + (question.points ?? 0), 0)

  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[
          { label: t('nav.teacherClasses'), to: '/teacher/courses' },
          { label: courseTitle, to: `/teacher/courses/${courseId}?tab=practice` },
          { label: title },
        ]}
      />
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid grid-cols-1 gap-1">
          <h1 className="font-display text-display-lg text-text font-semibold">{title}</h1>
          <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
            {quiz.code}
            {quiz.status !== undefined && <StatusBadge status={quiz.status} />}
            <span>{t('authoring.questionCount', { count: list.length, points })}</span>
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

      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-12">
        <div className="grid grid-cols-1 gap-4 xl:col-span-4">
          <QuizTextsCard quiz={quiz} required={required} onReload={onReload} />
          <QuizSettingsCard quiz={quiz} onReload={onReload} />
        </div>
        <section aria-labelledby="questions-title" className="grid grid-cols-1 gap-4 xl:col-span-8">
          <h2 id="questions-title" className="font-display text-h2 text-text font-semibold">
            {t('authoring.questions')}
          </h2>
          {questions.isPending && <Skeleton className="h-64" />}
          {questions.isError && (
            <ErrorState
              problem={toApiProblem(questions.error)}
              onRetry={() => {
                void questions.refetch()
              }}
            />
          )}
          {questions.isSuccess && list.length === 0 && (
            <EmptyState title={t('authoring.noQuestions')} body={t('authoring.noQuestionsBody')} />
          )}
          {list.map((question, index) => (
            <QuestionCard
              key={question.id}
              quizId={quizId}
              question={question}
              index={index}
              count={list.length}
              required={required}
              onMove={(delta) => {
                reorder.mutate(moved(list, index, delta).map((item) => item.id ?? ''))
              }}
            />
          ))}
          <Button
            variant="outline"
            className="justify-self-start"
            onClick={() => {
              setAdding(true)
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('authoring.addQuestion')}
          </Button>
          {adding && (
            <NewQuestionForm
              quizId={quizId}
              onDone={() => {
                setAdding(false)
              }}
            />
          )}
        </section>
      </div>
    </div>
  )
}

// ------------------------------------------------------------------ the quiz

function quizTextsOf(quiz: QuizRaw, locale: Locale): QuizTexts | undefined {
  const row = quiz.translations?.find((item) => item.locale === locale)
  return row === undefined
    ? undefined
    : { title: row.title ?? '', description: row.description ?? '', instructions: row.instructions ?? '' }
}

function QuizTextsCard({
  quiz,
  required,
  onReload,
}: {
  quiz: QuizRaw
  required: readonly Locale[]
  onReload: () => void
}) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const save = useSaveQuizText(quiz.id ?? '')
  const autosave = useAutosave((value: { locale: Locale; texts: QuizTexts }) => save.mutateAsync(value))
  return (
    <Card>
      <CardHeader
        title={t('authoring.quizTexts')}
        action={<SaveStatus state={autosave.state} onReload={onReload} />}
      />
      <CardContent className="grid grid-cols-1 gap-4">
        <LocaleTabs
          value={locale}
          onValueChange={(next) => {
            autosave.flush()
            setLocale(next)
          }}
          has={(l) => quizTextsOf(quiz, l) !== undefined}
          required={required}
        />
        <QuizTextFields
          key={locale}
          initial={quizTextsOf(quiz, locale) ?? { title: '', description: '', instructions: '' }}
          onChange={(texts) => {
            if (texts.title.trim() !== '') autosave.change({ locale, texts })
          }}
        />
      </CardContent>
    </Card>
  )
}

function QuizTextFields({ initial, onChange }: { initial: QuizTexts; onChange: (texts: QuizTexts) => void }) {
  const { t } = useTranslation()
  const [texts, setTexts] = useState(initial)
  const set = (key: keyof QuizTexts, value: string) => {
    const next = { ...texts, [key]: value }
    setTexts(next)
    onChange(next)
  }
  return (
    <div className="grid grid-cols-1 gap-3">
      <Input
        label={t('authoring.quizTitle')}
        value={texts.title}
        onChange={(event) => {
          set('title', event.target.value)
        }}
      />
      <Textarea
        label={t('authoring.quizDescription')}
        value={texts.description}
        rows={3}
        onChange={(event) => {
          set('description', event.target.value)
        }}
      />
      <Textarea
        label={t('authoring.quizInstructions')}
        value={texts.instructions}
        rows={3}
        onChange={(event) => {
          set('instructions', event.target.value)
        }}
      />
    </div>
  )
}

function QuizSettingsCard({ quiz, onReload }: { quiz: QuizRaw; onReload: () => void }) {
  const { t } = useTranslation()
  const save = useSaveQuizSettings(quiz.id ?? '')
  const [settings, setSettings] = useState<QuizSettings>(() => ({
    ...(quiz.timeLimitSeconds !== undefined && { timeLimitSeconds: quiz.timeLimitSeconds }),
    ...(quiz.maxAttempts !== undefined && { maxAttempts: quiz.maxAttempts }),
    ...(quiz.passingPercent !== undefined && { passingPercent: quiz.passingPercent }),
    ...(quiz.maxPoints !== undefined && { maxPoints: quiz.maxPoints }),
    revealPolicy: quiz.revealPolicy ?? 'AFTER_ATTEMPT',
    shuffleQuestions: quiz.shuffleQuestions ?? false,
    shuffleOptions: quiz.shuffleOptions ?? false,
  }))
  const autosave = useAutosave((value: QuizSettings) => save.mutateAsync(value))
  const change = (patch: Partial<QuizSettings>) => {
    const next = { ...settings, ...patch }
    setSettings(next)
    autosave.change(next)
  }
  const whole = (value: string) => {
    const parsed = Number.parseInt(value, 10)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
  }
  return (
    <Card>
      <CardHeader
        title={t('authoring.quizSettings')}
        action={<SaveStatus state={autosave.state} onReload={onReload} />}
      />
      <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-1">
        <Input
          label={t('authoring.timeLimitMinutes')}
          type="number"
          min={1}
          placeholder={t('quiz.unlimited')}
          value={
            settings.timeLimitSeconds === undefined ? '' : String(Math.round(settings.timeLimitSeconds / 60))
          }
          onChange={(event) => {
            const minutes = whole(event.target.value)
            change({ timeLimitSeconds: minutes === undefined ? undefined : minutes * 60 })
          }}
        />
        <Input
          label={t('authoring.maxAttempts')}
          type="number"
          min={1}
          placeholder={t('quiz.unlimited')}
          value={settings.maxAttempts === undefined ? '' : String(settings.maxAttempts)}
          onChange={(event) => {
            change({ maxAttempts: whole(event.target.value) })
          }}
        />
        <Input
          label={t('authoring.passingPercent')}
          type="number"
          min={0}
          max={100}
          value={settings.passingPercent === undefined ? '' : String(settings.passingPercent)}
          onChange={(event) => {
            const value = Number(event.target.value)
            change({
              passingPercent: event.target.value === '' || !Number.isFinite(value) ? undefined : value,
            })
          }}
        />
        <Select
          label={t('authoring.revealPolicy')}
          value={settings.revealPolicy ?? 'AFTER_ATTEMPT'}
          onValueChange={(value) => {
            const revealPolicy = REVEAL.find((item) => item === value)
            if (revealPolicy !== undefined) change({ revealPolicy })
          }}
          options={REVEAL.map((value) => ({ value, label: t(`authoring.reveal.${value}`) }))}
        />
        <div className="grid grid-cols-1 gap-3">
          <Switch
            label={t('authoring.shuffleQuestions')}
            checked={settings.shuffleQuestions === true}
            onCheckedChange={(checked) => {
              change({ shuffleQuestions: checked })
            }}
          />
          <Switch
            label={t('authoring.shuffleOptions')}
            checked={settings.shuffleOptions === true}
            onCheckedChange={(checked) => {
              change({ shuffleOptions: checked })
            }}
          />
        </div>
      </CardContent>
    </Card>
  )
}

// ------------------------------------------------------------------ questions

function NewQuestionForm({ quizId, onDone }: { quizId: string; onDone: () => void }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const add = useAddQuestion(quizId)
  const addOption = useAddOption(quizId)
  const [type, setType] = useState<QuestionType>('SINGLE_CHOICE')
  const [prompt, setPrompt] = useState('')
  return (
    <Card>
      <CardHeader title={t('authoring.newQuestion')} />
      <CardContent>
        <form
          className="grid grid-cols-1 gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            if (prompt.trim() === '') return
            add.mutate(
              { type, prompt: prompt.trim(), locale },
              {
                onSuccess: (question) => {
                  // true / false always has exactly these two options; "false" is right until changed
                  if (type !== 'TRUE_FALSE' || question.id === undefined) {
                    onDone()
                    return
                  }
                  const questionId = question.id
                  void addOption
                    .mutateAsync({ questionId, isCorrect: false, text: t('authoring.true'), locale })
                    .then(() =>
                      addOption.mutateAsync({
                        questionId,
                        isCorrect: true,
                        text: t('authoring.false'),
                        locale,
                      }),
                    )
                    .then(onDone)
                },
              },
            )
          }}
        >
          <Select
            label={t('authoring.questionType')}
            value={type}
            onValueChange={(value) => {
              const next = QUESTION_TYPES.find((item) => item === value)
              if (next !== undefined) setType(next)
            }}
            options={QUESTION_TYPES.map((value) => ({ value, label: t(`authoring.questionTypes.${value}`) }))}
          />
          <Textarea
            label={t('authoring.prompt')}
            value={prompt}
            rows={3}
            onChange={(event) => {
              setPrompt(event.target.value)
            }}
          />
          {add.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(add.error).code}`)}
            </p>
          )}
          <div className="flex gap-2">
            <Button
              type="submit"
              disabled={prompt.trim() === ''}
              loading={add.isPending || addOption.isPending}
            >
              {t('authoring.add')}
            </Button>
            <Button variant="ghost" onClick={onDone}>
              {t('common.cancel')}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}

function questionTextsOf(question: QuestionRaw, locale: Locale): QuestionTexts | undefined {
  const row = question.translations?.find((item) => item.locale === locale)
  return row === undefined
    ? undefined
    : { prompt: row.prompt ?? '', hint: row.hint ?? '', explanation: row.explanation ?? '' }
}

type QuestionCardProps = {
  quizId: string
  question: QuestionRaw
  index: number
  count: number
  required: readonly Locale[]
  onMove: (delta: -1 | 1) => void
}

function QuestionCard({ quizId, question, index, count, required, onMove }: QuestionCardProps) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const [confirming, setConfirming] = useState(false)
  const saveText = useSaveQuestionText(quizId)
  const saveFields = useSaveQuestionFields(quizId)
  const remove = useDeleteQuestion(quizId)
  const autosave = useAutosave((value: { locale: Locale; texts: QuestionTexts }) =>
    saveText.mutateAsync({ id: question.id ?? '', ...value }),
  )
  const [points, setPoints] = useState(String(question.points ?? 1))
  const type = question.questionType ?? 'SINGLE_CHOICE'
  const label = t('authoring.questionNumber', { number: index + 1 })

  return (
    <article
      aria-label={label}
      className="border-border bg-surface-raised grid grid-cols-1 gap-4 rounded-lg border p-4"
    >
      <header className="flex flex-wrap items-center gap-2">
        <h3 className="text-body text-text font-semibold">{label}</h3>
        <Badge tone="info">{t(`authoring.questionTypes.${type}`)}</Badge>
        <span className="ml-auto flex items-center gap-1">
          <SaveStatus state={autosave.state} />
          <IconButton
            size="sm"
            aria-label={t('authoring.moveUp')}
            disabled={index === 0}
            onClick={() => {
              onMove(-1)
            }}
          >
            <ArrowUp aria-hidden className="size-4" />
          </IconButton>
          <IconButton
            size="sm"
            aria-label={t('authoring.moveDown')}
            disabled={index === count - 1}
            onClick={() => {
              onMove(1)
            }}
          >
            <ArrowDown aria-hidden className="size-4" />
          </IconButton>
          <IconButton
            size="sm"
            aria-label={t('authoring.deleteQuestion', { number: index + 1 })}
            onClick={() => {
              setConfirming(true)
            }}
          >
            <Trash2 aria-hidden className="size-4" />
          </IconButton>
        </span>
      </header>
      <div className="flex flex-wrap items-end justify-between gap-3">
        <LocaleTabs
          value={locale}
          onValueChange={(next) => {
            autosave.flush()
            setLocale(next)
          }}
          has={(l) => questionTextsOf(question, l) !== undefined}
          required={required}
        />
        <Input
          className="w-24"
          label={t('authoring.points')}
          type="number"
          min={0}
          step={0.5}
          value={points}
          onChange={(event) => {
            setPoints(event.target.value)
          }}
          onBlur={() => {
            const value = Number(points)
            if (Number.isFinite(value) && value !== question.points)
              saveFields.mutate({ id: question.id ?? '', points: value })
          }}
        />
      </div>
      <QuestionTextFields
        key={locale}
        initial={questionTextsOf(question, locale) ?? { prompt: '', hint: '', explanation: '' }}
        onChange={(texts) => {
          if (texts.prompt.trim() !== '') autosave.change({ locale, texts })
        }}
      />
      <AnswerEditor quizId={quizId} question={question} type={type} locale={locale} />
      <ConfirmDialog
        open={confirming}
        onOpenChange={setConfirming}
        title={t('authoring.deleteQuestionTitle')}
        body={t('authoring.deleteQuestionBody')}
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending}
        {...(remove.isError && { error: t(`errors.${toApiProblem(remove.error).code}`) })}
        onConfirm={() => {
          remove.mutate(question.id ?? '', {
            onSuccess: () => {
              setConfirming(false)
            },
          })
        }}
      />
    </article>
  )
}

function QuestionTextFields({
  initial,
  onChange,
}: {
  initial: QuestionTexts
  onChange: (texts: QuestionTexts) => void
}) {
  const { t } = useTranslation()
  const [texts, setTexts] = useState(initial)
  const set = (key: keyof QuestionTexts, value: string) => {
    const next = { ...texts, [key]: value }
    setTexts(next)
    onChange(next)
  }
  return (
    <div className="grid grid-cols-1 gap-3">
      <Textarea
        label={t('authoring.prompt')}
        value={texts.prompt}
        rows={3}
        onChange={(event) => {
          set('prompt', event.target.value)
        }}
      />
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <Textarea
          label={t('authoring.hint')}
          value={texts.hint}
          rows={2}
          onChange={(event) => {
            set('hint', event.target.value)
          }}
        />
        <Textarea
          label={t('authoring.questionExplanation')}
          value={texts.explanation}
          rows={2}
          onChange={(event) => {
            set('explanation', event.target.value)
          }}
        />
      </div>
    </div>
  )
}

// ------------------------------------------------------------------ answers

type OptionRaw = NonNullable<QuestionRaw['options']>[number]

function optionText(option: OptionRaw, locale: Locale): string {
  return option.translations?.find((item) => item.locale === locale)?.text ?? ''
}

function AnswerEditor({
  quizId,
  question,
  type,
  locale,
}: {
  quizId: string
  question: QuestionRaw
  type: QuestionType
  locale: Locale
}) {
  switch (type) {
    case 'SHORT_ANSWER':
    case 'NUMERIC':
      return <AnswerKeys quizId={quizId} question={question} numeric={type === 'NUMERIC'} />
    case 'MATCHING':
      return <MatchingPairs quizId={quizId} question={question} locale={locale} />
    default:
      return <OptionList quizId={quizId} question={question} type={type} locale={locale} />
  }
}

function OptionList({
  quizId,
  question,
  type,
  locale,
}: {
  quizId: string
  question: QuestionRaw
  type: QuestionType
  locale: Locale
}) {
  const { t } = useTranslation()
  const add = useAddOption(quizId)
  const saveFields = useSaveOptionFields(quizId)
  const reorder = useReorderOptions(quizId)
  const remove = useDeleteOption(quizId)
  const options = (question.options ?? []).slice().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
  const ordering = type === 'ORDERING'
  const single = type === 'SINGLE_CHOICE' || type === 'TRUE_FALSE'
  const questionId = question.id ?? ''

  return (
    <fieldset className="grid grid-cols-1 gap-2">
      <legend className="text-small text-text mb-1 font-medium">
        {ordering
          ? t('authoring.orderHint')
          : single
            ? t('authoring.singleHint')
            : t('authoring.multipleHint')}
      </legend>
      {options.map((option, index) => (
        <div key={option.id} className="flex items-center gap-2">
          {ordering ? (
            <span className="text-small text-text-muted w-6 text-right tabular-nums">{index + 1}.</span>
          ) : (
            <Checkbox
              label={<span className="sr-only">{t('authoring.correct')}</span>}
              checked={option.isCorrect === true}
              onCheckedChange={(checked) => {
                if (single && checked) {
                  // one right answer: marking this one unmarks the others
                  for (const other of options) {
                    if (other.id !== option.id && other.isCorrect === true) {
                      saveFields.mutate({ optionId: other.id ?? '', isCorrect: false })
                    }
                  }
                }
                saveFields.mutate({ optionId: option.id ?? '', isCorrect: checked })
              }}
            />
          )}
          <OptionText
            key={`${option.id}-${locale}`}
            option={option}
            locale={locale}
            label={t('authoring.optionNumber', { number: index + 1 })}
          />
          {ordering && (
            <>
              <IconButton
                size="sm"
                aria-label={t('authoring.moveUp')}
                disabled={index === 0}
                onClick={() => {
                  reorder.mutate({
                    questionId,
                    orderedOptionIds: moved(options, index, -1).map((item) => item.id ?? ''),
                  })
                }}
              >
                <ArrowUp aria-hidden className="size-4" />
              </IconButton>
              <IconButton
                size="sm"
                aria-label={t('authoring.moveDown')}
                disabled={index === options.length - 1}
                onClick={() => {
                  reorder.mutate({
                    questionId,
                    orderedOptionIds: moved(options, index, 1).map((item) => item.id ?? ''),
                  })
                }}
              >
                <ArrowDown aria-hidden className="size-4" />
              </IconButton>
            </>
          )}
          {type !== 'TRUE_FALSE' && (
            <IconButton
              size="sm"
              aria-label={t('authoring.deleteOption', { number: index + 1 })}
              onClick={() => {
                remove.mutate(option.id ?? '')
              }}
            >
              <Trash2 aria-hidden className="size-4" />
            </IconButton>
          )}
        </div>
      ))}
      {type !== 'TRUE_FALSE' && (
        <Button
          variant="ghost"
          size="sm"
          className="justify-self-start"
          loading={add.isPending}
          onClick={() => {
            add.mutate({ questionId, isCorrect: false, text: '', locale })
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('authoring.addOption')}
        </Button>
      )}
    </fieldset>
  )
}

function OptionText({ option, locale, label }: { option: OptionRaw; locale: Locale; label: string }) {
  const save = useSaveOptionText()
  const [text, setText] = useState(() => optionText(option, locale))
  const autosave = useAutosave((value: string) =>
    save.mutateAsync({ optionId: option.id ?? '', locale, text: value, feedback: '' }),
  )
  return (
    <div className="flex min-w-0 flex-1 items-center gap-2">
      <input
        aria-label={label}
        value={text}
        onChange={(event) => {
          setText(event.target.value)
          if (event.target.value.trim() !== '') autosave.change(event.target.value)
        }}
        className="border-border bg-surface-raised text-body text-text focus-visible:outline-focus-ring h-10 min-w-0 flex-1 rounded-md border px-3 focus-visible:outline-2 focus-visible:outline-offset-2"
      />
      {autosave.state.kind !== 'saved' && <SaveStatus state={autosave.state} />}
    </div>
  )
}

/** Matching: options sharing a key belong together — shown and added as pairs. */
function MatchingPairs({
  quizId,
  question,
  locale,
}: {
  quizId: string
  question: QuestionRaw
  locale: Locale
}) {
  const { t } = useTranslation()
  const add = useAddOption(quizId)
  const remove = useDeleteOption(quizId)
  const options = (question.options ?? []).slice().sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
  const keys = [...new Set(options.map((option) => option.matchKey ?? ''))].filter((key) => key !== '')
  const questionId = question.id ?? ''

  return (
    <fieldset className="grid grid-cols-1 gap-2">
      <legend className="text-small text-text mb-1 font-medium">{t('authoring.matchingHint')}</legend>
      {keys.map((key, index) => {
        const pair = options.filter((option) => option.matchKey === key)
        return (
          <div key={key} className="flex items-center gap-2">
            {pair.slice(0, 2).map((option, side) => (
              <OptionText
                key={`${option.id}-${locale}`}
                option={option}
                locale={locale}
                label={t(side === 0 ? 'authoring.pairLeft' : 'authoring.pairRight', { number: index + 1 })}
              />
            ))}
            {pair.length < 2 && <span />}
            <IconButton
              size="sm"
              aria-label={t('authoring.deletePair', { number: index + 1 })}
              onClick={() => {
                for (const option of pair) remove.mutate(option.id ?? '')
              }}
            >
              <Trash2 aria-hidden className="size-4" />
            </IconButton>
          </div>
        )
      })}
      <Button
        variant="ghost"
        size="sm"
        className="justify-self-start"
        loading={add.isPending}
        onClick={() => {
          const key = `p${String(Date.now()).slice(-6)}`
          void add
            .mutateAsync({ questionId, isCorrect: false, matchKey: key, text: '', locale })
            .then(() => add.mutateAsync({ questionId, isCorrect: false, matchKey: key, text: '', locale }))
        }}
      >
        <Plus aria-hidden className="size-4" />
        {t('authoring.addPair')}
      </Button>
    </fieldset>
  )
}

function AnswerKeys({
  quizId,
  question,
  numeric,
}: {
  quizId: string
  question: QuestionRaw
  numeric: boolean
}) {
  const { t } = useTranslation()
  const add = useAddAnswerKey(quizId)
  const remove = useDeleteAnswerKey(quizId)
  const [value, setValue] = useState('')
  const [mode, setMode] = useState<NonNullable<MatchMode>>('CASE_INSENSITIVE')
  const [tolerance, setTolerance] = useState('0')
  const keys = question.answerKeys ?? []
  const questionId = question.id ?? ''

  return (
    <fieldset className="grid grid-cols-1 gap-2">
      <legend className="text-small text-text mb-1 font-medium">
        {numeric ? t('authoring.numericHint') : t('authoring.shortAnswerHint')}
      </legend>
      <ul className="grid grid-cols-1 gap-1">
        {keys.map((key) => (
          <li key={key.id} className="border-border flex items-center gap-2 rounded-md border px-3 py-1.5">
            <code className="text-small text-text font-mono">{key.expectedValue}</code>
            <span className="text-caption text-text-muted">
              {key.matchMode === 'NUMERIC_TOLERANCE'
                ? t('authoring.toleranceValue', { tolerance: key.tolerance ?? 0 })
                : t(`authoring.matchMode.${key.matchMode ?? 'EXACT'}`)}
            </span>
            <IconButton
              size="sm"
              className="ml-auto"
              aria-label={t('authoring.deleteAnswer', { value: key.expectedValue ?? '' })}
              onClick={() => {
                remove.mutate(key.id ?? '')
              }}
            >
              <Trash2 aria-hidden className="size-4" />
            </IconButton>
          </li>
        ))}
      </ul>
      <form
        className="flex flex-wrap items-end gap-2"
        onSubmit={(event) => {
          event.preventDefault()
          if (value.trim() === '') return
          add.mutate(
            numeric
              ? {
                  questionId,
                  expectedValue: value.trim(),
                  matchMode: 'NUMERIC_TOLERANCE',
                  tolerance: Number(tolerance) || 0,
                }
              : { questionId, expectedValue: value.trim(), matchMode: mode },
            {
              onSuccess: () => {
                setValue('')
              },
            },
          )
        }}
      >
        <Input
          className="w-48"
          label={t('authoring.rightAnswer')}
          inputMode={numeric ? 'decimal' : 'text'}
          value={value}
          onChange={(event) => {
            setValue(event.target.value)
          }}
        />
        {numeric ? (
          <Input
            className="w-28"
            label={t('authoring.tolerance')}
            type="number"
            min={0}
            step="any"
            value={tolerance}
            onChange={(event) => {
              setTolerance(event.target.value)
            }}
          />
        ) : (
          <Select
            className="w-52"
            label={t('authoring.comparison')}
            value={mode}
            onValueChange={(next) => {
              const found = MATCH_MODES.find((item) => item === next)
              if (found !== undefined) setMode(found)
            }}
            options={MATCH_MODES.map((item) => ({ value: item, label: t(`authoring.matchMode.${item}`) }))}
          />
        )}
        <Button type="submit" variant="outline" disabled={value.trim() === ''} loading={add.isPending}>
          <Plus aria-hidden className="size-4" />
          {t('authoring.addAnswer')}
        </Button>
      </form>
      {add.isError && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(add.error).code}`)}
        </p>
      )}
    </fieldset>
  )
}
