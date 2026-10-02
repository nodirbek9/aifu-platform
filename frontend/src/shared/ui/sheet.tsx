import type { ReactNode } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import type { VariantProps } from 'tailwind-variants'
import { tv } from '@/shared/lib/tv'
import { cn } from '@/shared/lib/cn'

export const Sheet = DialogPrimitive.Root
export const SheetTrigger = DialogPrimitive.Trigger
export const SheetClose = DialogPrimitive.Close

const sheetContent = tv({
  base: [
    'fixed inset-y-0 z-50 flex w-full flex-col border-border bg-surface-raised shadow-dialog',
    'data-[state=open]:animate-sheet-in',
  ],
  variants: {
    side: {
      right: 'right-0 border-l sm:max-w-md',
      left: 'left-0 border-r sm:max-w-md',
      bottom: 'inset-y-auto bottom-0 max-h-[85vh] rounded-t-lg border-t',
    },
  },
  defaultVariants: { side: 'right' },
})

export type SheetContentProps = DialogPrimitive.DialogContentProps &
  VariantProps<typeof sheetContent> & {
    children: ReactNode
  }

/** A drawer: side panel on desktop, bottom sheet behaviour on the phone where noted. */
export function SheetContent({ className, children, side, ...props }: SheetContentProps) {
  const { t } = useTranslation()
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Overlay className="bg-brand-navy/50 data-[state=open]:animate-overlay-in fixed inset-0 z-40" />
      <DialogPrimitive.Content className={sheetContent({ side, class: className })} {...props}>
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

export function SheetHeader({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('border-border grid gap-1 border-b px-5 py-4', className)} {...props} />
}

export function SheetTitle({ className, ...props }: DialogPrimitive.DialogTitleProps) {
  return (
    <DialogPrimitive.Title
      className={cn('font-display text-title text-text font-semibold', className)}
      {...props}
    />
  )
}

export function SheetDescription({ className, ...props }: DialogPrimitive.DialogDescriptionProps) {
  return <DialogPrimitive.Description className={cn('text-small text-text-muted', className)} {...props} />
}

export function SheetBody({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('flex-1 overflow-y-auto px-5 py-4', className)} {...props} />
}
