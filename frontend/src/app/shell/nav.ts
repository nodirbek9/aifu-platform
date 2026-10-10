import {
  AlertTriangle,
  BookOpen,
  CalendarDays,
  CalendarRange,
  ClipboardCheck,
  ClipboardList,
  Database,
  GraduationCap,
  House,
  Layers,
  Megaphone,
  Route,
  ScrollText,
  Table2,
  UserCheck,
  Users,
  UsersRound,
  type LucideIcon,
} from 'lucide-react'
import type { Role } from '@/features/auth/model'

export type NavKey =
  | 'home'
  | 'courses'
  | 'timetable'
  | 'assignments'
  | 'grades'
  | 'attendance'
  | 'teacherClasses'
  | 'teacherJournal'
  | 'groups'
  | 'announcements'
  | 'adminTimetable'
  | 'adminUsers'
  | 'adminStructure'
  | 'adminReference'
  | 'adminLoad'
  | 'adminAudit'
  | 'adminProblems'
  | 'learningPath'

export type NavItem = {
  key: NavKey
  to: string
  icon: LucideIcon
  /** Matches only the exact path (the role home). */
  end?: boolean
  /** Shown in the phone tab bar; the rest sits under "More". */
  primary?: boolean
}

const NAV: Record<Role, readonly NavItem[]> = {
  STUDENT: [
    { key: 'home', to: '/student', icon: House, end: true, primary: true },
    { key: 'timetable', to: '/student/timetable', icon: CalendarDays, primary: true },
    { key: 'courses', to: '/student/courses', icon: BookOpen, primary: true },
    { key: 'assignments', to: '/student/assignments', icon: ClipboardList, primary: true },
    { key: 'grades', to: '/student/grades', icon: GraduationCap },
    { key: 'attendance', to: '/student/attendance', icon: UserCheck },
    { key: 'announcements', to: '/announcements', icon: Megaphone },
  ],
  TEACHER: [
    { key: 'home', to: '/teacher', icon: House, end: true, primary: true },
    { key: 'timetable', to: '/teacher/timetable', icon: CalendarDays, primary: true },
    { key: 'teacherClasses', to: '/teacher/courses', icon: BookOpen, primary: true },
    { key: 'teacherJournal', to: '/teacher/journal', icon: ClipboardCheck, primary: true },
    { key: 'groups', to: '/teacher/groups', icon: UsersRound },
    { key: 'announcements', to: '/announcements', icon: Megaphone },
  ],
  ADMIN: [
    { key: 'home', to: '/admin', icon: House, end: true, primary: true },
    { key: 'adminTimetable', to: '/admin/timetable', icon: CalendarRange, primary: true },
    { key: 'adminUsers', to: '/admin/users', icon: Users, primary: true },
    { key: 'adminStructure', to: '/admin/structure', icon: Layers, primary: true },
    { key: 'learningPath', to: '/admin/learning-path', icon: Route },
    { key: 'adminProblems', to: '/admin/problems', icon: AlertTriangle },
    { key: 'adminReference', to: '/admin/reference', icon: Database },
    { key: 'adminLoad', to: '/admin/load', icon: Table2 },
    { key: 'announcements', to: '/announcements', icon: Megaphone },
    { key: 'adminAudit', to: '/admin/audit', icon: ScrollText },
  ],
}

export function navFor(role: Role): readonly NavItem[] {
  return NAV[role]
}

/** The nav item a path belongs to: the longest `to` that is a prefix of it. */
export function activeNavItem(role: Role, pathname: string): NavItem | undefined {
  let best: NavItem | undefined
  for (const item of NAV[role]) {
    const matches =
      item.end === true ? pathname === item.to : pathname === item.to || pathname.startsWith(`${item.to}/`)
    if (matches && (best === undefined || item.to.length > best.to.length)) best = item
  }
  return best
}

/** The role a path is written for, by its first segment. */
export function roleOfPath(pathname: string): Role | null {
  if (pathname === '/student' || pathname.startsWith('/student/')) return 'STUDENT'
  if (pathname === '/teacher' || pathname.startsWith('/teacher/')) return 'TEACHER'
  if (pathname === '/admin' || pathname.startsWith('/admin/')) return 'ADMIN'
  return null
}
