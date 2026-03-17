import { TrendingUp, TrendingDown, type LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

type Props = {
  title: string
  value: number
  icon: LucideIcon
  trend: {
    value: number
    direction: 'up' | 'down'
  }
}

export default function StatisticsCard({ title, value, icon: Icon, trend }: Props) {
  const isUp = trend.direction === 'up'
  const TrendIcon = isUp ? TrendingUp : TrendingDown

  return (
    <div className="flex items-center justify-between bg-indigo-600 dark:bg-indigo-950 text-white p-5 rounded-lg flex-1">
      <div className="flex flex-col gap-1">
        <span className="text-sm text-indigo-200 dark:text-indigo-300">{title}</span>
        <span className="text-3xl font-bold">{value}</span>
        <div className={cn("flex items-center gap-1 text-xs font-medium", isUp ? "text-emerald-300" : "text-red-300")}>
          <TrendIcon className="w-3 h-3" />
          <span>{trend.value}% vs ontem</span>
        </div>
      </div>
      <div className="bg-indigo-500 dark:bg-indigo-900 p-3 rounded-full">
        <Icon className="w-6 h-6 text-indigo-100 dark:text-indigo-300" />
      </div>
    </div>
  )
}
