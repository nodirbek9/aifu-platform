import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useSubjectQuery } from '@/features/courses/queries'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { IconButton } from '@/shared/ui/icon-button'
import { Select } from '@/shared/ui/select'
import { titleOf, type EditorLesson } from './model'
import { useAuthoringCourseQuery, useCurriculumTreeQuery, useLessonLink } from './queries'

type Choice = { id: string; label: string }

/**
 * T-04.2 — what the lesson builds on and what it is for: the lessons of the course to be done
 * first (the student sees them above the lesson) and the subject's learning outcomes it serves.
 */
export function LessonLinksCard({ lesson, courseId }: { lesson: EditorLesson; courseId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const tree = useCurriculumTreeQuery(courseId)
  const course = useAuthoringCourseQuery(courseId)
  const subject = useSubjectQuery(course.data?.subjectId, locale)
  const prerequisites = useLessonLink(lesson.id, 'prerequisites')
  const outcomes = useLessonLink(lesson.id, 'outcomes')

  const lessons: Choice[] = (tree.data?.modules ?? []).flatMap((module) =>
    module.lessons
      .filter((item) => item.id !== lesson.id)
      .map((item) => ({
        id: item.id,
        label: [item.code, titleOf(item, locale)].filter(Boolean).join(' · '),
      })),
  )
  const outcomeChoices: Choice[] = (subject.data?.outcomes ?? []).map((outcome) => ({
    id: outcome.id,
    label: outcome.code === '' ? outcome.statement : `${outcome.code} · ${outcome.statement}`,
  }))
  const failure = prerequisites.error ?? outcomes.error

  return (
    <Card>
      <CardHeader title={t('authoring.linksTitle')} description={t('authoring.linksBody')} />
      <CardContent className="grid grid-cols-1 gap-5">
        <LinkList
          heading={t('authoring.prerequisites')}
          empty={t('authoring.noPrerequisites')}
          addLabel={t('authoring.addPrerequisite')}
          removeLabel={(label) => t('authoring.removeLink', { name: label })}
          linked={lesson.prerequisites}
          choices={lessons}
          busy={prerequisites.isPending}
          onChange={(targetId, linked) => {
            prerequisites.mutate({ targetId, linked })
          }}
        />
        <LinkList
          heading={t('authoring.outcomes')}
          empty={
            outcomeChoices.length === 0 ? t('authoring.subjectHasNoOutcomes') : t('authoring.noOutcomes')
          }
          addLabel={t('authoring.addOutcome')}
          removeLabel={(label) => t('authoring.removeLink', { name: label })}
          linked={lesson.outcomes}
          choices={outcomeChoices}
          busy={outcomes.isPending}
          onChange={(targetId, linked) => {
            outcomes.mutate({ targetId, linked })
          }}
        />
        {failure !== null && (
          <p role="alert" className="text-small text-danger">
            {t(`errors.${toApiProblem(failure).code}`)}
          </p>
        )}
      </CardContent>
    </Card>
  )
}

function LinkList({
  heading,
  empty,
  addLabel,
  removeLabel,
  linked,
  choices,
  busy,
  onChange,
}: {
  heading: string
  empty: string
  addLabel: string
  removeLabel: (label: string) => string
  linked: readonly string[]
  choices: readonly Choice[]
  busy: boolean
  onChange: (targetId: string, linked: boolean) => void
}) {
  const { t } = useTranslation()
  const shown = linked.map((id) => choices.find((choice) => choice.id === id) ?? { id, label: id })
  const available = choices.filter((choice) => !linked.includes(choice.id))
  return (
    <section className="grid grid-cols-1 gap-2">
      <h3 className="text-small text-text font-semibold">{heading}</h3>
      {shown.length === 0 ? (
        <p className="text-small text-text-muted">{empty}</p>
      ) : (
        <ul className="grid grid-cols-1 gap-1">
          {shown.map((item) => (
            <li key={item.id} className="bg-surface-sunken flex items-center gap-2 rounded-md py-1 pr-1 pl-3">
              <span className="text-small text-text min-w-0 flex-1">{item.label}</span>
              <IconButton
                size="sm"
                aria-label={removeLabel(item.label)}
                disabled={busy}
                onClick={() => {
                  onChange(item.id, false)
                }}
              >
                <X aria-hidden className="size-4" />
              </IconButton>
            </li>
          ))}
        </ul>
      )}
      {available.length > 0 && (
        <Select
          label={addLabel}
          value={undefined}
          placeholder={t('authoring.choose')}
          disabled={busy}
          onValueChange={(value) => {
            onChange(value, true)
          }}
          options={available.map((choice) => ({ value: choice.id, label: choice.label }))}
        />
      )}
    </section>
  )
}
