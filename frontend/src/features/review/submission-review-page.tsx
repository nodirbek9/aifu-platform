import { lazy, Suspense, useState } from 'react'
import { CheckCircle2, Send, XCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams, useSearchParams } from 'react-router'
import { useTaskQuery } from '@/features/authoring/task-queries'
import { formatScore } from '@/features/grades/model'
import { useCourseGradebookQuery, useRecordGrade } from '@/features/gradebook/queries'
import { useTeacherCoursesQuery } from '@/features/teaching/queries'
import { toApiProblem } from '@/shared/api/errors'
import { isEditorLanguage, languageName } from '@/shared/editor/languages'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Badge, VerdictBadge, VERDICTS, type Verdict } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { RichText } from '@/shared/ui/rich-text'
import { SegmentedControl } from '@/shared/ui/segmented-control'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import {
  useSubmissionFeedbackQuery,
  useTeacherSubmissionQuery,
  useWriteFeedback,
  type TeacherSubmission,
  type Visibility,
} from './queries'

const CodeEditor = lazy(async () => ({ default: (await import('@/shared/editor/code-editor')).CodeEditor }))

function isVerdict(value: string | undefined): value is Verdict {
  return value !== undefined && (VERDICTS as readonly string[]).includes(value)
}

/**
 * T-07 — one submission as the teacher reviews it: the code (read-only), every test with its
 * input and output (hidden ones too), the feedback written so far and a form for more, and the
 * grade for the course's gradebook — so the student sees both (spec §9, step 6).
 */
export function SubmissionReviewPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { taskId = '', submissionId = '' } = useParams()
  const [search] = useSearchParams()
  const studentId = search.get('student') ?? undefined
  const submission = useTeacherSubmissionQuery(taskId, submissionId)
  const task = useTaskQuery(taskId)
  const courses = useTeacherCoursesQuery()
  const courseId = task.data?.courseId
  const gradebook = useCourseGradebookQuery(courseId ?? '', locale)
  const student = gradebook.data?.find((row) => row.studentId === studentId)
  const courseTitle = courses.data?.find((item) => item.id === courseId)?.title ?? ''

  if (submission.isPending || task.isPending) return <Skeleton className="h-[32rem]" />
  if (submission.isError || task.isError) {
    return (
      <ErrorState
        problem={toApiProblem(submission.error ?? task.error)}
        onRetry={() => {
          void submission.refetch()
          void task.refetch()
        }}
      />
    )
  }
  const detail = submission.data
  const language = detail.languageCode ?? 'python'

  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[
          { label: t('nav.teacherClasses'), to: '/teacher/courses' },
          { label: courseTitle, to: `/teacher/courses/${courseId ?? ''}?tab=review` },
          { label: t('review.submissionOf', { name: student?.fullName ?? '' }) },
        ]}
      />
      <header className="grid grid-cols-1 gap-2">
        <h1 className="font-display text-display-lg text-text font-semibold">
          {student?.fullName ?? t('review.submission')}
        </h1>
        <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
          <span>{task.data.title}</span>
          <span>·</span>
          <span>{languageName(language)}</span>
          <span>·</span>
          <span>{t('review.attempt', { number: detail.attemptNumber ?? 1 })}</span>
          {detail.submittedAt !== undefined && (
            <>
              <span>·</span>
              <span>{formatDateTime(detail.submittedAt, locale)}</span>
            </>
          )}
          {isVerdict(detail.verdict) && (
            <VerdictBadge verdict={detail.verdict} label={t(`verdict.${detail.verdict}`)} />
          )}
          {detail.score !== undefined && (
            <span className="text-text font-semibold tabular-nums">
              {formatScore(detail.score)} / {formatScore(detail.maxScore ?? 100)}
            </span>
          )}
        </p>
      </header>

      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-12">
        <div className="grid min-w-0 grid-cols-1 gap-6 xl:col-span-7">
          <Card>
            <CardHeader title={t('review.code')} />
            <CardContent>
              <Suspense fallback={<Skeleton className="h-80" />}>
                <CodeEditor
                  aria-label={t('review.code')}
                  value={detail.sourceCode ?? ''}
                  language={isEditorLanguage(language) ? language : 'python'}
                  readOnly
                  className="border-border h-96 rounded-md border"
                />
              </Suspense>
            </CardContent>
          </Card>
          <TestResults submission={detail} />
        </div>
        <div className="grid grid-cols-1 gap-6 xl:col-span-5">
          <FeedbackPanel submissionId={submissionId} />
          {courseId !== undefined && student !== undefined && (
            <GradeForm
              courseId={courseId}
              enrollmentId={student.book.enrollmentId}
              components={student.book.components}
              suggestion={detail.score}
              suggestionMax={detail.maxScore}
            />
          )}
        </div>
      </div>
    </div>
  )
}

