import * as DropdownMenuPrimitive from '@radix-ui/react-dropdown-menu'
import { cn } from '@/shared/lib/cn'

export const DropdownMenu = DropdownMenuPrimitive.Root
export const DropdownMenuTrigger = DropdownMenuPrimitive.Trigger
export const DropdownMenuGroup = DropdownMenuPrimitive.Group

export type DropdownMenuContentProps = DropdownMenuPrimitive.DropdownMenuContentProps

export function DropdownMenuContent({ className, sideOffset = 4, ...props }: DropdownMenuContentProps) {
  return (
    <DropdownMenuPrimitive.Portal>
      <DropdownMenuPrimitive.Content
        sideOffset={sideOffset}
        className={cn(
          'border-border bg-surface-raised shadow-menu z-50 min-w-48 overflow-hidden rounded-md border p-1',
          'data-[state=open]:animate-menu-in',
          className,
        )}
        {...props}
      />
    </DropdownMenuPrimitive.Portal>
  )
}

export type DropdownMenuItemProps = DropdownMenuPrimitive.DropdownMenuItemProps & {
  destructive?: boolean
}

export function DropdownMenuItem({ className, destructive = false, ...props }: DropdownMenuItemProps) {
  return (
    <DropdownMenuPrimitive.Item
      className={cn(
        'text-small flex cursor-pointer items-center gap-2 rounded-sm px-2.5 py-2 outline-none select-none',
        destructive ? 'text-danger' : 'text-text',
        'data-[highlighted]:bg-surface-sunken',
        'data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
        className,
      )}
      {...props}
    />
  )
}

export type DropdownMenuLabelProps = DropdownMenuPrimitive.DropdownMenuLabelProps

export function DropdownMenuLabel({ className, ...props }: DropdownMenuLabelProps) {
  return (
    <DropdownMenuPrimitive.Label
      className={cn(
        'text-caption tracking-caption text-text-muted px-2.5 py-1.5 font-medium uppercase',
        className,
      )}
      {...props}
    />
  )
}

export function DropdownMenuSeparator({
  className,
  ...props
}: DropdownMenuPrimitive.DropdownMenuSeparatorProps) {
  return <DropdownMenuPrimitive.Separator className={cn('bg-border my-1 h-px', className)} {...props} />
}
