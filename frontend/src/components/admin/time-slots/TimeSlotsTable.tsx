'use client'

import { Pencil } from 'lucide-react'
import { type TimeSlot } from '@/types'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Switch } from '@/components/ui/switch'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

function formatDate(date: string) {
  return new Date(date + 'T00:00:00').toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

type Props = {
  timeSlots: TimeSlot[]
  onEdit: (timeSlot: TimeSlot) => void
  onToggleActive: (timeSlot: TimeSlot) => void
}

export function TimeSlotsTable({ timeSlots, onEdit, onToggleActive }: Props) {
  if (timeSlots.length === 0) {
    return (
      <div className="rounded-lg border border-border bg-card">
        <p className="text-center text-muted-foreground py-16 text-sm">
          Nenhum horário encontrado.
        </p>
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="px-6">Serviço</TableHead>
            <TableHead>Data</TableHead>
            <TableHead>Início</TableHead>
            <TableHead>Fim</TableHead>
            <TableHead>Vagas</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right px-6">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {timeSlots.map((slot) => (
            <TableRow key={slot.id}>
              <TableCell className="px-6 font-medium">{slot.serviceName}</TableCell>
              <TableCell>{formatDate(slot.date)}</TableCell>
              <TableCell>{slot.startTime}</TableCell>
              <TableCell>{slot.endTime}</TableCell>
              <TableCell>
                <span className={slot.availableSlots === 0 ? 'text-red-600 font-medium' : ''}>
                  {slot.availableSlots}/{slot.totalSlots}
                </span>
              </TableCell>
              <TableCell>
                <div className="flex items-center gap-2">
                  <Switch
                    checked={slot.active}
                    onCheckedChange={() => onToggleActive(slot)}
                  />
                  <Badge
                    variant="outline"
                    className={
                      slot.active
                        ? 'bg-green-100 text-green-700 border-green-200'
                        : 'bg-gray-100 text-gray-500 border-gray-200'
                    }
                  >
                    {slot.active ? 'Ativo' : 'Inativo'}
                  </Badge>
                </div>
              </TableCell>
              <TableCell className="text-right px-6">
                <Button
                  variant="ghost"
                  size="icon-sm"
                  onClick={() => onEdit(slot)}
                >
                  <Pencil className="w-4 h-4" />
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
