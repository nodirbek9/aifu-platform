import { useRef, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import gsap from 'gsap'
import { useGSAP } from '@gsap/react'
import campusPhoto from '@/assets/brand/campus.jpg'
import { applyLocale } from '@/shared/i18n'
import { useLocale } from '@/shared/i18n/use-locale'
import { LanguageSwitch } from '@/shared/ui/language-switch'
import { ShieldEmblem } from '@/shared/ui/shield'

gsap.registerPlugin(useGSAP)

type PublicLayoutProps = {
  children: ReactNode
}

/**
 * The public face (login, password change). Desktop: a brand pane beside the form. The pane is
 * itself the emblem at large — navy above a magenta foot — and holds the campus photograph in
 * the shield's silhouette (square shoulders, round base) under a navy duotone, then the emblem
 * and the university's name. Phone: the brand is a compact band above the form.
 *
 * Entrance (brief §8, moment 1): the photograph settles, the emblem and the name set in, the
 * form follows — once per page load; nothing moves under reduced motion.
 */
export function PublicLayout({ children }: PublicLayoutProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const rootRef = useRef<HTMLDivElement>(null)

  useGSAP(
    () => {
      if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
      gsap
        .timeline({ defaults: { ease: 'power3.out' } })
        .from('[data-animate="photo"]', { yPercent: -6, opacity: 0, duration: 0.7 })
        .from('[data-animate="emblem"]', { y: 16, opacity: 0, duration: 0.5 }, '-=0.35')
        .from('[data-animate="name"]', { y: 12, opacity: 0, duration: 0.45 }, '-=0.3')
        .from('[data-animate="foot"]', { scaleX: 0, duration: 0.5, transformOrigin: 'left center' }, '-=0.3')
        .from('[data-animate="form"]', { y: 12, opacity: 0, duration: 0.45 }, '-=0.45')
    },
    { scope: rootRef },
  )

  return (
    <div ref={rootRef} className="grid min-h-dvh lg:grid-cols-[45fr_55fr]">
      <aside className="bg-brand-navy relative hidden flex-col overflow-hidden lg:flex">
        <div data-animate="photo" className="relative h-[54%] shrink-0 overflow-hidden rounded-b-full">
          <img src={campusPhoto} alt="" className="size-full object-cover grayscale" />
          {/* duotone: the darks of the photograph become navy, the lights stay light */}
          <div className="bg-brand-navy absolute inset-0 mix-blend-screen" />
          <div className="from-brand-navy/10 to-brand-navy/55 absolute inset-0 bg-linear-to-b" />
        </div>

        <div className="flex flex-1 flex-col items-center justify-center gap-5 px-10 pb-10 text-center">
          <div data-animate="emblem">
            <ShieldEmblem className="h-24 w-20" />
          </div>
          <div data-animate="name" className="grid gap-2">
            <p className="font-display text-display-xl max-w-md font-semibold text-white">
              {t('app.universityName')}
            </p>
            <p className="text-caption font-medium text-white/70 uppercase">{t('app.platformTagline')}</p>
          </div>
        </div>

        <div data-animate="foot" aria-hidden className="bg-brand-magenta h-2 shrink-0" />
      </aside>

      <main className="bg-surface flex min-h-dvh flex-col">
        <div className="bg-brand-navy lg:hidden">
          <div className="flex items-center gap-3 px-4 py-4 sm:px-8">
            <ShieldEmblem className="h-12 w-10" />
            <div className="grid">
              <span className="font-display text-title font-semibold text-white">{t('app.name')}</span>
              <span className="text-caption tracking-normal text-white/75">{t('app.platformTagline')}</span>
            </div>
          </div>
          <div aria-hidden className="bg-brand-magenta h-1" />
        </div>

        <div className="flex items-center justify-end px-4 pt-4 sm:px-8">
          <LanguageSwitch value={locale} onChange={applyLocale} />
        </div>

        <div data-animate="form" className="flex flex-1 items-center justify-center px-4 py-10 sm:px-8">
          <div className="w-full max-w-sm">{children}</div>
        </div>

        <footer className="text-caption text-text-muted px-4 pb-6 text-center tracking-normal sm:px-8">
          {t('app.universityName')}
        </footer>
      </main>
    </div>
  )
}
