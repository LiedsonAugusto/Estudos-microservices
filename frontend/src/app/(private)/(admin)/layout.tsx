import { Sidebar } from "@/components/admin/Sidebar"
import { ClipboardClock } from "lucide-react"
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <div className="flex h-screen overflow-hidden bg-background">

      <Sidebar />

      <div className="flex flex-col flex-1 min-w-0">

        {/* Header */}
        <header className="shrink-0 bg-background border-b border-border flex items-center px-8 py-6">
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
        <main className="flex-1 overflow-y-auto p-8">
          {children}
        </main>

      </div>

    </div>
  )
}
