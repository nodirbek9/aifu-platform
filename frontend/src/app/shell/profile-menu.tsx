import { LogOut, Megaphone, MonitorSmartphone, Repeat, UserRound } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router'
import { useTheme, THEMES } from '@/app/theme'
import { useAuth } from '@/features/auth/auth-context'
import { roleHomePath, type CurrentUser, type Role } from '@/features/auth/model'
import { cn } from '@/shared/lib/cn'
import { Avatar } from '@/shared/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'

type ProfileMenuProps = {
  user: CurrentUser
  role: Role
}

const THEME_LABEL = {
  light: 'common.themeLight',
  dark: 'common.themeDark',
  system: 'common.themeSystem',
} as const

/**
 * The avatar of the top bar and what is behind it: who is signed in, and signing out.
 * On a phone it also carries what the rail and the top bar have no room for there:
 * the role switch and the theme.
 */
export function ProfileMenu({ user, role }: ProfileMenuProps) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { signOut, signOutAll, setActiveRole } = useAuth()
  const { theme, setTheme } = useTheme()
  const identity = [user.username, user.groupCode].filter((part) => part !== undefined).join(' · ')

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button
          type="button"
          aria-label={`${t('common.profile')}: ${user.fullName}`}
          className={cn(
            'flex size-11 items-center justify-center rounded-sm',
            'duration-fast ease-standard hover:bg-surface-sunken transition-[background-color]',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          )}
        >
          <Avatar name={user.fullName} size="sm" />
        </button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-72">
        <div className="flex items-center gap-3 px-2.5 py-2.5">
          <Avatar name={user.fullName} />
          <div className="grid min-w-0">
            <span className="text-small text-text truncate font-semibold">{user.fullName}</span>
            <span className="text-caption text-text-muted truncate tracking-normal">{identity}</span>
            <span className="text-caption text-text-muted tracking-normal">{t(`role.${role}`)}</span>
          </div>
        </div>

        {user.roles.length > 1 && (
          <DropdownMenuGroup className="md:hidden">
            <DropdownMenuSeparator />
            <DropdownMenuLabel>{t('role.switchRole')}</DropdownMenuLabel>
            {user.roles.map((option) => (
              <DropdownMenuItem
                key={option}
                onSelect={() => {
                  setActiveRole(option)
                  void navigate(roleHomePath(option))
                }}
              >
                <Repeat aria-hidden className="text-text-muted size-4" />
                {t(`role.${option}`)}
                {option === role && <span aria-hidden className="bg-primary ml-auto size-1.5 rounded-full" />}
              </DropdownMenuItem>
            ))}
          </DropdownMenuGroup>
        )}

        <DropdownMenuGroup className="md:hidden">
          <DropdownMenuSeparator />
          <DropdownMenuLabel>{t('common.theme')}</DropdownMenuLabel>
          {THEMES.map((option) => (
            <DropdownMenuItem
              key={option}
              onSelect={() => {
                setTheme(option)
              }}
            >
              {t(THEME_LABEL[option])}
              {option === theme && <span aria-hidden className="bg-primary ml-auto size-1.5 rounded-full" />}
            </DropdownMenuItem>
          ))}
        </DropdownMenuGroup>

        <DropdownMenuSeparator />
        <DropdownMenuItem
          onSelect={() => {
            void navigate('/profile')
          }}
        >
          <UserRound aria-hidden className="text-text-muted size-4" />
          {t('common.profile')}
        </DropdownMenuItem>
        <DropdownMenuItem
          onSelect={() => {
            void navigate('/announcements')
          }}
        >
          <Megaphone aria-hidden className="text-text-muted size-4" />
          {t('announcements.title')}
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onSelect={() => {
            void signOut()
          }}
        >
          <LogOut aria-hidden className="text-text-muted size-4" />
          {t('auth.logout')}
        </DropdownMenuItem>
        <DropdownMenuItem
          onSelect={() => {
            void signOutAll()
          }}
        >
          <MonitorSmartphone aria-hidden className="text-text-muted size-4" />
          {t('auth.logoutAll')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
