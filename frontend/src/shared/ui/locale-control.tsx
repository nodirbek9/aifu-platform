import { useTranslation } from 'react-i18next'
import { SUPPORTED_LOCALES, type Locale } from '@/shared/i18n'
import { cn } from '@/shared/lib/cn'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './dropdown-menu'
import { LanguageSwitch } from './language-switch'

export type LocaleControlProps = {
  value: Locale
  onChange: (locale: Locale) => void
}

/**
 * The language switch of a top bar, always visible (spec G-01): the UZ / RU / EN segments from
 * tablet width up, and a compact menu showing the current language on a phone.
 */
export function LocaleControl({ value, onChange }: LocaleControlProps) {
  const { t } = useTranslation()
  return (
    <>
      <LanguageSwitch value={value} onChange={onChange} className="hidden md:inline-flex" />
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <button
            type="button"
            aria-label={`${t('common.language')}: ${value.toUpperCase()}`}
            className={cn(
              'text-small text-text-muted flex h-11 min-w-11 items-center justify-center rounded-sm px-2 font-semibold md:hidden',
              'duration-fast ease-standard hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
              'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
            )}
          >
            {value.toUpperCase()}
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="min-w-28">
          {SUPPORTED_LOCALES.map((option) => (
            <DropdownMenuItem
              key={option}
              onSelect={() => {
                onChange(option)
              }}
            >
              {option.toUpperCase()}
              {option === value && <span aria-hidden className="bg-primary ml-auto size-1.5 rounded-full" />}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </>
  )
}
