import { createBrowserRouter, type RouteObject } from 'react-router'
import { ChangePasswordPage } from '@/features/auth/change-password-page'
import { LoginPage } from '@/features/auth/login-page'
import { PublicOnly, RequireAuth, RequireRole, RoleHomeRedirect, ShellRoute } from './guards'
import { LaterPhasePage } from './later-phase-page'
import { NotFoundPage, RouteErrorPage } from './system-pages'

// Pages are separate chunks: a role downloads only its own screens, when it opens them.
const studentRoutes: RouteObject[] = [
  {
    index: true,
    lazy: async () => ({
      Component: (await import('@/features/dashboard/student-dashboard-page')).StudentDashboardPage,
    }),
  },
  {
    path: 'timetable',
    lazy: async () => ({
      Component: (await import('@/features/timetable/student-timetable-page')).StudentTimetablePage,
    }),
  },
  {
    path: 'courses',
    lazy: async () => ({ Component: (await import('@/features/courses/courses-page')).CoursesPage }),
  },
  {
    path: 'courses/:courseId',
    lazy: async () => ({ Component: (await import('@/features/courses/course-page')).CoursePage }),
  },
  {
    path: 'assignments',
    lazy: async () => ({ Component: (await import('@/features/courses/assignments-page')).AssignmentsPage }),
  },
  {
    path: 'grades',
    lazy: async () => ({ Component: (await import('@/features/grades/grades-page')).GradesPage }),
  },
  {
    path: 'attendance',
    lazy: async () => ({
      Component: (await import('@/features/attendance/attendance-page')).AttendancePage,
    }),
  },
]

/** Pages of every signed-in person, whatever their role. */
const commonRoutes: RouteObject[] = [
  {
    path: '/notifications',
    lazy: async () => ({
      Component: (await import('@/features/notifications/notifications-page')).NotificationsPage,
    }),
  },
  {
    path: '/announcements',
    lazy: async () => ({
      Component: (await import('@/features/announcements/announcements-page')).AnnouncementsPage,
    }),
  },
  {
    path: '/profile',
    lazy: async () => ({ Component: (await import('@/features/profile/profile-page')).ProfilePage }),
  },
]

/** Pages that take the whole screen: the app shell steps aside for the work (brief §7). */
const studentFocusRoutes: RouteObject[] = [
  {
    path: 'courses/:courseId/lessons/:lessonId',
    lazy: async () => ({ Component: (await import('@/features/lesson/lesson-page')).LessonPage }),
  },
  {
    path: 'courses/:courseId/tasks/:taskId',
    lazy: async () => ({ Component: (await import('@/features/coding/task-page')).TaskPage }),
  },
  {
    path: 'courses/:courseId/quizzes/:quizId',
    lazy: async () => ({ Component: (await import('@/features/quiz/quiz-page')).QuizPage }),
  },
]

const teacherRoutes: RouteObject[] = [
  {
    index: true,
    lazy: async () => ({
      Component: (await import('@/features/teaching/teacher-dashboard-page')).TeacherDashboardPage,
    }),
  },
  {
    path: 'timetable',
    lazy: async () => ({
      Component: (await import('@/features/teaching/teacher-timetable-page')).TeacherTimetablePage,
    }),
  },
  {
    path: 'courses',
    lazy: async () => ({
      Component: (await import('@/features/teaching/teacher-courses-page')).TeacherCoursesPage,
    }),
  },
  {
    path: 'courses/:courseId',
    lazy: async () => ({
      Component: (await import('@/features/teaching/teacher-course-route')).TeacherCourseRoute,
    }),
  },
  {
    path: 'classes/:entryId/:date',
    lazy: async () => ({
      Component: (await import('@/features/teaching/class-session-page')).ClassSessionPage,
    }),
  },
  {
    path: 'journal',
    lazy: async () => ({ Component: (await import('@/features/teaching/journal-page')).JournalPage }),
  },
  {
    path: 'groups',
    lazy: async () => ({ Component: (await import('@/features/teaching/groups-page')).GroupsPage }),
  },
  {
    path: 'lessons/:lessonId/edit',
    lazy: async () => ({
      Component: (await import('@/features/authoring/lesson-editor-page')).LessonEditorPage,
    }),
  },
  {
    path: 'coding-tasks/:taskId/edit',
    lazy: async () => ({
      Component: (await import('@/features/authoring/coding-task-editor-page')).CodingTaskEditorPage,
    }),
  },
  {
    path: 'quizzes/:quizId/edit',
    lazy: async () => ({
      Component: (await import('@/features/authoring/quiz-editor-page')).QuizEditorPage,
    }),
  },
  {
    path: 'coding-tasks/:taskId/submissions/:submissionId',
    lazy: async () => ({
      Component: (await import('@/features/review/submission-review-page')).SubmissionReviewPage,
    }),
  },
]

