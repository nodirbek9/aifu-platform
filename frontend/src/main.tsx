import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import './index.css'
import { i18nReady } from './shared/i18n'
import { Providers } from './app/providers'
import { router } from './app/router'

const root = document.getElementById('root')
if (root === null) throw new Error('The #root element is missing from index.html')

// a Russian or English reader gets the first paint in their language, not a flash of Uzbek
void i18nReady.then(() => {
  createRoot(root).render(
    <StrictMode>
      <Providers>
        <RouterProvider router={router} />
      </Providers>
    </StrictMode>,
  )
})
