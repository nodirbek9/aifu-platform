import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { i18n, i18nReady } from '@/shared/i18n'
import uz from '@/shared/i18n/uz.json'
import { ApiFailure } from '@/shared/api/errors'
import { WeekPreview } from './week-preview'
import type { OneOffChanges } from './one-off-changes'

const mocks = vi.hoisted(() => ({
  query: vi.fn(),
  retry: vi.fn(),
  form: vi.fn(),
}))
vi.mock('./queries', () => ({ useWeekPreviewQuery: mocks.query }))
vi.mock('./one-off-changes', () => ({
  OneOffChanges: ({
    initialKind,
    selectedDate,
    sourceClasses,
  }: React.ComponentProps<typeof OneOffChanges>) => {
    mocks.form({ initialKind, selectedDate, sourceClasses })
    return <div data-testid="single-class-form" />
  },
}))
vi.mock('@/features/timetable/week-view', () => ({
  WeekView: () => <div data-testid="week-content" />,
}))

beforeEach(async () => {
  await i18nReady
  await i18n.changeLanguage('uz')
  mocks.query.mockReturnValue({
    isLoading: false,
    isError: false,
    isSuccess: true,
    data: { days: [], slots: [] },
    refetch: mocks.retry,
  })
})
afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function open(overrides: Partial<React.ComponentProps<typeof WeekPreview>> = {}) {
  const onClose = vi.fn()
  const onCreateSource = vi.fn()
  const view = render(
    <WeekPreview
      timetableId="timetable"
      scope={{ kind: 'group', id: 'group' }}
      status="DRAFT"
      sourceClasses={[{ value: 'entry', label: 'Source class' }]}
      onCreateSource={onCreateSource}
      onClose={onClose}
      {...overrides}
    />,
  )
  return { ...view, onClose, onCreateSource }
}

describe('week preview entry point (7.5-F3)', () => {
  for (const status of ['DRAFT', 'PUBLISHED'] as const) {
    it(`opens the ADD form by keyboard for ${status} with the selected date and source choices`, async () => {
      open({ status })
      const user = userEvent.setup()
      fireEvent.change(screen.getByLabelText(uz.builder.weekOf), { target: { value: '2026-10-13' } })
      screen.getByRole('button', { name: uz.builder.addSingleClass }).focus()
      await user.keyboard('{Enter}')
      expect(screen.getByTestId('single-class-form')).toBeVisible()
      expect(mocks.form).toHaveBeenLastCalledWith({
        initialKind: 'ADD',
        selectedDate: '2026-10-13',
        sourceClasses: [{ value: 'entry', label: 'Source class' }],
      })
      fireEvent.change(screen.getByLabelText(uz.builder.weekOf), { target: { value: '2026-10-20' } })
      expect(mocks.form).toHaveBeenLastCalledWith(expect.objectContaining({ selectedDate: '2026-10-20' }))
      expect(mocks.query).toHaveBeenLastCalledWith(
        'timetable',
        { kind: 'group', id: 'group' },
        '2026-10-20',
        'uz',
        true,
      )
    })
  }

  for (const status of ['ARCHIVED', undefined] as const) {
    it(`does not offer mutations for status ${status}`, () => {
      open({ status })
      expect(screen.queryByRole('button', { name: uz.builder.addSingleClass })).not.toBeInTheDocument()
      expect(screen.getByTestId('week-content')).toBeVisible()
    })
  }

  for (const status of ['DRAFT', 'PUBLISHED'] as const) {
    it(`offers a next action when ${status} has no source classes`, async () => {
      const { onCreateSource } = open({ sourceClasses: [], status })
      expect(screen.getByText(uz.builder.noSourceClass)).toBeVisible()
      expect(screen.queryByRole('button', { name: uz.builder.addSingleClass })).not.toBeInTheDocument()
      await userEvent.setup().click(
        screen.getByRole('button', {
          name: status === 'DRAFT' ? uz.builder.addClass : uz.builder.copyToDraft,
        }),
      )
      expect(onCreateSource).toHaveBeenCalledOnce()
    })
  }

  it('shows a loading skeleton without stale content', () => {
    mocks.query.mockReturnValue({ isLoading: true, isSuccess: false, isError: false })
    const { container } = open()
    expect(container.ownerDocument.querySelector('.animate-pulse')).toBeInTheDocument()
    expect(screen.queryByTestId('week-content')).not.toBeInTheDocument()
  })

  it('shows the preview error and request ID and retries the query', async () => {
    mocks.query.mockReturnValue({
      isLoading: false,
      isError: true,
      isSuccess: false,
      error: new ApiFailure({
        status: 503,
        code: 'INTERNAL_ERROR',
        message: '',
        requestId: 'preview-request',
      }),
      refetch: mocks.retry,
    })
    open()
    expect(screen.getByText('preview-request')).toBeVisible()
    await userEvent.setup().click(screen.getByRole('button', { name: uz.common.retry }))
    expect(mocks.retry).toHaveBeenCalledOnce()
    expect(screen.queryByTestId('week-content')).not.toBeInTheDocument()
  })

  it('closes with Escape', async () => {
    const { onClose } = open()
    await userEvent.setup().keyboard('{Escape}')
    expect(onClose).toHaveBeenCalledOnce()
  })
})
