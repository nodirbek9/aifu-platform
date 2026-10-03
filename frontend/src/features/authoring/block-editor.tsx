import { lazy, Suspense, useState, type DragEvent } from 'react'
import { ArrowDown, ArrowUp, GripVertical, Plus, Trash2, Upload, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { EDITOR_LANGUAGES, isEditorLanguage, languageName } from '@/shared/editor/languages'
import type { Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { readString } from '@/shared/lib/translations'
import { cn } from '@/shared/lib/cn'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { SaveStatus } from './save-status'
import { useAutosave } from './use-autosave'
import { ConfirmDialog } from './confirm-dialog'
import { HtmlField } from './html-field'
import { LocaleTabs } from './locale-tabs'
import {
  acceptFor,
  isFileBlock,
  isRefBlock,
  missingLocales,
  readTimeline,
  type BlockType,
  type BlockVisibility,
  type EditorBlock,
  type JsonObject,
  type TimelineStep,
  videoIdFrom,
} from './model'
import { useDeleteBlock, useSaveBlockFields, useSaveBlockPayload, useUploadFile } from './queries'

const CodeEditor = lazy(async () => ({ default: (await import('@/shared/editor/code-editor')).CodeEditor }))

export type RefChoice = { value: string; label: string }

type BlockEditorProps = {
  lessonId: string
  block: EditorBlock
  index: number
  count: number
  required: readonly Locale[]
  /** Quizzes, coding tasks and activities of the course, for a reference block to point at. */
  refChoices: Partial<Record<BlockType, RefChoice[]>>
  revision: number
  onMove: (delta: -1 | 1) => void
  onReload: () => void
  dragging: boolean
  onDragStart: (event: DragEvent) => void
  onDragEnd: () => void
}

const VISIBILITIES: readonly BlockVisibility[] = ['ALL', 'STUDENT_ONLY', 'TEACHER_ONLY']

/** One block of the lesson being edited: its settings, its text in each language, its place. */
export function BlockEditor({
  lessonId,
  block,
  index,
  count,
  required,
  refChoices,
  revision,
  onMove,
  onReload,
  dragging,
  onDragStart,
  onDragEnd,
}: BlockEditorProps) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  const [deleting, setDeleting] = useState(false)
  const remove = useDeleteBlock(lessonId)
  const savePayload = useSaveBlockPayload(lessonId)
  // the language travels with the text: a save still waiting when the tab changes keeps its own
  const payloadSave = useAutosave((value: { locale: Locale; payload: JsonObject }) =>
    savePayload.mutateAsync({ id: block.id, ...value }),
  )
  const translatable = !isRefBlock(block.type)
  const missing = translatable ? missingLocales(block.payloads, required) : []
  const label = t(`authoring.blockType.${block.type}`)

  return (
    <article
      aria-label={t('authoring.blockLabel', { position: index + 1, type: label })}
      className={cn('border-border bg-surface-raised grid rounded-lg border', dragging && 'opacity-50')}
    >
      <header className="border-border flex flex-wrap items-center gap-2 border-b px-3 py-2">
        <button
          type="button"
          draggable
          onDragStart={onDragStart}
          onDragEnd={onDragEnd}
          onKeyDown={(event) => {
            if (!event.altKey) return
            const delta = event.key === 'ArrowUp' ? -1 : event.key === 'ArrowDown' ? 1 : null
            if (delta === null) return
            event.preventDefault()
            if (index + delta >= 0 && index + delta < count) onMove(delta)
          }}
          aria-label={t('authoring.dragBlock', { position: index + 1 })}
          aria-describedby="reorder-hint"
          className="text-text-muted hover:text-text focus-visible:outline-focus-ring grid size-8 cursor-grab place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
        >
          <GripVertical aria-hidden className="size-4" />
        </button>
        <span className="text-small text-text font-semibold">
          {index + 1}. {label}
        </span>
        {block.visibility !== 'ALL' && (
          <Badge tone="info">{t(`authoring.visibility.${block.visibility}`)}</Badge>
        )}
        {missing.length > 0 && (
          <Badge tone="warning">
            {t('authoring.missing', { locales: missing.join(', ').toUpperCase() })}
          </Badge>
        )}
        <span className="ml-auto flex items-center gap-1">
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
            aria-label={t('authoring.deleteBlock', { position: index + 1 })}
            onClick={() => {
              setDeleting(true)
            }}
          >
            <Trash2 aria-hidden className="size-4" />
          </IconButton>
        </span>
      </header>

      <div className="grid gap-4 p-3 sm:p-4">
        <BlockSettings
          key={`settings-${revision}`}
          lessonId={lessonId}
          block={block}
          refChoices={refChoices[block.type] ?? []}
          onReload={onReload}
        />
        {translatable && (
          <div className="grid gap-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <LocaleTabs
                value={locale}
                onValueChange={(next) => {
                  payloadSave.flush()
                  setLocale(next)
                }}
                has={(l) => block.payloads[l] !== undefined}
                required={required}
              />
              <SaveStatus state={payloadSave.state} onReload={onReload} />
            </div>
            <PayloadForm
              key={`${locale}-${revision}`}
              type={block.type}
              settings={block.settings}
              initial={block.payloads[locale] ?? {}}
              onChange={(payload) => {
                payloadSave.change({ locale, payload })
              }}
            />
          </div>
        )}
      </div>

      <ConfirmDialog
        open={deleting}
        onOpenChange={setDeleting}
        title={t('authoring.deleteBlockTitle')}
        body={t('authoring.deleteBlockBody', { type: label })}
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending}
        {...(remove.isError && { error: t(`errors.${toApiProblem(remove.error).code}`) })}
        onConfirm={() => {
          remove.mutate(block.id, {
            onSuccess: () => {
              setDeleting(false)
            },
          })
        }}
      />
    </article>
  )
}

