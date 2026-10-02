---
name: aifu-api
description: 'How the AIFU frontend talks to the backend — typed client from the OpenAPI contract, auth and single-flight token refresh, error codes, locale, pagination, polling of runs and submissions, files as blobs, sanitized HTML, timetable conflicts. Use whenever you fetch, mutate, or model server data in frontend/. Triggers on: API, endpoint, fetch, query, mutation, TanStack Query, openapi, token, refresh, 401, 409, error, pagination, polling, submission, run, upload, file, locale.'
---

# Talking to the AIFU backend

Sources, in order of authority: `docs/api/openapi.json` (the contract) → `docs/api/ENDPOINTS.md`
(one line per endpoint) → `docs/03-API-GUIDE.md` (rules and the screen → endpoint map). Find the
endpoints of a screen in the guide §6 before writing any hook.

## Rules

1. **No mock data.** No fixtures in the app, no fallback arrays, no fake delays. Missing data →
   an entry in `frontend/BACKEND-REQUESTS.md` + an honest empty state.
2. **Types are generated**, never hand-written:
   `openapi-typescript ../docs/api/openapi.json -o src/shared/api/schema.d.ts`
   (script `npm run api:types`; the generated file is committed and never edited).
3. **Only `src/shared/api` knows about HTTP.** Features export hooks (`useLessonQuery`,
   `useSubmitSolutionMutation`); components use hooks.
4. **Null fields are absent** from responses and every generated property is optional. Narrow
   once, at the edge of the feature (a Zod schema or a mapper that returns a strict type). Do not
   scatter `?.` and `??` through components, and do not cast.
5. **Branch on `error.code`**, never on the message or only on the HTTP status.

## The client

```ts
// src/shared/api/client.ts
import createClient from 'openapi-fetch'
import type { paths } from './schema'

export const api = createClient<paths>({ baseUrl: '', fetch: authFetch })
// paths already start with /api/v1; Vite proxies /api to http://localhost:8080

const { data, error } = await api.GET('/api/v1/lessons/{id}', {
  params: { path: { id }, query: { locale } },
})
```

`authFetch` adds `Authorization: Bearer <access token>` and handles expiry:

```ts
let refreshing: Promise<void> | null = null

const refreshOnce = (): Promise<void> => {
  refreshing ??= doRefresh().finally(() => {
    refreshing = null
  })
  return refreshing
}

export const authFetch: typeof fetch = async (input, init) => {
  const request = new Request(input, init)
  const retry = request.clone() // a body can be read only once
  const response = await fetch(withToken(request))
  if (response.status !== 401) return response
  const body = await response.clone().json().catch(() => null)
  if (body?.code !== 'AUTH_TOKEN_EXPIRED') return response
  await refreshOnce() // on failure: clear the session, go to /login
  return fetch(withToken(retry))
}
```

- The refresh token is **single use**. Two parallel refresh calls with the same token are
  treated as theft and log the account out everywhere — hence the single shared promise. With
  several browser tabs, guard the refresh with `navigator.locks` and re-read the stored token
  inside the lock.
- Access token in memory; refresh token in `localStorage`. On start: if a refresh token exists,
  refresh, then `GET /auth/me`.
- `mustChangePassword: true` → route guard sends the user to the change-password page before
  anything else. After `PUT /auth/password` log in again (all refresh tokens were revoked).
- Roles and permissions come from `GET /auth/me`. Navigation by `roles`, single actions by
  `permissions`.

## Errors

One mapper turns a failed response into `ApiProblem { status, code, message, details, requestId }`.
`code` → translated text in `i18n` (`errors.<CODE>`); `message` is only the fallback.

| Code | UI |
|---|---|
| `VALIDATION_FAILED` | `details.fields` → field errors in React Hook Form (`setError`) |
| `AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_NOT_ACTIVE` | Login form message |
| `AUTH_TOKEN_MISSING` / `_INVALID`, `AUTH_REFRESH_TOKEN_INVALID` / `_REUSED` | Clear session → login, keep the return URL |
| `AUTH_FORBIDDEN`, `NOT_COURSE_TEACHER`, `NOT_ENROLLED` | "No access" page or inline message |
| `RESOURCE_NOT_FOUND` | Not-found state (also returned for things that are not yours) |
| `TIMETABLE_CONFLICT` | Not an error: show `details.conflicts` (blocking) and `details.warnings` (repeat with `force: true` + `forceReason`) |
| `CONCURRENT_MODIFICATION` | Reload the data, tell the user, let them retry |
| `ATTEMPT_LIMIT_REACHED`, `ATTEMPT_EXPIRED`, `LANGUAGE_NOT_ENABLED`, `SOURCE_TOO_LARGE`, … (422) | Explain next to the action |
| `RATE_LIMIT_EXCEEDED`, `TOO_MANY_CONCURRENT_SUBMISSIONS` | "Wait a moment"; no automatic retry |
| `EXECUTION_ENGINE_UNAVAILABLE`, `STORAGE_UNAVAILABLE` | Error state with retry |

