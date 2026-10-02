import { useRef, useState, type KeyboardEvent, type PointerEvent, type ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'

export type SplitPaneProps = {
  left: ReactNode
  right: ReactNode
  /** localStorage key under which the divider's position is remembered. */
  storageKey: string
  /** Accessible name of the divider. */
  dividerLabel: string
  className?: string
}

const MIN = 25
const MAX = 70
const DEFAULT = 42

function readStored(key: string): number {
  try {
    const stored = Number(localStorage.getItem(key))
    return stored >= MIN && stored <= MAX ? stored : DEFAULT
  } catch {
    return DEFAULT
  }
}

/** Two panes side by side with a divider that is dragged, or moved with the arrow keys. */
export function SplitPane({ left, right, storageKey, dividerLabel, className }: SplitPaneProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [percent, setPercent] = useState(() => readStored(storageKey))
  const [dragging, setDragging] = useState(false)

  const commit = (next: number) => {
    const clamped = Math.min(MAX, Math.max(MIN, next))
    setPercent(clamped)
    try {
      localStorage.setItem(storageKey, String(Math.round(clamped)))
    } catch {
      /* storage unavailable — the position lasts until reload */
    }
  }

  const onPointerMove = (event: PointerEvent<HTMLDivElement>) => {
    const container = containerRef.current
    if (!dragging || container === null) return
    const bounds = container.getBoundingClientRect()
    commit(((event.clientX - bounds.left) / bounds.width) * 100)
  }

  const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'ArrowLeft') commit(percent - 2)
    else if (event.key === 'ArrowRight') commit(percent + 2)
    else return
    event.preventDefault()
  }

  return (
    <div ref={containerRef} className={cn('flex min-h-0 min-w-0', dragging && 'select-none', className)}>
      <div className="min-h-0 min-w-0 shrink-0" style={{ width: `${percent}%` }}>
        {left}
      </div>
      <div
        role="separator"
        aria-orientation="vertical"
        aria-label={dividerLabel}
        aria-valuemin={MIN}
        aria-valuemax={MAX}
        aria-valuenow={Math.round(percent)}
        tabIndex={0}
        onPointerDown={(event) => {
          event.currentTarget.setPointerCapture(event.pointerId)
          setDragging(true)
        }}
        onPointerMove={onPointerMove}
        onPointerUp={() => {
          setDragging(false)
        }}
        onPointerCancel={() => {
          setDragging(false)
        }}
        onKeyDown={onKeyDown}
        className={cn(
          'group border-border relative w-2 shrink-0 cursor-col-resize touch-none border-x',
          'focus-visible:outline-focus-ring focus-visible:outline-2',
        )}
      >
        <div
          className={cn(
            'duration-fast absolute inset-y-0 left-1/2 w-0.5 -translate-x-1/2 transition-[background-color]',
            dragging ? 'bg-primary' : 'group-hover:bg-border-strong bg-transparent',
          )}
        />
      </div>
      <div className="min-h-0 min-w-0 flex-1">{right}</div>
    </div>
  )
}
