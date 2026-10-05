// The texts of a course's subject in three languages: title, short title, summary, description,
// aim ("Fan maqsadi"), objectives ("Fanning vazifasi") and the learning outcomes (docs/06-CONTENT-GUIDE.md).
//
//   node subject.mjs export <course folder>     writes subject.uz.json from the platform
//   node subject.mjs import <course folder> [--dry-run]
//
// The files live next to course.json: subject.uz.json, subject.ru.json, subject.en.json. Learning
// outcomes are keyed by their codes (TN1, TN2, ...), so the files import into any database that
// holds the subject. subject.ru.json and subject.en.json must have the same outcome codes as
// subject.uz.json: a translation can never silently drop an outcome. Courses of one subject in
// several terms keep the subject files in one of their folders only.
import fs from 'node:fs'
import path from 'node:path'
import { apiFromEnvironment } from './api.mjs'

const LOCALES = ['uz', 'ru', 'en']
const FIELDS = ['title', 'shortTitle', 'summary', 'description', 'aim', 'objectives']

/** The fields of an object that have a value: what a translation file keeps. */
function present(object, fields) {
  const out = {}
  for (const field of fields) {
    if (object?.[field] !== null && object?.[field] !== undefined && object[field] !== '') out[field] = object[field]
  }
  return out
}

async function readSubject(api, subjectCode) {
  const page = await api.call('GET', `/admin/subjects?query=${encodeURIComponent(subjectCode)}&size=50`)
  const found = (page.content ?? []).filter((s) => s.code === subjectCode)
  if (found.length !== 1) throw new Error(`subject ${subjectCode}: ${found.length} matches`)
  return api.call('GET', `/admin/subjects/${found[0].id}`)
}

function exportLocale(subject, locale) {
  const translation = subject.translations.find((t) => t.locale === locale)
  const outcomes = {}
  for (const outcome of [...subject.learningOutcomes].sort((a, b) => a.displayOrder - b.displayOrder)) {
    const statement = outcome.translations.find((t) => t.locale === locale)?.statement
    if (statement) outcomes[outcome.code] = statement
  }
  return { ...present(translation, FIELDS), outcomes }
}

function checkShape(file, reference, name) {
  if (!file.title) throw new Error(`${name}: title is required`)
  const codes = Object.keys(file.outcomes ?? {}).sort().join(',')
  const expected = Object.keys(reference.outcomes ?? {}).sort().join(',')
  if (codes !== expected) throw new Error(`${name}: outcomes [${codes}] differ from subject.uz.json [${expected}]`)
}

async function importLocale(api, subject, locale, file, dryRun) {
  let calls = 0
  const body = present(file, FIELDS)
  if (!dryRun) await api.call('PUT', `/admin/subjects/${subject.id}/translations/${locale}`, body)
  calls++
  for (const [code, statement] of Object.entries(file.outcomes ?? {})) {
    const outcome = subject.learningOutcomes.find((o) => o.code === code)
    if (!outcome) throw new Error(`${subject.code}: no learning outcome ${code}`)
    if (!dryRun) await api.call('PUT', `/admin/subjects/learning-outcomes/${outcome.id}/translations/${locale}`, { statement })
    calls++
  }
  return calls
}

async function main() {
  const [command, folder] = process.argv.slice(2)
  const dryRun = process.argv.includes('--dry-run')
  if (!['export', 'import'].includes(command) || !folder) {
    console.error('usage: node subject.mjs export|import <course folder> [--dry-run]')
    process.exit(2)
  }
  const spec = JSON.parse(fs.readFileSync(path.join(folder, 'course.json'), 'utf8'))
  const api = apiFromEnvironment()
  const subject = await readSubject(api, spec.subjectCode)
  if (command === 'export') {
    const target = path.join(folder, 'subject.uz.json')
    fs.writeFileSync(target, JSON.stringify(exportLocale(subject, 'uz'), null, 2) + '\n')
    console.log(`${spec.subjectCode}: wrote ${target}`)
    return
  }
  const read = (locale) => {
    const file = path.join(folder, `subject.${locale}.json`)
    return fs.existsSync(file) ? JSON.parse(fs.readFileSync(file, 'utf8')) : null
  }
  const reference = read('uz')
  if (!reference) throw new Error(`${folder}: subject.uz.json is missing (run export first)`)
  for (const locale of LOCALES) {
    const file = read(locale)
    if (!file) continue
    checkShape(file, reference, `subject.${locale}.json`)
    const calls = await importLocale(api, subject, locale, file, dryRun)
    console.log(`${spec.subjectCode} ${locale}: ${calls} translations ${dryRun ? 'checked' : 'written'}`)
  }
}

main().catch((error) => {
  console.error(error.message)
  process.exit(1)
})
