// Imports lesson content written as Markdown (content/<course>/<lesson>/{uz,ru,en}.md) into the
// platform through its API. The format and the rules are in docs/06-CONTENT-GUIDE.md.
//
//   node import.mjs <course folder> [--lesson M5] [--dry-run]
//   node import.mjs <content folder> --all [--dry-run]
//
// Environment: AIFU_API (default http://localhost:8080), AIFU_USER (default admin),
// AIFU_PASSWORD (required unless --dry-run). Re-running gives the same lesson.
import fs from 'node:fs'
import path from 'node:path'
import { marked } from 'marked'
import { Api, findCourse, lessonsByCode } from './api.mjs'

const LOCALES = ['uz', 'ru', 'en']
const CODE_LANGUAGES = { python: 'python', py: 'python', java: 'java', cpp: 'cpp', 'c++': 'cpp' }
const CALLOUTS = { note: 'INFO', info: 'INFO', tip: 'TIP', warning: 'WARNING', danger: 'DANGER' }
/** The block types this importer writes, and so replaces on a re-import. */
const OWNED = new Set(['TEXT', 'CALLOUT', 'CODE_EXAMPLE', 'CASE_STUDY', 'TABLE', 'LINK'])

marked.setOptions({ gfm: true, breaks: false })

// ------------------------------------------------------------------ parsing

function frontMatter(text, file) {
  const match = /^---\r?\n([\s\S]*?)\r?\n---\r?\n/.exec(text)
  if (match === null) throw new Error(`${file}: the front matter (--- title / summary ---) is missing`)
  const meta = {}
  for (const line of match[1].split(/\r?\n/)) {
    const at = line.indexOf(':')
    if (at > 0) meta[line.slice(0, at).trim()] = line.slice(at + 1).trim()
  }
  if (!meta.title) throw new Error(`${file}: no title in the front matter`)
  return { meta, body: text.slice(match[0].length) }
}

const html = (markdown) => marked.parse(markdown.trim()).trim()

/** Q:/A: pairs become questions whose answer opens on demand. */
function selfCheck(lines, file) {
  const items = []
  let current = null
  for (const line of lines) {
    const q = /^Q:\s*(.*)$/.exec(line)
    const a = /^A:\s*(.*)$/.exec(line)
    if (q) {
      current = { q: q[1], a: [] }
      items.push(current)
    } else if (a && current) current.a.push(a[1])
    else if (line.trim() !== '' && current && current.a.length > 0) current.a.push(line)
    else if (line.trim() !== '') throw new Error(`${file}: a :::check line is neither Q: nor A:: ${line}`)
  }
  if (items.length === 0) throw new Error(`${file}: an empty :::check`)
  return items
    .map(
      (item) =>
        `<details><summary>${marked.parseInline(item.q)}</summary>${html(item.a.join('\n'))}</details>`,
    )
    .join('')
}

/**
 * The blocks of one language file, in order. Each block: {type, settings, payload, key} where the
 * key is what must agree across languages (the type and its settings).
 */
