import { ChevronsLeft, ChevronsRight, Repeat } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { NavLink, useNavigate } from 'react-router'
import { useAuth } from '@/features/auth/auth-context'
import { roleHomePath, type Role } from '@/features/auth/model'
import { cn } from '@/shared/lib/cn'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { ShieldEmblem } from '@/shared/ui/shield'
import { Tooltip } from '@/shared/ui/tooltip'
import { navFor, type NavItem } from './nav'

type RailProps = {
  role: Role
  roles: readonly Role[]
  /** Wide (264 px) or narrow (72 px). Below 1024 px the shell always passes false. */
  expanded: boolean
  /** Absent where the width is not the user's choice (tablet). */
  onToggle?: () => void
}

/**
 * The left rail of tablet and desktop: emblem, the role's sections, the role switch.
 * 72 px collapsed (icons with tooltips), 264 px expanded.
 */
export function Rail({ role, roles, expanded, onToggle }: RailProps) {
  const { t } = useTranslation()
  return (
    <aside
      data-expanded={expanded}
      className={cn(
        'border-border bg-surface-raised sticky top-0 hidden h-dvh shrink-0 flex-col border-r md:flex',
        expanded ? 'w-66' : 'w-18',
      )}
    >
      <NavLink
        to={roleHomePath(role)}
        aria-label={t('app.name')}
        className={cn(
          'flex h-16 shrink-0 items-center gap-3 px-4',
          'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:-outline-offset-2',
        )}
      >
        <ShieldEmblem className="size-10" />
        {expanded && (
          <span className="grid min-w-0">
            <span className="font-display text-title text-text leading-6 font-semibold">{t('app.name')}</span>
            <span className="text-caption text-text-muted truncate tracking-normal">
              {t('app.platformTagline')}
            </span>
          </span>
        )}
      </NavLink>

      <nav aria-label={t('common.mainNavigation')} className="flex-1 overflow-y-auto px-3 py-3">
        <ul className="grid gap-1">
          {navFor(role).map((item) => (
            <li key={item.key}>
              <RailLink item={item} expanded={expanded} />
            </li>
          ))}
        </ul>
      </nav>

      <div className="border-border grid gap-1 border-t px-3 py-3">
        {roles.length > 1 && <RoleSwitch role={role} roles={roles} expanded={expanded} />}
        {onToggle !== undefined && (
          <button
            type="button"
            onClick={onToggle}
            aria-label={expanded ? t('common.collapseSidebar') : t('common.expandSidebar')}
            className={cn(railRow, 'text-text-muted hover:bg-surface-sunken hover:text-text')}
          >
            {expanded ? (
              <ChevronsLeft aria-hidden className="size-5 shrink-0" />
            ) : (
              <ChevronsRight aria-hidden className="size-5 shrink-0" />
            )}
            {expanded && <span className="truncate">{t('common.collapseSidebar')}</span>}
          </button>
        )}
      </div>
    </aside>
  )
}

const railRow = cn(
  'flex h-11 w-full items-center gap-3 rounded-sm px-3.5 text-small font-medium',
  'transition-[background-color,color] duration-fast ease-standard',
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-focus-ring',
)

function RailLink({ item, expanded }: { item: NavItem; expanded: boolean }) {
  const { t } = useTranslation()
  const label = t(`nav.${item.key}`)
  const Icon = item.icon
  const link = (
    <NavLink
      to={item.to}
      end={item.end === true}
      viewTransition
      className={({ isActive }) =>
        cn(
          railRow,
          'relative',
          isActive
            ? 'bg-surface-sunken text-primary before:bg-primary before:absolute before:inset-y-2 before:left-0 before:w-0.5 before:rounded-full'
            : 'text-text-muted hover:bg-surface-sunken hover:text-text',
        )
      }
    >
      <Icon aria-hidden className="size-5 shrink-0" />
      <span className={expanded ? 'truncate' : 'sr-only'}>{label}</span>
    </NavLink>
  )
  if (expanded) return link
  // Narrow, the label is only for screen readers: sighted users get it as a tooltip.
  return (
    <Tooltip content={label} side="right">
      {link}
    </Tooltip>
  )
}

function RoleSwitch({ role, roles, expanded }: { role: Role; roles: readonly Role[]; expanded: boolean }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { setActiveRole } = useAuth()
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button
          type="button"
          aria-label={`${t('role.switchRole')}: ${t(`role.${role}`)}`}
          className={cn(railRow, 'text-text hover:bg-surface-sunken')}
        >
          <Repeat aria-hidden className="text-text-muted size-5 shrink-0" />
          {expanded && (
            <span className="grid min-w-0 text-left">
              <span className="text-caption text-text-muted tracking-normal">{t('role.currentRole')}</span>
              <span className="truncate">{t(`role.${role}`)}</span>
            </span>
          )}
        </button>
      </DropdownMenuTrigger>
      <DropdownMenuContent side="right" align="end">
        <DropdownMenuLabel>{t('role.switchRole')}</DropdownMenuLabel>
        {roles.map((option) => (
          <DropdownMenuItem
            key={option}
            onSelect={() => {
              setActiveRole(option)
              void navigate(roleHomePath(option))
            }}
          >
            {t(`role.${option}`)}
            {option === role && <span aria-hidden className="bg-primary ml-auto size-1.5 rounded-full" />}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
