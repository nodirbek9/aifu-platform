import type { KeyboardCoordinateGetter } from '@dnd-kit/core'
import type { Entry, EntryRequest } from './queries'

export type Parity = 'EVERY' | 'ODD' | 'EVEN'
export type ViewKind = 'group' | 'teacher' | 'room'

/** Monday to Saturday: the days a class can be put on. */
export const DAYS = [1, 2, 3, 4, 5, 6] as const

/** The id of a cell of the grid, as the drop target knows it. */
export function cellId(day: number, pairSlotId: string): string {
  return `cell:${day}:${pairSlotId}`
}

export function parseCellId(id: string): { day: number; pairSlotId: string } | null {
  const [kind, day, pairSlotId] = id.split(':')
  if (kind !== 'cell' || day === undefined || pairSlotId === undefined) return null
  return { day: Number(day), pairSlotId }
}

/** In an odd-week view, the classes of every week and of odd weeks; and so on. */
export function visibleIn(entry: Pick<Entry, 'weekParity'>, parity: Parity): boolean {
  const own = entry.weekParity ?? 'EVERY'
  return parity === 'EVERY' || own === 'EVERY' || own === parity
}

/** The request that saves an entry as it is, optionally put somewhere else. */
export function requestOf(entry: Entry, place?: { day: number; pairSlotId: string }): EntryRequest {
  return {
    courseId: entry.courseId ?? '',
    sessionType: entry.sessionType ?? 'LECTURE',
    teacherUserId: entry.teacherUserId ?? '',
    groupIds: (entry.groups ?? []).flatMap((group) => (group.id === undefined ? [] : [group.id])),
    pairSlotId: place?.pairSlotId ?? entry.pairSlotId ?? '',
    dayOfWeek: place?.day ?? entry.dayOfWeek ?? 1,
    weekParity: entry.weekParity ?? 'EVERY',
    ...(entry.roomId !== undefined && { roomId: entry.roomId }),
    ...(entry.subgroup !== undefined && entry.subgroup !== '' && { subgroup: entry.subgroup }),
    ...(entry.note !== undefined && entry.note !== '' && { note: entry.note }),
    ...(entry.validFrom !== undefined && { validFrom: entry.validFrom }),
    ...(entry.validTo !== undefined && { validTo: entry.validTo }),
  }
}

/** A 409 TIMETABLE_CONFLICT carries what clashes; the shape is the check's. */
export type Clash = {
  conflicts: {
    type?: string
    courseTitle?: string
    subjectCode?: string
    teacherName?: string
    roomLabel?: string
    groupCodes?: string[]
    dayOfWeek?: number
    pairNo?: number
  }[]
  warnings: { code?: string; message?: string }[]
}

function asArray(value: unknown): Record<string, unknown>[] {
  return Array.isArray(value)
    ? value.filter((item): item is Record<string, unknown> => typeof item === 'object' && item !== null)
    : []
}

const text = (value: unknown) => (typeof value === 'string' ? value : undefined)
const number = (value: unknown) => (typeof value === 'number' ? value : undefined)

/** Reads the clash from a problem's details (or a check result). */
export function clashOf(details: Record<string, unknown> | undefined): Clash {
  return {
    conflicts: asArray(details?.conflicts).map((item) => ({
      ...(text(item.type) !== undefined && { type: text(item.type) }),
      ...(text(item.courseTitle) !== undefined && { courseTitle: text(item.courseTitle) }),
      ...(text(item.subjectCode) !== undefined && { subjectCode: text(item.subjectCode) }),
      ...(text(item.teacherName) !== undefined && { teacherName: text(item.teacherName) }),
      ...(text(item.roomLabel) !== undefined && { roomLabel: text(item.roomLabel) }),
      ...(Array.isArray(item.groupCodes) && { groupCodes: item.groupCodes.map(String) }),
      ...(number(item.dayOfWeek) !== undefined && { dayOfWeek: number(item.dayOfWeek) }),
      ...(number(item.pairNo) !== undefined && { pairNo: number(item.pairNo) }),
    })),
    warnings: asArray(details?.warnings).map((item) => ({
      ...(text(item.code) !== undefined && { code: text(item.code) }),
      ...(text(item.message) !== undefined && { message: text(item.message) }),
    })),
  }
}

/**
 * The keyboard way to drag: the arrows jump from cell to cell (not pixel by pixel) — to the
 * nearest drop target whose centre lies in the pressed direction.
 */
export const cellByCell: KeyboardCoordinateGetter = (event, { context, currentCoordinates }) => {
  const direction = { ArrowRight: [1, 0], ArrowLeft: [-1, 0], ArrowDown: [0, 1], ArrowUp: [0, -1] }[
    event.code
  ]
  if (direction === undefined) return undefined
  event.preventDefault()
  const [dx = 0, dy = 0] = direction
  // measured from the centre of the cell the class is over, not from the card: a card sits at
  // the top of its cell, so the cell's own centre would look like the next one down
  const overRect = context.over === null ? undefined : context.droppableRects.get(context.over.id)
  const active = context.collisionRect
  const origin =
    overRect !== undefined
      ? { x: overRect.left + overRect.width / 2, y: overRect.top + overRect.height / 2 }
      : active === null
        ? currentCoordinates
        : { x: active.left + active.width / 2, y: active.top + active.height / 2 }
  let best: { x: number; y: number; distance: number } | undefined
  for (const rect of context.droppableRects.values()) {
    const center = { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
    const along = (center.x - origin.x) * dx + (center.y - origin.y) * dy
    if (along <= 4) continue
    const across = Math.abs((center.x - origin.x) * dy) + Math.abs((center.y - origin.y) * dx)
    const distance = along + across * 3
    if (best === undefined || distance < best.distance) best = { ...center, distance }
  }
  if (best === undefined) return currentCoordinates
  return { x: currentCoordinates.x + (best.x - origin.x), y: currentCoordinates.y + (best.y - origin.y) }
}
