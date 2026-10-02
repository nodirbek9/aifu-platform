import { useEffect, useRef } from 'react'
import { cn } from '@/shared/lib/cn'

/**
 * A thin line that fills as the page is read (brief §8, moment 7). It follows the scroll
 * position by writing the transform straight to the element: no React render per scroll frame.
 */
export function ReadingProgress({ className }: { className?: string }) {
  const barRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    let frame = 0
    const update = () => {
      frame = 0
      const bar = barRef.current
      if (bar === null) return
      const scrollable = document.documentElement.scrollHeight - window.innerHeight
      const ratio = scrollable <= 0 ? 0 : Math.min(1, Math.max(0, window.scrollY / scrollable))
      bar.style.transform = `scaleX(${ratio})`
    }
    const onScroll = () => {
      if (frame === 0) frame = window.requestAnimationFrame(update)
    }
    update()
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onScroll)
    return () => {
      window.removeEventListener('scroll', onScroll)
      window.removeEventListener('resize', onScroll)
      if (frame !== 0) window.cancelAnimationFrame(frame)
    }
  }, [])

  return (
    <div aria-hidden className={cn('bg-border h-0.5 w-full', className)}>
      <div ref={barRef} className="bg-primary h-full origin-left scale-x-0" />
    </div>
  )
}
