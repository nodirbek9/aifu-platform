import { useState } from 'react'
import { CalendarClock } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import {
  fromLocalInput,
  toLocalInput,
  useActivityStatus,
  useCourseActivitiesQuery,
  useCreateCourseWork,
  useSetActivityDeadline,
  type ActivityRaw,
  type CourseWorkType,
} from './activity-queries'
import { StatusBadge } from './curriculum-tree'
import { titleOf } from './model'
import { useAuthoringCourseQuery, useCurriculumTreeQuery } from './queries'
import { useCourseQuizzesQuery } from './quiz-queries'
import { useCourseTasksQuery } from './task-queries'

type Titled = {
  id?: string
  code?: string
  title?: string
  translations?: readonly { locale?: string; title?: string }[]
  maxPoints?: number
}

function titleIn(item: Titled | undefined, locale: string): string {
  if (item === undefined) return '—'
  return item.translations?.find((row) => row.locale === locale)?.title ?? item.title ?? item.code ?? '—'
}

/**
 * T-05 / T-06 — the course work: which coding tasks and quizzes the students get as work of the
 * course, by when, and whether it is out. Late work is accepted and marked in the review queue.
 */
export function CourseWorkCard({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const activities = useCourseActivitiesQuery(courseId)
  const tasks = useCourseTasksQuery(courseId)
  const quizzes = useCourseQuizzesQuery(courseId)
  const items = (activities.data ?? []).filter(
    (activity) => activity.activityType === 'CODING_TASK' || activity.activityType === 'QUIZ',
  )
  const referenced = (activity: ActivityRaw) =>
    activity.activityType === 'QUIZ'
      ? quizzes.data?.find((quiz) => quiz.id === activity.referenceId)
      : tasks.data?.find((task) => task.id === activity.referenceId)

  return (
    <Card className="lg:col-span-2">
      <CardHeader title={t('authoring.courseWork')} description={t('authoring.courseWorkBody')} />
      <CardContent className="grid grid-cols-1 gap-5">
        {activities.isPending && <Skeleton className="h-32" />}
        {activities.isSuccess && items.length === 0 && (
          <EmptyState
            icon={<CalendarClock aria-hidden className="size-6" />}
            title={t('authoring.noCourseWork')}
          />
        )}
        {items.length > 0 && (
          <ul className="divide-border grid grid-cols-1 divide-y" aria-label={t('authoring.courseWork')}>
            {items.map((activity) => (
              <CourseWorkRow
                key={activity.id}
                courseId={courseId}
                activity={activity}
                title={
                  activity.translations?.find((row) => row.locale === locale)?.title ??
                  titleIn(referenced(activity), locale)
                }
              />
            ))}
          </ul>
        )}
        <NewCourseWork courseId={courseId} />
      </CardContent>
    </Card>
  )
}

function CourseWorkRow({
  courseId,
  activity,
  title,
}: {
  courseId: string
  activity: ActivityRaw
  title: string
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const deadline = useSetActivityDeadline(courseId)
  const status = useActivityStatus(courseId)
  const [due, setDue] = useState(toLocalInput(activity.dueAt))
  const changed = due !== toLocalInput(activity.dueAt)
  const published = activity.status === 'PUBLISHED'
  const failure = deadline.error ?? status.error
  return (
    <li className="grid grid-cols-1 gap-2 py-3 md:grid-cols-[minmax(0,1fr)_auto] md:items-end">
      <div className="grid min-w-0 grid-cols-1 gap-1">
        <span className="text-body text-text truncate font-medium">{title}</span>
        <span className="flex flex-wrap items-center gap-2">
          <Badge tone="info">{t(`authoring.workType.${activity.activityType ?? 'QUIZ'}`)}</Badge>
          {activity.status !== undefined && <StatusBadge status={activity.status} />}
          <span className="text-caption text-text-muted tracking-normal">
            {activity.dueAt === undefined
              ? t('authoring.noDeadline')
              : t('authoring.dueOn', { date: formatDateTime(activity.dueAt, locale) })}
          </span>
        </span>
      </div>
      <div className="flex flex-wrap items-end gap-2">
        <Input
          className="w-56"
          type="datetime-local"
          label={t('authoring.deadline')}
          value={due}
          onChange={(event) => {
            setDue(event.target.value)
          }}
        />
        <Button
          variant="outline"
          disabled={!changed}
          loading={deadline.isPending}
          onClick={() => {
            const dueAt = fromLocalInput(due)
            deadline.mutate({ activity, ...(dueAt !== undefined && { dueAt }) })
          }}
        >
          {t('authoring.saveDeadline')}
        </Button>
        <Button
          variant="ghost"
          loading={status.isPending}
          onClick={() => {
            status.mutate({ id: activity.id ?? '', status: published ? 'DRAFT' : 'PUBLISHED' })
          }}
        >
          {published ? t('authoring.unpublish') : t('authoring.publish')}
        </Button>
      </div>
      {failure !== null && (
        <p role="alert" className="text-small text-danger md:col-span-2">
          {t(`errors.${toApiProblem(failure).code}`)}
        </p>
      )}
    </li>
  )
}

function NewCourseWork({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const tasks = useCourseTasksQuery(courseId)
  const quizzes = useCourseQuizzesQuery(courseId)
  const tree = useCurriculumTreeQuery(courseId)
  const course = useAuthoringCourseQuery(courseId)
  const create = useCreateCourseWork(courseId)
  const [type, setType] = useState<CourseWorkType>('CODING_TASK')
  const [reference, setReference] = useState<string>()
  const [moduleId, setModuleId] = useState<string>()
  const [component, setComponent] = useState<string>()
  const [due, setDue] = useState('')
  const pool: Titled[] = type === 'QUIZ' ? (quizzes.data ?? []) : (tasks.data ?? [])
  const chosen = pool.find((item) => item.id === reference)

  return (
    <section className="border-border grid grid-cols-1 gap-3 border-t pt-4">
      <h3 className="text-small text-text font-semibold">{t('authoring.newCourseWork')}</h3>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
        <Select
          label={t('authoring.workTypeLabel')}
          value={type}
          onValueChange={(value) => {
            setType(value === 'QUIZ' ? 'QUIZ' : 'CODING_TASK')
            setReference(undefined)
          }}
          options={(['CODING_TASK', 'QUIZ'] as const).map((value) => ({
            value,
            label: t(`authoring.workType.${value}`),
          }))}
        />
        <Select
          label={type === 'QUIZ' ? t('authoring.whichQuiz') : t('authoring.whichTask')}
          value={reference}
          placeholder={t('authoring.choose')}
          onValueChange={setReference}
          options={pool.map((item) => ({ value: item.id ?? '', label: titleIn(item, locale) }))}
        />
        <Select
          label={t('authoring.inModule')}
          value={moduleId}
          placeholder={t('authoring.choose')}
          onValueChange={setModuleId}
          options={(tree.data?.modules ?? []).map((module) => ({
            value: module.id,
            label: titleOf(module, locale),
          }))}
        />
        <Input
          type="datetime-local"
          label={t('authoring.deadline')}
          value={due}
          onChange={(event) => {
            setDue(event.target.value)
          }}
        />
        <Select
          label={t('authoring.gradeComponent')}
          value={component}
          placeholder={t('authoring.notGraded')}
          onValueChange={setComponent}
          options={(course.data?.components ?? []).map((item) => ({
            value: item.id,
            label: `${item.code} · ${String(item.maxPoints)}`,
          }))}
        />
      </div>
      {create.isError && (
        <p role="alert" className="text-small text-danger">
          {t(`errors.${toApiProblem(create.error).code}`)}
        </p>
      )}
      <Button
        className="justify-self-start"
        disabled={reference === undefined || moduleId === undefined}
        loading={create.isPending}
        onClick={() => {
          if (reference === undefined || moduleId === undefined) return
          const dueAt = fromLocalInput(due)
          const titles = (chosen?.translations ?? []).flatMap((row) =>
            row.locale !== undefined && row.title !== undefined && row.title !== ''
              ? [{ locale: row.locale, title: row.title }]
              : [],
          )
          create.mutate(
            {
              type,
              referenceId: reference,
              moduleId,
              titles: titles.length > 0 ? titles : [{ locale, title: titleIn(chosen, locale) }],
              ...(dueAt !== undefined && { dueAt }),
              ...(component !== undefined && { componentId: component }),
              ...(chosen?.maxPoints !== undefined && { maxPoints: chosen.maxPoints }),
            },
            {
              onSuccess: () => {
                setReference(undefined)
                setDue('')
                setComponent(undefined)
              },
            },
          )
        }}
      >
        {t('authoring.giveAsCourseWork')}
      </Button>
    </section>
  )
}
