1# Sistema de Agendamento de Serviços Públicos - Especificação Técnica

## Visão Geral do Projeto

Este é um projeto fullstack de estudo com foco em **arquitetura de microserviços**, desenvolvido para consolidar conhecimentos em comunicação assíncrona, API REST e desenvolvimento frontend moderno.

O sistema permite que cidadãos agendem atendimentos presenciais em um órgão público, recebam confirmações por email e gerenciem seus agendamentos. Administradores podem gerenciar horários disponíveis, visualizar a agenda e confirmar presenças.

**Nível de complexidade:** Iniciante a Intermediário  
**Objetivo educacional:** Praticar microserviços, mensageria com RabbitMQ, autenticação JWT e frontend com Next.js.

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND                                        │
│                         Next.js 14 (App Router)                              │
│                    http://localhost:3000                                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY                                        │
│                      Spring Cloud Gateway                                    │
│                    http://localhost:8080                                     │
│                                                                              │
│  Responsabilidades:                                                          │
│  - Roteamento de requisições para os microserviços                          │
│  - Validação de tokens JWT                                                   │
│  - Rate limiting                                                             │
│  - CORS                                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
          │                           │                           │
          ▼                           ▼                           ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   USER-SERVICE   │    │SCHEDULING-SERVICE│    │  EMAIL-SERVICE   │
│   Port: 8081     │    │   Port: 8082     │    │   Port: 8083     │
│                  │    │                  │    │                  │
│ - Cadastro       │    │ - CRUD Serviços  │    │ - Consumidor     │
│ - Autenticação   │    │ - CRUD Horários  │    │   RabbitMQ       │
│ - Perfis/Roles   │    │ - Agendamentos   │    │ - Envio SMTP     │
│ - Publicação de  │    │ - Publicação de  │    │ - Templates      │
│   eventos        │    │   eventos        │    │   de email       │
│                  │    │                  │    └──────────────────┘
│ PostgreSQL:5433  │    │ PostgreSQL:5434  │              ▲
└──────────────────┘    └──────────────────┘              │
          │                       │                        │
          └───────────────────────┼────────────────────────┘
                                  ▼
                    ┌──────────────────┐
                    │    RABBITMQ      │
                    │   Port: 5672     │
                    │   Admin: 15672   │
                    │                  │
                    │ Exchanges:       │
                    │ - user.ex        │
                    │ - scheduling.ex  │
                    │                  │
                    │ Queues:          │
                    │ - email.queue    │
                    └──────────────────┘
```

---

## Microserviços - Detalhamento

### 1. USER-SERVICE (Porta 8081)

**Responsabilidade:** Gerenciamento de usuários, autenticação e autorização.

#### Entidades

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false, unique = true)
    private String cpf;  // 11 dígitos, sem formatação
    
    @Column(nullable = false)
    private String password;  // BCrypt hash
    
    @Column(nullable = false)
    private String phone;  // formato: 5583999999999
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // CITIZEN, ADMIN
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private boolean active = true;
}

public enum Role {
    CITIZEN,  // Cidadão comum - pode agendar
    ADMIN     // Administrador - gerencia horários e serviços
}
```

#### Endpoints REST

```
POST   /api/auth/register     - Cadastro de novo usuário (público)
POST   /api/auth/login        - Autenticação, retorna JWT (público)
POST   /api/auth/refresh      - Refresh do token JWT
GET    /api/users/me          - Dados do usuário logado
PUT    /api/users/me          - Atualizar próprios dados
GET    /api/users             - Listar usuários (ADMIN)
GET    /api/users/{id}        - Buscar usuário por ID (ADMIN)
PUT    /api/users/{id}/status - Ativar/desativar usuário (ADMIN)
```

#### DTOs

```java
// Request
record RegisterRequest(String name, String email, String cpf, String password, String phone) {}
record LoginRequest(String email, String password) {}
record UpdateUserRequest(String name, String phone) {}

// Response
record UserResponse(UUID id, String name, String email, String cpf, String phone, Role role, LocalDateTime createdAt) {}
record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
```

#### Eventos RabbitMQ (Publicados)

```java
// Evento publicado quando novo usuário é registrado
record UserCreatedEvent(
    UUID userId,
    String name,
    String email,
    LocalDateTime timestamp
) {}
```

