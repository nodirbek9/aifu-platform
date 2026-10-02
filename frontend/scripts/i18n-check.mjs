#!/usr/bin/env node
/**
 * i18n:check — the three catalogues must have identical keys, no empty values,
 * a text for every ApiError code of the contract, and no unused keys in src/.
 * Shared by `npm run i18n:check` and the vitest parity test.
 */
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('..', import.meta.url))
const I18N_DIR = join(root, 'src', 'shared', 'i18n')
const SCHEMA = join(root, 'src', 'shared', 'api', 'schema.d.ts')
const SRC = join(root, 'src')

export function flattenKeys(value, prefix = '') {
  const keys = []
  for (const [key, child] of Object.entries(value)) {
    const path = prefix === '' ? key : `${prefix}.${key}`
    if (typeof child === 'object' && child !== null) keys.push(...flattenKeys(child, path))
    else keys.push(path)
  }
  return keys.sort()
}

export function loadCatalogs() {
  const catalogs = {}
  for (const locale of ['uz', 'ru', 'en']) {
    catalogs[locale] = JSON.parse(readFileSync(join(I18N_DIR, `${locale}.json`), 'utf8'))
  }
  return catalogs
}

export function extractErrorCodes() {
  const schema = readFileSync(SCHEMA, 'utf8')
  const apiErrorBlock = schema.match(/ApiError:\s*\{[\s\S]*?code\?:\s*([^;]+);/)
  if (apiErrorBlock === null) throw new Error('ApiError.code not found in schema.d.ts')
  return [...apiErrorBlock[1].matchAll(/"([A-Z_]+)"/g)].map((match) => match[1]).sort()
}

function* walk(dir) {
  for (const entry of readdirSync(dir)) {
    if (entry === 'node_modules' || entry.startsWith('.')) continue
    const full = join(dir, entry)
    if (statSync(full).isDirectory()) yield* walk(full)
    else if (/\.(ts|tsx)$/.test(entry) && !entry.endsWith('.test.ts') && !entry.endsWith('.test.tsx'))
      yield full
  }
}

/** Keys referenced as t('a.b') plus dynamic prefixes like t(`sessionType.${x}`). */
export function findUsedKeys() {
  const used = new Set()
  const dynamicPrefixes = new Set()
  for (const file of walk(SRC)) {
    const source = readFileSync(file, 'utf8')
    for (const match of source.matchAll(/\bt\(\s*['"]([a-zA-Z0-9._-]+)['"]/g)) used.add(match[1])
    for (const match of source.matchAll(/\bi18nKey=\s*['"]([a-zA-Z0-9._-]+)['"]/g)) used.add(match[1])
    for (const match of source.matchAll(/\bt\(\s*`([a-zA-Z0-9._-]+)\.\$\{/g)) dynamicPrefixes.add(match[1])
  }
  return { used, dynamicPrefixes }
}

export function check() {
  const problems = []
  const catalogs = loadCatalogs()
  const keysByLocale = Object.fromEntries(
    Object.entries(catalogs).map(([locale, catalog]) => [locale, flattenKeys(catalog)]),
  )
  const base = keysByLocale['uz']
  for (const locale of ['ru', 'en']) {
    const keys = keysByLocale[locale]
    const missing = base.filter((key) => !keys.includes(key))
    const extra = keys.filter((key) => !base.includes(key))
    for (const key of missing) problems.push(`${locale}: missing key ${key}`)
    for (const key of extra) problems.push(`${locale}: extra key ${key}`)
  }
  for (const [locale, catalog] of Object.entries(catalogs)) {
    for (const key of flattenKeys(catalog)) {
      const value = key.split('.').reduce((node, part) => node?.[part], catalog)
      if (typeof value !== 'string' || value.trim() === '') problems.push(`${locale}: empty value ${key}`)
    }
  }
  for (const code of extractErrorCodes()) {
    for (const locale of ['uz', 'ru', 'en']) {
      if (!keysByLocale[locale].includes(`errors.${code}`)) {
        problems.push(`${locale}: no text for error code ${code}`)
      }
    }
  }
  const { used, dynamicPrefixes } = findUsedKeys()
  const isUsed = (key) => used.has(key) || [...dynamicPrefixes].some((prefix) => key.startsWith(`${prefix}.`))
  for (const key of used) {
    if (!base.includes(key)) problems.push(`code uses unknown key ${key}`)
  }
  for (const key of base) {
    // errors.* keys are looked up dynamically by error code — always allowed.
    if (key.startsWith('errors.')) continue
    if (!isUsed(key)) problems.push(`unused key ${key}`)
  }
  return problems
}

const isMain = process.argv[1] !== undefined && fileURLToPath(import.meta.url) === process.argv[1]
if (isMain) {
  const problems = check()
  if (problems.length > 0) {
    console.error(`i18n:check failed with ${problems.length} problem(s):`)
    for (const problem of problems) console.error(`  - ${problem}`)
    process.exit(1)
  }
  const count = flattenKeys(loadCatalogs()['uz']).length
  console.log(`i18n:check OK — ${count} keys × 3 locales, all error codes covered`)
}
