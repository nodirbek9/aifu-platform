import * as AvatarPrimitive from '@radix-ui/react-avatar'
import { cn } from '@/shared/lib/cn'

export type AvatarProps = {
  name: string
  src?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const sizeClass = { sm: 'size-8 text-caption', md: 'size-10 text-small', lg: 'size-14 text-title' } as const

function initialsOf(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  const first = parts[0]?.[0] ?? ''
  const last = parts.length > 1 ? (parts[parts.length - 1]?.[0] ?? '') : ''
  return `${first}${last}`.toUpperCase()
}

/** User avatar in the brand's shield frame: square shoulders, round base. */
export function Avatar({ name, src, size = 'md', className }: AvatarProps) {
  return (
    <AvatarPrimitive.Root
      className={cn(
        'relative inline-flex shrink-0 items-center justify-center overflow-hidden rounded-b-full',
        'bg-primary text-primary-foreground tracking-normal',
        sizeClass[size],
        className,
      )}
    >
      {src !== undefined && <AvatarPrimitive.Image src={src} alt={name} className="size-full object-cover" />}
      <AvatarPrimitive.Fallback
        className="font-display -translate-y-0.5 leading-none font-semibold"
        delayMs={src !== undefined ? 400 : 0}
      >
        {initialsOf(name)}
      </AvatarPrimitive.Fallback>
    </AvatarPrimitive.Root>
  )
}
