import { Languages } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'

/** A small note that a text is shown in Uzbek because it has no translation yet (spec G-01). */
export function FallbackMark({ className }: { className?: string }) {
  const { t } = useTranslation()
  return (
    <span
      className={cn(
        'text-caption text-text-muted inline-flex items-center gap-1.5 tracking-normal',
        className,
      )}
    >
      <Languages aria-hidden className="size-3.5" />
      {t('common.shownInUzbek')}
    </span>
  )
}
