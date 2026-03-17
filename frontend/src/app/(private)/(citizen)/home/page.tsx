'use client'

import { type CitizenAppointment, type Service } from '@/types'
import { UpcomingAppointments } from '@/components/citizen/home/UpcomingAppointments'
import { AvailableServicesPreview } from '@/components/citizen/home/AvailableServicesPreview'

// dados simulados — substituir por fetch à API futuramente
const mockAppointments: CitizenAppointment[] = [
  { id: '1', confirmationCode: 'AGD-3F8A1B', serviceName: 'Renovação de CNH',  date: '2026-03-20', time: '09:00', status: 'SCHEDULED'  },
  { id: '2', confirmationCode: 'AGD-7C2D4E', serviceName: '1ª Via de RG',      date: '2026-03-22', time: '10:30', status: 'CONFIRMED'  },
  { id: '3', confirmationCode: 'AGD-9E5F2A', serviceName: 'Emissão de CPF',    date: '2026-03-25', time: '14:00', status: 'SCHEDULED'  },
]

const mockServices: Service[] = [
  { id: '1', name: 'Renovação de CNH',       description: 'Renovação da Carteira Nacional de Habilitação.',           durationMinutes: 30, active: true },
  { id: '2', name: '1ª Via de RG',           description: 'Emissão da primeira via do documento de identidade.',      durationMinutes: 20, active: true },
  { id: '3', name: 'Emissão de CPF',         description: 'Cadastro de Pessoa Física junto à Receita Federal.',       durationMinutes: 15, active: true },
  { id: '4', name: 'Certidão de Nascimento', description: 'Segunda via de certidão de nascimento.',                   durationMinutes: 45, active: true },
]

export default function CitizenHomePage() {
  return (
    <div className="flex flex-col gap-6">

      <div>
        <h1 className="text-2xl font-bold text-foreground">Olá, Maria!</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Acompanhe seus agendamentos ou agende um novo serviço.
        </p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <UpcomingAppointments appointments={mockAppointments} />
        <AvailableServicesPreview services={mockServices} />
      </div>

    </div>
  )
}
