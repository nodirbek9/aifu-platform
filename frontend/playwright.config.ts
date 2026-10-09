import { defineConfig, devices } from '@playwright/test'

/**
 * Flows run against the real backend (http://localhost:8080 via the Vite proxy) —
 * no request interception. Serial workers: parallel logins of the same account would
 * trip the single-use refresh-token theft detection.
 *
 * The demo password is read from E2E_DEMO_PASSWORD (see docs/03-API-GUIDE.md §9).
 */
export default defineConfig({
  testDir: './e2e',
  globalSetup: './e2e/global-setup.ts',
  globalTeardown: './e2e/global-teardown.ts',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [['list']],
  use: {
    // E2E_BASE_URL points the flows at another build, e.g. the nginx image on :3000
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:4173',
    // A browser set to Uzbek: the app starts in Uzbek, as it does for most of its users.
    locale: 'uz-UZ',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      // Only for review screenshots (`npm run shots`); the flows run on desktop and phone.
      name: 'tablet',
      grep: /@shots/,
      use: { ...devices['Desktop Chrome'], viewport: { width: 768, height: 1024 }, hasTouch: true },
    },
    {
      name: 'desktop',
      use: { ...devices['Desktop Chrome'], viewport: { width: 1440, height: 900 } },
    },
    {
      name: 'phone',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 390, height: 844 },
        hasTouch: true,
        isMobile: true,
      },
    },
  ],
  webServer:
    process.env.E2E_BASE_URL === undefined
      ? {
          // the production build, not the dev server: no on-the-fly module optimisation reloading a
          // page in the middle of a flow, and the code that ships is the code that is tested
          command: 'npm run build && npm run preview',
          url: 'http://localhost:4173',
          reuseExistingServer: true,
          timeout: 120_000,
        }
      : undefined,
})
