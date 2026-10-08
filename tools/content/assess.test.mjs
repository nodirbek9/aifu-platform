// Tests for the offline assessment checker (docs/PLAN.md §4, phase 6 C0).
// Each rule is proven to fail on a fixture that breaks only that rule, and one fully
// valid assess.json / midterm.json is proven to pass.
import assert from 'node:assert/strict'
import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { test } from 'node:test'
import { validateAssess, validateMidterm } from './assess-schema.mjs'
import { checkCourse } from './assess.mjs'

const tr = (uz = 'Savol matni', ru = 'Текст вопроса', en = 'Question text') => ({ uz, ru, en })

const option = (text) => tr(text, 'Вариант', 'Option')

function validQuestion(overrides = {}) {
  return {
    type: 'SINGLE',
    difficulty: 'EASY',
    points: 1,
    prompt: tr(),
    options: [option('Birinchi'), option('Ikkinchi'), option('Uchinchi')],
    correct: [1],
    explanation: tr('Izoh', 'Пояснение', 'Explanation'),
    ...overrides,
  }
}

/** 9 questions: 3 EASY, 3 MEDIUM, 3 HARD, mixing all five types. */
function validQuestions() {
  return [
    validQuestion({ difficulty: 'EASY' }),
    validQuestion({ difficulty: 'EASY', type: 'MULTI', correct: [0, 2] }),
    validQuestion({ difficulty: 'EASY', type: 'TF', options: [option('Toʻgʻri'), option('Notoʻgʻri')], correct: [0] }),
    validQuestion({ difficulty: 'MEDIUM', type: 'SHORT', answers: ['massiv'], options: undefined, correct: undefined }),
    validQuestion({ difficulty: 'MEDIUM', type: 'NUM', answers: [3.14], tolerance: 0.01, options: undefined, correct: undefined }),
    validQuestion({ difficulty: 'MEDIUM' }),
    validQuestion({ difficulty: 'HARD' }),
    validQuestion({ difficulty: 'HARD', type: 'MULTI', correct: [1] }),
    validQuestion({ difficulty: 'HARD', type: 'SHORT', answers: ['sikl', 'takrorlash'], options: undefined, correct: undefined }),
  ]
}

const STARTER = {
  python: 'n = int(input())\nprint(n)\n',
  java: 'import java.util.Scanner;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner in = new Scanner(System.in);\n    System.out.println(in.nextInt());\n  }\n}\n',
  cpp: '#include <iostream>\nint main() {\n  int n;\n  std::cin >> n;\n  std::cout << n << std::endl;\n  return 0;\n}\n',
}

function validCodePractice() {
  return {
    kind: 'CODE',
    title: tr('Mashq', 'Задание', 'Practice'),
    statement: tr('Shart', 'Условие', 'Statement'),
    code: {
      starter: { ...STARTER },
      solution: { ...STARTER },
      tests: [
        { input: '1\n', expected: '1\n', hidden: false },
        { input: '2\n', expected: '2\n', hidden: true },
        { input: '42\n', expected: '42\n', hidden: true },
      ],
    },
  }
}

function validAssess() {
  return { test: { questions: validQuestions() }, practice: validCodePractice() }
}

/** Strips a key set to undefined in fixtures (JSON.stringify drops undefined, validate does not). */
const clean = (value) => JSON.parse(JSON.stringify(value))

/** Applies a mutation to a valid assess.json and returns the problems it must produce. */
function problemsOf(mutate) {
  const data = clean(validAssess())
  mutate(data)
  return validateAssess(data)
}

const hasProblem = (problems, fragment) =>
  assert.ok(
    problems.some((problem) => problem.includes(fragment)),
    `expected a problem containing ${JSON.stringify(fragment)}, got:\n${problems.join('\n')}`,
  )

test('a fully valid assess.json passes', () => {
  assert.deepEqual(validateAssess(clean(validAssess())), [])
})

test('a valid ANSWER practice passes', () => {
  const data = clean(validAssess())
  data.practice = {
    kind: 'ANSWER',
    title: tr('Savollar', 'Вопросы', 'Questions'),
    statement: tr('Shart', 'Условие', 'Statement'),
    answer: { questions: [validQuestion(), validQuestion({ type: 'SHORT', answers: ['javob'], options: undefined, correct: undefined })] },
  }
  assert.deepEqual(validateAssess(clean(data)), [])
})

test('test bank: fewer than 8 questions fails', () => {
  const problems = problemsOf((data) => {
    data.test.questions = data.test.questions.slice(0, 7)
  })
  hasProblem(problems, 'need 8–10')
})

