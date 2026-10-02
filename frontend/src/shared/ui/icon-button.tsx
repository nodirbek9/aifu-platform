import { forwardRef, type ButtonHTMLAttributes } from 'react'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'

export const iconButton = tv({
  base: [
    'inline-flex items-center justify-center rounded-sm',
    'transition-[background-color,color,transform] duration-fast ease-standard',
    'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring',
    'disabled:pointer-events-none disabled:opacity-50',
    'active:not-disabled:scale-[0.97]',
  ],
  variants: {
    variant: {
      ghost: 'text-text-muted hover:bg-surface-sunken hover:text-text',
      outline: 'border border-border-strong bg-surface-raised text-text hover:bg-surface-sunken',
      primary: 'bg-primary text-primary-foreground hover:bg-primary-hover',
    },
    size: {
      sm: 'size-9',
      md: 'size-11',
    },
  },
  defaultVariants: { variant: 'ghost', size: 'md' },
})

export type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  VariantProps<typeof iconButton> & {
    'aria-label': string
  }

export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>(function IconButton(
  { className, variant, size, type = 'button', ...props },
  ref,
) {
  return (
    <button ref={ref} type={type} className={iconButton({ variant, size, class: className })} {...props} />
  )
})
