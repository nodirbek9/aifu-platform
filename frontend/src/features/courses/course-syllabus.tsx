import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { ErrorState } from '@/shared/ui/error-state'
import { ProgressBar } from '@/shared/ui/progress'
import { sessionBar } from '@/shared/ui/session-type'
import { SkeletonText } from '@/shared/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import type { Course } from './model'
import { useSubjectQuery } from './queries'

/** The syllabus tab: what the subject is for, its outcomes, where the hours go, how it is assessed. */
export function CourseSyllabus({ course }: { course: Course }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const subject = useSubjectQuery(course.subjectId, locale)
  const totalHours = course.hours.reduce((sum, allocation) => sum + allocation.hours, 0)

  return (
    <div className="grid gap-10">
      {subject.isPending && course.subjectId !== undefined && <SkeletonText lines={6} />}
      {subject.isError && (
        <ErrorState
          problem={toApiProblem(subject.error)}
          onRetry={() => {
            void subject.refetch()
          }}
        />
      )}
      {subject.isSuccess && (
        <>
          {(subject.data.aim !== undefined || subject.data.objectives !== undefined) && (
            <div className="max-w-lesson grid gap-6">
              {subject.data.aim !== undefined && (
                <SyllabusSection title={t('courses.aim')}>
                  <p className="font-reading text-body text-text leading-relaxed">{subject.data.aim}</p>
                </SyllabusSection>
              )}
              {subject.data.objectives !== undefined && (
                <SyllabusSection title={t('courses.objectives')}>
                  <p className="font-reading text-body text-text leading-relaxed">
                    {subject.data.objectives}
                  </p>
                </SyllabusSection>
              )}
            </div>
          )}
          {subject.data.outcomes.length > 0 && (
            <SyllabusSection title={t('courses.outcomes')}>
              <ol className="max-w-lesson grid gap-3">
                {subject.data.outcomes.map((outcome) => (
                  <li key={outcome.id} className="flex gap-3">
                    <span className="text-text-muted text-small w-10 shrink-0 pt-0.5 font-mono">
                      {outcome.code}
                    </span>
                    <span className="font-reading text-body text-text">{outcome.statement}</span>
                  </li>
                ))}
              </ol>
            </SyllabusSection>
          )}
        </>
      )}

      {course.hours.length > 0 && (
        <SyllabusSection title={t('courses.hourBudget')}>
          <ul className="grid max-w-xl gap-3">
            {course.hours.map((allocation) => (
              <li key={allocation.sessionType} className="grid gap-1.5">
                <div className="text-small flex items-baseline justify-between gap-4">
                  <span className="text-text">{t(`sessionType.${allocation.sessionType}`)}</span>
                  <span className="text-text-muted tabular-nums">
                    {t('courses.hours', { count: allocation.hours })}
                  </span>
                </div>
                <ProgressBar
                  value={allocation.hours}
                  max={Math.max(totalHours, 1)}
                  barClassName={sessionBar[allocation.sessionType]}
                />
              </li>
            ))}
          </ul>
        </SyllabusSection>
      )}

      {course.assessment.length > 0 && (
        <SyllabusSection title={t('courses.assessment')}>
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>{t('courses.component')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('courses.maxPoints')}</TableHeaderCell>
                <TableHeaderCell numeric>{t('courses.passMark')}</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {course.assessment.map((component) => (
                <TableRow key={component.id}>
                  <TableCell>
                    <span className="text-text font-medium">{component.code}</span>
                    <span className="text-text-muted"> — {t(`assessmentKind.${component.kind}`)}</span>
                    {component.blocksFinal && (
                      <span className="text-small text-text-muted block">{t('courses.blocksFinal')}</span>
                    )}
                  </TableCell>
                  <TableCell numeric>{component.maxPoints}</TableCell>
                  <TableCell numeric>
                    {component.minPassPercent !== undefined ? `${component.minPassPercent}%` : '—'}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </SyllabusSection>
      )}
    </div>
  )
}

function SyllabusSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="grid gap-3">
      <h2 className="font-display text-title text-text font-semibold">{title}</h2>
      {children}
    </section>
  )
}
