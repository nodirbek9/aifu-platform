import assert from 'node:assert/strict'
import fs from 'node:fs'
import { test } from 'node:test'
import { scaffold } from './scaffold.mjs'
import { validateManifest } from './scaffold-manifest.mjs'

const tr = (uz, ru = uz, en = uz) => ({ uz, ru, en })
function manifest() {
  return {
    subjectCode: 'TEST1000', termName: '1-semestr', title: 'Test course',
    subject: { names: tr('Test subject'), departmentCode: 'TEST', type: 'ELECTIVE' },
    course: { credits: 1, totalHours: 4, sessionAllocations: { LECTURE: 2, PRACTICAL: 2 } },
    assessmentComponents: [
      // Child precedes parent to exercise dependency ordering.
      { code: 'JN_P', kind: 'PRACTICAL_ACTIVITY', maxPoints: 30, parent: 'JN' },
      { code: 'JN', kind: 'CONTINUOUS', maxPoints: 30, minPassPercent: 60, blocksFinal: true },
      { code: 'ON', kind: 'MIDTERM', maxPoints: 30, minPassPercent: 60 },
      { code: 'YaN', kind: 'FINAL', maxPoints: 40 },
    ],
    modules: [
      { code: 'UNIT', titles: tr('Unit'), groupingSource: 'PLATFORM_INFERRED', lessons: [
        { code: 'M1', sessionType: 'LECTURE', hours: 2, titles: tr('Lecture') },
        { code: 'A1', sessionType: 'PRACTICAL', hours: 2, titles: tr('Practice') },
      ] },
    ],
  }
}

// In-memory HTTP-shaped API. It returns detached DTOs and omits nullable parents like Jackson.
class FakeApi {
  constructor({ pageSize = 100 } = {}) {
    this.calls = []
    this.pageSize = pageSize
    this.next = 0
    this.years = [{ id: 'year', code: '2026-2027' }]
    this.terms = [{ id: 'term', academicYearId: 'year', name: '1-semestr' }]
    this.departments = [{ id: 'department', code: 'TEST' }]
    this.subjects = []
    this.courses = []
    this.modules = []
    this.lessons = []
  }

  data() {
    return structuredClone({ subjects: this.subjects, courses: this.courses, modules: this.modules, lessons: this.lessons })
  }

  async call(method, url, body) {
    this.calls.push({ method, url, body: structuredClone(body) })
    return structuredClone(this.handle(method, url, body))
  }

