import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSearchParams } from 'react-router'
import { textIn } from '@/features/authoring/model'
import { useCurriculumTreeQuery } from '@/features/authoring/queries'
import { useCourseQuizzesQuery } from '@/features/authoring/quiz-queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { formatDateTime } from '@/shared/lib/dates'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from '@/shared/ui/dialog'
import { EmptyState } from '@/shared/ui/empty-state'
import { ErrorState } from '@/shared/ui/error-state'
import { Input } from '@/shared/ui/input'
import { Pagination } from '@/shared/ui/pagination'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Switch } from '@/shared/ui/switch'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import {
  buildLearningPathConfigRequest,
  isoToCampusDatetimeLocal,
  resolveGrantTarget,
  useAttemptGrantsQuery,
  useGrantAttempt,
  useLearningPathConfigQuery,
  useQuizDetailQuery,
  useSaveLearningPathConfig,
  validateGrantReason,
  type AttemptGrantView,
} from './learning-path-queries'
import {
  useAdminCourseQuery,
  useAdminEnrollmentQuery,
  useCourseEnrollmentsQuery,
  useCoursesQuery,
} from './structure-queries'

function ProblemAlert({ error }: { error: unknown }) {
  const { t } = useTranslation()
  if (error === null || error === undefined) return null
  const problem = toApiProblem(error)
  return (
    <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
      {t(`errors.${problem.code}`)}
      {problem.message !== '' && ` — ${problem.message}`}
    </p>
  )
}

export function LearningPathPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const [searchParams, setSearchParams] = useSearchParams()

  const courseParam = searchParams.get('course') ?? ''
  const enrollmentParam = searchParams.get('enrollment') ?? ''
  const quizParam = searchParams.get('quiz') ?? ''

  const coursesQuery = useCoursesQuery({ query: '', status: 'PUBLISHED', page: 0, size: 100 })
  const publishedCourses = coursesQuery.data?.content ?? []

  const directCourseQuery = useAdminCourseQuery(courseParam)
  const directCourse = directCourseQuery.data

  const directTitle =
    directCourse?.translations?.find((tr) => tr.locale === locale)?.title ??
    directCourse?.translations?.[0]?.title ??
    directCourse?.cohortCode ??
    ''

  const courseOptions = [...publishedCourses]
  if (directCourse?.id && !courseOptions.some((c) => c.id === directCourse.id)) {
    courseOptions.unshift({
      id: directCourse.id,
      subjectCode: directCourse.subjectCode,
      title: directTitle,
      status: directCourse.status,
    })
  }

  const selectedCourseId = courseParam !== '' ? courseParam : (publishedCourses[0]?.id ?? '')

  const selectedCourse = courseOptions.find((c) => c.id === selectedCourseId)
  const courseTitle = selectedCourse
    ? `${selectedCourse.subjectCode ?? ''} — ${selectedCourse.title ?? ''}`
    : directCourse?.id === selectedCourseId
      ? `${directCourse.subjectCode ?? ''} — ${directTitle}`
      : ''

  const handleCourseChange = (nextCourseId: string) => {
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        if (nextCourseId) {
          next.set('course', nextCourseId)
        } else {
          next.delete('course')
        }
        next.delete('enrollment')
        next.delete('quiz')
        return next
      },
      { replace: true },
    )
  }

  const isCourseLoading =
    coursesQuery.isPending || (courseParam !== '' && directCourseQuery.isPending && courseOptions.length === 0)
  const isCourseError =
    (courseParam !== '' && directCourseQuery.isError && courseOptions.length === 0) ||
    (courseParam === '' && coursesQuery.isError)
  const courseError =
    courseParam !== '' && directCourseQuery.isError ? directCourseQuery.error : coursesQuery.error

  return (
    <div className="grid grid-cols-1 gap-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-display-lg text-text font-semibold">
            {t('admin.learningPath.title')}
          </h1>
          <p className="text-small text-text-muted mt-1">{t('admin.learningPath.description')}</p>
        </div>

        <div className="w-full sm:w-80">
          <Select
            label={t('admin.learningPath.selectCourse')}
            value={selectedCourseId}
            onValueChange={handleCourseChange}
            options={courseOptions.map((c) => ({
              value: c.id ?? '',
              label: `${c.subjectCode ?? ''} — ${c.title ?? ''}`,
            }))}
            placeholder={t('authoring.choose')}
          />
        </div>
      </header>

      {isCourseLoading && <Skeleton className="h-96" />}

      {isCourseError && (
        <ErrorState
          problem={toApiProblem(courseError)}
          onRetry={() => {
            if (courseParam !== '') void directCourseQuery.refetch()
            else void coursesQuery.refetch()
          }}
        />
      )}

      {!isCourseLoading && !isCourseError && courseOptions.length === 0 && (
        <EmptyState title={t('admin.noCourses')} />
      )}

      {selectedCourseId !== '' && !isCourseError && (
        <>
          <LearningPathCard key={selectedCourseId} courseId={selectedCourseId} courseTitle={courseTitle} />
          <ExtraAttemptsSection
            courseId={selectedCourseId}
            initialEnrollmentId={enrollmentParam !== '' ? enrollmentParam : undefined}
            initialQuizId={quizParam !== '' ? quizParam : undefined}
          />
        </>
      )}
    </div>
  )
}

