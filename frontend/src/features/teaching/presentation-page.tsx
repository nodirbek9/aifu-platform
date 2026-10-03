import { useCallback, useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, Minus, Plus, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useParams, useSearchParams } from 'react-router'
import { LessonBlocks } from '@/features/lesson/lesson-blocks'
import type { LessonBlock } from '@/features/lesson/model'
import { useLessonQuery } from '@/features/lesson/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { ProgressBar } from '@/shared/ui/progress'
import { Skeleton } from '@/shared/ui/skeleton'
import { Stage } from '@/shared/ui/stage'

const ZOOMS = [1.25, 1.5, 1.75, 2, 2.25] as const
const DEFAULT_ZOOM = 2

/**
 * T-03.3 — a lesson on the projector: one section at a time, very large, dark, nothing around it.
 * Arrow keys, Page Up / Down and Space move; F toggles full screen; Escape leaves.
 */
export function PresentationPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const navigate = useNavigate()
  const { lessonId = '' } = useParams()
  const [search, setSearch] = useSearchParams()
  const lesson = useLessonQuery(lessonId, locale)
  const [zoomIndex, setZoomIndex] = useState<number>(ZOOMS.indexOf(DEFAULT_ZOOM))

  // practice references are for students working on their own, not for the wall
  const slides: LessonBlock[] = (lesson.data?.blocks ?? []).filter((block) => block.kind !== 'ref')
  const total = slides.length + 1
  const requested = Number(search.get('s') ?? '0')
  const index = Number.isInteger(requested) ? Math.min(Math.max(requested, 0), total - 1) : 0

  const go = useCallback(
    (next: number) => {
      // until the lesson is here there is nowhere to go
      if (!lesson.isSuccess) return
      setSearch({ s: String(Math.min(Math.max(next, 0), total - 1)) }, { replace: true })
    },
    [setSearch, total, lesson.isSuccess],
  )

  const leave = useCallback(() => {
    if (document.fullscreenElement !== null) void document.exitFullscreen()
    void navigate(-1)
  }, [navigate])

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.target instanceof HTMLElement && event.target.closest('input, textarea, .cm-editor')) return
      if (['ArrowRight', 'PageDown', ' '].includes(event.key)) {
        event.preventDefault()
        go(index + 1)
      } else if (['ArrowLeft', 'PageUp'].includes(event.key)) {
        event.preventDefault()
        go(index - 1)
      } else if (event.key === 'Home') go(0)
      else if (event.key === 'End') go(total - 1)
      else if (event.key === 'Escape' && document.fullscreenElement === null) leave()
      else if (event.key.toLowerCase() === 'f') {
        if (document.fullscreenElement === null)
          void document.documentElement.requestFullscreen().catch(() => undefined)
        else void document.exitFullscreen()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => {
      window.removeEventListener('keydown', onKey)
    }
  }, [go, index, total, leave])

  const zoom = ZOOMS[zoomIndex] ?? DEFAULT_ZOOM
  const slide = index === 0 ? undefined : slides[index - 1]

  return (
    <div className="dark bg-surface text-text flex h-dvh flex-col">
      <header className="flex h-14 shrink-0 items-center gap-2 px-4">
        <IconButton aria-label={t('teaching.leavePresentation')} onClick={leave}>
          <X aria-hidden className="size-5" />
        </IconButton>
        <p className="text-small text-text-muted min-w-0 flex-1 truncate">{lesson.data?.title}</p>
        <IconButton
          aria-label={t('task.fontSmaller')}
          disabled={zoomIndex === 0}
          onClick={() => {
            setZoomIndex(zoomIndex - 1)
          }}
        >
          <Minus aria-hidden className="size-5" />
        </IconButton>
        <IconButton
          aria-label={t('task.fontLarger')}
          disabled={zoomIndex === ZOOMS.length - 1}
          onClick={() => {
            setZoomIndex(zoomIndex + 1)
          }}
        >
          <Plus aria-hidden className="size-5" />
        </IconButton>
      </header>

      <main className="min-h-0 flex-1 overflow-y-auto">
        {lesson.isPending && <Skeleton className="m-10 h-96" />}
        {lesson.isError && (
          <div className="mx-auto max-w-xl p-6">
            <ErrorState
              problem={toApiProblem(lesson.error)}
              onRetry={() => {
                void lesson.refetch()
              }}
            />
          </div>
        )}
        {lesson.isSuccess && (
          <Stage zoom={zoom} className="min-h-full">
            <div className="mx-auto w-full max-w-4xl px-8 py-10">
              {slide === undefined ? (
                <div className="grid min-h-96 content-center gap-4">
                  {lesson.data.code !== undefined && (
                    <p className="text-caption tracking-caption text-text-muted font-medium uppercase">
                      {lesson.data.code} · {t(`sessionType.${lesson.data.sessionType}`)}
                    </p>
                  )}
                  <h1 className="font-display text-display-xl text-text font-semibold">
                    {lesson.data.title}
                  </h1>
                </div>
              ) : (
                <LessonBlocks blocks={[slide]} />
              )}
            </div>
          </Stage>
        )}
      </main>

      <footer className="flex h-16 shrink-0 items-center justify-center gap-6">
        <IconButton
          variant="outline"
          aria-label={t('common.previous')}
          disabled={index === 0}
          onClick={() => {
            go(index - 1)
          }}
        >
          <ChevronLeft aria-hidden className="size-6" />
        </IconButton>
        <span aria-live="polite" className="text-body text-text-muted min-w-16 text-center tabular-nums">
          {lesson.isSuccess && `${index + 1} / ${total}`}
        </span>
        <IconButton
          variant="outline"
          aria-label={t('common.next')}
          disabled={index === total - 1}
          onClick={() => {
            go(index + 1)
          }}
        >
          <ChevronRight aria-hidden className="size-6" />
        </IconButton>
      </footer>
      <ProgressBar
        value={index + 1}
        max={total}
        name={`${index + 1} / ${total}`}
        className="px-4 pb-3"
        barClassName="bg-accent"
      />
    </div>
  )
}
