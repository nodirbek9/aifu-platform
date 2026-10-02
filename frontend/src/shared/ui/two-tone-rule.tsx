import { cn } from '@/shared/lib/cn'

/** The shield's navy/magenta split as a single thin brand rule (login, system pages). */
export function TwoToneRule({ className }: { className?: string }) {
  return (
    <div aria-hidden className={cn('h-1 w-full', className)}>
      <div className="h-full w-full bg-[linear-gradient(90deg,#002E60_50%,#FF0158_50%)]" />
    </div>
  )
}
