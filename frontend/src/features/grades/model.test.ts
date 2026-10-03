import { describe, expect, it } from 'vitest'
import { admission, formatScore, hasHistory, narrowGradebook, totalMax, type ComponentGrade } from './model'

function component(overrides: Partial<ComponentGrade>): ComponentGrade {
  return {
    id: 'c',
    code: 'JN',
    kind: 'CONTINUOUS',
    maxPoints: 30,
    earned: 0,
    graded: false,
    blocksFinal: true,
    minPassPercent: 60,
    ...overrides,
  }
}

describe('admission to the final exam, read off the gates the server evaluated', () => {
  it('is pending while a gate is not graded yet', () => {
    expect(admission([component({ gatePassed: true }), component({ id: 'on', code: 'ON' })])).toBe('pending')
  })

  it('is granted once every gate is passed', () => {
    expect(
      admission([component({ gatePassed: true }), component({ id: 'on', code: 'ON', gatePassed: true })]),
    ).toBe('granted')
  })

  it('is refused as soon as one gate is failed, whatever the others say', () => {
    expect(admission([component({ gatePassed: false }), component({ id: 'on', code: 'ON' })])).toBe('denied')
  })

  it('does not exist in a scheme without gates', () => {
    expect(admission([component({ blocksFinal: false })])).toBe('none')
    // the final itself has a pass mark but blocks nothing
    expect(admission([component({ kind: 'FINAL', blocksFinal: false, gatePassed: false })])).toBe('none')
  })
})

describe('the scheme', () => {
  it('is worth the sum of its top-level components; parts are inside their parent', () => {
    expect(
      totalMax([
        component({ id: 'jn', maxPoints: 30 }),
        component({ id: 'att', parentId: 'jn', maxPoints: 10 }),
        component({ id: 'on', maxPoints: 30 }),
        component({ id: 'yan', maxPoints: 40 }),
      ]),
    ).toBe(100)
  })
})

describe('what the server sends, narrowed', () => {
  const raw = {
    enrollmentId: 'e',
    finalScore: 48,
    finalGradeSymbol: '2',
    complete: false,
    components: [
      { id: 'jn', code: 'JN', kind: 'CONTINUOUS' as const, maxPoints: 30, earned: 24, graded: true },
    ],
    entries: [{ id: 'g2', score: 8, maxScore: 10, status: 'PUBLISHED' as const, revision: 2 }],
    history: [
      { id: 'g2', score: 8, maxScore: 10, status: 'PUBLISHED' as const, revision: 2 },
      { id: 'g1', score: 6, maxScore: 10, status: 'REVISED' as const, revision: 1 },
      { id: 'g0', score: 5, maxScore: 10, status: 'DRAFT' as const },
    ],
  }

  it('keeps the corrections in the history and leaves drafts out', () => {
    const book = narrowGradebook(raw)
    expect(book.history.map((line) => line.id)).toEqual(['g2', 'g1'])
    expect(hasHistory(book)).toBe(true)
    expect(book.components[0]?.gatePassed).toBeUndefined()
  })

  it('a history of nothing but the grades in force is not worth showing', () => {
    expect(hasHistory(narrowGradebook({ ...raw, history: raw.entries }))).toBe(false)
  })

  it('writes scores as people do', () => {
    expect(formatScore(24)).toBe('24')
    expect(formatScore(7.5)).toBe('7.5')
  })
})
