import { useEffect, useRef } from 'react'
import { indentWithTab } from '@codemirror/commands'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'
import { HighlightStyle, indentUnit, syntaxHighlighting, type LanguageSupport } from '@codemirror/language'
import { Compartment, EditorState, Prec, type Extension } from '@codemirror/state'
import { EditorView, keymap } from '@codemirror/view'
import { tags } from '@lezer/highlight'
import { basicSetup } from 'codemirror'
import { cn } from '@/shared/lib/cn'
import type { EditorLanguage } from './languages'

const LANGUAGES: Record<EditorLanguage, () => LanguageSupport> = { python, java, cpp }

/* Colours come from the design tokens (tokens.css), so the editor follows the theme by itself. */
const highlight = HighlightStyle.define([
  {
    tag: [tags.comment, tags.lineComment, tags.blockComment],
    color: 'var(--syntax-comment)',
    fontStyle: 'italic',
  },
  {
    tag: [tags.keyword, tags.modifier, tags.controlKeyword, tags.operatorKeyword],
    color: 'var(--syntax-keyword)',
  },
  { tag: [tags.string, tags.special(tags.string), tags.character], color: 'var(--syntax-string)' },
  { tag: [tags.number, tags.bool, tags.null, tags.atom], color: 'var(--syntax-number)' },
  {
    tag: [tags.function(tags.variableName), tags.function(tags.propertyName)],
    color: 'var(--syntax-function)',
  },
  {
    tag: [tags.typeName, tags.className, tags.namespace, tags.standard(tags.name)],
    color: 'var(--syntax-type)',
  },
  { tag: [tags.processingInstruction, tags.meta, tags.annotation], color: 'var(--syntax-keyword)' },
  { tag: tags.invalid, color: 'var(--danger)' },
])

const chrome = EditorView.theme({
  '&': { color: 'var(--text)', backgroundColor: 'transparent', height: '100%' },
  '&.cm-focused': { outline: 'none' },
  '.cm-scroller': { fontFamily: 'var(--font-mono)', lineHeight: '1.6', overflow: 'auto' },
  '.cm-content': { caretColor: 'var(--text)', padding: '12px 0' },
  '.cm-cursor, .cm-dropCursor': { borderLeftColor: 'var(--text)' },
  '.cm-gutters': { backgroundColor: 'transparent', color: 'var(--text-muted)', border: 'none' },
  '.cm-lineNumbers .cm-gutterElement': { padding: '0 12px 0 16px', opacity: '0.7' },
  '.cm-activeLine': { backgroundColor: 'color-mix(in srgb, var(--text) 5%, transparent)' },
  '.cm-activeLineGutter': { backgroundColor: 'transparent', color: 'var(--text)', opacity: '1' },
  '&.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground, .cm-selectionBackground, ::selection':
    {
      backgroundColor: 'color-mix(in srgb, var(--primary) 22%, transparent)',
    },
  '.cm-matchingBracket, &.cm-focused .cm-matchingBracket': {
    backgroundColor: 'color-mix(in srgb, var(--primary) 18%, transparent)',
    outline: '1px solid var(--border-strong)',
  },
  '.cm-tooltip': {
    backgroundColor: 'var(--surface-raised)',
    border: '1px solid var(--border)',
    borderRadius: '6px',
    color: 'var(--text)',
  },
  '.cm-tooltip-autocomplete ul li[aria-selected]': {
    backgroundColor: 'var(--surface-sunken)',
    color: 'var(--text)',
  },
  '.cm-panels': { backgroundColor: 'var(--surface-sunken)', color: 'var(--text)' },
  '.cm-searchMatch': { backgroundColor: 'color-mix(in srgb, var(--warning) 30%, transparent)' },
})

function fontSizeTheme(fontSize: number): Extension {
  return EditorView.theme({ '.cm-scroller': { fontSize: `${fontSize}px` } })
}

export type CodeEditorProps = {
  value: string
  language: EditorLanguage
  /** Called on every edit by the user. Absent for a read-only view. */
  onChange?: (value: string) => void
  readOnly?: boolean
  fontSize?: number
  /** Ctrl/Cmd+Enter. */
  onRun?: () => void
  /** Ctrl/Cmd+Shift+Enter. */
  onSubmit?: () => void
  'aria-label': string
  className?: string
}

/**
 * The CodeMirror 6 editor of the platform: Python, Java and C++ with highlighting, auto-indent,
 * bracket matching, line numbers and a four-space tab. Also used read-only wherever code is
 * shown (lesson examples, a past submission). This module is heavy: import it lazily.
 */
export function CodeEditor({
  value,
  language,
  onChange,
  readOnly = false,
  fontSize = 14,
  onRun,
  onSubmit,
  'aria-label': ariaLabel,
  className,
}: CodeEditorProps) {
  const hostRef = useRef<HTMLDivElement>(null)
  const viewRef = useRef<EditorView | null>(null)
  const compartments = useRef({
    language: new Compartment(),
    readOnly: new Compartment(),
    fontSize: new Compartment(),
  })
  // the latest callbacks, read by the editor's long-lived listeners
  const handlers = useRef({ onChange, onRun, onSubmit })
  useEffect(() => {
    handlers.current = { onChange, onRun, onSubmit }
  })

  useEffect(() => {
    const host = hostRef.current
    if (host === null) return
    const { language: languageSlot, readOnly: readOnlySlot, fontSize: fontSizeSlot } = compartments.current
    const view = new EditorView({
      parent: host,
      state: EditorState.create({
        doc: value,
        extensions: [
          // before the default keymap, where Mod-Enter inserts a blank line
          Prec.highest(
            keymap.of([
              {
                key: 'Mod-Enter',
                run: () => {
                  handlers.current.onRun?.()
                  return handlers.current.onRun !== undefined
                },
              },
              {
                key: 'Mod-Shift-Enter',
                run: () => {
                  handlers.current.onSubmit?.()
                  return handlers.current.onSubmit !== undefined
                },
              },
            ]),
          ),
          basicSetup,
          keymap.of([indentWithTab]),
          indentUnit.of('    '),
          EditorState.tabSize.of(4),
          languageSlot.of(LANGUAGES[language]()),
          readOnlySlot.of([EditorState.readOnly.of(readOnly), EditorView.editable.of(!readOnly)]),
          fontSizeSlot.of(fontSizeTheme(fontSize)),
          chrome,
          syntaxHighlighting(highlight),
          EditorView.contentAttributes.of({ 'aria-label': ariaLabel }),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) handlers.current.onChange?.(update.state.doc.toString())
          }),
        ],
      }),
    })
    viewRef.current = view
    return () => {
      view.destroy()
      viewRef.current = null
    }
    // The editor is created once; the effects below keep it in step with the props.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // a value set from outside (another draft, a restored submission) replaces the document
  useEffect(() => {
    const view = viewRef.current
    if (view === null || view.state.doc.toString() === value) return
    view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: value } })
  }, [value])

  useEffect(() => {
    viewRef.current?.dispatch({ effects: compartments.current.language.reconfigure(LANGUAGES[language]()) })
  }, [language])

  useEffect(() => {
    viewRef.current?.dispatch({
      effects: compartments.current.readOnly.reconfigure([
        EditorState.readOnly.of(readOnly),
        EditorView.editable.of(!readOnly),
      ]),
    })
  }, [readOnly])

  useEffect(() => {
    viewRef.current?.dispatch({ effects: compartments.current.fontSize.reconfigure(fontSizeTheme(fontSize)) })
  }, [fontSize])

  return <div ref={hostRef} className={cn('min-h-0 overflow-hidden', className)} />
}