#### Regras de Negócio
- CPF deve ser válido (algoritmo de validação)
- Email deve ser único no sistema
- Senha mínimo 8 caracteres
- Apenas ADMIN pode listar todos os usuários
- Usuário não pode alterar seu próprio role
- Ao registrar um usuário, publicar evento `UserCreatedEvent` no RabbitMQ

---

### 2. SCHEDULING-SERVICE (Porta 8082)

**Responsabilidade:** Gerenciamento de serviços, horários disponíveis e agendamentos.

#### Entidades

```java
// Service.java (Tipo de serviço oferecido)
@Entity
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;  // Ex: "Renovação de CNH", "Primeira Via"
    
    private String description;
    
    @Column(nullable = false)
    private Integer durationMinutes;  // Duração estimada do atendimento
    
    @Column(nullable = false)
    private boolean active = true;
    
    private LocalDateTime createdAt;
}

// TimeSlot.java (Horário disponível para agendamento)
@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private Integer capacity;  // Quantos atendimentos simultâneos
    
    @Column(nullable = false)
    private Integer bookedCount = 0;  // Quantos já agendados
    
    @Column(nullable = false)
    private boolean active = true;
}

// Appointment.java (Agendamento realizado)
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;  // Referência ao user-service
    
    @Column(nullable = false)
    private String userEmail;  // Desnormalizado para o email-service
    
    @Column(nullable = false)
    private String userName;  // Desnormalizado para o email-service
    
    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    
    private String notes;  // Observações do cidadão
    
    private String cancellationReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Column(unique = true, nullable = false)
    private String confirmationCode;  // Código de 8 caracteres para check-in
}

public enum AppointmentStatus {
    SCHEDULED,   // Agendado, aguardando
    CONFIRMED,   // Cidadão fez check-in
    COMPLETED,   // Atendimento realizado
    CANCELLED,   // Cancelado pelo cidadão
    NO_SHOW      // Cidadão não compareceu
}
```

#### Endpoints REST

```
# Serviços (ADMIN)
POST   /api/services                    - Criar serviço
GET    /api/services                    - Listar serviços (público)
GET    /api/services/{id}               - Buscar serviço
PUT    /api/services/{id}               - Atualizar serviço
DELETE /api/services/{id}               - Desativar serviço

# Horários (ADMIN)
POST   /api/time-slots                  - Criar horário
POST   /api/time-slots/batch            - Criar horários em lote
GET    /api/time-slots                  - Listar horários (filtros: serviceId, date, available)
GET    /api/time-slots/{id}             - Buscar horário
PUT    /api/time-slots/{id}             - Atualizar horário
DELETE /api/time-slots/{id}             - Desativar horário

# Agendamentos
POST   /api/appointments                - Criar agendamento (CITIZEN)
GET    /api/appointments                - Listar agendamentos (ADMIN: todos, CITIZEN: próprios)
GET    /api/appointments/{id}           - Buscar agendamento
GET    /api/appointments/code/{code}    - Buscar por código de confirmação
PUT    /api/appointments/{id}/cancel    - Cancelar agendamento (CITIZEN próprio ou ADMIN)
PUT    /api/appointments/{id}/confirm   - Check-in do cidadão (ADMIN)
PUT    /api/appointments/{id}/complete  - Marcar como atendido (ADMIN)
PUT    /api/appointments/{id}/no-show   - Marcar como não compareceu (ADMIN)
```

#### DTOs

```java
// Requests
record CreateServiceRequest(String name, String description, Integer durationMinutes) {}
record CreateTimeSlotRequest(UUID serviceId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer capacity) {}
record CreateTimeSlotsInBatchRequest(UUID serviceId, LocalDate startDate, LocalDate endDate, List<DayOfWeek> daysOfWeek, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes, Integer capacity) {}
record CreateAppointmentRequest(UUID timeSlotId, String notes) {}
record CancelAppointmentRequest(String reason) {}

// Responses
record ServiceResponse(UUID id, String name, String description, Integer durationMinutes, boolean active) {}
record TimeSlotResponse(UUID id, ServiceResponse service, LocalDate date, LocalTime startTime, LocalTime endTime, Integer capacity, Integer availableSpots, boolean active) {}
record AppointmentResponse(UUID id, UUID userId, String userName, TimeSlotResponse timeSlot, AppointmentStatus status, String notes, String confirmationCode, LocalDateTime createdAt) {}
```

