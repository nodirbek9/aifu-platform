import { useState, type DragEvent, type KeyboardEvent } from 'react'
import {
  ArrowDown,
  ArrowUp,
  Eye,
  EyeOff,
  GripVertical,
  MoreHorizontal,
  Pencil,
  Plus,
  Trash2,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { SUPPORTED_LOCALES, type Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { Badge, SessionTypeBadge, SESSION_TYPES, type SessionType } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { ConfirmDialog } from './confirm-dialog'
import { hourBudget, missingLocales, movedTo, titleOf, type PublicationStatus } from './model'
import {
  useAuthoringCourseQuery,
  useCreateLesson,
  useCreateModule,
  useCurriculumTreeQuery,
  useDeleteLesson,
  useDeleteModule,
  useLessonStatus,
  useModuleStatus,
  useRenameModule,
  useReorderLessons,
  useReorderModules,
  type CurriculumTree as Tree,
} from './queries'

type TreeModule = Tree['modules'][number]
type TreeLesson = TreeModule['lessons'][number]

const STATUS_TONE = { DRAFT: 'neutral', PUBLISHED: 'success', ARCHIVED: 'warning' } as const

export function StatusBadge({ status }: { status: PublicationStatus }) {
  const { t } = useTranslation()
  return <Badge tone={STATUS_TONE[status]}>{t(`authoring.status.${status}`)}</Badge>
}

/** Alt+↑ / Alt+↓ on a drag handle moves the row: the keyboard way to reorder. */
function reorderKey(event: KeyboardEvent): -1 | 1 | null {
  if (!event.altKey) return null
  if (event.key === 'ArrowUp') return -1
  if (event.key === 'ArrowDown') return 1
  return null
}

/**
 * T-04.1 — the curriculum of a course: modules and their lessons in order. Create, rename,
 * reorder (drag, or Alt+arrows on the handle), publish / unpublish and delete; the hours the
 * lessons carry set against the syllabus.
 */
export function CurriculumTree({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const course = useAuthoringCourseQuery(courseId)
  const tree = useCurriculumTreeQuery(courseId)
  const reorderModules = useReorderModules(courseId)
  const [creatingModule, setCreatingModule] = useState(false)
  const [announcement, setAnnouncement] = useState('')
  const [dragged, setDragged] = useState<string>()

  if (tree.isPending || course.isPending) return <Skeleton className="h-96" />
  if (tree.isError || course.isError) {
    return (
      <ErrorState
        problem={toApiProblem(tree.error ?? course.error)}
        onRetry={() => {
          void tree.refetch()
          void course.refetch()
        }}
      />
    )
  }

  const modules = tree.data.modules
  const budget = hourBudget(
    course.data,
    modules.flatMap((module) => module.lessons),
  )
  const moveModule = (index: number, to: number) => {
    const next = movedTo(modules, index, to)
    const module = modules[index]
    reorderModules.mutate(next.map((item) => item.id))
    if (module !== undefined) {
      setAnnouncement(t('authoring.movedTo', { title: titleOf(module, locale), position: to + 1 }))
    }
  }

  return (
    <div className="grid gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-small text-text-muted max-w-prose">{t('authoring.treeHint')}</p>
        <Button
          onClick={() => {
            setCreatingModule(true)
          }}
        >
          <Plus aria-hidden className="size-4" />
          {t('authoring.newModule')}
        </Button>
      </div>

      {budget.length > 0 && (
        <Callout variant="WARNING" title={t('authoring.budgetTitle')}>
          <ul className="grid gap-1">
            {budget.map((line) => (
              <li key={line.sessionType}>
                {t('authoring.budgetLine', {
                  type: t(`sessionType.${line.sessionType}`),
                  planned: line.planned,
                  inLessons: line.inLessons,
                })}
              </li>
            ))}
          </ul>
        </Callout>
      )}

      <p aria-live="polite" className="sr-only">
        {announcement}
      </p>
      <p id="reorder-hint" className="sr-only">
        {t('authoring.reorderHint')}
      </p>

      {modules.length === 0 && (
        <EmptyState title={t('authoring.noModules')} body={t('authoring.noModulesBody')} />
      )}

      <ol className="grid gap-4">
        {modules.map((module, index) => (
          <li
            key={module.id}
            onDragOver={(event) => {
              if (dragged?.startsWith('module:') === true) event.preventDefault()
            }}
            onDrop={(event) => {
              event.preventDefault()
              const from = modules.findIndex((item) => `module:${item.id}` === dragged)
              setDragged(undefined)
              if (from >= 0) moveModule(from, index)
            }}
            className={cn(dragged === `module:${module.id}` && 'opacity-50')}
          >
            <ModuleCard
              courseId={courseId}
              module={module}
              index={index}
              count={modules.length}
              required={course.data.requiredLocales}
              onMove={(delta) => {
                moveModule(index, index + delta)
              }}
              onDragStart={(event) => {
                event.dataTransfer.effectAllowed = 'move'
                setDragged(`module:${module.id}`)
              }}
              onDragEnd={() => {
                setDragged(undefined)
              }}
              onAnnounce={setAnnouncement}
            />
          </li>
        ))}
      </ol>

      <NewModuleDialog courseId={courseId} open={creatingModule} onOpenChange={setCreatingModule} />
    </div>
  )
}

type ModuleCardProps = {
  courseId: string
  module: TreeModule
  index: number
  count: number
  required: readonly Locale[]
  onMove: (delta: -1 | 1) => void
  onDragStart: (event: DragEvent) => void
  onDragEnd: () => void
  onAnnounce: (text: string) => void
}

function ModuleCard({
  courseId,
  module,
  index,
  count,
  required,
  onMove,
  onDragStart,
  onDragEnd,
  onAnnounce,
}: ModuleCardProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const status = useModuleStatus(courseId)
  const remove = useDeleteModule(courseId)
  const reorderLessons = useReorderLessons(courseId)
  const lessonStatus = useLessonStatus(courseId)
  const deleteLesson = useDeleteLesson(courseId)
  const [renaming, setRenaming] = useState(false)
  const [adding, setAdding] = useState(false)
  const [confirming, setConfirming] = useState<{ kind: 'module' } | { kind: 'lesson'; lesson: TreeLesson }>()
  const [draggedLesson, setDraggedLesson] = useState<string>()
  const [error, setError] = useState<string>()
  const title = titleOf(module, locale)
  const missing = missingLocales(module.texts, required)

  const moveLesson = (from: number, to: number) => {
    const lesson = module.lessons[from]
    reorderLessons.mutate({
      moduleId: module.id,
      orderedLessonIds: movedTo(module.lessons, from, to).map((item) => item.id),
    })
    if (lesson !== undefined)
      onAnnounce(t('authoring.movedTo', { title: titleOf(lesson, locale), position: to + 1 }))
  }

  const changeStatus = (next: PublicationStatus) => {
    setError(undefined)
    status.mutate(
      { id: module.id, status: next },
      {
        onError: (failure) => {
          setError(t(`errors.${toApiProblem(failure).code}`))
        },
      },
    )
  }

  return (
    <section
      aria-labelledby={`module-${module.id}`}
      className="border-border bg-surface-raised grid rounded-lg border"
    >
      <header className="border-border flex flex-wrap items-center gap-2 border-b px-3 py-3 sm:px-4">
        <button
          type="button"
          draggable
          onDragStart={onDragStart}
          onDragEnd={onDragEnd}
          onKeyDown={(event) => {
            const delta = reorderKey(event)
            if (delta === null) return
            event.preventDefault()
            if (index + delta >= 0 && index + delta < count) onMove(delta)
          }}
          aria-label={t('authoring.dragModule', { title })}
          aria-describedby="reorder-hint"
          className="text-text-muted hover:text-text focus-visible:outline-focus-ring grid size-8 cursor-grab place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
        >
          <GripVertical aria-hidden className="size-4" />
        </button>
        <div className="grid min-w-0 flex-1 gap-0.5">
          <h3 id={`module-${module.id}`} className="font-display text-h3 text-text truncate font-semibold">
            {title}
          </h3>
          <p className="text-caption text-text-muted tracking-normal">
            {[module.code, t('authoring.lessonCount', { count: module.lessons.length })]
              .filter(Boolean)
              .join(' · ')}
          </p>
        </div>
        {missing.length > 0 && (
          <Badge tone="warning">
            {t('authoring.missing', { locales: missing.join(', ').toUpperCase() })}
          </Badge>
        )}
        <StatusBadge status={module.status} />
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <IconButton aria-label={t('authoring.moduleActions', { title })}>
              <MoreHorizontal aria-hidden className="size-4" />
            </IconButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem
              onSelect={() => {
                setRenaming(true)
              }}
            >
              <Pencil aria-hidden className="size-4" />
              {t('authoring.rename')}
            </DropdownMenuItem>
            {module.status === 'PUBLISHED' ? (
              <DropdownMenuItem
                onSelect={() => {
                  changeStatus('DRAFT')
                }}
              >
                <EyeOff aria-hidden className="size-4" />
                {t('authoring.unpublish')}
              </DropdownMenuItem>
            ) : (
              <DropdownMenuItem
                onSelect={() => {
                  changeStatus('PUBLISHED')
                }}
              >
                <Eye aria-hidden className="size-4" />
                {t('authoring.publish')}
              </DropdownMenuItem>
            )}
            <DropdownMenuItem
              disabled={index === 0}
              onSelect={() => {
                onMove(-1)
              }}
            >
              <ArrowUp aria-hidden className="size-4" />
              {t('authoring.moveUp')}
            </DropdownMenuItem>
            <DropdownMenuItem
              disabled={index === count - 1}
              onSelect={() => {
                onMove(1)
              }}
            >
              <ArrowDown aria-hidden className="size-4" />
              {t('authoring.moveDown')}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              destructive
              onSelect={() => {
                setConfirming({ kind: 'module' })
              }}
            >
              <Trash2 aria-hidden className="size-4" />
              {t('authoring.delete')}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </header>

      {error !== undefined && (
        <p
          role="alert"
          className="bg-danger-soft text-small text-danger mx-4 mt-3 rounded-sm px-3 py-2 font-medium"
        >
          {error}
        </p>
      )}

      <ol className="divide-border grid divide-y">
        {module.lessons.map((lesson, lessonIndex) => (
          <li
            key={lesson.id}
            onDragOver={(event) => {
              if (draggedLesson !== undefined) event.preventDefault()
            }}
            onDrop={(event) => {
              event.preventDefault()
              event.stopPropagation()
              const from = module.lessons.findIndex((item) => item.id === draggedLesson)
              setDraggedLesson(undefined)
              if (from >= 0) moveLesson(from, lessonIndex)
            }}
            className={cn(
              'flex flex-wrap items-center gap-2 px-3 py-2.5 sm:px-4',
              draggedLesson === lesson.id && 'opacity-50',
            )}
          >
            <button
              type="button"
              draggable
              onDragStart={(event) => {
                event.stopPropagation()
                event.dataTransfer.effectAllowed = 'move'
                setDraggedLesson(lesson.id)
              }}
              onDragEnd={() => {
                setDraggedLesson(undefined)
              }}
              onKeyDown={(event) => {
                const delta = reorderKey(event)
                if (delta === null) return
                event.preventDefault()
                const to = lessonIndex + delta
                if (to >= 0 && to < module.lessons.length) moveLesson(lessonIndex, to)
              }}
              aria-label={t('authoring.dragLesson', { title: titleOf(lesson, locale) })}
              aria-describedby="reorder-hint"
              className="text-text-muted hover:text-text focus-visible:outline-focus-ring grid size-8 cursor-grab place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
            >
              <GripVertical aria-hidden className="size-4" />
            </button>
            <SessionTypeBadge type={lesson.sessionType} label={t(`sessionType.${lesson.sessionType}`)} />
            <Link
              to={`/teacher/lessons/${lesson.id}/edit`}
              className="text-body text-text hover:text-primary-hover min-w-0 flex-1 truncate font-medium underline-offset-4 hover:underline"
            >
              {titleOf(lesson, locale)}
            </Link>
            {lesson.hours !== undefined && (
              <span className="text-small text-text-muted tabular-nums">
                {t('authoring.hours', { count: lesson.hours })}
              </span>
            )}
            <StatusBadge status={lesson.status} />
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <IconButton aria-label={t('authoring.lessonActions', { title: titleOf(lesson, locale) })}>
                  <MoreHorizontal aria-hidden className="size-4" />
                </IconButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link to={`/teacher/lessons/${lesson.id}/edit`}>
                    <Pencil aria-hidden className="size-4" />
                    {t('authoring.edit')}
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem
                  onSelect={() => {
                    setError(undefined)
                    lessonStatus.mutate(
                      { id: lesson.id, status: lesson.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED' },
                      {
                        onError: (failure) => {
                          setError(t(`errors.${toApiProblem(failure).code}`))
                        },
                      },
                    )
                  }}
                >
                  {lesson.status === 'PUBLISHED' ? (
                    <EyeOff aria-hidden className="size-4" />
                  ) : (
                    <Eye aria-hidden className="size-4" />
                  )}
                  {lesson.status === 'PUBLISHED' ? t('authoring.unpublish') : t('authoring.publish')}
                </DropdownMenuItem>
                <DropdownMenuItem
                  disabled={lessonIndex === 0}
                  onSelect={() => {
                    moveLesson(lessonIndex, lessonIndex - 1)
                  }}
                >
                  <ArrowUp aria-hidden className="size-4" />
                  {t('authoring.moveUp')}
                </DropdownMenuItem>
                <DropdownMenuItem
                  disabled={lessonIndex === module.lessons.length - 1}
                  onSelect={() => {
                    moveLesson(lessonIndex, lessonIndex + 1)
                  }}
                >
                  <ArrowDown aria-hidden className="size-4" />
                  {t('authoring.moveDown')}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  destructive
                  onSelect={() => {
                    setConfirming({ kind: 'lesson', lesson })
                  }}
                >
                  <Trash2 aria-hidden className="size-4" />
                  {t('authoring.delete')}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </li>
        ))}
        <li className="px-3 py-2 sm:px-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setAdding(true)
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('authoring.newLesson')}
          </Button>
        </li>
      </ol>
      <RenameModuleDialog courseId={courseId} module={module} open={renaming} onOpenChange={setRenaming} />
      <NewLessonDialog courseId={courseId} moduleId={module.id} open={adding} onOpenChange={setAdding} />
      <ConfirmDialog
        open={confirming !== undefined}
        onOpenChange={(open) => {
          if (!open) setConfirming(undefined)
        }}
        title={
          confirming?.kind === 'lesson' ? t('authoring.deleteLessonTitle') : t('authoring.deleteModuleTitle')
        }
        body={
          confirming?.kind === 'lesson'
            ? t('authoring.deleteLessonBody', { title: titleOf(confirming.lesson, locale) })
            : t('authoring.deleteModuleBody', { title })
        }
        confirmLabel={t('authoring.delete')}
        pending={remove.isPending || deleteLesson.isPending}
        onConfirm={() => {
          const done = {
            onSuccess: () => {
              setConfirming(undefined)
            },
            onError: (failure: unknown) => {
              setConfirming(undefined)
              setError(t(`errors.${toApiProblem(failure).code}`))
            },
          }
          if (confirming?.kind === 'lesson') deleteLesson.mutate(confirming.lesson.id, done)
          else remove.mutate(module.id, done)
        }}
      />
    </section>
  )
}

/** A short code from a title: "Massivlar va satrlar" → "MASSIVLAR-VA-SATRLAR" (the server needs one). */
function codeFrom(title: string): string {
  return (
    title
      .normalize('NFKD')
      .replace(/[^A-Za-z0-9]+/g, '-')
      .replace(/^-|-$/g, '')
      .toUpperCase()
      .slice(0, 40) || 'NEW'
  )
}

function NewModuleDialog({
  courseId,
  open,
  onOpenChange,
}: {
  courseId: string
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const create = useCreateModule(courseId)
  const [title, setTitle] = useState('')
  const [error, setError] = useState<string>()

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogTitle>{t('authoring.newModule')}</DialogTitle>
        <DialogDescription>{t('authoring.newModuleBody')}</DialogDescription>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (title.trim() === '') return
            setError(undefined)
            create.mutate(
              {
                code: `M-${codeFrom(title)}-${Date.now().toString(36).slice(-4).toUpperCase()}`,
                title: title.trim(),
                locale,
              },
              {
                onSuccess: () => {
                  setTitle('')
                  onOpenChange(false)
                },
                onError: (failure) => {
                  setError(t(`errors.${toApiProblem(failure).code}`))
                },
              },
            )
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
          {error !== undefined && (
            <p role="alert" className="text-small text-danger">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={title.trim() === ''} loading={create.isPending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function RenameModuleDialog({
  courseId,
  module,
  open,
  onOpenChange,
}: {
  courseId: string
  module: TreeModule
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { t } = useTranslation()
  const rename = useRenameModule(courseId)
  const [drafts, setDrafts] = useState(() =>
    Object.fromEntries(SUPPORTED_LOCALES.map((l) => [l, module.texts[l]?.title ?? ''])),
  )
  const [error, setError] = useState<string>()

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogTitle>{t('authoring.renameModule')}</DialogTitle>
        <DialogDescription>{t('authoring.renameModuleBody')}</DialogDescription>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            setError(undefined)
            const changed = SUPPORTED_LOCALES.filter(
              (l) =>
                (drafts[l] ?? '').trim() !== '' &&
                (drafts[l] ?? '').trim() !== (module.texts[l]?.title ?? ''),
            )
            Promise.all(
              changed.map((l) =>
                rename.mutateAsync({
                  id: module.id,
                  locale: l,
                  title: (drafts[l] ?? '').trim(),
                  ...(module.texts[l]?.description !== undefined && {
                    description: module.texts[l]?.description,
                  }),
                }),
              ),
            ).then(
              () => {
                onOpenChange(false)
              },
              (failure: unknown) => {
                setError(t(`errors.${toApiProblem(failure).code}`))
              },
            )
          }}
        >
          {SUPPORTED_LOCALES.map((l) => (
            <Input
              key={l}
              label={t('authoring.titleIn', { locale: l.toUpperCase() })}
              value={drafts[l] ?? ''}
              maxLength={200}
              onChange={(event) => {
                setDrafts({ ...drafts, [l]: event.target.value })
              }}
            />
          ))}
          {error !== undefined && (
            <p role="alert" className="text-small text-danger">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" loading={rename.isPending}>
              {t('authoring.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function NewLessonDialog({
  courseId,
  moduleId,
  open,
  onOpenChange,
}: {
  courseId: string
  moduleId: string
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const create = useCreateLesson(courseId)
  const [title, setTitle] = useState('')
  const [sessionType, setSessionType] = useState<SessionType>('LECTURE')
  const [hours, setHours] = useState('2')
  const [error, setError] = useState<string>()
  const hoursValue = Number(hours)

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogTitle>{t('authoring.newLesson')}</DialogTitle>
        <DialogDescription>{t('authoring.newLessonBody')}</DialogDescription>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (title.trim() === '') return
            setError(undefined)
            create.mutate(
              {
                moduleId,
                code: `L-${codeFrom(title)}-${Date.now().toString(36).slice(-4).toUpperCase()}`,
                sessionType,
                ...(hours !== '' && Number.isFinite(hoursValue) && { hours: hoursValue }),
                title: title.trim(),
                locale,
              },
              {
                onSuccess: () => {
                  setTitle('')
                  onOpenChange(false)
                },
                onError: (failure) => {
                  setError(t(`errors.${toApiProblem(failure).code}`))
                },
              },
            )
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
          <div className="grid gap-4 sm:grid-cols-2">
            <Select
              label={t('authoring.sessionType')}
              value={sessionType}
              onValueChange={(value) => {
                const type = SESSION_TYPES.find((item) => item === value)
                if (type !== undefined) setSessionType(type)
              }}
              options={SESSION_TYPES.map((type) => ({ value: type, label: t(`sessionType.${type}`) }))}
            />
            <Input
              label={t('authoring.hoursLabel')}
              type="number"
              min={0}
              step={0.5}
              value={hours}
              onChange={(event) => {
                setHours(event.target.value)
              }}
            />
          </div>
          {error !== undefined && (
            <p role="alert" className="text-small text-danger">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="submit" disabled={title.trim() === ''} loading={create.isPending}>
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
