'use client'

import { type AdminUser, type UserRole } from '@/types'
import { Badge } from '@/components/ui/badge'
import { Switch } from '@/components/ui/switch'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

const ROLE_CONFIG: Record<UserRole, { label: string; className: string }> = {
  ADMIN:   { label: 'Admin',    className: 'bg-purple-100 text-purple-700 border-purple-200' },
  CITIZEN: { label: 'Cidadão', className: 'bg-blue-100 text-blue-700 border-blue-200' },
}

function formatCPF(cpf: string) {
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
}

type Props = {
  users: AdminUser[]
  onToggleActive: (user: AdminUser) => void
}

export function UsersTable({ users, onToggleActive }: Props) {
  if (users.length === 0) {
    return (
      <div className="rounded-lg border border-border bg-card">
        <p className="text-center text-muted-foreground py-16 text-sm">
          Nenhum usuário encontrado.
        </p>
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="px-6">Nome</TableHead>
            <TableHead>Email</TableHead>
            <TableHead>CPF</TableHead>
            <TableHead>Perfil</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right px-6">Ativar/Desativar</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((user) => {
            const role = ROLE_CONFIG[user.role]
            return (
              <TableRow key={user.id}>
                <TableCell className="px-6 font-medium">{user.name}</TableCell>
                <TableCell className="text-muted-foreground">{user.email}</TableCell>
                <TableCell>
                  <span className="font-mono text-sm">{formatCPF(user.cpf)}</span>
                </TableCell>
                <TableCell>
                  <Badge variant="outline" className={role.className}>
                    {role.label}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge
                    variant="outline"
                    className={
                      user.active
                        ? 'bg-green-100 text-green-700 border-green-200'
                        : 'bg-gray-100 text-gray-500 border-gray-200'
                    }
                  >
                    {user.active ? 'Ativo' : 'Inativo'}
                  </Badge>
                </TableCell>
                <TableCell className="text-right px-6">
                  <Switch
                    checked={user.active}
                    onCheckedChange={() => onToggleActive(user)}
                  />
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
