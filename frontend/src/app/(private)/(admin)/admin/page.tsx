import AppointmentsToday from '@/components/admin/AppointmentsToday'
import GraphicBar from '@/components/admin/GraphicBar'
import StatisticsCard from '@/components/admin/StatisticsCard'
import { CalendarDays, CheckCircle, XCircle } from 'lucide-react'

export default function DashboardPage() {
  return (
    <div className="flex flex-col gap-8">

      <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>

      {/* Cards de estatísticas */}
      <div className="flex gap-4">
        <StatisticsCard
          title="Agendamentos hoje"
          value={24}
          icon={CalendarDays}
          trend={{ value: 12, direction: 'up' }}
        />
        <StatisticsCard
          title="Concluídos hoje"
          value={18}
          icon={CheckCircle}
          trend={{ value: 5, direction: 'up' }}
        />
        <StatisticsCard
          title="Cancelados hoje"
          value={3}
          icon={XCircle}
          trend={{ value: 2, direction: 'down' }}
        />
      </div>

      {/* Gráfico */}
      <div className="rounded-lg border border-border bg-card p-6">
        <h2 className="text-base font-semibold text-foreground mb-4">
          Atendimentos — últimos 7 dias
        </h2>
        <GraphicBar />
      </div>

      {/* Tabela de agendamentos de hoje */}
      <AppointmentsToday />

    </div>
  )
}