test('test bank: more than 10 questions fails', () => {
  const problems = problemsOf((data) => {
    for (let i = 0; i < 2; i += 1) data.test.questions.push(validQuestion())
  })
  hasProblem(problems, 'need 8–10')
})

test('test bank: fewer than 2 questions per difficulty band fails', () => {
  const problems = problemsOf((data) => {
    data.test.questions = data.test.questions.filter((q) => q.difficulty !== 'HARD')
    data.test.questions.push(validQuestion({ difficulty: 'EASY' }))
  })
  hasProblem(problems, 'only 0 HARD question(s)')
})

test('locales: an empty prompt locale fails', () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].prompt.ru = '   '
  })
  hasProblem(problems, 'test.questions[0].prompt.ru: empty or missing')
})

test('locales: a missing explanation locale fails', () => {
  const problems = problemsOf((data) => {
    delete data.test.questions[0].explanation.en
  })
  hasProblem(problems, 'test.questions[0].explanation.en: empty or missing')
})

test("uz orthography: ASCII apostrophe after o/g fails (must be ʻ U+02BB)", () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].prompt.uz = "O'zbekiston qayerda joylashgan?"
  })
  hasProblem(problems, 'test.questions[0].prompt.uz: ASCII apostrophe')
})

test('uz orthography: ASCII apostrophe between other letters fails (must be ʼ U+02BC)', () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].explanation.uz = "Bu ma'lumot muhim."
  })
  hasProblem(problems, 'test.questions[0].explanation.uz: ASCII apostrophe')
})

test('uz orthography: ʻ and ʼ pass', () => {
  const data = clean(validAssess())
  data.test.questions[0].prompt.uz = 'Oʻzbekiston maʼlumoti toʻgʻrimi?'
  assert.deepEqual(validateAssess(data), [])
})

test('SINGLE: options are required', () => {
  const problems = problemsOf((data) => {
    delete data.test.questions[0].options
  })
  hasProblem(problems, 'options: SINGLE needs at least 2 options')
})

test('TF: exactly 2 options are required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[2].options.push(option('Uchinchi'))
  })
  hasProblem(problems, 'options: TF needs exactly 2 options')
})

test('SINGLE: exactly 1 correct index is required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].correct = [0, 1]
  })
  hasProblem(problems, 'correct: SINGLE needs exactly 1 index')
})

test('MULTI: at least 1 correct index is required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[1].correct = []
  })
  hasProblem(problems, 'correct: must be a non-empty array')
})

test('correct indices must be in range', () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].correct = [3]
  })
  hasProblem(problems, 'index 3 out of range (0..2)')
})

test('SHORT: a non-empty answers list is required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[3].answers = []
  })
  hasProblem(problems, 'answers: SHORT needs at least one non-empty accepted answer')
})

test('NUM: a numeric first answer is required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[4].answers = ['3.14']
  })
  hasProblem(problems, 'answers: NUM needs a numeric first answer')
})

test('NUM: a tolerance >= 0 is required', () => {
  const problems = problemsOf((data) => {
    data.test.questions[4].tolerance = -0.1
  })
  hasProblem(problems, 'tolerance: NUM needs a numeric tolerance >= 0')
})

test('unknown question type fails', () => {
  const problems = problemsOf((data) => {
    data.test.questions[0].type = 'ESSAY'
  })
  hasProblem(problems, 'type: must be one of SINGLE|MULTI|TF|SHORT|NUM')
})

test('CODE practice: at least 3 tests are required', () => {
  const problems = problemsOf((data) => {
    data.practice.code.tests = data.practice.code.tests.slice(0, 2)
  })
  hasProblem(problems, '2 test(s), need at least 3')
})

test('CODE practice: at least 2 hidden tests are required', () => {
  const problems = problemsOf((data) => {
    data.practice.code.tests[2].hidden = false
  })
  hasProblem(problems, 'only 1 hidden, need at least 2')
})

test('CODE practice: all three languages are required for starter and solution', () => {
  const problems = problemsOf((data) => {
    delete data.practice.code.starter.cpp
    delete data.practice.code.solution.java
  })
  hasProblem(problems, 'code.starter.cpp: empty or missing')
  hasProblem(problems, 'code.solution.java: empty or missing')
})

test('CODE practice: starter must not contain comments', () => {
  const problems = problemsOf((data) => {
    data.practice.code.starter.python = '# oqiladigan son\nn = int(input())\nprint(n)\n'
    data.practice.code.starter.java = `// read a number\n${STARTER.java}`
  })
  hasProblem(problems, 'code.starter.python: starter code must not contain natural-language comments')
  hasProblem(problems, 'code.starter.java: starter code must not contain natural-language comments')
})

