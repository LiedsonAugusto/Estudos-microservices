'use client'

import { Pencil } from 'lucide-react'
import { type Service } from '@/types'
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

function formatDuration(minutes: number) {
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m === 0 ? `${h}h` : `${h}h ${m}min`
}

type Props = {
  services: Service[]
  onEdit: (service: Service) => void
  onToggleActive: (service: Service) => void
}

export function ServicesTable({ services, onEdit, onToggleActive }: Props) {
  if (services.length === 0) {
    return (
      <div className="rounded-lg border border-border bg-card">
        <p className="text-center text-muted-foreground py-16 text-sm">
          Nenhum serviço cadastrado.
        </p>
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="px-6">Nome</TableHead>
            <TableHead>Descrição</TableHead>
            <TableHead>Duração</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right px-6">Ações</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {services.map((service) => (
            <TableRow key={service.id}>
              <TableCell className="px-6 font-medium">{service.name}</TableCell>
              <TableCell className="text-muted-foreground max-w-xs truncate">
                {service.description || '—'}
              </TableCell>
              <TableCell>{formatDuration(service.durationMinutes)}</TableCell>
              <TableCell>
                <div className="flex items-center gap-2">
                  <Switch
                    checked={service.active}
                    onCheckedChange={() => onToggleActive(service)}
                  />
                  <Badge
                    variant="outline"
                    className={
                      service.active
                        ? 'bg-green-100 text-green-700 border-green-200'
                        : 'bg-gray-100 text-gray-500 border-gray-200'
                    }
                  >
                    {service.active ? 'Ativo' : 'Inativo'}
                  </Badge>
                </div>
              </TableCell>
              <TableCell className="text-right px-6">
                <Button
                  variant="ghost"
                  size="icon-sm"
                  onClick={() => onEdit(service)}
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
