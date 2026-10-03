import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useCourseQuizzesQuery, useQuestionsQuery } from '@/features/authoring/quiz-queries'
import { useCourseTasksQuery } from '@/features/authoring/task-queries'
import { useAtRiskQuery, useCourseLessonsQuery, useCourseRosterQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatRelativeTime } from '@/shared/lib/dates'
import { Badge } from '@/shared/ui/badge'
import { BarChart, type BarTone } from '@/shared/ui/bar-chart'
import { Card, CardContent } from '@/shared/ui/card'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Stat } from '@/shared/ui/stat'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import {
  useCompletionQuery,
  useLessonAnalyticsQuery,
  useQuestionAnalyticsQuery,
  useQuizAnalyticsQuery,
  useTaskAnalyticsQuery,
} from './queries'

const percent = (value: number | undefined) => `${Math.round(value ?? 0)} %`

/** Green from 70 %, amber from 40 %, red below: how a share reads at a glance (and in words beside it). */
function toneFor(share: number): BarTone {
  if (share >= 70) return 'success'
  if (share >= 40) return 'warning'
  return 'danger'
}

type Titled = {
  id?: string
  title?: string
  code?: string
  translations?: readonly { locale?: string; title?: string }[]
}

function titleOf(item: Titled | undefined, locale: string): string {
  if (item === undefined) return '—'
  return item.translations?.find((row) => row.locale === locale)?.title ?? item.title ?? item.code ?? '—'
}

/**
 * T-09 — how the course goes: the completion funnel, each lesson's completion, each quiz with an
 * item analysis of its questions, each coding task's acceptance, and the students falling behind.
 * Every chart says in a sentence what it shows and has its numbers as a table.
 */
export function CourseAnalytics({ courseId }: { courseId: string }) {
  return (
    <div className="grid grid-cols-1 gap-6">
      <CompletionFunnel courseId={courseId} />
      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-2">
        <LessonEngagement courseId={courseId} />
        <TaskStatistics courseId={courseId} />
      </div>
      <QuizAnalysis courseId={courseId} />
      <AtRiskList courseId={courseId} />
    </div>
  )
}

function Loading({
  query,
}: {
  query: { isPending: boolean; isError: boolean; error: unknown; refetch: () => unknown }
}) {
  if (query.isPending) return <Skeleton className="h-48" />
  if (query.isError) {
    return (
      <ErrorState
        problem={toApiProblem(query.error)}
        onRetry={() => {
          void query.refetch()
        }}
      />
    )
  }
  return null
}

