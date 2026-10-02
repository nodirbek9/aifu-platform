import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { IconButton } from './icon-button'

export type PaginationProps = {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
  className?: string
}

export function Pagination({ page, totalPages, onPageChange, className }: PaginationProps) {
  const { t } = useTranslation()
  if (totalPages <= 1) return null
  return (
    <nav aria-label="pagination" className={cn('flex items-center justify-between gap-3', className)}>
      <span className="text-small text-text-muted tabular-nums">
        {page + 1} / {totalPages}
      </span>
      <div className="flex items-center gap-1">
        <IconButton
          aria-label={t('common.previous')}
          variant="outline"
          size="sm"
          disabled={page <= 0}
          onClick={() => {
            onPageChange(page - 1)
          }}
        >
          <ChevronLeft aria-hidden className="size-4" />
        </IconButton>
        <IconButton
          aria-label={t('common.next')}
          variant="outline"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => {
            onPageChange(page + 1)
          }}
        >
          <ChevronRight aria-hidden className="size-4" />
        </IconButton>
      </div>
    </nav>
  )
}
