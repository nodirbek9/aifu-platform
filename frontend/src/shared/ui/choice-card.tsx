import type { ReactNode } from 'react'
import { Check, X } from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type ChoiceMark = { tone: 'right' | 'wrong'; label: string }

export type ChoiceCardProps = {
  /** `radio`: one of a group sharing `name`; `checkbox`: any number. */
  type: 'radio' | 'checkbox'
  name: string
  checked: boolean
  onChange?: () => void
  /** Shown as it was answered, not to be changed (a review). */
  readOnly?: boolean
  /** The verdict on this option in a review: said in words and by an icon, not by colour alone. */
  mark?: ChoiceMark
  children: ReactNode
}

/**
 * An answer option as a whole-row target: the row is the label of a real radio or checkbox,
 * so the keyboard, screen readers and forms treat it as the native control it is.
 */
export function ChoiceCard({
  type,
  name,
  checked,
  onChange,
  readOnly = false,
  mark,
  children,
}: ChoiceCardProps) {
  return (
    <label
      className={cn(
        'border-border bg-surface-raised flex min-h-12 items-center gap-3 rounded-md border px-4 py-3',
        'duration-fast ease-standard transition-[background-color,border-color,box-shadow]',
        'has-focus-visible:outline-focus-ring has-focus-visible:outline-2 has-focus-visible:outline-offset-2',
        !readOnly && 'hover:border-border-strong cursor-pointer',
        checked && 'border-primary ring-primary ring-1',
        mark?.tone === 'right' && 'border-success ring-success bg-success-soft ring-1',
        mark?.tone === 'wrong' && 'border-danger ring-danger bg-danger-soft ring-1',
      )}
    >
      <input
        type={type}
        name={name}
        className="peer sr-only"
        checked={checked}
        disabled={readOnly}
        onChange={() => {
          onChange?.()
        }}
      />
      <span
        aria-hidden
        className={cn(
          'border-border-strong bg-surface-raised flex size-5 shrink-0 items-center justify-center border',
          'duration-fast ease-standard transition-[background-color,border-color]',
          type === 'radio' ? 'rounded-full' : 'rounded-[4px]',
          checked && 'border-primary',
          checked && type === 'checkbox' && 'bg-primary text-primary-foreground',
        )}
      >
        {checked &&
          (type === 'radio' ? (
            <span className="bg-primary size-2.5 rounded-full" />
          ) : (
            <Check className="size-3.5" strokeWidth={3} />
          ))}
      </span>
      <span className="text-body text-text min-w-0 flex-1">{children}</span>
      {mark !== undefined && (
        <span
          className={cn(
            'text-caption flex shrink-0 items-center gap-1 font-medium tracking-normal',
            mark.tone === 'right' ? 'text-success' : 'text-danger',
          )}
        >
          {mark.tone === 'right' ? (
            <Check aria-hidden className="size-4" />
          ) : (
            <X aria-hidden className="size-4" />
          )}
          {mark.label}
        </span>
      )}
    </label>
  )
}
