import { useState } from 'react'
import { Download, ExternalLink, FileText, Maximize2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { toApiProblem } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { useObjectUrl } from '@/shared/lib/use-object-url'
import { useSharedPreference } from '@/shared/lib/use-shared-preference'
import { Button } from '@/shared/ui/button'
import { Callout } from '@/shared/ui/callout'
import { CodeBlock } from '@/shared/ui/code-block'
import { Dialog, DialogContent, DialogTitle } from '@/shared/ui/dialog'
import { RichText } from '@/shared/ui/rich-text'
import { Skeleton } from '@/shared/ui/skeleton'
import { headingPrefix, type CodeExample, type LessonBlock, type MediaKind } from './model'
import { useFileBlobQuery } from './queries'

const CODE_LANGUAGE_KEY = 'aifu-code-language'
const LANGUAGE_NAME: Record<string, string> = { python: 'Python', java: 'Java', cpp: 'C++' }

function languageName(code: string): string {
  return LANGUAGE_NAME[code] ?? code
}

/** Renders the blocks of a lesson in order. References to practice are shown by the page, at the end. */
export function LessonBlocks({ blocks }: { blocks: readonly LessonBlock[] }) {
  return (
    <div className="grid gap-7">
      {blocks.map((block) => (
        <Block key={block.id} block={block} />
      ))}
    </div>
  )
}

function Block({ block }: { block: LessonBlock }) {
  const { t } = useTranslation()
  switch (block.kind) {
    case 'text':
      return block.caseStudy ? (
        <section className="border-border bg-surface-raised rounded-lg border px-5 py-4">
          <p className="text-caption text-text-muted mb-2 font-medium uppercase">
            {t('activityType.CASE_STUDY')}
          </p>
          <RichText html={block.html} headingIdPrefix={headingPrefix(block.id)} className="text-lead" />
        </section>
      ) : (
        <RichText html={block.html} headingIdPrefix={headingPrefix(block.id)} className="text-lead" />
      )
    case 'callout':
      return (
        <Callout variant={block.variant}>
          <RichText html={block.html} className="text-body font-sans" />
        </Callout>
      )
    case 'code':
      return <CodeExamples examples={block.examples} />
    case 'media':
      return <MediaBlock media={block.media} fileId={block.fileId} caption={block.caption} />
    case 'link':
      return (
        <a
          href={block.url}
          target="_blank"
          rel="noopener noreferrer"
          className={cn(
            'border-border bg-surface-raised flex items-start gap-3 rounded-lg border px-4 py-3',
            'duration-fast ease-standard hover:border-border-strong transition-[border-color]',
            'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
          )}
        >
          <ExternalLink aria-hidden className="text-text-muted mt-0.5 size-5 shrink-0" />
          <span className="grid min-w-0 gap-0.5">
            <span className="text-body text-primary-hover font-medium">{block.label}</span>
            {block.note !== undefined && <span className="text-small text-text-muted">{block.note}</span>}
            <span className="text-caption text-text-muted truncate tracking-normal">
              {new URL(block.url).hostname} · {t('lesson.opensInNewTab')}
            </span>
          </span>
        </a>
      )
    case 'embed':
      return (
        <figure className="grid gap-2">
          <div className="border-border aspect-video overflow-hidden rounded-lg border">
            <iframe
              src={block.src}
              title={block.caption ?? t('lesson.video')}
              loading="lazy"
              allow="accelerometer; encrypted-media; gyroscope; picture-in-picture; fullscreen"
              referrerPolicy="strict-origin-when-cross-origin"
              className="size-full"
            />
          </div>
          {block.caption !== undefined && <Caption>{block.caption}</Caption>}
        </figure>
      )
    case 'ref':
      return null
  }
}

function Caption({ children }: { children: string }) {
  return <figcaption className="text-small text-text-muted text-center">{children}</figcaption>
}

/** One example in one or several languages. The chosen language is remembered across the app. */
function CodeExamples({ examples }: { examples: readonly CodeExample[] }) {
  const { t } = useTranslation()
  const [preferred, setPreferred] = useSharedPreference(CODE_LANGUAGE_KEY)
  const shown = examples.find((example) => example.language === preferred) ?? examples[0]
  if (shown === undefined) return null

  return (
    <figure className="grid gap-3">
      {examples.length > 1 && (
        <div role="tablist" aria-label={t('lesson.codeLanguage')} className="flex gap-1">
          {examples.map((example) => {
            const selected = example.language === shown.language
            return (
              <button
                key={example.language}
                type="button"
                role="tab"
                aria-selected={selected}
                onClick={() => {
                  setPreferred(example.language)
                }}
                className={cn(
                  'text-small rounded-sm px-3 py-1.5 font-medium',
                  'duration-fast ease-standard transition-[background-color,color]',
                  'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                  selected ? 'bg-primary text-primary-foreground' : 'text-text-muted hover:bg-surface-sunken',
                )}
              >
                {languageName(example.language)}
              </button>
            )
          })}
        </div>
      )}
      <CodeBlock
        code={shown.code}
        language={languageName(shown.language)}
        copyLabel={t('lesson.copy')}
        copiedLabel={t('lesson.copied')}
      />
      {shown.explanation !== undefined && (
        <figcaption>
          <RichText html={shown.explanation} className="text-body text-text-muted" />
        </figcaption>
      )}
    </figure>
  )
}

type MediaBlockProps = { media: MediaKind; fileId: string; caption: string | undefined }

function MediaBlock({ media, fileId, caption }: MediaBlockProps) {
  const { t } = useTranslation()
  const file = useFileBlobQuery(fileId)
  const url = useObjectUrl(file.data?.blob)
  const [zoomed, setZoomed] = useState(false)

  if (file.isError) {
    return (
      <div
        role="alert"
        className="border-border text-small text-text-muted flex items-center gap-3 rounded-lg border px-4 py-3"
      >
        <span className="flex-1">
          {t('lesson.fileError')} — {t(`errors.${toApiProblem(file.error).code}`)}
        </span>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            void file.refetch()
          }}
        >
          {t('common.retry')}
        </Button>
      </div>
    )
  }
  if (file.isPending || url === undefined) {
    return <Skeleton className={media === 'AUDIO' || media === 'FILE' ? 'h-14' : 'aspect-video'} />
  }

  if (media === 'FILE') {
    return (
      <a
        href={url}
        download={file.data.name}
        className={cn(
          'border-border bg-surface-raised flex items-center gap-3 rounded-lg border px-4 py-3',
          'duration-fast ease-standard hover:border-border-strong transition-[border-color]',
          'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
        )}
      >
        <FileText aria-hidden className="text-text-muted size-5 shrink-0" />
        <span className="grid min-w-0 flex-1">
          <span className="text-body text-text truncate font-medium">{caption ?? file.data.name}</span>
          <span className="text-caption text-text-muted truncate tracking-normal">{file.data.name}</span>
        </span>
        <Download aria-hidden className="text-text-muted size-5 shrink-0" />
        <span className="sr-only">{t('lesson.download')}</span>
      </a>
    )
  }
  if (media === 'AUDIO') {
    return (
      <figure className="grid gap-2">
        <audio controls src={url} className="w-full" />
        {caption !== undefined && <Caption>{caption}</Caption>}
      </figure>
    )
  }
  if (media === 'VIDEO') {
    return (
      <figure className="grid gap-2">
        <video controls src={url} className="border-border w-full rounded-lg border" />
        {caption !== undefined && <Caption>{caption}</Caption>}
      </figure>
    )
  }
  // IMAGE and DIAGRAM: shown in the column, enlarged on click.
  return (
    <figure className="grid gap-2">
      <button
        type="button"
        onClick={() => {
          setZoomed(true)
        }}
        aria-label={t('lesson.zoom')}
        className="group focus-visible:outline-focus-ring relative rounded-lg focus-visible:outline-2 focus-visible:outline-offset-2"
      >
        <img src={url} alt={caption ?? ''} className="bg-surface-raised w-full rounded-lg" />
        <span className="bg-surface-raised/90 text-text-muted duration-fast absolute top-2 right-2 flex size-8 items-center justify-center rounded-sm opacity-0 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100">
          <Maximize2 aria-hidden className="size-4" />
        </span>
      </button>
      {caption !== undefined && <Caption>{caption}</Caption>}
      <Dialog open={zoomed} onOpenChange={setZoomed}>
        <DialogContent className="max-w-5xl">
          <DialogTitle className="sr-only">{caption ?? t('lesson.zoom')}</DialogTitle>
          <img src={url} alt={caption ?? ''} className="w-full rounded-sm" />
        </DialogContent>
      </Dialog>
    </figure>
  )
}