/** The lesson on the projector: nothing of the app around it. */
const teacherFocusRoutes: RouteObject[] = [
  {
    path: 'lessons/:lessonId/present',
    lazy: async () => ({
      Component: (await import('@/features/teaching/presentation-page')).PresentationPage,
    }),
  },
]

const adminRoutes: RouteObject[] = [
  {
    index: true,
    lazy: async () => ({
      Component: (await import('@/features/admin/admin-dashboard-page')).AdminDashboardPage,
    }),
  },
  { path: 'timetable', element: <LaterPhasePage section="adminTimetable" /> },
  {
    path: 'users',
    lazy: async () => ({ Component: (await import('@/features/admin/users-page')).UsersPage }),
  },
  {
    path: 'users/import',
    lazy: async () => ({ Component: (await import('@/features/admin/import-page')).ImportPage }),
  },
  {
    path: 'users/:userId',
    lazy: async () => ({ Component: (await import('@/features/admin/user-page')).UserPage }),
  },
  {
    path: 'structure',
    lazy: async () => ({ Component: (await import('@/features/admin/structure-page')).StructurePage }),
  },
  {
    path: 'structure/subjects/:subjectId',
    lazy: async () => ({ Component: (await import('@/features/admin/subject-page')).SubjectPage }),
  },
  {
    path: 'structure/courses/:courseId',
    lazy: async () => ({ Component: (await import('@/features/admin/course-admin-page')).CourseAdminPage }),
  },
  {
    path: 'reference',
    lazy: async () => ({ Component: (await import('@/features/admin/reference-page')).ReferencePage }),
  },
  {
    path: 'load',
    lazy: async () => ({ Component: (await import('@/features/admin/load-page')).LoadPage }),
  },
  {
    path: 'audit',
    lazy: async () => ({ Component: (await import('@/features/admin/audit-page')).AuditPage }),
  },
]

// The design-system catalogue exists in development builds only.
const devRoutes: RouteObject[] = import.meta.env.DEV
  ? [
      {
        path: '/dev/ui',
        lazy: async () => ({ Component: (await import('./dev-ui-page')).DevUiPage }),
      },
    ]
  : []

export const router = createBrowserRouter([
  {
    errorElement: <RouteErrorPage />,
    children: [
      {
        element: <PublicOnly />,
        children: [{ path: '/login', element: <LoginPage /> }],
      },
      ...devRoutes,
      {
        element: <RequireAuth />,
        children: [
          { path: '/change-password', element: <ChangePasswordPage /> },
          { path: '/student', element: <RequireRole role="STUDENT" />, children: studentFocusRoutes },
          { path: '/teacher', element: <RequireRole role="TEACHER" />, children: teacherFocusRoutes },
          {
            element: <ShellRoute />,
            children: [
              { path: '/', element: <RoleHomeRedirect /> },
              ...commonRoutes,
              { path: '/student', element: <RequireRole role="STUDENT" />, children: studentRoutes },
              { path: '/teacher', element: <RequireRole role="TEACHER" />, children: teacherRoutes },
              { path: '/admin', element: <RequireRole role="ADMIN" />, children: adminRoutes },
              { path: '*', element: <NotFoundPage /> },
            ],
          },
        ],
      },
    ],
  },
])
