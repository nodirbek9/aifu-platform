import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import type { Heading } from '@/shared/ui/rich-text'

/** The heading the reader is in: the last one that has passed the top of the viewport. */
function useActiveHeading(headings: readonly Heading[]): string | undefined {
  const [active, setActive] = useState<string>()

  useEffect(() => {
    if (headings.length === 0) return
    let frame = 0
    const update = () => {
      frame = 0
      // a heading counts as reached a little before it touches the sticky header
      const line = 120
      let current = headings[0]?.id
      for (const heading of headings) {
        const element = document.getElementById(heading.id)
        if (element !== null && element.getBoundingClientRect().top <= line) current = heading.id
      }
      setActive(current)
    }
    const onScroll = () => {
      if (frame === 0) frame = window.requestAnimationFrame(update)
    }
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => {
      window.removeEventListener('scroll', onScroll)
      if (frame !== 0) window.cancelAnimationFrame(frame)
    }
  }, [headings])

  return active
}

type LessonTocProps = {
  headings: readonly Heading[]
  /** Called after a jump (the phone drawer closes itself). */
  onNavigate?: () => void
  className?: string
}

/** The table of contents of a lesson, built from its headings; it follows the section in view. */
export function LessonToc({ headings, onNavigate, className }: LessonTocProps) {
  const { t } = useTranslation()
  const active = useActiveHeading(headings)
  if (headings.length === 0) return null

  return (
    <nav aria-label={t('lesson.contents')} className={className}>
      <p className="text-caption text-text-muted mb-3 font-medium uppercase">{t('lesson.contents')}</p>
      <ol className="border-border grid border-l">
        {headings.map((heading) => {
          const current = heading.id === active
          return (
            <li key={heading.id}>
              <a
                href={`#${heading.id}`}
                aria-current={current ? 'location' : undefined}
                onClick={onNavigate}
                className={cn(
                  'text-small -ml-px block border-l-2 py-1.5 pr-2',
                  heading.level === 3 ? 'pl-7' : 'pl-4',
                  'duration-fast ease-standard transition-[color,border-color]',
                  'focus-visible:outline-focus-ring focus-visible:outline-2',
                  current
                    ? 'border-primary text-text font-medium'
                    : 'text-text-muted hover:text-text border-transparent',
                )}
              >
                {heading.text}
              </a>
            </li>
          )
        })}
      </ol>
    </nav>
  )
}
