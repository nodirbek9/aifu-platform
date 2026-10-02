import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router'
import { cn } from '@/shared/lib/cn'

export type BreadcrumbItem = {
  label: string
  to?: string
}

export type BreadcrumbProps = {
  items: readonly BreadcrumbItem[]
  className?: string
}

export function Breadcrumb({ items, className }: BreadcrumbProps) {
  return (
    <nav aria-label="breadcrumb" className={cn('text-small flex items-center gap-1.5', className)}>
      {items.map((item, index) => {
        const last = index === items.length - 1
        return (
          <span key={`${item.label}-${index}`} className="flex items-center gap-1.5">
            {index > 0 && <ChevronRight aria-hidden className="text-text-muted/60 size-3.5" />}
            {item.to !== undefined && !last ? (
              <Link to={item.to} className="text-text-muted duration-fast hover:text-text transition-colors">
                {item.label}
              </Link>
            ) : (
              <span
                aria-current={last ? 'page' : undefined}
                className={last ? 'text-text font-medium' : 'text-text-muted'}
              >
                {item.label}
              </span>
            )}
          </span>
        )
      })}
    </nav>
  )
}
