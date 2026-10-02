import type { components } from '@/shared/api/schema'
import type { Locale } from '@/shared/i18n'
import { pickTranslation } from '@/shared/lib/translations'
import type { SessionType } from '@/shared/ui/badge'

type Schemas = components['schemas']

export type Enrollment = {
  id: string
  courseId: string
  subjectCode: string
  termName?: string
}

export function narrowEnrollment(raw: Schemas['EnrollmentSummary']): Enrollment | null {
  if (raw.id === undefined || raw.courseId === undefined || raw.status !== 'ACTIVE') return null
  const enrollment: Enrollment = { id: raw.id, courseId: raw.courseId, subjectCode: raw.subjectCode ?? '' }
  if (raw.termName !== undefined) enrollment.termName = raw.termName
  return enrollment
}

export type CourseTeacher = { userId: string; fullName: string; lead: boolean }
export type HourAllocation = { sessionType: SessionType; hours: number }
export type AssessmentComponent = {
  id: string
  code: string
  kind: NonNullable<Schemas['AssessmentComponentView']['kind']>
  maxPoints: number
  minPassPercent?: number
  blocksFinal: boolean
}

export type Course = {
  id: string
  subjectId?: string
  subjectCode: string
  title: string
  titleIsFallback: boolean
  termName?: string
  credits?: number
  totalHours?: number
  teachers: CourseTeacher[]
  hours: HourAllocation[]
  assessment: AssessmentComponent[]
}

export function narrowCourse(raw: Schemas['CourseDetail'], locale: Locale): Course {
  if (raw.id === undefined) throw new Error('CourseDetail without an id')
  const translation = pickTranslation(raw.translations, locale)
  const course: Course = {
    id: raw.id,
    subjectCode: raw.subjectCode ?? '',
    title: translation?.value.title ?? raw.subjectCode ?? '',
    titleIsFallback: translation?.isFallback ?? false,
    teachers: (raw.teachers ?? []).flatMap((teacher) =>
      teacher.userId !== undefined && teacher.fullName !== undefined
        ? [{ userId: teacher.userId, fullName: teacher.fullName, lead: teacher.teacherRole === 'LEAD' }]
        : [],
    ),
    hours: (raw.sessionAllocations ?? []).flatMap((allocation) =>
      allocation.sessionType !== undefined && allocation.allocatedHours !== undefined
        ? [{ sessionType: allocation.sessionType, hours: allocation.allocatedHours }]
        : [],
    ),
    assessment: (raw.assessmentComponents ?? [])
      .slice()
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
      .flatMap((component) => {
        if (component.id === undefined || component.code === undefined || component.kind === undefined)
          return []
        const narrowed: AssessmentComponent = {
          id: component.id,
          code: component.code,
          kind: component.kind,
          maxPoints: component.maxPoints ?? 0,
          blocksFinal: component.blocksFinal === true,
        }
        if (component.minPassPercent !== undefined) narrowed.minPassPercent = component.minPassPercent
        return [narrowed]
      }),
  }
  if (raw.subjectId !== undefined) course.subjectId = raw.subjectId
  if (raw.termName !== undefined) course.termName = raw.termName
  if (raw.credits !== undefined) course.credits = raw.credits
  if (raw.totalHours !== undefined) course.totalHours = raw.totalHours
  return course
}

export type LessonStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

export type PlanLesson = {
  id: string
  code?: string
  title: string
  sessionType: SessionType
  hours?: number
  status: LessonStatus
  locked: boolean
  prerequisiteLessonIds: string[]
  activitiesTotal: number
  activitiesCompleted: number
}

export type PlanModule = { id: string; title: string; lessons: PlanLesson[] }

export type CoursePlan = {
  courseId: string
  enrollmentId: string
  lessonsTotal: number
  lessonsCompleted: number
  continueLessonId?: string
  modules: PlanModule[]
}

function narrowPlanLesson(raw: Schemas['PlanLesson']): PlanLesson | null {
  if (raw.id === undefined || raw.sessionType === undefined) return null
  const lesson: PlanLesson = {
    id: raw.id,
    title: raw.title ?? raw.code ?? '',
    sessionType: raw.sessionType,
    status: raw.status ?? 'NOT_STARTED',
    locked: raw.locked === true,
    prerequisiteLessonIds: raw.prerequisiteLessonIds ?? [],
    activitiesTotal: raw.activitiesTotal ?? 0,
    activitiesCompleted: raw.activitiesCompleted ?? 0,
  }
  if (raw.code !== undefined) lesson.code = raw.code
  if (raw.allocatedHours !== undefined) lesson.hours = raw.allocatedHours
  return lesson
}

