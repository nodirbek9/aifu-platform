// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { parseCsv, readXlsx, recordsOf } from './spreadsheet-read'
import { buildXlsx, crc32 } from './xlsx'

const encoder = new TextEncoder()

async function deflate(data: Uint8Array): Promise<Uint8Array> {
  const stream = new Blob([data.slice().buffer]).stream().pipeThrough(new CompressionStream('deflate-raw'))
  return new Uint8Array(await new Response(stream).arrayBuffer())
}

/** A ZIP with compressed (method 8) entries, as Excel itself writes them. */
async function zipDeflated(files: [string, string][]): Promise<Uint8Array> {
  const parts: Uint8Array[] = []
  const directory: Uint8Array[] = []
  let offset = 0
  for (const [name, text] of files) {
    const raw = encoder.encode(text)
    const data = await deflate(raw)
    const nameBytes = encoder.encode(name)
    const local = new DataView(new ArrayBuffer(30))
    local.setUint32(0, 0x04034b50, true)
    local.setUint16(8, 8, true)
    local.setUint32(14, crc32(raw), true)
    local.setUint32(18, data.length, true)
    local.setUint32(22, raw.length, true)
    local.setUint16(26, nameBytes.length, true)
    const central = new DataView(new ArrayBuffer(46))
    central.setUint32(0, 0x02014b50, true)
    central.setUint16(10, 8, true)
    central.setUint32(16, crc32(raw), true)
    central.setUint32(20, data.length, true)
    central.setUint32(24, raw.length, true)
    central.setUint16(28, nameBytes.length, true)
    central.setUint32(42, offset, true)
    parts.push(new Uint8Array(local.buffer), nameBytes, data)
    directory.push(new Uint8Array(central.buffer), nameBytes)
    offset += 30 + nameBytes.length + data.length
  }
  const size = directory.reduce((sum, part) => sum + part.length, 0)
  const end = new DataView(new ArrayBuffer(22))
  end.setUint32(0, 0x06054b50, true)
  end.setUint16(8, files.length, true)
  end.setUint16(10, files.length, true)
  end.setUint32(12, size, true)
  end.setUint32(16, offset, true)
  const all = [...parts, ...directory, new Uint8Array(end.buffer)]
  const out = new Uint8Array(all.reduce((sum, part) => sum + part.length, 0))
  let at = 0
  for (const part of all) {
    out.set(part, at)
    at += part.length
  }
  return out
}

describe('reading spreadsheets', () => {
  it('reads CSV with quotes, doubled quotes, semicolons and a BOM', () => {
    expect(parseCsv('﻿F.I.Sh;Guruh\n"Karimov; Ali";BK-DI26/1\r\n"Say ""hi""";X\n\n')).toEqual([
      ['F.I.Sh', 'Guruh'],
      ['Karimov; Ali', 'BK-DI26/1'],
      ['Say "hi"', 'X'],
    ])
  })

  it('reads back what the writer wrote (stored entries)', async () => {
    const rows = await readXlsx(
      buildXlsx([
        {
          name: 'S',
          rows: [
            ['Ism', 'Ball'],
            ['Oʻgʻiloy & co', 87.5],
            [null, 3],
          ],
        },
      ]),
    )
    expect(rows).toEqual([
      ['Ism', 'Ball'],
      ['Oʻgʻiloy & co', '87.5'],
      ['', '3'],
    ])
  })

  it('reads compressed parts, shared strings, rich runs and skipped columns', async () => {
    const zip = await zipDeflated([
      [
        'xl/sharedStrings.xml',
        '<sst><si><t>Fan</t></si><si><r><t>Ma</t></r><r><t>ʼruza</t></r></si><si><t>DAS1110</t></si></sst>',
      ],
      [
        'xl/worksheets/sheet1.xml',
        '<worksheet><sheetData><row r="1"><c r="A1" t="s"><v>0</v></c><c r="C1" t="s"><v>1</v></c></row>' +
          '<row r="2"><c r="A2" t="s"><v>2</v></c><c r="C2"><v>30</v></c></row></sheetData></worksheet>',
      ],
    ])
    expect(await readXlsx(zip)).toEqual([
      ['Fan', '', 'Maʼruza'],
      ['DAS1110', '', '30'],
    ])
  })

  it('maps a header to fields by any accepted spelling', () => {
    const records = recordsOf(
      [
        ['Full name', 'Guruh', 'Talaba raqami'],
        ['Ali Valiyev', 'BK-DI26/1', '26001'],
      ],
      {
        fullName: ['F.I.Sh', 'full name'],
        group: ['guruh', 'group'],
        number: ['talaba raqami'],
        email: ['email'],
      },
    )
    expect(records).toEqual([{ fullName: 'Ali Valiyev', group: 'BK-DI26/1', number: '26001', email: '' }])
  })
})
