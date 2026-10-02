import type { ReactNode } from 'react'
import { QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '@/features/auth/auth-context'
import { queryClient } from '@/shared/api/query-client'
import { ToastProvider } from '@/shared/ui/toast'
import { TooltipProvider } from '@/shared/ui/tooltip'
import { ThemeProvider } from './theme'

export function Providers({ children }: { children: ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthProvider>
          <TooltipProvider delayDuration={300}>
            <ToastProvider>{children}</ToastProvider>
          </TooltipProvider>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
