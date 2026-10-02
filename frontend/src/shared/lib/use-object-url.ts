import { useEffect, useState } from 'react'

/** An object URL for a blob, revoked when the blob changes or the component leaves. */
export function useObjectUrl(blob: Blob | undefined): string | undefined {
  const [url, setUrl] = useState<string>()
  useEffect(() => {
    if (blob === undefined) return
    const created = URL.createObjectURL(blob)
    // The URL is an external resource that must be created and revoked as a pair, in the effect.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setUrl(created)
    return () => {
      URL.revokeObjectURL(created)
    }
  }, [blob])
  return blob === undefined ? undefined : url
}
