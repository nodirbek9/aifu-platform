import { forwardRef, useId, type InputHTMLAttributes, type ReactNode } from 'react'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'
import { cn } from '@/shared/lib/cn'

export const input = tv({
  base: [
    'w-full rounded-sm border border-border-strong bg-surface-raised px-3 text-body text-text',
    'placeholder:text-text-muted/70',
    'transition-[border-color,box-shadow] duration-fast ease-standard',
    'hover:border-text-muted/50',
    'focus-visible:border-primary-hover focus-visible:outline-2 focus-visible:outline-offset-0 focus-visible:outline-focus-ring',
    'disabled:cursor-not-allowed disabled:bg-surface-sunken disabled:opacity-70',
    'aria-invalid:border-danger aria-invalid:focus-visible:outline-danger',
  ],
  variants: {
    size: {
      md: 'h-11',
      lg: 'h-12',
    },
  },
  defaultVariants: { size: 'md' },
})

export type InputProps = InputHTMLAttributes<HTMLInputElement> &
  VariantProps<typeof input> & {
    label?: string
    error?: string
    trailing?: ReactNode
  }

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { className, size, label, error, trailing, id, ...props },
  ref,
) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const errorId = `${inputId}-error`
  return (
    <div className="grid gap-1.5">
      {label !== undefined && (
        <label htmlFor={inputId} className="text-small text-text font-medium">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          ref={ref}
          id={inputId}
          aria-invalid={error !== undefined || undefined}
          aria-describedby={error !== undefined ? errorId : undefined}
          className={cn(input({ size, class: className }), trailing !== undefined && 'pr-11')}
          {...props}
        />
        {trailing !== undefined && (
          <div className="absolute inset-y-0 right-1 flex items-center">{trailing}</div>
        )}
      </div>
      {error !== undefined && (
        <p id={errorId} role="alert" className="text-small text-danger">
          {error}
        </p>
      )}
    </div>
  )
})
