import { useEffect, useMemo, useState, type ReactNode } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { Search } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'

export type CommandItem = {
  id: string
  label: string
  hint?: string
  group?: string
  icon?: ReactNode
  keywords?: string
  onSelect: () => void
}

export type CommandPaletteProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  items: readonly CommandItem[]
}

/** Ctrl+K palette: one input, a filtered list, arrows + Enter. Global search lands in F3. */
export function CommandPalette({ open, onOpenChange, items }: CommandPaletteProps) {
  const { t } = useTranslation()

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        onOpenChange(!open)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => {
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [open, onOpenChange])

  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="bg-brand-navy/50 data-[state=open]:animate-overlay-in fixed inset-0 z-40" />
        <DialogPrimitive.Content
          aria-label={t('common.commandPalette')}
          className={cn(
            'fixed top-[15vh] left-1/2 z-50 w-[calc(100vw-2rem)] max-w-lg -translate-x-1/2',
            'border-border bg-surface-raised shadow-dialog overflow-hidden rounded-lg border',
            'data-[state=open]:animate-dialog-in',
          )}
        >
          <DialogPrimitive.Title className="sr-only">{t('common.commandPalette')}</DialogPrimitive.Title>
          <DialogPrimitive.Description className="sr-only">
            {t('common.commandPlaceholder')}
          </DialogPrimitive.Description>
          <CommandList
            items={items}
            onSelect={(item) => {
              onOpenChange(false)
              item.onSelect()
            }}
          />
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  )
}

type CommandListProps = {
  items: readonly CommandItem[]
  onSelect: (item: CommandItem) => void
}

/** Mounted only while the palette is open, so the query and the cursor start fresh every time. */
function CommandList({ items, onSelect }: CommandListProps) {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')
  const [activeIndex, setActiveIndex] = useState(0)
  const listId = 'command-list'

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (normalized === '') return items
    return items.filter((item) =>
      `${item.label} ${item.hint ?? ''} ${item.keywords ?? ''}`.toLowerCase().includes(normalized),
    )
  }, [items, query])

  const activeItem = filtered[activeIndex]

  return (
    <>
      <div className="border-border flex items-center gap-2.5 border-b px-4">
        <Search aria-hidden className="text-text-muted size-4 shrink-0" />
        <input
          role="combobox"
          aria-expanded="true"
          aria-controls={listId}
          aria-activedescendant={activeItem !== undefined ? `${listId}-${activeItem.id}` : undefined}
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setActiveIndex(0)
          }}
          onKeyDown={(event) => {
            if (event.key === 'ArrowDown') {
              event.preventDefault()
              setActiveIndex((index) => Math.min(index + 1, filtered.length - 1))
            } else if (event.key === 'ArrowUp') {
              event.preventDefault()
              setActiveIndex((index) => Math.max(index - 1, 0))
            } else if (event.key === 'Enter' && activeItem !== undefined) {
              event.preventDefault()
              onSelect(activeItem)
            }
          }}
          placeholder={t('common.commandPlaceholder')}
          className="text-body text-text placeholder:text-text-muted/70 h-12 w-full bg-transparent outline-none"
        />
        <kbd className="border-border bg-surface-sunken text-caption text-text-muted shrink-0 rounded border px-1.5 py-0.5">
          Esc
        </kbd>
      </div>
      <div role="listbox" id={listId} className="max-h-80 overflow-y-auto p-1.5">
        {filtered.length === 0 && (
          <p className="text-small text-text-muted px-3 py-8 text-center">{t('common.commandEmpty')}</p>
        )}
        {filtered.map((item, index) => (
          <div
            key={item.id}
            id={`${listId}-${item.id}`}
            role="option"
            aria-selected={index === activeIndex}
            onMouseEnter={() => {
              setActiveIndex(index)
            }}
            onClick={() => {
              onSelect(item)
            }}
            className={cn(
              'text-small flex cursor-pointer items-center gap-2.5 rounded-sm px-3 py-2.5',
              index === activeIndex ? 'bg-surface-sunken text-text' : 'text-text',
            )}
          >
            {item.icon !== undefined && <span className="text-text-muted">{item.icon}</span>}
            <span className="flex-1">{item.label}</span>
            {item.hint !== undefined && <span className="text-caption text-text-muted">{item.hint}</span>}
          </div>
        ))}
      </div>
    </>
  )
}
