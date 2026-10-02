// Runs every coding task's reference solutions (Python and Java) against its test cases, in
// the same images the docker-run sandbox uses, and reports any mismatch.
//   node tools/content-seed/verify-solutions.mjs [TASK-CODE-PREFIX]
// Needs Docker. One container per language runs all tasks, so a full check takes ~1-2 min.
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { execFileSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const prefix = process.argv[2] ?? ''
const tasks = []
for (const m of ['das1110', 'mta1204', 'kia1406', 'kot1306', 'iaktt1105']) {
  const mod = (await import(`./content/${m}.mjs`)).default
  for (const s of Array.isArray(mod) ? mod : [mod]) {
    for (const t of s.tasks ?? []) if (t.code.startsWith(prefix)) tasks.push(t)
    for (const extra of s.addJavaTo ?? []) if (extra.code.startsWith(prefix) && extra.tests) tasks.push({ code: extra.code, java: extra, tests: extra.tests })
  }
}

const work = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-verify-'))
for (const t of tasks) {
  const dir = path.join(work, t.code)
  fs.mkdirSync(dir)
  if (t.python) fs.writeFileSync(path.join(dir, 'sol.py'), t.python.solution)
  if (t.java) fs.writeFileSync(path.join(dir, 'Main.java'), t.java.solution)
  t.tests.forEach(([stdin, expected], i) => {
    fs.writeFileSync(path.join(dir, `${i}.in`), stdin)
    fs.writeFileSync(path.join(dir, `${i}.expected`), expected)
  })
}

const loop = (run) => `for d in /w/*/; do cd "$d"; ${run}; done; true`
const pyScript = loop('[ -f sol.py ] && for f in *.in; do timeout 10 python3 sol.py < "$f" > "${f%.in}.py.out" 2>"${f%.in}.py.err"; done')
const javaScript = loop(
  '[ -f Main.java ] && mkdir -p cls && javac -encoding UTF-8 -d cls Main.java 2> javac.err && for f in *.in; do timeout 10 java -Xss16m -cp cls Main < "$f" > "${f%.in}.java.out" 2>"${f%.in}.java.err"; done',
)
const mount = `${work.replace(/\\/g, '/')}:/w`
const env = { ...process.env, MSYS_NO_PATHCONV: '1' }
execFileSync('docker', ['run', '--rm', '--network=none', '-v', mount, 'python:3.11-slim', 'sh', '-c', pyScript], { stdio: 'inherit', env })
execFileSync('docker', ['run', '--rm', '--network=none', '-v', mount, 'eclipse-temurin:21-jdk', 'sh', '-c', javaScript], { stdio: 'inherit', env })

const norm = (s) => s.replace(/\r\n/g, '\n').trim()
let failures = 0
for (const t of tasks) {
  const dir = path.join(work, t.code)
  const javacErr = path.join(dir, 'javac.err')
  if (fs.existsSync(javacErr) && fs.readFileSync(javacErr, 'utf8').trim()) {
    failures++
    console.log(`FAIL ${t.code} java: compile error\n${fs.readFileSync(javacErr, 'utf8')}`)
  }
  t.tests.forEach(([, expected], i) => {
    for (const lang of ['py', 'java']) {
      const outFile = path.join(dir, `${i}.${lang}.out`)
      if (!fs.existsSync(outFile)) continue
      const got = norm(fs.readFileSync(outFile, 'utf8'))
      if (got !== norm(expected)) {
        failures++
        const err = fs.readFileSync(path.join(dir, `${i}.${lang}.err`), 'utf8').trim().split('\n').slice(-3).join(' | ')
        console.log(`FAIL ${t.code} ${lang} test ${i}: expected ${JSON.stringify(norm(expected)).slice(0, 120)} got ${JSON.stringify(got).slice(0, 120)}${err ? `  stderr: ${err}` : ''}`)
      }
    }
  })
}
console.log(`${tasks.length} tasks, ${tasks.reduce((n, t) => n + t.tests.length, 0)} tests, ${failures} failures`)
fs.rmSync(work, { recursive: true, force: true })
process.exit(failures ? 1 : 0)
