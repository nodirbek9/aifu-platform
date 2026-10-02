import { QueryClient } from '@tanstack/react-query'
import { toApiProblem } from './errors'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        const problem = toApiProblem(error)
        // Never retry client errors (4xx): they fail again identically.
        if (problem.status >= 400 && problem.status < 500) return false
        return failureCount < 2
      },
    },
    mutations: {
      retry: false,
    },
  },
})
