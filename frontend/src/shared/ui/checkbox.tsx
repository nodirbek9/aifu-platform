import { forwardRef, useId, type ReactNode } from 'react'
import * as CheckboxPrimitive from '@radix-ui/react-checkbox'
import { Check } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type CheckboxProps = {
  checked?: boolean
  onCheckedChange?: (checked: boolean) => void
  disabled?: boolean
  label?: ReactNode
  id?: string
  className?: string
}

export const Checkbox = forwardRef<HTMLButtonElement, CheckboxProps>(function Checkbox(
  { checked, onCheckedChange, disabled, label, id, className },
  ref,
) {
  const generatedId = useId()
  const checkboxId = id ?? generatedId
  return (
    <div className={cn('flex items-center gap-2.5', className)}>
      <CheckboxPrimitive.Root
        ref={ref}
        id={checkboxId}
        checked={checked}
        onCheckedChange={(value) => {
          onCheckedChange?.(value === true)
        }}
        disabled={disabled}
        className={cn(
          'border-border-strong bg-surface-raised relative flex size-5 shrink-0 items-center justify-center rounded-[4px] border',
          'duration-fast ease-standard transition-[background-color,border-color]',
          'hover:border-text-muted/60',
          'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          'disabled:cursor-not-allowed disabled:opacity-50',
          'data-[state=checked]:border-primary data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground',
        )}
      >
        <CheckboxPrimitive.Indicator>
          <Check aria-hidden className="size-3.5" strokeWidth={3} />
        </CheckboxPrimitive.Indicator>
      </CheckboxPrimitive.Root>
      {label !== undefined && (
        <label htmlFor={checkboxId} className="text-body text-text select-none">
          {label}
        </label>
      )}
    </div>
  )
})
