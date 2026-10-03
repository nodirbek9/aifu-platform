import { useId } from 'react'
import * as SelectPrimitive from '@radix-ui/react-select'
import { Check, ChevronDown } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type SelectOption = {
  value: string
  label: string
}

export type SelectProps = {
  value?: string
  onValueChange?: (value: string) => void
  options: readonly SelectOption[]
  placeholder?: string
  label?: string
  error?: string
  disabled?: boolean
  id?: string
  className?: string
}

export function Select({
  value,
  onValueChange,
  options,
  placeholder,
  label,
  error,
  disabled,
  id,
  className,
}: SelectProps) {
  const generatedId = useId()
  const selectId = id ?? generatedId
  const errorId = `${selectId}-error`
  return (
    <div className={cn('grid gap-1.5', className)}>
      {label !== undefined && (
        <label htmlFor={selectId} className="text-small text-text font-medium">
          {label}
        </label>
      )}
      <SelectPrimitive.Root value={value} onValueChange={onValueChange} disabled={disabled}>
        <SelectPrimitive.Trigger
          id={selectId}
          aria-invalid={error !== undefined || undefined}
          aria-describedby={error !== undefined ? errorId : undefined}
          className={cn(
            'border-border-strong bg-surface-raised text-body text-text flex h-11 w-full min-w-0 items-center justify-between gap-2 rounded-sm border px-3',
            'duration-fast ease-standard transition-[border-color,box-shadow]',
            'hover:border-text-muted/50',
            'focus-visible:border-primary-hover focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-0',
            'disabled:bg-surface-sunken disabled:cursor-not-allowed disabled:opacity-70',
            'aria-invalid:border-danger',
            'data-[placeholder]:text-text-muted/70',
          )}
        >
          <span className="min-w-0 flex-1 truncate text-left">
            <SelectPrimitive.Value placeholder={placeholder} />
          </span>
          <SelectPrimitive.Icon className="shrink-0">
            <ChevronDown aria-hidden className="text-text-muted size-4" />
          </SelectPrimitive.Icon>
        </SelectPrimitive.Trigger>
        <SelectPrimitive.Portal>
          <SelectPrimitive.Content
            position="popper"
            sideOffset={4}
            className={cn(
              'border-border bg-surface-raised shadow-menu z-50 max-h-72 min-w-(--radix-select-trigger-width) overflow-y-auto rounded-md border p-1',
              'data-[state=open]:animate-menu-in',
            )}
          >
            <SelectPrimitive.Viewport>
              {options.map((option) => (
                <SelectPrimitive.Item
                  key={option.value}
                  value={option.value}
                  className={cn(
                    'text-body text-text flex cursor-pointer items-center justify-between gap-2 rounded-sm px-2.5 py-2 outline-none select-none',
                    'data-[highlighted]:bg-surface-sunken',
                  )}
                >
                  <SelectPrimitive.ItemText>{option.label}</SelectPrimitive.ItemText>
                  <SelectPrimitive.ItemIndicator>
                    <Check aria-hidden className="text-primary-hover size-4" />
                  </SelectPrimitive.ItemIndicator>
                </SelectPrimitive.Item>
              ))}
            </SelectPrimitive.Viewport>
          </SelectPrimitive.Content>
        </SelectPrimitive.Portal>
      </SelectPrimitive.Root>
      {error !== undefined && (
        <p id={errorId} role="alert" className="text-small text-danger">
          {error}
        </p>
      )}
    </div>
  )
}
