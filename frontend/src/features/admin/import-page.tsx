import { useState } from 'react'
import { Download, FileCheck2, FileUp, Upload } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { readSpreadsheet, recordsOf } from '@/shared/lib/spreadsheet-read'
import { downloadXlsx } from '@/shared/lib/xlsx'
import { Badge } from '@/shared/ui/badge'
import { Breadcrumb } from '@/shared/ui/breadcrumb'
import { Button } from '@/shared/ui/button'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Input } from '@/shared/ui/input'
import { Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow } from '@/shared/ui/table'
import { useImportStudents, type ImportResult, type ImportRow } from './users-queries'

/** The accepted spellings of each column, in the three languages and as people write them. */
const FIELDS = {
  fullName: ['F.I.Sh', 'FISh', 'Familiya ism', 'Full name', 'Name', 'ФИО', 'Ф.И.О.'],
  studentNumber: ['Talaba raqami', 'Student number', 'Student ID', 'ID', 'Номер студента', 'Номер зачётки'],
  groupCode: ['Guruh', 'Group', 'Группа'],
  email: ['Email', 'E-mail', 'Pochta', 'Почта'],
  phone: ['Telefon', 'Phone', 'Телефон'],
  username: ['Login', 'Username', 'Логин'],
  enrollmentYear: ['Qabul yili', 'Enrollment year', 'Год поступления'],
} as const

const OUTCOME_TONE = { CREATED: 'success', MOVED: 'info', UNCHANGED: 'neutral', FAILED: 'danger' } as const

function toRows(records: Record<keyof typeof FIELDS, string>[]): ImportRow[] {
  return records
    .filter((record) => record.fullName !== '' || record.studentNumber !== '')
    .map((record) => {
      const year = Number.parseInt(record.enrollmentYear, 10)
      return {
        fullName: record.fullName,
        studentNumber: record.studentNumber,
        ...(record.groupCode !== '' && { groupCode: record.groupCode }),
        ...(record.email !== '' && { email: record.email }),
        ...(record.phone !== '' && { phone: record.phone }),
        ...(record.username !== '' && { username: record.username }),
        ...(Number.isFinite(year) && { enrollmentYear: year }),
      }
    })
}

/**
 * A-05 — students from a spreadsheet: the file is read in the browser, checked by the server
 * without saving anything (a dry run, row by row), and imported only when the admin says so.
 */