export function LearningPathCard({ courseId, courseTitle }: { courseId: string; courseTitle?: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()

  const configQuery = useLearningPathConfigQuery(courseId)
  const saveMutation = useSaveLearningPathConfig(courseId)
  const quizzesQuery = useCourseQuizzesQuery(courseId)
  const treeQuery = useCurriculumTreeQuery(courseId)

  const config = configQuery.data

  const [enabled, setEnabled] = useState(false)
  const [midtermActivityId, setMidtermActivityId] = useState<string>('')
  const [midtermAfterLessonId, setMidtermAfterLessonId] = useState<string>('')
  const [midtermOpensAtLocal, setMidtermOpensAtLocal] = useState<string>('')

  // Confirm dialog state for enabling
  const [confirmOpen, setConfirmOpen] = useState(false)

  // Sync state from server config (adjusting state while rendering, not in an effect)
  const [syncedConfig, setSyncedConfig] = useState(config)
  if (config !== syncedConfig) {
    setSyncedConfig(config)
    if (config) {
      setEnabled(config.enabled === true)
      setMidtermActivityId(config.midtermActivityId ?? '')
      setMidtermAfterLessonId(config.midtermAfterLessonId ?? '')
      setMidtermOpensAtLocal(isoToCampusDatetimeLocal(config.midtermOpensAt))
    }
  }

  const quizChoices = (quizzesQuery.data ?? []).map((q) => {
    const title = q.translations?.find((tr) => tr.locale === locale)?.title ?? q.translations?.[0]?.title
    return {
      value: q.id ?? '',
      label: title ? `${title} (${q.code ?? ''})` : (q.code ?? q.id ?? ''),
    }
  })
  if (midtermActivityId !== '' && !quizChoices.some((q) => q.value === midtermActivityId)) {
    quizChoices.unshift({ value: midtermActivityId, label: midtermActivityId })
  }

  const allLessons = (treeQuery.data?.modules ?? []).flatMap((m) => m.lessons)
  const lessonChoices: { value: string; label: string }[] = [
    { value: '', label: t('admin.learningPath.noAnchorLesson') },
    ...allLessons.map((l) => {
      const title = textIn(l.texts, locale)?.title ?? l.code ?? l.id
      return {
        value: l.id,
        label: l.code ? `${l.code}: ${title}` : title,
      }
    }),
  ]
  if (midtermAfterLessonId !== '' && !lessonChoices.some((l) => l.value === midtermAfterLessonId)) {
    lessonChoices.push({ value: midtermAfterLessonId, label: midtermAfterLessonId })
  }

  const handleToggle = (checked: boolean) => {
    if (checked && !enabled) {
      // Prompt confirm dialog when turning ON
      setConfirmOpen(true)
    } else {
      setEnabled(checked)
    }
  }

  const handleConfirmEnable = () => {
    setEnabled(true)
    setConfirmOpen(false)
  }

  const handleSave = () => {
    const request = buildLearningPathConfigRequest({
      enabled,
      midtermActivityId,
      midtermAfterLessonId,
      midtermOpensAtLocal,
    })

    saveMutation.mutate(request, {
      onSuccess: () => {
        toast({ tone: 'success', title: t('admin.saved') })
      },
    })
  }

  if (configQuery.isPending) {
    return <Skeleton className="h-64" />
  }

  if (configQuery.isError) {
    return (
      <ErrorState
        problem={toApiProblem(configQuery.error)}
        onRetry={() => {
          void configQuery.refetch()
        }}
      />
    )
  }

  return (
    <Card>
      <CardHeader
        title={
          courseTitle
            ? `${courseTitle} — ${t('admin.learningPath.cardTitle')}`
            : t('admin.learningPath.cardTitle')
        }
        description={t('admin.learningPath.enableSwitchDescription')}
      />
      <CardContent className="grid grid-cols-1 gap-6">
        {/* Enable / Disable switch row */}
        <div className="border-border flex flex-wrap items-center justify-between gap-4 rounded-lg border p-4">
          <div>
            <p className="text-text font-medium">{t('admin.learningPath.enableSwitchLabel')}</p>
            {config?.enabled && config?.enabledAt && (
              <p className="text-caption text-text-muted mt-1">
                {t('admin.learningPath.enabledAt', {
                  date: formatDateTime(config.enabledAt, locale),
                  by: config.enabledBy ?? '',
                })}
              </p>
            )}
          </div>
          <Switch
            checked={enabled}
            onCheckedChange={handleToggle}
            label={<span className="sr-only">{t('admin.learningPath.enableSwitchLabel')}</span>}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <Select
              label={t('admin.learningPath.midtermQuiz')}
              value={midtermActivityId}
              onValueChange={setMidtermActivityId}
              options={quizChoices}
              placeholder={t('authoring.choose')}
            />
            {quizChoices.length === 0 && (
              <p className="text-caption text-text-muted mt-1">{t('admin.learningPath.noMidtermActivity')}</p>
            )}
          </div>

          <div>
            <Select
              label={t('admin.learningPath.anchorLesson')}
              value={midtermAfterLessonId}
              onValueChange={setMidtermAfterLessonId}
              options={lessonChoices}
              placeholder={t('authoring.choose')}
            />
          </div>

          <div className="max-w-sm md:col-span-2">
            <Input
              type="datetime-local"
              label={t('admin.learningPath.midtermOpensAt')}
              value={midtermOpensAtLocal}
              onChange={(e) => {
                setMidtermOpensAtLocal(e.target.value)
              }}
            />
            <p className="text-caption text-text-muted mt-1">{t('admin.learningPath.midtermOpensAtHint')}</p>
          </div>
        </div>

        <ProblemAlert error={saveMutation.error} />

        <div className="flex justify-end">
          <Button loading={saveMutation.isPending} onClick={handleSave}>
            {t('authoring.save')}
          </Button>
        </div>

        {/* Confirmation Dialog for Enabling */}
        <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
          <DialogContent>
            <DialogTitle>{t('admin.learningPath.confirmTitle')}</DialogTitle>
            <DialogDescription className="mt-2 space-y-2">
              <p>{t('admin.learningPath.confirmWaiverExplanation')}</p>
              <p className="text-text font-medium">{t('admin.learningPath.confirmAuditNotice')}</p>
            </DialogDescription>
            <DialogFooter>
              <DialogClose asChild>
                <Button variant="ghost">{t('common.cancel')}</Button>
              </DialogClose>
              <Button onClick={handleConfirmEnable}>{t('admin.learningPath.confirmEnableAction')}</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  )
}

function ExtraAttemptsSection({
  courseId,
  initialEnrollmentId,
  initialQuizId,
}: {
  courseId: string
  initialEnrollmentId?: string
  initialQuizId?: string
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const [, setSearchParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const grantsQuery = useAttemptGrantsQuery({ courseId, page, size: 10 })
  const [grantDialogOpen, setGrantDialogOpen] = useState(false)

  const grantsData = grantsQuery.data
  const grants = grantsData?.content ?? []
  const totalPages = grantsData?.totalPages ?? 1

  const isDeepLink = Boolean(initialEnrollmentId && initialQuizId)
  const isDialogOpen = grantDialogOpen || isDeepLink

  const handleCloseDialog = () => {
    setGrantDialogOpen(false)
    if (initialEnrollmentId || initialQuizId) {
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev)
          next.delete('enrollment')
          next.delete('quiz')
          return next
        },
        { replace: true },
      )
    }
  }

  return (
    <Card>
      <CardHeader
        title={t('admin.learningPath.extraAttemptsTitle')}
        action={
          <Button
            size="sm"
            onClick={() => {
              setGrantDialogOpen(true)
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('admin.learningPath.giveAttemptAction')}
          </Button>
        }
      />
      <CardContent className="grid grid-cols-1 gap-4">
        {grantsQuery.isPending && <Skeleton className="h-48" />}

        {grantsQuery.isError && (
          <ErrorState
            problem={toApiProblem(grantsQuery.error)}
            onRetry={() => {
              void grantsQuery.refetch()
            }}
          />
        )}

        {grantsQuery.isSuccess && grants.length === 0 && (
          <EmptyState title={t('admin.learningPath.noGrantsFound')} />
        )}

        {grantsQuery.isSuccess && grants.length > 0 && (
          <>
            <div className="border-border overflow-x-auto rounded-lg border">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableHeaderCell>{t('admin.learningPath.tableQuiz')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.learningPath.tableStudent')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.learningPath.tableReason')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.learningPath.tableWho')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.learningPath.tableWhen')}</TableHeaderCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {grants.map((grant: AttemptGrantView) => (
                    <TableRow key={grant.id}>
                      <TableCell className="text-small font-mono">{grant.quizId ?? '—'}</TableCell>
                      <TableCell className="text-small font-mono">{grant.enrollmentId ?? '—'}</TableCell>
                      <TableCell className="text-small">{grant.reason ?? '—'}</TableCell>
                      <TableCell className="text-small text-text-muted font-mono">
                        {grant.grantedBy ?? '—'}
                      </TableCell>
                      <TableCell className="text-small text-text-muted">
                        {grant.createdAt ? formatDateTime(grant.createdAt, locale) : '—'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            {totalPages > 1 && <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />}
          </>
        )}

        {isDialogOpen && (
          <GiveAttemptDialog
            courseId={courseId}
            initialEnrollmentId={initialEnrollmentId}
            initialQuizId={initialQuizId}
            onClose={handleCloseDialog}
          />
        )}
      </CardContent>
    </Card>
  )
}

export function GiveAttemptDialog({
  courseId,
  initialEnrollmentId,
  initialQuizId,
  onClose,
}: {
  courseId: string
  initialEnrollmentId?: string
  initialQuizId?: string
  onClose: () => void
}) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()

  const enrollmentsQuery = useCourseEnrollmentsQuery(courseId)
  const quizzesQuery = useCourseQuizzesQuery(courseId)
  const targetEnrollmentQuery = useAdminEnrollmentQuery(initialEnrollmentId)
  const targetQuizQuery = useQuizDetailQuery(initialQuizId)
  const grantMutation = useGrantAttempt()

  const [pickedQuizId, setQuizId] = useState<string>('')
  const [pickedEnrollmentId, setEnrollmentId] = useState<string>('')
  const [reason, setReason] = useState<string>('')
  const [touched, setTouched] = useState(false)

  const isTargetLoading =
    quizzesQuery.isPending ||
    (Boolean(initialEnrollmentId) && targetEnrollmentQuery.isPending) ||
    (Boolean(initialQuizId) && targetQuizQuery.isPending && !quizzesQuery.data?.some((q) => q.id === initialQuizId))

  const targetValidation = resolveGrantTarget({
    courseId,
    targetEnrollmentId: initialEnrollmentId,
    targetQuizId: initialQuizId,
    enrollment: targetEnrollmentQuery.data,
    quizzes: quizzesQuery.data,
    quiz: targetQuizQuery.data,
  })

  const hasTargetMismatch = !isTargetLoading && !targetValidation.valid

  const quizChoices = (quizzesQuery.data ?? []).map((q) => {
    const title = q.translations?.find((tr) => tr.locale === locale)?.title ?? q.translations?.[0]?.title
    return {
      value: q.id ?? '',
      label: title ? `${title} (${q.code ?? ''})` : (q.code ?? q.id ?? ''),
    }
  })
  if (
    targetQuizQuery.data?.id &&
    targetValidation.valid &&
    !quizChoices.some((q) => q.value === targetQuizQuery.data?.id)
  ) {
    const title =
      targetQuizQuery.data.translations?.find((tr) => tr.locale === locale)?.title ??
      targetQuizQuery.data.translations?.[0]?.title
    quizChoices.unshift({
      value: targetQuizQuery.data.id,
      label: title ? `${title} (${targetQuizQuery.data.code ?? ''})` : (targetQuizQuery.data.code ?? targetQuizQuery.data.id),
    })
  }

  const quizId =
    pickedQuizId !== ''
      ? pickedQuizId
      : initialQuizId !== undefined && initialQuizId !== ''
        ? targetValidation.valid
          ? initialQuizId
          : ''
        : (quizChoices[0]?.value ?? '')

  const enrollments = enrollmentsQuery.data ?? []
  const enrollmentChoices = enrollments.map((en) => ({
    value: en.id ?? '',
    label: en.studentFullName ?? en.id ?? '',
  }))
  if (
    targetEnrollmentQuery.data?.id &&
    targetValidation.valid &&
    !enrollmentChoices.some((en) => en.value === targetEnrollmentQuery.data?.id)
  ) {
    enrollmentChoices.unshift({
      value: targetEnrollmentQuery.data.id,
      label: targetEnrollmentQuery.data.studentFullName ?? targetEnrollmentQuery.data.id,
    })
  }

  const enrollmentId =
    pickedEnrollmentId !== ''
      ? pickedEnrollmentId
      : initialEnrollmentId !== undefined && initialEnrollmentId !== ''
        ? targetValidation.valid
          ? initialEnrollmentId
          : ''
        : (enrollmentChoices[0]?.value ?? '')

  const reasonValid = validateGrantReason(reason)
  const canSubmit = !hasTargetMismatch && quizId !== '' && enrollmentId !== '' && reasonValid

  const handleSubmit = (e: React.SyntheticEvent) => {
    e.preventDefault()
    setTouched(true)
    if (!canSubmit) return

    grantMutation.mutate(
      { quizId, enrollmentId, reason: reason.trim() },
      {
        onSuccess: () => {
          toast({ tone: 'success', title: t('admin.learningPath.grantSuccess') })
          onClose()
        },
      },
    )
  }

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle>{t('admin.learningPath.giveAttemptTitle')}</DialogTitle>
        <DialogDescription>{t('admin.learningPath.giveAttemptDescription')}</DialogDescription>

        {isTargetLoading ? (
          <Skeleton className="my-4 h-48" />
        ) : (
          <form onSubmit={handleSubmit} className="mt-4 grid grid-cols-1 gap-4">
            {hasTargetMismatch && (
              <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
                {t('admin.learningPath.targetMismatch')}
              </p>
            )}

            <Select
              label={t('admin.learningPath.selectQuiz')}
              value={quizId}
              onValueChange={setQuizId}
              options={quizChoices}
              placeholder={t('authoring.choose')}
            />

            <Select
              label={t('admin.learningPath.selectStudent')}
              value={enrollmentId}
              onValueChange={setEnrollmentId}
              options={enrollmentChoices}
              placeholder={t('authoring.choose')}
            />

            <div>
              <Textarea
                label={t('admin.learningPath.grantReasonLabel')}
                rows={3}
                value={reason}
                onChange={(e) => {
                  setReason(e.target.value)
                }}
                error={touched && !reasonValid ? t('admin.learningPath.grantReasonMinLength') : undefined}
              />
              <p className="text-caption text-text-muted mt-1">{t('admin.learningPath.grantReasonHint')}</p>
            </div>

            <ProblemAlert error={grantMutation.error} />

            <DialogFooter>
              <DialogClose asChild>
                <Button variant="ghost">{t('common.cancel')}</Button>
              </DialogClose>
              <Button type="submit" disabled={!canSubmit} loading={grantMutation.isPending}>
                {t('admin.learningPath.submitGrant')}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
