// Generates backend/api/src/main/resources/db/seed/V906__dev_seed_practice_content.sql from the
// content modules in ./content, and V907__dev_seed_cpp_solutions.sql from ./content/_cpp.mjs.
// Run: node tools/content-seed/generate.mjs
//
// Why a generator: the lesson material, coding tasks and quizzes are easier to write and review
// as data than as hand-escaped SQL, while the platform's dev data stays reproducible through
// Flyway like V901–V905. Text goes into Postgres dollar-quoted literals, so HTML and code need
// no escaping.
//
// Every generated statement is guarded, so V906 is safe on databases that already carry
// content (e.g. one where V903 ran): lessons that already have blocks only receive blocks
// declared with `append`, and those are skipped when an equal block is already there.
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const out = path.resolve(here, '../../backend/api/src/main/resources/db/seed/V906__dev_seed_practice_content.sql')

const modules = ['das1110', 'mta1204', 'kia1406', 'kot1306', 'iaktt1105']
const subjects = []
for (const m of modules) {
  const mod = (await import(`./content/${m}.mjs`)).default
  subjects.push(...(Array.isArray(mod) ? mod : [mod]))
}

// ---------------------------------------------------------------------------- quoting helpers
let tagSeq = 0
function dollarQuote(s) {
  let tag
  do tag = `q${(tagSeq++).toString(36)}`
  while (s.includes(`$${tag}$`))
  return `$${tag}$${s}$${tag}$`
}
// Flyway substitutes `${name}` placeholders anywhere in the script text, so no `{` may follow a
// `$` — neither inside the text nor right after an opening `$tag$`. Such braces become chr(123).
function dq(text) {
  if (text == null) return 'NULL'
  const pieces = String(text).split(/(?<=^|\$)\{/)
  if (pieces.length === 1) return dollarQuote(pieces[0])
  return `(${pieces.map((p) => (p ? dollarQuote(p) : "''")).join(' || chr(123) || ')})`
}
const plain = (html) =>
  html
    .replace(/<[^>]+>/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/\s+/g, ' ')
    .trim()
const jsonb = (obj) => `${dq(JSON.stringify(obj))}::jsonb`

const NOTE_HTML =
  '<p>Ushbu material platforma tomonidan sillabusdagi mavzu asosida tayyorlangan oʻquv yordamidir. ' +
  'Rasmiy maʼruza matni va baholash talablarini oʻqituvchi belgilaydi.</p>'

// ---------------------------------------------------------------------------- SQL builders
function courseSel(s) {
  return `SELECT c.id FROM courses c JOIN subjects s ON s.id=c.subject_id WHERE s.code=${dq(s.subject)} AND c.cohort_code=${dq(s.cohort)}`
}

function taskSql(s, t) {
  const lines = []
  const cid = `(${courseSel(s)})`
  lines.push(`-- task ${t.code}`)
  lines.push(`INSERT INTO coding_tasks (course_id, code, difficulty, status, max_points, scoring_strategy, max_attempts, cpu_time_limit_ms, wall_time_limit_ms, memory_limit_kb, published_at)
SELECT ${cid}, ${dq(t.code)}, ${dq(t.difficulty ?? 'EASY')}, 'PUBLISHED', 100, 'PER_TEST_WEIGHTED', 20, 2000, 10000, 262144, now()
WHERE ${cid} IS NOT NULL AND NOT EXISTS (SELECT 1 FROM coding_tasks WHERE code=${dq(t.code)});`)
  lines.push(`INSERT INTO coding_task_translations (coding_task_id, locale, title, description, instructions, constraints_text)
SELECT id, 'uz', ${dq(t.title)}, ${dq(t.description)}, ${dq(t.instructions ?? null)}, ${dq(t.constraints ?? null)}
FROM coding_tasks WHERE code=${dq(t.code)} ON CONFLICT DO NOTHING;`)
  for (const lang of ['python', 'java']) {
    if (!t[lang]) throw new Error(`${t.code}: missing ${lang}`)
    lines.push(`INSERT INTO coding_task_languages (coding_task_id, language_code, starter_code, solution_code)
SELECT id, ${dq(lang)}, ${dq(t[lang].starter)}, ${dq(t[lang].solution)} FROM coding_tasks WHERE code=${dq(t.code)}
ON CONFLICT DO NOTHING;`)
  }
  t.tests.forEach((tc, i) => {
    const [stdin, expected, opts = {}] = tc
    const example = opts.example ?? i < 2
    const hidden = opts.hidden ?? !example
    lines.push(`INSERT INTO coding_task_test_cases (coding_task_id, position, name, visibility, is_example, weight, stdin, expected_stdout, comparison_mode)
SELECT id, ${i}, ${dq(opts.name ?? (example ? `Namuna ${i + 1}` : `Test ${i + 1}`))}, ${dq(hidden ? 'HIDDEN' : 'VISIBLE')}, ${example}, ${opts.weight ?? 1}, ${dq(stdin)}, ${dq(expected)}, ${dq(opts.mode ?? 'TRIMMED')}
FROM coding_tasks t WHERE t.code=${dq(t.code)} AND NOT EXISTS (SELECT 1 FROM coding_task_test_cases x WHERE x.coding_task_id=t.id AND x.position=${i});`)
  })
  return lines.join('\n')
}

function quizSql(s, q) {
  const lines = []
  const cid = `(${courseSel(s)})`
  const total = q.questions.reduce((sum, x) => sum + (x.points ?? 1), 0)
  lines.push(`-- quiz ${q.code}`)
  lines.push(`INSERT INTO quizzes (course_id, code, status, max_points, passing_percent, time_limit_seconds, max_attempts, shuffle_questions, shuffle_options, reveal_policy, published_at)
SELECT ${cid}, ${dq(q.code)}, 'PUBLISHED', ${total}, 60, ${q.timeLimit ?? 900}, 3, false, true, 'AFTER_ATTEMPT', now()
WHERE ${cid} IS NOT NULL AND NOT EXISTS (SELECT 1 FROM quizzes WHERE code=${dq(q.code)});`)
  lines.push(`INSERT INTO quiz_translations (quiz_id, locale, title, description, instructions)
SELECT id, 'uz', ${dq(q.title)}, ${dq(q.description ?? null)}, ${dq(q.instructions ?? 'Har bir savolni diqqat bilan oʻqing. Bir nechta javobli savollarda barcha toʻgʻri variantlarni belgilang.')}
FROM quizzes WHERE code=${dq(q.code)} ON CONFLICT DO NOTHING;`)
  q.questions.forEach((x, i) => {
    const type = { SINGLE: 'SINGLE_CHOICE', MULTI: 'MULTIPLE_CHOICE', TF: 'TRUE_FALSE', SHORT: 'SHORT_ANSWER', NUM: 'NUMERIC' }[x.type]
    if (!type) throw new Error(`${q.code}: bad type ${x.type}`)
    lines.push(`INSERT INTO questions (quiz_id, position, question_type, points, difficulty, settings)
SELECT q.id, ${i}, ${dq(type)}, ${x.points ?? 1}, ${dq(x.difficulty ?? 'EASY')}, ${jsonb(x.type === 'MULTI' ? { partialCredit: true } : {})}
FROM quizzes q WHERE q.code=${dq(q.code)} AND NOT EXISTS (SELECT 1 FROM questions z WHERE z.quiz_id=q.id AND z.position=${i});`)
    const qsel = `SELECT z.id FROM questions z JOIN quizzes q ON q.id=z.quiz_id WHERE q.code=${dq(q.code)} AND z.position=${i}`
    lines.push(`INSERT INTO question_translations (question_id, locale, prompt, explanation)
SELECT (${qsel}), 'uz', ${dq(x.prompt)}, ${dq(x.explanation ?? null)} ON CONFLICT DO NOTHING;`)
    let options = x.options
    let correct = x.correct
    if (x.type === 'TF') {
      options = ['Toʻgʻri', 'Notoʻgʻri']
      correct = [x.answer ? 0 : 1]
    }
    if (options) {
      options.forEach((text, j) => {
        lines.push(`INSERT INTO answer_options (question_id, position, is_correct)
SELECT (${qsel}), ${j}, ${correct.includes(j)} WHERE NOT EXISTS (SELECT 1 FROM answer_options o WHERE o.question_id=(${qsel}) AND o.position=${j});`)
        lines.push(`INSERT INTO answer_option_translations (option_id, locale, text)
SELECT o.id, 'uz', ${dq(text)} FROM answer_options o WHERE o.question_id=(${qsel}) AND o.position=${j} ON CONFLICT DO NOTHING;`)
      })
    }
    if (x.type === 'SHORT' || x.type === 'NUM') {
      const answers = x.type === 'NUM' ? [String(x.answer)] : x.answers
      for (const a of answers) {
        lines.push(`INSERT INTO question_answer_keys (question_id, match_mode, expected_value, tolerance)
SELECT (${qsel}), ${dq(x.type === 'NUM' ? 'NUMERIC_TOLERANCE' : (x.mode ?? 'CASE_INSENSITIVE'))}, ${dq(a)}, ${x.type === 'NUM' ? (x.tolerance ?? 0) : 'NULL'}
WHERE NOT EXISTS (SELECT 1 FROM question_answer_keys k WHERE k.question_id=(${qsel}) AND k.expected_value=${dq(a)});`)
      }
    }
  })
  return lines.join('\n')
}

/** One content block → [blockType, settings SQL expr, payload object, guard SQL (append only)]. */
function blockParts(b) {
  switch (b.type) {
    case 'TEXT':
      return ['TEXT', jsonb({}), { html: b.html }]
    case 'CALLOUT':
      return ['CALLOUT', jsonb({ variant: b.variant }), { html: b.html }]
    case 'CODE':
      return ['CODE_EXAMPLE', jsonb({ language: b.lang }), { code: b.code, explanation: b.explanation ?? null }]
    case 'LINK':
      return ['LINK', jsonb({ url: b.url }), { label: b.label }]
    case 'TASK':
      return ['CODING_TASK_REF', `jsonb_build_object('refId', (SELECT id FROM coding_tasks WHERE code=${dq(b.ref)})::text)`, {}]
    case 'QUIZ':
      return ['QUIZ_REF', `jsonb_build_object('refId', (SELECT id FROM quizzes WHERE code=${dq(b.ref)})::text)`, {}]
    default:
      throw new Error(`unknown block ${b.type}`)
  }
}

function lessonSql(s, code, lesson) {
  const append = Boolean(lesson.append)
  const blocks = append ? lesson.blocks : [...lesson.blocks, { type: 'CALLOUT', variant: 'INFO', html: NOTE_HTML }]
  const body = []
  body.push(`  SELECT l.id INTO v_lesson FROM lessons l
    JOIN course_modules m ON m.id=l.module_id JOIN courses c ON c.id=m.course_id JOIN subjects s ON s.id=c.subject_id
   WHERE s.code=${dq(s.subject)} AND c.cohort_code=${dq(s.cohort)} AND l.code=${dq(code)};
  IF v_lesson IS NULL THEN RAISE NOTICE 'V906: lesson not found % %', ${dq(s.subject)}, ${dq(code)}; RETURN; END IF;`)
  if (!append) body.push(`  IF EXISTS (SELECT 1 FROM content_blocks WHERE lesson_id=v_lesson) THEN RETURN; END IF;`)
  body.push(`  SELECT coalesce(max(position) + 1, 0) INTO v_pos FROM content_blocks WHERE lesson_id=v_lesson;`)
  for (const b of blocks) {
    const [type, settings, payload] = blockParts(b)
    const text = plain(payload.html ?? payload.explanation ?? payload.label ?? '') || null
    const guard =
      type === 'CODING_TASK_REF' || type === 'QUIZ_REF'
        ? `NOT EXISTS (SELECT 1 FROM content_blocks x WHERE x.lesson_id=v_lesson AND x.block_type=${dq(type)} AND x.settings->>'refId'=(${settings})->>'refId') AND (${settings})->>'refId' IS NOT NULL`
        : type === 'CODE_EXAMPLE'
          ? `NOT EXISTS (SELECT 1 FROM content_blocks x WHERE x.lesson_id=v_lesson AND x.block_type='CODE_EXAMPLE' AND x.settings->>'language'=${dq(b.lang)})`
          : 'true'
    body.push(`  IF ${append ? guard : 'true'} THEN
    INSERT INTO content_blocks (lesson_id, position, block_type, is_required, settings)
    VALUES (v_lesson, v_pos, ${dq(type)}, true, ${settings}) RETURNING id INTO v_block;
    INSERT INTO content_block_translations (block_id, locale, payload, plain_text)
    VALUES (v_block, 'uz', ${jsonb(Object.fromEntries(Object.entries(payload).filter(([, v]) => v != null)))}, ${dq(text)});
    v_pos := v_pos + 1;
  END IF;`)
  }
  return `-- lesson ${s.subject} ${s.cohort} ${code}${append ? ' (append)' : ''}
DO $do$
DECLARE v_lesson uuid; v_block uuid; v_pos int;
BEGIN
${body.join('\n')}
END
$do$;`
}

// ---------------------------------------------------------------------------- assemble
const parts = [
  `-- ============================================================================
-- V906 (DEV ONLY) — Study material, coding tasks (Python + Java) and quizzes for every
-- syllabus topic of DAS1110, MTA1204, KIA1406, KOT1306 and IAKTT1105.
--
-- GENERATED by tools/content-seed/generate.mjs from tools/content-seed/content/*.mjs.
-- Do not edit by hand: change the content modules and regenerate.
--
-- ⚠️  PLATFORM-WRITTEN LEARNING SUPPORT, NOT AN AIFU ACADEMIC DELIVERABLE.
-- Lesson titles, hours and outcomes come from the syllabi (V901, V904); the explanations,
-- examples, practice tasks and quizzes here are written for the platform so every topic has
-- material to study and practise. Each generated lesson ends with a note saying so.
-- Official lecture text and grading rules remain the teacher's.
--
-- Coding tasks accept Python and Java (V018). Solutions and tests are checked by
-- tools/content-seed/verify-solutions.mjs before this file is regenerated.
-- ============================================================================`,
]

let stats = { tasks: 0, quizzes: 0, lessons: 0 }
for (const s of subjects) {
  parts.push(`\n-- ======================= ${s.subject} (${s.cohort}) =======================`)
  for (const t of s.tasks ?? []) {
    parts.push(taskSql(s, t))
    stats.tasks++
  }
  for (const extra of s.addJavaTo ?? []) {
    parts.push(`INSERT INTO coding_task_languages (coding_task_id, language_code, starter_code, solution_code)
SELECT id, 'java', ${dq(extra.starter)}, ${dq(extra.solution)} FROM coding_tasks WHERE code=${dq(extra.code)}
ON CONFLICT DO NOTHING;`)
  }
  for (const q of s.quizzes ?? []) {
    parts.push(quizSql(s, q))
    stats.quizzes++
  }
  for (const [code, lesson] of Object.entries(s.lessons ?? {})) {
    parts.push(lessonSql(s, code, lesson))
    stats.lessons++
  }
}

fs.writeFileSync(out, parts.join('\n\n') + '\n')
console.log(`wrote ${path.relative(process.cwd(), out)}: ${stats.lessons} lessons, ${stats.tasks} tasks, ${stats.quizzes} quizzes`)

// ---------------------------------------------------------------------------- C++ (V907)
// C++ became the third teaching language after V906 had shipped. V906 must stay byte-identical
// (Flyway checksums), so the C++ rows go into their own script.
const cpp = (await import('./content/_cpp.mjs')).default
const taskCodes = subjects.flatMap((s) => [...(s.tasks ?? []), ...(s.addJavaTo ?? [])].map((t) => t.code))
const missing = taskCodes.filter((code) => !cpp[code])
const unknown = Object.keys(cpp).filter((code) => !taskCodes.includes(code))
if (missing.length || unknown.length) {
  throw new Error(`_cpp.mjs: missing ${JSON.stringify(missing)}, unknown ${JSON.stringify(unknown)}`)
}
const cppOut = path.resolve(here, '../../backend/api/src/main/resources/db/seed/V907__dev_seed_cpp_solutions.sql')
const cppParts = [
  `-- ============================================================================
-- V907 (DEV ONLY) — C++ starter code and reference solutions for every coding task of V902
-- and V906, so each task is solvable in Python, Java and C++ (product spec S-05.2).
--
-- GENERATED by tools/content-seed/generate.mjs from tools/content-seed/content/_cpp.mjs.
-- Do not edit by hand: change the content module and regenerate.
--
-- Solutions are checked against the tasks' tests by tools/content-seed/verify-solutions.mjs
-- (g++ -std=c++17 -O2 in gcc:13, the sandbox's own image and flags).
-- ============================================================================`,
]
for (const code of taskCodes) {
  cppParts.push(`INSERT INTO coding_task_languages (coding_task_id, language_code, starter_code, solution_code)
SELECT id, 'cpp', ${dq(cpp[code].starter)}, ${dq(cpp[code].solution)} FROM coding_tasks WHERE code=${dq(code)}
ON CONFLICT DO NOTHING;`)
}
cppParts.push(`-- The first fixture task predates the language decision: it offered Go, which is no longer a
-- teaching language (V023 disables it).
UPDATE activity_translations SET instructions = 'Yechimni Python, Java yoki C++ tilida topshiring.'
WHERE instructions = 'Yechimni Python yoki Go tilida topshiring.';`)
fs.writeFileSync(cppOut, cppParts.join('\n\n') + '\n')
console.log(`wrote ${path.relative(process.cwd(), cppOut)}: ${taskCodes.length} C++ solutions`)
