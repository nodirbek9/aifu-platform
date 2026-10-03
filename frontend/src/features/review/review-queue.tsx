import { useState } from 'react'
import { MessageSquareText } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { useCourseTasksQuery } from '@/features/authoring/task-queries'
import { useCourseGradebookQuery } from '@/features/gradebook/queries'
import { toApiProblem } from '@/shared/api/errors'
import { languageName } from '@/shared/editor/languages'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge, VerdictBadge, VERDICTS, type Verdict } from '@/shared/ui/badge'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Switch } from '@/shared/ui/switch'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useReviewQueueQuery, type ReviewFilter } from './queries'

const REVIEW_VERDICTS: readonly Verdict[] = [
  'ACCEPTED',
  'PARTIAL',
  'WRONG_ANSWER',
  'COMPILATION_ERROR',
  'RUNTIME_ERROR',
  'TIME_LIMIT_EXCEEDED',
]

function isVerdict(value: string | undefined): value is Verdict {
  return value !== undefined && (VERDICTS as readonly string[]).includes(value)
}

/** T-07 — the submissions of a course to review: by task, verdict and group; the ones still without a word first. */
export function ReviewQueue({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const tasks = useCourseTasksQuery(courseId)
  const gradebook = useCourseGradebookQuery(courseId, locale)
  const [filter, setFilter] = useState<ReviewFilter>({ needsFeedback: true, latestOnly: true, page: 0 })
  const queue = useReviewQueueQuery(courseId, filter, locale)
  const groups = [
    ...new Set((gradebook.data ?? []).flatMap((row) => (row.groupCode === undefined ? [] : [row.groupCode]))),
  ].sort()
  const change = (patch: Partial<ReviewFilter>) => {
    setFilter({ ...filter, ...patch, page: 0 })
  }
  const choose = (value: string) => (value === 'all' ? undefined : value)

  return (
    <div className="grid grid-cols-1 gap-4">
      <div className="flex flex-wrap items-end gap-3">
        <Select
          className="w-64"
          label={t('review.task')}
          value={filter.taskId ?? 'all'}
          onValueChange={(value) => {
            change({ taskId: choose(value) })
          }}
          options={[
            { value: 'all', label: t('review.allTasks') },
            ...(tasks.data ?? []).map((task) => ({
              value: task.id ?? '',
              label: task.title ?? task.code ?? '',
            })),
          ]}
        />
        <Select
          className="w-52"
          label={t('review.verdict')}
          value={filter.verdict ?? 'all'}
          onValueChange={(value) => {
            change({ verdict: choose(value) })
          }}
          options={[
            { value: 'all', label: t('review.allVerdicts') },
            ...REVIEW_VERDICTS.map((verdict) => ({ value: verdict, label: t(`verdict.${verdict}`) })),
          ]}
        />
        <Select
          className="w-44"
          label={t('profile.group')}
          value={filter.groupCode ?? 'all'}
          onValueChange={(value) => {
            change({ groupCode: choose(value) })
          }}
          options={[
            { value: 'all', label: t('gradebook.allGroups') },
            ...groups.map((code) => ({ value: code, label: code })),
          ]}
        />
        <div className="grid grid-cols-1 gap-2 pb-1">
          <Switch
            label={t('review.needsFeedback')}
            checked={filter.needsFeedback}
            onCheckedChange={(checked) => {
              change({ needsFeedback: checked })
            }}
          />
          <Switch
            label={t('review.latestOnly')}
            checked={filter.latestOnly}
            onCheckedChange={(checked) => {
              change({ latestOnly: checked })
            }}
          />
        </div>
      </div>

      {queue.isPending && <Skeleton className="h-64" />}
      {queue.isError && (
        <ErrorState
          problem={toApiProblem(queue.error)}
          onRetry={() => {
            void queue.refetch()
          }}
        />
      )}
      {queue.isSuccess && (queue.data.content ?? []).length === 0 && (
        <EmptyState
          icon={<MessageSquareText aria-hidden className="size-6" />}
          title={filter.needsFeedback ? t('review.allReviewed') : t('review.empty')}
          body={filter.needsFeedback ? t('review.allReviewedBody') : t('review.emptyBody')}
        />
      )}
      {queue.isSuccess && (queue.data.content ?? []).length > 0 && (
        <>
          <p className="text-small text-text-muted" role="status">
            {t('review.count', { count: queue.data.totalElements ?? 0 })}
          </p>
          <div className="border-border overflow-x-auto rounded-lg border">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('gradebook.student')}</TableHeaderCell>
                  <TableHeaderCell>{t('review.task')}</TableHeaderCell>
                  <TableHeaderCell>{t('review.language')}</TableHeaderCell>
                  <TableHeaderCell>{t('review.verdict')}</TableHeaderCell>
                  <TableHeaderCell numeric>{t('review.score')}</TableHeaderCell>
                  <TableHeaderCell>{t('review.submitted')}</TableHeaderCell>
                  <TableHeaderCell>{t('review.feedback')}</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(queue.data.content ?? []).map((item) => (
                  <TableRow key={item.submissionId}>
                    <TableCell>
                      <Link
                        to={`/teacher/coding-tasks/${item.codingTaskId ?? ''}/submissions/${item.submissionId ?? ''}?student=${item.studentUserId ?? ''}`}
                        className="text-text hover:text-primary-hover font-medium underline-offset-4 hover:underline"
                      >
                        {item.studentFullName}
                      </Link>
                      {item.groupCode !== undefined && (
                        <span className="text-caption text-text-muted block">{item.groupCode}</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {item.taskTitle}
                      <span className="text-caption text-text-muted block">
                        {t('review.attempt', { number: item.attemptNumber ?? 1 })}
                      </span>
                    </TableCell>
                    <TableCell>{languageName(item.languageCode ?? '')}</TableCell>
                    <TableCell>
                      {isVerdict(item.verdict) ? (
                        <VerdictBadge verdict={item.verdict} label={t(`verdict.${item.verdict}`)} />
                      ) : (
                        <Badge tone="info">
                          {t(`review.status.${item.status === 'FAILED' ? 'FAILED' : 'PENDING'}`)}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell numeric>
                      {item.score === undefined ? '—' : `${item.score} / ${item.maxScore ?? 100}`}
                    </TableCell>
                    <TableCell className="whitespace-nowrap">
                      {item.submittedAt === undefined ? '' : formatRelativeTime(item.submittedAt, locale)}
                    </TableCell>
                    <TableCell>
                      {item.hasFeedback === true ? (
                        <Badge tone="success">{t('review.written')}</Badge>
                      ) : (
                        <Badge tone="warning">{t('review.waiting')}</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <Pagination
            page={filter.page}
            totalPages={queue.data.totalPages ?? 1}
            onPageChange={(page) => {
              setFilter({ ...filter, page })
            }}
          />
        </>
      )}
    </div>
  )
}
