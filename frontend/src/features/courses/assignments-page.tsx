import { ClipboardList } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Select } from '@/shared/ui/select'
import { SkeletonText } from '@/shared/ui/skeleton'
import { ActivityRow } from './activity-row'
import type { Activity, ActivityState } from './model'
import { useActivitiesQuery } from './queries'

const ALL = 'all'
const STATES: readonly ActivityState[] = ['OVERDUE', 'TODO', 'IN_PROGRESS', 'COMPLETED', 'UPCOMING']

/** Work with a deadline first, soonest first; then work without one. */
function byDeadline(a: Activity, b: Activity): number {
  if (a.dueAt === undefined && b.dueAt === undefined) return a.title.localeCompare(b.title)
  if (a.dueAt === undefined) return 1
  if (b.dueAt === undefined) return -1
  return a.dueAt.localeCompare(b.dueAt)
}

/** S-07 — everything the student has to do, across courses, filtered and sorted by deadline. */
export function AssignmentsPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const [searchParams, setSearchParams] = useSearchParams()
  const activities = useActivitiesQuery(locale)
  const course = searchParams.get('course') ?? ALL
  const type = searchParams.get('type') ?? ALL
  const state = searchParams.get('state') ?? ALL

  // filters live in the address: a filtered list can be linked to and survives a reload
  const setFilter = (name: string, value: string) => {
    const next = new URLSearchParams(searchParams)
    if (value === ALL) next.delete(name)
    else next.set(name, value)
    setSearchParams(next, { replace: true })
  }

  const all = activities.data ?? []
  const courses = [...new Map(all.map((item) => [item.courseId, item.courseTitle])).entries()]
  const types = [...new Set(all.map((item) => item.type))]
  const shown = all
    .filter((item) => course === ALL || item.courseId === course)
    .filter((item) => type === ALL || item.type === type)
    .filter((item) => state === ALL || item.state === state)
    .sort(byDeadline)
  const filtered = course !== ALL || type !== ALL || state !== ALL

  return (
    <div className="grid gap-6">
      <PageHeader
        title={t('nav.assignments')}
        context={activities.isSuccess ? t('assignments.count', { count: shown.length }) : undefined}
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <Select
          label={t('assignments.filterCourse')}
          value={course}
          onValueChange={(value) => {
            setFilter('course', value)
          }}
          options={[
            { value: ALL, label: t('assignments.all') },
            ...courses.map(([id, title]) => ({ value: id, label: title })),
          ]}
        />
        <Select
          label={t('assignments.filterType')}
          value={type}
          onValueChange={(value) => {
            setFilter('type', value)
          }}
          options={[
            { value: ALL, label: t('assignments.all') },
            ...types.map((item) => ({ value: item, label: t(`activityType.${item}`) })),
          ]}
        />
        <Select
          label={t('assignments.filterState')}
          value={state}
          onValueChange={(value) => {
            setFilter('state', value)
          }}
          options={[
            { value: ALL, label: t('assignments.all') },
            ...STATES.map((item) => ({ value: item, label: t(`activityState.${item}`) })),
          ]}
        />
      </div>

      {activities.isPending && <SkeletonText lines={8} />}
      {activities.isError && (
        <ErrorState
          problem={toApiProblem(activities.error)}
          onRetry={() => {
            void activities.refetch()
          }}
        />
      )}
      {activities.isSuccess && shown.length === 0 && (
        <EmptyState
          icon={<ClipboardList aria-hidden className="size-7" />}
          title={filtered ? t('assignments.emptyTitle') : t('dashboard.deadlinesEmptyTitle')}
          body={filtered ? t('assignments.emptyBody') : t('dashboard.deadlinesEmptyBody')}
          action={
            filtered ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSearchParams({}, { replace: true })
                }}
              >
                {t('assignments.resetFilters')}
              </Button>
            ) : undefined
          }
        />
      )}
      {shown.length > 0 && (
        <ul className="divide-border border-border bg-surface-raised divide-y rounded-lg border px-5">
          {shown.map((activity) => (
            <li key={activity.activityId}>
              <ActivityRow activity={activity} withCourse />
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
