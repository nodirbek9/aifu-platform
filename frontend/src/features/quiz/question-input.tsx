import { useId, useState } from 'react'
import { ArrowDown, ArrowLeftRight, ArrowUp, Check, Unlink, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/button'
import { ChoiceCard, type ChoiceMark } from '@/shared/ui/choice-card'
import { IconButton } from '@/shared/ui/icon-button'
import { Input } from '@/shared/ui/input'
import {
  addPair,
  moveOption,
  parseNumber,
  removePair,
  type Answer,
  type Option,
  type Question,
} from './model'

type QuestionInputProps = {
  question: Question
  answer: Answer
  /**
   * Absent in a review: the answer is shown as it was given. `settled` is false while the
   * student is still typing — the answer is then saved after a pause.
   */
  onChange?: (answer: Answer, settled: boolean) => void
  /** The id of the element holding the question's text. */
  labelledBy: string
}

/** The way to answer one question, by its type; the same component shows the answer in a review. */
export function QuestionInput({ question, answer, onChange, labelledBy }: QuestionInputProps) {
  switch (answer.kind) {
    case 'choice':
      return (
        <ChoiceInput
          question={question}
          optionIds={answer.optionIds}
          onChange={onChange}
          labelledBy={labelledBy}
        />
      )
    case 'text':
    case 'number':
      return <TypedInput question={question} answer={answer} onChange={onChange} labelledBy={labelledBy} />
    case 'order':
      return (
        <OrderInput
          question={question}
          optionIds={answer.optionIds}
          onChange={onChange}
          labelledBy={labelledBy}
        />
      )
    case 'pairs':
      return (
        <PairsInput question={question} pairs={answer.pairs} onChange={onChange} labelledBy={labelledBy} />
      )
  }
}

type PartProps = Pick<QuestionInputProps, 'question' | 'onChange' | 'labelledBy'>

function ChoiceInput({
  question,
  optionIds,
  onChange,
  labelledBy,
}: PartProps & { optionIds: readonly string[] }) {
  const { t } = useTranslation()
  const name = useId()
  const multiple = question.type === 'MULTIPLE_CHOICE'

  const markOf = (option: Option, checked: boolean): ChoiceMark | undefined => {
    if (option.correct === true) return { tone: 'right', label: t('quiz.rightOption') }
    if (option.correct === false && checked) return { tone: 'wrong', label: t('quiz.wrongOption') }
    return undefined
  }

  return (
    <fieldset aria-labelledby={labelledBy} className="grid gap-2">
      {question.options.map((option) => {
        const checked = optionIds.includes(option.id)
        return (
          <ChoiceCard
            key={option.id}
            type={multiple ? 'checkbox' : 'radio'}
            name={name}
            checked={checked}
            readOnly={onChange === undefined}
            mark={markOf(option, checked)}
            onChange={() => {
              const next = multiple
                ? checked
                  ? optionIds.filter((id) => id !== option.id)
                  : [...optionIds, option.id]
                : [option.id]
              onChange?.({ kind: 'choice', optionIds: next }, true)
            }}
          >
            {option.text}
          </ChoiceCard>
        )
      })}
    </fieldset>
  )
}

function TypedInput({
  question,
  answer,
  onChange,
  labelledBy,
}: PartProps & { answer: Extract<Answer, { kind: 'text' | 'number' }> }) {
  const { t } = useTranslation()
  const numeric = answer.kind === 'number'
  const notANumber = numeric && answer.text.trim() !== '' && parseNumber(answer.text) === null
  const review = question.review

  if (onChange === undefined) {
    return (
      <div className="grid gap-2">
        <p
          className={cn(
            'border-border bg-surface-raised text-body flex min-h-12 items-center gap-2 rounded-md border px-4 py-3',
            review?.correct === true && 'border-success bg-success-soft',
            review?.correct === false && 'border-danger bg-danger-soft',
          )}
        >
          <span className="text-text-muted">{t('quiz.yourAnswer')}:</span>
          {answer.text.trim() === '' ? (
            <span className="text-text-muted">{t('quiz.notAnswered')}</span>
          ) : (
            <span className="text-text font-mono font-medium">{answer.text}</span>
          )}
        </p>
        {review?.correctAnswer !== undefined && !review.correct && (
          <p className="text-small text-text-muted">
            {t('quiz.acceptedAnswer')}:{' '}
            <span className="text-text font-mono font-medium">{review.correctAnswer}</span>
          </p>
        )}
      </div>
    )
  }

  return (
    <Input
      aria-labelledby={labelledBy}
      className="max-w-sm font-mono"
      inputMode={numeric ? 'decimal' : 'text'}
      autoComplete="off"
      autoCapitalize="off"
      spellCheck={false}
      maxLength={200}
      placeholder={numeric ? t('quiz.numberPlaceholder') : t('quiz.textPlaceholder')}
      value={answer.text}
      error={notANumber ? t('quiz.notANumber') : undefined}
      onChange={(event) => {
        onChange({ kind: answer.kind, text: event.target.value }, false)
      }}
      onBlur={() => {
        onChange(answer, true)
      }}
    />
  )
}

function OrderInput({
  question,
  optionIds,
  onChange,
  labelledBy,
}: PartProps & { optionIds: readonly string[] }) {
  const { t } = useTranslation()
  const [announcement, setAnnouncement] = useState('')
  const untouched = optionIds.length === 0
  const order = untouched ? question.options.map((option) => option.id) : optionIds
  const byId = new Map(question.options.map((option) => [option.id, option]))

  const move = (index: number, direction: -1 | 1, option: Option) => {
    onChange?.({ kind: 'order', optionIds: moveOption(order, index, direction) }, true)
    setAnnouncement(t('quiz.movedTo', { text: option.text, place: index + direction + 1 }))
  }

  return (
    <div className="grid gap-3">
      <ol aria-labelledby={labelledBy} className="grid gap-2">
        {order.map((id, index) => {
          const option = byId.get(id)
          if (option === undefined) return null
          const inPlace = option.correctPosition === undefined ? undefined : option.correctPosition === index
          return (
            <li
              key={id}
              className={cn(
                'border-border bg-surface-raised flex min-h-12 items-center gap-3 rounded-md border py-1 pr-1 pl-4',
                inPlace === true && 'border-success bg-success-soft',
                inPlace === false && 'border-danger bg-danger-soft',
              )}
            >
              <span className="text-small text-text-muted w-5 shrink-0 tabular-nums">{index + 1}.</span>
              <span className="text-body text-text min-w-0 flex-1 py-2">{option.text}</span>
              {onChange !== undefined && (
                <>
                  <IconButton
                    aria-label={t('quiz.moveUp', { text: option.text })}
                    disabled={index === 0}
                    onClick={() => {
                      move(index, -1, option)
                    }}
                  >
                    <ArrowUp aria-hidden className="size-5" />
                  </IconButton>
                  <IconButton
                    aria-label={t('quiz.moveDown', { text: option.text })}
                    disabled={index === order.length - 1}
                    onClick={() => {
                      move(index, 1, option)
                    }}
                  >
                    <ArrowDown aria-hidden className="size-5" />
                  </IconButton>
                </>
              )}
              {inPlace === true && (
                <span className="text-caption text-success flex shrink-0 items-center gap-1 pr-3 font-medium tracking-normal">
                  <Check aria-hidden className="size-4" />
                  {t('quiz.rightPlace')}
                </span>
              )}
              {inPlace === false && option.correctPosition !== undefined && (
                <span className="text-caption text-danger flex shrink-0 items-center gap-1 pr-3 font-medium tracking-normal">
                  <X aria-hidden className="size-4" />
                  {t('quiz.shouldBeAt', { place: option.correctPosition + 1 })}
                </span>
              )}
            </li>
          )
        })}
      </ol>
      {onChange !== undefined && untouched && (
        <div className="flex flex-wrap items-center gap-3">
          <p className="text-small text-text-muted">{t('quiz.orderHint')}</p>
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              onChange({ kind: 'order', optionIds: order }, true)
            }}
          >
            {t('quiz.confirmOrder')}
          </Button>
        </div>
      )}
      {onChange === undefined && untouched && (
        <p className="text-small text-text-muted">{t('quiz.notAnswered')}</p>
      )}
      <p role="status" className="sr-only">
        {announcement}
      </p>
    </div>
  )
}