#### Eventos RabbitMQ (Publicados)

```java
// Evento publicado quando agendamento é criado
record AppointmentCreatedEvent(
    UUID appointmentId,
    String userEmail,
    String userName,
    String serviceName,
    LocalDate date,
    LocalTime time,
    String confirmationCode,
    LocalDateTime timestamp
) {}

// Evento publicado quando agendamento é cancelado
record AppointmentCancelledEvent(
    UUID appointmentId,
    String userEmail,
    String userName,
    String serviceName,
    LocalDate date,
    LocalTime time,
    String reason,
    LocalDateTime timestamp
) {}

// Evento publicado quando falta 24h para o agendamento (job agendado)
record AppointmentReminderEvent(
    UUID appointmentId,
    String userEmail,
    String userName,
    String serviceName,
    LocalDate date,
    LocalTime time,
    String confirmationCode,
    LocalDateTime timestamp
) {}
```

#### Regras de Negócio
- Cidadão só pode agendar se houver vaga disponível (bookedCount < capacity)
- Cidadão não pode ter dois agendamentos no mesmo horário
- Cidadão não pode ter agendamento duplicado para o mesmo serviço no mesmo dia
- Cancelamento só é permitido até 24h antes do horário
- Código de confirmação é gerado automaticamente (8 caracteres alfanuméricos)
- Ao criar agendamento, incrementar bookedCount do TimeSlot
- Ao cancelar, decrementar bookedCount

---

### 3. EMAIL-SERVICE (Porta 8083)

**Responsabilidade:** Consumir eventos do RabbitMQ e enviar emails.

**Status Atual:** Implementação parcial - apenas eventos de usuário criado.

#### Dependências (pom.xml)

```xml
<!-- RabbitMQ -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

#### Configuração RabbitMQ (Implementado)

```java
// Exchange e Queues ATUAIS
Exchange: user.exchange (topic)
Queue: email.queue
Bindings:
  - user.created -> email.queue

// TODO: Implementar no futuro
Exchange: scheduling.exchange (topic)
Bindings futuros:
  - appointment.created -> email.queue
  - appointment.cancelled -> email.queue
  - appointment.reminder -> email.queue
```

#### Consumidores (Implementado)

```java
@Component
public class UserEventConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleUserCreated(UserCreatedEvent event) {
        // Envia email de boas-vindas usando SimpleMailMessage
        emailService.sendEmailToUserCreated(event);
    }
}

// TODO: Implementar consumidores para eventos de agendamento
// - handleAppointmentCreated()
// - handleAppointmentCancelled()
// - handleAppointmentReminder()
```

#### Estrutura de Evento Atual

```java
public record UserCreatedEvent(
    UUID userId,
    String name,
    String email,
    LocalDateTime timestamp
) {}
```

#### Email Service (Implementado)

```java
@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender mailSender;

    public void sendEmailToUserCreated(UserCreatedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.email());
        message.setSubject("Bem-vindo ao Sistema de Agendamentos!");
        message.setText("Olá " + event.name() + ",\n\n" +
            "Bem-vindo ao nosso sistema de agendamentos!\n\n" +
            "Agora você pode agendar seus atendimentos de forma rápida e prática.\n\n" +
            "Atenciosamente,\n" +
            "Equipe de Agendamentos");
        mailSender.send(message);
    }

    // TODO: Adicionar métodos para outros tipos de email
}
```

#### Templates de Email (Implementado)

**Status:** Atualmente usando texto simples (SimpleMailMessage)

1. **Boas-vindas** ✅ IMPLEMENTADO
   - Assunto: "Bem-vindo ao Sistema de Agendamentos!"
   - Corpo: Mensagem de texto simples com saudação e instruções básicas

**TODO - Templates futuros:**

2. **Confirmação de Agendamento** ❌ NÃO IMPLEMENTADO
   - Assunto: "Agendamento Confirmado - {serviceName}"
   - Corpo: Nome do cidadão, serviço, data/hora, código de confirmação, instruções

3. **Cancelamento** ❌ NÃO IMPLEMENTADO
   - Assunto: "Agendamento Cancelado - {serviceName}"
   - Corpo: Confirmação do cancelamento, orientações para novo agendamento

4. **Lembrete** ❌ NÃO IMPLEMENTADO
   - Assunto: "Lembrete: Seu agendamento é amanhã!"
   - Corpo: Detalhes do agendamento, código de confirmação, documentos necessários

#### Configuração SMTP

**Ambiente Atual:** Produção com Gmail

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=liedsonaugusto@gmail.com
spring.mail.password=<app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Alternativa para Desenvolvimento:**
- Usar MailHog ou Mailtrap para testes
- MailHog: porta SMTP 1025, web interface 8025

#### Configuração RabbitMQ

**Ambiente Atual:** CloudAMQP (RabbitMQ hospedado na nuvem)

```properties
spring.rabbitmq.addresses=amqps://[user]:[pass]@[host]/[vhost]

