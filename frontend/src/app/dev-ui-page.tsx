import { useState, type ReactNode } from 'react'
import { Bell, House, Search, Settings } from 'lucide-react'
import { useTheme } from '@/app/theme'
import { applyLocale, DEFAULT_LOCALE, isLocale } from '@/shared/i18n'
import { useTranslation } from 'react-i18next'
import {
  Avatar,
  Badge,
  Breadcrumb,
  Button,
  Callout,
  Card,
  CardContent,
  CardHeader,
  Checkbox,
  CodeBlock,
  Combobox,
  CommandPalette,
  DatePicker,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
  DialogTrigger,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  EmptyState,
  ErrorState,
  IconButton,
  Input,
  LanguageSwitch,
  PageHeader,
  Pagination,
  Popover,
  PopoverContent,
  PopoverTrigger,
  ProgressBar,
  ProgressRing,
  RadioGroup,
  RichText,
  SegmentedControl,
  Select,
  Separator,
  SessionTypeBadge,
  Sheet,
  SheetBody,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
  ShieldEmblem,
  Skeleton,
  SkeletonText,
  Stat,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeaderCell,
  TableRow,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Textarea,
  ThemeSwitch,
  Tooltip,
  TooltipProvider,
  VerdictBadge,
  SESSION_TYPES,
  VERDICTS,
  useToast,
} from '@/shared/ui'
import { cn } from '@/shared/lib/cn'
import { toApiProblem } from '@/shared/api/errors'

const GLYPH_TEST = 'Oʻzbekiston · gʻoya · maʼlumot · Ўзбекистон · Щёлочь · Qq Gg 0O 1lI'

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="grid gap-4">
      <h2 className="border-border font-display text-title border-b pb-2 font-semibold">{title}</h2>
      {children}
    </section>
  )
}

function Row({ children }: { children: ReactNode }) {
  return <div className="flex flex-wrap items-center gap-3">{children}</div>
}

/** Renders the demo twice — light and dark — so both themes are reviewable at once. */
function BothThemes({ render }: { render: () => ReactNode }) {
  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <div className="border-border bg-surface rounded-lg border p-5">{render()}</div>
      <div className="dark border-border bg-surface text-text rounded-lg border p-5">{render()}</div>
    </div>
  )
}

function ToastDemo() {
  const { toast } = useToast()
  return (
    <Row>
      <Button
        variant="outline"
        size="sm"
        onClick={() => {
          toast({ tone: 'success', title: 'Saqlandi', body: 'Oʻzgarishlar muvaffaqiyatli saqlandi.' })
        }}
      >
        Success toast
      </Button>
      <Button
        variant="outline"
        size="sm"
        onClick={() => {
          toast({ tone: 'danger', title: 'Xatolik', body: 'Soʻrov raqami: 6f1c2f0a4b8e4d51' })
        }}
      >
        Danger toast
      </Button>
    </Row>
  )
}

function CommandPaletteDemo() {
  const [open, setOpen] = useState(false)
  return (
    <Row>
      <Button
        variant="outline"
        size="sm"
        onClick={() => {
          setOpen(true)
        }}
      >
        Open palette (Ctrl+K)
      </Button>
      <CommandPalette
        open={open}
        onOpenChange={setOpen}
        items={[
          { id: 'home', label: 'Bosh sahifa', icon: <House className="size-4" />, onSelect: () => undefined },
          {
            id: 'settings',
            label: 'Sozlamalar',
            icon: <Settings className="size-4" />,
            onSelect: () => undefined,
          },
          {
            id: 'notifications',
            label: 'Bildirishnomalar',
            icon: <Bell className="size-4" />,
            hint: '3',
            onSelect: () => undefined,
          },
        ]}
      />
    </Row>
  )
}

