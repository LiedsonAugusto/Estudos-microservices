'use client'

import { Button } from '@/components/ui/button'
import { CalendarCheck, CheckCircle } from 'lucide-react'
import Link from 'next/link'
import { type Service } from '@/types'

type Props = {
  service: Service
  date: string
  time: string
  confirmed: boolean
  confirmationCode: string
  onConfirm: () => void
  onBack: () => void
}

export function ConfirmStep({ service, date, time, confirmed, confirmationCode, onConfirm, onBack }: Props) {
  if (confirmed) {
    return (
      <div className="flex flex-col items-center gap-4 py-8">
        <div className="w-16 h-16 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center">
          <CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400" />
        </div>
        <h2 className="text-xl font-semibold text-foreground">Agendamento confirmado!</h2>
        <p className="text-sm text-muted-foreground text-center max-w-sm">
          Seu agendamento foi realizado com sucesso. Guarde o código de confirmação abaixo.
        </p>
        <div className="bg-muted rounded-lg px-6 py-3">
          <span className="text-lg font-mono font-bold text-foreground">{confirmationCode}</span>
        </div>
        <div className="flex gap-3 mt-4">
          <Button variant="outline" asChild>
            <Link href="/meus-agendamentos">Meus agendamentos</Link>
          </Button>
          <Button asChild>
            <Link href="/agendar">Novo agendamento</Link>
          </Button>
        </div>
      </div>
    )
  }

  const formattedDate = new Date(date + 'T00:00:00').toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-lg font-semibold text-foreground">Confirme seu agendamento</h2>
        <p className="text-sm text-muted-foreground mt-1">Revise os dados antes de confirmar.</p>
      </div>

      <div className="rounded-lg border border-border bg-card p-6 space-y-4">
        <div className="flex items-center gap-3 mb-2">
          <CalendarCheck className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
          <span className="font-semibold text-foreground">Resumo</span>
        </div>

        <div className="grid gap-3 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Serviço</span>
            <span className="font-medium text-foreground">{service.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Data</span>
            <span className="font-medium text-foreground capitalize">{formattedDate}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Horário</span>
            <span className="font-medium text-foreground">{time}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Duração estimada</span>
            <span className="font-medium text-foreground">{service.durationMinutes} min</span>
          </div>
        </div>
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Voltar
        </Button>
        <Button onClick={onConfirm}>
          Confirmar agendamento
        </Button>
      </div>
    </div>
  )
}
