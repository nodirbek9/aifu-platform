import { useEffect, useRef, useState } from 'react'
import { BookOpen, Check, CheckCheck, Loader2, MapPin, MonitorPlay, UsersRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router'
import { isHeld } from '@/features/timetable/model'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatLongDay, formatTimeOfDay } from '@/shared/lib/dates'
import { Badge, SessionTypeBadge } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button, button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { ErrorState } from '@/shared/ui/error-state'
import { RichText } from '@/shared/ui/rich-text'
import { Select } from '@/shared/ui/select'
import { Skeleton, SkeletonText } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import {
  ATTENDANCE_STATUSES,
  countMarks,
  type AttendanceStatus,
  type ClassSession,
  type RosterStudent,
} from './model'
import {
  useClassSessionQuery,
  useCourseLessonsQuery,
  useLessonPlanQuery,
  useMarkAttendance,
  useSaveSessionDetails,
} from './queries'

/**
 * T-03 — the class page, used live at the desk: what the class is, its topic and lesson, the
 * attendance as a fast list, notes that save as they are typed, and the teacher's lesson plan.
 */
export function ClassSessionPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { entryId = '', date = '' } = useParams()
  const session = useClassSessionQuery(entryId, date, locale)

  if (session.isPending) {
    return (
      <div className="grid gap-6">
        <Skeleton className="h-24" />
        <Skeleton className="h-96" />
      </div>
    )
  }
  if (session.isError) {
    return (
      <ErrorState
        problem={toApiProblem(session.error)}
        onRetry={() => {
          void session.refetch()
        }}
      />
    )
  }
  const { info } = session.data

  return (
    <div className="grid gap-6">
      <Breadcrumb
        items={[
          { label: t('nav.timetable'), to: `/teacher/timetable?date=${date}` },
          { label: info.courseTitle },
        ]}
      />
      <header className="grid gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <SessionTypeBadge type={info.sessionType} label={t(`sessionType.${info.sessionType}`)} />
          {info.status !== 'SCHEDULED' && (
            <Badge tone={isHeld(info.status) ? 'info' : 'danger'}>{t(`classStatus.${info.status}`)}</Badge>
          )}
          {info.subgroup !== undefined && <Badge>{t('timetable.subgroup', { name: info.subgroup })}</Badge>}
        </div>
        <h1 className="font-display text-display-lg text-text font-semibold">{info.courseTitle}</h1>
        <p className="text-body text-text-muted flex flex-wrap items-center gap-x-5 gap-y-1">
          <span className="tabular-nums">
            {formatLongDay(info.date, locale)}, {formatTimeOfDay(info.startsAt)}–
            {formatTimeOfDay(info.endsAt)}
            {info.pairNo !== undefined && ` · ${t('timetable.pair', { number: info.pairNo })}`}
          </span>
          {info.roomLabel !== undefined && (
            <span className="inline-flex items-center gap-1.5">
              <MapPin aria-hidden className="size-4" />
              {info.roomLabel}
            </span>
          )}
          {info.groups.length > 0 && (
            <span className="inline-flex items-center gap-1.5">
              <UsersRound aria-hidden className="size-4" />
              {info.groups.join(', ')}
            </span>
          )}
        </p>
        {info.changeReason !== undefined && <Callout variant="WARNING">{info.changeReason}</Callout>}
      </header>

      <div className="grid items-start gap-6 lg:grid-cols-12">
        <div className="grid min-w-0 gap-6 lg:col-span-7">
          <Attendance key={`${entryId}:${date}`} entryId={entryId} date={date} session={session.data} />
        </div>
        <div className="grid min-w-0 gap-6 lg:col-span-5">
          <TopicAndNotes key={`${entryId}:${date}`} entryId={entryId} date={date} session={session.data} />
          <LessonPlanCard lessonId={session.data.lesson?.id} />
        </div>
      </div>
    </div>
  )
}

const markTone = {
  PRESENT: 'aria-pressed:bg-success aria-pressed:border-success',
  LATE: 'aria-pressed:bg-warning aria-pressed:border-warning',
  ABSENT: 'aria-pressed:bg-danger aria-pressed:border-danger',
  EXCUSED: 'aria-pressed:bg-info aria-pressed:border-info',
} as const satisfies Record<AttendanceStatus, string>

