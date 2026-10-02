import { createTV } from 'tailwind-variants'
import { twMergeConfig } from './cn'

/** `tv()` that merges classes with the same knowledge of our tokens as `cn()`. Use this one. */
export const tv = createTV({ twMergeConfig })
