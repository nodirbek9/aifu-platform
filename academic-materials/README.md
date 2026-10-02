# academic-materials/ — Source of Truth (DO NOT MODIFY)

This directory holds **original academic documents supplied by AIFU**. It is an *input*
to engineering, never an output.

## Rules for humans and AI agents

1. **Never edit, reformat, rename, or delete anything in `syllabi/` or `branding/`.**
2. Engineering artifacts derived from these documents live in `docs/academic-analysis/`.
3. `_extracted/` contains plain-text conversions generated for agent readability.
   They are **derived, lossy, and regenerable** — never cite them as authoritative
   when the original is available. Regenerate with:
   `libreoffice --headless --convert-to txt --outdir academic-materials/_extracted <file>`
4. When new syllabi arrive, drop them into `syllabi/` and follow
   `docs/academic-analysis/HOW-TO-ONBOARD-A-SYLLABUS.md`.

## Current contents

### syllabi/
| File | Subject | Code | Status |
|---|---|---|---|
| `IAKT_iqtisod_1k_2025_sillabus.docx` | Iqtisodiyotda axborot kommunikatsion texnologiyalar va tizimlar | `IAKTT1105` | Analyzed |
| `1.09. Dasturlash 142_bo'yicha_DI.doc` | Dasturlash | `DAS1110` | Analyzed |

### branding/
**Empty — pending.** Expected: AIFU logo (SVG preferred + PNG), colour palette, typography.
No branding asset is referenced by the backend today; see
`docs/academic-analysis/PENDING-INPUTS.md`.

### Expected later (13+ subjects)
Programming (have), Cybersecurity, Fundamentals of Artificial Intelligence,
Computer Networks, + others. Until a syllabus is supplied, **no subject-specific
academic content is created.** The architecture is deliberately generic so those
subjects are *data*, not code changes.