function PairsInput({
  question,
  pairs,
  onChange,
  labelledBy,
}: PartProps & { pairs: Extract<Answer, { kind: 'pairs' }>['pairs'] }) {
  const { t } = useTranslation()
  const [picked, setPicked] = useState<string | null>(null)
  const byId = new Map(question.options.map((option) => [option.id, option]))
  const paired = new Set(pairs.flat())
  const free = question.options.filter((option) => !paired.has(option.id))
  const reviewed = question.options.some((option) => option.pairKey !== undefined)

  const pick = (id: string) => {
    if (picked === null) setPicked(id)
    else if (picked === id) setPicked(null)
    else {
      onChange?.({ kind: 'pairs', pairs: addPair(pairs, picked, id) }, true)
      setPicked(null)
    }
  }

  // the pairs the key holds, for a review: options sharing a pair key
  const rightPairs = reviewed
    ? [...new Set(question.options.map((option) => option.pairKey))].flatMap((key) => {
        const members = question.options.filter((option) => option.pairKey === key && key !== undefined)
        const [first, second] = members
        return first !== undefined && second !== undefined ? [[first, second] as const] : []
      })
    : []

  return (
    <div role="group" aria-labelledby={labelledBy} className="grid gap-4">
      {pairs.length > 0 && (
        <ul aria-label={t('quiz.pairs')} className="grid gap-2">
          {pairs.map(([firstId, secondId]) => {
            const first = byId.get(firstId)
            const second = byId.get(secondId)
            if (first === undefined || second === undefined) return null
            const right = reviewed
              ? first.pairKey !== undefined && first.pairKey === second.pairKey
              : undefined
            return (
              <li
                key={`${firstId}:${secondId}`}
                className={cn(
                  'border-border bg-surface-raised flex min-h-12 items-center gap-3 rounded-md border py-1 pr-1 pl-4',
                  right === true && 'border-success bg-success-soft',
                  right === false && 'border-danger bg-danger-soft',
                )}
              >
                <span className="text-body text-text min-w-0 flex-1 py-2">{first.text}</span>
                <ArrowLeftRight aria-hidden className="text-text-muted size-4 shrink-0" />
                <span className="text-body text-text min-w-0 flex-1 py-2">{second.text}</span>
                {onChange !== undefined && (
                  <IconButton
                    aria-label={t('quiz.unpair', { first: first.text, second: second.text })}
                    onClick={() => {
                      onChange({ kind: 'pairs', pairs: removePair(pairs, firstId) }, true)
                    }}
                  >
                    <Unlink aria-hidden className="size-5" />
                  </IconButton>
                )}
                {right === true && (
                  <Check aria-label={t('quiz.rightOption')} className="text-success mr-3 size-5" />
                )}
                {right === false && (
                  <X aria-label={t('quiz.wrongOption')} className="text-danger mr-3 size-5" />
                )}
              </li>
            )
          })}
        </ul>
      )}

      {onChange !== undefined && free.length > 0 && (
        <div className="grid gap-2">
          <p className="text-small text-text-muted">{t('quiz.pairHint')}</p>
          <div className="flex flex-wrap gap-2">
            {free.map((option) => (
              <button
                key={option.id}
                type="button"
                aria-pressed={picked === option.id}
                onClick={() => {
                  pick(option.id)
                }}
                className={cn(
                  'border-border-strong bg-surface-raised text-body text-text min-h-11 rounded-md border px-4 py-2 text-left',
                  'duration-fast ease-standard hover:bg-surface-sunken transition-[background-color,border-color,box-shadow]',
                  'focus-visible:outline-focus-ring focus-visible:outline-2 focus-visible:outline-offset-2',
                  'aria-pressed:border-primary aria-pressed:ring-primary aria-pressed:ring-1',
                )}
              >
                {option.text}
              </button>
            ))}
          </div>
        </div>
      )}

      {onChange === undefined && pairs.length === 0 && (
        <p className="text-small text-text-muted">{t('quiz.notAnswered')}</p>
      )}

      {onChange === undefined && rightPairs.length > 0 && question.review?.correct === false && (
        <div className="grid gap-1">
          <p className="text-small text-text-muted">{t('quiz.rightPairs')}:</p>
          <ul className="text-small text-text grid gap-1">
            {rightPairs.map(([first, second]) => (
              <li key={first.id} className="flex items-center gap-2">
                <span>{first.text}</span>
                <ArrowLeftRight aria-hidden className="text-text-muted size-3.5 shrink-0" />
                <span>{second.text}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
