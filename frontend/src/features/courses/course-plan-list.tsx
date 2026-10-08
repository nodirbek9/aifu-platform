import { Fragment, useState } from 'react'
import { BookOpen, CheckCircle2, Circle, CircleDot, Lock } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { sessionBar } from '@/shared/ui/session-type'
import { StepDots } from '@/shared/ui/step-dots'
import type { CoursePlan, LessonStatus, MidtermStanding, PlanLesson } from './model'

const statusIcon = {
  NOT_STARTED: { Icon: Circle, className: 'text-text-muted/60' },
  IN_PROGRESS: { Icon: CircleDot, className: 'text-info' },
  COMPLETED: { Icon: CheckCircle2, className: 'text-success' },
} as const satisfies Record<LessonStatus, { Icon: typeof Circle; className: string }>

/** The course plan: modules as sections, each lesson a row with its type, hours and status. */
export function CoursePlanList({ plan }: { plan: CoursePlan }) {
  const { t } = useTranslation()

  if (plan.modules.every((module) => module.lessons.length === 0)) {
    return (
      <EmptyState
        icon={<BookOpen aria-hidden className="size-7" />}
        title={t('courses.planEmptyTitle')}
        body={t('courses.planEmptyBody')}
      />
    )
  }

  const hasMidterm = plan.midterm !== undefined
  const midtermAfterLessonId = plan.midterm?.afterLessonId
  const anchorInList = plan.modules.some((module) =>
    module.lessons.some((lesson) => lesson.id === midtermAfterLessonId),
  )

  return (
    <div className="grid gap-8">
      {plan.modules
        .filter((module) => module.lessons.length > 0)
        .map((module) => (
          <section key={module.id} className="grid gap-3">
            <h2 className="font-display text-title text-text font-semibold">{module.title}</h2>
            <ol className="divide-border border-border bg-surface-raised divide-y overflow-hidden rounded-lg border">
              {module.lessons.map((lesson) => {
                const isMidtermAnchor =
                  hasMidterm && midtermAfterLessonId !== undefined && lesson.id === midtermAfterLessonId

                return (
                  <Fragment key={lesson.id}>
                    <li>
                      <LessonRow courseId={plan.courseId} lesson={lesson} pathEnabled={plan.pathEnabled} />
                    </li>
                    {isMidtermAnchor && plan.midterm !== undefined && (
                      <li className="bg-surface-sunken/40 px-4 py-2">
                        <MidtermCard courseId={plan.courseId} midterm={plan.midterm} />
                      </li>
                    )}
                  </Fragment>
                )
              })}
            </ol>
          </section>
        ))}

      {hasMidterm && !anchorInList && plan.midterm !== undefined && (
        <section className="grid gap-3">
          <MidtermCard courseId={plan.courseId} midterm={plan.midterm} />
        </section>
      )}
    </div>
  )
}

function LessonRow({
  courseId,
  lesson,
  pathEnabled,
}: {
  courseId: string
  lesson: PlanLesson
  pathEnabled: boolean
}) {
  const { t } = useTranslation()
  const { Icon, className: iconClass } = statusIcon[lesson.status]

  const lockReasonText =
    lesson.lockReason === 'PREVIOUS_LESSON_INCOMPLETE'
      ? t('courses.lockReasonPrevious')
      : lesson.lockReason === 'MIDTERM_REQUIRED'
        ? t('courses.lockReasonMidterm')
        : t('courses.locked')

  const body = (
    <>
      <div aria-hidden className={cn('w-1 shrink-0 self-stretch', sessionBar[lesson.sessionType])} />
      <div className="flex min-w-0 flex-1 flex-wrap items-center gap-x-4 gap-y-2 py-3 pr-4">
        <div className="grid min-w-0 flex-1 basis-64 gap-1">
          <div className="flex flex-wrap items-center gap-2">
            {lesson.code !== undefined && (
              <span className="text-text-muted text-small font-mono">{lesson.code}</span>
            )}
            <p className="text-body text-text font-medium">{lesson.title}</p>
            {lesson.waived && <Badge tone="accent">{t('courses.waived')}</Badge>}
          </div>
          <div className="text-small text-text-muted flex flex-wrap items-center gap-x-3 gap-y-1">
            <SessionTypeBadge type={lesson.sessionType} label={t(`sessionType.${lesson.sessionType}`)} />
            {lesson.hours !== undefined && <span>{t('courses.hours', { count: lesson.hours })}</span>}
            {lesson.activitiesTotal > 0 && (
              <span className="tabular-nums">
                {t('courses.activitiesCount', {
                  done: lesson.activitiesCompleted,
                  total: lesson.activitiesTotal,
                })}
              </span>
            )}
          </div>
        </div>
        <div className="text-small text-text-muted flex shrink-0 items-center gap-3">
          {pathEnabled && <StepDots steps={lesson.steps} />}
          {lesson.locked ? (
            <div className="text-text-muted flex items-center gap-1.5">
              <Lock aria-hidden className="size-4" />
              <span>{lockReasonText}</span>
            </div>
          ) : !pathEnabled ? (
            <div className="flex items-center gap-1.5">
              <Icon aria-hidden className={cn('size-5', iconClass)} />
              <span>{t(`lessonStatus.${lesson.status}`)}</span>
            </div>
          ) : null}
        </div>
      </div>
    </>
  )

  if (lesson.locked) {
    return <div className="flex cursor-not-allowed gap-4 opacity-70">{body}</div>
  }

  return (
    <Link
      to={`/student/courses/${courseId}/lessons/${lesson.id}`}
      className={cn(
        'flex gap-4',
        'duration-fast ease-standard hover:bg-surface-sunken transition-[background-color]',
        'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:-outline-offset-2',
      )}
    >
      {body}
    </Link>
  )
}

function MidtermCard({ courseId, midterm }: { courseId: string; midterm: MidtermStanding }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const [now] = useState(() => Date.now())
  const isOpen = !midterm.opensAt || new Date(midterm.opensAt).getTime() <= now

  return (
    <div className="border-border bg-surface-raised flex flex-wrap items-center justify-between gap-4 rounded-lg border p-4">
      <div className="grid gap-1">
        <div className="flex items-center gap-2">
          <p className="text-body text-text font-semibold">{t('courses.midtermCardTitle')}</p>
          <Badge tone={midterm.passed ? 'success' : 'warning'}>
            {midterm.passed ? t('courses.midtermPassed') : t('courses.midtermNotPassed')}
          </Badge>
        </div>
        <div className="text-small text-text-muted flex flex-wrap items-center gap-x-3 gap-y-1">
          {midterm.opensAt && (
            <span>{t('courses.midtermOpensAt', { date: formatDateTime(midterm.opensAt, locale) })}</span>
          )}
          <span>
            {midterm.attemptsAllowed !== undefined
              ? t('courses.midtermAttempts', {
                  used: midterm.attemptsUsed,
                  allowed: midterm.attemptsAllowed,
                })
              : t('courses.midtermAttemptsUnlimited', { used: midterm.attemptsUsed })}
          </span>
        </div>
      </div>
      {midterm.activityId && (
        <div className="flex shrink-0 items-center">
          {isOpen ? (
            <Link
              to={`/student/courses/${courseId}/activities/${midterm.activityId}`}
              className={button({ variant: 'accent', size: 'sm' })}
            >
              {t('courses.startMidterm')}
            </Link>
          ) : (
            <span className="text-caption text-text-muted">
              {t('courses.midtermOpensAt', {
                date: formatDateTime(midterm.opensAt ?? '', locale),
              })}
            </span>
          )}
        </div>
      )}
    </div>
  )
}
