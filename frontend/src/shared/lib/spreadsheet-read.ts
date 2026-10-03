/**
 * Reads a spreadsheet chosen in the browser — .xlsx or .csv — into rows of text, without a
 * library: the .xlsx is a ZIP whose parts are inflated with the browser's DecompressionStream,
 * and the first sheet's cells are read from its XML. Formulas are not evaluated; their last
 * computed value (what Excel saved) is used.
 */

export type Rows = string[][]

// ------------------------------------------------------------------ CSV

/** CSV as Excel writes it: quoted fields with doubled quotes; comma or semicolon, whichever the header uses. */
export function parseCsv(text: string): Rows {
  // a byte-order mark Excel puts before the header is not part of the first name
  const source = text.charCodeAt(0) === 0xfeff ? text.slice(1) : text
  const firstLine = source.split(/\r?\n/, 1)[0] ?? ''
  const delimiter = (firstLine.match(/;/g) ?? []).length > (firstLine.match(/,/g) ?? []).length ? ';' : ','
  const rows: Rows = []
  let row: string[] = []
  let field = ''
  let quoted = false
  for (let i = 0; i < source.length; i += 1) {
    const char = source.charAt(i)
    if (quoted) {
      if (char === '"' && source.charAt(i + 1) === '"') {
        field += '"'
        i += 1
      } else if (char === '"') {
        quoted = false
      } else {
        field += char
      }
    } else if (char === '"') {
      quoted = true
    } else if (char === delimiter) {
      row.push(field)
      field = ''
    } else if (char === '\n' || char === '\r') {
      if (char === '\r' && source.charAt(i + 1) === '\n') i += 1
      row.push(field)
      rows.push(row)
      row = []
      field = ''
    } else {
      field += char
    }
  }
  if (field !== '' || row.length > 0) {
    row.push(field)
    rows.push(row)
  }
  return rows.filter((cells) => cells.some((cell) => cell.trim() !== ''))
}

// ------------------------------------------------------------------ ZIP

type Entry = { name: string; method: number; compressedSize: number; offset: number }

function entries(zip: Uint8Array): Entry[] {
  const view = new DataView(zip.buffer, zip.byteOffset, zip.byteLength)
  let end = -1
  for (let i = zip.length - 22; i >= Math.max(0, zip.length - 65_557); i -= 1) {
    if (view.getUint32(i, true) === 0x06054b50) {
      end = i
      break
    }
  }
  if (end < 0) throw new Error('Not a ZIP file')
  const count = view.getUint16(end + 10, true)
  let pointer = view.getUint32(end + 16, true)
  const decoder = new TextDecoder()
  const list: Entry[] = []
  for (let n = 0; n < count; n += 1) {
    if (view.getUint32(pointer, true) !== 0x02014b50) throw new Error('Broken ZIP directory')
    const method = view.getUint16(pointer + 10, true)
    const compressedSize = view.getUint32(pointer + 20, true)
    const nameLength = view.getUint16(pointer + 28, true)
    const extraLength = view.getUint16(pointer + 30, true)
    const commentLength = view.getUint16(pointer + 32, true)
    const offset = view.getUint32(pointer + 42, true)
    const name = decoder.decode(zip.subarray(pointer + 46, pointer + 46 + nameLength))
    list.push({ name, method, compressedSize, offset })
    pointer += 46 + nameLength + extraLength + commentLength
  }
  return list
}

async function inflate(data: Uint8Array): Promise<Uint8Array> {
  const stream = new Blob([data.slice().buffer]).stream().pipeThrough(new DecompressionStream('deflate-raw'))
  return new Uint8Array(await new Response(stream).arrayBuffer())
}

async function readEntry(zip: Uint8Array, entry: Entry): Promise<string> {
  const view = new DataView(zip.buffer, zip.byteOffset, zip.byteLength)
  const nameLength = view.getUint16(entry.offset + 26, true)
  const extraLength = view.getUint16(entry.offset + 28, true)
  const start = entry.offset + 30 + nameLength + extraLength
  const data = zip.subarray(start, start + entry.compressedSize)
  const bytes = entry.method === 0 ? data : await inflate(data)
  return new TextDecoder().decode(bytes)
}

// ------------------------------------------------------------------ SpreadsheetML