export function parseLesson(text, file) {
  const { meta, body } = frontMatter(text, file)
  const lines = body.split(/\r?\n/)
  const blocks = []
  let prose = []
  const flush = () => {
    const markdown = prose.join('\n').trim()
    prose = []
    if (markdown !== '') blocks.push({ type: 'TEXT', settings: {}, payload: { html: html(markdown) } })
  }
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i]
    const fence = /^```\s*([\w+]+)\s*$/.exec(line)
    const directive = /^:::(\w+)\s*(.*)$/.exec(line)
    if (fence && fence[1].toLowerCase() === 'text') {
      // pseudocode stays in the prose as a plain preformatted block, not a runnable example
      let end = i + 1
      while (end < lines.length && !/^```\s*$/.test(lines[end])) end += 1
      prose.push(...lines.slice(i, end + 1))
      i = end
      continue
    }
    if (fence) {
      flush()
      const language = CODE_LANGUAGES[fence[1].toLowerCase()]
      if (!language) throw new Error(`${file}:${i + 1}: unknown code language ${fence[1]}`)
      const code = []
      for (i += 1; i < lines.length && !/^```\s*$/.test(lines[i]); i += 1) code.push(lines[i])
      if (i >= lines.length) throw new Error(`${file}: an unclosed code fence`)
      blocks.push({ type: 'CODE_EXAMPLE', settings: { language }, payload: { code: code.join('\n') } })
    } else if (directive) {
      flush()
      const kind = directive[1].toLowerCase()
      const inner = []
      for (i += 1; i < lines.length && !/^:::\s*$/.test(lines[i]); i += 1) inner.push(lines[i])
      if (i >= lines.length) throw new Error(`${file}: an unclosed :::${kind}`)
      if (CALLOUTS[kind]) {
        blocks.push({ type: 'CALLOUT', settings: { variant: CALLOUTS[kind] }, payload: { html: html(inner.join('\n')) } })
      } else if (kind === 'case') {
        blocks.push({ type: 'CASE_STUDY', settings: {}, payload: { html: html(inner.join('\n')) } })
      } else if (kind === 'check') {
        blocks.push({ type: 'TEXT', settings: {}, check: true, payload: { html: selfCheck(inner, file) } })
      } else if (kind === 'link') {
        const url = directive[2].trim()
        if (!/^https?:\/\/\S+$/.test(url)) throw new Error(`${file}:${i}: :::link needs an http(s) address`)
        const [label, ...note] = inner.map((l) => l.trim()).filter(Boolean)
        if (!label) throw new Error(`${file}: :::link ${url} has no label`)
        blocks.push({
          type: 'LINK',
          settings: { url },
          payload: { label, ...(note.length > 0 && { note: note.join(' ') }) },
        })
      } else {
        throw new Error(`${file}:${i}: unknown block :::${kind}`)
      }
    } else if (/^##\s/.test(line)) {
      flush()
      prose.push(line)
    } else {
      prose.push(line)
    }
  }
  flush()
  for (const block of blocks) block.key = `${block.type}${block.check ? ':check' : ''}:${JSON.stringify(block.settings)}`
  return { title: meta.title, summary: meta.summary ?? '', blocks }
}

/** The three languages of a lesson, checked to have one skeleton. */
export function readLesson(dir) {
  const parsed = {}
  for (const locale of LOCALES) {
    const file = path.join(dir, `${locale}.md`)
    if (!fs.existsSync(file)) throw new Error(`${file} is missing`)
    parsed[locale] = parseLesson(fs.readFileSync(file, 'utf8'), file)
  }
  const reference = parsed.uz.blocks.map((b) => b.key)
  for (const locale of ['ru', 'en']) {
    const keys = parsed[locale].blocks.map((b) => b.key)
    const at = reference.findIndex((key, index) => keys[index] !== key)
    if (keys.length !== reference.length || at >= 0) {
      const index = at >= 0 ? at : Math.min(keys.length, reference.length)
      throw new Error(
        `${dir}: ${locale}.md differs from uz.md at block ${index + 1}: ${keys[index] ?? '(none)'} vs ${reference[index] ?? '(none)'}`,
      )
    }
  }
  return parsed
}

async function importLesson(api, lesson, parsed) {
  const id = lesson.id
  for (const locale of LOCALES) {
    await api.call('PUT', `/teacher/lessons/${id}/translations/${locale}`, {
      title: parsed[locale].title,
      ...(parsed[locale].summary && { summary: parsed[locale].summary }),
    })
  }
  const detail = await api.call('GET', `/teacher/lessons/${id}`)
  const existing = (detail.blocks ?? []).slice().sort((a, b) => a.position - b.position)
  for (const block of existing) {
    if (OWNED.has(block.blockType)) await api.call('DELETE', `/teacher/blocks/${block.id}`)
  }
  const kept = existing.filter((block) => !OWNED.has(block.blockType))
  const created = []
  for (const [index, block] of parsed.uz.blocks.entries()) {
    const made = await api.call('POST', `/teacher/lessons/${id}/blocks`, {
      blockType: block.type,
      settings: block.settings,
      visibility: 'ALL',
    })
    for (const locale of LOCALES) {
      await api.call('PUT', `/teacher/blocks/${made.id}/translations/${locale}`, {
        payload: parsed[locale].blocks[index].payload,
      })
    }
    created.push(made.id)
  }
  // the teacher's plan first, then the lesson, then what it leads to (practice, files)
  const plan = kept.filter((block) => block.blockType === 'LESSON_PLAN').map((block) => block.id)
  const rest = kept.filter((block) => block.blockType !== 'LESSON_PLAN').map((block) => block.id)
  await api.call('PUT', `/teacher/lessons/${id}/blocks/reorder`, { orderedBlockIds: [...plan, ...created, ...rest] })
  return { written: created.length, kept: kept.length }
}

// ------------------------------------------------------------------ main

async function importCourse(folder, options, api) {
  const spec = JSON.parse(fs.readFileSync(path.join(folder, 'course.json'), 'utf8'))
  const lessonDirs = fs
    .readdirSync(folder, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && (!options.lesson || entry.name === options.lesson))
    .map((entry) => entry.name)
    .sort((a, b) => a.localeCompare(b, 'en', { numeric: true }))
  const parsed = new Map()
  for (const code of lessonDirs) parsed.set(code, readLesson(path.join(folder, code)))
  const words = [...parsed.values()].reduce(
    (sum, lesson) => sum + lesson.uz.blocks.reduce((n, b) => n + JSON.stringify(b.payload).split(/\s+/).length, 0),
    0,
  )
  console.log(`${path.basename(folder)}: ${parsed.size} lessons parsed, ~${words} words (uz)`)
  if (options.dryRun) return
  const courseId = await findCourse(api, spec)
  const lessons = await lessonsByCode(api, courseId)
  for (const [code, lesson] of parsed) {
    const target = lessons.get(code)
    if (!target) throw new Error(`${spec.subjectCode}: no lesson with the code ${code}`)
    const result = await importLesson(api, target, lesson)
    console.log(`  ${code}: ${result.written} blocks written, ${result.kept} kept`)
  }
}

const args = process.argv.slice(2)
const options = {
  dryRun: args.includes('--dry-run'),
  all: args.includes('--all'),
  lesson: args.includes('--lesson') ? args[args.indexOf('--lesson') + 1] : undefined,
}
const target = args.find((arg) => !arg.startsWith('--') && arg !== options.lesson)
if (!target) {
  console.error('usage: node import.mjs <course folder> [--lesson CODE] [--dry-run] | <content folder> --all')
  process.exit(2)
}
const api = new Api(process.env.AIFU_API ?? 'http://localhost:8080', process.env.AIFU_USER ?? 'admin', process.env.AIFU_PASSWORD)
if (!options.dryRun && !process.env.AIFU_PASSWORD) {
  console.error('AIFU_PASSWORD is not set')
  process.exit(2)
}
const folders = options.all
  ? fs
      .readdirSync(target, { withFileTypes: true })
      .filter((entry) => entry.isDirectory() && fs.existsSync(path.join(target, entry.name, 'course.json')))
      .map((entry) => path.join(target, entry.name))
  : [target]
try {
  for (const folder of folders) await importCourse(folder, options, api)
} catch (error) {
  console.error(error.message)
  process.exit(1)
}
