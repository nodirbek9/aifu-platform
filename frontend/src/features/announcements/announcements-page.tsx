import { useTranslation } from 'react-i18next'
import { useLocale } from '@/shared/i18n/use-locale'
import { PageHeader } from '@/shared/ui/page-header'
import { AnnouncementList } from './announcement-list'
import { useMyAnnouncementsQuery } from './queries'

/** Everything announced to me: by the teachers of my courses and to my group. */
export function AnnouncementsPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const announcements = useMyAnnouncementsQuery(locale)
  return (
    <div className="max-w-lesson grid gap-6">
      <PageHeader title={t('announcements.title')} />
      <AnnouncementList announcements={announcements} withAudience />
    </div>
  )
}
