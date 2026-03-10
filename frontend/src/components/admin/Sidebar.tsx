'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useTheme } from 'next-themes'
import { useEffect, useState } from 'react'
import {
  CalendarCheck,
  LayoutDashboard,
  Briefcase,
  Clock,
  CalendarDays,
  Users,
  LogOut,
  Sun,
  Moon,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { label: 'Dashboard',    href: '/admin',            icon: LayoutDashboard },
  { label: 'Serviços',     href: '/services',   icon: Briefcase       },
  { label: 'Horários',     href: '/time-slots', icon: Clock           },
  { label: 'Agendamentos', href: '/appointments',icon: CalendarDays   },
  { label: 'Usuários',     href: '/users',      icon: Users           },
]

export function Sidebar() {
  const pathname = usePathname()
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const isDark = theme === 'dark'

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-blue-700 text-white">

      <div className="flex items-center gap-3 px-6 py-5 border-b border-blue-600">
        <CalendarCheck className="w-7 h-7" />
        <span className="text-lg font-semibold">AgendaFácil</span>
      </div>

      <nav className="flex flex-col gap-1 px-3 py-4 flex-1">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive =
            item.href === '/admin'
              ? pathname === '/admin'
              : pathname.startsWith(item.href)

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-white text-blue-700'
                  : 'text-blue-100 hover:bg-blue-600'
              )}
            >
              <Icon className="w-5 h-5" />
              {item.label}
            </Link>
          )
        })}
      </nav>
      <div className="border-t border-blue-600 px-4 py-4 space-y-3">

        {mounted && (
          <button
            onClick={() => setTheme(isDark ? 'light' : 'dark')}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-blue-100 hover:bg-blue-600 transition-colors cursor-pointer"
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            {isDark ? 'Tema claro' : 'Tema escuro'}
          </button>
        )}

        <div className="flex items-center gap-3 px-2">
          <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-sm font-bold">
            A
          </div>
          <div className="flex flex-col min-w-0">
            <span className="text-sm font-medium truncate">Administrador</span>
            <span className="text-xs text-blue-300 truncate">admin@email.com</span>
          </div>
        </div>

        <Link
          href="/login"
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-blue-100 hover:bg-blue-600 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair
        </Link>

      </div>
    </aside>
  )
}
