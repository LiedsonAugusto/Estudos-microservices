import { Sidebar } from "@/components/admin/Sidebar"
import { ClipboardClock } from "lucide-react"
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="flex min-h-screen bg-white">

      <Sidebar />

      <div className="flex flex-col flex-1 min-w-0">

        {/* Header */}
        <header className="bg-white border-b border-gray-200 flex items-center px-8 py-6">
          <p className="text-sm text-muted-foreground">
            {new Date().toLocaleDateString("pt-BR", {
              weekday: "long",
              day: "2-digit",
              month: "long",
              year: "numeric",
            })}
          </p>
        </header>

        {/* Conteúdo */}
        <main className="flex-1 p-8">
          {children}
        </main>

      </div>

    </div>
  )
}
