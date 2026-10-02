import { clsx, type ClassValue } from 'clsx'
import { extendTailwindMerge } from 'tailwind-merge'

/**
 * What tailwind-merge must be told about our tokens. Without it, it takes a type-scale class
 * such as `text-body` for a text colour and silently drops the real colour next to it
 * (`text-primary-foreground text-body` → `text-body`).
 */
export const twMergeConfig = {
  extend: {
    classGroups: {
      'font-size': [
        { text: ['caption', 'small', 'body', 'lead', 'title', 'display-lg', 'display-xl', 'display-2xl'] },
      ],
      shadow: [{ shadow: ['menu', 'dialog', 'drag'] }],
    },
  },
}

const twMerge = extendTailwindMerge(twMergeConfig)

export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs))
}
