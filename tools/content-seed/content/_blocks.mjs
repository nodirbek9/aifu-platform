// Small builders shared by the content modules. See ../generate.mjs for how they become SQL.

export const text = (html) => ({ type: 'TEXT', html })
export const tip = (html) => ({ type: 'CALLOUT', variant: 'TIP', html })
export const warn = (html) => ({ type: 'CALLOUT', variant: 'WARNING', html })
export const info = (html) => ({ type: 'CALLOUT', variant: 'INFO', html })
export const py = (code, explanation) => ({ type: 'CODE', lang: 'python', code, explanation })
export const java = (code, explanation) => ({ type: 'CODE', lang: 'java', code, explanation })
export const task = (ref) => ({ type: 'TASK', ref })
export const quiz = (ref) => ({ type: 'QUIZ', ref })
export const link = (url, label) => ({ type: 'LINK', url, label })

/** A lecture-style lesson: overview, key points, optional code, one practical hint. */
export function topic({ intro, points, detail, pyCode, javaCode, pyNote, javaNote, mistake, practice, refs = [], links = [] }) {
  const blocks = []
  let html = `<h2>Mavzu mazmuni</h2>${intro}`
  if (points?.length) html += `<h3>Asosiy tushunchalar</h3><ul>${points.map((p) => `<li>${p}</li>`).join('')}</ul>`
  if (detail) html += detail
  blocks.push(text(html))
  if (pyCode) blocks.push(py(pyCode, pyNote))
  if (javaCode) blocks.push(java(javaCode, javaNote))
  if (mistake) blocks.push(warn(`<p><strong>Koʻp uchraydigan xato:</strong> ${mistake}</p>`))
  if (practice) blocks.push(tip(`<p><strong>Oʻzingizni tekshiring:</strong> ${practice}</p>`))
  for (const r of refs) blocks.push(r)
  for (const l of links) blocks.push(link(l[0], l[1]))
  return { blocks }
}

/** A practical session: goal, steps, then the linked task or quiz. */
export function practical({ goal, steps, theory, pyCode, javaCode, refs = [], check, links = [] }) {
  let html = `<h2>Amaliy mashgʻulot maqsadi</h2>${goal}`
  if (theory) html += theory
  if (steps?.length) html += `<h3>Bajarish tartibi</h3><ol>${steps.map((s) => `<li>${s}</li>`).join('')}</ol>`
  const blocks = [text(html)]
  if (pyCode) blocks.push(py(pyCode))
  if (javaCode) blocks.push(java(javaCode))
  for (const r of refs) blocks.push(r)
  if (check) blocks.push(tip(`<p><strong>Nazorat savollari:</strong> ${check}</p>`))
  for (const l of links) blocks.push(link(l[0], l[1]))
  return { blocks }
}

/** An independent-study brief. */
export function independent({ goal, tasks, deliverable, refs = [], links = [] }) {
  let html = `<h2>Mustaqil ish</h2>${goal}<h3>Topshiriqlar</h3><ol>${tasks.map((t) => `<li>${t}</li>`).join('')}</ol>`
  if (deliverable) html += `<h3>Topshiriladigan natija</h3>${deliverable}`
  const blocks = [text(html), ...refs]
  for (const l of links) blocks.push(link(l[0], l[1]))
  return { blocks }
}

export const LINKS = {
  pyTutorial: ['https://docs.python.org/3/tutorial/', 'Python rasmiy qoʻllanmasi (docs.python.org)'],
  javaTutorial: ['https://dev.java/learn/', 'Java rasmiy oʻquv resursi (dev.java)'],
  owaspTop10: ['https://owasp.org/www-project-top-ten/', 'OWASP Top 10 — veb-ilovalar xavfsizligi xatarlari'],
  mdnHttp: ['https://developer.mozilla.org/en-US/docs/Web/HTTP', 'MDN — HTTP protokoli qoʻllanmasi'],
  visualgo: ['https://visualgo.net/en', 'VisuAlgo — algoritmlar va maʼlumotlar tuzilmalari vizualizatsiyasi'],
}