function Attendance({ entryId, date, session }: { entryId: string; date: string; session: ClassSession }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const { toast } = useToast()
  const mark = useMarkAttendance(entryId, date, locale)
  // the list on screen follows every tap at once; the server's answer confirms or undoes it
  const [students, setStudents] = useState<RosterStudent[]>(session.students)
  const queue = useRef<Promise<void>>(Promise.resolve())
  const totals = countMarks(students)
  const editable = session.held && session.canMarkAttendance

  const apply = (changes: { studentId: string; status: AttendanceStatus }[]) => {
    const before = students
    setStudents((current) =>
      current.map((student) => {
        const change = changes.find((item) => item.studentId === student.userId)
        return change === undefined ? student : { ...student, status: change.status }
      }),
    )
    // one request after another: the server sees the taps in the order they were made
    queue.current = queue.current.then(async () => {
      try {
        await mark.mutateAsync(changes)
      } catch (error) {
        setStudents(before)
        toast({
          tone: 'danger',
          title: t('teaching.markFailed'),
          body: t(`errors.${toApiProblem(error).code}`),
        })
      }
    })
  }

  return (
    <Card>
      <CardHeader
        title={t('teaching.attendance')}
        description={
          session.markedAt === undefined && totals.unmarked === students.length
            ? t('teaching.attendanceNotTaken')
            : t('teaching.attendanceCounts', {
                present: totals.present,
                late: totals.late,
                absent: totals.absent,
                excused: totals.excused,
                total: students.length,
              })
        }
        action={
          editable && students.length > 0 ? (
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                apply(
                  students
                    .filter((student) => student.status === undefined)
                    .map((student) => ({ studentId: student.userId, status: 'PRESENT' as const })),
                )
              }}
              disabled={totals.unmarked === 0}
            >
              <CheckCheck aria-hidden className="size-4" />
              {t('teaching.allPresent')}
            </Button>
          ) : undefined
        }
      />
      <CardContent>
        {!session.held && <Callout variant="WARNING">{t('teaching.notHeld')}</Callout>}
        {session.held && !session.canMarkAttendance && <Callout>{t('teaching.notYet')}</Callout>}
        {students.length === 0 && <p className="text-small text-text-muted">{t('teaching.noStudents')}</p>}
        <ol className="divide-border divide-y">
          {students.map((student, index) => (
            <li key={student.userId} className="flex flex-wrap items-center gap-x-4 gap-y-2 py-2.5">
              <span className="text-small text-text-muted w-6 tabular-nums">{index + 1}.</span>
              <span className="grid min-w-0 flex-1">
                <span className="text-body text-text font-medium">{student.fullName}</span>
                {student.note !== undefined && (
                  <span className="text-small text-text-muted">{student.note}</span>
                )}
              </span>
              <div role="group" aria-label={student.fullName} className="flex gap-1">
                {ATTENDANCE_STATUSES.map((status) => (
                  <button
                    key={status}
                    type="button"
                    disabled={!editable}
                    aria-pressed={student.status === status}
                    aria-label={`${t(`attendance.status.${status}`)}: ${student.fullName}`}
                    title={t(`attendance.status.${status}`)}
                    onClick={() => {
                      apply([{ studentId: student.userId, status }])
                    }}
                    className={cn(
                      'border-border-strong bg-surface-raised text-small text-text-muted flex h-11 min-w-11 items-center justify-center rounded-sm border px-2 font-semibold',
                      'duration-fast ease-standard transition-[background-color,border-color,color]',
                      'hover:text-text focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                      'disabled:cursor-not-allowed disabled:opacity-60',
                      'aria-pressed:text-accent-foreground',
                      markTone[status],
                    )}
                  >
                    {t(`teaching.markShort.${status}`)}
                  </button>
                ))}
              </div>
            </li>
          ))}
        </ol>
        {mark.isPending && (
          <p role="status" className="text-small text-text-muted mt-2 flex items-center gap-2">
            <Loader2 aria-hidden className="size-4 animate-spin" />
            {t('quiz.saving')}
          </p>
        )}
      </CardContent>
    </Card>
  )
}

const NOTES_PAUSE_MS = 800

