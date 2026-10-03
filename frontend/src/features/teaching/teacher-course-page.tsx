import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { useParams, useSearchParams } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { ErrorState } from '@/shared/ui/error-state'
import { PageHeader } from '@/shared/ui/page-header'
import { Skeleton } from '@/shared/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { AnnouncementsEditor } from './announcements-editor'
import { AttendanceJournal } from './attendance-journal'
import { ClassPlan } from './class-plan'
import { CourseRoster } from './course-roster'
import { useTeacherCoursesQuery } from './queries'

/** A section of the course page: its key in the address, its label, what it shows. */
export type CourseSection = {
  key: string
  label: string
  render: (courseId: string, courseTitle: string) => ReactNode
}

type TeacherCoursePageProps = {
  /** Sections that later phases add (content, tasks, quizzes, grades, analytics). */
  extraSections?: readonly CourseSection[]
}

/** A course I teach: its calendar-thematic plan, journal, students and announcements — one tab each. */
export function TeacherCoursePage({ extraSections = [] }: TeacherCoursePageProps) {
  const { t } = useTranslation()
  const { courseId = '' } = useParams()
  const [search, setSearch] = useSearchParams()
  const courses = useTeacherCoursesQuery()
  const course = courses.data?.find((item) => item.id === courseId)

  const sections: CourseSection[] = [
    { key: 'plan', label: t('teaching.tabPlan'), render: (id) => <ClassPlan courseId={id} /> },
    ...extraSections,
    {
      key: 'journal',
      label: t('teaching.journal'),
      render: (id, title) => <AttendanceJournal courseId={id} courseTitle={title} />,
    },
    { key: 'students', label: t('teaching.tabStudents'), render: (id) => <CourseRoster courseId={id} /> },
    {
      key: 'announcements',
      label: t('announcements.title'),
      render: (id) => <AnnouncementsEditor courseId={id} />,
    },
  ]
  const requested = search.get('tab')
  const tab = sections.some((section) => section.key === requested) ? (requested ?? 'plan') : 'plan'

  if (courses.isPending) return <Skeleton className="h-96" />
  if (courses.isError) {
    return (
      <ErrorState
        problem={toApiProblem(courses.error)}
        onRetry={() => {
          void courses.refetch()
        }}
      />
    )
  }
  if (course === undefined) {
    return <ErrorState problem={{ status: 404, code: 'RESOURCE_NOT_FOUND', message: '' }} />
  }

  return (
    <div className="grid gap-6">
      <Breadcrumb
        items={[{ label: t('nav.teacherClasses'), to: '/teacher/courses' }, { label: course.title }]}
      />
      <PageHeader
        title={course.title}
        context={[course.subjectCode, course.cohortCode, course.termName].filter(Boolean).join(' · ')}
      />
      <Tabs
        value={tab}
        onValueChange={(value) => {
          setSearch(value === 'plan' ? {} : { tab: value }, { replace: true })
        }}
      >
        <TabsList className="overflow-x-auto">
          {sections.map((section) => (
            <TabsTrigger key={section.key} value={section.key}>
              {section.label}
            </TabsTrigger>
          ))}
        </TabsList>
        {sections.map((section) => (
          <TabsContent key={section.key} value={section.key} className="pt-6">
            {section.key === tab && section.render(course.id, course.title)}
          </TabsContent>
        ))}
      </Tabs>
    </div>
  )
}
