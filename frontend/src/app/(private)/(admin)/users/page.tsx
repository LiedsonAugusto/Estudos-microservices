'use client'

import { useState, useMemo } from 'react'
import { Search } from 'lucide-react'
import { type AdminUser } from '@/types'
import { Input } from '@/components/ui/input'
import { UsersTable } from '@/components/admin/users/UsersTable'

// dados simulados — substituir por fetch à API futuramente
const initialUsers: AdminUser[] = [
  { id: '1', name: 'João Silva',    email: 'joao@email.com',     cpf: '12345678901', role: 'CITIZEN', active: true  },
  { id: '2', name: 'Maria Lima',    email: 'maria@email.com',    cpf: '23456789012', role: 'CITIZEN', active: true  },
  { id: '3', name: 'Pedro Souza',   email: 'pedro@email.com',    cpf: '34567890123', role: 'CITIZEN', active: true  },
  { id: '4', name: 'Ana Costa',     email: 'ana@email.com',      cpf: '45678901234', role: 'CITIZEN', active: false },
  { id: '5', name: 'Carlos Melo',   email: 'carlos@email.com',   cpf: '56789012345', role: 'CITIZEN', active: true  },
  { id: '6', name: 'Fernanda Dias', email: 'fernanda@email.com', cpf: '67890123456', role: 'CITIZEN', active: true  },
  { id: '7', name: 'Ricardo Alves', email: 'ricardo@email.com',  cpf: '78901234567', role: 'CITIZEN', active: true  },
  { id: '8', name: 'Administrador', email: 'admin@email.com',    cpf: '00000000000', role: 'ADMIN',   active: true  },
]

export default function UsersPage() {
  const [users, setUsers] = useState<AdminUser[]>(initialUsers)
  const [search, setSearch] = useState('')

  function handleToggleActive(user: AdminUser) {
    setUsers((prev) =>
      prev.map((u) => u.id === user.id ? { ...u, active: !u.active } : u)
    )
  }

  const filtered = useMemo(() => {
    if (!search.trim()) return users
    const q = search.trim().toLowerCase()
    return users.filter(
      (u) =>
        u.name.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        u.cpf.includes(q.replace(/\D/g, ''))
    )
  }, [users, search])

  return (
    <div className="flex flex-col gap-6">

      <h1 className="text-2xl font-bold text-foreground">Usuários</h1>

      {/* Busca */}
      <div className="relative w-80">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input
          className="pl-9"
          placeholder="Buscar por nome, email ou CPF"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <UsersTable
        users={filtered}
        onToggleActive={handleToggleActive}
      />

    </div>
  )
}
