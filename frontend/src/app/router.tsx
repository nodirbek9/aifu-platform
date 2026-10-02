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
  { path: 'timetable', element: <LaterPhasePage section="timetable" /> },
  {
    path: 'courses',
    lazy: async () => ({ Component: (await import('@/features/courses/courses-page')).CoursesPage }),
  },
  {
    path: 'courses/:courseId',
    lazy: async () => ({ Component: (await import('@/features/courses/course-page')).CoursePage }),
  },
  // The practice task and the quiz are built in phase F2.
  { path: 'courses/:courseId/tasks/:taskId', element: <LaterPhasePage section="assignments" /> },
  { path: 'courses/:courseId/quizzes/:quizId', element: <LaterPhasePage section="assignments" /> },
  { path: 'assignments', element: <LaterPhasePage section="assignments" /> },
  { path: 'grades', element: <LaterPhasePage section="grades" /> },
  { path: 'attendance', element: <LaterPhasePage section="attendance" /> },
]

/** Pages that take the whole screen: the app shell steps aside for the work (brief §7). */
const studentFocusRoutes: RouteObject[] = [
  {
    path: 'courses/:courseId/lessons/:lessonId',
    lazy: async () => ({ Component: (await import('@/features/lesson/lesson-page')).LessonPage }),
  },
]

const teacherRoutes: RouteObject[] = [
  { index: true, element: <LaterPhasePage section="home" /> },
  { path: 'timetable', element: <LaterPhasePage section="timetable" /> },
  { path: 'courses', element: <LaterPhasePage section="teacherClasses" /> },
  { path: 'journal', element: <LaterPhasePage section="teacherJournal" /> },
  { path: 'groups', element: <LaterPhasePage section="groups" /> },
]

const adminRoutes: RouteObject[] = [
  { index: true, element: <LaterPhasePage section="home" /> },
  { path: 'timetable', element: <LaterPhasePage section="adminTimetable" /> },
  { path: 'users', element: <LaterPhasePage section="adminUsers" /> },
  { path: 'structure', element: <LaterPhasePage section="adminStructure" /> },
  { path: 'reference', element: <LaterPhasePage section="adminReference" /> },
  { path: 'load', element: <LaterPhasePage section="adminLoad" /> },
  { path: 'audit', element: <LaterPhasePage section="adminAudit" /> },
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
          {
            element: <ShellRoute />,
            children: [
              { path: '/', element: <RoleHomeRedirect /> },
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
