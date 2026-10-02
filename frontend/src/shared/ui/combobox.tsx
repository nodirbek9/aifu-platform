import { useId, useMemo, useState } from 'react'
import * as PopoverPrimitive from '@radix-ui/react-popover'
import { Check, ChevronDown } from 'lucide-react'
import { cn } from '@/shared/lib/cn'
import { input } from './input'

export type ComboboxOption = {
  value: string
  label: string
  hint?: string
}

export type ComboboxProps = {
  value?: string
  onValueChange: (value: string) => void
  options: readonly ComboboxOption[]
  placeholder?: string
  searchPlaceholder?: string
  emptyText?: string
  label?: string
  disabled?: boolean
  className?: string
}

/** A filterable single-select for long lists (courses, teachers, groups). */
export function Combobox({
  value,
  onValueChange,
  options,
  placeholder,
  searchPlaceholder,
  emptyText,
  label,
  disabled,
  className,
}: ComboboxProps) {
  const generatedId = useId()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const selected = options.find((option) => option.value === value)

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (normalized === '') return options
    return options.filter((option) =>
      `${option.label} ${option.hint ?? ''}`.toLowerCase().includes(normalized),
    )
  }, [options, query])

  return (
    <div className={cn('grid gap-1.5', className)}>
      {label !== undefined && (
        <label htmlFor={generatedId} className="text-small text-text font-medium">
          {label}
        </label>
      )}
      <PopoverPrimitive.Root
        open={open}
        onOpenChange={(next) => {
          setOpen(next)
          if (!next) setQuery('')
        }}
      >
        <PopoverPrimitive.Trigger asChild>
          <button
            type="button"
            id={generatedId}
            disabled={disabled}
            className={cn(
              input({}),
              'flex items-center justify-between gap-2 text-left',
              selected === undefined && 'text-text-muted/70',
            )}
          >
            <span className="truncate">{selected?.label ?? placeholder}</span>
            <ChevronDown aria-hidden className="text-text-muted size-4 shrink-0" />
          </button>
        </PopoverPrimitive.Trigger>
        <PopoverPrimitive.Portal>
          <PopoverPrimitive.Content
            align="start"
            sideOffset={4}
            className={cn(
              'border-border bg-surface-raised shadow-menu z-50 w-(--radix-popover-trigger-width) overflow-hidden rounded-md border',
              'data-[state=open]:animate-menu-in',
            )}
          >
            <div className="border-border border-b p-1.5">
              <input
                value={query}
                onChange={(event) => {
                  setQuery(event.target.value)
                }}
                placeholder={searchPlaceholder}
                className="bg-surface-sunken text-small text-text placeholder:text-text-muted/70 h-9 w-full rounded-sm px-2.5 outline-none"
              />
            </div>
            <div className="max-h-64 overflow-y-auto p-1">
              {filtered.length === 0 && (
                <p className="text-small text-text-muted px-3 py-6 text-center">{emptyText}</p>
              )}
              {filtered.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => {
                    onValueChange(option.value)
                    setOpen(false)
                  }}
                  className={cn(
                    'text-body text-text flex w-full items-center justify-between gap-2 rounded-sm px-2.5 py-2 text-left',
                    'duration-fast hover:bg-surface-sunken transition-[background-color]',
                    'focus-visible:outline-focus-ring focus-visible:outline-2',
                  )}
                >
                  <span className="grid">
                    <span>{option.label}</span>
                    {option.hint !== undefined && (
                      <span className="text-caption text-text-muted">{option.hint}</span>
                    )}
                  </span>
                  {option.value === value && (
                    <Check aria-hidden className="text-primary-hover size-4 shrink-0" />
                  )}
                </button>
              ))}
            </div>
          </PopoverPrimitive.Content>
        </PopoverPrimitive.Portal>
      </PopoverPrimitive.Root>
    </div>
  )
}
