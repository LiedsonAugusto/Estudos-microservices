'use client'

import { type CitizenProfile } from '@/types'
import { ProfileForm } from '@/components/citizen/profile/ProfileForm'
import { ChangePasswordForm } from '@/components/citizen/profile/ChangePasswordForm'

// dados simulados — substituir por fetch à API futuramente
const mockProfile: CitizenProfile = {
  id: '1',
  name: 'Maria Lima',
  email: 'maria@email.com',
  cpf: '12345678900',
  phone: '5583999999999',
}

export default function ProfilePage() {
  return (
    <div className="flex flex-col mx-auto gap-6 max-w-2xl">

      <div>
        <h1 className="text-2xl font-bold text-foreground">Meu perfil</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Gerencie seus dados pessoais e senha.
        </p>
      </div>

      <ProfileForm profile={mockProfile} />
      <ChangePasswordForm />

    </div>
  )
}
