import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, unwrap } from '@/shared/api/client'
import type { Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { pickTranslation } from '@/shared/lib/translations'
import type { Audience } from '@/features/announcements/queries'
import {
  lessonPlanOf,
  lessonTitle,
  narrowClassSession,
  narrowJournal,
  narrowPlannedClass,
  narrowTeacherCourse,
  type AttendanceStatus,
  type ClassSession,
  type CourseLesson,
} from './model'

export const teachingKeys = {
  courses: ['teacher', 'courses'] as const,
  session: (entryId: string, date: string) => ['teacher', 'class-session', entryId, date] as const,
  plan: (courseId: string) => ['teacher', 'class-plan', courseId] as const,
  journal: (courseId: string) => ['teacher', 'journal', courseId] as const,
  lessons: (courseId: string, locale: Locale) => ['teacher', 'course-lessons', courseId, locale] as const,
  lessonPlan: (lessonId: string) => ['teacher', 'lesson-plan', lessonId] as const,
  roster: (courseId: string) => ['teacher', 'roster', courseId] as const,
  atRisk: (courseId: string) => ['teacher', 'at-risk', courseId] as const,
  activities: (courseId: string) => ['teacher', 'activities', courseId] as const,
  groups: ['groups'] as const,
  groupStudents: (groupId: string) => ['groups', groupId, 'students'] as const,
}

/** The courses I teach. */
export function useTeacherCoursesQuery() {
  const locale = useLocale()
  return useQuery({
    queryKey: [...teachingKeys.courses, locale],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses', { params: { query: { page: 0, size: 100, locale } } }),
      )
      return (page.content ?? []).map(narrowTeacherCourse).filter((course) => course !== null)
    },
  })
}

/** The class page: the class, its lesson (linked or suggested), notes, the roster with marks. */
export function useClassSessionQuery(entryId: string, date: string, locale: Locale) {
  return useQuery({
    queryKey: [...teachingKeys.session(entryId, date), locale],
    queryFn: async () =>
      narrowClassSession(
        await unwrap(
          api.GET('/api/v1/teacher/class-sessions/{entryId}/{date}', {
            params: { path: { entryId, date }, query: { locale } },
          }),
        ),
      ),
  })
}

export type SessionDetails = { lessonId: string | null; topic: string; notes: string }

/**
 * Saves the lesson, the topic and the notes of a class together: the server takes all three at
 * once (a missing lesson unlinks it).
 */
export function useSaveSessionDetails(entryId: string, date: string, locale: Locale) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (details: SessionDetails) =>
      narrowClassSession(
        await unwrap(
          api.PUT('/api/v1/teacher/class-sessions/{entryId}/{date}', {
            params: { path: { entryId, date }, query: { locale } },
            body: {
              ...(details.lessonId !== null && { lessonId: details.lessonId }),
              topic: details.topic,
              notes: details.notes,
            },
          }),
        ),
      ),
    onSuccess: (session) => {
      // the notes field keeps what is being typed: only the lesson and the topic are taken over
      queryClient.setQueryData<ClassSession>([...teachingKeys.session(entryId, date), locale], (current) =>
        current === undefined
          ? session
          : {
              ...current,
              lesson: session.lesson,
              suggestedLesson: session.suggestedLesson,
              topic: session.topic,
            },
      )
      void queryClient.invalidateQueries({ queryKey: ['teacher', 'class-plan'] })
    },
  })
}

export type Mark = { studentId: string; status: AttendanceStatus | null }

/** Marks attendance for the given students; the others keep their marks. */
export function useMarkAttendance(entryId: string, date: string, locale: Locale) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (marks: readonly Mark[]) =>
      narrowClassSession(
        await unwrap(
          api.PUT('/api/v1/teacher/class-sessions/{entryId}/{date}/attendance', {
            params: { path: { entryId, date }, query: { locale } },
            body: {
              marks: marks.map((mark) => ({
                studentId: mark.studentId,
                ...(mark.status !== null && { status: mark.status }),
              })),
            },
          }),
        ),
      ),
    onSuccess: (session) => {
      queryClient.setQueryData<ClassSession>([...teachingKeys.session(entryId, date), locale], (current) =>
        current === undefined ? session : { ...current, totals: session.totals, markedAt: session.markedAt },
      )
      void queryClient.invalidateQueries({ queryKey: ['teacher', 'journal'] })
      void queryClient.invalidateQueries({ queryKey: ['teacher', 'class-plan'] })
    },
  })
}

