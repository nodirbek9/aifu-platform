import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import './index.css'
import './shared/i18n'
import { Providers } from './app/providers'
import { router } from './app/router'

const root = document.getElementById('root')
if (root === null) throw new Error('The #root element is missing from index.html')

createRoot(root).render(
  <StrictMode>
    <Providers>
      <RouterProvider router={router} />
    </Providers>
  </StrictMode>,
)
