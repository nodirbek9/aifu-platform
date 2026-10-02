/** The teaching languages of the platform (spec S-05): the ones the editor has a mode for. */
export const EDITOR_LANGUAGES = ['python', 'java', 'cpp'] as const
export type EditorLanguage = (typeof EDITOR_LANGUAGES)[number]

export function isEditorLanguage(value: string): value is EditorLanguage {
  return (EDITOR_LANGUAGES as readonly string[]).includes(value)
}

const NAMES: Record<EditorLanguage, string> = { python: 'Python', java: 'Java', cpp: 'C++' }

/** "C++" for "cpp"; an unknown code is shown as it is. */
export function languageName(code: string): string {
  return isEditorLanguage(code) ? NAMES[code] : code
}

/** The browser preference shared by every language choice: lesson examples and the task editor. */
export const CODE_LANGUAGE_KEY = 'aifu-code-language'
