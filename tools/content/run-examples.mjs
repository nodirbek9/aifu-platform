// Runs the worked examples of a lesson and checks their output (docs/06-CONTENT-GUIDE.md).
//
//   node run-examples.mjs ../../content/DAS1110-S1 [--lesson M2]
//
// A lesson folder may hold examples.json: one entry per code example (a run of ``` fences), in
// the order they appear, each with the runs to check:
//   [{ "runs": [{ "input": "5\n", "output": "78.54\n" }] }, { "skip": "reads a file" }]
// Every language of the example (python, java, cpp) runs in the same images the sandbox uses and
// must print exactly the expected output. The code runs from en.md; the code of uz.md and ru.md must
// be the same apart from comments and string literals, which is checked too.
import { execFileSync } from 'node:child_process'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'

const IMAGES = {
  python: { image: 'python:3.11-slim', file: 'main.py', cmd: 'python3 main.py' },
  java: { image: 'eclipse-temurin:21-jdk', file: 'Main.java', cmd: 'javac Main.java && java Main' },
  cpp: { image: 'gcc:13', file: 'main.cpp', cmd: 'g++ -O2 -std=c++17 -o main main.cpp && ./main' },
}

function examples(markdown) {
  const groups = []
  let current = null
  let lastFenceEnd = -1
  const lines = markdown.split('\n')
  for (let i = 0; i < lines.length; i++) {
    const open = /^```(python|java|cpp)\s*$/.exec(lines[i])
    if (!open) continue
    let j = i + 1
    while (j < lines.length && lines[j] !== '```') j++
    const code = lines.slice(i + 1, j).join('\n') + '\n'
    if (!current || lastFenceEnd !== i - 1) {
      current = {}
      groups.push(current)
    }
    current[open[1]] = code
    lastFenceEnd = j
    i = j
  }
  return groups
}

/** The code without comments, string literals and blank lines: what a translation must not change. */
function skeleton(code) {
  return code
    .replace(/"(?:[^"\\\n]|\\.)*"/g, '""')
    .replace(/(\/\/|#(?!include)).*$/gm, '')
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .split('\n')
    .map((line) => line.trimEnd())
    .filter(Boolean)
    .join('\n')
}

function run(lang, code, input) {
  const { image, file, cmd } = IMAGES[lang]
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-ex-'))
  fs.writeFileSync(path.join(dir, file), code)
  try {
    return execFileSync(
      'docker',
      ['run', '--rm', '-i', '--network', 'none', '-v', `${dir}:/w`, '-w', '/w', image, 'sh', '-c', cmd],
      { input, encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'], timeout: 120_000 },
    )
  } catch (error) {
    return `<<failed: ${(error.stderr || error.message).trim()}>>`
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
}

function checkLesson(dir) {
  const name = path.basename(dir)
  const groups = examples(fs.readFileSync(path.join(dir, 'en.md'), 'utf8'))
  const specFile = path.join(dir, 'examples.json')
  if (groups.length === 0) return 0
  if (!fs.existsSync(specFile)) {
    console.log(`${name}: ${groups.length} example(s), no examples.json — not checked`)
    return 1
  }
  const spec = JSON.parse(fs.readFileSync(specFile, 'utf8'))
  if (spec.length !== groups.length) {
    console.log(`${name}: examples.json has ${spec.length} entries, the lesson has ${groups.length} examples`)
    return 1
  }
  let failures = 0
  for (const locale of ['uz', 'ru']) {
    const translated = examples(fs.readFileSync(path.join(dir, `${locale}.md`), 'utf8'))
    groups.forEach((group, index) => {
      for (const [lang, code] of Object.entries(group)) {
        if (skeleton(translated[index]?.[lang] ?? '') !== skeleton(code)) {
          failures++
          console.log(`${name} #${index + 1} ${lang}: the ${locale}.md code differs from en.md beyond comments and strings`)
        }
      }
    })
  }
  groups.forEach((group, index) => {
    const entry = spec[index]
    if (entry.skip) {
      console.log(`${name} #${index + 1}: skipped (${entry.skip})`)
      return
    }
    for (const [lang, code] of Object.entries(group)) {
      for (const { input, output } of entry.runs) {
        const actual = run(lang, code, input).replace(/\r\n/g, '\n')
        const ok = actual === output
        if (!ok) failures++
        console.log(`${name} #${index + 1} ${lang.padEnd(6)} ${ok ? 'ok' : `FAIL\n  expected ${JSON.stringify(output)}\n  actual   ${JSON.stringify(actual)}`}`)
      }
    }
  })
  return failures
}

const [target, ...rest] = process.argv.slice(2)
if (!target) {
  console.error('usage: node run-examples.mjs <course folder> [--lesson CODE]')
  process.exit(2)
}
const only = rest.includes('--lesson') ? rest[rest.indexOf('--lesson') + 1] : null
const lessons = fs
  .readdirSync(target, { withFileTypes: true })
  .filter((entry) => entry.isDirectory() && entry.name !== 'node_modules' && (!only || entry.name === only))
  .map((entry) => path.join(target, entry.name))
const failures = lessons.reduce((sum, dir) => sum + checkLesson(dir), 0)
process.exit(failures === 0 ? 0 : 1)
