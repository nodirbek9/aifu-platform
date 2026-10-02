import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import uz from './uz.json'
import ru from './ru.json'
import en from './en.json'

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

/** Applies the locale everywhere: i18next, storage, the <html lang> attribute. */
export function applyLocale(locale: Locale): void {
  if (i18n.language !== locale) {
    void i18n.changeLanguage(locale)
  }
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, locale)
  } catch {
    /* storage unavailable */
  }
  document.documentElement.lang = locale
}

void i18n.use(initReactI18next).init({
  resources: {
    uz: { translation: uz },
    ru: { translation: ru },
    en: { translation: en },
  },
  lng: detectInitialLocale(),
  fallbackLng: DEFAULT_LOCALE,
  interpolation: { escapeValue: false },
  returnNull: false,
})

export { i18n }
