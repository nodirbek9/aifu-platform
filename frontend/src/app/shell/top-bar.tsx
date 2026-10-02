import { Search } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useLocation } from 'react-router'
import { useTheme } from '@/app/theme'
import { useAuth } from '@/features/auth/auth-context'
import type { CurrentUser, Role } from '@/features/auth/model'
import { NotificationBell } from '@/features/notifications/notification-bell'
import { SUPPORTED_LOCALES } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { cn } from '@/shared/lib/cn'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { LanguageSwitch } from '@/shared/ui/language-switch'
import { ShieldEmblem } from '@/shared/ui/shield'
import { ThemeSwitch } from '@/shared/ui/theme-switch'
import { activeNavItem } from './nav'
import { ProfileMenu } from './profile-menu'

type TopBarProps = {
  user: CurrentUser
  role: Role
  onOpenPalette: () => void
}

/**
 * The slim bar above every page. Tablet and desktop: search (the command palette), language,
 * theme, notifications, profile. Phone: the emblem and the section's name instead of search,
 * and a compact language menu — the language switch stays visible everywhere (spec G-01).
 */
export function TopBar({ user, role, onOpenPalette }: TopBarProps) {
  const { t } = useTranslation()
  const location = useLocation()
  const { changeLocale } = useAuth()
  const { theme, setTheme } = useTheme()
  const locale = useLocale()
  const section = activeNavItem(role, location.pathname)

  return (
    <header className="border-border bg-surface sticky top-0 z-20 flex h-14 shrink-0 items-center gap-1 border-b px-4 md:h-16 md:gap-2 md:px-6 lg:px-12">
      <div className="flex min-w-0 flex-1 items-center gap-3 md:hidden">
        <ShieldEmblem className="size-8" />
        <span className="font-display text-body text-text truncate font-semibold">
          {section !== undefined ? t(`nav.${section.key}`) : t('app.name')}
        </span>
      </div>

      <div className="hidden flex-1 md:block">
        <button
          type="button"
          onClick={onOpenPalette}
          className={cn(
            'border-border bg-surface-raised flex h-10 w-full max-w-xs items-center gap-2.5 rounded-sm border px-3',
            'text-small text-text-muted duration-fast ease-standard transition-[border-color,color]',
            'hover:border-border-strong hover:text-text',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          )}
        >
          <Search aria-hidden className="size-4 shrink-0" />
          <span className="flex-1 truncate text-left">{t('common.search')}</span>
          <kbd className="border-border bg-surface-sunken text-caption rounded-sm border px-1.5 py-0.5 font-sans tracking-normal">
            Ctrl K
          </kbd>
        </button>
      </div>

      <LanguageSwitch value={locale} onChange={changeLocale} className="hidden md:inline-flex" />

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <button
            type="button"
            aria-label={`${t('common.language')}: ${locale.toUpperCase()}`}
            className={cn(
              'text-small text-text-muted flex h-11 min-w-11 items-center justify-center rounded-sm px-2 font-semibold md:hidden',
              'duration-fast ease-standard hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
              'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
            )}
          >
            {locale.toUpperCase()}
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="min-w-28">
          {SUPPORTED_LOCALES.map((option) => (
            <DropdownMenuItem
              key={option}
              onSelect={() => {
                changeLocale(option)
              }}
            >
              {option.toUpperCase()}
              {option === locale && <span aria-hidden className="bg-primary ml-auto size-1.5 rounded-full" />}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>

      <ThemeSwitch value={theme} onChange={setTheme} className="hidden md:inline-flex" />
      <NotificationBell />
      <ProfileMenu user={user} role={role} />
    </header>
  )
}