  handle(method, url, body) {
    const { pathname: p, searchParams: q } = new URL(url, 'http://fake')
    const parts = p.split('/')
    const page = (items) => {
      const n = Number(q.get('page') ?? 0)
      const size = Math.min(Number(q.get('size') ?? 100), this.pageSize)
      return { content: items.slice(n * size, (n + 1) * size), hasNext: (n + 1) * size < items.length, totalPages: Math.ceil(items.length / size) }
    }
    if (method === 'GET') {
      if (p === '/admin/academic-years') return this.years
      if (p.endsWith('/terms')) return this.terms.filter((t) => t.academicYearId === parts[3])
      if (p === '/reference/departments') return this.departments
      if (p === '/admin/subjects') return page(this.subjects.filter((s) => s.code.includes(q.get('query'))))
      if (p === '/admin/courses') return page(this.courses.filter((c) => c.subjectCode.includes(q.get('query')) && c.termId === q.get('termId')))
      if (p.startsWith('/admin/subjects/')) return this.subjects.find((s) => s.id === parts[3])
      if (p.startsWith('/admin/courses/')) return this.courses.find((c) => c.id === parts[3])
      if (p.endsWith('/modules')) return page(this.modules.filter((m) => m.courseId === parts[3]).sort((a, b) => a.position - b.position))
      if (p.endsWith('/lessons')) return page(this.lessons.filter((l) => l.moduleId === parts[3]).sort((a, b) => a.position - b.position))
    }
    const create = (list, extra = {}) => {
      const made = { ...structuredClone(body), ...extra, id: `id-${++this.next}`, status: 'DRAFT', translations: [] }
      list.push(made)
      return made
    }
    if (method === 'POST') {
      if (p === '/admin/subjects') return create(this.subjects)
      if (p === '/admin/courses') {
        // Unlike the OpenAPI required list, the actual server requires totalHours.
        assert.ok(body.totalHours > 0)
        return create(this.courses, {
          subjectCode: this.subjects.find((s) => s.id === body.subjectId).code,
          termName: this.terms.find((t) => t.id === body.termId).name,
          cohortCode: body.cohortCode ?? 'DEFAULT', sessionAllocations: [], assessmentComponents: [],
        })
      }
      if (p.endsWith('/assessment-components')) {
        const course = this.courses.find((c) => c.id === parts[3])
        if (body.parentComponentId) assert.ok(course.assessmentComponents.some((c) => c.id === body.parentComponentId))
        // the platform upper-cases component codes ("YaN" is stored as "YAN")
        course.assessmentComponents.push({ ...structuredClone(body), code: body.code.toUpperCase(), id: `id-${++this.next}`, blocksFinal: body.blocksFinal ?? false })
        return course
      }
      if (p.endsWith('/modules')) return create(this.modules, { courseId: parts[3], position: this.modules.filter((m) => m.courseId === parts[3]).length, required: true })
      if (p.endsWith('/lessons')) {
        assert.deepEqual(Object.keys(body).sort(), ['code', 'sessionType'])
        return create(this.lessons, { moduleId: parts[3], position: this.lessons.filter((l) => l.moduleId === parts[3]).length, required: true })
      }
    }
    const list = parts[2] === 'subjects' ? this.subjects : parts[2] === 'courses' ? this.courses : parts[2] === 'modules' ? this.modules : this.lessons
    const entity = list.find((e) => e.id === parts[3])
    if (method === 'PATCH' && parts[2] === 'lessons') {
      assert.equal(typeof body.isRequired, 'boolean')
      Object.assign(entity, body, { required: body.isRequired })
      delete entity.isRequired
      return entity
    }
    if (method === 'PUT') {
      if (parts[4] === 'translations') {
        entity.translations = entity.translations.filter((t) => t.locale !== parts[5])
        entity.translations.push({ locale: parts[5], ...structuredClone(body) })
        return entity
      }
      if (parts[4] === 'session-allocations') {
        entity.sessionAllocations = entity.sessionAllocations.filter((a) => a.sessionType !== body.sessionType)
        entity.sessionAllocations.push(structuredClone(body))
        return entity
      }
      if (parts[4] === 'status') {
        assert.equal(entity.status, 'DRAFT')
        assert.equal(entity.translations.length, 3)
        entity.status = body.status
        return entity
      }
      if (p.endsWith('/reorder')) {
        const ids = body.orderedModuleIds ?? body.orderedLessonIds
        const all = body.orderedModuleIds ? this.modules.filter((m) => m.courseId === parts[3]) : this.lessons.filter((l) => l.moduleId === parts[3])
        assert.deepEqual(new Set(ids), new Set(all.map((e) => e.id)))
        ids.forEach((id, i) => { all.find((e) => e.id === id).position = i })
        return null
      }
    }
    throw new Error(`Fake API has no route: ${method} ${url}`)
  }
}

const run = (spec, api, options = {}) => scaffold(spec, { api, log: () => {}, ...options })

