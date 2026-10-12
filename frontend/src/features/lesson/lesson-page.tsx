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
import { activityPath, flattenPlan, type PlanLesson } from '@/features/courses/model'
import { useActivitiesQuery, useCoursePlanQuery, useCourseQuery } from '@/features/courses/queries'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { Button, button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { IconButton } from '@/shared/ui/icon-button'
import { LocaleControl } from '@/shared/ui/locale-control'
import { ReadingProgress } from '@/shared/ui/reading-progress'
import { Sheet, SheetBody, SheetContent, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { StepDots } from '@/shared/ui/step-dots'
import { useToast } from '@/shared/ui/toast'
import { LessonBlocks } from './lesson-blocks'
import { LessonToc } from './lesson-toc'
import { lessonOutline, nextActionOf, type Lesson, type LessonBlock, type LessonPath } from './model'
import { useLessonPathQuery, useLessonQuery, useRecordLessonProgress } from './queries'
import { useReadingHeartbeat } from './use-reading-heartbeat'

type LockedLessonDetails = {
  reason?: 'PREVIOUS_LESSON_INCOMPLETE' | 'MIDTERM_REQUIRED'
  blockingLessonId?: string
  midtermActivityId?: string
}

function extractLockDetails(problem: ApiProblem | undefined): LockedLessonDetails | undefined {
  if (problem?.code !== 'LESSON_PREREQUISITE_NOT_MET') return undefined
  const details = problem.details
  const rawReason = typeof details?.reason === 'string' ? details.reason : undefined
  const reason =
    rawReason === 'PREVIOUS_LESSON_INCOMPLETE' || rawReason === 'MIDTERM_REQUIRED' ? rawReason : undefined
  const blockingLessonId =
    typeof details?.blockingLessonId === 'string' ? details.blockingLessonId : undefined
  const midtermActivityId =
    typeof details?.midtermActivityId === 'string' ? details.midtermActivityId : undefined
  return { reason, blockingLessonId, midtermActivityId }
}

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

  const apiProblem = lesson.isError ? toApiProblem(lesson.error) : undefined
  const isServerLocked = apiProblem?.code === 'LESSON_PREREQUISITE_NOT_MET'
  const lockDetails = isServerLocked ? extractLockDetails(apiProblem) : undefined

  const pathQuery = useLessonPathQuery(lessonId, { enabled: !isServerLocked })
  const pathEnabled = pathQuery.data?.pathEnabled ?? plan.data?.pathEnabled ?? false

  const order = useMemo(() => (plan.data === undefined ? [] : flattenPlan(plan.data)), [plan.data])
  const index = order.findIndex((item) => item.id === lessonId)
  const planLesson = index === -1 ? undefined : order[index]
  const headings = useMemo(
    () => (lesson.data === undefined ? [] : lessonOutline(lesson.data.blocks)),
    [lesson.data],
  )
  const locked = isServerLocked || (!pathEnabled && planLesson?.locked === true)
  const prerequisites = useMemo(
    () =>
      planLesson === undefined
        ? []
        : order.filter((item) => planLesson.prerequisiteLessonIds.includes(item.id)),
    [order, planLesson],
  )

  // a new lesson starts at its top
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [lessonId])

  return (
    <div className="bg-reading-surface text-reading-text min-h-dvh">
      <header className="bg-reading-surface border-border/60 sticky top-0 z-20 border-b">
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
          {pathEnabled && !locked && pathQuery.data && (
            <div className="mr-2 flex items-center gap-3">
              <StepDots steps={pathQuery.data.steps} size="sm" />
              <span className="text-small text-text-muted hidden tabular-nums sm:inline-block">
                {t('lesson.readingProgress', {
                  current: Math.floor(pathQuery.data.activeReadingSeconds / 60),
                  total: Math.ceil(
                    (pathQuery.data.requiredReadingSeconds || (lesson.data?.readingMinutes ?? 1) * 60) / 60,
                  ),
                })}
              </span>
            </div>
          )}
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
          {isServerLocked && (
            <LockedLesson
              courseId={courseId}
              title={planLesson?.title}
              lockDetails={lockDetails}
              prerequisites={prerequisites}
            />
          )}
          {lesson.isPending && !isServerLocked && <LessonSkeleton />}
          {lesson.isError && !isServerLocked && (
            <ErrorState
              problem={apiProblem}
              onRetry={() => {
                void lesson.refetch()
              }}
            />
          )}
          {lesson.isSuccess && locked && !isServerLocked && planLesson !== undefined && (
            <LockedLesson courseId={courseId} title={planLesson.title} prerequisites={prerequisites} />
          )}
          {lesson.isSuccess && !locked && (
            <LessonBody
              courseId={courseId}
              lesson={lesson.data}
              planLesson={planLesson}
              lessonPath={pathQuery.data}
              pathEnabled={pathEnabled}
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

type LockedLessonProps = {
  courseId: string
  title?: string
  lockDetails?: LockedLessonDetails
  prerequisites?: PlanLesson[]
}

function LockedLesson({ courseId, title, lockDetails, prerequisites = [] }: LockedLessonProps) {
  const { t } = useTranslation()

  const isPreviousIncomplete = lockDetails?.reason === 'PREVIOUS_LESSON_INCOMPLETE'
  const isMidtermRequired = lockDetails?.reason === 'MIDTERM_REQUIRED'

  const headingText = isPreviousIncomplete
    ? t('lesson.lockedPreviousTitle')
    : isMidtermRequired
      ? t('lesson.lockedMidtermTitle')
      : t('lesson.lockedTitle')

  const bodyText = isPreviousIncomplete
    ? t('lesson.lockedPreviousBody')
    : isMidtermRequired
      ? t('lesson.lockedMidtermBody')
      : t('lesson.lockedBody')

  return (
    <section className="grid justify-items-start gap-5 py-6">
      <Lock aria-hidden className="text-text-muted size-9" />
      <div className="grid gap-2">
        <h1 className="font-display text-display-md md:text-display-lg text-text font-semibold">
          {headingText}
        </h1>
        {title && <p className="font-reading text-lead text-text-muted">{title}</p>}
        <p className="text-body text-text">{bodyText}</p>
      </div>

      {prerequisites.length > 0 && !isPreviousIncomplete && !isMidtermRequired && (
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
      )}

      <div className="flex flex-wrap items-center gap-3 pt-2">
        {lockDetails?.blockingLessonId && (
          <Link
            to={`/student/courses/${courseId}/lessons/${lockDetails.blockingLessonId}`}
            className={button({ variant: 'primary' })}
          >
            {t('lesson.goToBlockingLesson')}
          </Link>
        )}
        {lockDetails?.midtermActivityId && (
          <Link
            to={`/student/courses/${courseId}/activities/${lockDetails.midtermActivityId}`}
            className={button({ variant: 'primary' })}
          >
            {t('lesson.goToMidterm')}
          </Link>
        )}
        <Link to={`/student/courses/${courseId}`} className={button({ variant: 'outline' })}>
          {t('lesson.back')}
        </Link>
      </div>
    </section>
  )
}

type LessonBodyProps = {
  courseId: string
  lesson: Lesson
  planLesson: PlanLesson | undefined
  lessonPath: LessonPath | undefined
  pathEnabled: boolean
  previous: PlanLesson | undefined
  next: PlanLesson | undefined
}

function LessonBody({
  courseId,
  lesson,
  planLesson,
  lessonPath,
  pathEnabled,
  previous,
  next,
}: LessonBodyProps) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const record = useRecordLessonProgress(courseId, lesson.id)
  const status = planLesson?.status
  const done = status === 'COMPLETED'
  const sentinelRef = useRef<HTMLDivElement>(null)
  const nextAction = lessonPath ? nextActionOf(lessonPath.steps) : undefined

  useReadingHeartbeat({
    courseId,
    lessonId: lesson.id,
    pathEnabled,
    lectureDone: lessonPath?.steps.lecture === 'DONE',
    sentinelRef,
  })

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
    <article className="grid grid-cols-1 gap-10 pb-[calc(theme(spacing.24)+env(safe-area-inset-bottom))]">
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
        <h1 className="font-display text-display-md md:text-display-lg text-text font-semibold">
          {lesson.title}
        </h1>
        {lesson.hasFallback && <FallbackMark />}
      </header>

      {lesson.blocks.every((block) => block.kind === 'ref') ? (
        <EmptyState title={t('lesson.emptyTitle')} body={t('lesson.emptyBody')} />
      ) : (
        <LessonBlocks blocks={lesson.blocks} sentinelRef={sentinelRef} />
      )}

      <div id="practice-section">
        <Practice courseId={courseId} lessonId={lesson.id} blocks={lesson.blocks} lessonPath={lessonPath} />
      </div>

      <footer className="border-border grid gap-8 border-t pt-8">
        {pathEnabled ? (
          <div className="border-border bg-surface-raised grid gap-4 rounded-lg border p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <p className="text-body text-text font-semibold">{t('lesson.stepsTitle')}</p>
                {lessonPath && <StepDots steps={lessonPath.steps} />}
              </div>
            </div>

            {nextAction === 'KEEP_READING' && (
              <div className="bg-surface-sunken text-small text-text-muted flex items-center gap-3 rounded-md p-3">
                <Clock aria-hidden className="size-4 shrink-0" />
                <span>{t('lesson.nextActionKeepReading')}</span>
              </div>
            )}

            {nextAction === 'TAKE_TEST' && (
              <div className="bg-accent-soft text-accent-text flex flex-wrap items-center justify-between gap-3 rounded-md p-3">
                <span className="text-small font-medium">{t('lesson.nextActionTakeTestPrompt')}</span>
                <Link
                  to={`/student/courses/${courseId}?tab=work`}
                  className={button({ variant: 'accent', size: 'sm' })}
                >
                  {t('lesson.nextActionTakeTest')}
                </Link>
              </div>
            )}

            {nextAction === 'PRACTICE' && (
              <div className="bg-accent-soft text-accent-text flex flex-wrap items-center justify-between gap-3 rounded-md p-3">
                <span className="text-small font-medium">{t('lesson.nextActionPracticePrompt')}</span>
                <button
                  type="button"
                  onClick={() => {
                    document.getElementById('practice-section')?.scrollIntoView({ behavior: 'smooth' })
                  }}
                  className={button({ variant: 'accent', size: 'sm' })}
                >
                  {t('lesson.nextActionPractice')}
                </button>
              </div>
            )}

            {nextAction === 'ALL_DONE' && (
              <div className="bg-success-soft text-success text-small flex items-center gap-3 rounded-md p-3 font-medium">
                <CheckCircle2 aria-hidden className="size-5 shrink-0" />
                <span>{t('lesson.nextActionAllDone')}</span>
              </div>
            )}
          </div>
        ) : (
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
        )}

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

type PracticeProps = {
  courseId: string
  lessonId: string
  blocks: readonly LessonBlock[]
  lessonPath?: LessonPath
}

/** The practice of a lesson: its quizzes and tasks with where the student stands in each. */
function Practice({ courseId, lessonId, blocks, lessonPath }: PracticeProps) {
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

  // The section lists the lesson's test and its practice, so it is done only when both are.
  const sectionSteps = [lessonPath?.steps.test, lessonPath?.steps.practice].filter(
    (step) => step !== undefined && step !== 'NONE',
  )
  const sectionDone = sectionSteps.length > 0 && sectionSteps.every((step) => step === 'DONE')
  const practiceActivityId = lessonPath?.practiceActivityId
  const practiceActivity =
    practiceActivityId !== undefined
      ? (activities.data ?? []).find((a) => a.activityId === practiceActivityId)
      : undefined
  const practiceLink =
    practiceActivity !== undefined
      ? activityPath(practiceActivity)
      : practiceActivityId !== undefined
        ? `/student/courses/${courseId}/activities/${practiceActivityId}`
        : null

  return (
    <section className="grid gap-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="grid gap-1">
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="font-display text-display-lg text-text font-semibold">
              {t('lesson.practiceTitle')}
            </h2>
            {sectionSteps.length > 0 && (
              <Badge tone={sectionDone ? 'success' : 'warning'}>
                {t(sectionDone ? 'steps.statusDone' : 'steps.statusTodo')}
              </Badge>
            )}
          </div>
          <p className="text-small text-text-muted">{t('lesson.practiceBody')}</p>
        </div>
        {practiceLink !== null && (
          <Link to={practiceLink} className={button({ variant: 'outline', size: 'sm' })}>
            {t('lesson.openPractice')}
          </Link>
        )}
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