function TestResults({ submission }: { submission: TeacherSubmission }) {
  const { t } = useTranslation()
  const tests = submission.testResults ?? []
  return (
    <Card>
      <CardHeader
        title={t('review.tests')}
        description={
          submission.testsTotal === undefined
            ? undefined
            : t('authoring.testsPassed', {
                passed: submission.testsPassed ?? 0,
                total: submission.testsTotal,
              })
        }
      />
      <CardContent className="grid grid-cols-1 gap-3">
        {submission.compileOutput !== undefined && submission.compileOutput !== '' && (
          <pre className="bg-surface-sunken text-small text-danger max-h-60 overflow-auto rounded-md p-3 font-mono">
            {submission.compileOutput}
          </pre>
        )}
        {tests.map((test, index) => (
          <details
            key={test.testCaseId ?? index}
            className="border-border group rounded-md border"
            open={test.passed !== true && index < 3}
          >
            <summary className="text-small flex cursor-pointer items-center gap-2 px-3 py-2">
              {test.passed === true ? (
                <CheckCircle2 aria-label={t('authoring.passed')} className="text-success size-4" />
              ) : (
                <XCircle aria-label={t('authoring.failed')} className="text-danger size-4" />
              )}
              <span className="text-text font-medium">
                {test.name !== undefined && test.name !== ''
                  ? test.name
                  : t('authoring.testNumber', { number: index + 1 })}
              </span>
              {test.isExample === true && <Badge tone="accent">{t('authoring.example')}</Badge>}
              {test.passed !== true && test.verdict !== undefined && (
                <span className="text-caption text-text-muted">{test.verdict}</span>
              )}
              {test.timeMs !== undefined && (
                <span className="text-caption text-text-muted ml-auto tabular-nums">{test.timeMs} ms</span>
              )}
            </summary>
            <div className="grid grid-cols-1 gap-2 px-3 pb-3 md:grid-cols-3">
              {(
                [
                  ['stdin', test.stdin],
                  ['expectedStdout', test.expectedStdout],
                  ['actualStdout', test.actualStdout],
                ] as const
              ).map(([key, value]) => (
                <div key={key} className="grid min-w-0 grid-cols-1 gap-1">
                  <span className="text-caption text-text-muted font-medium uppercase">
                    {t(`review.${key}`)}
                  </span>
                  <pre className="bg-surface-sunken text-small text-text max-h-40 overflow-auto rounded-sm p-2 font-mono">
                    {value ?? ''}
                  </pre>
                </div>
              ))}
              {test.actualStderr !== undefined && test.actualStderr !== '' && (
                <pre className="bg-danger-soft text-small text-danger max-h-40 overflow-auto rounded-sm p-2 font-mono md:col-span-3">
                  {test.actualStderr}
                </pre>
              )}
            </div>
          </details>
        ))}
      </CardContent>
    </Card>
  )
}

