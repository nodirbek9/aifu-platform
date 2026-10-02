import { CodeEditor } from './code-editor'
import type { EditorLanguage } from './languages'

type HighlightedCodeProps = {
  code: string
  language: EditorLanguage
  'aria-label': string
}

/** Read-only code with syntax highlighting — the body of a lesson's code listing. Import lazily. */
export default function HighlightedCode({ code, language, 'aria-label': ariaLabel }: HighlightedCodeProps) {
  return <CodeEditor aria-label={ariaLabel} value={code} language={language} readOnly />
}