export function narrowPlan(raw: Schemas['CoursePlanView']): CoursePlan {
  if (raw.courseId === undefined || raw.enrollmentId === undefined) {
    throw new Error('CoursePlanView without its course or enrollment')
  }
  const plan: CoursePlan = {
    courseId: raw.courseId,
    enrollmentId: raw.enrollmentId,
    lessonsTotal: raw.lessonsTotal ?? 0,
    lessonsCompleted: raw.lessonsCompleted ?? 0,
    modules: (raw.modules ?? []).flatMap((module) =>
      module.id === undefined
        ? []
        : [
            {
              id: module.id,
              title: module.title ?? module.code ?? '',
              lessons: (module.lessons ?? []).map(narrowPlanLesson).filter((lesson) => lesson !== null),
            },
          ],
    ),
  }
  if (raw.continueLessonId !== undefined) plan.continueLessonId = raw.continueLessonId
  return plan
}

/** Every lesson of the plan in reading order. */
export function flattenPlan(plan: CoursePlan): PlanLesson[] {
  return plan.modules.flatMap((module) => module.lessons)
}

export function planPercent(plan: Pick<CoursePlan, 'lessonsCompleted' | 'lessonsTotal'>): number {
  return plan.lessonsTotal === 0 ? 0 : Math.round((plan.lessonsCompleted / plan.lessonsTotal) * 100)
}

export type ActivityState = 'UPCOMING' | 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'OVERDUE'
export type ActivityType = NonNullable<Schemas['MyActivityView']['type']>

export type Activity = {
  activityId: string
  courseId: string
  courseTitle: string
  lessonId?: string
  type: ActivityType
  /** The quiz or the coding task to open; absent for the other types. */
  referenceId?: string
  title: string
  dueAt?: string
  state: ActivityState
  attemptsUsed: number
  attemptLimit?: number
  bestScore?: number
  maxPoints?: number
}

export function narrowActivity(raw: Schemas['MyActivityView']): Activity | null {
  if (raw.activityId === undefined || raw.courseId === undefined || raw.type === undefined) return null
  const activity: Activity = {
    activityId: raw.activityId,
    courseId: raw.courseId,
    courseTitle: raw.courseTitle ?? raw.subjectCode ?? '',
    type: raw.type,
    title: raw.title ?? '',
    state: raw.state ?? 'TODO',
    attemptsUsed: raw.attemptsUsed ?? 0,
  }
  if (raw.lessonId !== undefined) activity.lessonId = raw.lessonId
  if (raw.referenceId !== undefined) activity.referenceId = raw.referenceId
  if (raw.dueAt !== undefined) activity.dueAt = raw.dueAt
  if (raw.attemptLimit !== undefined) activity.attemptLimit = raw.attemptLimit
  if (raw.bestScore !== undefined) activity.bestScore = raw.bestScore
  if (raw.maxPoints !== undefined) activity.maxPoints = raw.maxPoints
  return activity
}

/** Where an activity is done. Quizzes and coding tasks have their own pages (phase F2). */
export function activityPath(activity: Pick<Activity, 'type' | 'referenceId' | 'courseId'>): string | null {
  if (activity.referenceId === undefined) return null
  if (activity.type === 'CODING_TASK')
    return `/student/courses/${activity.courseId}/tasks/${activity.referenceId}`
  if (activity.type === 'QUIZ') return `/student/courses/${activity.courseId}/quizzes/${activity.referenceId}`
  return null
}

export type GradeEntry = {
  id: string
  score: number
  maxScore: number
  publishedAt: string
  source: NonNullable<Schemas['GradeEntryView']['source']>
}

export function narrowGrade(raw: Schemas['GradeEntryView']): GradeEntry | null {
  if (raw.status !== 'PUBLISHED' && raw.status !== 'REVISED') return null
  if (raw.id === undefined || raw.score === undefined || raw.publishedAt === undefined) return null
  return {
    id: raw.id,
    score: raw.score,
    maxScore: raw.maxScore ?? 0,
    publishedAt: raw.publishedAt,
    source: raw.source ?? 'MANUAL',
  }
}

export type Subject = {
  title: string
  aim?: string
  objectives?: string
  outcomes: { id: string; code: string; statement: string }[]
}

export function narrowSubject(raw: Schemas['SubjectDetail'], locale: Locale): Subject {
  const translation = pickTranslation(raw.translations, locale)
  // The long texts may exist only in the language the syllabus was written in.
  const withAim = raw.translations?.find((item) => item.locale === locale && item.aim !== undefined)
  const anyAim = withAim ?? raw.translations?.find((item) => item.aim !== undefined)
  const subject: Subject = {
    title: translation?.value.title ?? raw.code ?? '',
    outcomes: (raw.learningOutcomes ?? [])
      .slice()
      .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
      .flatMap((outcome) => {
        const statement = pickTranslation(outcome.translations, locale)?.value.statement
        return outcome.id !== undefined && statement !== undefined
          ? [{ id: outcome.id, code: outcome.code ?? '', statement }]
          : []
      }),
  }
  if (anyAim?.aim !== undefined) subject.aim = anyAim.aim
  if (anyAim?.objectives !== undefined) subject.objectives = anyAim.objectives
  return subject
}
