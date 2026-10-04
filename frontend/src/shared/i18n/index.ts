import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import uz from './uz.json'

export const SUPPORTED_LOCALES = ['uz', 'ru', 'en'] as const
export type Locale = (typeof SUPPORTED_LOCALES)[number]
export const DEFAULT_LOCALE: Locale = 'uz'

const LOCALE_STORAGE_KEY = 'aifu-locale'

export function isLocale(value: unknown): value is Locale {
  return typeof value === 'string' && (SUPPORTED_LOCALES as readonly string[]).includes(value)
}

export function getStoredLocale(): Locale | null {
  try {
    const stored = localStorage.getItem(LOCALE_STORAGE_KEY)
    return isLocale(stored) ? stored : null
  } catch {
    return null
  }
}

export function detectInitialLocale(): Locale {
  const stored = getStoredLocale()
  if (stored !== null) return stored
  const browserLanguages = typeof navigator === 'undefined' ? [] : navigator.languages
  for (const language of browserLanguages) {
    const short = language.slice(0, 2).toLowerCase()
    if (isLocale(short)) return short
  }
  return DEFAULT_LOCALE
}

/**
 * Uzbek is in the main bundle (every text falls back to it); Russian and English are chunks of
 * their own, fetched the first time they are needed.
 */
const loaders: Record<Exclude<Locale, 'uz'>, () => Promise<{ default: Record<string, unknown> }>> = {
  ru: () => import('./ru.json'),
  en: () => import('./en.json'),
}

async function ensureLoaded(locale: Locale): Promise<void> {
  if (locale === 'uz' || i18n.hasResourceBundle(locale, 'translation')) return
  const catalogue = await loaders[locale]()
  i18n.addResourceBundle(locale, 'translation', catalogue.default, true, true)
}

/** Applies the locale everywhere: i18next, storage, the <html lang> attribute. */
export function applyLocale(locale: Locale): void {
  if (i18n.language !== locale) {
    void ensureLoaded(locale).then(() => i18n.changeLanguage(locale))
  }
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, locale)
  } catch {
    /* storage unavailable */
  }
  document.documentElement.lang = locale
}

void i18n.use(initReactI18next).init({
  resources: { uz: { translation: uz } },
  lng: DEFAULT_LOCALE,
  fallbackLng: DEFAULT_LOCALE,
  interpolation: { escapeValue: false },
  returnNull: false,
})

const initialLocale = detectInitialLocale()

/** Resolves when the first language to show is loaded: the app renders after it. */
export const i18nReady: Promise<void> = ensureLoaded(initialLocale)
  .then(() => i18n.changeLanguage(initialLocale))
  .then(
    () => undefined,
    () => undefined,
  )

export { i18n }
