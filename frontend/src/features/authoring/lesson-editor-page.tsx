import { useMemo, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Eye, EyeOff, Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { LessonBlocks } from '@/features/lesson/lesson-blocks'
import { narrowLesson } from '@/features/lesson/model'
import { useTeacherActivitiesQuery, useTeacherCoursesQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import { SUPPORTED_LOCALES, isLocale, type Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { Input } from '@/shared/ui/input'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { SESSION_TYPES, type SessionType } from '@/shared/ui/badge'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { AddBlockDialog } from './add-block-dialog'
import { SaveStatus } from './save-status'
import { useAutosave } from './use-autosave'
import { BlockEditor, type RefChoice } from './block-editor'
import { StatusBadge } from './curriculum-tree'
import { LocaleTabs } from './locale-tabs'
import { publishError } from './publish-error'
import { movedTo, previewLesson, textIn, titleOf, type BlockType, type EditorLesson } from './model'
import {
  authoringKeys,
  useAuthoringCourseQuery,
  useEditorLessonQuery,
  useLessonStatusInEditor,
  useModuleQuery,
  useReorderBlocks,
  useSaveLessonFields,
  useSaveLessonText,
  type LessonFields,
} from './queries'
import { useCourseQuizzesQuery } from './quiz-queries'
import { useCourseTasksQuery } from './task-queries'

/**
 * T-04.2 — the lesson editor: the lesson's titles and hours, its blocks of every kind with their
 * text in UZ / RU / EN, and beside them the lesson exactly as a student will read it.
 */
export function LessonEditorPage() {
  const { lessonId = '' } = useParams()
  const queryClient = useQueryClient()
  const lesson = useEditorLessonQuery(lessonId)
  const module = useModuleQuery(lesson.data?.moduleId)
  const courseId = module.data?.courseId
  const [revision, setRevision] = useState(0)

  const reload = () => {
    void queryClient.invalidateQueries({ queryKey: authoringKeys.lesson(lessonId) }).then(() => {
      setRevision((value) => value + 1)
    })
  }

  if (lesson.isPending) return <Skeleton className="h-[32rem]" />
  if (lesson.isError) {
    return (
      <ErrorState
        problem={toApiProblem(lesson.error)}
        onRetry={() => {
          void lesson.refetch()
        }}
      />
    )
  }
  if (courseId === undefined) return <Skeleton className="h-[32rem]" />
  return (
    <LessonEditor
      key={revision}
      lesson={lesson.data}
      courseId={courseId}
      revision={revision}
      onReload={reload}
    />
  )
}

type LessonEditorProps = { lesson: EditorLesson; courseId: string; revision: number; onReload: () => void }

function LessonEditor({ lesson, courseId, revision, onReload }: LessonEditorProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const courses = useTeacherCoursesQuery()
  const course = useAuthoringCourseQuery(courseId)
  const tasks = useCourseTasksQuery(courseId)
  const quizzes = useCourseQuizzesQuery(courseId)
  const activities = useTeacherActivitiesQuery(courseId, locale)
  const status = useLessonStatusInEditor(lesson.id)
  const reorder = useReorderBlocks(lesson.id)
  const [previewLocale, setPreviewLocale] = useState<Locale>(locale)
  const [adding, setAdding] = useState(false)
  const [dragged, setDragged] = useState<string>()
  const [showPreview, setShowPreview] = useState(false)
  const [announcement, setAnnouncement] = useState('')
  const required = course.data?.requiredLocales ?? ['uz']
  const courseTitle = courses.data?.find((item) => item.id === courseId)?.title ?? ''

  const refChoices = useMemo((): Partial<Record<BlockType, RefChoice[]>> => {
    const pick = (rows: readonly { locale?: string; title?: string }[] | undefined) =>
      (rows ?? []).find((row) => row.locale === locale)?.title ?? rows?.[0]?.title
    return {
      CODING_TASK_REF: (tasks.data ?? []).flatMap((task) =>
        task.id === undefined
          ? []
          : [{ value: task.id, label: pick(task.translations) ?? task.title ?? task.code ?? '' }],
      ),
      QUIZ_REF: (quizzes.data ?? []).flatMap((quiz) =>
        quiz.id === undefined
          ? []
          : [{ value: quiz.id, label: pick(quiz.translations) ?? quiz.title ?? quiz.code ?? '' }],
      ),
      ACTIVITY_REF: (activities.data ?? []).map((activity) => ({
        value: activity.id,
        label: activity.title,
      })),
    }
  }, [tasks.data, quizzes.data, activities.data, locale])

  const preview = narrowLesson(previewLesson(lesson, previewLocale), previewLocale)
  const title = titleOf(lesson, locale)
  const moveBlock = (from: number, to: number) => {
    const block = lesson.blocks[from]
    reorder.mutate(movedTo(lesson.blocks, from, to).map((item) => item.id))
    if (block !== undefined) {
      setAnnouncement(
        t('authoring.blockMovedTo', { type: t(`authoring.blockType.${block.type}`), position: to + 1 }),
      )
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[
          { label: t('nav.teacherClasses'), to: '/teacher/courses' },
          { label: courseTitle, to: `/teacher/courses/${courseId}?tab=content` },
          { label: title },
        ]}
      />
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid grid-cols-1 gap-1">
          <h1 className="font-display text-display-lg text-text font-semibold">{title}</h1>
          <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
            {lesson.code}
            <StatusBadge status={lesson.status} />
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="outline"
            className="lg:hidden"
            aria-pressed={showPreview}
            onClick={() => {
              setShowPreview(!showPreview)
            }}
          >
            <Eye aria-hidden className="size-4" />
            {showPreview ? t('authoring.backToEditing') : t('authoring.preview')}
          </Button>
          <Button
            variant={lesson.status === 'PUBLISHED' ? 'outline' : 'primary'}
            loading={status.isPending}
            onClick={() => {
              status.mutate(lesson.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED')
            }}
          >
            {lesson.status === 'PUBLISHED' ? (
              <EyeOff aria-hidden className="size-4" />
            ) : (
              <Eye aria-hidden className="size-4" />
            )}
            {lesson.status === 'PUBLISHED' ? t('authoring.unpublish') : t('authoring.publish')}
          </Button>
        </div>
      </header>
      {status.isError && (
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {publishError(toApiProblem(status.error), t)}
        </p>
      )}
      <p aria-live="polite" className="sr-only">
        {announcement}
      </p>
      <p id="reorder-hint" className="sr-only">
        {t('authoring.reorderHint')}
      </p>

      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
        <div className={showPreview ? 'hidden lg:col-span-7 lg:grid lg:gap-4' : 'grid gap-4 lg:col-span-7'}>
          <LessonDetailsCard lesson={lesson} required={required} onReload={onReload} />
          {lesson.blocks.length === 0 && (
            <EmptyState title={t('authoring.noBlocks')} body={t('authoring.noBlocksBody')} />
          )}
          {lesson.blocks.map((block, index) => (
            <div
              key={block.id}
              onDragOver={(event) => {
                if (dragged !== undefined) event.preventDefault()
              }}
              onDrop={(event) => {
                event.preventDefault()
                const from = lesson.blocks.findIndex((item) => item.id === dragged)
                setDragged(undefined)
                if (from >= 0) moveBlock(from, index)
              }}
            >
              <BlockEditor
                lessonId={lesson.id}
                block={block}
                index={index}
                count={lesson.blocks.length}
                required={required}
                refChoices={refChoices}
                revision={revision}
                onReload={onReload}
                onMove={(delta) => {
                  moveBlock(index, index + delta)
                }}
                dragging={dragged === block.id}
                onDragStart={(event) => {
                  event.dataTransfer.effectAllowed = 'move'
                  setDragged(block.id)
                }}
                onDragEnd={() => {
                  setDragged(undefined)
                }}
              />
            </div>
          ))}
          <Button
            variant="outline"
            className="justify-self-start"
            onClick={() => {
              setAdding(true)
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('authoring.addBlock')}
          </Button>
        </div>

        <aside
          aria-label={t('authoring.preview')}
          className={
            showPreview
              ? 'grid gap-4 lg:sticky lg:top-20 lg:col-span-5 lg:max-h-[calc(100dvh-6rem)] lg:overflow-y-auto'
              : 'hidden lg:sticky lg:top-20 lg:col-span-5 lg:grid lg:max-h-[calc(100dvh-6rem)] lg:gap-4 lg:overflow-y-auto'
          }
        >
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-display text-h3 text-text font-semibold">{t('authoring.preview')}</h2>
            <SegmentedControl
              size="sm"
              aria-label={t('authoring.previewLanguage')}
              value={previewLocale}
              onValueChange={(value) => {
                if (isLocale(value)) setPreviewLocale(value)
              }}
              options={SUPPORTED_LOCALES.map((l) => ({ value: l, label: l.toUpperCase() }))}
            />
          </div>
          <div className="border-border bg-surface grid grid-cols-1 gap-5 rounded-lg border p-5">
            <h3 className="font-display text-h2 text-text font-semibold">{preview.title}</h3>
            {preview.hasFallback && <FallbackMark />}
            {preview.blocks.length === 0 ? (
              <p className="text-small text-text-muted">{t('authoring.previewEmpty')}</p>
            ) : (
              <LessonBlocks blocks={preview.blocks} />
            )}
          </div>
        </aside>
      </div>

      <AddBlockDialog
        lessonId={lesson.id}
        open={adding}
        onOpenChange={setAdding}
        refChoices={refChoices}
        onAdded={() => {
          setAnnouncement(t('authoring.blockAdded'))
        }}
      />
    </div>
  )
}

function LessonDetailsCard({
  lesson,
  required,
  onReload,
}: {
  lesson: EditorLesson
  required: readonly Locale[]
  onReload: () => void
}) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const saveText = useSaveLessonText(lesson.id)
  const saveFields = useSaveLessonFields(lesson.id)
  const textSave = useAutosave((value: { locale: Locale; title: string; summary: string }) =>
    saveText.mutateAsync(value),
  )
  const fieldSave = useAutosave((fields: LessonFields) => saveFields.mutateAsync(fields))
  const [fields, setFields] = useState({
    sessionType: lesson.sessionType,
    hours: lesson.hours === undefined ? '' : String(lesson.hours),
    minutes: lesson.estimatedMinutes === undefined ? '' : String(lesson.estimatedMinutes),
  })
  const changeFields = (next: typeof fields) => {
    setFields(next)
    const hours = Number(next.hours)
    const minutes = Number(next.minutes)
    fieldSave.change({
      sessionType: next.sessionType,
      ...(next.hours !== '' && Number.isFinite(hours) && { hours }),
      ...(next.minutes !== '' && Number.isInteger(minutes) && { estimatedMinutes: minutes }),
    })
  }
  const failed = [textSave.state, fieldSave.state].find((state) => state.kind === 'failed')
  const busy = [textSave.state, fieldSave.state].find((state) => state.kind !== 'saved')

  return (
    <Card>
      <CardHeader
        title={t('authoring.lessonDetails')}
        action={<SaveStatus state={failed ?? busy ?? { kind: 'saved' }} onReload={onReload} />}
      />
      <CardContent className="grid grid-cols-1 gap-4">
        <LocaleTabs
          value={locale}
          onValueChange={(next) => {
            textSave.flush()
            setLocale(next)
          }}
          has={(l) => lesson.texts[l] !== undefined}
          required={required}
        />
        <LessonTextFields
          key={locale}
          initial={lesson.texts[locale] ?? { title: '', summary: '' }}
          placeholder={textIn(lesson.texts, locale)?.title ?? ''}
          onChange={(text) => {
            if (text.title.trim() !== '') textSave.change({ locale, ...text })
          }}
        />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Select
            label={t('authoring.sessionType')}
            value={fields.sessionType}
            onValueChange={(value) => {
              const type: SessionType | undefined = SESSION_TYPES.find((item) => item === value)
              if (type !== undefined) changeFields({ ...fields, sessionType: type })
            }}
            options={SESSION_TYPES.map((type) => ({ value: type, label: t(`sessionType.${type}`) }))}
          />
          <Input
            label={t('authoring.hoursLabel')}
            type="number"
            min={0}
            step={0.5}
            value={fields.hours}
            onChange={(event) => {
              changeFields({ ...fields, hours: event.target.value })
            }}
          />
          <Input
            label={t('authoring.readingMinutes')}
            type="number"
            min={1}
            value={fields.minutes}
            onChange={(event) => {
              changeFields({ ...fields, minutes: event.target.value })
            }}
          />
        </div>
      </CardContent>
    </Card>
  )
}

function LessonTextFields({
  initial,
  placeholder,
  onChange,
}: {
  initial: { title: string; summary?: string }
  placeholder: string
  onChange: (text: { title: string; summary: string }) => void
}) {
  const { t } = useTranslation()
  const [title, setTitle] = useState(initial.title)
  const [summary, setSummary] = useState(initial.summary ?? '')
  return (
    <div className="grid grid-cols-1 gap-4">
      <Input
        label={t('authoring.lessonTitle')}
        value={title}
        placeholder={placeholder}
        maxLength={300}
        onChange={(event) => {
          setTitle(event.target.value)
          onChange({ title: event.target.value, summary })
        }}
      />
      <Textarea
        label={t('authoring.summary')}
        value={summary}
        rows={3}
        onChange={(event) => {
          setSummary(event.target.value)
          onChange({ title, summary: event.target.value })
        }}
      />
    </div>
  )
}