export function ImportPage() {
  const { t } = useTranslation()
  const run = useImportStudents()
  const [fileName, setFileName] = useState<string>()
  const [rows, setRows] = useState<ImportRow[]>([])
  const [readError, setReadError] = useState<string>()
  const [password, setPassword] = useState('')
  const [results, setResults] = useState<{ dryRun: boolean; rows: ImportResult[] }>()

  const choose = (file: File) => {
    setFileName(file.name)
    setResults(undefined)
    setReadError(undefined)
    readSpreadsheet(file).then(
      (sheet) => {
        const parsed = toRows(recordsOf(sheet, FIELDS))
        setRows(parsed)
        if (parsed.length === 0) setReadError(t('admin.importNoRows'))
      },
      () => {
        setRows([])
        setReadError(t('admin.importUnreadable'))
      },
    )
  }

  const send = (dryRun: boolean) => {
    run.mutate(
      { rows, dryRun, initialPassword: password },
      {
        onSuccess: (outcome) => {
          setResults({ dryRun, rows: outcome })
        },
      },
    )
  }

  const failed = results?.rows.filter((row) => row.outcome === 'FAILED').length ?? 0
  const counts = results?.rows.reduce<Record<string, number>>((all, row) => {
    const key = row.outcome ?? 'FAILED'
    return { ...all, [key]: (all[key] ?? 0) + 1 }
  }, {})

  return (
    <div className="grid grid-cols-1 gap-6">
      <Breadcrumb
        items={[{ label: t('nav.adminUsers'), to: '/admin/users' }, { label: t('admin.importStudents') }]}
      />
      <h1 className="font-display text-display-lg text-text font-semibold">{t('admin.importStudents')}</h1>

      <Card>
        <CardHeader
          title={t('admin.importStep1')}
          description={t('admin.importStep1Body')}
          action={
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                downloadXlsx(t('admin.importTemplateName'), [
                  {
                    name: t('admin.importStudents'),
                    rows: [
                      ['F.I.Sh', 'Talaba raqami', 'Guruh', 'Email', 'Telefon', 'Qabul yili'],
                      ['Valiyev Ali', '2600001', 'BK-DI26/1', '', '', 2026],
                    ],
                  },
                ])
              }}
            >
              <Download aria-hidden className="size-4" />
              {t('admin.importTemplate')}
            </Button>
          }
        />
        <CardContent className="grid grid-cols-1 gap-3">
          <label className="border-border-strong bg-surface-sunken text-body text-text hover:bg-surface focus-within:outline-focus-ring grid cursor-pointer place-items-center gap-2 rounded-lg border border-dashed px-4 py-8 text-center focus-within:outline-2">
            <FileUp aria-hidden className="text-text-muted size-6" />
            <span className="font-medium">{fileName ?? t('admin.chooseFile')}</span>
            <span className="text-small text-text-muted">{t('admin.fileFormats')}</span>
            <input
              type="file"
              accept=".xlsx,.csv,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              className="sr-only"
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file !== undefined) choose(file)
              }}
            />
          </label>
          {readError !== undefined && (
            <p role="alert" className="text-small text-danger">
              {readError}
            </p>
          )}
          {rows.length > 0 && (
            <p role="status" className="text-small text-text">
              {t('admin.importRowsRead', { count: rows.length })}
            </p>
          )}
        </CardContent>
      </Card>

      {rows.length > 0 && (
        <Card>
          <CardHeader title={t('admin.importStep2')} description={t('admin.importStep2Body')} />
          <CardContent className="grid grid-cols-1 gap-4">
            <Input
              className="max-w-sm"
              label={t('admin.importPassword')}
              type="password"
              autoComplete="new-password"
              value={password}
              required
              onChange={(event) => {
                setPassword(event.target.value)
              }}
            />
            <div className="flex flex-wrap gap-2">
              <Button
                variant="outline"
                disabled={password.length < 8}
                loading={run.isPending && run.variables.dryRun}
                onClick={() => {
                  send(true)
                }}
              >
                <FileCheck2 aria-hidden className="size-4" />
                {t('admin.dryRun')}
              </Button>
              <Button
                disabled={
                  password.length < 8 ||
                  results === undefined ||
                  !results.dryRun ||
                  failed === results.rows.length
                }
                loading={run.isPending && !run.variables.dryRun}
                onClick={() => {
                  send(false)
                }}
              >
                <Upload aria-hidden className="size-4" />
                {t('admin.importNow')}
              </Button>
            </div>
            {run.isError && (
              <p
                role="alert"
                className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium"
              >
                {t(`errors.${toApiProblem(run.error).code}`)}
              </p>
            )}
          </CardContent>
        </Card>
      )}

      {results !== undefined && (
        <Card>
          <CardHeader
            title={results.dryRun ? t('admin.dryRunResult') : t('admin.importResult')}
            description={Object.entries(counts ?? {})
              .map(([outcome, count]) => `${t(`admin.outcome.${outcome}`)}: ${count}`)
              .join(' · ')}
          />
          <CardContent>
            <div className="overflow-x-auto">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableHeaderCell numeric>{t('admin.row')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.studentNumber')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.fullName')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.username')}</TableHeaderCell>
                    <TableHeaderCell>{t('admin.outcomeTitle')}</TableHeaderCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.rows.map((row) => (
                    <TableRow key={`${row.row ?? 0}-${row.studentNumber ?? ''}`}>
                      <TableCell numeric>{row.row}</TableCell>
                      <TableCell className="font-mono">{row.studentNumber}</TableCell>
                      <TableCell>{rows[(row.row ?? 1) - 1]?.fullName}</TableCell>
                      <TableCell className="font-mono">{row.username}</TableCell>
                      <TableCell>
                        <Badge tone={OUTCOME_TONE[row.outcome ?? 'FAILED']}>
                          {t(`admin.outcome.${row.outcome ?? 'FAILED'}`)}
                        </Badge>
                        {row.message !== undefined && row.message !== '' && (
                          <span className="text-caption text-text-muted block">{row.message}</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
