import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/features/auth/auth-context'
import { useLocale } from '@/shared/i18n/use-locale'
import { Button } from '@/shared/ui/button'
import { PageHeader } from '@/shared/ui/page-header'
import { AnnouncementComposer } from './announcement-composer'
import { AnnouncementList } from './announcement-list'
import { useMyAnnouncementsQuery } from './queries'

/**
 * Everything announced to me — by my teachers, to my group, by the study office to everyone — and,
 * for a teacher or the study office, where a new one is written.
 */
export function AnnouncementsPage() {
  const { t } = useTranslation()
  const locale = useLocale()
  const { activeRole } = useAuth()
  const announcements = useMyAnnouncementsQuery(locale)
  const [writing, setWriting] = useState(false)
  const writer = activeRole === 'TEACHER' || activeRole === 'ADMIN'
  return (
    <div className="max-w-lesson grid gap-6">
      <PageHeader
        title={t('announcements.title')}
        action={
          writer ? (
            <Button
              onClick={() => {
                setWriting(true)
              }}
            >
              <Plus aria-hidden className="size-4" />
              {t('teaching.newAnnouncement')}
            </Button>
          ) : undefined
        }
      />
      <AnnouncementList announcements={announcements} withAudience manage={writer} />
      {writer && (
        <AnnouncementComposer
          open={writing}
          onClose={() => {
            setWriting(false)
          }}
        />
      )}
    </div>
  )
}
