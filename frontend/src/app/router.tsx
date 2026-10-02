import { createBrowserRouter, type RouteObject } from 'react-router'
import { ChangePasswordPage } from '@/features/auth/change-password-page'
import { LoginPage } from '@/features/auth/login-page'
import { PublicOnly, RequireAuth, RequireRole, RoleHomeRedirect, ShellRoute } from './guards'
import { LaterPhasePage } from './later-phase-page'
import { NotFoundPage, RouteErrorPage } from './system-pages'

const studentRoutes: RouteObject[] = [
  { index: true, element: <LaterPhasePage section="home" /> },
  { path: 'timetable', element: <LaterPhasePage section="timetable" /> },
  { path: 'courses', element: <LaterPhasePage section="courses" /> },
  { path: 'assignments', element: <LaterPhasePage section="assignments" /> },
  { path: 'grades', element: <LaterPhasePage section="grades" /> },
  { path: 'attendance', element: <LaterPhasePage section="attendance" /> },
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
        lazy: async () => {
          const { DevUiPage } = await import('./dev-ui-page')
          return { Component: DevUiPage }
        },
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
