import { useState } from 'react'
import { Ellipsis } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router'
import type { Role } from '@/features/auth/model'
import { cn } from '@/shared/lib/cn'
import { Sheet, SheetBody, SheetContent, SheetHeader, SheetTitle } from '@/shared/ui/sheet'
import { activeNavItem, navFor, type NavItem } from './nav'

const MAX_TABS = 5

const tab = cn(
  'relative flex h-full min-w-0 flex-1 flex-col items-center justify-center gap-1 px-1',
  'text-caption tracking-normal font-medium',
  'transition-[color] duration-fast ease-standard',
  'focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-focus-ring',
)
const tabActive =
  'text-primary before:absolute before:inset-x-3 before:top-0 before:h-0.5 before:rounded-full before:bg-primary'
const tabIdle = 'text-text-muted'

/** The phone's navigation: the role's main sections; what does not fit sits under "More". */
export function TabBar({ role }: { role: Role }) {
  const { t } = useTranslation()
  const location = useLocation()
  const [moreOpen, setMoreOpen] = useState(false)
  const items = navFor(role)
  const fitsAll = items.length <= MAX_TABS
  const tabs = fitsAll ? items : items.filter((item) => item.primary === true)
  const rest = fitsAll ? [] : items.filter((item) => item.primary !== true)
  const current = activeNavItem(role, location.pathname)
  const inMore = current !== undefined && rest.includes(current)

  return (
    <nav
      data-print-hidden
      aria-label={t('common.mainNavigation')}
      className="border-border bg-surface-raised pb-safe fixed inset-x-0 bottom-0 z-20 border-t md:hidden"
    >
      <ul className="flex h-16 items-stretch">
        {tabs.map((item) => (
          <li key={item.key} className="flex min-w-0 flex-1">
            <TabLink item={item} />
          </li>
        ))}
        {rest.length > 0 && (
          <li className="flex min-w-0 flex-1">
            <button
              type="button"
              aria-expanded={moreOpen}
              onClick={() => {
                setMoreOpen(true)
              }}
              className={cn(tab, inMore ? tabActive : tabIdle)}
            >
              <Ellipsis aria-hidden className="size-5" />
              <span className="max-w-full truncate">{t('common.more')}</span>
            </button>
          </li>
        )}
      </ul>

      <Sheet open={moreOpen} onOpenChange={setMoreOpen}>
        <SheetContent side="bottom">
          <SheetHeader>
            <SheetTitle>{t('common.more')}</SheetTitle>
          </SheetHeader>
          <SheetBody className="px-3">
            <ul className="grid gap-1">
              {rest.map((item) => {
                const Icon = item.icon
                return (
                  <li key={item.key}>
                    <NavLink
                      to={item.to}
                      onClick={() => {
                        setMoreOpen(false)
                      }}
                      className={({ isActive }) =>
                        cn(
                          'text-body flex h-12 items-center gap-3 rounded-sm px-3 font-medium',
                          'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                          isActive ? 'bg-surface-sunken text-primary' : 'text-text',
                        )
                      }
                    >
                      <Icon aria-hidden className="text-text-muted size-5" />
                      {t(`nav.${item.key}`)}
                    </NavLink>
                  </li>
                )
              })}
            </ul>
          </SheetBody>
        </SheetContent>
      </Sheet>
    </nav>
  )
}

function TabLink({ item }: { item: NavItem }) {
  const { t } = useTranslation()
  const Icon = item.icon
  return (
    <NavLink
      to={item.to}
      end={item.end === true}
      className={({ isActive }) => cn(tab, isActive ? tabActive : tabIdle)}
    >
      <Icon aria-hidden className="size-5" />
      <span className="max-w-full truncate">{t(`nav.${item.key}`)}</span>
    </NavLink>
  )
}
