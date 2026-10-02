import { forwardRef, type ButtonHTMLAttributes } from 'react'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'
import { Loader2 } from 'lucide-react'

export const button = tv({
  base: [
    'inline-flex items-center justify-center gap-2 rounded-sm font-medium whitespace-nowrap',
    'transition-[background-color,color,box-shadow,transform] duration-fast ease-standard',
    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring',
    'disabled:pointer-events-none disabled:opacity-50',
    'active:not-disabled:scale-[0.97]',
  ],
  variants: {
    variant: {
      primary: 'bg-primary text-primary-foreground hover:bg-primary-hover',
      accent: 'bg-accent-strong text-accent-foreground hover:bg-accent-text',
      outline: 'border border-border-strong bg-surface-raised text-text hover:bg-surface-sunken',
      ghost: 'text-text hover:bg-surface-sunken',
      danger: 'bg-danger text-accent-foreground hover:opacity-90',
      link: 'text-primary-hover underline-offset-4 hover:underline',
    },
    size: {
      sm: 'h-9 px-3 text-small',
      md: 'h-11 px-4 text-body',
      lg: 'h-12 px-6 text-body',
    },
    full: { true: 'w-full' },
  },
  defaultVariants: { variant: 'primary', size: 'md' },
})

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  VariantProps<typeof button> & {
    loading?: boolean
  }

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { className, variant, size, full, loading = false, disabled, children, type = 'button', ...props },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled ?? loading}
      aria-busy={loading || undefined}
      className={button({ variant, size, full, class: className })}
      {...props}
    >
      {loading ? <Loader2 aria-hidden className="size-4 animate-spin" /> : null}
      {children}
    </button>
  )
})
