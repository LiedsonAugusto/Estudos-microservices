import type { Metadata } from "next"
import { CalendarCheck } from "lucide-react"

export const metadata: Metadata = {
  title: "Agendamento de Serviços Públicos",
  description: "Agende seu atendimento de forma rápida e simples.",
}

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="min-h-screen grid md:grid-cols-2">

      {/* Lado esquerdo — branding (oculto no mobile) */}
      <div className="hidden md:flex flex-col justify-between bg-blue-700 text-white p-12">

        <div className="flex items-center gap-3">
          <CalendarCheck className="w-8 h-8" />
          <span className="text-xl font-semibold">AgendaFácil</span>
        </div>

        <div className="space-y-4">
          <h1 className="text-4xl font-bold leading-tight">
            Agende seu atendimento sem sair de casa.
          </h1>
          <p className="text-blue-200 text-lg">
            Acesse os serviços públicos de forma rápida, simples e sem filas.
          </p>
        </div>

        <p className="text-blue-300 text-sm">
          © {new Date().getFullYear()} AgendaFácil. Todos os direitos reservados.
        </p>

      </div>

      {/* Lado direito — formulário */}
      <div className="flex flex-col items-center justify-center p-8">

        {/* Logo visível apenas no mobile */}
        <div className="flex items-center gap-2 mb-8 md:hidden">
          <CalendarCheck className="w-6 h-6 text-blue-700" />
          <span className="text-lg font-semibold text-blue-700">AgendaFácil</span>
        </div>

        <div className="w-full max-w-sm">
          {children}
        </div>

      </div>

    </div>
  )
}