function TopicAndNotes({ entryId, date, session }: { entryId: string; date: string; session: ClassSession }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const save = useSaveSessionDetails(entryId, date, locale)
  const lessons = useCourseLessonsQuery(
    session.info.courseId ?? '',
    locale,
    session.info.courseId !== undefined,
  )
  const [lessonId, setLessonId] = useState<string | null>(session.lesson?.id ?? null)
  const [topic, setTopic] = useState(session.topic)
  const [notes, setNotes] = useState(session.notes)
  const [saved, setSaved] = useState(true)
  const timer = useRef<number | undefined>(undefined)
  const latest = useRef({ lessonId, topic, notes })

  const persist = (next: { lessonId: string | null; topic: string; notes: string }) => {
    latest.current = next
    setSaved(false)
    save.mutate(next, {
      onSuccess: () => {
        if (latest.current === next) setSaved(true)
      },
    })
  }

  const later = (next: { lessonId: string | null; topic: string; notes: string }) => {
    latest.current = next
    setSaved(false)
    window.clearTimeout(timer.current)
    timer.current = window.setTimeout(() => {
      persist(next)
    }, NOTES_PAUSE_MS)
  }

  // leaving the page mid-typing: what waits for the pause is saved now
  useEffect(
    () => () => {
      if (timer.current !== undefined) {
        window.clearTimeout(timer.current)
        save.mutate(latest.current)
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps -- runs once, on leaving
    [],
  )

  const linked = lessons.data?.find((lesson) => lesson.id === lessonId) ?? session.lesson
  const suggestion = lessonId === null ? session.suggestedLesson : undefined

  return (
    <Card>
      <CardHeader
        title={t('timetable.topic')}
        action={
          <span role="status" className="text-small text-text-muted flex items-center gap-1.5">
            {save.isPending || !saved ? (
              <>
                <Loader2 aria-hidden className="size-4 animate-spin" />
                {t('quiz.saving')}
              </>
            ) : (
              <>
                <Check aria-hidden className="text-success size-4" />
                {t('quiz.saved')}
              </>
            )}
          </span>
        }
      />
      <CardContent className="grid gap-4">
        {linked !== undefined && lessonId !== null && (
          <div className="border-border bg-surface-sunken grid gap-3 rounded-md border p-4">
            <p className="font-reading text-body text-text font-medium">{linked.title}</p>
            <div className="flex flex-wrap gap-2">
              <Link
                to={`/teacher/lessons/${lessonId}/present`}
                className={button({ variant: 'primary', size: 'sm' })}
              >
                <MonitorPlay aria-hidden className="size-4" />
                {t('teaching.present')}
              </Link>
            </div>
          </div>
        )}
        {suggestion !== undefined && (
          <div className="border-border grid gap-2 rounded-md border border-dashed p-4">
            <p className="text-small text-text-muted">{t('teaching.suggested')}</p>
            <p className="font-reading text-body text-text font-medium">{suggestion.title}</p>
            <Button
              size="sm"
              className="justify-self-start"
              onClick={() => {
                setLessonId(suggestion.id)
                persist({ lessonId: suggestion.id, topic, notes })
              }}
            >
              <BookOpen aria-hidden className="size-4" />
              {t('teaching.linkLesson')}
            </Button>
          </div>
        )}
        <Select
          label={t('teaching.lessonOfClass')}
          value={lessonId ?? 'none'}
          onValueChange={(value) => {
            const next = value === 'none' ? null : value
            setLessonId(next)
            persist({ lessonId: next, topic, notes })
          }}
          options={[
            { value: 'none', label: t('teaching.noLesson') },
            ...(lessons.data ?? []).map((lesson) => ({
              value: lesson.id,
              label: [lesson.code, lesson.title].filter(Boolean).join(' · '),
            })),
          ]}
        />
        {lessonId === null && (
          <Textarea
            label={t('teaching.freeTopic')}
            rows={2}
            value={topic}
            onChange={(event) => {
              setTopic(event.target.value)
              later({ lessonId, topic: event.target.value, notes })
            }}
          />
        )}
        <div className="grid gap-1">
          <Textarea
            label={t('teaching.notes')}
            rows={4}
            value={notes}
            onChange={(event) => {
              setNotes(event.target.value)
              later({ lessonId, topic, notes: event.target.value })
            }}
          />
          <p className="text-small text-text-muted">{t('teaching.notesHint')}</p>
        </div>
      </CardContent>
    </Card>
  )
}

function LessonPlanCard({ lessonId }: { lessonId: string | undefined }) {
  const { t } = useTranslation()
  const plan = useLessonPlanQuery(lessonId)
  if (lessonId === undefined) return null
  return (
    <Card>
      <CardHeader title={t('teaching.lessonPlan')} description={t('teaching.lessonPlanHint')} />
      <CardContent className="grid gap-4">
        {plan.isPending && <SkeletonText lines={4} />}
        {plan.isSuccess && plan.data === null && (
          <p className="text-small text-text-muted">{t('teaching.noPlan')}</p>
        )}
        {plan.isSuccess && plan.data !== null && (
          <>
            {plan.data.objectives !== undefined && (
              <PlanPart title={t('teaching.planObjectives')} html={plan.data.objectives} />
            )}
            {plan.data.timeline.length > 0 && (
              <section className="grid gap-2">
                <h3 className="text-caption tracking-caption text-text-muted font-medium uppercase">
                  {t('teaching.planTimeline')}
                </h3>
                <ol className="grid gap-2">
                  {plan.data.timeline.map((step, index) => (
                    <li key={`${index}:${step.title}`} className="flex gap-3">
                      <span className="text-small text-text w-14 shrink-0 font-semibold tabular-nums">
                        {t('teaching.minutes', { count: step.minutes })}
                      </span>
                      <span className="grid gap-0.5">
                        <span className="text-small text-text font-medium">{step.title}</span>
                        {step.notes !== undefined && (
                          <RichText html={step.notes} className="text-small text-text-muted" />
                        )}
                      </span>
                    </li>
                  ))}
                </ol>
              </section>
            )}
            {plan.data.materials !== undefined && (
              <PlanPart title={t('teaching.planMaterials')} html={plan.data.materials} />
            )}
            {plan.data.questions !== undefined && (
              <PlanPart title={t('teaching.planQuestions')} html={plan.data.questions} />
            )}
            {plan.data.homework !== undefined && (
              <PlanPart title={t('teaching.planHomework')} html={plan.data.homework} />
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}

function PlanPart({ title, html }: { title: string; html: string }) {
  return (
    <section className="grid gap-1.5">
      <h3 className="text-caption tracking-caption text-text-muted font-medium uppercase">{title}</h3>
      <RichText html={html} className="text-small" />
    </section>
  )
}
