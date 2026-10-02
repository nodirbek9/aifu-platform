import { BookOpen, CheckCircle2, Circle, CircleDot, Lock } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { cn } from '@/shared/lib/cn'
import { SessionTypeBadge } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { sessionBar } from '@/shared/ui/session-type'
import type { CoursePlan, LessonStatus, PlanLesson } from './model'

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
  return (
    <div className="grid gap-8">
      {plan.modules
        .filter((module) => module.lessons.length > 0)
        .map((module) => (
          <section key={module.id} className="grid gap-3">
            <h2 className="font-display text-title text-text font-semibold">{module.title}</h2>
            <ol className="divide-border border-border bg-surface-raised divide-y overflow-hidden rounded-lg border">
              {module.lessons.map((lesson) => (
                <li key={lesson.id}>
                  <LessonRow courseId={plan.courseId} lesson={lesson} />
                </li>
              ))}
            </ol>
          </section>
        ))}
    </div>
  )
}

function LessonRow({ courseId, lesson }: { courseId: string; lesson: PlanLesson }) {
  const { t } = useTranslation()
  const { Icon, className: iconClass } = statusIcon[lesson.status]
  const body = (
    <>
      <div aria-hidden className={cn('w-1 shrink-0 self-stretch', sessionBar[lesson.sessionType])} />
      <div className="flex min-w-0 flex-1 flex-wrap items-center gap-x-4 gap-y-2 py-3 pr-4">
        <div className="grid min-w-0 flex-1 basis-64 gap-1">
          <p className="text-body text-text">
            {lesson.code !== undefined && (
              <span className="text-text-muted text-small mr-2 font-mono">{lesson.code}</span>
            )}
            {lesson.title}
          </p>
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
        <div className="text-small text-text-muted flex shrink-0 items-center gap-2">
          {lesson.locked ? (
            <>
              <Lock aria-hidden className="size-4" />
              <span>{t('courses.locked')}</span>
            </>
          ) : (
            <>
              <Icon aria-hidden className={cn('size-5', iconClass)} />
              <span>{t(`lessonStatus.${lesson.status}`)}</span>
            </>
          )}
        </div>
      </div>
    </>
  )

  if (lesson.locked) return <div className="flex gap-4 opacity-70">{body}</div>
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
