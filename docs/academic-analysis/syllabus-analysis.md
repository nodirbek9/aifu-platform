# Syllabus Analysis — evidence extracted from supplied AIFU documents

**Inputs analyzed (2 of 15+ expected):**
1. `IAKT_iqtisod_1k_2025_sillabus.docx` — *Iqtisodiyotda axborot kommunikatsion texnologiyalar
   va tizimlar*, code `IAKTT1105`, syllabus format, 2025/2026.
2. `1.09. Dasturlash 142_bo'yicha_DI.doc` — *Dasturlash*, code `DAS1110`, o'quv dastur
   (curriculum programme) format, 2026/2027.

Everything below is traceable to those two files. Claims not traceable to them are marked
**[INFERRED]** or **[PRODUCT REQUIREMENT]** (from the build brief, not from AIFU).

---

## 1. Why two documents are enough to design the generic model

They are different document *genres* (a syllabus vs a curriculum programme), from different
faculties (Business/Economics vs ICT/Mathematics), different years, different credit loads,
and different session mixes. Yet they share an identical skeleton. That is exactly the
signal needed: the skeleton is an **institutional standard**, not a subject quirk.

| Structural element | `IAKTT1105` | `DAS1110` | Modelled as |
|---|---|---|---|
| Subject code | `IAKTT1105` | `DAS1110` | `subjects.code` (unique) |
| Knowledge area | `400000 Biznes, boshqaruv va huquq` | `600000 AKT`, `500000 Tabiiy fanlar` | `academic_programmes.knowledge_area_code` |
| Education area | `410000 Biznes va boshqaruv` | `610000 AKT`, `540000 Matematika` | `academic_programmes.education_area_code` |
| Programme(s) | `60410100 Iqtisodiyot` | `60610600 Dasturiy injiniring`, `70540101 Matematika` | `subject_programmes` (M:N) |
| Curriculum ordinal | — | `1.09` | `subject_programmes.curriculum_ordinal` |
| Subject type | `Majburiy` | `Majburiy` | `subjects.subject_kind` = `MANDATORY` |
| Instruction language | `O'zbek` | `O'zbek` | `courses.instruction_language` |
| Academic year | `2025/2026` | `2026-2027` | `academic_years.code` |
| Semester(s) | 1 | 1 **and** 2 | `terms` + one `course` per term |
| Credits | 5 | 6 (sem 1) + 5 (sem 2) | `courses.credits` — **per term, not per subject** |
| Total hours | 150 | 330 (180 + 150) | `courses.total_hours` |
| Weekly hours | — | 4 | `courses.weekly_contact_hours` |
| Session breakdown | Lecture 30 / Seminar 30 / Independent 90 | Lecture 60 / Practical 60 / Independent 210 | `course_session_allocations` |
| Assessment form | `Imtihon`, mixed oral+written | written final | `courses.assessment_form` |
| Department | `Aniq fanlar` kafedrasi | `Aniq fanlar` kafedrasi | `departments` |
| Teacher + contact | Akbarov B., phone, email, office hours `Se/Pa 14:00-16:00` | Akbarov B., Zoxidov A. | `teacher_profiles`, `course_teachers` |
| Reviewers | 2 named (O'zMU) | 2 named (O'zMU professors) | `syllabus_revisions.reviewers` (jsonb) |
| Approval trail | kafedra protocol + university Kengash protocol | same | `syllabus_revisions` (approval state + protocol no/date) |
| Aim / tasks | `Fan maqsadi` + `Fanning vazifasi` | same | `subject_translations.aim` / `.objectives` |
| Prerequisites | prose: maths basics, computer skills | **coded**: `(ALG112318) Algebra` | `subject_prerequisites` (+ free-text note) |
| Learning outcomes | `TN1..TN3`, split Bilim/Ko'nikma | `TN1..TN9`, split Kompetensiya/Ko'nikma | `learning_outcomes` (code + category) |
| Topic lists | M×15, S×15, MT×31 | M×20, A×17, MT×13 | `lessons` (typed by session type, with hours) |
| Literature | 3 primary + 5 supplementary + 8 web | 5 primary + 2 supplementary + 2 web | `course_materials` (typed) |
| Grade bands | — | 90-100→5, 80-89→4, 60-79→3, 0-59→2 | `grade_scales` + `grade_scale_bands` |
| Rubric | JN 30 (10+10+10) / ON 30 / YaN 40 | — | `assessment_components` (nested) |
| Benchmarking | QS/THE Top-300 mapping table w/ URLs | — | `course_materials` type `EXTERNAL_COURSE` |

**Conclusion: `Subject ≠ Course`.** `DAS1110` alone settles it — one subject, two semesters,
two different credit values and two different topic lists. A design that hangs lessons off
"Subject" would already be broken by the second document we received.

---

## 2. The recurring academic pattern (the real domain)

Both documents describe learning as **parallel tracks of typed sessions**, each with an
hour budget, converging on a **weighted, gated assessment**:

```
Subject (fan, stable catalogue entity)
 └── delivered in Term  ──►  Course (credits, hours, language, teachers)
        ├── LECTURE track          (ma'ruza)              theory topics, 2-6 h each
        ├── SEMINAR track          (seminar)              discussion topics
        ├── PRACTICAL track        (amaliy mashg'ulot)    hands-on topics
        ├── LABORATORY track       (laboratoriya)         present in the form, unused in both docs
        └── INDEPENDENT track      (mustaqil ta'lim)      largest budget: 60% / 64% of hours
                                    └── variant-based tasks, 1/3 must be cooperative
        └── Assessment: JN (continuous) + ON (midterm) + YaN (final) = 100 → 5-point band
```

