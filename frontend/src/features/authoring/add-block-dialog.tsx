import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { useLocale } from '@/shared/i18n/use-locale'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogTitle } from '@/shared/ui/dialog'
import { Input } from '@/shared/ui/input'
import { Select } from '@/shared/ui/select'
import type { RefChoice } from './block-editor'
import {
  acceptFor,
  BLOCK_GROUPS,
  initialSettings,
  isFileBlock,
  isRefBlock,
  videoIdFrom,
  type BlockType,
} from './model'
import { useCreateBlock, useUploadFile } from './queries'

type AddBlockDialogProps = {
  lessonId: string
  open: boolean
  onOpenChange: (open: boolean) => void
  refChoices: Partial<Record<BlockType, RefChoice[]>>
  onAdded: (blockId: string) => void
}

/** Adding a block: its type first, then what that type cannot exist without (a file, an address…). */
export function AddBlockDialog({ lessonId, open, onOpenChange, refChoices, onAdded }: AddBlockDialogProps) {
  const { t } = useTranslation()
  const locale = useLocale()
  const create = useCreateBlock(lessonId)
  const upload = useUploadFile()
  const [type, setType] = useState<BlockType>()
  const [url, setUrl] = useState('')
  const [video, setVideo] = useState('')
  const [provider, setProvider] = useState('YOUTUBE')
  const [refId, setRefId] = useState<string>()
  const [file, setFile] = useState<File>()

  const reset = () => {
    setType(undefined)
    setUrl('')
    setVideo('')
    setRefId(undefined)
    setFile(undefined)
    create.reset()
    upload.reset()
  }

  const ready =
    type !== undefined &&
    (type === 'LINK'
      ? /^https?:\/\/\S+$/i.test(url)
      : type === 'EMBED'
        ? video !== ''
        : isRefBlock(type)
          ? refId !== undefined
          : isFileBlock(type)
            ? file !== undefined
            : true)

  const add = async () => {
    if (type === undefined) return
    let settings = initialSettings(type)
    if (type === 'LINK') settings = { url }
    if (type === 'EMBED') settings = { provider, externalId: videoIdFrom(video) }
    if (isRefBlock(type) && refId !== undefined) settings = { refId }
    const fileId = isFileBlock(type) && file !== undefined ? (await upload.mutateAsync(file)).id : undefined
    const id = await create.mutateAsync({
      type,
      settings,
      visibility: type === 'LESSON_PLAN' ? 'TEACHER_ONLY' : 'ALL',
      ...(fileId !== undefined && { fileId }),
      ...(type === 'LINK' && { payload: { locale, value: { label: url } } }),
      ...(isRefBlock(type) && { payload: { locale, value: {} } }),
    })
    reset()
    onOpenChange(false)
    if (id !== undefined) onAdded(id)
  }

  const failure = create.error ?? upload.error

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) reset()
        onOpenChange(next)
      }}
    >
      <DialogContent className="max-w-2xl">
        <DialogTitle>{t('authoring.addBlock')}</DialogTitle>
        <DialogDescription>{t('authoring.addBlockBody')}</DialogDescription>
        <div className="grid max-h-[60vh] gap-4 overflow-y-auto">
          {BLOCK_GROUPS.map((group) => (
            <fieldset key={group.key} className="grid gap-2">
              <legend className="text-caption text-text-muted mb-1 font-medium uppercase">
                {t(`authoring.blockGroup.${group.key}`)}
              </legend>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {group.types.map((item) => (
                  <button
                    key={item}
                    type="button"
                    aria-pressed={type === item}
                    onClick={() => {
                      setType(item)
                    }}
                    className="border-border bg-surface-raised text-small text-text hover:border-border-strong aria-pressed:border-primary aria-pressed:bg-accent-soft focus-visible:outline-focus-ring rounded-md border px-3 py-2 text-left font-medium focus-visible:outline-2 focus-visible:outline-offset-2"
                  >
                    {t(`authoring.blockType.${item}`)}
                  </button>
                ))}
              </div>
            </fieldset>
          ))}
        </div>

        {type === 'LINK' && (
          <Input
            label={t('authoring.url')}
            type="url"
            value={url}
            placeholder="https://"
            onChange={(event) => {
              setUrl(event.target.value.trim())
            }}
          />
        )}
        {type === 'EMBED' && (
          <div className="grid gap-3 sm:grid-cols-2">
            <Select
              label={t('authoring.provider')}
              value={provider}
              onValueChange={setProvider}
              options={[
                { value: 'YOUTUBE', label: 'YouTube' },
                { value: 'VIMEO', label: 'Vimeo' },
              ]}
            />
            <Input
              label={t('authoring.videoAddress')}
              value={video}
              onChange={(event) => {
                setVideo(event.target.value)
              }}
            />
          </div>
        )}
        {type !== undefined && isRefBlock(type) && (
          <Select
            label={t(`authoring.refLabel.${type}`)}
            value={refId}
            placeholder={t('authoring.choose')}
            onValueChange={setRefId}
            options={refChoices[type] ?? []}
          />
        )}
        {type !== undefined && isFileBlock(type) && (
          <label className="grid gap-1.5">
            <span className="text-small text-text font-medium">{t('authoring.file')}</span>
            <input
              type="file"
              accept={acceptFor(type)}
              className="text-small text-text file:border-border-strong file:bg-surface-raised file:text-text file:mr-3 file:rounded-md file:border file:px-3 file:py-1.5"
              onChange={(event) => {
                setFile(event.target.files?.[0])
              }}
            />
          </label>
        )}

        {failure !== null && (
          <p role="alert" className="bg-danger-soft text-small text-danger rounded-sm px-3 py-2 font-medium">
            {t(`errors.${toApiProblem(failure).code}`)}
          </p>
        )}
        <DialogFooter>
          <Button
            disabled={!ready}
            loading={create.isPending || upload.isPending}
            onClick={() => {
              add().catch(() => undefined)
            }}
          >
            {t('authoring.add')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
