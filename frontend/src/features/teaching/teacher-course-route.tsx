import { useTranslation } from 'react-i18next'
import { CourseAnalytics } from '@/features/analytics/course-analytics'
import { CurriculumTree } from '@/features/authoring/curriculum-tree'
import { PracticeList } from '@/features/authoring/practice-list'
import { TeacherGradebook } from '@/features/gradebook/teacher-gradebook'
import { ReviewQueue } from '@/features/review/review-queue'
import { TeacherCoursePage, type CourseSection } from './teacher-course-page'

/** The course page as the router shows it, with the sections every phase has added. */
export function TeacherCourseRoute() {
  const { t } = useTranslation()
  const sections: CourseSection[] = [
    { key: 'content', label: t('authoring.tabContent'), render: (id) => <CurriculumTree courseId={id} /> },
    { key: 'practice', label: t('authoring.tabPractice'), render: (id) => <PracticeList courseId={id} /> },
    { key: 'review', label: t('review.tab'), render: (id) => <ReviewQueue courseId={id} /> },
    {
      key: 'grades',
      label: t('gradebook.title'),
      render: (id, title) => <TeacherGradebook courseId={id} courseTitle={title} />,
    },
    { key: 'analytics', label: t('analytics.tab'), render: (id) => <CourseAnalytics courseId={id} /> },
  ]
  return <TeacherCoursePage extraSections={sections} />
}
