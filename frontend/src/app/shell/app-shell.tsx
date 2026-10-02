import { useMemo, useState } from 'react'
import { Bell, BookOpen, FileText, LogOut, Megaphone, Monitor, Moon, Sun, UserRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { OfflineBanner } from '@/app/system-pages'
import { useTheme } from '@/app/theme'
import { useAuth } from '@/features/auth/auth-context'
import type { CurrentUser, Role } from '@/features/auth/model'
import { isSearchable, searchHitPath, useSearchQuery } from '@/features/search/queries'
import { useLocale } from '@/shared/i18n/use-locale'
import { useDebouncedValue } from '@/shared/lib/use-debounced-value'
import { useMediaQuery } from '@/shared/lib/use-media-query'
import { useStoredFlag } from '@/shared/lib/use-stored-flag'
import { CommandPalette, type CommandItem } from '@/shared/ui/command-palette'
import { navFor, roleOfPath } from './nav'
import { Rail } from './rail'
import { TabBar } from './tab-bar'
import { TopBar } from './top-bar'

type AppShellProps = {
  user: CurrentUser
  activeRole: Role
}

/**
 * The frame of every signed-in page: rail (tablet, desktop) or tab bar (phone), the top bar,
 * and the command palette. The sections shown are those of the role the current address
 * belongs to, so a deep link into another of the user's roles shows the right navigation at once.
 */
export function AppShell({ user, activeRole }: AppShellProps) {
  const { t } = useTranslation()
  const location = useLocation()
  const navigate = useNavigate()
  const { signOut } = useAuth()
  const { setTheme } = useTheme()
  const isDesktop = useMediaQuery('(min-width: 64rem)')
  const [railExpanded, setRailExpanded] = useStoredFlag('aifu-rail-expanded', true)
  const [paletteOpen, setPaletteOpen] = useState(false)
  const [searchText, setSearchText] = useState('')
  const locale = useLocale()
  const settledSearch = useDebouncedValue(searchText, 250)
  const search = useSearchQuery(settledSearch, locale)

  const pathRole = roleOfPath(location.pathname)
  const role = pathRole !== null && user.roles.includes(pathRole) ? pathRole : activeRole

  const commands = useMemo<CommandItem[]>(() => {
    const iconClass = 'size-4'
    return [
      ...navFor(role).map((item) => {
        const Icon = item.icon
        return {
          id: `nav-${item.key}`,
          label: t(`nav.${item.key}`),
          icon: <Icon aria-hidden className={iconClass} />,
          onSelect: () => {
            void navigate(item.to)
          },
        }
      }),
      {
        id: 'go-notifications',
        label: t('common.notifications'),
        icon: <Bell aria-hidden className={iconClass} />,
        onSelect: () => {
          void navigate('/notifications')
        },
      },
      {
        id: 'go-announcements',
        label: t('announcements.title'),
        icon: <Megaphone aria-hidden className={iconClass} />,
        onSelect: () => {
          void navigate('/announcements')
        },
      },
      {
        id: 'go-profile',
        label: t('common.profile'),
        icon: <UserRound aria-hidden className={iconClass} />,
        onSelect: () => {
          void navigate('/profile')
        },
      },
      {
        id: 'theme-light',
        label: `${t('common.theme')}: ${t('common.themeLight')}`,
        icon: <Sun aria-hidden className={iconClass} />,
        onSelect: () => {
          setTheme('light')
        },
      },
      {
        id: 'theme-dark',
        label: `${t('common.theme')}: ${t('common.themeDark')}`,
        icon: <Moon aria-hidden className={iconClass} />,
        onSelect: () => {
          setTheme('dark')
        },
      },
      {
        id: 'theme-system',
        label: `${t('common.theme')}: ${t('common.themeSystem')}`,
        icon: <Monitor aria-hidden className={iconClass} />,
        onSelect: () => {
          setTheme('system')
        },
      },
      {
        id: 'sign-out',
        label: t('auth.logout'),
        icon: <LogOut aria-hidden className={iconClass} />,
        onSelect: () => {
          void signOut()
        },
      },
    ]
  }, [role, t, navigate, setTheme, signOut])

  const found: CommandItem[] = (search.data ?? []).flatMap((hit) => {
    const to = searchHitPath(hit, role)
    if (to === null) return []
    const Icon = hit.type === 'LESSON' ? FileText : BookOpen
    return [
      {
        id: `search-${hit.type}-${hit.id}`,
        label: hit.title,
        hint: t(`search.type.${hit.type}`),
        icon: <Icon aria-hidden className="size-4" />,
        onSelect: () => {
          void navigate(to)
        },
      },
    ]
  })

  return (
    <div className="bg-surface flex min-h-dvh">
      <a
        href="#main"
        className="bg-primary text-small text-primary-foreground sr-only rounded-sm px-4 py-2 font-medium focus:not-sr-only focus:fixed focus:top-3 focus:left-3 focus:z-70"
      >
        {t('common.skipToContent')}
      </a>

      <Rail
        role={role}
        roles={user.roles}
        expanded={isDesktop && railExpanded}
        {...(isDesktop && {
          onToggle: () => {
            setRailExpanded(!railExpanded)
          },
        })}
      />

      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar
          user={user}
          role={role}
          onOpenPalette={() => {
            setPaletteOpen(true)
          }}
        />
        <OfflineBanner />
        <main
          id="main"
          tabIndex={-1}
          className="flex-1 px-4 pt-6 pb-24 outline-none md:px-6 md:pb-10 lg:px-12 lg:pt-8"
        >
          <div className="max-w-app mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>

      <TabBar role={role} />
      <CommandPalette
        open={paletteOpen}
        onOpenChange={(open) => {
          setPaletteOpen(open)
          setSearchText('')
        }}
        items={commands}
        search={{
          label: t('search.results'),
          loading: isSearchable(searchText) && (search.isFetching || settledSearch !== searchText),
          onQueryChange: setSearchText,
          items: isSearchable(settledSearch) ? found : [],
        }}
      />
    </div>
  )
}