function FeedbackPanel({ submissionId }: { submissionId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()
  const feedback = useSubmissionFeedbackQuery(submissionId)
  const write = useWriteFeedback(submissionId)
  const [body, setBody] = useState('')
  const [visibility, setVisibility] = useState<Visibility>('STUDENT_VISIBLE')

  return (
    <Card>
      <CardHeader title={t('review.feedback')} />
      <CardContent className="grid grid-cols-1 gap-4">
        {feedback.isPending && <Skeleton className="h-16" />}
        {feedback.data?.map((item) => (
          <article key={item.id} className="border-border grid grid-cols-1 gap-1 rounded-md border px-3 py-2">
            <p className="text-caption text-text-muted flex items-center gap-2 tracking-normal">
              {item.createdAt !== undefined && formatDateTime(item.createdAt, locale)}
              <Badge tone={item.visibility === 'INTERNAL' ? 'neutral' : 'info'}>
                {t(`review.visibility.${item.visibility ?? 'STUDENT_VISIBLE'}`)}
              </Badge>
            </p>
            <RichText html={item.bodySanitizedHtml ?? ''} className="text-small" />
          </article>
        ))}
        <form
          className="grid grid-cols-1 gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            if (body.trim() === '') return
            write.mutate(
              { body: body.trim(), visibility },
              {
                onSuccess: () => {
                  setBody('')
                  toast({ tone: 'success', title: t('review.feedbackSent') })
                },
              },
            )
          }}
        >
          <Textarea
            label={t('review.writeFeedback')}
            value={body}
            rows={5}
            onChange={(event) => {
              setBody(event.target.value)
            }}
          />
          <SegmentedControl
            size="sm"
            aria-label={t('review.whoSees')}
            value={visibility}
            onValueChange={(value) => {
              setVisibility(value === 'INTERNAL' ? 'INTERNAL' : 'STUDENT_VISIBLE')
            }}
            options={[
              { value: 'STUDENT_VISIBLE', label: t('review.visibility.STUDENT_VISIBLE') },
              { value: 'INTERNAL', label: t('review.visibility.INTERNAL') },
            ]}
          />
          {write.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(write.error).code}`)}
            </p>
          )}
          <Button
            type="submit"
            className="justify-self-start"
            disabled={body.trim() === ''}
            loading={write.isPending}
          >
            <Send aria-hidden className="size-4" />
            {t('review.send')}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}

type GradeFormProps = {
  courseId: string
  enrollmentId: string
  components: readonly { id: string; code: string; maxPoints: number }[]
  suggestion: number | undefined
  suggestionMax: number | undefined
}

function GradeForm({ courseId, enrollmentId, components, suggestion, suggestionMax }: GradeFormProps) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const record = useRecordGrade(courseId)
  const [componentId, setComponentId] = useState(components[0]?.id)
  const component = components.find((item) => item.id === componentId)
  const [score, setScore] = useState(() =>
    suggestion !== undefined && suggestionMax !== undefined && component !== undefined && suggestionMax > 0
      ? formatScore((suggestion / suggestionMax) * component.maxPoints)
      : '',
  )
  const value = Number(score)
  const valid = component !== undefined && score !== '' && value >= 0 && value <= component.maxPoints

  return (
    <Card>
      <CardHeader title={t('review.gradeTitle')} description={t('review.gradeHint')} />
      <CardContent>
        <form
          className="grid grid-cols-1 gap-3"
          onSubmit={(event) => {
            event.preventDefault()
            if (!valid) return
            record.mutate(
              { enrollmentId, componentId: component.id, score: value, maxScore: component.maxPoints },
              {
                onSuccess: () => {
                  toast({ tone: 'success', title: t('review.gradeSaved') })
                },
              },
            )
          }}
        >
          <Select
            label={t('review.component')}
            value={componentId}
            onValueChange={setComponentId}
            options={components.map((item) => ({
              value: item.id,
              label: `${item.code} · ${t('gradebook.outOf', { max: formatScore(item.maxPoints) })}`,
            }))}
          />
          <Input
            label={t('gradebook.score')}
            type="number"
            min={0}
            step="any"
            inputMode="decimal"
            value={score}
            onChange={(event) => {
              setScore(event.target.value)
            }}
          />
          {record.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(record.error).code}`)}
            </p>
          )}
          <Button type="submit" className="justify-self-start" disabled={!valid} loading={record.isPending}>
            {t('gradebook.save')}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
