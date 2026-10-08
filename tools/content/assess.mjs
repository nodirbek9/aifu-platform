#!/usr/bin/env node
// tools/content/assess.mjs — offline checker for the assessment files of phase 6
// (docs/PLAN.md §4, C0). No network, no Docker.
//
//   node tools/content/assess.mjs check <COURSE> [--lesson X]
//
// <COURSE> is a folder name under content/ (DAS1110-S1) or a path to the folder.
// With no --lesson every lesson folder is walked: each assess.json is checked and its
// problems printed, and the folders without one are reported as a count, not an error.
// midterm.json is checked too when present. Exit code 1 when any problem is found.
import fs from 'node:fs'
import path from 'node:path'
import process from 'node:process'
import { fileURLToPath } from 'node:url'
import { validateAssess, validateMidterm } from './assess-schema.mjs'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const REPO_ROOT = path.resolve(HERE, '..', '..')
const CONTENT_DIR = path.join(REPO_ROOT, 'content')

export function courseDir(spec) {
  if (fs.existsSync(spec) && fs.statSync(spec).isDirectory()) return path.resolve(spec)
  const candidate = path.join(CONTENT_DIR, spec)
  if (fs.existsSync(candidate) && fs.statSync(candidate).isDirectory()) return candidate
  throw new Error(`course folder not found: ${spec}`)
}

function readJson(file) {
  try {
    return { data: JSON.parse(fs.readFileSync(file, 'utf8')) }
  } catch (error) {
    return { parseError: error.message }
  }
}

/**
 * Checks a course folder. Returns { results: [{name, problems}], missing, midterm }:
 * one result per assess.json found, the count of lesson folders without one, and the
 * midterm result (null when midterm.json does not exist or --lesson was given).
 */
export function checkCourse(dir, { lesson = null } = {}) {
  const results = []
  let missing = 0
  let midterm = null

  const lessonNames = fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && !entry.name.startsWith('.'))
    .map((entry) => entry.name)
    .sort()

  const targets = lesson === null ? lessonNames : [lesson]
  for (const name of targets) {
    const lessonPath = path.join(dir, name)
    if (lesson !== null && (!fs.existsSync(lessonPath) || !fs.statSync(lessonPath).isDirectory())) {
      throw new Error(`lesson folder not found: ${name}`)
    }
    const file = path.join(lessonPath, 'assess.json')
    if (!fs.existsSync(file)) {
      if (lesson !== null) results.push({ name, problems: ['assess.json not found'] })
      else missing += 1
      continue
    }
    const { data, parseError } = readJson(file)
    const problems = parseError !== undefined ? [`invalid JSON: ${parseError}`] : validateAssess(data)
    results.push({ name, problems })
  }

  if (lesson === null) {
    const midtermFile = path.join(dir, 'midterm.json')
    if (fs.existsSync(midtermFile)) {
      const { data, parseError } = readJson(midtermFile)
      const problems = parseError !== undefined ? [`invalid JSON: ${parseError}`] : validateMidterm(data)
      midterm = { name: 'midterm.json', problems }
    }
  }

  return { results, missing, midterm }
}

function main() {
  const [command, spec, ...rest] = process.argv.slice(2)
  const lessonFlag = rest.indexOf('--lesson')
  const lesson = lessonFlag === -1 ? null : rest[lessonFlag + 1]
  if (command !== 'check' || spec === undefined || (lessonFlag !== -1 && lesson === undefined)) {
    console.error('Usage: node tools/content/assess.mjs check <COURSE> [--lesson X]')
    process.exit(2)
  }

  let dir
  try {
    dir = courseDir(spec)
  } catch (error) {
    console.error(error.message)
    process.exit(2)
  }

  const { results, missing, midterm } = checkCourse(dir, { lesson })
  const entries = midterm === null ? results : [...results, midterm]

  let failed = 0
  for (const entry of entries) {
    if (entry.problems.length === 0) {
      console.log(`${entry.name}: OK`)
      continue
    }
    failed += 1
    console.log(`${entry.name}: ${entry.problems.length} problem(s)`)
    for (const problem of entry.problems) console.log(`  - ${problem}`)
  }

  if (lesson === null) {
    if (missing > 0) console.log(`${missing} lesson folder(s) without assess.json (counted, not an error)`)
    if (midterm === null) console.log('midterm.json: not present (not checked)')
  }
  console.log(`Checked ${results.length} assess.json file(s).`)
  if (failed > 0) process.exit(1)
  console.log('All checks passed.')
}

if (process.argv[1] && path.resolve(process.argv[1]) === path.resolve(fileURLToPath(import.meta.url))) {
  main()
}
