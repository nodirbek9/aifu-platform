// Generates docs/api/ENDPOINTS.md — a one-line-per-operation catalogue — from
// docs/api/openapi.json. The OpenAPI file is the contract; this is the page a person (or a
// coding agent) reads to find the endpoint for a screen.
//   node tools/api-docs/endpoints.mjs
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const source = path.resolve(here, '../../docs/api/openapi.json')
const target = path.resolve(here, '../../docs/api/ENDPOINTS.md')
const api = JSON.parse(fs.readFileSync(source, 'utf8'))

const typeOf = (schema) => {
  if (!schema) return ''
  if (schema.$ref) return schema.$ref.split('/').pop()
  if (schema.type === 'array') return `${typeOf(schema.items) || 'object'}[]`
  if (schema.additionalProperties) return 'map'
  return schema.format === 'uuid' ? 'uuid' : (schema.type ?? 'object')
}
const bodyOf = (op) => {
  const content = op.requestBody?.content
  if (!content) return ''
  if (content['multipart/form-data']) return 'multipart'
  return typeOf(Object.values(content)[0]?.schema)
}
const responseOf = (op) => {
  const ok = Object.entries(op.responses ?? {}).find(([code]) => code.startsWith('2'))
  if (!ok) return ''
  const schema = Object.values(ok[1].content ?? {})[0]?.schema
  const type = typeOf(schema)
  return `${ok[0]}${type ? ' ' + type : ''}`
}
const queryOf = (op) =>
  (op.parameters ?? [])
    .filter((p) => p.in === 'query')
    .map((p) => (p.required ? p.name : `${p.name}?`))
    .join(', ')
const cell = (text) => String(text ?? '').replace(/\|/g, '\\|').replace(/\s+/g, ' ').trim()

const byTag = new Map()
for (const [route, methods] of Object.entries(api.paths)) {
  for (const [method, op] of Object.entries(methods)) {
    const tag = op.tags?.[0] ?? 'Other'
    if (!byTag.has(tag)) byTag.set(tag, [])
    byTag.get(tag).push({ method: method.toUpperCase(), route: route.replace('/api/v1', ''), op })
  }
}
const order = { GET: 0, POST: 1, PUT: 2, PATCH: 3, DELETE: 4 }
const lines = [
  '# API endpoints',
  '',
  'GENERATED from `docs/api/openapi.json` by `tools/api-docs/endpoints.mjs` — do not edit by hand.',
  'Every path is under `/api/v1`. Types are schema names of the OpenAPI file; `PageResponseX` is a',
  'page of `X` (`content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`).',
  'A `?` marks an optional query parameter. How to use the API (auth, errors, locale, polling) is',
  'in `docs/03-API-GUIDE.md`.',
  '',
]
let total = 0
for (const tag of [...byTag.keys()].sort((a, b) => a.localeCompare(b))) {
  const description = api.tags?.find((t) => t.name === tag)?.description
  lines.push(`## ${tag}`, '')
  if (description) lines.push(description, '')
  lines.push('| Method | Path | What it does | Query | Body | Returns |', '|---|---|---|---|---|---|')
  const ops = byTag.get(tag).sort((a, b) => a.route.localeCompare(b.route) || order[a.method] - order[b.method])
  for (const { method, route, op } of ops) {
    total++
    lines.push(`| ${method} | \`${route}\` | ${cell(op.summary)} | ${cell(queryOf(op))} | ${cell(bodyOf(op))} | ${cell(responseOf(op))} |`)
  }
  lines.push('')
}
fs.writeFileSync(target, lines.join('\n'))
console.log(`wrote ${path.relative(process.cwd(), target)}: ${total} operations in ${byTag.size} groups`)
