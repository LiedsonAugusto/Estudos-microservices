'use client'

import { useState, useMemo } from 'react'
import { Plus } from 'lucide-react'
import { type TimeSlot, type Service } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TimeSlotsTable } from '@/components/admin/time-slots/TimeSlotsTable'
import { TimeSlotFormDialog } from '@/components/admin/time-slots/TimeSlotFormDialog'

// dados simulados — substituir por fetch à API futuramente
const mockServices: Service[] = [
  { id: '1', name: 'Renovação de CNH',       description: '', durationMinutes: 30, active: true  },
  { id: '2', name: '1ª Via de RG',           description: '', durationMinutes: 20, active: true  },
  { id: '3', name: 'Emissão de CPF',         description: '', durationMinutes: 15, active: true  },
  { id: '4', name: 'Certidão de Nascimento', description: '', durationMinutes: 45, active: false },
]

const initialTimeSlots: TimeSlot[] = [
  { id: '1', serviceId: '1', serviceName: 'Renovação de CNH', date: '2026-03-04', startTime: '08:00', endTime: '08:30', totalSlots: 10, availableSlots: 7,  active: true  },
  { id: '2', serviceId: '1', serviceName: 'Renovação de CNH', date: '2026-03-04', startTime: '08:30', endTime: '09:00', totalSlots: 10, availableSlots: 10, active: true  },
  { id: '3', serviceId: '2', serviceName: '1ª Via de RG',     date: '2026-03-04', startTime: '09:00', endTime: '09:20', totalSlots: 8,  availableSlots: 3,  active: true  },
  { id: '4', serviceId: '3', serviceName: 'Emissão de CPF',   date: '2026-03-05', startTime: '10:00', endTime: '10:15', totalSlots: 12, availableSlots: 12, active: true  },
  { id: '5', serviceId: '1', serviceName: 'Renovação de CNH', date: '2026-03-05', startTime: '14:00', endTime: '14:30', totalSlots: 10, availableSlots: 0,  active: false },
]

export default function TimeSlotsPage() {
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>(initialTimeSlots)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null)
  const [filterServiceId, setFilterServiceId] = useState<string>('ALL')
  const [filterDate, setFilterDate] = useState<string>('')

  function handleOpenCreate() {
    setSelectedSlot(null)
    setDialogOpen(true)
  }

  function handleOpenEdit(slot: TimeSlot) {
    setSelectedSlot(slot)
    setDialogOpen(true)
  }

  function handleToggleActive(slot: TimeSlot) {
    setTimeSlots((prev) =>
      prev.map((s) => s.id === slot.id ? { ...s, active: !s.active } : s)
    )
  }

  function handleSubmit(slots: Omit<TimeSlot, 'id' | 'availableSlots'>[]) {
    if (selectedSlot) {
      const updated = slots[0]
      setTimeSlots((prev) =>
        prev.map((s) =>
          s.id === selectedSlot.id
            ? { ...s, ...updated, active: s.active }
            : s
        )
      )
    } else {
      const newSlots: TimeSlot[] = slots.map((slot) => ({
        ...slot,
        id: crypto.randomUUID(),
        availableSlots: slot.totalSlots,
      }))
      setTimeSlots((prev) => [...prev, ...newSlots])
    }
  }

  const filtered = useMemo(() => {
    return timeSlots.filter((s) => {
      const matchService = filterServiceId === 'ALL' || s.serviceId === filterServiceId
      const matchDate = !filterDate || s.date === filterDate
      return matchService && matchDate
    })
  }, [timeSlots, filterServiceId, filterDate])

  return (
    <div className="flex flex-col gap-6">

      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground">Horários</h1>
        <Button onClick={handleOpenCreate}>
          <Plus className="w-4 h-4" />
          Novo horário
        </Button>
      </div>

      {/* Filtros */}
      <div className="flex gap-3">
        <Select value={filterServiceId} onValueChange={setFilterServiceId}>
          <SelectTrigger className="w-56">
            <SelectValue placeholder="Serviço" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos os serviços</SelectItem>
            {mockServices.map((s) => (
              <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Input
          type="date"
          className="w-48"
          value={filterDate}
          onChange={(e) => setFilterDate(e.target.value)}
        />

        {(filterServiceId !== 'ALL' || filterDate) && (
          <button
            onClick={() => { setFilterServiceId('ALL'); setFilterDate('') }}
            className="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Limpar filtros
          </button>
        )}
      </div>

      <TimeSlotsTable
        timeSlots={filtered}
        onEdit={handleOpenEdit}
        onToggleActive={handleToggleActive}
      />

      <TimeSlotFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        timeSlot={selectedSlot}
        services={mockServices}
        onSubmit={handleSubmit}
      />

    </div>
  )
}
