import { useEffect, useMemo, useRef, useState } from 'react'
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  ChevronRight,
  Clock,
  Code2,
  ListChecks,
  ListTree,
  Lock,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { ActivityRow } from '@/features/courses/activity-row'
import { flattenPlan, type PlanLesson } from '@/features/courses/model'
import { useActivitiesQuery, useCoursePlanQuery, useCourseQuery } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { SessionTypeBadge } from '@/shared/ui/badge'
import { Button, button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { IconButton } from '@/shared/ui/icon-button'
import { LocaleControl } from '@/shared/ui/locale-control'
import { ReadingProgress } from '@/shared/ui/reading-progress'
import { Sheet, SheetBody, SheetContent, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { useToast } from '@/shared/ui/toast'
import { LessonBlocks } from './lesson-blocks'
import { LessonToc } from './lesson-toc'
import { lessonOutline, type Lesson, type LessonBlock } from './model'
import { useLessonQuery, useRecordLessonProgress } from './queries'

/**
 * S-04 — the lesson, the core of the product: a chapter to read. The app shell steps aside;
 * a slim bar leads back to the course, a line shows how far the reader is, the text sits in a
 * reading column with its table of contents beside it (a drawer on a phone).
 */
export function LessonPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { changeLocale } = useAuth()
  const { courseId = '', lessonId = '' } = useParams()
  const lesson = useLessonQuery(lessonId, locale)
  const plan = useCoursePlanQuery(courseId, locale)
  const course = useCourseQuery(courseId, locale)
  const [tocOpen, setTocOpen] = useState(false)

  const order = useMemo(() => (plan.data === undefined ? [] : flattenPlan(plan.data)), [plan.data])
  const index = order.findIndex((item) => item.id === lessonId)
  const planLesson = index === -1 ? undefined : order[index]
  const headings = useMemo(
    () => (lesson.data === undefined ? [] : lessonOutline(lesson.data.blocks)),
    [lesson.data],
  )
  const locked = planLesson?.locked === true

  // a new lesson starts at its top
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [lessonId])

  return (
    <div className="bg-surface min-h-dvh">
      <header className="bg-surface sticky top-0 z-20">
        <div className="flex h-14 items-center gap-1 px-2 md:gap-2 md:px-6">
          <Link
            to={`/student/courses/${courseId}`}
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
          {headings.length > 0 && !locked && (
            <IconButton
              aria-label={t('lesson.contents')}
              className="lg:hidden"
              onClick={() => {
                setTocOpen(true)
              }}
            >
              <ListTree aria-hidden className="size-5" />
            </IconButton>
          )}
        </div>
        <ReadingProgress />
      </header>

      <div className="max-w-app mx-auto flex gap-12 px-4 py-8 md:px-6 md:py-12 lg:px-12">
        <main className="max-w-lesson mx-auto w-full min-w-0">
          {lesson.isPending && <LessonSkeleton />}
          {lesson.isError && (
            <ErrorState
              problem={toApiProblem(lesson.error)}
              onRetry={() => {
                void lesson.refetch()
              }}
            />
          )}
          {lesson.isSuccess && locked && planLesson !== undefined && (
            <LockedLesson courseId={courseId} lesson={planLesson} order={order} />
          )}
          {lesson.isSuccess && !locked && (
            <LessonBody
              courseId={courseId}
              lesson={lesson.data}
              planLesson={planLesson}
              previous={index > 0 ? order[index - 1] : undefined}
              next={index !== -1 ? order[index + 1] : undefined}
            />
          )}
        </main>
        {lesson.isSuccess && !locked && headings.length > 0 && (
          <aside className="hidden w-60 shrink-0 lg:block">
            <LessonToc headings={headings} className="sticky top-24 max-h-screen overflow-y-auto pb-28" />
          </aside>
        )}
      </div>

      <Sheet open={tocOpen} onOpenChange={setTocOpen}>
        <SheetContent side="right">
          <SheetHeader>
            <SheetTitle>{t('lesson.contents')}</SheetTitle>
          </SheetHeader>
          <SheetBody>
            <LessonToc
              headings={headings}
              onNavigate={() => {
                setTocOpen(false)
              }}
            />
          </SheetBody>
        </SheetContent>
      </Sheet>
    </div>
  )
}

function LessonSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6">
      <Skeleton className="h-4 w-40" />
      <Skeleton className="h-10 w-4/5" />
      <SkeletonText lines={5} />
      <Skeleton className="h-40" />
      <SkeletonText lines={6} />
    </div>
  )
}

function LockedLesson({
  courseId,
  lesson,
  order,
}: {
  courseId: string
  lesson: PlanLesson
  order: PlanLesson[]
}) {
  const { t } = useTranslation()
  const prerequisites = order.filter((item) => lesson.prerequisiteLessonIds.includes(item.id))
  return (
    <section className="grid justify-items-start gap-5 py-6">
      <Lock aria-hidden className="text-text-muted size-9" />
      <div className="grid gap-2">
        <h1 className="font-display text-display-lg text-text font-semibold">{t('lesson.lockedTitle')}</h1>
        <p className="font-reading text-lead text-text-muted">{lesson.title}</p>
      </div>
      {prerequisites.length > 0 && (
        <div className="grid gap-2">
          <p className="text-body text-text">{t('lesson.lockedBody')}</p>
          <ul className="grid gap-1">
            {prerequisites.map((item) => (
              <li key={item.id}>
                <Link
                  to={`/student/courses/${courseId}/lessons/${item.id}`}
                  className={button({ variant: 'link', class: 'h-auto px-0 whitespace-normal' })}
                >
                  {item.title}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}
      <Link to={`/student/courses/${courseId}`} className={button({ variant: 'outline' })}>
        {t('lesson.back')}
      </Link>
    </section>
  )
}

type LessonBodyProps = {
  courseId: string
  lesson: Lesson
  planLesson: PlanLesson | undefined
  previous: PlanLesson | undefined
  next: PlanLesson | undefined
}

function LessonBody({ courseId, lesson, planLesson, previous, next }: LessonBodyProps) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const record = useRecordLessonProgress(courseId, lesson.id)
  const status = planLesson?.status
  const done = status === 'COMPLETED'

  // Opening a lesson for the first time starts it. Once per lesson, whatever re-renders.
  const startedRef = useRef<string | null>(null)
  const { mutate } = record
  useEffect(() => {
    if (status === 'NOT_STARTED' && startedRef.current !== lesson.id) {
      startedRef.current = lesson.id
      mutate('IN_PROGRESS')
    }
  }, [status, lesson.id, mutate])

  return (
    <article className="grid grid-cols-1 gap-10">
      <header className="grid gap-4">
        <div className="text-small text-text-muted flex flex-wrap items-center gap-x-4 gap-y-2">
          <SessionTypeBadge type={lesson.sessionType} label={t(`sessionType.${lesson.sessionType}`)} />
          {lesson.code !== undefined && <span className="font-mono">{lesson.code}</span>}
          {lesson.hours !== undefined && <span>{t('courses.hours', { count: lesson.hours })}</span>}
          <span className="inline-flex items-center gap-1.5">
            <Clock aria-hidden className="size-4" />
            {t('lesson.readingTime', { minutes: lesson.readingMinutes })}
          </span>
        </div>
        <h1 className="font-display text-display-lg text-text md:text-display-xl font-semibold">
          {lesson.title}
        </h1>
        {lesson.hasFallback && <FallbackMark />}
      </header>

      {lesson.blocks.every((block) => block.kind === 'ref') ? (
        <EmptyState title={t('lesson.emptyTitle')} body={t('lesson.emptyBody')} />
      ) : (
        <LessonBlocks blocks={lesson.blocks} />
      )}

      <Practice courseId={courseId} lessonId={lesson.id} blocks={lesson.blocks} />

      <footer className="border-border grid gap-8 border-t pt-8">
        <div className="flex flex-wrap items-center gap-4">
          {done ? (
            <p className="text-body text-success inline-flex items-center gap-2 font-medium">
              <CheckCircle2 aria-hidden className="size-5" />
              {t('lesson.done')}
            </p>
          ) : (
            <Button
              variant="accent"
              loading={record.isPending && record.variables === 'COMPLETED'}
              onClick={() => {
                record.mutate('COMPLETED', {
                  onError: (error) => {
                    const problem = toApiProblem(error)
                    toast({
                      tone: 'danger',
                      title: t(`errors.${problem.code}`),
                      ...(problem.requestId !== undefined && {
                        body: `${t('common.requestId')}: ${problem.requestId}`,
                      }),
                    })
                  },
                })
              }}
            >
              <CheckCircle2 aria-hidden className="size-5" />
              {t('lesson.markDone')}
            </Button>
          )}
        </div>

        <nav aria-label={t('lesson.neighbours')} className="grid gap-3 sm:grid-cols-2">
          {previous !== undefined && !previous.locked ? (
            <NeighbourLink courseId={courseId} lesson={previous} direction="previous" />
          ) : (
            <span />
          )}
          {next !== undefined && !next.locked && (
            <NeighbourLink courseId={courseId} lesson={next} direction="next" />
          )}
        </nav>
      </footer>
    </article>
  )
}

type NeighbourLinkProps = { courseId: string; lesson: PlanLesson; direction: 'previous' | 'next' }

function NeighbourLink({ courseId, lesson, direction }: NeighbourLinkProps) {
  const { t } = useTranslation()
  const forward = direction === 'next'
  return (
    <Link
      to={`/student/courses/${courseId}/lessons/${lesson.id}`}
      viewTransition
      className={cn(
        'border-border bg-surface-raised flex items-center gap-3 rounded-lg border px-4 py-3',
        'duration-fast ease-standard hover:border-border-strong transition-[border-color]',
        'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
        forward && 'sm:text-right',
      )}
    >
      {!forward && <ArrowLeft aria-hidden className="text-text-muted size-5 shrink-0" />}
      <span className="grid min-w-0 flex-1 gap-0.5">
        <span className="text-caption text-text-muted font-medium uppercase">
          {forward ? t('lesson.next') : t('lesson.previous')}
        </span>
        <span className="text-body text-text">{lesson.title}</span>
      </span>
      {forward && <ArrowRight aria-hidden className="text-text-muted size-5 shrink-0" />}
    </Link>
  )
}

type PracticeProps = { courseId: string; lessonId: string; blocks: readonly LessonBlock[] }

/** The practice of a lesson: its quizzes and tasks with where the student stands in each. */
function Practice({ courseId, lessonId, blocks }: PracticeProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const activities = useActivitiesQuery(locale, courseId)
  const ofLesson = (activities.data ?? []).filter((activity) => activity.lessonId === lessonId)
  // a reference in the text whose task has no activity yet: still reachable, without a status
  const unbound = blocks.flatMap((block) =>
    block.kind === 'ref' &&
    block.ref !== 'ACTIVITY' &&
    !ofLesson.some((activity) => activity.referenceId === block.refId)
      ? [block]
      : [],
  )
  if (!blocks.some((block) => block.kind === 'ref') && ofLesson.length === 0) return null

  return (
    <section className="grid gap-3">
      <div className="grid gap-1">
        <h2 className="font-display text-display-lg text-text font-semibold">{t('lesson.practiceTitle')}</h2>
        <p className="text-small text-text-muted">{t('lesson.practiceBody')}</p>
      </div>
      {activities.isPending && <SkeletonText lines={2} />}
      {activities.isError && (
        <ErrorState
          problem={toApiProblem(activities.error)}
          onRetry={() => {
            void activities.refetch()
          }}
        />
      )}
      {activities.isSuccess && (
        <ul className="divide-border border-border bg-surface-raised divide-y rounded-lg border px-4">
          {ofLesson.map((activity) => (
            <li key={activity.activityId}>
              <ActivityRow activity={activity} />
            </li>
          ))}
          {unbound.map((block) => {
            const Icon = block.ref === 'CODING_TASK' ? Code2 : ListChecks
            const segment = block.ref === 'CODING_TASK' ? 'tasks' : 'quizzes'
            return (
              <li key={block.id}>
                <Link
                  to={`/student/courses/${courseId}/${segment}/${block.refId}`}
                  className="hover:bg-surface-sunken focus-visible:outline-focus-ring -mx-2 flex items-center gap-3 rounded-sm px-2 py-3 focus-visible:outline-2"
                >
                  <Icon aria-hidden className="text-text-muted size-5 shrink-0" />
                  <span className="text-body text-text flex-1 font-medium">
                    {t(`activityType.${block.ref === 'CODING_TASK' ? 'CODING_TASK' : 'QUIZ'}`)}
                  </span>
                  <ChevronRight aria-hidden className="text-text-muted size-5 shrink-0" />
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
