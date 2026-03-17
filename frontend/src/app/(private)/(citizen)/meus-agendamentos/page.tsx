'use client'

import { useState, useMemo } from 'react'
import { type CitizenAppointment, type AppointmentStatus } from '@/types'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { CitizenAppointmentsTable } from '@/components/citizen/appointments/CitizenAppointmentsTable'

const statusFilters: { key: AppointmentStatus | 'ALL'; label: string }[] = [
  { key: 'ALL',       label: 'Todos' },
  { key: 'SCHEDULED', label: 'Agendados' },
  { key: 'CONFIRMED', label: 'Confirmados' },
  { key: 'COMPLETED', label: 'Concluídos' },
  { key: 'CANCELLED', label: 'Cancelados' },
]

// dados simulados — substituir por fetch à API futuramente
const initialAppointments: CitizenAppointment[] = [
  { id: '1',  confirmationCode: 'AGD-3F8A1B', serviceName: 'Renovação de CNH',       date: '2026-03-20', time: '09:00', status: 'SCHEDULED'  },
  { id: '2',  confirmationCode: 'AGD-7C2D4E', serviceName: '1ª Via de RG',           date: '2026-03-22', time: '10:30', status: 'CONFIRMED'  },
  { id: '3',  confirmationCode: 'AGD-9E5F2A', serviceName: 'Emissão de CPF',         date: '2026-03-25', time: '14:00', status: 'SCHEDULED'  },
  { id: '4',  confirmationCode: 'AGD-1A3B5C', serviceName: 'Renovação de CNH',       date: '2026-03-10', time: '08:30', status: 'COMPLETED'  },
  { id: '5',  confirmationCode: 'AGD-4D6E8F', serviceName: '1ª Via de RG',           date: '2026-03-05', time: '11:00', status: 'COMPLETED'  },
  { id: '6',  confirmationCode: 'AGD-2B4C6D', serviceName: 'Certidão de Nascimento', date: '2026-03-08', time: '15:00', status: 'CANCELLED'  },
  { id: '7',  confirmationCode: 'AGD-8F0A2B', serviceName: 'Emissão de CPF',         date: '2026-03-02', time: '09:30', status: 'NO_SHOW'    },
  { id: '8',  confirmationCode: 'AGD-6E8D0C', serviceName: 'Renovação de CNH',       date: '2026-02-28', time: '10:00', status: 'COMPLETED'  },
]

export default function MyAppointmentsPage() {
  const [appointments, setAppointments] = useState(initialAppointments)
  const [filter, setFilter] = useState<AppointmentStatus | 'ALL'>('ALL')

  const filtered = useMemo(
    () => filter === 'ALL' ? appointments : appointments.filter((a) => a.status === filter),
    [appointments, filter]
  )

  function handleCancel(id: string) {
    setAppointments((prev) =>
      prev.map((a) => a.id === id ? { ...a, status: 'CANCELLED' as const } : a)
    )
  }

  return (
    <div className="flex flex-col gap-6">

      <div>
        <h1 className="text-2xl font-bold text-foreground">Meus agendamentos</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Acompanhe o status de todos os seus agendamentos.
        </p>
      </div>

      <div className="flex flex-wrap gap-2">
        {statusFilters.map((sf) => (
          <Button
            key={sf.key}
            variant={filter === sf.key ? 'default' : 'outline'}
            size="sm"
            onClick={() => setFilter(sf.key)}
            className={cn(
              filter === sf.key && 'bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-600 dark:hover:bg-indigo-700 text-white'
            )}
          >
            {sf.label}
          </Button>
        ))}
      </div>

      <CitizenAppointmentsTable appointments={filtered} onCancel={handleCancel} />

    </div>
  )
}
