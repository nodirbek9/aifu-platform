// Creates a course skeleton through the same HTTP client as the content importer.
// All reads and conflict checks precede writes. Dry-run executes only these reads.
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { Api } from './api.mjs'
import { LOCALES, validateManifest } from './scaffold-manifest.mjs'

const pick = (value, keys) => Object.fromEntries(keys.filter((key) => value[key] !== undefined).map((key) => [key, value[key]]))
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b)

async function pages(api, url) {
  const result = []
  for (let page = 0; ; page += 1) {
    const data = await api.call('GET', `${url}${url.includes('?') ? '&' : '?'}size=100&page=${page}`)
    result.push(...data.content)
    if (!data.hasNext && page + 1 >= (data.totalPages ?? 1)) return result
  }
}

function one(items, label, required = false) {
  if (items.length > 1) throw new Error(`${label}: ${items.length} matches; lookup is ambiguous`)
  if (required && items.length === 0) throw new Error(`${label}: not found; supply its creation fields in course.json`)
  return items[0]
}

function compatible(entity, expected, label) {
  for (const [key, value] of Object.entries(expected)) {
    if (value !== undefined && !same(entity[key], value)) {
      throw new Error(`${label}: existing ${key}=${JSON.stringify(entity[key])} differs from manifest ${JSON.stringify(value)}; scaffold will not overwrite catalogue/rubric data`)
    }
  }
}

