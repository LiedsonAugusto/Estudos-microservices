'use client'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

type Props = {
  selected: string
  onSelect: (date: string) => void
  onNext: () => void
  onBack: () => void
}

function getTomorrow() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
}

export function DateStep({ selected, onSelect, onNext, onBack }: Props) {
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-lg font-semibold text-foreground">Escolha a data</h2>
        <p className="text-sm text-muted-foreground mt-1">Selecione a data desejada para o atendimento.</p>
      </div>

      <div className="max-w-xs">
        <Label htmlFor="booking-date">Data</Label>
        <Input
          id="booking-date"
          type="date"
          min={getTomorrow()}
          value={selected}
          onChange={(e) => onSelect(e.target.value)}
          className="mt-1.5"
        />
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Voltar
        </Button>
        <Button onClick={onNext} disabled={!selected}>
          Próximo
        </Button>
      </div>
    </div>
  )
}