rabbitmq.exchange.user=user.exchange
rabbitmq.routingkey.user.created=user.created
rabbitmq.queue.email=email.queue
```

**Alternativa para Desenvolvimento:**
- RabbitMQ local via Docker Compose (porta 5672, management 15672)

---

## API Gateway - Configuração

```yaml
# application.yml do Gateway
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/**, /api/users/**
        
        - id: scheduling-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/services/**, /api/time-slots/**, /api/appointments/**
      
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin

  # CORS
  web:
    cors:
      allowed-origins: "http://localhost:3000"
      allowed-methods: "*"
      allowed-headers: "*"
```

---

## Frontend - Next.js 14

### Estrutura de Pastas

```
frontend/
├── src/
│   ├── app/
│   │   ├── (public)/
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   └── register/
│   │   │       └── page.tsx
│   │   ├── (protected)/
│   │   │   ├── layout.tsx          # Layout com verificação de auth
│   │   │   ├── dashboard/
│   │   │   │   └── page.tsx        # Dashboard do cidadão
│   │   │   ├── appointments/
│   │   │   │   ├── page.tsx        # Lista de agendamentos
│   │   │   │   ├── new/
│   │   │   │   │   └── page.tsx    # Novo agendamento
│   │   │   │   └── [id]/
│   │   │   │       └── page.tsx    # Detalhes do agendamento
│   │   │   └── admin/
│   │   │       ├── layout.tsx      # Verificação de role ADMIN
│   │   │       ├── services/
│   │   │       │   └── page.tsx    # Gerenciar serviços
│   │   │       ├── time-slots/
│   │   │       │   └── page.tsx    # Gerenciar horários
│   │   │       └── appointments/
│   │   │           └── page.tsx    # Ver todos os agendamentos
│   │   ├── layout.tsx
│   │   └── page.tsx                # Landing page
│   ├── components/
│   │   ├── ui/                     # Componentes base (Button, Input, etc)
│   │   ├── forms/                  # Formulários
│   │   ├── calendar/               # Componente de calendário
│   │   └── layout/                 # Header, Sidebar, etc
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   └── useAppointments.ts
│   ├── lib/
│   │   ├── api.ts                  # Configuração do fetch/axios
│   │   └── utils.ts
│   ├── types/
│   │   └── index.ts                # Interfaces TypeScript
│   └── context/
│       └── AuthContext.tsx
├── package.json
├── tailwind.config.js
└── next.config.js
```

### Bibliotecas Sugeridas
- **UI:** Tailwind CSS + shadcn/ui
- **Formulários:** React Hook Form + Zod
- **Data Fetching:** TanStack Query (React Query)
- **Calendário:** react-day-picker ou date-fns
- **HTTP Client:** Axios ou fetch nativo
- **Ícones:** Lucide React

### Fluxos de Tela

1. **Cidadão não autenticado:**
   - Landing → Login/Cadastro

2. **Cidadão autenticado:**
   - Dashboard (próximos agendamentos)
   - Novo Agendamento (selecionar serviço → data → horário → confirmar)
   - Meus Agendamentos (lista com filtros)
   - Detalhes do Agendamento (cancelar)

3. **Administrador:**
   - Dashboard com métricas
   - Gerenciar Serviços (CRUD)
   - Gerenciar Horários (criação em lote por período)
   - Agenda do Dia (lista de agendamentos, check-in, marcar atendido)

---

## Stack Tecnológica Completa

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Backend Runtime | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.2 |
| API Gateway | Spring Cloud Gateway | 4.1+ |
| Segurança | Spring Security + JWT | - |
| Mensageria | RabbitMQ (CloudAMQP) | Hospedado na nuvem |
| Banco de Dados | PostgreSQL | 16 |
| Migrations | Flyway | 9+ |
| Frontend Runtime | Node.js | 20 (LTS) |
| Framework Frontend | Next.js | 14 (App Router) |
| Estilização | Tailwind CSS | 3.4+ |
| Linguagem Frontend | TypeScript | 5+ |
| Containerização | Docker + Docker Compose | - |
| Testes Backend | JUnit 5 + Mockito + Testcontainers | - |
| Testes Frontend | Jest + React Testing Library | - |

---

## Docker Compose - Desenvolvimento

**Status Atual:** RabbitMQ e SMTP estão sendo usados em serviços hospedados na nuvem.

### Configuração Atual (Cloud)

- **RabbitMQ:** CloudAMQP (https://www.cloudamqp.com/)
- **SMTP:** Gmail SMTP

### Configuração Alternativa Local (Recomendada para desenvolvimento)

```yaml
version: '3.8'

services:
  postgres-users:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: users_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres_users_data:/var/lib/postgresql/data

  postgres-scheduling:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: scheduling_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5434:5432"
    volumes:
      - postgres_scheduling_data:/var/lib/postgresql/data

  # OPCIONAL: RabbitMQ local (atualmente usando CloudAMQP)
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  # OPCIONAL: MailHog para testes (atualmente usando Gmail)
  mailhog:
    image: mailhog/mailhog
    ports:
      - "1025:1025"   # SMTP
      - "8025:8025"   # Web UI

volumes:
  postgres_users_data:
  postgres_scheduling_data:
  rabbitmq_data:
```

**Nota:** Para usar RabbitMQ e SMTP locais, altere as configurações em `application.properties`:

```properties
# RabbitMQ Local
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# MailHog Local
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

---

## Estrutura de Repositórios

Recomendação: **Monorepo** para facilitar o desenvolvimento e estudo.

```
scheduling-system/
├── docker-compose.yml
├── README.md
├── backend/
│   ├── api-gateway/
│   │   ├── src/
│   │   └── pom.xml
│   ├── user-service/
│   │   ├── src/
│   │   └── pom.xml
│   ├── scheduling-service/
│   │   ├── src/
│   │   └── pom.xml
│   ├── email-service/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/estudo/emailService/
│   │   │   │   │   ├── configs/
│   │   │   │   │   │   └── RabbitMQConfig.java
│   │   │   │   │   ├── consumers/
│   │   │   │   │   │   └── UserEventConsumer.java
│   │   │   │   │   ├── dtos/
│   │   │   │   │   │   └── UserCreatedEvent.java
│   │   │   │   │   ├── services/
│   │   │   │   │   │   └── EmailService.java
│   │   │   │   │   └── EmailServiceApplication.java
│   │   │   │   └── resources/
│   │   │   │       └── application.properties
│   │   │   └── test/
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   └── PROJECT_SPEC.md
│   └── pom.xml (parent POM - TODO)
└── frontend/
    ├── src/
    ├── package.json
    └── next.config.js
```

### Estrutura Atual do Email Service

```
emailService/
├── src/
│   ├── main/
│   │   ├── java/com/estudo/emailService/
│   │   │   ├── configs/
│   │   │   │   └── RabbitMQConfig.java         # Configuração de exchange, queue e bindings
│   │   │   ├── consumers/
│   │   │   │   └── UserEventConsumer.java      # Listener para eventos de usuário
│   │   │   ├── dtos/
│   │   │   │   └── UserCreatedEvent.java       # Record do evento
│   │   │   ├── services/
│   │   │   │   └── EmailService.java           # Lógica de envio de email
│   │   │   └── EmailServiceApplication.java    # Classe principal
│   │   └── resources/
│   │       └── application.properties          # Configurações do serviço
│   └── test/
│       └── java/com/estudo/emailService/
│           └── EmailServiceApplicationTests.java
├── pom.xml                                      # Dependências Maven
├── mvnw / mvnw.cmd                             # Maven Wrapper
└── PROJECT_SPEC.md                             # Esta especificação
```

---

## Ordem de Implementação Sugerida

### Fase 1 - Fundação
1. Configurar Docker Compose com PostgreSQL, RabbitMQ e MailHog
2. Criar estrutura do user-service com autenticação JWT
3. Testar fluxo de registro e login

### Fase 2 - Core
4. Criar scheduling-service com CRUD de serviços e horários
5. Implementar criação de agendamentos
6. Configurar publicação de eventos no RabbitMQ

### Fase 3 - Integração
7. Criar email-service consumindo eventos
8. Implementar templates de email
9. Configurar API Gateway

### Fase 4 - Frontend
10. Setup Next.js com autenticação
11. Telas públicas (login, cadastro)
12. Dashboard e fluxo de agendamento
13. Área administrativa

### Fase 5 - Melhorias
14. Testes automatizados
15. Documentação com Swagger/OpenAPI
16. Job para envio de lembretes

---

## Comandos Úteis

```bash
# Subir infraestrutura
docker-compose up -d

# Logs do RabbitMQ
docker-compose logs -f rabbitmq

# Acessar RabbitMQ Management
# http://localhost:15672 (guest/guest)

# Acessar MailHog
# http://localhost:8025

# Build de todos os serviços (do diretório backend/)
./mvnw clean package -DskipTests

# Rodar um serviço específico
cd user-service && ./mvnw spring-boot:run

# Frontend
cd frontend && npm run dev
```

---

## Status de Implementação - Email Service

### ✅ Implementado

1. **Estrutura Base**
   - Spring Boot 4.0.2 configurado
   - Java 21
   - Maven como build tool
   - Lombok para redução de boilerplate

2. **RabbitMQ**
   - Conexão com CloudAMQP
   - Queue `email.queue` criada e configurada
   - Exchange `user.exchange` (topic) configurado
   - Binding `user.created` -> `email.queue`
   - Conversão JSON automática com Jackson

3. **Consumidor de Eventos**
   - `UserEventConsumer` implementado
   - Processamento de eventos `UserCreatedEvent`
   - Logging detalhado de eventos recebidos
   - Tratamento de erros com try-catch

4. **Envio de Emails**
   - Integração com Gmail SMTP
   - Email de boas-vindas em texto simples
   - Configuração STARTTLS e autenticação

### ❌ Pendente / TODO

1. **Eventos de Agendamento**
   - Consumidor para `AppointmentCreatedEvent`
   - Consumidor para `AppointmentCancelledEvent`
   - Consumidor para `AppointmentReminderEvent`
   - Exchange `scheduling.exchange`

2. **Templates de Email**
   - Migrar de texto simples para HTML
   - Template engine (Thymeleaf ou FreeMarker)
   - Templates responsivos
   - Personalização avançada

3. **Melhorias**
   - Retry policy para falhas de envio
   - Dead Letter Queue (DLQ)
   - Persistência de histórico de emails enviados
   - Testes unitários e integração
   - Docker Compose local para desenvolvimento

4. **Segurança**
   - Remover credenciais do `application.properties`
   - Usar variáveis de ambiente
   - Implementar configuração por profiles (dev, prod)

---

## Contexto Adicional para LLMs

**Desenvolvedor:** Liedson
**Nível:** Estudando microserviços, experiência prévia com PHP/MySQL, familiaridade com Docker
**Objetivo:** Projeto de estudo para consolidar conhecimentos em arquitetura distribuída
**Ambiente:** Windows, desenvolvimento usando serviços cloud (CloudAMQP, Gmail)
**Preferências:** Código limpo, boas práticas, explicações didáticas quando necessário

**Email Service - Contexto Atual:**
- Primeiro microserviço da arquitetura a ser implementado
- Foco inicial em comunicação assíncrona com RabbitMQ
- Usando serviços hospedados para acelerar o desenvolvimento
- Implementação incremental: começou com eventos de usuário, expandirá para agendamentos

Ao ajudar neste projeto, considere:
- Explicar o "porquê" das decisões arquiteturais
- Sugerir boas práticas de Spring Boot e Next.js
- Apontar possíveis armadilhas em comunicação assíncrona
- Manter consistência com as definições deste documento
- O email-service está em implementação parcial - muitas features ainda serão adicionadas
