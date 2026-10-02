import { useTranslation } from 'react-i18next'
import { useAuth } from '@/features/auth/auth-context'
import { useEnrollmentsQuery } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatLongDay, useCampusNow } from '@/shared/lib/dates'
import { ErrorState } from '@/shared/ui/error-state'
import { ContinueBlock } from './continue-block'
import { TodayBlock } from './today-block'
import { DeadlinesBlock, RecentGradesBlock } from './work-blocks'

/**
 * S-01 — what the student sees first. The day is the hero: today's classes; beside it, where
 * to continue; below, what is due and what was graded. Each block loads and fails on its own.
 */
export function StudentDashboardPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { state } = useAuth()
  const now = useCampusNow()
  const enrollments = useEnrollmentsQuery()

  if (state.status !== 'authenticated') return null
  const { user } = state
  const identity = [user.groupCode, user.studentNumber].filter((part) => part !== undefined).join(' · ')

  return (
    <div className="grid gap-8">
      <header className="grid gap-1.5">
        <p className="text-caption text-text-muted font-medium uppercase">
          {formatLongDay(now.date, locale)}
        </p>
        <h1 className="font-display text-display-lg text-text md:text-display-xl font-semibold">
          {user.fullName}
        </h1>
        {identity !== '' && <p className="text-small text-text-muted">{identity}</p>}
      </header>

      <div className="grid items-start gap-6 lg:grid-cols-12">
        <section aria-label={t('dashboard.todayTitle')} className="lg:col-span-7">
          <TodayBlock />
        </section>
        <section aria-label={t('dashboard.continueTitle')} className="lg:col-span-5">
          {enrollments.isError ? (
            <ErrorState
              problem={toApiProblem(enrollments.error)}
              onRetry={() => {
                void enrollments.refetch()
              }}
            />
          ) : (
            <ContinueBlock enrollments={enrollments.data} />
          )}
        </section>
        <section aria-label={t('dashboard.deadlinesTitle')} className="lg:col-span-7">
          <DeadlinesBlock />
        </section>
        <section aria-label={t('dashboard.gradesTitle')} className="lg:col-span-5">
          {!enrollments.isError && <RecentGradesBlock enrollments={enrollments.data} />}
        </section>
      </div>
    </div>
  )
}
