import { useTranslation } from 'react-i18next'
import { isLocale, SUPPORTED_LOCALES, type Locale } from '@/shared/i18n'
import { SegmentedControl } from './segmented-control'

export type LanguageSwitchProps = {
  value: Locale
  onChange: (locale: Locale) => void
  size?: 'sm' | 'md'
  className?: string
}

const OPTIONS = SUPPORTED_LOCALES.map((locale) => ({
  value: locale,
  label: locale.toUpperCase(),
}))

/** UZ / RU / EN — always visible (spec G-01). Pure UI; the shell wires persistence. */
export function LanguageSwitch({ value, onChange, size = 'sm', className }: LanguageSwitchProps) {
  const { t } = useTranslation()
  return (
    <SegmentedControl
      aria-label={t('common.language')}
      value={value}
      onValueChange={(next) => {
        if (isLocale(next)) onChange(next)
      }}
      options={OPTIONS}
      size={size}
      className={className}
    />
  )
}
