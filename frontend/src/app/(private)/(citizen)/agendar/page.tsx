'use client'

import { useState } from 'react'
import { type Service, type BookingStep } from '@/types'
import { BookingStepper } from '@/components/citizen/booking/BookingStepper'
import { ServiceStep } from '@/components/citizen/booking/ServiceStep'
import { DateStep } from '@/components/citizen/booking/DateStep'
import { TimeSlotStep } from '@/components/citizen/booking/TimeSlotStep'
import { ConfirmStep } from '@/components/citizen/booking/ConfirmStep'

// dados simulados — substituir por fetch à API futuramente
const mockServices: Service[] = [
  { id: '1', name: 'Renovação de CNH',       description: 'Renovação da Carteira Nacional de Habilitação.',           durationMinutes: 30, active: true },
  { id: '2', name: '1ª Via de RG',           description: 'Emissão da primeira via do documento de identidade.',      durationMinutes: 20, active: true },
  { id: '3', name: 'Emissão de CPF',         description: 'Cadastro de Pessoa Física junto à Receita Federal.',       durationMinutes: 15, active: true },
  { id: '4', name: 'Certidão de Nascimento', description: 'Segunda via de certidão de nascimento.',                   durationMinutes: 45, active: true },
]

function generateMockSlots(serviceId: string) {
  const times = [
    { start: '08:00', end: '08:30' },
    { start: '08:30', end: '09:00' },
    { start: '09:00', end: '09:30' },
    { start: '09:30', end: '10:00' },
    { start: '10:00', end: '10:30' },
    { start: '10:30', end: '11:00' },
    { start: '14:00', end: '14:30' },
    { start: '14:30', end: '15:00' },
    { start: '15:00', end: '15:30' },
    { start: '15:30', end: '16:00' },
  ]

  return times.map((t, i) => ({
    id: `${serviceId}-slot-${i}`,
    startTime: t.start,
    endTime: t.end,
    available: i !== 2 && i !== 5,
  }))
}

function generateConfirmationCode() {
  const chars = 'ABCDEF0123456789'
  let code = 'AGD-'
  for (let i = 0; i < 6; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
  }
  return code
}

export default function BookingPage() {
  const [step, setStep] = useState<BookingStep>('service')
  const [selectedService, setSelectedService] = useState<Service | null>(null)
  const [selectedDate, setSelectedDate] = useState('')
  const [selectedSlotId, setSelectedSlotId] = useState('')
  const [confirmed, setConfirmed] = useState(false)
  const [confirmationCode, setConfirmationCode] = useState('')

  const slots = selectedService ? generateMockSlots(selectedService.id) : []
  const selectedSlot = slots.find((s) => s.id === selectedSlotId)

  function handleConfirm() {
    setConfirmationCode(generateConfirmationCode())
    setConfirmed(true)
  }

  return (
    <div className="flex flex-col gap-8 max-w-2xl mx-auto">

      <div>
        <h1 className="text-2xl font-bold text-foreground">Agendar serviço</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Siga os passos abaixo para realizar seu agendamento.
        </p>
      </div>

      <BookingStepper currentStep={step} />

      <div className="rounded-lg border border-border bg-card p-6">
        {step === 'service' && (
          <ServiceStep
            services={mockServices}
            selected={selectedService}
            onSelect={setSelectedService}
            onNext={() => setStep('date')}
          />
        )}

        {step === 'date' && (
          <DateStep
            selected={selectedDate}
            onSelect={(date) => {
              setSelectedDate(date)
              setSelectedSlotId('')
            }}
            onNext={() => setStep('time')}
            onBack={() => setStep('service')}
          />
        )}

        {step === 'time' && (
          <TimeSlotStep
            slots={slots}
            selected={selectedSlotId}
            onSelect={setSelectedSlotId}
            onNext={() => setStep('confirm')}
            onBack={() => setStep('date')}
          />
        )}

        {step === 'confirm' && selectedService && selectedSlot && (
          <ConfirmStep
            service={selectedService}
            date={selectedDate}
            time={`${selectedSlot.startTime} – ${selectedSlot.endTime}`}
            confirmed={confirmed}
            confirmationCode={confirmationCode}
            onConfirm={handleConfirm}
            onBack={() => setStep('time')}
          />
        )}
      </div>

    </div>
  )
}