// ------------------------------------------------------------------ settings

function BlockSettings({
  lessonId,
  block,
  refChoices,
  onReload,
}: {
  lessonId: string
  block: EditorBlock
  refChoices: RefChoice[]
  onReload: () => void
}) {
  const { t } = useTranslation()
  const save = useSaveBlockFields(lessonId)
  const [settings, setSettings] = useState(block.settings)
  const [visibility, setVisibility] = useState(block.visibility)
  const autosave = useAutosave((next: { settings: JsonObject; visibility: BlockVisibility }) =>
    save.mutateAsync({ id: block.id, fields: next }),
  )
  const set = (key: string, value: string) => {
    const next = { ...settings, [key]: value }
    setSettings(next)
    autosave.change({ settings: next, visibility })
  }

  const fields = (() => {
    switch (block.type) {
      case 'CALLOUT':
        return (
          <Select
            label={t('authoring.calloutVariant')}
            value={readString(settings, 'variant') ?? 'INFO'}
            onValueChange={(value) => {
              set('variant', value)
            }}
            options={['INFO', 'TIP', 'WARNING', 'DANGER'].map((value) => ({
              value,
              label: t(`authoring.callout.${value}`),
            }))}
          />
        )
      case 'CODE_EXAMPLE':
        return (
          <Select
            label={t('authoring.codeLanguage')}
            value={readString(settings, 'language') ?? 'python'}
            onValueChange={(value) => {
              set('language', value)
            }}
            options={EDITOR_LANGUAGES.map((value) => ({ value, label: languageName(value) }))}
          />
        )
      case 'LINK':
        return (
          <Input
            label={t('authoring.url')}
            type="url"
            inputMode="url"
            value={readString(settings, 'url') ?? ''}
            onChange={(event) => {
              set('url', event.target.value.trim())
            }}
          />
        )
      case 'EMBED':
        return (
          <div className="grid gap-3 sm:grid-cols-2">
            <Select
              label={t('authoring.provider')}
              value={readString(settings, 'provider') ?? 'YOUTUBE'}
              onValueChange={(value) => {
                set('provider', value)
              }}
              options={[
                { value: 'YOUTUBE', label: 'YouTube' },
                { value: 'VIMEO', label: 'Vimeo' },
              ]}
            />
            <Input
              label={t('authoring.videoId')}
              value={readString(settings, 'externalId') ?? ''}
              onChange={(event) => {
                set('externalId', videoIdFrom(event.target.value))
              }}
            />
          </div>
        )
      case 'QUIZ_REF':
      case 'CODING_TASK_REF':
      case 'ACTIVITY_REF':
        return (
          <Select
            label={t(`authoring.refLabel.${block.type}`)}
            value={readString(settings, 'refId')}
            placeholder={t('authoring.choose')}
            onValueChange={(value) => {
              set('refId', value)
            }}
            options={refChoices}
          />
        )
      default:
        return null
    }
  })()

  return (
    <div className="grid gap-3">
      <div className="flex flex-wrap items-end gap-3">
        {block.type !== 'LESSON_PLAN' && (
          <Select
            className="w-56"
            label={t('authoring.whoSees')}
            value={visibility}
            onValueChange={(value) => {
              const next = VISIBILITIES.find((item) => item === value)
              if (next === undefined) return
              setVisibility(next)
              autosave.change({ settings, visibility: next })
            }}
            options={VISIBILITIES.map((value) => ({ value, label: t(`authoring.visibility.${value}`) }))}
          />
        )}
        {autosave.state.kind !== 'saved' && <SaveStatus state={autosave.state} onReload={onReload} />}
      </div>
      {fields}
      {isFileBlock(block.type) && <FileField lessonId={lessonId} block={block} />}
    </div>
  )
}

