import { useState } from 'react'
import { Archive, Copy, Pencil, Plus, Send, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { ConfirmDialog } from '@/features/authoring/confirm-dialog'
import { useAcademicYearsQuery, useTermsQuery } from '@/features/admin/reference-queries'
import { toApiProblem } from '@/shared/api/errors'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import {
  useArchiveTimetable,
  useCreateTimetable,
  useDeleteTimetable,
  usePublishTimetable,
  useRenameTimetable,
  type Timetable,
} from './queries'

const STATUS_TONE = { DRAFT: 'warning', PUBLISHED: 'success', ARCHIVED: 'neutral' } as const

type TimetableBarProps = {
  timetables: readonly Timetable[]
  selected: Timetable | undefined
  onSelect: (id: string) => void
}

/** A-02.1 — which timetable is on the table, its state beyond doubt, and what can be done with it. */
export function TimetableBar({ timetables, selected, onSelect }: TimetableBarProps) {
  const { t } = useTranslation()
  const [dialog, setDialog] = useState<'create' | 'copy' | 'rename' | 'publish' | 'archive' | 'delete'>()
  const id = selected?.id ?? ''
  const publish = usePublishTimetable(id)
  const archive = useArchiveTimetable(id)
  const remove = useDeleteTimetable(id)
  const status = selected?.status ?? 'DRAFT'
  const failure = publish.error ?? archive.error ?? remove.error

  return (
    <div className="grid grid-cols-1 gap-3" data-print-hidden>
      <div className="flex flex-wrap items-end gap-3">
        <Select
          className="w-80 max-w-full"
          label={t('builder.timetable')}
          value={selected?.id}
          placeholder={t('authoring.choose')}
          onValueChange={onSelect}
          options={timetables.map((item) => ({
            value: item.id ?? '',
            label: `${item.name ?? ''} · ${t(`builder.status.${item.status ?? 'DRAFT'}`)}`,
          }))}
        />
        {selected !== undefined && (
          <Badge tone={STATUS_TONE[status]} className="text-small mb-2.5 px-3 py-1">
            {t(`builder.status.${status}`)}
          </Badge>
        )}
        <div className="flex flex-wrap gap-2">
          <Button
            variant="outline"
            onClick={() => {
              setDialog('create')
            }}
          >
            <Plus aria-hidden className="size-4" />
            {t('builder.newDraft')}
          </Button>
          {selected !== undefined && status !== 'DRAFT' && (
            <Button
              variant="outline"
              onClick={() => {
                setDialog('copy')
              }}
            >
              <Copy aria-hidden className="size-4" />
              {t('builder.copyToDraft')}
            </Button>
          )}
          {selected !== undefined && status === 'DRAFT' && (
            <>
              <Button
                variant="ghost"
                onClick={() => {
                  setDialog('rename')
                }}
              >
                <Pencil aria-hidden className="size-4" />
                {t('authoring.rename')}
              </Button>
              <Button
                onClick={() => {
                  setDialog('publish')
                }}
              >
                <Send aria-hidden className="size-4" />
                {t('authoring.publish')}
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  setDialog('delete')
                }}
              >
                <Trash2 aria-hidden className="size-4" />
                {t('authoring.delete')}
              </Button>
            </>
          )}
          {selected !== undefined && status === 'PUBLISHED' && (
            <Button
              variant="ghost"
              onClick={() => {
                setDialog('archive')
              }}
            >
              <Archive aria-hidden className="size-4" />
              {t('admin.archive')}
            </Button>
          )}
        </div>
      </div>
      {status === 'PUBLISHED' && selected !== undefined && (
        <p className="bg-info-soft text-small text-info rounded-sm px-3 py-2">
          {t('builder.publishedReadOnly')}
        </p>
      )}
      {failure !== null && (
        <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
          {t(`errors.${toApiProblem(failure).code}`)}
        </p>
      )}

      {(dialog === 'create' || dialog === 'copy') && (
        <NewTimetableDialog
          copyFrom={dialog === 'copy' ? selected : undefined}
          timetables={timetables}
          onClose={(created) => {
            setDialog(undefined)
            if (created !== undefined) onSelect(created)
          }}
        />
      )}
      {dialog === 'rename' && selected !== undefined && (
        <RenameDialog
          timetable={selected}
          onClose={() => {
            setDialog(undefined)
          }}
        />
      )}
      <ConfirmDialog
        open={dialog === 'publish' || dialog === 'archive' || dialog === 'delete'}
        onOpenChange={(open) => {
          if (!open) setDialog(undefined)
        }}
        title={t(`builder.confirm.${dialog ?? 'publish'}Title`, { name: selected?.name ?? '' })}
        body={t(`builder.confirm.${dialog ?? 'publish'}Body`, { count: selected?.entryCount ?? 0 })}
        confirmLabel={
          dialog === 'delete'
            ? t('authoring.delete')
            : dialog === 'archive'
              ? t('admin.archive')
              : t('authoring.publish')
        }
        pending={publish.isPending || archive.isPending || remove.isPending}
        onConfirm={() => {
          const done = {
            onSettled: () => {
              setDialog(undefined)
            },
          }
          if (dialog === 'publish') publish.mutate(undefined, done)
          else if (dialog === 'archive') archive.mutate(undefined, done)
          else remove.mutate(undefined, done)
        }}
      />
    </div>
  )
}

