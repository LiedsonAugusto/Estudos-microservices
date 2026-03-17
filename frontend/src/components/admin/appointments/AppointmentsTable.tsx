'use client'

import { useState } from 'react'
import { type AdminAppointment, type AppointmentStatus } from '@/types'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { cn } from '@/lib/utils'

const STATUS_CONFIG: Record<AppointmentStatus, { label: string; className: string }> = {
  SCHEDULED: { label: 'Agendado',       className: 'bg-indigo-50 text-indigo-600 border-indigo-200 dark:bg-indigo-950 dark:text-indigo-300 dark:border-indigo-900' },
  CONFIRMED: { label: 'Confirmado',     className: 'bg-green-100 text-green-700 border-green-200' },
  COMPLETED: { label: 'Concluído',      className: 'bg-gray-100 text-gray-600 border-gray-200' },
  CANCELLED: { label: 'Cancelado',      className: 'bg-red-100 text-red-700 border-red-200' },
  NO_SHOW:   { label: 'Não compareceu', className: 'bg-orange-100 text-orange-700 border-orange-200' },
}

function formatDate(date: string) {
  return new Date(date + 'T00:00:00').toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

type Props = {
  appointments: AdminAppointment[]
  onUpdateStatus: (id: string, status: AppointmentStatus) => void
}

export function AppointmentsTable({ appointments, onUpdateStatus }: Props) {
  const [cancelTarget, setCancelTarget] = useState<AdminAppointment | null>(null)

  if (appointments.length === 0) {
    return (
      <div className="rounded-lg border border-border bg-card">
        <p className="text-center text-muted-foreground py-16 text-sm">
          Nenhum agendamento encontrado.
        </p>
      </div>
    )
  }

  return (
    <>
      <div className="rounded-lg border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="px-6">Data / Hora</TableHead>
              <TableHead>Cidadão</TableHead>
              <TableHead>Serviço</TableHead>
              <TableHead>Código</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right px-6">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {appointments.map((appointment) => {
              const status = STATUS_CONFIG[appointment.status]

              return (
                <TableRow key={appointment.id}>
                  <TableCell className="px-6">
                    <div className="font-medium">{formatDate(appointment.date)}</div>
                    <div className="text-sm text-muted-foreground">{appointment.time}</div>
                  </TableCell>
                  <TableCell>
                    <div className="font-medium">{appointment.citizenName}</div>
                    <div className="text-sm text-muted-foreground">{appointment.citizenEmail}</div>
                  </TableCell>
                  <TableCell>{appointment.serviceName}</TableCell>
                  <TableCell>
                    <span className="font-mono text-sm">{appointment.confirmationCode}</span>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className={cn('font-normal', status.className)}>
                      {status.label}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right px-6">
                    <div className="flex justify-end gap-2">
                      {appointment.status === 'SCHEDULED' && (
                        <Button size="xs" variant="outline" onClick={() => onUpdateStatus(appointment.id, 'CONFIRMED')}>
                          Confirmar
                        </Button>
                      )}
                      {(appointment.status === 'CONFIRMED') && (
                        <>
                          <Button size="xs" variant="outline" onClick={() => onUpdateStatus(appointment.id, 'COMPLETED')}>
                            Concluir
                          </Button>
                          <Button size="xs" variant="outline" onClick={() => onUpdateStatus(appointment.id, 'NO_SHOW')}>
                            Não compareceu
                          </Button>
                        </>
                      )}
                      {(appointment.status === 'SCHEDULED' || appointment.status === 'CONFIRMED') && (
                        <Button size="xs" variant="destructive" onClick={() => setCancelTarget(appointment)}>
                          Cancelar
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </div>

      <AlertDialog open={!!cancelTarget} onOpenChange={(open) => !open && setCancelTarget(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar agendamento</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja cancelar o agendamento de{' '}
              <strong>{cancelTarget?.citizenName}</strong> para{' '}
              <strong>{cancelTarget?.serviceName}</strong>?
              Esta ação não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Voltar</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-white hover:bg-destructive/90"
              onClick={() => {
                if (cancelTarget) onUpdateStatus(cancelTarget.id, 'CANCELLED')
                setCancelTarget(null)
              }}
            >
              Confirmar cancelamento
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
