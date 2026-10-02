import * as PopoverPrimitive from '@radix-ui/react-popover'
import { cn } from '@/shared/lib/cn'

export const Popover = PopoverPrimitive.Root
export const PopoverTrigger = PopoverPrimitive.Trigger
export const PopoverClose = PopoverPrimitive.Close

export type PopoverContentProps = PopoverPrimitive.PopoverContentProps

export function PopoverContent({ className, sideOffset = 6, align = 'end', ...props }: PopoverContentProps) {
  return (
    <PopoverPrimitive.Portal>
      <PopoverPrimitive.Content
        sideOffset={sideOffset}
        align={align}
        className={cn(
          'border-border bg-surface-raised shadow-menu z-50 w-72 rounded-md border p-3',
          'data-[state=open]:animate-menu-in',
          className,
        )}
        {...props}
      />
    </PopoverPrimitive.Portal>
  )
}
