import { useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { LocaleTabs } from '@/features/authoring/locale-tabs'
import { toApiProblem } from '@/shared/api/errors'
import { isLocale, type Locale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { Badge } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { ErrorState } from '@/shared/ui/error-state'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import { Skeleton } from '@/shared/ui/skeleton'
import { Textarea } from '@/shared/ui/textarea'
import { useToast } from '@/shared/ui/toast'
import { PUBLICATION_TONE } from './model'
import {
  useAddOutcome,
  useAddPrerequisite,
  useAllSubjectsQuery,
  useRemovePrerequisite,
  useSaveSubjectText,
  useSubjectQuery,
  useSubjectStatus,
  type SubjectDetail,
} from './structure-queries'

function Problem({ error }: { error: unknown }) {
  const { t } = useTranslation()
  if (error === null || error === undefined) return null
  return (
    <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
      {t(`errors.${toApiProblem(error).code}`)}
    </p>
  )
}

/** A-06 — one subject: its texts in each language, prerequisites, learning outcomes, status. */
export function SubjectPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { subjectId = '' } = useParams()
  const subject = useSubjectQuery(subjectId)
  const status = useSubjectStatus(subjectId)
  if (subject.isPending) return <Skeleton className="h-96" />
  if (subject.isError) {
    return (
      <ErrorState
        problem={toApiProblem(subject.error)}
        onRetry={() => {
          void subject.refetch()
        }}
      />
    )
  }
  const data = subject.data
  const title =
    data.translations?.find((row) => row.locale === locale)?.title ??
    data.translations?.[0]?.title ??
    data.code ??
    ''
  const current = data.status ?? 'DRAFT'
  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb items={[{ label: t('nav.adminStructure'), to: '/admin/structure' }, { label: title }]} />
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid grid-cols-1 gap-1">
          <h1 className="font-display text-display-lg text-text font-semibold wrap-anywhere">{title}</h1>
          <p className="text-small text-text-muted flex flex-wrap items-center gap-2">
            <span className="font-mono">{data.code}</span>
            <span>{data.departmentName}</span>
            <Badge tone={PUBLICATION_TONE[current]}>{t(`admin.publication.${current}`)}</Badge>
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          {current !== 'PUBLISHED' && (
            <Button
              loading={status.isPending}
              onClick={() => {
                status.mutate('PUBLISHED')
              }}
            >
              {t('authoring.publish')}
            </Button>
          )}
          {current !== 'ARCHIVED' && (
            <Button
              variant="outline"
              loading={status.isPending}
              onClick={() => {
                status.mutate('ARCHIVED')
              }}
            >
              {t('admin.archive')}
            </Button>
          )}
        </div>
      </header>
      <Problem error={status.error} />
      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-2">
        <SubjectTexts subject={data} />
        <div className="grid grid-cols-1 gap-6">
          <Prerequisites subject={data} />
          <Outcomes subject={data} />
        </div>
      </div>
    </div>
  )
}

type SubjectText = {
  title: string
  shortTitle: string
  summary: string
  description: string
  aim: string
  objectives: string
}

function textOf(subject: SubjectDetail, locale: Locale): SubjectText | undefined {
  const row = subject.translations?.find((item) => item.locale === locale)
  return row === undefined
    ? undefined
    : {
        title: row.title ?? '',
        shortTitle: row.shortTitle ?? '',
        summary: row.summary ?? '',
        description: row.description ?? '',
        aim: row.aim ?? '',
        objectives: row.objectives ?? '',
      }
}

function SubjectTexts({ subject }: { subject: SubjectDetail }) {
  const { t } = useTranslation()
  const uiLocale = useLocale()
  const [locale, setLocale] = useState<Locale>(uiLocale)
  return (
    <Card>
      <CardHeader title={t('admin.texts')} />
      <CardContent className="grid grid-cols-1 gap-4">
        <LocaleTabs
          value={locale}
          onValueChange={setLocale}
          has={(l) => textOf(subject, l) !== undefined}
          required={
            isLocale(subject.defaultInstructionLanguage) ? [subject.defaultInstructionLanguage] : ['uz']
          }
        />
        <SubjectTextForm
          key={locale}
          subjectId={subject.id ?? ''}
          locale={locale}
          initial={
            textOf(subject, locale) ?? {
              title: '',
              shortTitle: '',
              summary: '',
              description: '',
              aim: '',
              objectives: '',
            }
          }
        />
      </CardContent>
    </Card>
  )
}

function SubjectTextForm({
  subjectId,
  locale,
  initial,
}: {
  subjectId: string
  locale: Locale
  initial: SubjectText
}) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const save = useSaveSubjectText(subjectId)
  const [text, setText] = useState(initial)
  const field = (key: keyof SubjectText, label: string, long = false) =>
    long ? (
      <Textarea
        label={label}
        rows={3}
        value={text[key]}
        onChange={(event) => {
          setText({ ...text, [key]: event.target.value })
        }}
      />
    ) : (
      <Input
        label={label}
        value={text[key]}
        onChange={(event) => {
          setText({ ...text, [key]: event.target.value })
        }}
      />
    )
  return (
    <form
      className="grid grid-cols-1 gap-3"
      onSubmit={(event) => {
        event.preventDefault()
        const body = Object.fromEntries(Object.entries(text).filter(([, value]) => value.trim() !== ''))
        save.mutate(
          { locale, text: { ...body, title: text.title.trim() } },
          {
            onSuccess: () => {
              toast({ tone: 'success', title: t('admin.saved') })
            },
          },
        )
      }}
    >
      {field('title', t('admin.title'))}
      {field('shortTitle', t('admin.shortTitle'))}
      {field('summary', t('authoring.summary'), true)}
      {field('aim', t('admin.aim'), true)}
      {field('objectives', t('admin.objectives'), true)}
      {field('description', t('authoring.quizDescription'), true)}
      <Problem error={save.error} />
      <Button
        type="submit"
        className="justify-self-start"
        disabled={text.title.trim() === ''}
        loading={save.isPending}
      >
        {t('authoring.save')}
      </Button>
    </form>
  )
}

