import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { api, unwrap } from '@/shared/api/client'
import { narrowProblemPage, readProblemType, type AdminProblemType } from './problems-model'

export type ProblemsQueryParams = { type?: AdminProblemType; page?: number; size?: number }
type ProblemsFilterUpdate = { type?: AdminProblemType | null; page?: number; size?: number }
export const problemKeys = { list: ['admin', 'problems'] as const }

function pageNumber(value: unknown, fallback: number, minimum: number, maximum = Number.MAX_SAFE_INTEGER) {
  const number =
    typeof value === 'number' ? value : typeof value === 'string' && value.trim() !== '' ? Number(value) : NaN
  return Number.isSafeInteger(number) && number >= minimum ? Math.min(number, maximum) : fallback
}

export function readProblemsFilter(search: URLSearchParams, params: ProblemsQueryParams = {}) {
  return {
    type: readProblemType(params.type ?? search.get('type')),
    page: pageNumber(params.page ?? search.get('page'), 0, 0),
    size: pageNumber(params.size ?? search.get('size'), 20, 1, 100),
  }
}

export function updateProblemsSearch(search: URLSearchParams, update: ProblemsFilterUpdate) {
  const next = new URLSearchParams(search)
  if ('type' in update) {
    if (update.type) next.set('type', update.type)
    else next.delete('type')
  }
  if (update.size !== undefined) {
    const size = pageNumber(update.size, 20, 1, 100)
    if (size === 20) next.delete('size')
    else next.set('size', String(size))
  }
  const filterChanged = next.get('type') !== search.get('type') || next.get('size') !== search.get('size')
  if (filterChanged || update.page !== undefined) {
    const page = filterChanged ? 0 : pageNumber(update.page, 0, 0)
    if (page === 0) next.delete('page')
    else next.set('page', String(page))
  }
  return next
}

export function useAdminProblemsQuery(params: ProblemsQueryParams = {}) {
  const [search, setSearch] = useSearchParams()
  const filter = readProblemsFilter(search, params)
  const query = useQuery({
    queryKey: [...problemKeys.list, filter],
    queryFn: async () =>
      narrowProblemPage(await unwrap(api.GET('/api/v1/admin/problems', { params: { query: filter } }))),
    placeholderData: keepPreviousData,
  })
  return {
    ...query,
    ...filter,
    updateParams: (update: ProblemsFilterUpdate) => {
      setSearch((current) => updateProblemsSearch(current, update), { replace: true })
    },
  }
}
