import type { ReactNode } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'

export const Dialog = DialogPrimitive.Root
export const DialogTrigger = DialogPrimitive.Trigger
export const DialogClose = DialogPrimitive.Close

export type DialogContentProps = DialogPrimitive.DialogContentProps & {
  children: ReactNode
}

export function DialogContent({ className, children, ...props }: DialogContentProps) {
  const { t } = useTranslation()
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="bg-brand-navy/50 data-[state=open]:animate-overlay-in fixed inset-0 z-40" />
      <DialogPrimitive.Content
        className={cn(
          'fixed top-1/2 left-1/2 z-50 w-[calc(100vw-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2',
          'border-border bg-surface-raised shadow-dialog rounded-lg border p-6',
          'data-[state=open]:animate-dialog-in',
          className,
        )}
        {...props}
      >
        {children}
        <DialogPrimitive.Close
          aria-label={t('common.close')}
          className={cn(
            'text-text-muted absolute top-3 right-3 flex size-9 items-center justify-center rounded-sm',
            'duration-fast hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
            'focus-visible:outline-focus-ring focus-visible:outline-2',
          )}
        >
          <X aria-hidden className="size-4" />
        </DialogPrimitive.Close>
      </DialogPrimitive.Content>
    </DialogPrimitive.Portal>
  )
}

export type DialogTitleProps = DialogPrimitive.DialogTitleProps

export function DialogTitle({ className, ...props }: DialogTitleProps) {
  return (
    <DialogPrimitive.Title
      className={cn('font-display text-title text-text font-semibold', className)}
      {...props}
    />
  )
}

export type DialogDescriptionProps = DialogPrimitive.DialogDescriptionProps

export function DialogDescription({ className, ...props }: DialogDescriptionProps) {
  return (
    <DialogPrimitive.Description className={cn('text-small text-text-muted mt-1', className)} {...props} />
  )
}

export function DialogFooter({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('mt-6 flex justify-end gap-2', className)} {...props} />
}