function FileField({ lessonId, block }: { lessonId: string; block: EditorBlock }) {
  const { t } = useTranslation()
  const upload = useUploadFile()
  const save = useSaveBlockFields(lessonId)
  const [name, setName] = useState<string>()
  const failure = upload.error ?? save.error
  return (
    <div className="grid gap-1.5">
      <span className="text-small text-text font-medium">{t('authoring.file')}</span>
      <div className="flex flex-wrap items-center gap-3">
        <label className="border-border-strong bg-surface-raised text-small text-text hover:bg-surface-sunken focus-within:outline-focus-ring inline-flex h-9 cursor-pointer items-center gap-2 rounded-md border px-3 font-medium focus-within:outline-2">
          <Upload aria-hidden className="size-4" />
          {t('authoring.replaceFile')}
          <input
            type="file"
            className="sr-only"
            accept={acceptFor(block.type)}
            onChange={(event) => {
              const file = event.target.files?.[0]
              if (file === undefined) return
              upload.mutate(file, {
                onSuccess: (uploaded) => {
                  setName(uploaded.name)
                  save.mutate({ id: block.id, fields: { fileId: uploaded.id } })
                },
              })
            }}
          />
        </label>
        <span className="text-small text-text-muted">
          {upload.isPending || save.isPending
            ? t('authoring.uploading')
            : (name ?? t('authoring.fileAttached'))}
        </span>
      </div>
      {failure !== null && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(failure).code}`)}
        </p>
      )}
    </div>
  )
}

// ------------------------------------------------------------------ the text of one language

type PayloadFormProps = {
  type: BlockType
  settings: JsonObject
  initial: JsonObject
  onChange: (payload: JsonObject) => void
}

function PayloadForm({ type, settings, initial, onChange }: PayloadFormProps) {
  const { t } = useTranslation()
  const [payload, setPayload] = useState<JsonObject>(initial)
  const update = (patch: JsonObject) => {
    const next = { ...payload, ...patch }
    setPayload(next)
    onChange(withoutEmpty(next))
  }
  const text = (key: string) => (typeof payload[key] === 'string' ? payload[key] : '')

  switch (type) {
    case 'TEXT':
    case 'TABLE':
    case 'CASE_STUDY':
    case 'CALLOUT':
      return (
        <HtmlField
          label={t('authoring.content')}
          value={text('html')}
          rows={type === 'CALLOUT' ? 4 : 10}
          onChange={(html) => {
            update({ html })
          }}
        />
      )
    case 'CODE_EXAMPLE': {
      const language = readString(settings, 'language') ?? 'python'
      return (
        <div className="grid gap-3">
          <div className="grid gap-1.5">
            <span className="text-small text-text font-medium">{t('authoring.code')}</span>
            <Suspense fallback={<Skeleton className="h-48" />}>
              <CodeEditor
                aria-label={t('authoring.code')}
                value={text('code')}
                language={isEditorLanguage(language) ? language : 'python'}
                onChange={(code) => {
                  update({ code })
                }}
                className="border-border h-56 rounded-md border"
              />
            </Suspense>
          </div>
          <HtmlField
            label={t('authoring.explanation')}
            value={text('explanation')}
            rows={4}
            onChange={(explanation) => {
              update({ explanation })
            }}
          />
        </div>
      )
    }
    case 'IMAGE':
    case 'DIAGRAM':
    case 'VIDEO':
    case 'AUDIO':
    case 'FILE':
    case 'EMBED':
      return (
        <Input
          label={t('authoring.caption')}
          value={text('caption')}
          onChange={(event) => {
            update({ caption: event.target.value })
          }}
        />
      )
    case 'LINK':
      return (
        <div className="grid gap-3">
          <Input
            label={t('authoring.linkLabel')}
            value={text('label')}
            onChange={(event) => {
              update({ label: event.target.value })
            }}
          />
          <Input
            label={t('authoring.linkNote')}
            value={text('note')}
            onChange={(event) => {
              update({ note: event.target.value })
            }}
          />
        </div>
      )
    case 'LESSON_PLAN':
      return <LessonPlanForm payload={payload} onChange={update} />
    default:
      return null
  }
}

/** Empty strings are left out: the server treats a missing part as "not written yet". */
function withoutEmpty(payload: JsonObject): JsonObject {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== ''))
}

function LessonPlanForm({
  payload,
  onChange,
}: {
  payload: JsonObject
  onChange: (patch: JsonObject) => void
}) {
  const { t } = useTranslation()
  const [steps, setSteps] = useState<TimelineStep[]>(() => readTimeline(payload))
  const text = (key: string) => (typeof payload[key] === 'string' ? payload[key] : '')
  const setTimeline = (next: TimelineStep[]) => {
    setSteps(next)
    onChange({ timeline: next.filter((step) => step.title.trim() !== '' && step.minutes > 0) })
  }
  const total = steps.reduce((sum, step) => sum + step.minutes, 0)

  return (
    <div className="grid gap-4">
      <HtmlField
        label={t('teaching.planObjectives')}
        value={text('objectives')}
        rows={4}
        onChange={(objectives) => {
          onChange({ objectives })
        }}
      />
      <fieldset className="grid gap-2">
        <legend className="text-small text-text mb-1 font-medium">
          {t('teaching.planTimeline')} · {t('authoring.minutesTotal', { count: total })}
        </legend>
        {steps.map((step, index) => (
          <div key={index} className="grid grid-cols-[5rem_1fr_auto] items-end gap-2">
            <Input
              label={t('authoring.minutes')}
              type="number"
              min={1}
              value={step.minutes === 0 ? '' : String(step.minutes)}
              onChange={(event) => {
                setTimeline(
                  steps.map((item, i) =>
                    i === index ? { ...item, minutes: Number(event.target.value) || 0 } : item,
                  ),
                )
              }}
            />
            <Input
              label={t('authoring.stepTitle')}
              value={step.title}
              onChange={(event) => {
                setTimeline(
                  steps.map((item, i) => (i === index ? { ...item, title: event.target.value } : item)),
                )
              }}
            />
            <IconButton
              aria-label={t('authoring.removeStep', { position: index + 1 })}
              onClick={() => {
                setTimeline(steps.filter((_, i) => i !== index))
              }}
            >
              <X aria-hidden className="size-4" />
            </IconButton>
          </div>
        ))}
        <Button
          variant="ghost"
          size="sm"
          className="justify-self-start"
          onClick={() => {
            setTimeline([...steps, { minutes: 10, title: '' }])
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('authoring.addStep')}
        </Button>
      </fieldset>
      <HtmlField
        label={t('teaching.planMaterials')}
        value={text('materials')}
        rows={3}
        onChange={(materials) => {
          onChange({ materials })
        }}
      />
      <HtmlField
        label={t('teaching.planQuestions')}
        value={text('questions')}
        rows={3}
        onChange={(questions) => {
          onChange({ questions })
        }}
      />
      <Textarea
        label={t('teaching.planHomework')}
        value={text('homework')}
        rows={3}
        onChange={(event) => {
          onChange({ homework: event.target.value })
        }}
      />
    </div>
  )
}
