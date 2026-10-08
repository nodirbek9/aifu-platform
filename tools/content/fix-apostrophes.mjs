#!/usr/bin/env node
// tools/content/fix-apostrophes.mjs — Uzbek apostrophe orthography for the content tree.
//
//   node tools/content/fix-apostrophes.mjs --check    # list uz files that still need fixing, exit 1 if any
//   node tools/content/fix-apostrophes.mjs --write    # rewrite them in place
//
// Rules (docs/PLAN.md §6, Phase 8.1):
//   1. [OoGg] followed by '  `  ‘  ’   ->  that letter + ʻ  (U+02BB, modifier letter turned comma)
//   2. letter + ' + letter              ->  letter + ʼ      (U+02BC, modifier letter apostrophe)
//
// Never rewritten: fenced code blocks (``` / ~~~) and inline code (`...`) inside markdown.
// Never touched:   ru / en files (ru.md, en.md, *.ru.json, *.en.json).
// Idempotent: the replacement letters are not matched by either rule, so a second run changes nothing.

import fs from 'node:fs'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const REPO_ROOT = path.resolve(HERE, '..', '..')
const CONTENT_DIR = path.join(REPO_ROOT, 'content')

const TURNED_COMMA = 'ʻ' // ʻ  after o / g
const MODIFIER_APOSTROPHE = 'ʼ' // ʼ  everywhere else between letters

const RULE_ONE_GLOBAL = /([OoGg])['`‘’]/g
const RULE_TWO_GLOBAL = /(\p{L})'(\p{L})/gu

const FENCE = /^\s*(`{3,}|~{3,})/

/**
 * Applies both rules. Rule 2 consumes the letter after the apostrophe, so two apostrophes
 * separated by one letter (`a'b'c`) need a second pass; repeat until the text stops changing.
 */
export function rewriteUzbekText(text) {
  if (typeof text !== 'string' || text === '') return text
  let current = text
  for (let pass = 0; pass < 8; pass += 1) {
    const next = current
      .replace(RULE_ONE_GLOBAL, (match, letter) => letter + TURNED_COMMA)
      .replace(RULE_TWO_GLOBAL, (match, before, after) => before + MODIFIER_APOSTROPHE + after)
    if (next === current) return next
    current = next
  }
  return current
}

/** How many apostrophes in this text still break Uzbek orthography. */
export function countViolations(text) {
  if (typeof text !== 'string' || text === '') return 0
  const one = text.match(RULE_ONE_GLOBAL)?.length ?? 0
  const two = text.match(RULE_TWO_GLOBAL)?.length ?? 0
  return one + two
}

// ------------------------------------------------------------------ markdown

/** Splits one line into segments, marking inline code (`...`) as protected. */
function inlineSegments(line) {
  const segments = []
  let text = ''
  let i = 0
  while (i < line.length) {
    if (line[i] !== '`') {
      let j = i
      while (j < line.length && line[j] !== '`') j += 1
      text += line.slice(i, j)
      i = j
      continue
    }
    let j = i
    while (j < line.length && line[j] === '`') j += 1
    const runLength = j - i
    // A code span closes on the next run of exactly the same length on this line.
    let k = j
    let close = -1
    while (k < line.length) {
      if (line[k] !== '`') {
        k += 1
        continue
      }
      let m = k
      while (m < line.length && line[m] === '`') m += 1
      if (m - k === runLength) {
        close = m
        break
      }
      k = m
    }
    if (close === -1) {
      // Unmatched backtick run: ordinary text, so `o` + backtick stays one rewrite.
      text += line.slice(i, j)
      i = j
      continue
    }
    if (text !== '') {
      segments.push({ text, code: false })
      text = ''
    }
    segments.push({ text: line.slice(i, close), code: true })
    i = close
  }
  if (text !== '') segments.push({ text, code: false })
  return segments
}

/** Splits markdown into protected (fenced / inline code) and rewritable segments. */
export function markdownSegments(content) {
  const lines = content.split('\n')
  const segments = []
  let fence = null
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    const newline = index < lines.length - 1 ? '\n' : ''
    if (fence !== null) {
      segments.push({ text: line + newline, code: true })
      const closer = new RegExp(`^\\s*${fence.char}{${fence.length},}\\s*$`)
      if (closer.test(line)) fence = null
      continue
    }
    const open = FENCE.exec(line)
    if (open !== null) {
      fence = { char: open[1][0] === '`' ? '[`]' : '[~]', length: open[1].length }
      segments.push({ text: line + newline, code: true })
      continue
    }
    const parts = inlineSegments(line)
    if (parts.length === 0) {
      // Blank line: the line break is the only thing to carry over.
      if (newline !== '') segments.push({ text: newline, code: false })
      continue
    }
    parts.forEach((segment, position) => {
      const last = position === parts.length - 1
      segments.push({ text: segment.text + (last ? newline : ''), code: segment.code })
    })
  }
  return segments
}

