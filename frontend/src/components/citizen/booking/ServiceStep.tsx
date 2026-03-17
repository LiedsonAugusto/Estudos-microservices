'use client'

import { Button } from '@/components/ui/button'
import { Clock } from 'lucide-react'
import { cn } from '@/lib/utils'
import { type Service } from '@/types'

type Props = {
  services: Service[]
  selected: Service | null
  onSelect: (service: Service) => void
  onNext: () => void
}

export function ServiceStep({ services, selected, onSelect, onNext }: Props) {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-lg font-semibold text-foreground">Escolha o serviço</h2>
        <p className="text-sm text-muted-foreground mt-1">Selecione o serviço que deseja agendar.</p>
      </div>

      <div className="grid sm:grid-cols-2 gap-3">
        {services.map((service) => (
          <button
            key={service.id}
            type="button"
            onClick={() => onSelect(service)}
            className={cn(
              'flex flex-col gap-2 p-4 rounded-lg border text-left transition-colors cursor-pointer',
              selected?.id === service.id
                ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/50 dark:border-indigo-700'
                : 'border-border bg-card hover:bg-muted/50'
            )}
          >
            <span className="text-sm font-medium text-foreground">{service.name}</span>
            {service.description && (
              <span className="text-xs text-muted-foreground line-clamp-2">{service.description}</span>
            )}
            <div className="flex items-center gap-1 text-xs text-muted-foreground mt-auto">
              <Clock className="w-3.5 h-3.5" />
              <span>{service.durationMinutes} min</span>
            </div>
          </button>
        ))}
      </div>

      <div className="flex justify-end">
        <Button onClick={onNext} disabled={!selected}>
          Próximo
        </Button>
      </div>
    </div>
  )
}
