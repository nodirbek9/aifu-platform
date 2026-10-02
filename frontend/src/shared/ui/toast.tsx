import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import * as ToastPrimitive from '@radix-ui/react-toast'
import { CheckCircle2, CircleAlert, Info, X } from 'lucide-react'
import { tv } from '@/shared/lib/tv'
import { cn } from '@/shared/lib/cn'

export type ToastTone = 'success' | 'danger' | 'info'

type ToastItem = {
  id: number
  title: string
  body?: string
  tone: ToastTone
}

type ToastContextValue = {
  toast: (item: Omit<ToastItem, 'id'>) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function useToast(): ToastContextValue {
  const value = useContext(ToastContext)
  if (value === null) throw new Error('useToast must be used inside ToastProvider')
  return value
}

const toastCard = tv({
  base: [
    'pointer-events-auto flex w-full items-start gap-3 rounded-md border border-border bg-surface-raised p-3.5 shadow-dialog',
    'data-[state=open]:animate-toast-in',
  ],
  variants: {
    tone: {
      success: 'border-l-4 border-l-success',
      danger: 'border-l-4 border-l-danger',
      info: 'border-l-4 border-l-info',
    },
  },
})

const toneIcon: Record<ToastTone, { Icon: typeof Info; className: string }> = {
  success: { Icon: CheckCircle2, className: 'text-success' },
  danger: { Icon: CircleAlert, className: 'text-danger' },
  info: { Icon: Info, className: 'text-info' },
}

let nextToastId = 1

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<readonly ToastItem[]>([])

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((item) => item.id !== id))
  }, [])

  const toast = useCallback((item: Omit<ToastItem, 'id'>) => {
    const id = nextToastId
    nextToastId += 1
    setToasts((current) => [...current.slice(-3), { ...item, id }])
  }, [])

  const contextValue = useMemo(() => ({ toast }), [toast])

  return (
    <ToastContext.Provider value={contextValue}>
      <ToastPrimitive.Provider swipeDirection="right" duration={5000}>
        {children}
        {toasts.map((item) => {
          const { Icon, className: iconClass } = toneIcon[item.tone]
          return (
            <ToastPrimitive.Root
              key={item.id}
              className={toastCard({ tone: item.tone })}
              onOpenChange={(open) => {
                if (!open) dismiss(item.id)
              }}
            >
              <Icon aria-hidden className={cn('mt-0.5 size-5 shrink-0', iconClass)} />
              <div className="grid flex-1 gap-0.5">
                <ToastPrimitive.Title className="text-small text-text font-semibold">
                  {item.title}
                </ToastPrimitive.Title>
                {item.body !== undefined && (
                  <ToastPrimitive.Description className="text-small text-text-muted">
                    {item.body}
                  </ToastPrimitive.Description>
                )}
              </div>
              <ToastPrimitive.Close
                className={cn(
                  'text-text-muted flex size-8 shrink-0 items-center justify-center rounded-sm',
                  'duration-fast hover:bg-surface-sunken hover:text-text transition-[background-color,color]',
                  'focus-visible:outline-focus-ring focus-visible:outline-2',
                )}
              >
                <X aria-hidden className="size-4" />
              </ToastPrimitive.Close>
            </ToastPrimitive.Root>
          )
        })}
        <ToastPrimitive.Viewport className="fixed right-4 bottom-4 z-60 flex w-[min(24rem,calc(100vw-2rem))] flex-col gap-2 outline-none" />
      </ToastPrimitive.Provider>
    </ToastContext.Provider>
  )
}