test('creates the complete skeleton with contract requests, then publishes in manifest order', async () => {
  const api = new FakeApi()
  const result = await run(manifest(), api)
  assert.equal(result.courseId, api.courses[0].id)
  assert.equal(api.subjects[0].subjectKind, 'ELECTIVE')
  assert.deepEqual(api.courses[0].sessionAllocations, [{ sessionType: 'LECTURE', allocatedHours: 2 }, { sessionType: 'PRACTICAL', allocatedHours: 2 }])
  assert.equal(api.courses[0].assessmentComponents.length, 4)
  assert.equal(api.courses[0].assessmentComponents[1].parentComponentId, api.courses[0].assessmentComponents[0].id)
  assert.equal(api.subjects[0].status, 'DRAFT')
  assert.equal(api.courses[0].status, 'DRAFT')
  assert.deepEqual(api.lessons.map((l) => [l.code, l.allocatedHours, l.status, l.translations.length]), [['M1', 2, 'PUBLISHED', 3], ['A1', 2, 'PUBLISHED', 3]])
  const writes = api.calls.filter((c) => c.method !== 'GET')
  const publishAt = writes.findIndex((c) => c.url.endsWith('/status'))
  assert.ok(publishAt > writes.findLastIndex((c) => !c.url.endsWith('/status')))
  assert.deepEqual(writes.slice(publishAt).map((c) => c.url), [...api.lessons.map((l) => `/teacher/lessons/${l.id}/status`), `/teacher/modules/${api.modules[0].id}/status`])
})

test('identical rerun performs zero writes, even with paginated lessons', async () => {
  const api = new FakeApi({ pageSize: 1 })
  const spec = manifest()
  await run(spec, api)
  const snapshot = api.data()
  api.calls = []
  assert.equal((await run(spec, api)).plan.length, 0)
  assert.ok(api.calls.every((c) => c.method === 'GET'))
  assert.deepEqual(api.data(), snapshot)
  assert.ok(api.calls.some((c) => c.url.includes('page=1')))
})

test('dry-run on missing entities uses GETs only and prints the complete write plan', async () => {
  const api = new FakeApi()
  const lines = []
  const snapshot = api.data()
  const result = await run(manifest(), api, { dryRun: true, log: (line) => lines.push(line) })
  assert.ok(result.plan.some((op) => op.url === '/admin/subjects'))
  assert.ok(result.plan.some((op) => op.url === '/admin/courses'))
  assert.ok(lines.some((line) => line.includes('PATCH /teacher/lessons/')))
  assert.ok(lines.some((line) => line.includes('PUBLISHED')))
  assert.ok(api.calls.every((c) => c.method === 'GET'))
  assert.deepEqual(api.data(), snapshot)
})

test('missing term clearly lists available terms and performs no writes', async () => {
  const api = new FakeApi()
  const spec = manifest()
  spec.termName = 'missing'
  await assert.rejects(run(spec, api), /Term "missing": not found\. Available terms: 1-semestr \[2026-2027\]/)
  assert.ok(api.calls.every((c) => c.method === 'GET'))
})

test('ambiguous term fails; academicYearCode selects exactly one year', async () => {
  const api = new FakeApi()
  api.years.push({ id: 'year2', code: '2027-2028' })
  api.terms.push({ id: 'term2', academicYearId: 'year2', name: '1-semestr' })
  const spec = manifest()
  await assert.rejects(run(spec, api), /ambiguous; set academicYearCode/)
  spec.academicYearCode = '2027-2028'
  await run(spec, api)
  assert.equal(api.courses[0].termId, 'term2')
})

for (const [name, change, message] of [
  ['root weights', (s) => { s.assessmentComponents[3].maxPoints = 39 }, /top-level weights must sum to 100/],
  ['child weights', (s) => { s.assessmentComponents[0].maxPoints = 29 }, /child weights must sum/],
  ['parent cycle', (s) => { s.assessmentComponents[1].parent = 'JN_P' }, /parent cycle/],
  ['unknown parent', (s) => { s.assessmentComponents[0].parent = 'missing' }, /unknown parent/],
  ['pass percentage', (s) => { s.assessmentComponents[1].minPassPercent = 101 }, /minPassPercent/],
  ['course hours', (s) => { s.course.totalHours = 5 }, /hours must sum to course.totalHours/],
  ['lesson hours', (s) => { s.modules[0].lessons[0].hours = 1 }, /hours must sum to session allocation/],
  ['missing total hours', (s) => { delete s.course.totalHours }, /totalHours \(required by the backend\)/],
  ['duplicate lesson codes', (s) => { s.modules[0].lessons[1].code = 'M1' }, /duplicate code/],
  ['incomplete translations', (s) => { delete s.modules[0].titles.ru }, /titles.ru/],
  ['rounding loss', (s) => { s.modules[0].lessons[0].hours = 2.001 }, /decimal places/],
]) {
  test(`invalid ${name} reported before any API call`, async () => {
    const api = new FakeApi()
    const spec = manifest()
    change(spec)
    await assert.rejects(run(spec, api), message)
    assert.equal(api.calls.length, 0)
  })
}

