export type Service = {
  id: string
  name: string
  description: string
  durationMinutes: number
  active: boolean
}

export type AppointmentStatus =
  | 'SCHEDULED'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW'

export type Appointment = {
  id: string
  time: string
  citizenName: string
  serviceName: string
  status: AppointmentStatus
}

export type AdminAppointment = {
  id: string
  confirmationCode: string
  citizenName: string
  citizenEmail: string
  serviceName: string
  date: string
  time: string
  status: AppointmentStatus
  notes?: string
  cancellationReason?: string
}