function Prerequisites({ subject }: { subject: SubjectDetail }) {
  const { t } = useTranslation()
  const all = useAllSubjectsQuery()
  const add = useAddPrerequisite(subject.id ?? '')
  const remove = useRemovePrerequisite(subject.id ?? '')
  const [chosen, setChosen] = useState<string>()
  const [type, setType] = useState<'REQUIRED' | 'RECOMMENDED'>('REQUIRED')
  return (
    <Card>
      <CardHeader title={t('admin.prerequisites')} />
      <CardContent className="grid grid-cols-1 gap-3">
        {(subject.prerequisites ?? []).length === 0 && (
          <p className="text-small text-text-muted">{t('admin.noPrerequisites')}</p>
        )}
        <ul className="grid grid-cols-1 gap-1">
          {(subject.prerequisites ?? []).map((item) => (
            <li key={item.id} className="flex items-center gap-2">
              <span className="text-body text-text min-w-0 flex-1 font-mono">
                {item.prerequisiteSubjectCode ?? item.freeTextNote}
              </span>
              <Badge tone={item.requirementType === 'RECOMMENDED' ? 'neutral' : 'info'}>
                {t(`admin.requirement.${item.requirementType ?? 'REQUIRED'}`)}
              </Badge>
              <IconButton
                size="sm"
                aria-label={t('admin.removePrerequisite', { code: item.prerequisiteSubjectCode ?? '' })}
                onClick={() => {
                  remove.mutate(item.id ?? '')
                }}
              >
                <Trash2 aria-hidden className="size-4" />
              </IconButton>
            </li>
          ))}
        </ul>
        <div className="flex flex-wrap items-end gap-2">
          <Select
            className="w-64"
            label={t('admin.subject')}
            value={chosen}
            placeholder={t('authoring.choose')}
            onValueChange={setChosen}
            options={(all.data ?? [])
              .filter((item) => item.id !== subject.id)
              .map((item) => ({ value: item.id ?? '', label: `${item.code ?? ''} · ${item.title ?? ''}` }))}
          />
          <Select
            className="w-40"
            label={t('admin.requirementType')}
            value={type}
            onValueChange={(value) => {
              setType(value === 'RECOMMENDED' ? 'RECOMMENDED' : 'REQUIRED')
            }}
            options={['REQUIRED', 'RECOMMENDED'].map((value) => ({
              value,
              label: t(`admin.requirement.${value}`),
            }))}
          />
          <Button
            variant="outline"
            disabled={chosen === undefined}
            loading={add.isPending}
            onClick={() => {
              if (chosen !== undefined) add.mutate({ prerequisiteSubjectId: chosen, requirementType: type })
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('authoring.add')}
          </Button>
        </div>
        <Problem error={add.error ?? remove.error} />
      </CardContent>
    </Card>
  )
}

const CATEGORIES = ['KNOWLEDGE', 'SKILL', 'COMPETENCY', 'ATTITUDE'] as const

function Outcomes({ subject }: { subject: SubjectDetail }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const add = useAddOutcome(subject.id ?? '')
  const [statement, setStatement] = useState('')
  const [category, setCategory] = useState<(typeof CATEGORIES)[number]>('KNOWLEDGE')
  const outcomes = (subject.learningOutcomes ?? [])
    .slice()
    .sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
  return (
    <Card>
      <CardHeader title={t('admin.outcomes')} description={t('admin.outcomesBody')} />
      <CardContent className="grid grid-cols-1 gap-3">
        <ol className="grid grid-cols-1 gap-2">
          {outcomes.map((outcome) => {
            const text =
              outcome.translations?.find((row) => row.locale === locale) ?? outcome.translations?.[0]
            return (
              <li key={outcome.id} className="grid grid-cols-1 gap-0.5">
                <span className="text-caption text-text-muted font-medium">
                  {outcome.code} · {t(`admin.outcomeCategory.${outcome.category ?? 'KNOWLEDGE'}`)}
                </span>
                <span className="text-body text-text">{text?.statement ?? '—'}</span>
              </li>
            )
          })}
        </ol>
        <form
          className="grid grid-cols-1 gap-2"
          onSubmit={(event) => {
            event.preventDefault()
            if (statement.trim() === '') return
            add.mutate(
              {
                code: `LO${outcomes.length + 1}`,
                category,
                displayOrder: outcomes.length + 1,
                statement: statement.trim(),
                locale,
              },
              {
                onSuccess: () => {
                  setStatement('')
                },
              },
            )
          }}
        >
          <Textarea
            label={t('admin.outcomeStatement', { locale: locale.toUpperCase() })}
            rows={2}
            value={statement}
            onChange={(event) => {
              setStatement(event.target.value)
            }}
          />
          <div className="flex flex-wrap items-end gap-2">
            <Select
              className="w-48"
              label={t('admin.category')}
              value={category}
              onValueChange={(value) => {
                setCategory(CATEGORIES.find((item) => item === value) ?? 'KNOWLEDGE')
              }}
              options={CATEGORIES.map((value) => ({ value, label: t(`admin.outcomeCategory.${value}`) }))}
            />
            <Button
              type="submit"
              variant="outline"
              disabled={statement.trim() === ''}
              loading={add.isPending}
            >
              <Plus aria-hidden className="size-4" />
              {t('authoring.add')}
            </Button>
          </div>
          <Problem error={add.error} />
        </form>
      </CardContent>
    </Card>
  )
}
