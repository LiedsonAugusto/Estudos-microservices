'use client'

import Link from 'next/link'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { CalendarDays } from 'lucide-react'
import { cn } from '@/lib/utils'
import { type CitizenAppointment, type AppointmentStatus } from '@/types'

const STATUS_CONFIG: Record<AppointmentStatus, { label: string; className: string }> = {
  SCHEDULED:  { label: 'Agendado',       className: 'bg-indigo-50 text-indigo-600 border-indigo-200 dark:bg-indigo-950 dark:text-indigo-300 dark:border-indigo-900' },
  CONFIRMED:  { label: 'Confirmado',     className: 'bg-green-100 text-green-700 border-green-200' },
  COMPLETED:  { label: 'Concluído',      className: 'bg-gray-100 text-gray-600 border-gray-200' },
  CANCELLED:  { label: 'Cancelado',      className: 'bg-red-100 text-red-700 border-red-200' },
  NO_SHOW:    { label: 'Não compareceu', className: 'bg-orange-100 text-orange-700 border-orange-200' },
}

type Props = {
  appointments: CitizenAppointment[]
}

export function UpcomingAppointments({ appointments }: Props) {
  return (
    <div className="rounded-lg border border-border bg-card p-6">
      <div className="flex items-center gap-2 mb-4">
        <CalendarDays className="w-5 h-5 text-muted-foreground" />
        <h2 className="text-base font-semibold text-foreground">Próximos agendamentos</h2>
      </div>

      {appointments.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-6">
          Você não tem agendamentos futuros.
        </p>
      ) : (
        <div className="space-y-3">
          {appointments.map((apt) => {
            const status = STATUS_CONFIG[apt.status]
            return (
              <div
                key={apt.id}
                className="flex items-center justify-between p-3 rounded-lg bg-muted/50"
              >
                <div className="flex flex-col gap-0.5">
                  <span className="text-sm font-medium text-foreground">{apt.serviceName}</span>
                  <span className="text-xs text-muted-foreground">
                    {new Date(apt.date + 'T00:00:00').toLocaleDateString('pt-BR', {
                      day: '2-digit',
                      month: 'short',
                    })}{' '}
                    às {apt.time}
                  </span>
                </div>
                <Badge variant="outline" className={cn('font-normal', status.className)}>
                  {status.label}
                </Badge>
              </div>
            )
          })}
        </div>
      )}

      <div className="mt-4">
        <Button variant="ghost" size="sm" asChild className="w-full text-muted-foreground">
          <Link href="/meus-agendamentos">Ver todos os agendamentos</Link>
        </Button>
      </div>
    </div>
  )
}