/** Every lesson of a course in plan order, for linking one to a class. */
export function useCourseLessonsQuery(courseId: string, locale: Locale, enabled = true) {
  return useQuery({
    queryKey: teachingKeys.lessons(courseId, locale),
    queryFn: async (): Promise<CourseLesson[]> => {
      const modules = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/modules', {
          params: { path: { courseId }, query: { page: 0, size: 100 } },
        }),
      )
      const ordered = (modules.content ?? [])
        .filter((module) => module.id !== undefined)
        .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      const perModule = await Promise.all(
        ordered.map(async (module) => {
          const lessons = await unwrap(
            api.GET('/api/v1/teacher/modules/{moduleId}/lessons', {
              params: { path: { moduleId: module.id ?? '' }, query: { page: 0, size: 200 } },
            }),
          )
          const moduleTitle = pickTranslation(module.translations, locale)?.value.title ?? module.code ?? ''
          return (lessons.content ?? [])
            .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
            .flatMap((lesson): CourseLesson[] => {
              if (lesson.id === undefined || lesson.status === 'ARCHIVED') return []
              const item: CourseLesson = {
                id: lesson.id,
                title: lessonTitle(lesson, locale),
                module: moduleTitle,
              }
              if (lesson.code !== undefined) item.code = lesson.code
              if (lesson.sessionType !== undefined) item.sessionType = lesson.sessionType
              return [item]
            })
        }),
      )
      return perModule.flat()
    },
    enabled,
    staleTime: 5 * 60_000,
  })
}

/** The teacher-only plan of a lesson (its LESSON_PLAN blocks); `null` when it has none. */
export function useLessonPlanQuery(lessonId: string | undefined) {
  const locale = useLocale()
  return useQuery({
    queryKey: [...teachingKeys.lessonPlan(lessonId ?? ''), locale],
    queryFn: async () =>
      lessonPlanOf(
        await unwrap(
          api.GET('/api/v1/teacher/lessons/{id}', {
            params: { path: { id: lessonId ?? '' }, query: { locale } },
          }),
        ),
      ),
    enabled: lessonId !== undefined,
  })
}

/** The calendar-thematic plan: every class of the term with its topic. */
export function useClassPlanQuery(courseId: string, locale: Locale) {
  return useQuery({
    queryKey: [...teachingKeys.plan(courseId), locale],
    queryFn: async () => {
      const rows = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/class-plan', {
          params: { path: { courseId }, query: { locale } },
        }),
      )
      return rows.map(narrowPlannedClass).filter((row) => row !== null)
    },
  })
}

export type JournalFilter = { groupId?: string; from?: string; to?: string }

/** A group I teach and my courses that meet it: what the journal is opened by. */
export type JournalChoice = {
  groupId: string
  groupCode: string
  courses: { id: string; title: string; subjectCode?: string }[]
}

export function useJournalChoicesQuery() {
  const locale = useLocale()
  return useQuery({
    queryKey: ['teacher', 'journal-choices', locale],
    queryFn: async () =>
      (await unwrap(api.GET('/api/v1/teacher/journal/groups', { params: { query: { locale } } }))).flatMap(
        (row): JournalChoice[] =>
          row.groupId === undefined || row.groupCode === undefined
            ? []
            : [
                {
                  groupId: row.groupId,
                  groupCode: row.groupCode,
                  courses: (row.courses ?? []).flatMap((course) =>
                    course.courseId === undefined
                      ? []
                      : [
                          {
                            id: course.courseId,
                            title: course.title ?? course.subjectCode ?? '',
                            ...(course.subjectCode !== undefined && { subjectCode: course.subjectCode }),
                          },
                        ],
                  ),
                },
              ],
      ),
  })
}

/** The attendance journal: the group's students × every class held in the period. */
export function useJournalQuery(courseId: string | undefined, filter: JournalFilter, locale: Locale) {
  return useQuery({
    queryKey: [...teachingKeys.journal(courseId ?? ''), filter, locale],
    queryFn: async () =>
      narrowJournal(
        await unwrap(
          api.GET('/api/v1/teacher/courses/{courseId}/attendance', {
            params: { path: { courseId: courseId ?? '' }, query: { locale, ...filter } },
          }),
        ),
      ),
    enabled: courseId !== undefined,
  })
}

export type RosterEntry = { enrollmentId: string; studentId: string; fullName: string }

