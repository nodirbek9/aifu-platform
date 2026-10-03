import { useTranslation } from 'react-i18next'
import { SUPPORTED_LOCALES, type Locale } from '@/shared/i18n'
import { cn } from '@/shared/lib/cn'

type LocaleTabsProps = {
  value: Locale
  onValueChange: (locale: Locale) => void
  /** Whether the text exists in a language: a missing one is marked. */
  has: (locale: Locale) => boolean
  /** The languages the course must have before it is published: their gap is a warning. */
  required: readonly Locale[]
  className?: string
}

/**
 * UZ / RU / EN above a translatable text, each with a dot when that language is still missing —
 * amber where the course requires it (publishing is refused without it), grey where it does not.
 */
export function LocaleTabs({ value, onValueChange, has, required, className }: LocaleTabsProps) {
  const { t } = useTranslation()
  return (
    <div role="group" aria-label={t('authoring.language')} className={cn('flex gap-1', className)}>
      {SUPPORTED_LOCALES.map((locale) => {
        const missing = !has(locale)
        const needed = missing && required.includes(locale)
        return (
          <button
            key={locale}
            type="button"
            aria-pressed={value === locale}
            onClick={() => {
              onValueChange(locale)
            }}
            className={cn(
              'text-small inline-flex h-8 items-center gap-1.5 rounded-sm border px-2.5 font-semibold uppercase',
              value === locale
                ? 'border-primary bg-primary text-primary-foreground'
                : 'border-border bg-surface-raised text-text-muted hover:text-text',
            )}
          >
            {locale}
            {missing && (
              <>
                <span
                  aria-hidden
                  className={cn('size-1.5 rounded-full', needed ? 'bg-warning' : 'bg-text-muted')}
                />
                <span className="sr-only">
                  {needed ? t('authoring.translationRequired') : t('authoring.translationMissing')}
                </span>
              </>
            )}
          </button>
        )
      })}
    </div>
  )
}
