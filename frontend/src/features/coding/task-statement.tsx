import { useState } from 'react'
import { Check, Copy, Lightbulb } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import type { Activity } from '@/features/courses/model'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { formatDateTime } from '@/shared/lib/dates'
import { Button } from '@/shared/ui/button'
import { FallbackMark } from '@/shared/ui/fallback-mark'
import { RichText } from '@/shared/ui/rich-text'
import { SkeletonText } from '@/shared/ui/skeleton'
import { numberedTest, type ExampleTest, type Hint, type Task } from './model'
import { useHintsQuery, useRevealHint } from './queries'

type TaskStatementProps = {
  task: Task
  /** The activity the task is assigned through, when there is one: its deadline and state. */
  activity: Activity | undefined
  attemptsUsed: number | undefined
}

/** The left side of the task page: what to do, in what form, within what limits — and hints. */
export function TaskStatement({ task, activity, attemptsUsed }: TaskStatementProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const overdue = activity?.state === 'OVERDUE'

  const facts: { label: string; value: string; danger?: boolean }[] = []
  if (activity?.dueAt !== undefined) {
    facts.push({ label: t('task.deadline'), value: formatDateTime(activity.dueAt, locale), danger: overdue })
  }
  if (task.maxAttempts !== undefined) {
    facts.push({
      label: t('task.attempts'),
      value: attemptsUsed === undefined ? `${task.maxAttempts}` : `${attemptsUsed} / ${task.maxAttempts}`,
    })
  }
  if (task.timeLimitMs !== undefined) {
    facts.push({ label: t('task.timeLimit'), value: t('task.seconds', { count: task.timeLimitMs / 1000 }) })
  }
  if (task.memoryLimitKb !== undefined) {
    facts.push({
      label: t('task.memoryLimit'),
      value: t('task.megabytes', { count: Math.round(task.memoryLimitKb / 1024) }),
    })
  }
  if (task.maxPoints !== undefined) facts.push({ label: t('task.points'), value: `${task.maxPoints}` })

  return (
    <div className="grid content-start gap-8">
      <header className="grid gap-3">
        {task.code !== undefined && <p className="text-small text-text-muted font-mono">{task.code}</p>}
        <h1 className="font-display text-display-lg text-text font-semibold">{task.title}</h1>
        {task.titleIsFallback && <FallbackMark />}
        {facts.length > 0 && (
          <dl className="text-small flex flex-wrap gap-x-6 gap-y-2">
            {facts.map((fact) => (
              <div key={fact.label} className="grid">
                <dt className="text-caption text-text-muted font-medium uppercase">{fact.label}</dt>
                <dd
                  className={cn(
                    'tabular-nums',
                    fact.danger === true ? 'text-danger font-medium' : 'text-text',
                  )}
                >
                  {fact.value}
                </dd>
              </div>
            ))}
          </dl>
        )}
      </header>

      <RichText html={task.description} className="text-lead" />

      {task.inputOutput !== undefined && (
        <Section title={t('task.inputOutput')}>
          <p className="font-reading text-body text-text whitespace-pre-line">{task.inputOutput}</p>
        </Section>
      )}
      {task.constraints !== undefined && (
        <Section title={t('task.constraints')}>
          <p className="font-reading text-body text-text whitespace-pre-line">{task.constraints}</p>
        </Section>
      )}

      {task.examples.length > 0 && (
        <Section title={t('task.examples')}>
          <div className="grid gap-4">
            {task.examples.map((example) => (
              <Example key={example.id} example={example} />
            ))}
          </div>
        </Section>
      )}

      <Hints taskId={task.id} />
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="grid gap-2.5">
      <h2 className="font-display text-title text-text font-semibold">{title}</h2>
      {children}
    </section>
  )
}

function Example({ example }: { example: ExampleTest }) {
  const { t } = useTranslation()
  const number = numberedTest(example.name)
  return (
    <div className="border-border overflow-hidden rounded-lg border">
      <p className="border-border bg-surface-sunken text-caption text-text-muted border-b px-3.5 py-1.5 font-medium uppercase">
        {number === null ? example.name : t('task.exampleNumber', { number })}
      </p>
      <div className="divide-border grid divide-y sm:grid-cols-2 sm:divide-x sm:divide-y-0">
        <Sample label={t('task.input')} text={example.stdin} />
        <Sample label={t('task.expectedOutput')} text={example.expected} />
      </div>
    </div>
  )
}

function Sample({ label, text }: { label: string; text: string }) {
  const { t } = useTranslation()
  const [copied, setCopied] = useState(false)
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      window.setTimeout(() => {
        setCopied(false)
      }, 1600)
    } catch {
      /* clipboard unavailable */
    }
  }
  return (
    <div className="bg-surface-raised grid min-w-0 gap-1 px-3.5 py-2.5">
      <div className="flex items-center justify-between gap-2">
        <span className="text-caption text-text-muted tracking-normal">{label}</span>
        <button
          type="button"
          aria-label={copied ? t('lesson.copied') : `${t('lesson.copy')}: ${label}`}
          onClick={() => {
            void copy()
          }}
          className="text-text-muted hover:bg-surface-sunken hover:text-text focus-visible:outline-focus-ring flex size-7 items-center justify-center rounded-sm focus-visible:outline-2"
        >
          {copied ? (
            <Check aria-hidden className="text-success size-4" />
          ) : (
            <Copy aria-hidden className="size-4" />
          )}
        </button>
      </div>
      <pre className="text-small text-text overflow-x-auto font-mono whitespace-pre-wrap">{text}</pre>
    </div>
  )
}

/** Hints open one at a time, in order; the server remembers which were opened. */
function Hints({ taskId }: { taskId: string }) {
  const { t } = useTranslation()
  const locale = useLocale()
  const hints = useHintsQuery(taskId, locale)
  const reveal = useRevealHint(taskId, locale)

  if (hints.isPending) return <SkeletonText lines={2} />
  // hints are an extra: if they cannot be loaded the task is still fully usable
  if (hints.isError || hints.data.length === 0) return null

  const opened = hints.data.filter((hint) => hint.revealed)
  const next: Hint | undefined = hints.data.find((hint) => !hint.revealed)

  return (
    <Section title={t('task.hints')}>
      <div className="grid gap-3">
        {opened.map((hint, index) => (
          <div
            key={hint.id}
            className="border-border bg-surface-raised flex gap-3 rounded-md border px-4 py-3"
          >
            <Lightbulb aria-hidden className="text-warning mt-0.5 size-5 shrink-0" />
            <div className="grid gap-1">
              <p className="text-caption text-text-muted font-medium uppercase">
                {t('task.hintNumber', { number: index + 1, total: hints.data.length })}
              </p>
              <p className="text-body text-text">{hint.body}</p>
            </div>
          </div>
        ))}
        {next !== undefined && (
          <div className="flex flex-wrap items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              loading={reveal.isPending}
              onClick={() => {
                reveal.mutate(next.id)
              }}
            >
              <Lightbulb aria-hidden className="size-4" />
              {t('task.revealHint', { number: opened.length + 1, total: hints.data.length })}
            </Button>
            {reveal.isError && (
              <span role="alert" className="text-small text-danger">
                {t(`errors.${toApiProblem(reveal.error).code}`)}
              </span>
            )}
          </div>
        )}
      </div>
    </Section>
  )
}
