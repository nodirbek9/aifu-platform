import { useEffect } from 'react'
import { TableKit } from '@tiptap/extension-table'
import { EditorContent, useEditor, useEditorState, type Editor } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { Bold, Code, Heading2, Heading3, Italic, List, ListOrdered, Pilcrow, Table } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { input } from '@/shared/ui/input'
import { richTextClasses } from '@/shared/ui/rich-text'

type Action = {
  key: string
  Icon: typeof Bold
  run: (editor: Editor) => void
  active: (editor: Editor) => boolean
}

const ACTIONS: readonly Action[] = [
  {
    key: 'paragraph',
    Icon: Pilcrow,
    run: (e) => e.chain().focus().setParagraph().run(),
    active: (e) => e.isActive('paragraph'),
  },
  {
    key: 'h2',
    Icon: Heading2,
    run: (e) => e.chain().focus().toggleHeading({ level: 2 }).run(),
    active: (e) => e.isActive('heading', { level: 2 }),
  },
  {
    key: 'h3',
    Icon: Heading3,
    run: (e) => e.chain().focus().toggleHeading({ level: 3 }).run(),
    active: (e) => e.isActive('heading', { level: 3 }),
  },
  {
    key: 'bold',
    Icon: Bold,
    run: (e) => e.chain().focus().toggleBold().run(),
    active: (e) => e.isActive('bold'),
  },
  {
    key: 'italic',
    Icon: Italic,
    run: (e) => e.chain().focus().toggleItalic().run(),
    active: (e) => e.isActive('italic'),
  },
  {
    key: 'code',
    Icon: Code,
    run: (e) => e.chain().focus().toggleCode().run(),
    active: (e) => e.isActive('code'),
  },
  {
    key: 'list',
    Icon: List,
    run: (e) => e.chain().focus().toggleBulletList().run(),
    active: (e) => e.isActive('bulletList'),
  },
  {
    key: 'orderedList',
    Icon: ListOrdered,
    run: (e) => e.chain().focus().toggleOrderedList().run(),
    active: (e) => e.isActive('orderedList'),
  },
  {
    key: 'table',
    Icon: Table,
    run: (e) => e.chain().focus().insertTable({ rows: 3, cols: 2, withHeaderRow: true }).run(),
    active: (e) => e.isActive('table'),
  },
]

/** The editor's text as the server keeps it: no empty paragraph left at the end. */
function htmlOf(editor: Editor): string {
  return editor.isEmpty ? '' : editor.getHTML().replace(/(?:<p><\/p>)+$/, '')
}

type VisualEditorProps = {
  value: string
  onChange: (value: string) => void
  labelledBy: string
  id: string
}

/**
 * The rich text typed as the student will see it (TipTap): the same marks the HTML toolbar
 * offers, written straight into the page. Loaded only when a teacher opens an editor.
 */
export default function VisualEditor({ value, onChange, labelledBy, id }: VisualEditorProps) {
  const { t } = useTranslation()
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: { levels: [2, 3] },
        link: { openOnClick: false },
        trailingNode: false,
      }),
      TableKit.configure({ table: { resizable: false } }),
    ],
    content: value,
    editorProps: {
      attributes: {
        id,
        role: 'textbox',
        'aria-multiline': 'true',
        'aria-labelledby': labelledBy,
        class: cn(
          input({}),
          richTextClasses,
          'h-auto min-h-40 max-w-none rounded-t-none py-3 focus-visible:outline-none',
        ),
      },
    },
    onUpdate: ({ editor: current }) => {
      onChange(htmlOf(current))
    },
  })
  const active = useEditorState({
    editor,
    selector: ({ editor: current }) => ACTIONS.map((action) => action.active(current)),
  })

  // the text changed from outside (another language, a reload): the editor shows it
  useEffect(() => {
    // a destroyed editor (replaced by TipTap on remount) has no schema left to read
    if (editor.isDestroyed || editor.isFocused) return
    const shown = htmlOf(editor)
    if (shown !== value) editor.commands.setContent(value, { emitUpdate: false })
  }, [editor, value])

  return (
    <div className="grid grid-cols-1">
      <div
        role="toolbar"
        aria-label={t('authoring.formatting')}
        aria-controls={id}
        className="border-border bg-surface-sunken flex flex-wrap gap-0.5 rounded-t-md border border-b-0 p-1"
      >
        {ACTIONS.map((action, index) => (
          <button
            key={action.key}
            type="button"
            title={t(`authoring.format.${action.key}`)}
            aria-label={t(`authoring.format.${action.key}`)}
            aria-pressed={active[index] === true}
            onClick={() => {
              action.run(editor)
            }}
            className="text-text-muted hover:bg-surface-raised hover:text-text aria-pressed:bg-surface-raised aria-pressed:text-text focus-visible:outline-focus-ring grid size-8 grid-cols-1 place-items-center rounded-sm focus-visible:outline-2 focus-visible:outline-offset-2"
          >
            <action.Icon aria-hidden className="size-4" />
          </button>
        ))}
      </div>
      <EditorContent editor={editor} />
    </div>
  )
}
