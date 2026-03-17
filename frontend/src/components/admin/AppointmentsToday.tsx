'use client'

import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { cn } from '@/lib/utils'

type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'

type Appointment = {
  id: string
  time: string
  citizenName: string
  serviceName: string
  status: AppointmentStatus
}

const STATUS_CONFIG: Record<AppointmentStatus, { label: string; className: string }> = {
  SCHEDULED:  { label: 'Agendado',       className: 'bg-indigo-50 text-indigo-600 border-indigo-200 dark:bg-indigo-950 dark:text-indigo-300 dark:border-indigo-900' },
  CONFIRMED:  { label: 'Confirmado',     className: 'bg-green-100 text-green-700 border-green-200' },
  COMPLETED:  { label: 'Concluído',      className: 'bg-gray-100 text-gray-600 border-gray-200' },
  CANCELLED:  { label: 'Cancelado',      className: 'bg-red-100 text-red-700 border-red-200' },
  NO_SHOW:    { label: 'Não compareceu', className: 'bg-orange-100 text-orange-700 border-orange-200' },
}

// dados simulados — substituir por fetch à API futuramente
const mockAppointments: Appointment[] = [
  { id: '1', time: '08:00', citizenName: 'João Silva',   serviceName: 'Renovação de CNH',  status: 'COMPLETED'  },
  { id: '2', time: '08:30', citizenName: 'Maria Lima',   serviceName: '1ª Via RG',         status: 'CONFIRMED'  },
  { id: '3', time: '09:00', citizenName: 'Pedro Souza',  serviceName: 'Emissão de CPF',    status: 'SCHEDULED'  },
  { id: '4', time: '09:30', citizenName: 'Ana Costa',    serviceName: 'Renovação de CNH',  status: 'CANCELLED'  },
  { id: '5', time: '10:00', citizenName: 'Carlos Melo',  serviceName: '1ª Via RG',         status: 'SCHEDULED'  },
  { id: '6', time: '10:30', citizenName: 'Fernanda Dias',serviceName: 'Emissão de CPF',    status: 'NO_SHOW'    },
]

export default function AppointmentsToday() {
  return (
    <div className="rounded-lg border border-border bg-card">
      <div className="px-6 py-4 border-b border-border">
        <h2 className="text-base font-semibold text-foreground">Agendamentos de hoje</h2>
      </div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="px-6">Horário</TableHead>
            <TableHead>Cidadão</TableHead>
            <TableHead>Serviço</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {mockAppointments.map((appointment) => {
            const status = STATUS_CONFIG[appointment.status]
            return (
              <TableRow key={appointment.id}>
                <TableCell className="px-6 font-medium">{appointment.time}</TableCell>
                <TableCell>{appointment.citizenName}</TableCell>
                <TableCell>{appointment.serviceName}</TableCell>
                <TableCell>
                  <Badge
                    variant="outline"
                    className={cn('font-normal', status.className)}
                  >
                    {status.label}
                  </Badge>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
