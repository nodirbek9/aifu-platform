import { useId, type ReactNode } from 'react'
import * as RadioGroupPrimitive from '@radix-ui/react-radio-group'
import { cn } from '@/shared/lib/cn'

export type RadioOption = {
  value: string
  label: ReactNode
  disabled?: boolean
}

export type RadioGroupProps = {
  value?: string
  onValueChange?: (value: string) => void
  options: readonly RadioOption[]
  label?: string
  disabled?: boolean
  className?: string
}

export function RadioGroup({ value, onValueChange, options, label, disabled, className }: RadioGroupProps) {
  const name = useId()
  return (
    <fieldset className={cn('grid gap-2', className)}>
      {label !== undefined && <legend className="text-small text-text mb-1.5 font-medium">{label}</legend>}
      <RadioGroupPrimitive.Root
        value={value}
        onValueChange={onValueChange}
        disabled={disabled}
        name={name}
        className="grid gap-2"
      >
        {options.map((option) => (
          <div key={option.value} className="flex items-center gap-2.5">
            <RadioGroupPrimitive.Item
              value={option.value}
              id={`${name}-${option.value}`}
              disabled={option.disabled}
              className={cn(
                'border-border-strong bg-surface-raised flex size-5 items-center justify-center rounded-full border',
                'duration-fast ease-standard transition-[border-color]',
                'hover:border-text-muted/60',
                'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                'disabled:cursor-not-allowed disabled:opacity-50',
                'data-[state=checked]:border-primary',
              )}
            >
              <RadioGroupPrimitive.Indicator className="bg-primary size-2.5 rounded-full" />
            </RadioGroupPrimitive.Item>
            <label htmlFor={`${name}-${option.value}`} className="text-body text-text select-none">
              {option.label}
            </label>
          </div>
        ))}
      </RadioGroupPrimitive.Root>
    </fieldset>
  )
}
