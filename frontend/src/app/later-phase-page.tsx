import { Hammer } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { EmptyState } from '@/shared/ui/empty-state'
import { PageHeader } from '@/shared/ui/page-header'
import type { NavKey } from './shell/nav'

/** A section whose screens arrive in a later build phase: a real page that says so. */
export function LaterPhasePage({ section }: { section: NavKey }) {
  const { t } = useTranslation()
  return (
    <div className="grid gap-6">
      <PageHeader title={t(`nav.${section}`)} />
      <EmptyState
        icon={<Hammer aria-hidden className="size-7" />}
        title={t('home.laterPhaseTitle')}
        body={t('home.laterPhaseBody')}
      />
    </div>
  )
}