export function processMarkdown(content) {
  let violations = 0
  let out = ''
  let pending = ''
  for (const segment of markdownSegments(content)) {
    if (segment.code) {
      if (pending !== '') {
        violations += countViolations(pending)
        out += rewriteUzbekText(pending)
        pending = ''
      }
      out += segment.text
      continue
    }
    pending += segment.text
  }
  if (pending !== '') {
    violations += countViolations(pending)
    out += rewriteUzbekText(pending)
  }
  return { result: out, violations }
}

// ------------------------------------------------------------------ json

function isUzKey(key) {
  return key === 'uz' || /(?:^|[_\-.])uz$/.test(key) || /[a-z0-9]Uz$/.test(key)
}

/** Rewrites string values that hold Uzbek text; keys, numbers and non-uz strings are left alone. */
export function processJsonValue(node, wholeFileIsUzbek) {
  let violations = 0
  const walk = (value, inUzbek) => {
    if (typeof value === 'string') {
      if (!inUzbek) return value
      violations += countViolations(value)
      return rewriteUzbekText(value)
    }
    if (Array.isArray(value)) return value.map((item) => walk(item, inUzbek))
    if (value !== null && typeof value === 'object') {
      const result = {}
      for (const [key, child] of Object.entries(value)) {
        result[key] = walk(child, inUzbek || isUzKey(key))
      }
      return result
    }
    return value
  }
  const value = walk(node, wholeFileIsUzbek)
  return { value, violations }
}

// ------------------------------------------------------------------ files

/** 'uz-md' | 'uz-json' | 'mixed-json' | null (null = not a file this tool may touch). */
export function fileKind(filePath) {
  const base = path.basename(filePath)
  if (/^(uz|ru|en)\.md$/.test(base)) return base === 'uz.md' ? 'uz-md' : null
  if (/\.(ru|en)\.(json|md)$/i.test(base)) return null
  if (/\.uz\.md$/i.test(base)) return 'uz-md'
  if (/\.uz\.json$/i.test(base)) return 'uz-json'
  if (/\.json$/i.test(base)) return 'mixed-json'
  return null
}

export function processFile(filePath) {
  const kind = fileKind(filePath)
  const content = fs.readFileSync(filePath, 'utf8')
  if (kind === null) {
    return { filePath, kind, violations: 0, changed: false, result: content }
  }
  if (kind === 'uz-md') {
    const { result, violations } = processMarkdown(content)
    return { filePath, kind, violations, changed: result !== content, result }
  }
  let parsed
  try {
    parsed = JSON.parse(content)
  } catch (error) {
    console.error(`skip ${filePath}: invalid JSON (${error.message})`)
    return { filePath, kind, violations: 0, changed: false, result: content }
  }
  const { value, violations } = processJsonValue(parsed, kind === 'uz-json')
  // Only re-serialize when something must be fixed, so clean files keep their formatting.
  const result = violations > 0
    ? JSON.stringify(value, null, 2) + (content.endsWith('\n') ? '\n' : '')
    : content
  return { filePath, kind, violations, changed: result !== content, result }
}

function walk(dir) {
  if (!fs.existsSync(dir)) return []
  const files = []
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name.startsWith('.')) continue
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) files.push(...walk(full))
    else if (entry.isFile()) files.push(full)
  }
  return files
}

const relative = (file) => path.relative(REPO_ROOT, file).replaceAll('\\', '/')

function main() {
  const flags = process.argv.slice(2)
  const check = flags.includes('--check')
  const write = flags.includes('--write')
  if (!check && !write) {
    console.error('Usage: node tools/content/fix-apostrophes.mjs --check | --write')
    process.exit(2)
  }

  const targets = walk(CONTENT_DIR)
    .map(processFile)
    .filter((file) => file.kind !== null)

  if (write) {
    const updated = targets.filter((file) => file.changed)
    for (const file of updated) fs.writeFileSync(file.filePath, file.result, 'utf8')
    console.log(`Rewrote ${updated.length} file(s).`)
    for (const file of updated) console.log(`  - ${relative(file.filePath)}`)
  }

  // In --check mode the report is the current state of the files; after --write it is what
  // is left on disk, which must be nothing (the rules fix every pattern they can flag).
  const reported = write ? targets.map((file) => processFile(file.filePath)) : targets
  const offending = reported.filter((file) => file.violations > 0)
  if (offending.length === 0) {
    console.log(`Clean: no ASCII apostrophes left in uz text (${targets.length} file(s) checked).`)
    return
  }
  console.error(`${offending.length} file(s) still contain ASCII apostrophes in uz text:`)
  for (const file of offending) console.error(`  - ${relative(file.filePath)} (${file.violations})`)
  process.exit(1)
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  main()
}
