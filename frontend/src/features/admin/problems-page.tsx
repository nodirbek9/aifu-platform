import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import {
  ADMIN_PROBLEM_TYPES,
  getProblemAction,
  readProblemType,
  type AdminProblemView,
} from './problems-model'
import { useAdminProblemsQuery } from './problems-queries'

/** 7.4-F2 — server-owned problems, with URL filters and exact fix destinations. */
export function ProblemsPage() {
  const { t } = useTranslation()
  const problems = useAdminProblemsQuery()

  return (
    <section aria-label={t('nav.adminProblems')} className="grid grid-cols-1 gap-6">
      <PageHeader title={t('admin.problems.title')} />
      <Select
        className="w-full md:max-w-md"
        label={t('assignments.filterType')}
        value={problems.type ?? 'all'}
        options={[
          { value: 'all', label: t('admin.problems.allTypes') },
          ...ADMIN_PROBLEM_TYPES.map((type) => ({ value: type, label: t(`admin.problems.type.${type}`) })),
        ]}
        onValueChange={(value) => {
          problems.updateParams({ type: readProblemType(value) ?? null })
        }}
      />
      {problems.isPending && (
        <div aria-label={t('common.loading')} className="grid grid-cols-1 gap-3">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
        </div>
      )}
      {problems.isError && (
        <ErrorState
          problem={toApiProblem(problems.error)}
          onRetry={() => {
            void problems.refetch()
          }}
        />
      )}
      {problems.isSuccess && (
        <div aria-busy={problems.isFetching} className="grid grid-cols-1 gap-4">
          <p role="status" className="text-small text-text-muted tabular-nums">
            {t('admin.problems.count', { count: problems.data.totalElements })}
          </p>
          {problems.data.content.length === 0 ? (
            <EmptyState
              title={t('admin.problems.empty')}
              body={t('admin.problems.emptyBody')}
              action={
                <Link to="/admin/users" className={button({ variant: 'outline' })}>
                  {t('admin.problems.reviewUsers')}
                </Link>
              }
            />
          ) : (
            <ul className="grid grid-cols-1 gap-3">
              {problems.data.content.map((problem) => (
                <li key={problem.problemKey} className="min-w-0">
                  <ProblemRow problem={problem} />
                </li>
              ))}
            </ul>
          )}
          <Pagination
            page={problems.data.page}
            totalPages={problems.data.totalPages}
            onPageChange={(page) => {
              if (!problems.isPlaceholderData) problems.updateParams({ page })
            }}
          />
        </div>
      )}
    </section>
  )
}

function ProblemRow({ problem }: { problem: AdminProblemView }) {
  const { t } = useTranslation()
  const action = getProblemAction(problem)
  const identity = [
    problem.fullName,
    problem.groupCode,
    problem.subjectCode,
    problem.cohortCode,
    problem.termName,
  ]
    .filter((value) => value?.trim())
    .join(' · ')
  const actionLabel =
    action.label === 'admin.problems.fix'
      ? t('admin.problems.fix')
      : action.label === 'admin.problems.reviewUsers'
        ? t('admin.problems.reviewUsers')
        : action.label === 'admin.problems.reviewAttempt'
          ? t('admin.problems.reviewAttempt')
          : t('admin.problems.actionUnavailable')

  return (
    <Card>
      <CardHeader
        title={t(`admin.problems.type.${problem.type}`)}
        action={
          action.type === 'navigate' ? (
            <Link to={action.path} className={button({ variant: 'outline' })}>
              {actionLabel}
            </Link>
          ) : (
            <p className="text-small text-text-muted">{actionLabel}</p>
          )
        }
      />
      <CardContent className="text-small text-text-muted wrap-anywhere">
        {identity ||
          problem.userId ||
          problem.groupId ||
          problem.courseId ||
          t('admin.problems.actionUnavailable')}
      </CardContent>
    </Card>
  )
}
