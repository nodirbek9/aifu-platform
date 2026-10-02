import { forwardRef, useId, type InputHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'
import { input } from './input'

export type DatePickerProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  label?: string
  error?: string
}

/** Date input (YYYY-MM-DD, as the API expects). A calendar popover refines it later. */
export const DatePicker = forwardRef<HTMLInputElement, DatePickerProps>(function DatePicker(
  { className, label, error, id, ...props },
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
      <input
        ref={ref}
        type="date"
        id={inputId}
        aria-invalid={error !== undefined || undefined}
        aria-describedby={error !== undefined ? errorId : undefined}
        className={cn(input({}), 'tabular-nums', className)}
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
