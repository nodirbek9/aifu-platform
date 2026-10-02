import { cn } from '@/shared/lib/cn'

export type ShieldEmblemProps = {
  className?: string
  /** Set false when no visible label sits next to the emblem. */
  decorative?: boolean
}

/*
 * Geometry measured from academic-materials/branding/AIFU_logo.png (1080 px artwork, one unit
 * here = 10 px there): the shield is 76 wide and 93 tall — straight sides for 55, then a
 * semicircle of radius 38; navy fills the top 41, magenta the rest. The letters sit at the
 * measured cap lines and widths.
 */
const OUTLINE = 'M2 2h76v55a38 38 0 0 1-76 0z'

/**
 * The AIFU emblem redrawn as SVG: square shoulders, round base, navy over magenta, AI / FU in
 * the slab display face, EST 2022. A white keyline surrounds it — invisible on paper, and on a
 * dark surface it is what keeps the navy half readable. Never recoloured or stretched.
 */
export function ShieldEmblem({ className, decorative = true }: ShieldEmblemProps) {
  return (
    <svg
      viewBox="0 0 80 97"
      role={decorative ? 'presentation' : 'img'}
      aria-hidden={decorative || undefined}
      aria-label={decorative ? undefined : 'AIFU'}
      className={cn('shrink-0', className)}
    >
      <path d={OUTLINE} fill="#FFFFFF" stroke="#FFFFFF" strokeWidth="3" strokeLinejoin="miter" />
      <path d="M2 2h76v41H2z" fill="#002E60" />
      <path d="M2 43h76v14a38 38 0 0 1-76 0z" fill="#FF0158" />
      <g fill="#FFFFFF" textAnchor="middle" fontFamily="'Bitter Variable', Georgia, serif" fontWeight={700}>
        <text x="40" y="37.2" fontSize="42" textLength="52.8" lengthAdjust="spacingAndGlyphs">
          AI
        </text>
        <text x="40" y="76" fontSize="42" textLength="52.8" lengthAdjust="spacingAndGlyphs">
          FU
        </text>
      </g>
      <text
        x="40"
        y="86.6"
        fill="#FFFFFF"
        textAnchor="middle"
        fontSize="6.2"
        fontWeight={500}
        fontFamily="'Onest Variable', system-ui, sans-serif"
        textLength="25.3"
        lengthAdjust="spacingAndGlyphs"
      >
        EST 2022
      </text>
    </svg>
  )
}