function CompletionFunnel({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const completion = useCompletionQuery(courseId)
  if (!completion.isSuccess) return <Loading query={completion} />
  const data = completion.data
  const enrolled = data.enrolledTotal ?? 0
  const started = (data.inProgressTotal ?? 0) + (data.completedTotal ?? 0)
  const completed = data.completedTotal ?? 0
  return (
    <Card className="min-w-0">
      <CardContent className="grid grid-cols-1 gap-6 pt-5">
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          <Stat label={t('analytics.enrolled')} value={enrolled} />
          <Stat label={t('analytics.started')} value={started} />
          <Stat label={t('analytics.avgCompletion')} value={percent(data.avgCompletionPercent)} />
          <Stat
            label={t('analytics.avgScore')}
            value={data.avgFinalScore === undefined ? '—' : Math.round(data.avgFinalScore * 10) / 10}
          />
        </div>
        <BarChart
          title={t('analytics.funnel')}
          summary={t('analytics.funnelSummary', { enrolled, started, completed })}
          max={Math.max(1, enrolled)}
          columns={[t('analytics.stage'), t('analytics.students')]}
          data={[
            { key: 'enrolled', label: t('analytics.enrolled'), value: enrolled, display: String(enrolled) },
            {
              key: 'started',
              label: t('analytics.started'),
              value: started,
              display: String(started),
              tone: 'info',
            },
            {
              key: 'completed',
              label: t('analytics.completed'),
              value: completed,
              display: String(completed),
              tone: 'success',
            },
          ]}
        />
      </CardContent>
    </Card>
  )
}

function LessonEngagement({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const lessons = useLessonAnalyticsQuery(courseId)
  const titles = useCourseLessonsQuery(courseId, locale)
  if (!lessons.isSuccess) return <Loading query={lessons} />
  const order = new Map((titles.data ?? []).map((lesson, index) => [lesson.id, index]))
  const known = lessons.data.filter((row) => row.lessonId !== undefined && order.has(row.lessonId))
  // a lesson nobody has opened yet has no share to draw: it is counted in a sentence instead
  const untouched = known.filter((row) => (row.trackedStudents ?? 0) === 0).length
  const rows = known
    .filter((row) => (row.trackedStudents ?? 0) > 0)
    .sort((a, b) => (order.get(a.lessonId ?? '') ?? 0) - (order.get(b.lessonId ?? '') ?? 0))
  const share = (row: (typeof rows)[number]) =>
    (row.trackedStudents ?? 0) === 0 ? 0 : ((row.completedStudents ?? 0) / (row.trackedStudents ?? 1)) * 100
  const weakest = rows.slice().sort((a, b) => share(a) - share(b))[0]
  return (
    <Card className="min-w-0">
      <CardContent className="pt-5">
        {rows.length === 0 ? (
          <EmptyState title={t('analytics.lessons')} body={t('analytics.noData')} />
        ) : (
          <BarChart
            title={t('analytics.lessons')}
            summary={[
              weakest === undefined
                ? ''
                : t('analytics.lessonsSummary', {
                    title: titles.data?.find((lesson) => lesson.id === weakest.lessonId)?.title ?? '',
                    percent: Math.round(share(weakest)),
                  }),
              untouched > 0 ? t('analytics.lessonsUntouched', { count: untouched }) : '',
            ]
              .filter(Boolean)
              .join(' ')}
            max={100}
            columns={[t('analytics.lesson'), t('analytics.completedShare')]}
            data={rows.map((row) => ({
              key: row.lessonId ?? '',
              label: titles.data?.find((lesson) => lesson.id === row.lessonId)?.title ?? '',
              value: share(row),
              display: `${row.completedStudents ?? 0} / ${row.trackedStudents ?? 0}`,
              tone: toneFor(share(row)),
            }))}
          />
        )}
      </CardContent>
    </Card>
  )
}

function TaskStatistics({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const stats = useTaskAnalyticsQuery(courseId)
  const tasks = useCourseTasksQuery(courseId)
  if (!stats.isSuccess) return <Loading query={stats} />
  const rows = stats.data.filter((row) => (row.studentsAttempted ?? 0) > 0)
  const hardest = rows.slice().sort((a, b) => (a.acceptancePercent ?? 0) - (b.acceptancePercent ?? 0))[0]
  const title = (id: string | undefined) =>
    titleOf(
      tasks.data?.find((task) => task.id === id),
      locale,
    )
  return (
    <Card className="min-w-0">
      <CardContent className="pt-5">
        {rows.length === 0 ? (
          <EmptyState title={t('analytics.tasks')} body={t('analytics.noData')} />
        ) : (
          <BarChart
            title={t('analytics.tasks')}
            summary={
              hardest === undefined
                ? ''
                : t('analytics.tasksSummary', {
                    title: title(hardest.codingTaskId),
                    percent: Math.round(hardest.acceptancePercent ?? 0),
                    attempts: Math.round((hardest.avgAttemptsUsed ?? 0) * 10) / 10,
                  })
            }
            max={100}
            columns={[t('analytics.task'), t('analytics.acceptance')]}
            data={rows.map((row) => ({
              key: row.codingTaskId ?? '',
              label: title(row.codingTaskId),
              value: row.acceptancePercent ?? 0,
              display: t('analytics.acceptanceValue', {
                percent: Math.round(row.acceptancePercent ?? 0),
                accepted: row.acceptedTotal ?? 0,
                students: row.studentsAttempted ?? 0,
              }),
              tone: toneFor(row.acceptancePercent ?? 0),
            }))}
          />
        )}
      </CardContent>
    </Card>
  )
}

function QuizAnalysis({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const stats = useQuizAnalyticsQuery(courseId)
  const quizzes = useCourseQuizzesQuery(courseId)
  const attempted = (stats.data ?? []).filter((row) => (row.attemptsTotal ?? 0) > 0)
  const [quizId, setQuizId] = useState<string>()
  const chosen = quizId ?? attempted[0]?.quizId
  const items = useQuestionAnalyticsQuery(chosen)
  const questions = useQuestionsQuery(chosen ?? '')
  if (!stats.isSuccess) return <Loading query={stats} />
  const title = (id: string | undefined) =>
    titleOf(
      quizzes.data?.find((quiz) => quiz.id === id),
      locale,
    )
  const ordered = (questions.data ?? []).map((question) => ({
    question,
    item: items.data?.find((row) => row.questionId === question.id),
  }))
  const hardest = ordered
    .filter((row) => (row.item?.responses ?? 0) > 0)
    .sort((a, b) => (a.item?.correctPercent ?? 0) - (b.item?.correctPercent ?? 0))[0]

  return (
    <Card className="min-w-0">
      <CardContent className="grid grid-cols-1 gap-6 pt-5">
        {attempted.length === 0 ? (
          <EmptyState title={t('analytics.quizzes')} body={t('analytics.noData')} />
        ) : (
          <>
            <BarChart
              title={t('analytics.quizzes')}
              summary={t('analytics.quizzesSummary', { count: attempted.length })}
              max={100}
              columns={[t('analytics.quiz'), t('analytics.avgPercent')]}
              data={attempted.map((row) => ({
                key: row.quizId ?? '',
                label: title(row.quizId),
                value: row.avgPercent ?? 0,
                display: t('analytics.quizValue', {
                  percent: Math.round(row.avgPercent ?? 0),
                  passed: row.attemptsPassed ?? 0,
                  total: row.attemptsTotal ?? 0,
                }),
                tone: toneFor(row.avgPercent ?? 0),
              }))}
            />
            <div className="grid grid-cols-1 gap-3">
              <Select
                className="w-80 max-w-full"
                label={t('analytics.itemAnalysisOf')}
                value={chosen}
                onValueChange={setQuizId}
                options={attempted.map((row) => ({ value: row.quizId ?? '', label: title(row.quizId) }))}
              />
              {items.isPending || questions.isPending ? (
                <Skeleton className="h-40" />
              ) : (
                <BarChart
                  title={t('analytics.itemAnalysis')}
                  summary={
                    hardest === undefined
                      ? t('analytics.noData')
                      : t('analytics.itemSummary', {
                          number: ordered.indexOf(hardest) + 1,
                          percent: Math.round(hardest.item?.correctPercent ?? 0),
                        })
                  }
                  max={100}
                  columns={[t('analytics.question'), t('analytics.correctShare')]}
                  data={ordered.map(({ question, item }, index) => ({
                    key: question.id ?? String(index),
                    label: `${index + 1}. ${question.prompt ?? ''}`,
                    value: item?.correctPercent ?? 0,
                    display:
                      (item?.responses ?? 0) === 0
                        ? t('analytics.noAnswers')
                        : `${Math.round(item?.correctPercent ?? 0)} % · ${item?.correctResponses ?? 0} / ${item?.responses ?? 0}`,
                    tone: toneFor(item?.correctPercent ?? 0),
                  }))}
                />
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}

function AtRiskList({ courseId }: { courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const atRisk = useAtRiskQuery(courseId)
  const roster = useCourseRosterQuery(courseId)
  if (!atRisk.isSuccess) return <Loading query={atRisk} />
  const name = (id: string) => roster.data?.find((row) => row.studentId === id)?.fullName ?? '—'
  return (
    <Card className="min-w-0">
      <CardContent className="grid grid-cols-1 gap-3 pt-5">
        <div className="grid grid-cols-1 gap-0.5">
          <h3 className="font-display text-title text-text font-semibold">{t('analytics.atRisk')}</h3>
          <p className="text-small text-text-muted">
            {t('analytics.atRiskSummary', { count: atRisk.data.length })}
          </p>
        </div>
        {atRisk.data.length === 0 ? (
          <EmptyState title={t('analytics.nobodyAtRisk')} />
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>{t('gradebook.student')}</TableHeaderCell>
                  <TableHeaderCell numeric>{t('analytics.completion')}</TableHeaderCell>
                  <TableHeaderCell numeric>{t('analytics.failedQuizzes')}</TableHeaderCell>
                  <TableHeaderCell numeric>{t('analytics.failedSubmissions')}</TableHeaderCell>
                  <TableHeaderCell>{t('analytics.lastSeen')}</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {atRisk.data.map((student) => (
                  <TableRow key={student.studentId}>
                    <TableCell>
                      {name(student.studentId)}
                      {student.academicDebt && (
                        <Badge tone="danger" className="ml-2">
                          {t('gradebook.debt')}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell numeric>{percent(student.completionPercent)}</TableCell>
                    <TableCell numeric>{student.failedQuizAttempts}</TableCell>
                    <TableCell numeric>{student.unsuccessfulSubmissions}</TableCell>
                    <TableCell>
                      {student.lastAccessedAt === undefined
                        ? t('analytics.never')
                        : formatRelativeTime(student.lastAccessedAt, locale)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
