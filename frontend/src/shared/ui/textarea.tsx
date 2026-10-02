import { forwardRef, useId, type TextareaHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'
import { input } from './input'

export type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement> & {
  label?: string
  error?: string
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { className, label, error, id, rows = 4, ...props },
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
      <textarea
        ref={ref}
        id={inputId}
        rows={rows}
        aria-invalid={error !== undefined || undefined}
        aria-describedby={error !== undefined ? errorId : undefined}
        className={cn(input({}), 'h-auto min-h-24 py-2', className)}
        {...props}
      />
      {error !== undefined && (
        <p id={errorId} role="alert" className="text-small text-danger">
          {error}
        </p>
      )}
    </div>
  )
})
