import { useSyncExternalStore, type ReactNode } from 'react'
import { Compass, Lock, WifiOff } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useRouteError } from 'react-router'
import { toApiProblem } from '@/shared/api/errors'
import { Button, button } from '@/shared/ui/button'
import { ShieldEmblem } from '@/shared/ui/shield'
import { TwoToneRule } from '@/shared/ui/two-tone-rule'

/** While the session is being restored on start: the emblem, nothing that can be clicked. */
export function AppSplash() {
  const { t } = useTranslation()
  return (
    <div
      role="status"
      aria-label={t('common.loading')}
      className="bg-surface grid min-h-dvh place-items-center"
    >
      <ShieldEmblem className="size-16 animate-pulse" />
    </div>
  )
}

type SystemMessageProps = {
  icon: ReactNode
  title: string
  body: string
  action: ReactNode
  detail?: ReactNode
}

/** One composition for the pages that explain why there is nothing to show, and offer a way on. */
function SystemMessage({ icon, title, body, action, detail }: SystemMessageProps) {
  return (
    <section className="mx-auto grid max-w-lg justify-items-start gap-5 py-10 md:py-16">
      <div className="text-text-muted">{icon}</div>
      <div className="grid gap-2">
        <h1 className="font-display text-display-xl text-text font-semibold">{title}</h1>
        <p className="text-body text-text-muted">{body}</p>
      </div>
      <TwoToneRule className="w-24" />
      <div>{action}</div>
      {detail}
    </section>
  )
}

export function NotFoundPage() {
  const { t } = useTranslation()
  return (
    <SystemMessage
      icon={<Compass aria-hidden className="size-9" />}
      title={t('system.notFoundTitle')}
      body={t('system.notFoundBody')}
      action={
        <Link to="/" className={button({ variant: 'primary' })}>
          {t('system.notFoundAction')}
        </Link>
      }
    />
  )
}

export function NoAccessPage() {
  const { t } = useTranslation()
  return (
    <SystemMessage
      icon={<Lock aria-hidden className="size-9" />}
      title={t('system.noAccessTitle')}
      body={t('system.noAccessBody')}
      action={
        <Link to="/" className={button({ variant: 'primary' })}>
          {t('system.noAccessAction')}
        </Link>
      }
    />
  )
}

/** The router's error element: a crash in rendering or loading a route lands here, not on a blank page. */
export function RouteErrorPage() {
  const { t } = useTranslation()
  const problem = toApiProblem(useRouteError())
  return (
    <main className="bg-surface min-h-dvh px-4 md:px-6">
      <div className="mx-auto max-w-lg pt-10">
        <ShieldEmblem className="size-12" />
      </div>
      <SystemMessage
        icon={null}
        title={t('system.errorTitle')}
        body={t('system.errorBody')}
        action={
          <Button
            onClick={() => {
              window.location.reload()
            }}
          >
            {t('system.reload')}
          </Button>
        }
        detail={
          problem.requestId !== undefined ? (
            <p className="text-caption text-text-muted tracking-normal">
              {t('common.requestId')}: <span className="font-mono select-all">{problem.requestId}</span>
            </p>
          ) : undefined
        }
      />
    </main>
  )
}

function subscribeToConnection(onChange: () => void): () => void {
  window.addEventListener('online', onChange)
  window.addEventListener('offline', onChange)
  return () => {
    window.removeEventListener('online', onChange)
    window.removeEventListener('offline', onChange)
  }
}

/** A band under the top bar while the browser is offline; it leaves by itself when the network returns. */
export function OfflineBanner() {
  const { t } = useTranslation()
  const online = useSyncExternalStore(subscribeToConnection, () => navigator.onLine)
  if (online) return null
  return (
    <div
      role="status"
      className="border-border bg-warning-soft flex items-start gap-3 border-b px-4 py-3 md:px-6 lg:px-12"
    >
      <WifiOff aria-hidden className="text-warning mt-0.5 size-5 shrink-0" />
      <p className="text-small text-text">
        <span className="font-semibold">{t('system.offlineTitle')}.</span> {t('system.offlineBody')}
      </p>
    </div>
  )
}
