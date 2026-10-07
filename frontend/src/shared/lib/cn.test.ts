import { describe, expect, it } from 'vitest'
import { cn } from './cn'
import { tv } from './tv'

describe('class merging knows the design tokens', () => {
  it('keeps a text colour next to a type-scale class', () => {
    expect(cn('text-primary-foreground', 'text-body')).toBe('text-primary-foreground text-body')
    expect(cn('text-caption', 'text-text-muted')).toBe('text-caption text-text-muted')
  })

  it('still lets a later type-scale class replace an earlier one', () => {
    expect(cn('text-body', 'text-small')).toBe('text-small')
    expect(cn('text-body', 'text-reading')).toBe('text-reading')
    expect(cn('text-display-lg', 'text-display-md')).toBe('text-display-md')
  })

  it('still lets a later colour replace an earlier one', () => {
    expect(cn('text-text', 'text-danger')).toBe('text-danger')
  })

  it('applies the same rules inside tv()', () => {
    const recipe = tv({
      base: 'text-accent-foreground',
      variants: { size: { md: 'text-body', sm: 'text-small' } },
    })
    expect(recipe({ size: 'md' })).toBe('text-accent-foreground text-body')
    expect(recipe({ size: 'sm', class: 'text-danger' })).toBe('text-small text-danger')
  })
})
