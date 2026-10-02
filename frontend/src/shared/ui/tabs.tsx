import * as TabsPrimitive from '@radix-ui/react-tabs'
import { cn } from '@/shared/lib/cn'

export const Tabs = TabsPrimitive.Root

export type TabsListProps = TabsPrimitive.TabsListProps

export function TabsList({ className, ...props }: TabsListProps) {
  return (
    <TabsPrimitive.List
      className={cn('border-border flex items-center gap-1 border-b', className)}
      {...props}
    />
  )
}

export type TabsTriggerProps = TabsPrimitive.TabsTriggerProps

export function TabsTrigger({ className, ...props }: TabsTriggerProps) {
  return (
    <TabsPrimitive.Trigger
      className={cn(
        'text-small text-text-muted relative -mb-px border-b-2 border-transparent px-3.5 py-2.5 font-medium',
        'duration-fast ease-standard transition-[color,border-color]',
        'hover:text-text',
        'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
        'data-[state=active]:border-primary data-[state=active]:text-text',
        className,
      )}
      {...props}
    />
  )
}

export type TabsContentProps = TabsPrimitive.TabsContentProps

export function TabsContent({ className, ...props }: TabsContentProps) {
  return (
    <TabsPrimitive.Content
      className={cn('focus-visible:outline-focus-ring focus-visible:outline-2', className)}
      {...props}
    />
  )
}