test('CODE practice: a // inside a string literal is not a comment', () => {
  const data = clean(validAssess())
  data.practice.code.starter.python = 'print("https://example.uz")\n'
  data.practice.code.solution.python = 'print("https://example.uz")\n'
  assert.deepEqual(validateAssess(data), [])
})

test('CODE practice: starter must be TODO-free', () => {
  const problems = problemsOf((data) => {
    data.practice.code.starter.cpp = 'int main() {\n  TODO\n}\n'
  })
  hasProblem(problems, 'code.starter.cpp: starter code must be TODO-free')
})

test('ANSWER practice: answer.questions is required', () => {
  const problems = problemsOf((data) => {
    data.practice = { kind: 'ANSWER', title: tr(), statement: tr() }
  })
  hasProblem(problems, 'answer.questions: required for kind ANSWER')
})

test('midterm: a valid midterm.json passes', () => {
  const questions = Array.from({ length: 60 }, (_, i) => validQuestion({ lesson: `M${(i % 9) + 1}` }))
  assert.deepEqual(validateMidterm(clean({ afterLesson: 'M14', questions })), [])
})

test('midterm: fewer than 60 questions fails', () => {
  const questions = Array.from({ length: 59 }, () => validQuestion({ lesson: 'M1' }))
  const problems = validateMidterm(clean({ afterLesson: 'M14', questions }))
  hasProblem(problems, '59 question(s), need at least 60')
})

test('midterm: every question needs a lesson code', () => {
  const questions = Array.from({ length: 60 }, () => validQuestion())
  const problems = validateMidterm(clean({ afterLesson: 'M14', questions }))
  hasProblem(problems, 'questions[0].lesson: missing')
})

test('midterm: afterLesson is required', () => {
  const questions = Array.from({ length: 60 }, () => validQuestion({ lesson: 'M1' }))
  const problems = validateMidterm(clean({ questions }))
  hasProblem(problems, 'afterLesson: missing')
})

test('checkCourse: per-lesson results, missing folders counted, midterm checked', () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-'))
  try {
    const good = path.join(dir, 'M1')
    const bad = path.join(dir, 'M2')
    const empty = path.join(dir, 'M3')
    fs.mkdirSync(good)
    fs.mkdirSync(bad)
    fs.mkdirSync(empty)
    fs.writeFileSync(path.join(good, 'assess.json'), JSON.stringify(validAssess()))
    const broken = clean(validAssess())
    broken.test.questions = broken.test.questions.slice(0, 5)
    fs.writeFileSync(path.join(bad, 'assess.json'), JSON.stringify(broken))
    fs.writeFileSync(path.join(dir, 'midterm.json'), JSON.stringify({ afterLesson: 'M3', questions: Array.from({ length: 60 }, () => validQuestion({ lesson: 'M1' })) }))

    const { results, missing, midterm } = checkCourse(dir)
    assert.equal(missing, 1)
    assert.equal(results.length, 2)
    const byName = new Map(results.map((result) => [result.name, result.problems]))
    assert.deepEqual(byName.get('M1'), [])
    assert.ok(byName.get('M2').length > 0)
    assert.deepEqual(midterm.problems, [])
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('checkCourse: --lesson targets one folder and reports a missing assess.json as a problem', () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-'))
  try {
    fs.mkdirSync(path.join(dir, 'M1'))
    fs.mkdirSync(path.join(dir, 'M2'))
    fs.writeFileSync(path.join(dir, 'M1', 'assess.json'), JSON.stringify(validAssess()))

    const single = checkCourse(dir, { lesson: 'M1' })
    assert.equal(single.results.length, 1)
    assert.deepEqual(single.results[0].problems, [])
    assert.equal(single.midterm, null)

    const absent = checkCourse(dir, { lesson: 'M2' })
    assert.deepEqual(absent.results[0].problems, ['assess.json not found'])
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})

test('checkCourse: invalid JSON in assess.json is reported, not thrown', () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'aifu-assess-'))
  try {
    fs.mkdirSync(path.join(dir, 'M1'))
    fs.writeFileSync(path.join(dir, 'M1', 'assess.json'), '{ not json')
    const { results } = checkCourse(dir)
    hasProblem(results[0].problems, 'invalid JSON')
  } finally {
    fs.rmSync(dir, { recursive: true, force: true })
  }
})
