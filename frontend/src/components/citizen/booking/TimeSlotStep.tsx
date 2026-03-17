'use client'

import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

type SlotOption = {
  id: string
  startTime: string
  endTime: string
  available: boolean
}

type Props = {
  slots: SlotOption[]
  selected: string
  onSelect: (slotId: string) => void
  onNext: () => void
  onBack: () => void
}

export function TimeSlotStep({ slots, selected, onSelect, onNext, onBack }: Props) {
  const availableSlots = slots.filter((s) => s.available)

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="text-lg font-semibold text-foreground">Escolha o horário</h2>
        <p className="text-sm text-muted-foreground mt-1">Selecione um dos horários disponíveis.</p>
      </div>

      {availableSlots.length === 0 ? (
        <p className="text-sm text-muted-foreground text-center py-8">
          Nenhum horário disponível para esta data. Tente outra data.
        </p>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          {slots.map((slot) => (
            <button
              key={slot.id}
              type="button"
              disabled={!slot.available}
              onClick={() => onSelect(slot.id)}
              className={cn(
                'p-3 rounded-lg border text-center text-sm transition-colors',
                !slot.available && 'opacity-40 cursor-not-allowed border-border bg-muted',
                slot.available && selected === slot.id
                  ? 'border-indigo-500 bg-indigo-50 text-indigo-700 dark:bg-indigo-950/50 dark:text-indigo-300 dark:border-indigo-700 cursor-pointer'
                  : slot.available && 'border-border bg-card hover:bg-muted/50 cursor-pointer'
              )}
            >
              <span className="font-medium">{slot.startTime}</span>
              <span className="text-muted-foreground"> – {slot.endTime}</span>
            </button>
          ))}
        </div>
      )}

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
