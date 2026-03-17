'use client'

import { Check } from 'lucide-react'
import { cn } from '@/lib/utils'
import { type BookingStep } from '@/types'

const steps: { key: BookingStep; label: string }[] = [
  { key: 'service', label: 'Serviço' },
  { key: 'date',    label: 'Data' },
  { key: 'time',    label: 'Horário' },
  { key: 'confirm', label: 'Confirmação' },
]

type Props = {
  currentStep: BookingStep
}

export function BookingStepper({ currentStep }: Props) {
  const currentIndex = steps.findIndex((s) => s.key === currentStep)

  return (
    <div className="flex items-center justify-center gap-2">
      {steps.map((step, index) => {
        const isCompleted = index < currentIndex
        const isCurrent = index === currentIndex

        return (
          <div key={step.key} className="flex items-center gap-2">
            <div className="flex flex-col items-center gap-1">
              <div
                className={cn(
                  'w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-colors',
                  isCompleted && 'bg-indigo-600 dark:bg-indigo-500 text-white',
                  isCurrent && 'bg-indigo-600 dark:bg-indigo-500 text-white ring-2 ring-indigo-300 dark:ring-indigo-700',
                  !isCompleted && !isCurrent && 'bg-muted text-muted-foreground'
                )}
              >
                {isCompleted ? <Check className="w-4 h-4" /> : index + 1}
              </div>
              <span
                className={cn(
                  'text-xs',
                  isCurrent ? 'text-foreground font-medium' : 'text-muted-foreground'
                )}
              >
                {step.label}
              </span>
            </div>

            {index < steps.length - 1 && (
              <div
                className={cn(
                  'w-12 h-0.5 mb-5',
                  index < currentIndex ? 'bg-indigo-600 dark:bg-indigo-500' : 'bg-muted'
                )}
              />
            )}
          </div>
        )
      })}
    </div>
  )
}
