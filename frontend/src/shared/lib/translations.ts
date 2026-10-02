import { DEFAULT_LOCALE, type Locale } from '@/shared/i18n'

type Translated = { locale?: string }

export type PickedTranslation<T> = {
  value: T
  /** True when the text is not in the language that was asked for (spec G-01). */
  isFallback: boolean
}

/**
 * Chooses the translation for the interface language from a `translations[]` array of the API:
 * the asked language, else Uzbek (the language every record has), else whatever exists.
 */
export function pickTranslation<T extends Translated>(
  translations: readonly T[] | undefined,
  locale: Locale,
): PickedTranslation<T> | null {
  if (translations === undefined || translations.length === 0) return null
  const exact = translations.find((item) => item.locale === locale)
  if (exact !== undefined) return { value: exact, isFallback: false }
  const fallback = translations.find((item) => item.locale === DEFAULT_LOCALE) ?? translations[0]
  return fallback === undefined ? null : { value: fallback, isFallback: true }
}

/** Reads a string property of an untyped JSON object (block `payload` and `settings`). */
export function readString(source: unknown, key: string): string | undefined {
  if (typeof source !== 'object' || source === null) return undefined
  const value = (source as Record<string, unknown>)[key]
  return typeof value === 'string' && value !== '' ? value : undefined
}