function InteractiveDemo() {
  const [checked, setChecked] = useState(true)
  const [switchOn, setSwitchOn] = useState(false)
  const [radio, setRadio] = useState('a')
  const [segment, setSegment] = useState('one')
  const [selectValue, setSelectValue] = useState('apple')
  const [combo, setCombo] = useState('b')
  const [page, setPage] = useState(1)
  return (
    <div className="grid gap-5">
      <Row>
        <Checkbox checked={checked} onCheckedChange={setChecked} label="Checkbox label" />
        <Checkbox checked={false} onCheckedChange={() => undefined} label="Unchecked" />
        <Checkbox checked={false} onCheckedChange={() => undefined} label="Disabled" disabled />
      </Row>
      <Row>
        <RadioGroup
          value={radio}
          onValueChange={setRadio}
          options={[
            { value: 'a', label: 'Variant A' },
            { value: 'b', label: 'Variant B' },
          ]}
        />
        <Switch checked={switchOn} onCheckedChange={setSwitchOn} label="Switch label" />
      </Row>
      <Row>
        <SegmentedControl
          aria-label="demo segments"
          value={segment}
          onValueChange={setSegment}
          options={[
            { value: 'one', label: 'Birinchi' },
            { value: 'two', label: 'Ikkinchi' },
            { value: 'three', label: 'Uchinchi' },
          ]}
        />
      </Row>
      <div className="grid gap-4 sm:grid-cols-2">
        <Select
          label="Select"
          value={selectValue}
          onValueChange={setSelectValue}
          options={[
            { value: 'apple', label: 'Olma' },
            { value: 'pear', label: 'Nok' },
            { value: 'plum', label: 'Olxoʻri' },
          ]}
        />
        <Combobox
          label="Combobox"
          value={combo}
          onValueChange={setCombo}
          placeholder="Tanlang…"
          searchPlaceholder="Qidirish…"
          emptyText="Topilmadi"
          options={[
            { value: 'a', label: 'BK-DI26/1', hint: '28 talaba' },
            { value: 'b', label: 'B-IQTR26/1', hint: '24 talaba' },
            { value: 'c', label: 'BK-DI25/1', hint: '31 talaba' },
          ]}
        />
      </div>
      <Pagination page={page} totalPages={7} onPageChange={setPage} className="max-w-xs" />
    </div>
  )
}

function OverlaysDemo() {
  return (
    <Row>
      <Dialog>
        <DialogTrigger asChild>
          <Button variant="outline" size="sm">
            Dialog
          </Button>
        </DialogTrigger>
        <DialogContent>
          <DialogTitle>Tasdiqlaysizmi?</DialogTitle>
          <DialogDescription>Bu amal qaytarib boʻlmaydigan namuna matni.</DialogDescription>
          <DialogFooter>
            <Button variant="ghost" size="sm">
              Bekor qilish
            </Button>
            <Button variant="accent" size="sm">
              Tasdiqlash
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <Sheet>
        <SheetTrigger asChild>
          <Button variant="outline" size="sm">
            Sheet
          </Button>
        </SheetTrigger>
        <SheetContent>
          <SheetHeader>
            <SheetTitle>Yon panel</SheetTitle>
            <SheetDescription>Tafsilotlar va tez amallar joyi.</SheetDescription>
          </SheetHeader>
          <SheetBody>
            <SkeletonText lines={5} />
          </SheetBody>
        </SheetContent>
      </Sheet>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="sm">
            Menu
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuLabel>Amallar</DropdownMenuLabel>
          <DropdownMenuItem>Tahrirlash</DropdownMenuItem>
          <DropdownMenuItem>Nusxalash</DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem destructive>Oʻchirish</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
      <Tooltip content="Qisqa izoh matni">
        <Button variant="outline" size="sm">
          Tooltip
        </Button>
      </Tooltip>
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" size="sm">
            Popover
          </Button>
        </PopoverTrigger>
        <PopoverContent>
          <p className="text-small text-text">Popover ichidagi erkin kontent.</p>
        </PopoverContent>
      </Popover>
    </Row>
  )
}

const COLOR_TOKENS = [
  ['surface', 'bg-surface'],
  ['surface-raised', 'bg-surface-raised'],
  ['surface-sunken', 'bg-surface-sunken'],
  ['text', 'bg-text'],
  ['text-muted', 'bg-text-muted'],
  ['border', 'bg-border'],
  ['border-strong', 'bg-border-strong'],
  ['primary', 'bg-primary'],
  ['primary-hover', 'bg-primary-hover'],
  ['accent', 'bg-accent'],
  ['accent-strong', 'bg-accent-strong'],
  ['accent-text', 'bg-accent-text'],
  ['accent-soft', 'bg-accent-soft'],
  ['success', 'bg-success'],
  ['warning', 'bg-warning'],
  ['danger', 'bg-danger'],
  ['info', 'bg-info'],
] as const