/** Validate, discover, plan, then apply. The injected api/log make this testable offline. */
export async function scaffold(manifest, { api, dryRun = false, log = console.log } = {}) {
  const errors = validateManifest(manifest)
  if (errors.length) throw new Error(`Invalid scaffold manifest:\n${errors.map((e) => `  - ${e}`).join('\n')}`)
  const plan = []
  const add = (method, url, body, bind) => plan.push({ method, url, body, ...(bind && { bind }) })
  const create = (url, body, id, componentCode) => {
    add('POST', url, body, { id, ...(componentCode && { componentCode }) })
    return { id, status: 'DRAFT', translations: [] }
  }
  const translations = (entity, url, titles, extraKeys = []) => {
    for (const locale of LOCALES) {
      if (titles?.[locale] === undefined) continue
      const current = entity.translations?.find((t) => t.locale === locale)
      if (current?.title === titles[locale]) continue
      add('PUT', `${url}/translations/${locale}`, { ...pick(current ?? {}, extraKeys), title: titles[locale] })
    }
  }
  const reorder = (all, wanted, url, field) => {
    const ids = [...wanted.map((e) => e.id), ...all.filter((e) => !wanted.some((w) => w.id === e.id)).map((e) => e.id)]
    if (!same(all.map((e) => e.id), ids)) add('PUT', `${url}/reorder`, { [field]: ids })
  }

  // Never create a term or silently choose a year, even when the name matches twice.
  const years = await api.call('GET', '/admin/academic-years')
  const terms = []
  for (const year of years) {
    for (const term of await api.call('GET', `/admin/academic-years/${year.id}/terms`)) {
      terms.push({ ...term, academicYearCode: year.code })
    }
  }
  const candidates = terms.filter((t) => t.name === manifest.termName &&
    (manifest.academicYearCode === undefined || t.academicYearCode === manifest.academicYearCode))
  if (candidates.length !== 1) {
    throw new Error(`Term ${JSON.stringify(manifest.termName)}${manifest.academicYearCode ? ` in ${manifest.academicYearCode}` : ''}: ${candidates.length === 0 ? 'not found' : 'ambiguous; set academicYearCode'}. Available terms: ${terms.map((t) => `${t.name} [${t.academicYearCode}]`).join(', ') || '(none)'}`)
  }
  const term = candidates[0]
  let department
  if (manifest.subject) {
    const departments = await api.call('GET', '/reference/departments')
    department = one(departments.filter((d) => d.code === manifest.subject.departmentCode), `Department ${manifest.subject.departmentCode}`)
    if (!department) throw new Error(`Department ${manifest.subject.departmentCode}: not found. Available departments: ${departments.map((d) => d.code).join(', ') || '(none)'}`)
  }
  let subject = one((await pages(api, `/admin/subjects?query=${encodeURIComponent(manifest.subjectCode)}`))
    .filter((s) => s.code === manifest.subjectCode), `Subject ${manifest.subjectCode}`, !manifest.subject)
  if (subject) subject = await api.call('GET', `/admin/subjects/${subject.id}`)
  if (manifest.subject) {
    const request = { code: manifest.subjectCode, departmentId: department.id,
      ...(manifest.subject.type !== undefined && { subjectKind: manifest.subject.type }),
      ...pick(manifest.subject, ['defaultInstructionLanguage']) }
    if (subject) compatible(subject, request, `Subject ${manifest.subjectCode}`)
    else subject = create('/admin/subjects', request, '$subject')
    translations(subject, `/admin/subjects/${subject.id}`, manifest.subject.names, ['shortTitle', 'summary', 'description', 'aim', 'objectives'])
  }
  // Course identity includes cohort, as enforced by the server's unique constraint.
  const catalog = await pages(api, `/admin/courses?query=${encodeURIComponent(manifest.subjectCode)}&termId=${encodeURIComponent(term.id)}`)
  let course = one(catalog.filter((c) => c.subjectCode === manifest.subjectCode && c.termName === term.name &&
    (!manifest.course || c.cohortCode === (manifest.course.cohortCode ?? 'DEFAULT'))),
  `Course ${manifest.subjectCode}/${term.name}`, !manifest.course)
  if (course) {
    course = await api.call('GET', `/admin/courses/${course.id}`)
    compatible(course, { subjectId: subject.id, termId: term.id }, 'Course identity')
  }
  if (manifest.course) {
    const request = { subjectId: subject.id, termId: term.id,
      ...pick(manifest.course, ['credits', 'totalHours', 'cohortCode', 'instructionLanguage', 'gradeScaleCode']) }
    if (course) compatible(course, pick(request, ['credits', 'totalHours', 'cohortCode', 'instructionLanguage']), 'Course')
    else course = create('/admin/courses', request, '$course')
    translations(course, `/admin/courses/${course.id}`, manifest.course.titles ??
      (manifest.title !== undefined ? { uz: manifest.title } : undefined), ['description'])
    for (const [sessionType, allocatedHours] of Object.entries(manifest.course.sessionAllocations ?? {})) {
      const current = course.sessionAllocations?.find((a) => a.sessionType === sessionType)
      if (current?.allocatedHours !== allocatedHours) add('PUT', `/admin/courses/${course.id}/session-allocations`, { sessionType, allocatedHours })
    }
    // Include untouched server budgets when checking the resulting course.
    if (manifest.course.sessionAllocations) {
      const final = Object.fromEntries((course.sessionAllocations ?? []).map((a) => [a.sessionType, a.allocatedHours]))
      Object.assign(final, manifest.course.sessionAllocations)
      if (Object.values(final).reduce((a, b) => a + b, 0) !== manifest.course.totalHours) {
        throw new Error('Existing session allocations plus manifest hours do not sum to course.totalHours; no writes performed')
      }
    }
  }
  const components = new Map((course.assessmentComponents ?? []).map((c) => [c.code, c]))
  if (manifest.assessmentComponents) {
    // A complete rubric is provided; extra server nodes would invalidate its totals.
    for (const c of components.values()) {
      if (!manifest.assessmentComponents.some((m) => m.code.toLowerCase() === c.code.toLowerCase())) {
        throw new Error(`Assessment component ${c.code}: exists outside the manifest; no writes performed`)
      }
    }
    const byCode = new Map(manifest.assessmentComponents.map((c, i) => [c.code, { ...c, displayOrder: i }]))
    const planned = new Set()
    const component = (spec) => {
      if (planned.has(spec.code)) return
      if (spec.parent !== undefined) component(byCode.get(spec.parent))
      const parentComponentId = spec.parent === undefined ? undefined : components.get(spec.parent).id
      const request = { ...pick(spec, ['code', 'kind', 'maxPoints', 'minPassPercent', 'blocksFinal', 'assessmentForm', 'displayOrder']),
        ...(parentComponentId !== undefined && { parentComponentId }) }
      const current = [...components.values()].find((c) => c.code.toLowerCase() === spec.code.toLowerCase())
      if (current) {
        compatible(current, { ...request, parentComponentId: parentComponentId ?? null }, `Assessment component ${spec.code}`)
        // Jackson omits null parents; compare absent and null alike below instead.
        components.set(spec.code, current)
      } else components.set(spec.code, create(`/admin/courses/${course.id}/assessment-components`, request, `$component:${spec.displayOrder}`, spec.code))
      planned.add(spec.code)
    }
    for (const spec of byCode.values()) component(spec)
  }

  const allModules = course.id === '$course' ? [] : await pages(api, `/teacher/courses/${course.id}/modules?sort=position,asc`)
  const lessonLists = new Map()
  const lessonCodes = new Map()
  for (const module of allModules) {
    const list = await pages(api, `/teacher/modules/${module.id}/lessons?sort=position,asc`)
    lessonLists.set(module.id, list)
    for (const lesson of list) {
      if (lessonCodes.has(lesson.code)) throw new Error(`Lesson code ${lesson.code}: multiple existing lessons in this course`)
      lessonCodes.set(lesson.code, lesson)
    }
  }
  const wantedModules = []
  const publishLessons = []
  for (const [i, spec] of (manifest.modules ?? []).entries()) {
    let module = one(allModules.filter((m) => m.code === spec.code), `Module ${spec.code}`)
    if (!module) {
      module = create(`/teacher/courses/${course.id}/modules`, pick(spec, ['code', 'groupingSource']), `$module:${i}`)
      allModules.push(module)
    } else if (spec.groupingSource !== undefined) compatible(module, { groupingSource: spec.groupingSource }, `Module ${spec.code}`)
    if (module.status === 'ARCHIVED') throw new Error(`Module ${spec.code}: archived; restore it explicitly before scaffolding`)
    wantedModules.push(module)
    translations(module, `/teacher/modules/${module.id}`, spec.titles, ['description'])
    const allLessons = lessonLists.get(module.id) ?? []
    const wantedLessons = []
    for (const [j, topic] of spec.lessons.entries()) {
      let lesson = lessonCodes.get(topic.code)
      if (lesson && lesson.moduleId !== module.id) throw new Error(`Lesson ${topic.code}: already belongs to another module; no writes performed`)
      if (!lesson) {
        lesson = create(`/teacher/modules/${module.id}/lessons`, pick(topic, ['code', 'sessionType']), `$lesson:${publishLessons.length}`)
        lesson.sessionType = topic.sessionType
        lesson.required = true // The server's new-lesson default, preserved by its replacing PATCH.
        allLessons.push(lesson)
      }
      if (lesson.status === 'ARCHIVED') throw new Error(`Lesson ${topic.code}: archived; restore it explicitly before scaffolding`)
      if (lesson.sessionType !== topic.sessionType || (topic.hours !== undefined && lesson.allocatedHours !== topic.hours)) {
        add('PATCH', `/teacher/lessons/${lesson.id}`, {
          ...pick(lesson, ['allocatedHours', 'estimatedMinutes', 'difficulty', 'availableFrom']),
          code: topic.code, sessionType: topic.sessionType, isRequired: lesson.required,
          ...(topic.hours !== undefined && { allocatedHours: topic.hours }),
        })
      }
      translations(lesson, `/teacher/lessons/${lesson.id}`, topic.titles, ['summary'])
      wantedLessons.push(lesson)
      publishLessons.push(lesson)
    }
    reorder(allLessons, wantedLessons, `/teacher/modules/${module.id}/lessons`, 'orderedLessonIds')
  }
  if (manifest.modules) reorder(allModules, wantedModules, `/teacher/courses/${course.id}/modules`, 'orderedModuleIds')
  // Complete the entire skeleton before publishing any lesson; order is the manifest order.
  for (const lesson of publishLessons) if (lesson.status !== 'PUBLISHED') add('PUT', `/teacher/lessons/${lesson.id}/status`, { status: 'PUBLISHED' })
  for (const module of wantedModules) if (module.status !== 'PUBLISHED') add('PUT', `/teacher/modules/${module.id}/status`, { status: 'PUBLISHED' })

  log(`${manifest.subjectCode}/${term.name}: ${plan.length} planned writes${dryRun ? ' (dry-run)' : ''}`)
  const ids = new Map()
  function resolve(value) {
    if (typeof value === 'string') return ids.get(value) ?? value
    if (Array.isArray(value)) return value.map(resolve)
    if (value !== null && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, resolve(v)]))
    return value
  }
  for (const op of plan) {
    log(`  ${op.method} ${op.url} ${JSON.stringify(op.body)}`)
    if (dryRun) continue
    const url = op.url.replace(/\$(?:subject|course|module:\d+|lesson:\d+|component:\d+)/g, (key) => {
      if (!ids.has(key)) throw new Error(`Unresolved scaffold reference ${key}`)
      return ids.get(key)
    })
    const result = await api.call(op.method, url, resolve(op.body))
    if (op.bind) {
      const entity = op.bind.componentCode ? result.assessmentComponents.find((c) => c.code === op.bind.componentCode) : result
      if (!entity?.id) throw new Error(`${op.method} ${url}: response has no created ID; re-run to resume`)
      ids.set(op.bind.id, entity.id)
    }
  }
  log(dryRun ? 'Dry-run: no academic data written (authenticated GETs only).' : 'Skeleton ready; subject/course publication remains an explicit academic review step.')
  return { courseId: resolve(course.id), plan }
}

async function main() {
  const args = process.argv.slice(2)
  const files = args.filter((arg) => arg !== '--dry-run')
  if (files.length !== 1 || files[0].startsWith('--')) throw new Error('Usage: node tools/content/scaffold.mjs <course folder|course.json> [--dry-run]')
  const location = path.resolve(files[0])
  const file = fs.statSync(location).isDirectory() ? path.join(location, 'course.json') : location
  const manifest = JSON.parse(fs.readFileSync(file, 'utf8'))
  const errors = validateManifest(manifest)
  if (errors.length) throw new Error(`Invalid scaffold manifest:\n${errors.join('\n')}`)
  if (!process.env.AIFU_PASSWORD) throw new Error('AIFU_PASSWORD is not set (dry-run also needs authenticated reads)')
  const api = new Api(process.env.AIFU_API ?? 'http://localhost:8080', process.env.AIFU_USER ?? 'admin', process.env.AIFU_PASSWORD)
  await scaffold(manifest, { api, dryRun: args.includes('--dry-run') })
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch((error) => { console.error(error.message); process.exitCode = 1 })
}