test('legacy course.json files remain valid and use existing courses without writes', async () => {
  for (const folder of fs.readdirSync(new URL('../../content/', import.meta.url))) {
    const file = new URL(`../../content/${folder}/course.json`, import.meta.url)
    if (fs.existsSync(file)) assert.deepEqual(validateManifest(JSON.parse(fs.readFileSync(file, 'utf8'))), [])
  }
  const api = new FakeApi()
  await run(manifest(), api)
  api.calls = []
  const result = await run({ subjectCode: 'TEST1000', termName: '1-semestr', title: 'Legacy title' }, api)
  assert.equal(result.plan.length, 0)
  assert.ok(api.calls.every((c) => c.method === 'GET'))
})

test('absent academic fields stay absent in requests; existing summary survives title changes', async () => {
  const api = new FakeApi()
  const spec = manifest()
  delete spec.subject.type
  delete spec.modules[0].groupingSource
  delete spec.modules[0].lessons[0].hours
  await run(spec, api)
  assert.ok(!Object.hasOwn(api.calls.find((c) => c.url === '/admin/subjects' && c.method === 'POST').body, 'subjectKind'))
  assert.ok(!Object.hasOwn(api.calls.find((c) => c.url.endsWith('/modules') && c.method === 'POST').body, 'groupingSource'))
  assert.ok(!Object.hasOwn(api.lessons[0], 'allocatedHours'))
  api.lessons[0].translations[0].summary = 'Teacher summary'
  api.lessons[0].difficulty = 'EASY'
  api.lessons[0].required = false
  spec.modules[0].lessons[0].titles.uz = 'Updated title'
  spec.modules[0].lessons[0].hours = 2
  await run(spec, api)
  assert.equal(api.lessons[0].translations[0].summary ?? api.lessons[0].translations[2].summary, 'Teacher summary')
  assert.equal(api.lessons[0].difficulty, 'EASY')
  assert.equal(api.lessons[0].required, false)
})

test('partially created drafts resume safely, and order is reconciled once', async () => {
  const api = new FakeApi({ pageSize: 1 })
  const spec = manifest()
  await run(spec, api)
  api.lessons[0].status = 'DRAFT'
  api.lessons[0].translations = api.lessons[0].translations.filter((t) => t.locale !== 'en')
  api.lessons[0].position = 1
  api.lessons[1].position = 0
  api.calls = []
  await run(spec, api)
  assert.equal(api.subjects.length, 1)
  assert.equal(api.courses.length, 1)
  assert.equal(api.lessons.length, 2)
  assert.equal(api.lessons[0].status, 'PUBLISHED')
  assert.ok(api.calls.some((c) => c.url.endsWith('/reorder')))
  assert.equal((await run(spec, api)).plan.length, 0)
})

test('immutable rubric conflicts are discovered before writes', async () => {
  const api = new FakeApi()
  const spec = manifest()
  await run(spec, api)
  api.courses[0].assessmentComponents[0].maxPoints = 25
  api.calls = []
  await assert.rejects(run(spec, api), /existing maxPoints.*differs from manifest/)
  assert.ok(api.calls.every((c) => c.method === 'GET'))
})

test('a lesson title that the lesson texts already set is not rewritten by the manifest', async () => {
  const api = new FakeApi()
  const spec = manifest()
  await run(spec, api)
  const lesson = api.lessons.find((l) => l.code === 'M1')
  for (const t of lesson.translations) t.title = 'Title written by import.mjs from the lesson text'
  api.calls = []
  const result = await run(spec, api)
  assert.equal(result.plan.length, 0, 'no PUT of the manifest title over the importer title')
  assert.ok(api.calls.every((c) => c.method === 'GET'))
})