export function DevUiPage() {
  const { t, i18n } = useTranslation()
  const { theme, setTheme } = useTheme()
  return (
    <TooltipProvider>
      <div className="max-w-app mx-auto grid gap-10 px-4 py-8 sm:px-6 lg:px-8">
        <PageHeader
          title={t('devui.title')}
          context="Internal review surface — every component, every state, both themes. Dev only."
          action={
            <div className="flex items-center gap-2">
              <LanguageSwitch
                value={isLocale(i18n.language) ? i18n.language : DEFAULT_LOCALE}
                onChange={applyLocale}
              />
              <ThemeSwitch value={theme} onChange={setTheme} />
            </div>
          }
        />

        <Section title="Typography">
          <BothThemes
            render={() => (
              <div className="grid gap-4">
                <div>
                  <div className="text-caption text-text-muted uppercase">display — Bitter</div>
                  <div className="font-display text-display-xl font-semibold">Aniq va Ijtimoiy Fanlar</div>
                  <div className="font-display text-display-lg font-semibold">Universiteti</div>
                  <div className="font-display text-title mt-1">Dars jadvali · 2026/2027-1</div>
                </div>
                <div>
                  <div className="text-caption text-text-muted uppercase">reading — Literata</div>
                  <p className="max-w-lesson font-reading text-lead">
                    Dasturlash — bu muammolarni algoritmlar yordamida yechish sanʼati. Har bir dars nazariya,
                    misollar va amaliyotdan iborat toʻliq bob sifatida qurilgan.
                  </p>
                </div>
                <div>
                  <div className="text-caption text-text-muted uppercase">interface — Onest</div>
                  <p className="text-body">Bildirishnomalar · 12 · JN / ON / YaN · {GLYPH_TEST}</p>
                  <p className="text-small text-text-muted">Small text · {GLYPH_TEST}</p>
                  <p className="text-caption tracking-caption text-text-muted uppercase">
                    Caption / overline · {GLYPH_TEST}
                  </p>
                </div>
                <div>
                  <div className="text-caption text-text-muted uppercase">code — Source Code Pro</div>
                  <code className="text-small font-mono">
                    def kabisa(yil): return yil % 4 == 0 # Oʻzbekiston · Ўзбекистон
                  </code>
                </div>
              </div>
            )}
          />
        </Section>

        <Section title="Colour tokens">
          <BothThemes
            render={() => (
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                {COLOR_TOKENS.map(([name, bg]) => (
                  <div key={name} className="grid gap-1">
                    <div className={cn('border-border h-10 rounded-sm border', bg)} />
                    <span className="text-caption text-text-muted font-mono">{name}</span>
                  </div>
                ))}
              </div>
            )}
          />
          <BothThemes
            render={() => (
              <div className="grid gap-2">
                <Row>
                  {SESSION_TYPES.map((type) => (
                    <SessionTypeBadge key={type} type={type} label={t(`sessionType.${type}`)} />
                  ))}
                </Row>
                <Row>
                  {VERDICTS.map((verdict) => (
                    <VerdictBadge key={verdict} verdict={verdict} label={verdict.replaceAll('_', ' ')} />
                  ))}
                  <Badge tone="info">QUEUED</Badge>
                </Row>
                <Row>
                  <Badge tone="neutral">Neutral</Badge>
                  <Badge tone="success">Success</Badge>
                  <Badge tone="warning">Warning</Badge>
                  <Badge tone="danger">Danger</Badge>
                  <Badge tone="accent">Accent</Badge>
                </Row>
              </div>
            )}
          />
        </Section>

        <Section title="Buttons">
          <BothThemes
            render={() => (
              <div className="grid gap-3">
                <Row>
                  <Button>Primary</Button>
                  <Button variant="accent">Accent</Button>
                  <Button variant="outline">Outline</Button>
                  <Button variant="ghost">Ghost</Button>
                  <Button variant="danger">Danger</Button>
                  <Button variant="link">Link</Button>
                </Row>
                <Row>
                  <Button size="sm">Small</Button>
                  <Button size="md">Medium</Button>
                  <Button size="lg">Large</Button>
                  <Button loading>Loading</Button>
                  <Button disabled>Disabled</Button>
                </Row>
                <Row>
                  <IconButton aria-label="search" variant="ghost">
                    <Search className="size-5" />
                  </IconButton>
                  <IconButton aria-label="notifications" variant="outline">
                    <Bell className="size-5" />
                  </IconButton>
                  <IconButton aria-label="settings" variant="primary">
                    <Settings className="size-5" />
                  </IconButton>
                  <IconButton aria-label="disabled" disabled>
                    <Settings className="size-5" />
                  </IconButton>
                </Row>
              </div>
            )}
          />
        </Section>

        <Section title="Forms">
          <BothThemes render={() => <InteractiveDemo />} />
          <BothThemes
            render={() => (
              <div className="grid gap-4 sm:grid-cols-2">
                <Input label="Login" placeholder="student3" />
                <Input label="Xatolik bilan" defaultValue="notoʻgʻri" error="Login yoki parol notoʻgʻri" />
                <Textarea label="Izoh" placeholder="Matn…" />
                <DatePicker label="Sana" defaultValue="2026-10-02" />
              </div>
            )}
          />
        </Section>

        <Section title="Navigation & disclosure">
          <BothThemes
            render={() => (
              <div className="grid gap-5">
                <Breadcrumb items={[{ label: 'Kurslarim', to: '/dev/ui' }, { label: 'Dasturlash' }]} />
                <Tabs defaultValue="plan">
                  <TabsList>
                    <TabsTrigger value="plan">Reja</TabsTrigger>
                    <TabsTrigger value="syllabus">Sillabus</TabsTrigger>
                    <TabsTrigger value="work">Ishlar</TabsTrigger>
                    <TabsTrigger value="grades">Baholar</TabsTrigger>
                  </TabsList>
                  <TabsContent value="plan" className="text-small text-text-muted pt-3">
                    Reja tabi kontenti
                  </TabsContent>
                </Tabs>
                <OverlaysDemo />
              </div>
            )}
          />
        </Section>

        <Section title="Data display">
          <BothThemes
            render={() => (
              <div className="grid gap-5">
                <div className="grid grid-cols-3 gap-4">
                  <Stat label="Talabalar" value={204} hint="+12 bu hafta" />
                  <Stat label="Oʻtish foizi" value="87%" hint="JN bo'yicha" />
                  <Stat label="Oʻrtacha ball" value={73.5} />
                </div>
                <div className="grid items-center gap-4 sm:grid-cols-[1fr_auto]">
                  <ProgressBar value={68} label="68%" />
                  <ProgressRing value={68} />
                </div>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableHeaderCell>Talaba</TableHeaderCell>
                      <TableHeaderCell>Guruh</TableHeaderCell>
                      <TableHeaderCell numeric>Ball</TableHeaderCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                      <TableCell>A. Karimova</TableCell>
                      <TableCell>BK-DI26/1</TableCell>
                      <TableCell numeric>91</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell>J. Rasulov</TableCell>
                      <TableCell>BK-DI26/1</TableCell>
                      <TableCell numeric>74</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
                <Row>
                  <Avatar name="Nodir Ayitbayev" />
                  <Avatar name="Botir Akbarov" size="sm" />
                  <Avatar name="Umar Xolmatov" size="lg" />
                </Row>
              </div>
            )}
          />
        </Section>

        <Section title="Feedback">
          <BothThemes
            render={() => (
              <div className="grid gap-4">
                <Callout variant="INFO" title="Eslatma">
                  Maʼruza 19-oktabrga koʻchirildi.
                </Callout>
                <Callout variant="TIP" title="Maslahat">
                  Misollarni oʻz kodingiz bilan sinang.
                </Callout>
                <Callout variant="WARNING" title="Diqqat">
                  Muddat 2 kundan soʻng tugaydi.
                </Callout>
                <Callout variant="DANGER" title="Xatolik">
                  Kompilyatsiya xatosi: 12-qator.
                </Callout>
                <EmptyState
                  title="Hozircha topshiriqlar yoʻq"
                  body="Yangi topshiriq eʼlon qilinishi bilan bu yerda paydo boʻladi."
                  action={
                    <Button size="sm" variant="outline">
                      Kurslarimga qaytish
                    </Button>
                  }
                />
                <ErrorState
                  problem={toApiProblem({
                    code: 'INTERNAL_ERROR',
                    message: 'demo',
                    requestId: '6f1c2f0a4b8e4d51',
                  })}
                  onRetry={() => undefined}
                />
                <div className="grid gap-3">
                  <Skeleton className="h-8 w-1/3" />
                  <SkeletonText lines={3} />
                </div>
                <ToastDemo />
                <CommandPaletteDemo />
                <Card>
                  <CardHeader title="Dasturlash" description="DAS1110 · 6 kredit" />
                  <CardContent className="text-small text-text-muted">2026/2027-1 · BK-DI26/1</CardContent>
                </Card>
              </div>
            )}
          />
        </Section>

        <Section title="Code & rich text">
          <BothThemes
            render={() => (
              <div className="grid gap-4">
                <CodeBlock
                  language="python"
                  copyLabel="Nusxalash"
                  copiedLabel="Nusxalandi"
                  code={
                    'def kabisa(yil: int) -> bool:\n    """Kabisa yilini aniqlaydi."""\n    return yil % 4 == 0 and (yil % 100 != 0 or yil % 400 == 0)'
                  }
                />
                <RichText html="<h2>Bo‘lim sarlavhasi</h2><p>Matn <strong>qalin</strong> va <a href='#'>havola</a> bilan. <code>inline kod</code> ham bor.</p><ul><li>Birinchi band</li><li>Ikkinchi band</li></ul><blockquote>Iqtibos matni.</blockquote>" />
              </div>
            )}
          />
        </Section>

        <Section title="Brand">
          <BothThemes
            render={() => (
              <div className="flex items-center gap-6">
                <ShieldEmblem className="size-16" />
                <ShieldEmblem className="size-10" />
                <ShieldEmblem className="size-6" />
                <div className="grid">
                  <span className="font-display text-title font-semibold">AIFU</span>
                  <span className="text-caption text-text-muted">{t('app.universityName')}</span>
                </div>
              </div>
            )}
          />
        </Section>

        <Separator />
        <p className="text-caption text-text-muted pb-8 text-center">
          AIFU design system · development build only
        </p>
      </div>
    </TooltipProvider>
  )
}
