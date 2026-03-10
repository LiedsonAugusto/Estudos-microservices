'use client'

import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { type Service, type TimeSlot } from '@/types'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { cn } from '@/lib/utils'

const WEEKDAYS = [
  { value: 1, label: 'Seg' },
  { value: 2, label: 'Ter' },
  { value: 3, label: 'Qua' },
  { value: 4, label: 'Qui' },
  { value: 5, label: 'Sex' },
  { value: 6, label: 'Sáb' },
  { value: 0, label: 'Dom' },
]

const schema = z.object({
  mode: z.enum(['single', 'batch']),
  serviceId: z.string().min(1, 'Selecione um serviço'),
  date: z.string().optional(),
  dateFrom: z.string().optional(),
  dateTo: z.string().optional(),
  weekdays: z.array(z.number()).optional(),
  startTime: z.string().min(1, 'Informe o horário de início'),
  totalSlots: z
    .number({ invalid_type_error: 'Informe o número de vagas' })
    .min(1, 'Mínimo de 1 vaga'),
}).superRefine((data, ctx) => {
  if (data.mode === 'single') {
    if (!data.date) {
      ctx.addIssue({ code: 'custom', path: ['date'], message: 'Informe a data' })
    }
  } else {
    if (!data.dateFrom) {
      ctx.addIssue({ code: 'custom', path: ['dateFrom'], message: 'Informe a data inicial' })
    }
    if (!data.dateTo) {
      ctx.addIssue({ code: 'custom', path: ['dateTo'], message: 'Informe a data final' })
    }
    if (!data.weekdays || data.weekdays.length === 0) {
      ctx.addIssue({ code: 'custom', path: ['weekdays'], message: 'Selecione pelo menos um dia da semana' })
    }
  }
})

type FormData = z.infer<typeof schema>

function addMinutes(time: string, minutes: number): string {
  const [h, m] = time.split(':').map(Number)
  const total = h * 60 + m + minutes
  const endH = Math.floor(total / 60) % 24
  const endM = total % 60
  return `${String(endH).padStart(2, '0')}:${String(endM).padStart(2, '0')}`
}

function generateDatesInRange(dateFrom: string, dateTo: string, weekdays: number[]): string[] {
  const dates: string[] = []
  const current = new Date(dateFrom + 'T12:00:00')
  const end = new Date(dateTo + 'T12:00:00')
  while (current <= end) {
    if (weekdays.includes(current.getDay())) {
      dates.push(current.toISOString().split('T')[0])
    }
    current.setDate(current.getDate() + 1)
  }
  return dates
}

type SlotData = Omit<TimeSlot, 'id' | 'availableSlots'>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  timeSlot: TimeSlot | null
  services: Service[]
  onSubmit: (slots: SlotData[]) => void
}

