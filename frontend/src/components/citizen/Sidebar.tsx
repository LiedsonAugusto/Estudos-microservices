'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useTheme } from 'next-themes'
import { useEffect, useState } from 'react'
import {
  CalendarCheck,
  Home,
  CalendarPlus,
  CalendarDays,
  User,
  LogOut,
  Sun,
  Moon,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const navItems = [
  { label: 'Início',             href: '/home',               icon: Home },
  { label: 'Agendar',            href: '/agendar',            icon: CalendarPlus },
  { label: 'Meus Agendamentos',  href: '/meus-agendamentos',  icon: CalendarDays },
  { label: 'Meu Perfil',         href: '/perfil',             icon: User },
]

export function CitizenSidebar() {
  const pathname = usePathname()
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const isDark = theme === 'dark'

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-indigo-600 dark:bg-indigo-950 text-white">

      <div className="flex items-center gap-3 px-6 py-5 border-b border-indigo-500 dark:border-indigo-900">
        <CalendarCheck className="w-7 h-7" />
        <span className="text-lg font-semibold">AgendaFácil</span>
      </div>

      <nav className="flex flex-col gap-1 px-3 py-4 flex-1">
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive =
            item.href === '/home'
              ? pathname === '/home'
              : pathname.startsWith(item.href)

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-white/20 text-white'
                  : 'text-indigo-100 hover:bg-white/10'
              )}
            >
              <Icon className="w-5 h-5" />
              {item.label}
            </Link>
          )
        })}
      </nav>

      <div className="border-t border-indigo-500 dark:border-indigo-900 px-4 py-4 space-y-3">

        {mounted && (
          <button
            onClick={() => setTheme(isDark ? 'light' : 'dark')}
            className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-indigo-100 hover:bg-white/10 transition-colors cursor-pointer"
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            {isDark ? 'Tema claro' : 'Tema escuro'}
          </button>
        )}

        <div className="flex items-center gap-3 px-2">
          <div className="w-8 h-8 rounded-full bg-indigo-400 dark:bg-indigo-800 flex items-center justify-center text-sm font-bold">
            M
          </div>
          <div className="flex flex-col min-w-0">
            <span className="text-sm font-medium truncate">Maria Lima</span>
            <span className="text-xs text-indigo-200 dark:text-indigo-400 truncate">maria@email.com</span>
          </div>
        </div>

        <Link
          href="/login"
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm text-indigo-100 hover:bg-white/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Sair
        </Link>

      </div>
    </aside>
  )
}
