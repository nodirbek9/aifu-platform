import { useDraggable, useDroppable } from '@dnd-kit/core'
import { Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { sessionBar, sessionTint } from '@/shared/ui/session-type'
import { splitCourseTitle } from '@/features/timetable/model'
import { cellId } from './model'
import type { Entry } from './queries'

export type CellMark = 'free' | 'warning' | 'conflict' | 'checking'

type EntryCardProps = {
  entry: Entry
  draggable: boolean
  shaking: boolean
  onOpen: (entry: Entry) => void
}

/** The look of a class, shared by the card in the grid and the one lifted under the pointer. */
export function EntryFace({ entry, lifted = false }: { entry: Entry; lifted?: boolean }) {
  const { t } = useTranslation()
  const type = entry.sessionType ?? 'LECTURE'
  const parity = entry.weekParity ?? 'EVERY'
  const parsedTitle = splitCourseTitle(entry.courseTitle ?? '')
  return (
    <span
      title={entry.courseTitle}
      className={cn(
        'text-caption relative grid w-full grid-cols-1 gap-0.5 overflow-hidden rounded-sm py-1 pr-1.5 pl-2.5 text-left tracking-normal',
        sessionTint[type],
        lifted && 'ring-border-strong cursor-grabbing shadow-lg ring-1',
      )}
    >
      <span aria-hidden className={cn('absolute inset-y-0 left-0 w-1', sessionBar[type])} />
      <span className="text-text line-clamp-2 min-w-0 font-semibold break-words">{parsedTitle.subject}</span>
      {parsedTitle.termSuffix !== undefined && (
        <span className="text-text-muted line-clamp-1 min-w-0 font-normal break-words">
          {parsedTitle.termSuffix}
        </span>
      )}
      <span className="text-text-muted truncate">
        {[entry.teacherName, entry.roomLabel].filter(Boolean).join(' · ')}
      </span>
      <span className="text-text-muted truncate">
        {(entry.groups ?? []).map((group) => group.code).join(', ')}
        {parity !== 'EVERY' && ` · ${t(`builder.parity.${parity}`)}`}
      </span>
    </span>
  )
}

/** One class in the grid: its ghost stays while it is dragged; it shakes once when the drop is refused. */
export function EntryCard({ entry, draggable, shaking, onOpen }: EntryCardProps) {
  const { t } = useTranslation()
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: entry.id ?? '',
    disabled: !draggable,
  })
  const type = entry.sessionType ?? 'LECTURE'
  const parity = entry.weekParity ?? 'EVERY'
  return (
    <button
      ref={setNodeRef}
      type="button"
      // read-only, the card is still a button that opens the class: no drag attributes
      // (dnd-kit would mark it aria-disabled)
      {...(draggable && listeners)}
      {...(draggable && attributes)}
      aria-roledescription={draggable ? t('builder.draggableClass') : undefined}
      aria-label={[
        entry.subjectCode,
        entry.courseTitle,
        t(`sessionType.${type}`),
        entry.teacherName,
        (entry.groups ?? []).map((group) => group.code).join(', '),
        entry.roomLabel,
        parity === 'EVERY' ? undefined : t(`builder.parity.${parity}`),
      ]
        .filter(Boolean)
        .join(', ')}
      onClick={() => {
        onOpen(entry)
      }}
      className={cn(
        'focus-visible:outline-focus-ring grid w-full grid-cols-1 rounded-sm focus-visible:outline-2',
        draggable && 'cursor-grab touch-none',
        // the card stays as a ghost where it was; the overlay is what moves
        isDragging && 'opacity-40',
        shaking && 'animate-shake',
      )}
    >
      <EntryFace entry={entry} />
    </button>
  )
}

type DropCellProps = {
  day: number
  pairSlotId: string
  entries: readonly Entry[]
  mark: CellMark | undefined
  editable: boolean
  shakingId: string | undefined
  onOpen: (entry: Entry) => void
  onAdd: () => void
  addLabel: string
}

/** A day × pair cell: its classes, the "+" of an empty slot, and the colour of a drag's verdict. */
export function DropCell({
  day,
  pairSlotId,
  entries,
  mark,
  editable,
  shakingId,
  onOpen,
  onAdd,
  addLabel,
}: DropCellProps) {
  const { setNodeRef, isOver } = useDroppable({ id: cellId(day, pairSlotId), disabled: !editable })
  return (
    <div
      ref={setNodeRef}
      className={cn(
        'grid h-full min-h-20 grid-cols-1 content-start gap-1 rounded-sm',
        isOver && mark === 'free' && 'bg-success-soft ring-success ring-2',
        isOver && mark === 'warning' && 'bg-warning-soft ring-warning ring-2',
        isOver && mark === 'conflict' && 'bg-danger-soft ring-danger ring-2',
        isOver && (mark === undefined || mark === 'checking') && 'ring-border-strong ring-2',
      )}
    >
      {entries.map((entry) => (
        <EntryCard
          key={entry.id}
          entry={entry}
          draggable={editable}
          shaking={shakingId === entry.id}
          onOpen={onOpen}
        />
      ))}
      {editable && (
        <button
          type="button"
          aria-label={addLabel}
          onClick={onAdd}
          data-print-hidden
          className={cn(
            'text-text-muted hover:bg-surface-sunken hover:text-text focus-visible:outline-focus-ring grid min-h-8 place-items-center rounded-sm group-hover:opacity-100 hover:opacity-100 focus-visible:opacity-100 focus-visible:outline-2',
            // an empty slot always shows its "+": a touch screen has no hover to reveal it
            entries.length === 0 ? 'opacity-40' : 'opacity-0',
          )}
        >
          <Plus aria-hidden className="size-4" />
        </button>
      )}
    </div>
  )
}