function NewTimetableDialog({
  copyFrom,
  timetables,
  onClose,
}: {
  copyFrom: Timetable | undefined
  timetables: readonly Timetable[]
  onClose: (createdId?: string) => void
}) {
  const { t } = useTranslation()
  const years = useAcademicYearsQuery()
  const currentYear = years.data?.find((year) => year.current === true)?.id
  const terms = useTermsQuery(currentYear)
  const create = useCreateTimetable()
  const [termId, setTermId] = useState(copyFrom?.termId)
  const [source, setSource] = useState(copyFrom?.id ?? 'none')
  const [name, setName] = useState(
    copyFrom === undefined ? '' : t('builder.copyName', { name: copyFrom.name ?? '' }),
  )
  const chosenTerm = termId ?? terms.data?.[0]?.id
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent>
        <DialogTitle>{copyFrom === undefined ? t('builder.newDraft') : t('builder.copyToDraft')}</DialogTitle>
        <DialogDescription>{t('builder.newDraftBody')}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            if (chosenTerm === undefined || name.trim() === '') return
            create.mutate(
              { termId: chosenTerm, name: name.trim(), ...(source !== 'none' && { copyFromId: source }) },
              {
                onSuccess: (created) => {
                  onClose(created.id)
                },
              },
            )
          }}
        >
          <Select
            label={t('admin.term')}
            value={chosenTerm}
            placeholder={t('authoring.choose')}
            onValueChange={setTermId}
            options={(terms.data ?? []).map((term) => ({
              value: term.id ?? '',
              label: `${term.academicYearCode ?? ''} · ${term.name ?? ''}`,
            }))}
          />
          <Select
            label={t('builder.copyFrom')}
            value={source}
            onValueChange={setSource}
            options={[
              { value: 'none', label: t('builder.empty') },
              ...timetables.map((item) => ({ value: item.id ?? '', label: item.name ?? '' })),
            ]}
          />
          <Input
            label={t('admin.title')}
            value={name}
            onChange={(event) => {
              setName(event.target.value)
            }}
          />
          {create.isError && (
            <p role="alert" className="text-small text-danger">
              {t(`errors.${toApiProblem(create.error).code}`)}
            </p>
          )}
          <DialogFooter>
            <Button
              type="submit"
              disabled={name.trim() === '' || chosenTerm === undefined}
              loading={create.isPending}
            >
              {t('authoring.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function RenameDialog({ timetable, onClose }: { timetable: Timetable; onClose: () => void }) {
  const { t } = useTranslation()
  const rename = useRenameTimetable(timetable.id ?? '')
  const [name, setName] = useState(timetable.name ?? '')
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <DialogContent>
        <DialogTitle>{t('authoring.rename')}</DialogTitle>
        <DialogDescription>{timetable.name}</DialogDescription>
        <form
          className="grid grid-cols-1 gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            rename.mutate(name.trim(), { onSuccess: onClose })
          }}
        >
          <Input
            label={t('admin.title')}
            value={name}
            onChange={(event) => {
              setName(event.target.value)
            }}
          />
          <DialogFooter>
            <Button type="submit" disabled={name.trim() === ''} loading={rename.isPending}>
              {t('authoring.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
