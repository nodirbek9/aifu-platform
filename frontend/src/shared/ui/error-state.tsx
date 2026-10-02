import { useTranslation } from 'react-i18next'
import { CircleAlert } from 'lucide-react'
import { cn } from '@/shared/lib/cn'
import type { ApiProblem } from '@/shared/api/errors'
import { Button } from './button'

export type ErrorStateProps = {
  problem?: ApiProblem
  onRetry?: () => void
  className?: string
}

/** Every error state: a translated message by code, a retry, and the request id. */
export function ErrorState({ problem, onRetry, className }: ErrorStateProps) {
  const { t } = useTranslation()
  const message = problem !== undefined ? t(`errors.${problem.code}`) : undefined
  return (
    <div
      role="alert"
      className={cn(
        'border-danger/40 bg-danger-soft flex flex-col items-start gap-3 rounded-lg border px-6 py-8',
        className,
      )}
    >
      <CircleAlert aria-hidden className="text-danger size-6" />
      <div className="grid gap-1">
        <p className="font-display text-title text-text font-semibold">{t('common.errorStateTitle')}</p>
        <p className="text-small text-text-muted max-w-prose">{message ?? t('common.errorStateBody')}</p>
      </div>
      {onRetry !== undefined && (
        <Button variant="outline" size="sm" onClick={onRetry}>
          {t('common.retry')}
        </Button>
      )}
      {problem?.requestId !== undefined && (
        <p className="text-caption text-text-muted">
          {t('common.requestId')}: <span className="font-mono select-all">{problem.requestId}</span>
        </p>
      )}
    </div>
  )
}