Five findings that drove the design:

**F1 — Independent study dominates.** 90/150 (60%) and 210/330 (64%) of hours are
`mustaqil ta'lim`. A platform that models only classroom lessons would digitize the
*minority* of the curriculum. This is the strongest argument that the product's real job is
hosting self-paced content + auto-graded practice. `lessons.session_type` therefore includes
`INDEPENDENT_STUDY`, and independent topics are first-class lessons with their own hours,
not footnotes.

**F2 — A session type is a property of a lesson, not a separate subsystem.** `IAKTT1105`
seminar S7 ("Big Data qayta ishlash texnologiyalari") and self-study MT9 are the *same
topic* in different modes. So: one `lessons` table, discriminated by `session_type`, and
topics may repeat across tracks. Modelling lectures/seminars/labs as different entities
would have produced three near-identical trees.

**F3 — Hours are the institutional currency.** Every topic row carries hours (2, 4, 6, 10,
20, 30) and the totals must reconcile (30+30+90=150; 60+60+210=330). Teachers and the
o'quv-uslubiy bo'lim will check this. So `lessons.allocated_hours` exists, and
`course_session_allocations.allocated_hours` is the declared budget, with a validator that
compares declared vs sum-of-lessons and warns on mismatch (warn, not block: a draft course
is legitimately incomplete).

**F4 — Assessment is weighted, nested, and gated.** `IAKTT1105` is explicit:
`YaB = JN + ON + YaN` where JN(30) = attendance(10) + practical activity(10) + independent
work(10), ON = 30, YaN = 40. Then the gate: *a student who fails to earn at least 60% of the
points allocated to JN and to ON is not admitted to the final*; and *a student who fails to
earn 60% of the 40 final points is an academic debtor (`akademik qarzdor`)*. This is a real
business rule with real consequences, so:
- `assessment_components` is a **tree** (`parent_component_id`) with `max_points`,
  `min_pass_percent`, `display_order`;
- `enrollment` carries derived `final_score` + admission/debt state;
- the gate is computed, auditable, and never silently overwritten.

**F5 — Independent work is variant-based and partly cooperative.** `DAS1110` footnote:
*"mustaqil ish topshiriqlari variativ tavsifga ega bo'lishi lozim"* and *"1/3 qismi kichik
guruhlarda hamkorlikda ishlash (kooperativlik)ga mo'ljallangan bo'lishi kerak"*. Two
capabilities follow: per-student task **variants**, and **group** submissions. Variants are
supported now (`activities.variant_strategy`, `activity_variants`, deterministic
per-student assignment). Group submission is designed-for but **not implemented** in the
foundation — see `PENDING-INPUTS.md` §5.

---

## 3. Why the content model must be generic (and how these docs prove it)

Compare what the two subjects actually need:

| | `IAKTT1105` (Economics ICT) | `DAS1110` (Programming) |
|---|---|---|
| Dominant activity | Excel/PowerPoint exercises, case studies, crypto exercises, tax-report filing | write/compile/debug programs, algorithms, OOP |
| Practical artifact | a spreadsheet, a presentation, a filled form | source code + test output |
| Auto-gradable? | partly (quiz), mostly file/manual | yes (tests) |
| Needs code editor | only for MT10-MT13 (algorithms/programming basics) | always |
| Needs diagrams | yes (`Hisoblash jarayonlarining grafik tasviri` — flowcharts) | yes (flowcharts) |
| External benchmarks | yes, 6 QS/THE course links | no |

One subject in hand already needs *five* practical shapes (spreadsheet, presentation, case
study, quiz, file upload) and the other needs auto-graded code. Hardcoding either would be
a dead end. Hence:

- `content_blocks` — typed, ordered, localized, JSONB payload per type. A lesson is never
  one text column.
- `activities` — a polymorphic graded "thing to do", with `activity_type` and a handler per
  type. **Adding Cybersecurity's "case study + practical task" or Networks' "configuration
  exercise" is a new `activity_type` + handler + data. Zero core schema change.**
- Nothing anywhere in the codebase branches on a subject code or subject name.

---

## 4. What the documents do NOT tell us

Listed with full honesty in `PENDING-INPUTS.md`. Headlines:
- 13+ subject syllabi absent ⇒ **no Cybersecurity / AI / Networks academic content created**.
- The one programming syllabus we have teaches **C++** (Visual Studio, pointers, templates),
  while the product brief requires **Python and Go** execution. Unresolved conflict, flagged.
- No module/week structure inside a semester: the docs give flat topic lists. Modules are a
  platform-level grouping the teacher decides; the seed data groups conservatively and says so.
- No student/teacher rosters, no group codes, no academic calendar dates.
- No Russian or English versions of any document — all supplied material is Uzbek only.

---

## 5. Preserved academic intent vs redesigned digital structure

Kept exactly as supplied: subject codes, credits, hour budgets, session types, learning
outcome codes and text, topic titles and their order, literature lists, grade bands,
the JN/ON/YaN weights and the 60% gates, department, teacher, reviewer and approval trail.

Redesigned for a digital experience (per brief §1, §54), academic objectives untouched:
- flat topic lists grouped into `modules` for navigation;
- each topic expanded into ordered `content_blocks` instead of a title-only row;
- `mustaqil ta'lim` topics become self-paced lessons with auto-graded practice, replacing
  "submit a notebook" with recorded, reviewable attempts;
- the `Talabalar bilimini baholash` prose rubric becomes machine-computable
  `assessment_components`;
- the QS/THE benchmarking table becomes clickable enrichment material attached to the exact
  topic it maps to, instead of an appendix.
