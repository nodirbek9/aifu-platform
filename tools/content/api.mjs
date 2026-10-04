// The platform's API as the content tools use it: sign-in, calls, and finding a course and its
// lessons by the codes the content folders are named with.

export class Api {
  constructor(base, user, password) {
    Object.assign(this, { base, user, password, token: null })
  }

  async login() {
    const response = await fetch(`${this.base}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier: this.user, password: this.password }),
    })
    if (!response.ok) throw new Error(`sign-in as ${this.user} failed: ${response.status}`)
    this.token = (await response.json()).accessToken
  }

  async call(method, url, body, retry = true) {
    if (this.token === null) await this.login()
    const response = await fetch(`${this.base}/api/v1${url}`, {
      method,
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${this.token}` },
      ...(body !== undefined && { body: JSON.stringify(body) }),
    })
    if (response.status === 401 && retry) {
      await this.login()
      return this.call(method, url, body, false)
    }
    const text = await response.text()
    if (!response.ok) throw new Error(`${method} ${url} → ${response.status} ${text.slice(0, 300)}`)
    return text === '' ? null : JSON.parse(text)
  }
}

export async function findCourse(api, spec) {
  const page = await api.call('GET', `/admin/courses?query=${encodeURIComponent(spec.subjectCode)}&size=100`)
  const found = (page.content ?? []).filter(
    (course) => course.subjectCode === spec.subjectCode && (!spec.termName || course.termName === spec.termName),
  )
  if (found.length !== 1) throw new Error(`course ${JSON.stringify(spec)}: ${found.length} matches`)
  return found[0].id
}

export async function lessonsByCode(api, courseId) {
  const modules = (await api.call('GET', `/teacher/courses/${courseId}/modules?size=100`)).content ?? []
  const lessons = new Map()
  for (const module of modules) {
    const page = await api.call('GET', `/teacher/modules/${module.id}/lessons?size=100`)
    for (const lesson of page.content ?? []) lessons.set(lesson.code, lesson)
  }
  return lessons
}

/** The API client from the environment: AIFU_API, AIFU_USER, AIFU_PASSWORD. */
export function apiFromEnvironment() {
  if (!process.env.AIFU_PASSWORD) {
    console.error('AIFU_PASSWORD is not set')
    process.exit(2)
  }
  return new Api(process.env.AIFU_API ?? 'http://localhost:8080', process.env.AIFU_USER ?? 'admin', process.env.AIFU_PASSWORD)
}
