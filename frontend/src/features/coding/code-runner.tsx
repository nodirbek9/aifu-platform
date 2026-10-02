import { useState } from 'react'
import { Play, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem, type ApiProblem } from '@/shared/api/errors'
import { CodeEditor } from '@/shared/editor/code-editor'
import { languageName, type EditorLanguage } from '@/shared/editor/languages'
import { Button } from '@/shared/ui/button'
import { IconButton } from '@/shared/ui/icon-button'
import { Textarea } from '@/shared/ui/textarea'
import { isFinished } from './model'
import { useRunQuery, useStartRun } from './queries'
import { CustomRunResult } from './task-results'

type CodeRunnerProps = {
  initialCode: string
  language: EditorLanguage
  onClose: () => void
}

/**
 * S-04.4 "Try it": a lesson example opened in a small runner, in place. The student may change
 * the code and its input and run it; nothing is graded or saved. This module brings the editor
 * with it: import it lazily.
 */
export function CodeRunner({ initialCode, language, onClose }: CodeRunnerProps) {
  const { t } = useTranslation()
  const [code, setCode] = useState(initialCode)
  const [stdin, setStdin] = useState('')
  const [job, setJob] = useState<{ id: string; startedAt: number }>()
  const [problem, setProblem] = useState<ApiProblem | null>(null)
  const startRun = useStartRun()
  const run = useRunQuery(job?.id, job?.startedAt ?? 0)
  const busy = startRun.isPending || (run.data !== undefined && !isFinished(run.data.status))

  const start = () => {
    if (busy) return
    setProblem(null)
    startRun.mutate(
      { language, source: code, mode: 'CUSTOM', stdin },
      {
        onSuccess: ({ run: started, startedAt }) => {
          setJob({ id: started.id, startedAt })
        },
        onError: (error) => {
          setProblem(toApiProblem(error))
        },
      },
    )
  }

  return (
    <div className="border-border-strong bg-surface-raised overflow-hidden rounded-lg border">
      <div className="border-border flex items-center gap-2 border-b px-3.5 py-1.5">
        <span className="text-caption text-text-muted font-medium uppercase">
          {languageName(language)} · {t('lesson.tryIt')}
        </span>
        <div className="flex-1" />
        <Button variant="primary" size="sm" disabled={busy} onClick={start}>
          <Play aria-hidden className="size-4" />
          {t('task.run')}
        </Button>
        <IconButton size="sm" aria-label={t('lesson.tryItClose')} onClick={onClose}>
          <X aria-hidden className="size-4" />
        </IconButton>
      </div>
      <CodeEditor
        aria-label={t('task.editorLabel')}
        className="max-h-96"
        value={code}
        language={language}
        onChange={setCode}
        onRun={start}
      />
      <div className="border-border grid gap-4 border-t p-3.5">
        <Textarea
          label={t('task.customInput')}
          rows={2}
          value={stdin}
          className="font-mono"
          onChange={(event) => {
            setStdin(event.target.value)
          }}
        />
        {problem !== null && (
          <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
            {t(`errors.${problem.code}`)}
          </p>
        )}
        {run.data !== undefined && <CustomRunResult run={run.data} onRetry={start} />}
      </div>
    </div>
  )
}
