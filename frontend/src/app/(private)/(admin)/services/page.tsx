'use client'

import { useState } from 'react'
import { Plus } from 'lucide-react'
import { type Service } from '@/types'
import { Button } from '@/components/ui/button'
import { ServicesTable } from '@/components/admin/services/ServicesTable'
import { ServiceFormDialog } from '@/components/admin/services/ServiceFormDialog'

// dados simulados — substituir por fetch à API futuramente
const initialServices: Service[] = [
  { id: '1', name: 'Renovação de CNH',  description: 'Renovação da Carteira Nacional de Habilitação.',  durationMinutes: 30,  active: true  },
  { id: '2', name: '1ª Via de RG',      description: 'Emissão da primeira via do documento de identidade.', durationMinutes: 20, active: true  },
  { id: '3', name: 'Emissão de CPF',    description: '',                                                  durationMinutes: 15,  active: true  },
  { id: '4', name: 'Certidão de Nascimento', description: 'Segunda via de certidão de nascimento.',       durationMinutes: 45, active: false },
]

export default function ServicesPage() {
  const [services, setServices] = useState<Service[]>(initialServices)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedService, setSelectedService] = useState<Service | null>(null)

  function handleOpenCreate() {
    setSelectedService(null)
    setDialogOpen(true)
  }

  function handleOpenEdit(service: Service) {
    setSelectedService(service)
    setDialogOpen(true)
  }

  function handleToggleActive(service: Service) {
    setServices((prev) =>
      prev.map((s) => s.id === service.id ? { ...s, active: !s.active } : s)
    )
  }

  function handleSubmit(data: Omit<Service, 'id' | 'active'>, id?: string) {
    if (id) {
      setServices((prev) =>
        prev.map((s) => s.id === id ? { ...s, ...data } : s)
      )
    } else {
      const newService: Service = {
        id: crypto.randomUUID(),
        active: true,
        ...data,
        description: data.description ?? '',
      }
      setServices((prev) => [...prev, newService])
    }
  }

  return (
    <div className="flex flex-col gap-6">

      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-foreground">Serviços</h1>
        <Button onClick={handleOpenCreate}>
          <Plus className="w-4 h-4" />
          Novo serviço
        </Button>
      </div>

      <ServicesTable
        services={services}
        onEdit={handleOpenEdit}
        onToggleActive={handleToggleActive}
      />

      <ServiceFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        service={selectedService}
        onSubmit={handleSubmit}
      />

    </div>
  )
}