function unescapeXml(text: string): string {
  return text
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#(\d+);/g, (_match, code: string) => String.fromCodePoint(Number(code)))
    .replace(/&#x([0-9a-f]+);/gi, (_match, code: string) => String.fromCodePoint(Number.parseInt(code, 16)))
    .replace(/&amp;/g, '&')
}

/** The text of every <t> inside a fragment (rich text runs included), joined. */
function textOf(fragment: string): string {
  return [...fragment.matchAll(/<t(?:\s[^>]*)?>([\s\S]*?)<\/t>/g)]
    .map((match) => unescapeXml(match[1] ?? ''))
    .join('')
}

/** "C12" → 2 (the zero-based column). */
function columnIndex(reference: string): number {
  const letters = /^[A-Z]+/.exec(reference)?.[0] ?? 'A'
  let index = 0
  for (const letter of letters) index = index * 26 + (letter.charCodeAt(0) - 64)
  return index - 1
}

function sheetRows(xml: string, shared: readonly string[]): Rows {
  const rows: Rows = []
  for (const rowMatch of xml.matchAll(/<row\b[^>]*>([\s\S]*?)<\/row>/g)) {
    const row: string[] = []
    for (const cell of (rowMatch[1] ?? '').matchAll(/<c\b([^>]*?)(?:\/>|>([\s\S]*?)<\/c>)/g)) {
      const attributes = cell[1] ?? ''
      const body = cell[2] ?? ''
      const reference = /\br="([A-Z]+\d+)"/.exec(attributes)?.[1]
      const type = /\bt="(\w+)"/.exec(attributes)?.[1]
      const value = /<v>([\s\S]*?)<\/v>/.exec(body)?.[1]
      let text = ''
      if (type === 's' && value !== undefined) text = shared[Number(value)] ?? ''
      else if (type === 'inlineStr') text = textOf(body)
      else if (value !== undefined) text = unescapeXml(value)
      const index = reference === undefined ? row.length : columnIndex(reference)
      while (row.length < index) row.push('')
      row[index] = text
    }
    rows.push(row)
  }
  return rows.filter((cells) => cells.some((cell) => cell.trim() !== ''))
}

/** The rows of the first sheet of an .xlsx file. */
export async function readXlsx(bytes: Uint8Array): Promise<Rows> {
  const list = entries(bytes)
  const find = (name: string) => list.find((entry) => entry.name === name)
  const sharedEntry = find('xl/sharedStrings.xml')
  const shared =
    sharedEntry === undefined
      ? []
      : [...(await readEntry(bytes, sharedEntry)).matchAll(/<si>([\s\S]*?)<\/si>/g)].map((match) =>
          textOf(match[1] ?? ''),
        )
  const sheets = list
    .filter((entry) => /^xl\/worksheets\/sheet\d+\.xml$/.test(entry.name))
    .sort((a, b) => Number(/\d+/.exec(a.name)?.[0]) - Number(/\d+/.exec(b.name)?.[0]))
  const first = sheets[0]
  if (first === undefined) throw new Error('The workbook has no sheet')
  return sheetRows(await readEntry(bytes, first), shared)
}

/** A chosen file's rows: .xlsx through the reader above, anything else as CSV text. */
export async function readSpreadsheet(file: File): Promise<Rows> {
  const bytes = new Uint8Array(await file.arrayBuffer())
  const isZip = bytes[0] === 0x50 && bytes[1] === 0x4b
  return isZip ? readXlsx(bytes) : parseCsv(new TextDecoder().decode(bytes))
}

/**
 * Turns rows with a header into records keyed by the header's names, matched loosely: the
 * caller lists the accepted spellings of each field (in any language and case).
 */
export function recordsOf<K extends string>(
  rows: Rows,
  fields: Record<K, readonly string[]>,
): Record<K, string>[] {
  const [header, ...body] = rows
  if (header === undefined) return []
  const normalize = (text: string) =>
    text
      .trim()
      .toLowerCase()
      .replace(/[\s_ʻʼ'’-]+/g, '')
  const positions = Object.fromEntries(
    (Object.entries(fields) as [K, readonly string[]][]).map(([key, names]) => [
      key,
      header.findIndex((cell) => names.map(normalize).includes(normalize(cell))),
    ]),
  ) as Record<K, number>
  return body.map(
    (cells) =>
      Object.fromEntries(
        (Object.keys(fields) as K[]).map((key) => [
          key,
          positions[key] < 0 ? '' : (cells[positions[key]] ?? '').trim(),
        ]),
      ) as Record<K, string>,
  )
}
