import { Monitor, Moon, Sun } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './dropdown-menu'
import { IconButton } from './icon-button'

export type ThemeValue = 'light' | 'dark' | 'system'

export type ThemeSwitchProps = {
  value: ThemeValue
  onChange: (theme: ThemeValue) => void
  className?: string
}

const themeIcon = { light: Sun, dark: Moon, system: Monitor } as const

export function ThemeSwitch({ value, onChange, className }: ThemeSwitchProps) {
  const { t } = useTranslation()
  const options: readonly { value: ThemeValue; label: string }[] = [
    { value: 'light', label: t('common.themeLight') },
    { value: 'dark', label: t('common.themeDark') },
    { value: 'system', label: t('common.themeSystem') },
  ]
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <IconButton aria-label={t('common.theme')} variant="ghost" className={cn('relative', className)}>
          {options.map(({ value: option }) => {
            const OptionIcon = themeIcon[option]
            return (
              <OptionIcon
                key={option}
                aria-hidden
                className={cn(
                  'duration-normal absolute size-5 transition-[opacity,scale]',
                  option === value ? 'scale-100 opacity-100' : 'scale-50 opacity-0',
                )}
              />
            )
          })}
          <span className="size-5" />
        </IconButton>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        {options.map(({ value: option, label }) => {
          const OptionIcon = themeIcon[option]
          return (
            <DropdownMenuItem
              key={option}
              onSelect={() => {
                onChange(option)
              }}
            >
              <OptionIcon aria-hidden className="size-4" />
              {label}
              {option === value && <span aria-hidden className="bg-accent ml-auto size-1.5 rounded-full" />}
            </DropdownMenuItem>
          )
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
