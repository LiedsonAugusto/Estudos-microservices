'use client'

import { useState } from 'react'
import { type CitizenAppointment, type AppointmentStatus } from '@/types'
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
  SCHEDULED:  { label: 'Agendado',       className: 'bg-indigo-50 text-indigo-600 border-indigo-200 dark:bg-indigo-950 dark:text-indigo-300 dark:border-indigo-900' },
  CONFIRMED:  { label: 'Confirmado',     className: 'bg-green-100 text-green-700 border-green-200' },
  COMPLETED:  { label: 'Concluído',      className: 'bg-gray-100 text-gray-600 border-gray-200' },
  CANCELLED:  { label: 'Cancelado',      className: 'bg-red-100 text-red-700 border-red-200' },
  NO_SHOW:    { label: 'Não compareceu', className: 'bg-orange-100 text-orange-700 border-orange-200' },
}

type Props = {
  appointments: CitizenAppointment[]
  onCancel: (id: string) => void
}

export function CitizenAppointmentsTable({ appointments, onCancel }: Props) {
  const [cancelId, setCancelId] = useState<string | null>(null)

  function handleConfirmCancel() {
    if (cancelId) {
      onCancel(cancelId)
      setCancelId(null)
    }
  }

  return (
    <>
      <div className="rounded-lg border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="px-6">Data</TableHead>
              <TableHead>Horário</TableHead>
              <TableHead>Serviço</TableHead>
              <TableHead>Código</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right px-6">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {appointments.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-muted-foreground py-8">
                  Nenhum agendamento encontrado.
                </TableCell>
              </TableRow>
            ) : (
              appointments.map((apt) => {
                const status = STATUS_CONFIG[apt.status]
                const canCancel = apt.status === 'SCHEDULED' || apt.status === 'CONFIRMED'

                return (
                  <TableRow key={apt.id}>
                    <TableCell className="px-6 font-medium">
                      {new Date(apt.date + 'T00:00:00').toLocaleDateString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                      })}
                    </TableCell>
                    <TableCell>{apt.time}</TableCell>
                    <TableCell>{apt.serviceName}</TableCell>
                    <TableCell>
                      <span className="font-mono text-xs">{apt.confirmationCode}</span>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className={cn('font-normal', status.className)}>
                        {status.label}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right px-6">
                      {canCancel && (
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/30"
                          onClick={() => setCancelId(apt.id)}
                        >
                          Cancelar
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })
            )}
          </TableBody>
        </Table>
      </div>

      <AlertDialog open={!!cancelId} onOpenChange={(open) => !open && setCancelId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancelar agendamento?</AlertDialogTitle>
            <AlertDialogDescription>
              Essa ação não pode ser desfeita. O horário ficará disponível para outros cidadãos.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Voltar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmCancel}
              className="bg-red-600 hover:bg-red-700 text-white"
            >
              Confirmar cancelamento
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
