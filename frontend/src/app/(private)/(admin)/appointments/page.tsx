'use client'

import { useState, useMemo } from 'react'
import { type AdminAppointment, type AppointmentStatus } from '@/types'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { AppointmentsTable } from '@/components/admin/appointments/AppointmentsTable'

// dados simulados — substituir por fetch à API futuramente
const initialAppointments: AdminAppointment[] = [
  { id: '1', confirmationCode: 'ABC12345', citizenName: 'João Silva',    citizenEmail: 'joao@email.com',    serviceName: 'Renovação de CNH',    date: '2026-03-02', time: '08:00', status: 'COMPLETED'  },
  { id: '2', confirmationCode: 'DEF67890', citizenName: 'Maria Lima',    citizenEmail: 'maria@email.com',   serviceName: '1ª Via de RG',        date: '2026-03-02', time: '08:30', status: 'CONFIRMED'  },
  { id: '3', confirmationCode: 'GHI11223', citizenName: 'Pedro Souza',   citizenEmail: 'pedro@email.com',   serviceName: 'Emissão de CPF',      date: '2026-03-02', time: '09:00', status: 'SCHEDULED'  },
  { id: '4', confirmationCode: 'JKL44556', citizenName: 'Ana Costa',     citizenEmail: 'ana@email.com',     serviceName: 'Renovação de CNH',    date: '2026-03-02', time: '09:30', status: 'CANCELLED'  },
  { id: '5', confirmationCode: 'MNO77889', citizenName: 'Carlos Melo',   citizenEmail: 'carlos@email.com',  serviceName: '1ª Via de RG',        date: '2026-03-02', time: '10:00', status: 'SCHEDULED'  },
  { id: '6', confirmationCode: 'PQR00112', citizenName: 'Fernanda Dias', citizenEmail: 'fernanda@email.com',serviceName: 'Emissão de CPF',      date: '2026-03-03', time: '08:00', status: 'SCHEDULED'  },
  { id: '7', confirmationCode: 'STU33445', citizenName: 'Ricardo Alves', citizenEmail: 'ricardo@email.com', serviceName: 'Certidão de Nascimento', date: '2026-03-03', time: '09:00', status: 'NO_SHOW' },
]

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState<AdminAppointment[]>(initialAppointments)
  const [filterStatus, setFilterStatus] = useState<string>('ALL')
  const [filterDate, setFilterDate] = useState<string>('')

  function handleUpdateStatus(id: string, status: AppointmentStatus) {
    setAppointments((prev) =>
      prev.map((a) => a.id === id ? { ...a, status } : a)
    )
  }

  const filtered = useMemo(() => {
    return appointments.filter((a) => {
      const matchStatus = filterStatus === 'ALL' || a.status === filterStatus
      const matchDate = !filterDate || a.date === filterDate
      return matchStatus && matchDate
    })
  }, [appointments, filterStatus, filterDate])

  return (
    <div className="flex flex-col gap-6">

      <h1 className="text-2xl font-bold text-foreground">Agendamentos</h1>

      {/* Filtros */}
      <div className="flex gap-3">
        <Select value={filterStatus} onValueChange={setFilterStatus}>
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos os status</SelectItem>
            <SelectItem value="SCHEDULED">Agendado</SelectItem>
            <SelectItem value="CONFIRMED">Confirmado</SelectItem>
            <SelectItem value="COMPLETED">Concluído</SelectItem>
            <SelectItem value="CANCELLED">Cancelado</SelectItem>
            <SelectItem value="NO_SHOW">Não compareceu</SelectItem>
          </SelectContent>
        </Select>

        <Input
          type="date"
          className="w-48"
          value={filterDate}
          onChange={(e) => setFilterDate(e.target.value)}
        />

        {(filterStatus !== 'ALL' || filterDate) && (
          <button
            onClick={() => { setFilterStatus('ALL'); setFilterDate('') }}
            className="text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            Limpar filtros
          </button>
        )}
      </div>

      <AppointmentsTable
        appointments={filtered}
        onUpdateStatus={handleUpdateStatus}
      />

    </div>
  )
}