export function TimeSlotFormDialog({ open, onOpenChange, timeSlot, services, onSubmit }: Props) {
  const isEditing = !!timeSlot

  const form = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      mode: 'single',
      serviceId: '',
      date: '',
      dateFrom: '',
      dateTo: '',
      weekdays: [],
      startTime: '08:00',
      totalSlots: 10,
    },
  })

  const mode = form.watch('mode')
  const watchedServiceId = form.watch('serviceId')
  const watchedStartTime = form.watch('startTime')
  const selectedService = services.find((s) => s.id === watchedServiceId)

  useEffect(() => {
    if (timeSlot) {
      form.reset({
        mode: 'single',
        serviceId: timeSlot.serviceId,
        date: timeSlot.date,
        dateFrom: '',
        dateTo: '',
        weekdays: [],
        startTime: timeSlot.startTime,
        totalSlots: timeSlot.totalSlots,
      })
    } else {
      form.reset({
        mode: 'single',
        serviceId: '',
        date: '',
        dateFrom: '',
        dateTo: '',
        weekdays: [],
        startTime: '08:00',
        totalSlots: 10,
      })
    }
  }, [timeSlot, form])

  function handleSubmit(data: FormData) {
    const service = services.find((s) => s.id === data.serviceId)
    if (!service) return

    const endTime = addMinutes(data.startTime, service.durationMinutes)

    if (data.mode === 'single') {
      onSubmit([{
        serviceId: data.serviceId,
        serviceName: service.name,
        date: data.date!,
        startTime: data.startTime,
        endTime,
        totalSlots: data.totalSlots,
        active: true,
      }])
    } else {
      const dates = generateDatesInRange(data.dateFrom!, data.dateTo!, data.weekdays ?? [])
      onSubmit(dates.map((date) => ({
        serviceId: data.serviceId,
        serviceName: service.name,
        date,
        startTime: data.startTime,
        endTime,
        totalSlots: data.totalSlots,
        active: true,
      })))
    }

    onOpenChange(false)
  }

  const previewEndTime =
    selectedService && watchedStartTime
      ? addMinutes(watchedStartTime, selectedService.durationMinutes)
      : null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar horário' : 'Novo horário'}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="flex flex-col gap-4">

            {/* Modo: unitário ou em lote (apenas na criação) */}
            {!isEditing && (
              <div className="flex rounded-lg border border-border overflow-hidden">
                <button
                  type="button"
                  onClick={() => form.setValue('mode', 'single')}
                  className={cn(
                    'flex-1 py-2 text-sm font-medium transition-colors',
                    mode === 'single'
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-card text-muted-foreground hover:text-foreground'
                  )}
                >
                  Unitário
                </button>
                <button
                  type="button"
                  onClick={() => form.setValue('mode', 'batch')}
                  className={cn(
                    'flex-1 py-2 text-sm font-medium transition-colors',
                    mode === 'batch'
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-card text-muted-foreground hover:text-foreground'
                  )}
                >
                  Em lote
                </button>
              </div>
            )}

            {/* Serviço */}
            <FormField
              control={form.control}
              name="serviceId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Serviço</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Selecione um serviço" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {services.filter((s) => s.active).map((service) => (
                        <SelectItem key={service.id} value={service.id}>
                          {service.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Modo unitário: data única */}
            {mode === 'single' && (
              <FormField
                control={form.control}
                name="date"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Data</FormLabel>
                    <FormControl>
                      <Input type="date" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            {/* Modo em lote: período + dias da semana */}
            {mode === 'batch' && (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <FormField
                    control={form.control}
                    name="dateFrom"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Data inicial</FormLabel>
                        <FormControl>
                          <Input type="date" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                  <FormField
                    control={form.control}
                    name="dateTo"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Data final</FormLabel>
                        <FormControl>
                          <Input type="date" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <FormField
                  control={form.control}
                  name="weekdays"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Dias da semana</FormLabel>
                      <FormControl>
                        <div className="flex gap-1.5">
                          {WEEKDAYS.map((day) => {
                            const selected = field.value?.includes(day.value)
                            return (
                              <button
                                key={day.value}
                                type="button"
                                onClick={() => {
                                  const current = field.value ?? []
                                  field.onChange(
                                    selected
                                      ? current.filter((d) => d !== day.value)
                                      : [...current, day.value]
                                  )
                                }}
                                className={cn(
                                  'flex-1 py-1.5 rounded text-xs font-medium transition-colors border',
                                  selected
                                    ? 'bg-primary text-primary-foreground border-primary'
                                    : 'bg-card text-muted-foreground border-border hover:border-primary/50'
                                )}
                              >
                                {day.label}
                              </button>
                            )
                          })}
                        </div>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </>
            )}

            {/* Hora de início + fim (calculado) */}
            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="startTime"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Hora de início</FormLabel>
                    <FormControl>
                      <Input type="time" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div>
                <Label className="text-sm font-medium">Hora de término</Label>
                <div className="mt-2 h-9 px-3 flex items-center rounded-md border border-border bg-muted text-sm text-muted-foreground">
                  {previewEndTime ?? (selectedService ? '—' : 'Selecione um serviço')}
                </div>
              </div>
            </div>

            {/* Total de vagas */}
            <FormField
              control={form.control}
              name="totalSlots"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Total de vagas</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      min={1}
                      placeholder="10"
                      {...field}
                      onChange={(e) => field.onChange(e.target.valueAsNumber)}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit">
                {isEditing ? 'Salvar' : mode === 'batch' ? 'Criar horários' : 'Criar'}
              </Button>
            </div>

          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
