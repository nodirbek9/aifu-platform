import { useQuery } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'
import { api, unwrap } from '@/shared/api/client'
import { narrowSlots, type PairSlot } from './model'

/** The bell schedule, for naming pairs; it changes rarely. */
export function usePairSlotsQuery() {
  return useQuery({
    queryKey: ['timetable', 'pair-slots'],
    queryFn: async () => narrowSlots(await unwrap(api.GET('/api/v1/timetable/pair-slots'))),
    staleTime: 60 * 60 * 1000,
  })
}

/**
 * Names a pair the way people do: "2-juftlik" is the second pair of its shift, whatever its place
 * in the whole day. Until the bell schedule has loaded, the place in the day stands in.
 */
export function usePairName(): (pairNo: number) => string {
  const { t } = useTranslation()
  const slots = usePairSlotsQuery()
  return (pairNo) => t('timetable.pair', { number: slotByNo(slots.data, pairNo)?.shiftPairNo ?? pairNo })
}

function slotByNo(slots: readonly PairSlot[] | undefined, pairNo: number): PairSlot | undefined {
  return slots?.find((slot) => slot.pairNo === pairNo)
}
