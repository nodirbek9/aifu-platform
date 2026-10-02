import createClient from 'openapi-fetch'
import type { paths } from './schema'
import { getAccessToken, refreshOnce } from './session'
import { ApiFailure, isApiErrorBody, toApiProblem, withStatus } from './errors'

function withToken(request: Request): Request {
  const token = getAccessToken()
  if (token === null) return request
  const headers = new Headers(request.headers)
  headers.set('Authorization', `Bearer ${token}`)
  return new Request(request, { headers })
}

/**
 * fetch used by the API client: attaches the access token and, once per
 * AUTH_TOKEN_EXPIRED, refreshes the pair and retries the original request.
 */
export const authFetch: typeof fetch = async (input, init) => {
  const request = new Request(input, init)
  const retryRequest = request.clone()
  const response = await fetch(withToken(request))
  if (response.status !== 401) return response
  const body: unknown = await response
    .clone()
    .json()
    .catch(() => null)
  if (!isApiErrorBody(body) || body.code !== 'AUTH_TOKEN_EXPIRED') return response
  const refreshed = await refreshOnce()
  if (!refreshed) return response
  return fetch(withToken(retryRequest))
}

// Paths in the contract already start with /api/v1; Vite proxies /api to the backend.
export const api = createClient<paths>({ baseUrl: '', fetch: authFetch })

type ApiResult = { data?: unknown; error?: unknown; response: Response }

/**
 * Awaits an `api.GET/POST/…` call and returns its data, or throws an ApiFailure whose
 * problem carries the error code and the HTTP status. Every hook goes through this, so
 * queries and mutations fail in one shape.
 */
export async function unwrap<R extends ApiResult>(request: Promise<R>): Promise<NonNullable<R['data']>> {
  let result: R
  try {
    result = await request
  } catch (error) {
    throw new ApiFailure(toApiProblem(error))
  }
  if (result.response.ok && result.error === undefined) {
    // openapi-fetch sets `data` on every 2xx with a body; after a 204 there is nothing to read.
    return result.data as NonNullable<R['data']>
  }
  throw new ApiFailure(withStatus(toApiProblem(result.error), result.response.status))
}