TanStack Query: `retry` only for network errors and 5xx, never for 4xx. Every error state shows
the `requestId`.

## Locale

Send `locale` (`uz` / `ru` / `en`) on every read of translated content and put it in the query
key: `['lesson', id, locale]`. When the response says `titleIsFallback` / `payloadIsFallback`,
show the "shown in another language" mark. Save the interface language with
`PATCH /auth/me {preferredLocale}`.

## Pagination

`page` (from 0), `size` (≤ 100), `sort=property,asc|desc` → `{content, page, size,
totalElements, totalPages, hasNext, hasPrevious}`. Keep page, sort and filters in the URL
(search params) so a list can be linked and survives reload. `placeholderData: keepPreviousData`
while paging.

## Runs and submissions (asynchronous)

`POST /runs` → 202, `POST /submissions` → 201, both with `status: QUEUED`. Then poll:

```ts
useQuery({
  queryKey: ['run', id],
  queryFn: () => getRun(id),
  refetchInterval: (query) => {
    const status = query.state.data?.status
    if (status === 'COMPLETED' || status === 'FAILED' || status === 'CANCELLED') return false
    return Date.now() - startedAt < 10_000 ? 1000 : 2000
  },
})
```

- Show queued → running → result. `status: FAILED` + `error` = the platform failed, not the
  student: say so, offer to run again.
- A run never counts as an attempt; only `POST /submissions` is graded.
- `Idempotency-Key: <uuid per click>` on `POST /submissions` and `POST /attempts/{id}/submit`.
- Disable Run / Submit while one is in flight; handle 429 with a calm message.
- Verdicts: `OK` (custom run finished), `ACCEPTED`, `PARTIAL`, `WRONG_ANSWER`,
  `COMPILATION_ERROR` (show `compileOutput`), `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`,
  `MEMORY_LIMIT_EXCEEDED`, `OUTPUT_LIMIT_EXCEEDED`, `INTERNAL_ERROR`. Take the exact enum from the
  contract.
- Hidden tests arrive without input or output: show only pass / fail, time, verdict.
- Drafts (per task and language) live in `localStorage`; the server stores submissions only.

## Files and HTML

- `GET /files/{id}/content` needs the token: fetch as a blob through the client, use
  `URL.createObjectURL`, revoke on unmount. `<img src="/api/…">` does not work.
- Upload: `POST /files` multipart; allowed types and size limit errors come back as
  `FILE_TYPE_NOT_ALLOWED` / validation errors.
- HTML from the server (`TEXT`, `CALLOUT`, `TABLE`, `CASE_STUDY`, code explanations,
  announcements, feedback) is sanitized on the server; still render it only through one
  `<RichText>` component that runs DOMPurify. No other `dangerouslySetInnerHTML` in the app.

## Calendar

The server computes week number, odd / even parity, holidays, cancelled and moved classes.
Never compute parity or week numbers in the browser. A class is addressed by `entryId` + `date`
exactly as given. Statuses `CANCELLED`, `HOLIDAY`, `MOVED_OUT` are not held: show them struck
through with `changeReason`. Times of day are campus time (Asia/Tashkent) — format with
`Intl.DateTimeFormat` and `timeZone: 'Asia/Tashkent'`, never with the browser's zone.

## Mutations

Invalidate exactly the queries the mutation changed. Optimistic updates only where the action is
frequent and cheap to undo (attendance marks, "mark as read", reorder); roll back on error and
say so. Forms: React Hook Form + Zod; the submit button shows progress and is disabled while
pending; server field errors land on their fields.

## When the API does not fit

Do not work around it with invented data or by calling endpoints of another role. Add to
`frontend/BACKEND-REQUESTS.md`:

```
## <date> — <screen, spec id>
Needs: <what data or action>
Today: <what the API offers and why it is not enough>
Proposal: <METHOD /path — request → response>
Meanwhile: <what the screen shows>
```
