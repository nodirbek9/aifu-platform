import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { RichText } from './rich-text'

describe('RichText reading typography', () => {
  it('renders with the font-reading token class', () => {
    const { container } = render(<RichText html="<p>Dasturlash asoslari darsi</p>" />)
    const element = container.firstElementChild
    expect(element).not.toBeNull()
    expect(element?.className).toContain('font-reading')
    expect(element?.className).toContain('text-reading-text')
  })

  it('renders content paragraphs and headings properly', () => {
    render(
      <RichText
        html="<h2>Bo'lim</h2><p>Paragraf matni</p><code>inline code</code>"
        headingIdPrefix="heading"
      />,
    )
    expect(screen.getByRole('heading', { level: 2, name: "Bo'lim" })).toHaveAttribute('id', 'heading-1')
    expect(screen.getByText('Paragraf matni')).toBeInTheDocument()
    expect(screen.getByText('inline code')).toBeInTheDocument()
  })
})
