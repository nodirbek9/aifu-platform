import { forwardRef, useId, type ReactNode } from 'react'
import * as SwitchPrimitive from '@radix-ui/react-switch'
import { cn } from '@/shared/lib/cn'

export type SwitchProps = {
  checked?: boolean
  onCheckedChange?: (checked: boolean) => void
  disabled?: boolean
  label?: ReactNode
  id?: string
  className?: string
}

export const Switch = forwardRef<HTMLButtonElement, SwitchProps>(function Switch(
  { checked, onCheckedChange, disabled, label, id, className },
  ref,
) {
  const generatedId = useId()
  const switchId = id ?? generatedId
  return (
    <div className={cn('flex items-center gap-2.5', className)}>
      <SwitchPrimitive.Root
        ref={ref}
        id={switchId}
        checked={checked}
        onCheckedChange={onCheckedChange}
        disabled={disabled}
        className={cn(
          'border-border-strong bg-surface-sunken relative h-6 w-11 shrink-0 rounded-full border',
          'duration-normal ease-standard transition-[background-color,border-color]',
          'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          'disabled:cursor-not-allowed disabled:opacity-50',
          'data-[state=checked]:border-primary data-[state=checked]:bg-primary',
        )}
      >
        <SwitchPrimitive.Thumb
          className={cn(
            'bg-surface-raised block size-5 translate-x-0.5 rounded-full shadow-sm',
            'duration-normal ease-standard transition-transform',
            'data-[state=checked]:translate-x-[22px]',
          )}
        />
      </SwitchPrimitive.Root>
      {label !== undefined && (
        <label htmlFor={switchId} className="text-body text-text select-none">
          {label}
        </label>
      )}
    </div>
  )
})
