// Pure validation of the extended course.json. No defaults for academic data, no IO.
export const LOCALES = ['uz', 'ru', 'en']
export const SESSION_TYPES = ['LECTURE', 'SEMINAR', 'PRACTICAL', 'LABORATORY', 'INDEPENDENT_STUDY']
const KINDS = ['CONTINUOUS', 'MIDTERM', 'FINAL', 'ATTENDANCE', 'INDEPENDENT_WORK', 'PRACTICAL_ACTIVITY', 'PROJECT', 'COURSE_WORK']
const object = (v) => v !== null && typeof v === 'object' && !Array.isArray(v)
const text = (v) => typeof v === 'string' && v.trim() !== '' && v === v.trim()
const equalSum = (values, total) => Math.abs(values.reduce((a, b) => a + b, 0) - total) < 1e-8

/** Human-readable problems; [] means valid. Legacy lookup-only manifests remain valid. */
export function validateManifest(manifest) {
  const errors = []
  function fields(value, at, keys) {
    if (!object(value)) { errors.push(`${at}: must be an object`); return false }
    for (const key of Object.keys(value)) if (!keys.includes(key)) errors.push(`${at}.${key}: unknown field`)
    return true
  }
  function string(value, at, max = Infinity) {
    if (!text(value) || value.length > max) errors.push(`${at}: must be non-empty text without surrounding whitespace (max ${max})`)
  }
  function number(value, at, min, max, decimals = 2) {
    if (typeof value !== 'number' || !Number.isFinite(value) || value < min || value > max ||
        Math.abs(value * 10 ** decimals - Math.round(value * 10 ** decimals)) > 1e-7) {
      errors.push(`${at}: must be a number in ${min}..${max}, at most ${decimals} decimal places`)
    }
  }
  function choice(value, at, choices) {
    if (!choices.includes(value)) errors.push(`${at}: must be one of ${choices.join('|')}`)
  }
  function localized(value, at) {
    if (fields(value, at, LOCALES)) for (const locale of LOCALES) string(value[locale], `${at}.${locale}`, 500)
  }
  function unique(items, at, insensitive = false) {
    const seen = new Set()
    for (const item of items) {
      const key = insensitive && typeof item?.code === 'string' ? item.code.toLowerCase() : item?.code
      if (seen.has(key)) errors.push(`${at}: duplicate code ${item?.code}`)
      seen.add(key)
    }
  }
  if (!fields(manifest, 'manifest', ['subjectCode', 'termName', 'title', 'academicYearCode', 'subject', 'course', 'assessmentComponents', 'modules'])) return errors
  string(manifest.subjectCode, 'subjectCode', 32)
  string(manifest.termName, 'termName')
  if (manifest.title !== undefined) string(manifest.title, 'title', 500)
  if (manifest.academicYearCode !== undefined) string(manifest.academicYearCode, 'academicYearCode')
  if (manifest.subject !== undefined && fields(manifest.subject, 'subject', ['names', 'departmentCode', 'type', 'defaultInstructionLanguage'])) {
    const s = manifest.subject
    localized(s.names, 'subject.names')
    string(s.departmentCode, 'subject.departmentCode')
    if (s.type !== undefined) choice(s.type, 'subject.type', ['MANDATORY', 'ELECTIVE', 'OPTIONAL'])
    if (s.defaultInstructionLanguage !== undefined) choice(s.defaultInstructionLanguage, 'subject.defaultInstructionLanguage', LOCALES)
  }
  if (manifest.course !== undefined && fields(manifest.course, 'course', ['credits', 'totalHours', 'cohortCode', 'instructionLanguage', 'gradeScaleCode', 'titles', 'sessionAllocations'])) {
    const c = manifest.course
    number(c.credits, 'course.credits', 0.1, 60, 1)
    // The server uses a primitive int + @Min(1), although OpenAPI omits required.
    number(c.totalHours, 'course.totalHours (required by the backend)', 1, 2147483647, 0)
    if (c.cohortCode !== undefined) string(c.cohortCode, 'course.cohortCode', 32)
    if (c.instructionLanguage !== undefined) choice(c.instructionLanguage, 'course.instructionLanguage', LOCALES)
    if (c.gradeScaleCode !== undefined) string(c.gradeScaleCode, 'course.gradeScaleCode')
    if (c.titles !== undefined) localized(c.titles, 'course.titles')
    if (c.titles && manifest.title !== undefined && c.titles.uz !== manifest.title) errors.push('title must equal course.titles.uz when both are supplied')
    if (c.sessionAllocations !== undefined && fields(c.sessionAllocations, 'course.sessionAllocations', SESSION_TYPES)) {
      for (const [type, hours] of Object.entries(c.sessionAllocations)) number(hours, `course.sessionAllocations.${type}`, 0, 2147483647, 0)
      if (!equalSum(Object.values(c.sessionAllocations), c.totalHours)) errors.push('course.sessionAllocations: hours must sum to course.totalHours')
    }
  }
  if (manifest.assessmentComponents !== undefined) {
    if (!Array.isArray(manifest.assessmentComponents)) errors.push('assessmentComponents: must be an array')
    else {
      unique(manifest.assessmentComponents, 'assessmentComponents', true)
      manifest.assessmentComponents.forEach((c, i) => {
        const at = `assessmentComponents[${i}]`
        if (!fields(c, at, ['code', 'kind', 'maxPoints', 'parent', 'minPassPercent', 'blocksFinal', 'assessmentForm'])) return
        string(c.code, `${at}.code`, 32)
        choice(c.kind, `${at}.kind`, KINDS)
        number(c.maxPoints, `${at}.maxPoints`, 0.01, 9999.99)
        if (c.parent !== undefined) string(c.parent, `${at}.parent`, 32)
        if (c.minPassPercent !== undefined) number(c.minPassPercent, `${at}.minPassPercent`, 0, 100)
        if (c.blocksFinal !== undefined && typeof c.blocksFinal !== 'boolean') errors.push(`${at}.blocksFinal: must be boolean`)
        if (c.assessmentForm !== undefined) choice(c.assessmentForm, `${at}.assessmentForm`, ['WRITTEN', 'ORAL', 'MIXED', 'PRACTICAL', 'TEST', 'PROJECT'])
      })
    }
  }
  const lessons = []
  if (manifest.modules !== undefined) {
    if (!Array.isArray(manifest.modules)) errors.push('modules: must be an array')
    else {
      unique(manifest.modules, 'modules')
      manifest.modules.forEach((m, i) => {
        const at = `modules[${i}]`
        if (!fields(m, at, ['code', 'titles', 'groupingSource', 'lessons'])) return
        string(m.code, `${at}.code`)
        localized(m.titles, `${at}.titles`)
        if (m.groupingSource !== undefined) choice(m.groupingSource, `${at}.groupingSource`, ['SYLLABUS_EXPLICIT', 'PLATFORM_INFERRED', 'TEACHER_DEFINED'])
        if (!Array.isArray(m.lessons)) { errors.push(`${at}.lessons: must be an array`); return }
        m.lessons.forEach((l, j) => {
          const loc = `${at}.lessons[${j}]`
          if (!fields(l, loc, ['code', 'sessionType', 'hours', 'titles'])) return
          lessons.push(l)
          string(l.code, `${loc}.code`)
          choice(l.sessionType, `${loc}.sessionType`, SESSION_TYPES)
          localized(l.titles, `${loc}.titles`)
          if (l.hours !== undefined) number(l.hours, `${loc}.hours`, 0, 999.99)
        })
      })
      unique(lessons, 'lessons (course-wide)')
    }
  }
  if (errors.length) return errors
  if (manifest.assessmentComponents !== undefined) {
    const components = manifest.assessmentComponents
    const byCode = new Map(components.map((c) => [c.code, c]))
    const roots = components.filter((c) => c.parent === undefined)
    if (!equalSum(roots.map((c) => c.maxPoints), 100)) errors.push('assessmentComponents: top-level weights must sum to 100')
    for (const c of components) {
      const seen = new Set([c.code])
      let parent = c.parent
      while (parent !== undefined) {
        if (!byCode.has(parent)) { errors.push(`assessmentComponents.${c.code}: unknown parent ${parent}`); break }
        if (seen.has(parent)) { errors.push(`assessmentComponents.${c.code}: parent cycle at ${parent}`); break }
        seen.add(parent)
        parent = byCode.get(parent).parent
      }
      const children = components.filter((child) => child.parent === c.code)
      if (children.length && !equalSum(children.map((child) => child.maxPoints), c.maxPoints)) errors.push(`assessmentComponents.${c.code}: child weights must sum to parent maxPoints (${c.maxPoints})`)
    }
  }
  const allocations = manifest.course?.sessionAllocations
  if (allocations && manifest.modules !== undefined) {
    for (const type of SESSION_TYPES) {
      const topics = lessons.filter((l) => l.sessionType === type)
      if (topics.length && allocations[type] === undefined) {
        errors.push(`course.sessionAllocations.${type}: missing budget for the supplied lessons`)
      } else if (topics.every((l) => l.hours !== undefined) && allocations[type] !== undefined &&
          !equalSum(topics.map((l) => l.hours), allocations[type])) {
        errors.push(`lessons.${type}: hours must sum to session allocation (${allocations[type]})`)
      }
    }
  }
  return errors
}
