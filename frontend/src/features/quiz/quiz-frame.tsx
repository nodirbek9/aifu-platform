import type { ReactNode } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Link } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import { LocaleControl } from '@/shared/ui/locale-control'

type QuizFrameProps = {
  backTo: string
  backLabel: string
  children: ReactNode
}

/** The quiz as a focus page: a slim header with the way back, and a reading-width column. */
export function QuizFrame({ backTo, backLabel, children }: QuizFrameProps) {
  const locale = useLocale()
  const { changeLocale } = useAuth()
  return (
    <div className="bg-surface flex min-h-dvh flex-col">
      <header className="border-border flex h-14 shrink-0 items-center gap-1 border-b px-2 md:gap-2 md:px-6">
        <Link
          to={backTo}
          className={cn(
            'text-small text-text-muted flex h-11 min-w-0 items-center gap-2 rounded-sm px-2 font-medium',
            'duration-fast ease-standard hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          )}
        >
          <ArrowLeft aria-hidden className="size-5 shrink-0" />
          <span className="truncate">{backLabel}</span>
        </Link>
        <div className="flex-1" />
        <LocaleControl value={locale} onChange={changeLocale} />
      </header>
      <main className="max-w-lesson mx-auto grid w-full content-start gap-8 px-4 py-8 md:px-6 md:py-12">
        {children}
      </main>
    </div>
  )
}
