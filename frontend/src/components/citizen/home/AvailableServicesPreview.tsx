'use client'

import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Briefcase, Clock } from 'lucide-react'
import { type Service } from '@/types'

type Props = {
  services: Service[]
}

export function AvailableServicesPreview({ services }: Props) {
  return (
    <div className="rounded-lg border border-border bg-card p-6">
      <div className="flex items-center gap-2 mb-4">
        <Briefcase className="w-5 h-5 text-muted-foreground" />
        <h2 className="text-base font-semibold text-foreground">Serviços disponíveis</h2>
      </div>

      {services.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-6">
          Nenhum serviço disponível no momento.
        </p>
      ) : (
        <div className="space-y-3">
          {services.map((service) => (
            <div
              key={service.id}
              className="flex items-center justify-between p-3 rounded-lg bg-muted/50"
            >
              <div className="flex flex-col gap-0.5">
                <span className="text-sm font-medium text-foreground">{service.name}</span>
                {service.description && (
                  <span className="text-xs text-muted-foreground line-clamp-1">
                    {service.description}
                  </span>
                )}
              </div>
              <div className="flex items-center gap-1 text-xs text-muted-foreground shrink-0 ml-4">
                <Clock className="w-3.5 h-3.5" />
                <span>{service.durationMinutes} min</span>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4">
        <Button variant="ghost" size="sm" asChild className="w-full text-muted-foreground">
          <Link href="/agendar">Ver todos e agendar</Link>
        </Button>
      </div>
    </div>
  )
}
