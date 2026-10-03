import { AlertTriangle, Check, Loader2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/shared/ui/button'
import type { SaveState } from './use-autosave'

/** Where saving stands, always visible beside what is edited (design brief §9, editors). */
export function SaveStatus({ state, onReload }: { state: SaveState; onReload?: () => void }) {
  const { t } = useTranslation()
  if (state.kind === 'failed') {
    const conflict = state.problem.code === 'CONCURRENT_MODIFICATION'
    return (
      <span role="alert" className="text-small text-danger flex flex-wrap items-center gap-2">
        <AlertTriangle aria-hidden className="size-4" />
        {conflict ? t('authoring.conflict') : t(`errors.${state.problem.code}`)}
        {conflict && onReload !== undefined && (
          <Button size="sm" variant="outline" onClick={onReload}>
            {t('authoring.reload')}
          </Button>
        )}
      </span>
    )
  }
  return (
    <span role="status" className="text-small text-text-muted flex items-center gap-1.5">
      {state.kind === 'saved' ? (
        <>
          <Check aria-hidden className="text-success size-4" />
          {t('quiz.saved')}
        </>
      ) : (
        <>
          <Loader2 aria-hidden className="size-4 animate-spin" />
          {t('quiz.saving')}
        </>
      )}
    </span>
  )
}