/** The students enrolled in a course. */
export function useCourseRosterQuery(courseId: string) {
  return useQuery({
    queryKey: teachingKeys.roster(courseId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/students', {
          params: { path: { courseId }, query: { page: 0, size: 500 } },
        }),
      )
      return (page.content ?? []).flatMap((row): RosterEntry[] =>
        row.id === undefined || row.studentUserId === undefined
          ? []
          : [{ enrollmentId: row.id, studentId: row.studentUserId, fullName: row.studentFullName ?? '' }],
      )
    },
  })
}

export type AtRiskStudent = {
  studentId: string
  completionPercent: number
  lastAccessedAt?: string
  academicDebt: boolean
  failedQuizAttempts: number
  unsuccessfulSubmissions: number
}

/** The students who are behind in a course. */
export function useAtRiskQuery(courseId: string) {
  return useQuery({
    queryKey: teachingKeys.atRisk(courseId),
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/analytics/at-risk', {
          params: { path: { courseId }, query: { page: 0, size: 100 } },
        }),
      )
      return (page.content ?? []).flatMap((row): AtRiskStudent[] => {
        if (row.studentUserId === undefined) return []
        const student: AtRiskStudent = {
          studentId: row.studentUserId,
          completionPercent: row.completionPercent ?? 0,
          academicDebt: row.academicDebt === true,
          failedQuizAttempts: row.failedQuizAttempts ?? 0,
          unsuccessfulSubmissions: row.unsuccessfulSubmissions ?? 0,
        }
        if (row.lastAccessedAt !== undefined) student.lastAccessedAt = row.lastAccessedAt
        return [student]
      })
    },
  })
}

export type TeacherActivity = {
  id: string
  courseId: string
  type: string
  title: string
  dueAt?: string
  status: string
}

/** The activities of a course, any status. */
export function useTeacherActivitiesQuery(courseId: string, locale: Locale) {
  return useQuery({
    queryKey: [...teachingKeys.activities(courseId), locale],
    queryFn: async () => {
      const page = await unwrap(
        api.GET('/api/v1/teacher/courses/{courseId}/activities', {
          params: { path: { courseId }, query: { page: 0, size: 500 } },
        }),
      )
      return (page.content ?? []).flatMap((row): TeacherActivity[] => {
        if (row.id === undefined || row.courseId === undefined) return []
        const activity: TeacherActivity = {
          id: row.id,
          courseId: row.courseId,
          type: row.activityType ?? 'MANUAL_TASK',
          title: pickTranslation(row.translations, locale)?.value.title ?? '',
          status: row.status ?? 'DRAFT',
        }
        if (row.dueAt !== undefined) activity.dueAt = row.dueAt
        return [activity]
      })
    },
  })
}

export type Group = { id: string; code: string; studentCount: number; courseYear?: number; curator?: string }

export function useGroupsQuery() {
  return useQuery({
    queryKey: teachingKeys.groups,
    queryFn: async () => {
      const list = await unwrap(api.GET('/api/v1/groups'))
      return list
        .flatMap((row): Group[] => {
          if (row.id === undefined || row.code === undefined) return []
          const group: Group = { id: row.id, code: row.code, studentCount: row.studentCount ?? 0 }
          if (row.courseYear !== undefined) group.courseYear = row.courseYear
          if (row.curatorName !== undefined) group.curator = row.curatorName
          return [group]
        })
        .sort((a, b) => a.code.localeCompare(b.code))
    },
  })
}

export type GroupStudent = { userId: string; fullName: string; studentNumber?: string; username?: string }

export function useGroupStudentsQuery(groupId: string | undefined) {
  return useQuery({
    queryKey: teachingKeys.groupStudents(groupId ?? ''),
    queryFn: async () => {
      const list = await unwrap(
        api.GET('/api/v1/groups/{id}/students', { params: { path: { id: groupId ?? '' } } }),
      )
      return list.flatMap((row): GroupStudent[] => {
        if (row.userId === undefined) return []
        const student: GroupStudent = { userId: row.userId, fullName: row.fullName ?? '' }
        if (row.studentNumber !== undefined) student.studentNumber = row.studentNumber
        if (row.username !== undefined) student.username = row.username
        return [student]
      })
    },
    enabled: groupId !== undefined,
  })
}

export type NewAnnouncement = {
  audience?: Audience
  courseId?: string
  groupId?: string
  title: string
  body: string
}

export function usePostAnnouncement() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (announcement: NewAnnouncement) =>
      unwrap(api.POST('/api/v1/teacher/announcements', { body: announcement })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['announcements'] }),
  })
}

export function useDeleteAnnouncement() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      unwrap(api.DELETE('/api/v1/teacher/announcements/{id}', { params: { path: { id } } })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['announcements'] }),
  })
}
