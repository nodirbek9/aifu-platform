// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { buildXlsx, columnName, crc32, zipStore } from './xlsx'

const decoder = new TextDecoder()

describe('the xlsx writer', () => {
  it('names columns as Excel does', () => {
    expect(columnName(0)).toBe('A')
    expect(columnName(25)).toBe('Z')
    expect(columnName(26)).toBe('AA')
    expect(columnName(701)).toBe('ZZ')
    expect(columnName(702)).toBe('AAA')
  })

  it('computes the CRC-32 every zip reader checks', () => {
    expect(crc32(new TextEncoder().encode('123456789'))).toBe(0xcbf43926)
  })

  it('writes a zip with a local header per file and the end record', () => {
    const zip = zipStore([['a.txt', new TextEncoder().encode('hi')]])
    const view = new DataView(zip.buffer)
    expect(view.getUint32(0, true)).toBe(0x04034b50)
    expect(view.getUint32(zip.length - 22, true)).toBe(0x06054b50)
    expect(view.getUint16(zip.length - 22 + 10, true)).toBe(1)
  })

  it('keeps text as text and numbers as numbers, escaped, Uzbek letters intact', () => {
    const zip = buildXlsx([
      {
        name: 'Davomat: BK-DI26/1',
        rows: [
          ['F.I.Sh', 'Foiz'],
          ['Oʻrinboyev <A&B>', 75],
        ],
      },
    ])
    const text = decoder.decode(zip)
    expect(text).toContain('<v>75</v>')
    expect(text).toContain('Oʻrinboyev &lt;A&amp;B&gt;')
    // a sheet name may not contain : or /
    expect(text).toContain('name="Davomat  BK-DI26 1"')
    expect(text).toContain('state="frozen"')
  })
})
