import { useId } from 'react'
import * as RadioGroupPrimitive from '@radix-ui/react-radio-group'
import { cn } from '@/shared/lib/cn'

export type SegmentedOption = {
  value: string
  label: string
  disabled?: boolean
}

export type SegmentedControlProps = {
  value: string
  onValueChange: (value: string) => void
  options: readonly SegmentedOption[]
  'aria-label': string
  size?: 'sm' | 'md'
  className?: string
}

/** A single control made of adjacent segments (UZ / RU / EN, light / dark / system). */
export function SegmentedControl({
  value,
  onValueChange,
  options,
  'aria-label': ariaLabel,
  size = 'md',
  className,
}: SegmentedControlProps) {
  const name = useId()
  return (
    <RadioGroupPrimitive.Root
      value={value}
      onValueChange={onValueChange}
      name={name}
      aria-label={ariaLabel}
      className={cn(
        'border-border bg-surface-sunken inline-flex items-center gap-0.5 rounded-sm border p-0.5',
        className,
      )}
    >
      {options.map((option) => (
        <RadioGroupPrimitive.Item
          key={option.value}
          value={option.value}
          disabled={option.disabled}
          className={cn(
            'text-text-muted rounded-[4px] px-3 font-medium',
            size === 'sm' ? 'text-caption h-8 tracking-normal' : 'text-small h-9',
            'duration-fast ease-standard transition-[background-color,color]',
            'hover:text-text',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
            'disabled:pointer-events-none disabled:opacity-50',
            'data-[state=checked]:bg-surface-raised data-[state=checked]:text-text data-[state=checked]:shadow-sm',
          )}
        >
          {option.label}
        </RadioGroupPrimitive.Item>
      ))}
    </RadioGroupPrimitive.Root>
  )
}
